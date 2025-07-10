public class TaskHome {
    public static void main(String[] args) {
        Buffer buffer = new Buffer();

        Thread producer = new Thread(() -> {
            try {
                int counter = 0;
                for (int i = 0; i < 10; i++) {
                    while(!buffer.produce()){
                        counter++;
                        System.out.println("Not Produced, buffer full");
                    }
                    System.out.println("Produced Sucessfully after " + counter + " tries");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        Thread consumer = new Thread(() -> {
            try {
                int counter = 0;
                for (int i = 0; i < 10; i++) {
                    while(!buffer.consume()){
                        counter++;
                        System.out.println("Not Consumed, buffer empty");
                    }
                    System.out.println("Consumed Sucessfully after " + counter + " tries");
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

    public synchronized boolean produce() throws InterruptedException {
        if (full) {
            return false;
        }
        full = true;
        return true;
    }

    public synchronized boolean consume() throws InterruptedException {
        if (!full) {
            return false;
        }
        full = false;
        return true;
    }
}



