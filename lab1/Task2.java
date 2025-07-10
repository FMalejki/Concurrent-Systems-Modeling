public class Task2 {
    public static void main(String[] args) {
        Buffer buffer = new Buffer();

        Thread producer = new Thread(() -> {
            try {
                for (int i = 0; i < 10; i++) {
                    buffer.produce();
                    System.out.println("Produced item " + i);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        Thread consumer = new Thread(() -> {
            try {
                for (int i = 0; i < 10; i++) {
                    buffer.consume();
                    System.out.println("Consumed item " + i);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        producer.start();
        consumer.start();
    }
}

class Buffer {
    private boolean full = false;

    public synchronized void produce() throws InterruptedException {
        while (full) {
            wait();
        }
        full = true;
        notify();
    }

    public synchronized void consume() throws InterruptedException {
        while (!full) {
            wait();
        }
        full = false;
        notify();
    }
}
