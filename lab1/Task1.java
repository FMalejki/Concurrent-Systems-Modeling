public class Task1 {
    public static void main(String[] args) {
        Buffer buffer = new Buffer();

        Thread producer = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                while (buffer.full) {
                }
                buffer.full = true;
                System.out.println("Produced " + i);
            }
        });

        Thread consumer = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                while (!buffer.full) {
                }
                buffer.full = false;
                System.out.println("Consumed " + i);
            }
        });

        producer.start();
        consumer.start();
    }
}

class Buffer {
    public volatile boolean full = false;
}

