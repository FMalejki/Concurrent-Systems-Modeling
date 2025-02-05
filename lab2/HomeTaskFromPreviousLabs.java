import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class HomeTaskFromPreviousLabs {
    static final int M = 5;
    static final int N = 3;

    public static void main(String[] args) {
        Buffer buffer = new Buffer();

        for (int i = 1; i <= M; i++) {
            int id = i;
            new Thread(() -> {
                try {
                    
                    while (true) {
                        buffer.produce("Item-from P" + id);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "Producer-" + i).start();
        }

        for (int i = 1; i <= N; i++) {
            int id = i;
            new Thread(() -> {
                try {
                    while (true) {
                        String item = buffer.consume();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "Consumer-" + i).start();
        }
    }
}

class Buffer {
    private String item = null;
    private boolean hasItem = false;

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notEmpty = lock.newCondition();
    private final Condition notFull = lock.newCondition();

    public void produce(String newItem) throws InterruptedException {
        lock.lock();
        try {
            while (hasItem) {
                System.out.println(Thread.currentThread().getName() + " waiting to produce...");
                notFull.await();
            }
            item = newItem;
            hasItem = true;
            System.out.println(Thread.currentThread().getName() + " produced: " + newItem);

            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    public String consume() throws InterruptedException {
        lock.lock();
        try {
            while (!hasItem) {
                System.out.println(Thread.currentThread().getName() + " waiting to consume...");
                notEmpty.await();
            }
            String consumed = item;
            item = null;
            hasItem = false;
            System.out.println(Thread.currentThread().getName() + " consumed: " + consumed);

            notFull.signal();
            return consumed;
        } finally {
            lock.unlock();
        }
    }
}
