import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class PK_NoStarvation {
    static final int M = 6;                 // producenci
    static final int N = 4;                 // konsumenci
    static final int BUFFER_SIZE = 10;      // bufor size
    static final long RUN_MS = 5_000;      // czas

    static volatile boolean running = true;

    public static void main(String[] args) throws InterruptedException {
        ConcurrentHashMap<String, Stats> stats = new ConcurrentHashMap<>();
        Buffer buffer = new Buffer(BUFFER_SIZE, stats);

        List<Thread> threads = new ArrayList<>();

        // Producer
        for (int i = 1; i <= M; i++) {
            String name = "Producer-" + i;
            stats.put(name, new Stats(name, Role.PRODUCER));
            int id = i;

            Thread t = new Thread(() -> {
                try {
                    int counter = 1;
                    while (running) {
                        int batch = 1; //ThreadLocalRandom.current().nextInt(1, 6); // 1 do 6
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

        // Consumer
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

        // Sleep (dzialanie programu)
        Thread.sleep(RUN_MS);
        running = false;
        buffer.shutdownWake();

        for (Thread t : threads) {
            t.join(200);
        }

        // Stats
        System.out.println("\n--- STATYSTYKI ---");
        long now = System.currentTimeMillis();
        long starvationThreshold = RUN_MS;

        for (Stats s : stats.values()) {
            long inactive = now - s.lastActive;
            String starv = "";
            if ((s.count.get() == 0) || (inactive > starvationThreshold)) {
                starv = "  <-- POSSIBLE STARVATION (inactive ms=" + inactive + ")";
            }
            System.out.printf("%-12s type=%-9s count=%5d lastActive=%6dms%s%n",
                    s.name,
                    s.role,
                    s.count.get(),
                    inactive,
                    starv);
        }
    }
}

// Role enum
enum Role { PRODUCER, CONSUMER }

// Stats
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

// Buffer
class Buffer {
    private final String[] buffer;
    private final int capacity;
    private int in = 0;
    private int out = 0;
    private int count = 0;
    private final ConcurrentHashMap<String, Stats> stats;

    private final ReentrantLock lock = new ReentrantLock();

    // 4 condition variables
    private final Condition firstProd = lock.newCondition();
    private final Condition restProd  = lock.newCondition();
    private final Condition firstCons = lock.newCondition();
    private final Condition restCons  = lock.newCondition();

    private boolean prodWaiting = false;
    private boolean consWaiting = false;

    public Buffer(int capacity, ConcurrentHashMap<String, Stats> stats) {
        this.capacity = capacity;
        buffer = new String[capacity];
        this.stats = stats;
    }

    public void produce(String item) throws InterruptedException {
        lock.lock();
        try {
            // Warunek czekania REST producerow
            while (prodWaiting) {
                restProd.await();
            }
            prodWaiting = true;

            // warunek czekania FIRST producera
            while (count == capacity) {
                firstProd.await();
            }

            buffer[in] = item;
            in = (in + 1) % capacity;
            count++;

            System.out.println(Thread.currentThread().getName() + " produced: " + item);

            Stats s = stats.get(Thread.currentThread().getName());
            if (s != null) s.touch();

            // Pierwszy konsument jest budzony (zmiana flagi)
            prodWaiting = false;
            // Sygnal dla reszty producentow
            restProd.signal();
            // Sygnal dla pierwszego konsumenta (unikamy sytuacji z hasWaiters())
            firstCons.signal();
        } finally {
            lock.unlock();
            // Useless
            //Thread.sleep(1);
        }
    }

    public String consume() throws InterruptedException {
        lock.lock();
        try {
            // Tylko jeden „pierwszy” konsument może czekać
            while (consWaiting) {
                restCons.await();
            }
            consWaiting = true;

            // Jeśli bufor pusty, czekaj
            while (count == 0) {
                firstCons.await();
            }

            String item = buffer[out];
            out = (out + 1) % capacity;
            count--;

            System.out.println(Thread.currentThread().getName() + " consumed: " + item);

            Stats s = stats.get(Thread.currentThread().getName());
            if (s != null) s.touch();

            // pierwszy producent budzony (flaga)
            consWaiting = false;
            // reszta konsumentow signal()
            restCons.signal();
            // pierwszy producent signal()
            firstProd.signal();

            return item;
        } finally {
            lock.unlock();
            // Useless
            //Thread.sleep(1);
        }
    }

    // zakonczenie dzialania, budzenie wszystkich watkow
    public void shutdownWake() {
        lock.lock();
        try {
            firstProd.signalAll();
            restProd.signalAll();
            firstCons.signalAll();
            restCons.signalAll();
        } finally {
            lock.unlock();
        }
    }
}
