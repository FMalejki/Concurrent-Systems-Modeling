import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.CountDownLatch;;;

public class RaceConditionTestAtomic {
    static AtomicInteger counter = new AtomicInteger(0);

    public static void main(String[] args) throws InterruptedException {
        int N = 10;
        int X = 1000;
        Thread[] threads = new Thread[N];

        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch endGate = new CountDownLatch(N);

        for (int i = 0; i < N; i++) {
            if (i < N / 2) {
                threads[i] = new Thread(() -> {
                    try {
                        startGate.await();
                        for (int j = 0; j < X; j++) {
                            counter.incrementAndGet();  // atomowe ++
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        endGate.countDown();
                    }
                });
            } else {
                threads[i] = new Thread(() -> {
                    try {
                        startGate.await();
                        for (int j = 0; j < X; j++) {
                            counter.decrementAndGet();  // atomowe --
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        endGate.countDown();
                    }
                });
            }
            threads[i].start();
        }

        startGate.countDown();
        endGate.await();

        System.out.println("Wynik końcowy: " + counter.get());
    }
}
