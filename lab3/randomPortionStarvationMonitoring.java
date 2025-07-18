import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.ArrayList;
import java.util.List;

public class randomPortionStarvationMonitoring {
    static final int M = 6;
    static final int N = 4;
    static final int BUFFER_SIZE = 10;
    static final long RUN_MS = 5_000;

    static volatile boolean running = true;

    public static void main(String[] args) throws InterruptedException {
        ConcurrentHashMap<String, Stats> stats = new ConcurrentHashMap<>();
        Buffer buffer = new Buffer(BUFFER_SIZE, stats);

        List<Thread> threads = new ArrayList<>();

        for (int i = 1; i <= M; i++) {
            String name = "Producer-" + i;
            stats.put(name, new Stats(name, Role.PRODUCER));
            int id = i;
            Thread t = new Thread(() -> {
                try {
                    int counter = 1;
                    while (running) {
                        int batch = 1;//ThreadLocalRandom.current().nextInt(1, 6);
                        for (int j = 0; j < batch && running; j++) {
                            buffer.produce("P" + id + "-item-" + (counter++));
                        }
                        Thread.sleep(1);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, name);
            t.start();
            threads.add(t);
        }

        for (int i = 1; i <= N; i++) {
            String name = "Consumer-" + i;
            stats.put(name, new Stats(name, Role.CONSUMER));
            Thread t = new Thread(() -> {
                try {
                    while (running) {
                        buffer.consume();
                    }
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, name);
            t.start();
            threads.add(t);
        }

        Thread.sleep(RUN_MS);
        running = false;

        buffer.shutdownWake();

        for (Thread t : threads) {
            t.join(200);
        }

        System.out.println("\n--- STATYSTYKI ---");
        long now = System.currentTimeMillis();
        long starvationThreshold = RUN_MS / 1000;
        for (Stats s : stats.values()) {
            String role = s.role == Role.PRODUCER ? "P" : "C";
            String starv = "";
            long inactive = now - s.lastActive;
            if ((s.count.get() == 0) || (inactive > starvationThreshold)) {
                starv = "  <-- POSSIBLE STARVATION (inactive ms=" + inactive + ")";
            }
            System.out.printf("%-12s type=%-8s count=%4d lastActive=%4dms%s%n",
                    s.name,
                    s.role,
                    s.count.get(),
                    inactive,
                    starv);
        }
    }
}

enum Role { PRODUCER, CONSUMER }

class Stats {
    final String name;
    final Role role;
    final AtomicInteger count = new AtomicInteger(0);
    volatile long lastActive = System.currentTimeMillis();

    Stats(String name, Role role) {
        this.name = name;
        this.role = role;
    }

    void touch() {
        count.incrementAndGet();
        lastActive = System.currentTimeMillis();
    }
}

class Buffer {
    private final String[] buffer;
    private int in = 0;
    private int out = 0;
    private int count = 0;

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notEmpty = lock.newCondition();
    private final Condition notFull = lock.newCondition();

    private final ConcurrentHashMap<String, Stats> stats;

    public Buffer(int size, ConcurrentHashMap<String, Stats> stats) {
        buffer = new String[size];
        this.stats = stats;
    }

    public void produce(String newItem) throws InterruptedException {
        lock.lock();
        try {
            while (count == buffer.length && randomPortionStarvationMonitoring.running) {
                System.out.println(Thread.currentThread().getName() + " waiting to produce...");
                notFull.await();
            }
            if (!randomPortionStarvationMonitoring.running && count == buffer.length) return;
            buffer[in] = newItem;
            in = (in + 1) % buffer.length;
            count++;
            System.out.println(Thread.currentThread().getName() + " produced: " + newItem);

            Stats s = stats.get(Thread.currentThread().getName());
            if (s != null) s.touch();

            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    public String consume() throws InterruptedException {
        lock.lock();
        try {
            while (count == 0 && randomPortionStarvationMonitoring.running) {
                System.out.println(Thread.currentThread().getName() + " waiting to consume...");
                notEmpty.await();
            }
            if (!randomPortionStarvationMonitoring.running && count == 0) return null;
            String consumed = buffer[out];
            out = (out + 1) % buffer.length;
            count--;
            System.out.println(Thread.currentThread().getName() + " consumed: " + consumed);

            Stats s = stats.get(Thread.currentThread().getName());
            if (s != null) s.touch();

            notFull.signal();
            return consumed;
        } finally {
            lock.unlock();
        }
    }

    public void shutdownWake() {
        lock.lock();
        try {
            notEmpty.signalAll();
            notFull.signalAll();
        } finally {
            lock.unlock();
        }
    }
}