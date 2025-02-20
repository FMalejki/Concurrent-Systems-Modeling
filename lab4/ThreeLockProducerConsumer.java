import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.atomic.AtomicInteger;


public class ThreeLockProducerConsumer {
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
                        int batch = 1; //ThreadLocalRandom.current().nextInt(1, 6);
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
                        Thread.sleep(1);
                    }
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
        long starvationThreshold = RUN_MS;
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
    private final int capacity;
    private int in = 0;
    private int out = 0;
    private int count = 0;
    private final ConcurrentHashMap<String, Stats> stats;

    private final ReentrantLock prodLock = new ReentrantLock();
    private final ReentrantLock consLock = new ReentrantLock();
    private final ReentrantLock bufferLock = new ReentrantLock();
    private final Condition available = bufferLock.newCondition();

    public Buffer(int capacity, ConcurrentHashMap<String, Stats> stats) {
        this.capacity = capacity;
        buffer = new String[capacity];
        this.stats = stats;
    }

    public void produce(String item) throws InterruptedException {
        prodLock.lock();
        try {
            bufferLock.lock();
            try {
                while (count == capacity) {
                    available.await();
                }

                buffer[in] = item;
                in = (in + 1) % capacity;
                count++;
                System.out.println(Thread.currentThread().getName() + " produced: " + item);

                Stats s = stats.get(Thread.currentThread().getName());
                if (s != null) s.touch();

                available.signal();
            } finally {
                bufferLock.unlock();
            }
        } finally {
            prodLock.unlock();
        }
    }

    public String consume() throws InterruptedException {
        consLock.lock();
        try {
            bufferLock.lock();
            try {
                while (count == 0) {
                    available.await();
                }

                String item = buffer[out];
                out = (out + 1) % capacity;
                count--;
                System.out.println(Thread.currentThread().getName() + " consumed: " + item);

                Stats s = stats.get(Thread.currentThread().getName());
                if (s != null) s.touch();

                available.signal();
                return item;
            } finally {
                bufferLock.unlock();
            }
        } finally {
            consLock.unlock();
        }
    }

    public void shutdownWake() {
        bufferLock.lock();
        try {
            available.signalAll();
        } finally {
            bufferLock.unlock();
        }
    }
}
