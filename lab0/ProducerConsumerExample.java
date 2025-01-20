public class ProducerConsumerExample {
    public static void main(String[] args) {

        Buffer buffer = new Buffer();

        Thread producer = new Thread(() -> {
            try {
                for (int i = 0; i < 10; i++) {
                    buffer.produce(1);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        

        Thread consumer = new Thread(() -> {
            try {
                for (int i = 0; i < 10; i++) {
                    buffer.consume();
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
    private int item;
    private boolean full = false;
    private int inc1 = 0;
    private int inc2 = 0;

    public synchronized void produce(int value) throws InterruptedException {
        while (full) {
            wait();
            System.out.println("Not produced, buffer full");
            inc1++;
        }
        item = value;
        full = true;
        System.out.println("Produced: " + value + " " + inc1);
        inc1 = 0;
        notify();
    }

    public synchronized void consume() throws InterruptedException {
        while (!full) {
            wait();
            System.out.println("Not consumed, buffer empty");
            inc2++;
        }
        int value = item;
        full = false;
        System.out.println("Consumed: " + value + " " + inc2);
        inc2 = 0;
        notify();
    }
}

