public class HomeTask3 {
    public static void main(String[] args) {
        Buffer buffer = new Buffer();

        Thread producer = new Thread(() -> {
            try {
                for (int i = 0; i < 10; i++) {
                    buffer.produce();
                    System.out.println("Producer>> Produced item " + i);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        Thread consumer1 = new Thread(() -> {
            try {
                for (int i = 0; i < 10; i++) {
                    buffer.consume();
                    System.out.println("Consumer1>> Consumed item " + i);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        Thread consumer2 = new Thread(() -> {
            try{
                for(int i = 0; i < 10; i++){
                    buffer.consume();
                    System.out.println("Consumer2>> Consumed item " + i);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        producer.start();
        consumer1.start();
        consumer2.start();
    }
}

class Buffer {
    private boolean full = false;

    public synchronized void produce() throws InterruptedException {
        while (full) {
            wait();
        }
        full = true;
        System.out.println("Producer>> Sending notify");
        //Thread.sleep(100);
        notify();
    }

    public synchronized void consume() throws InterruptedException {
        while (!full) {
            wait();
        }
        full = false;
        System.out.println("Consumer>> Sending notify");
        Thread.sleep(200);
        notify();
    }
}
