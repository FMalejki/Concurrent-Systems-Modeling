public class HomeTask4 {
    static final int M = 5;
    static final int N = 200;
    static final int W = 100;

    public static void main(String[] args) {
        Buffer buffer = new Buffer(W);

        for (int i = 1; i <= M; i++) {
            int id = i;
            new Thread(() -> {
                try {
                    for (int j = 1; j <= 100; j++) {
                        buffer.produce("Item-" + j + " from P" + id);
                        Thread.sleep(100);
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
                    for (int j = 1; j <= 100; j++) {
                        String item = buffer.consume();
                        Thread.sleep(150);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "Consumer-" + i).start();
        }
    }
}

class Buffer {
    private final String[] buffer;
    private int in = 0; 
    private int out = 0;
    private int count = 0; 

    public Buffer(int size) {
        buffer = new String[size];
    }

    public synchronized void produce(String item) throws InterruptedException {
        while (count == buffer.length) {
            System.out.println(Thread.currentThread().getName() + " waiting to produce...");
            wait();
        }
        buffer[in] = item;
        in = (in + 1) % buffer.length;
        count++;
        System.out.println(Thread.currentThread().getName() + " produced: " + item);

        notify(); 

        /* 
        notifyAll(); 
        */
    }

    public synchronized String consume() throws InterruptedException {
        while (count == 0) {
            System.out.println(Thread.currentThread().getName() + " waiting to consume...");
            wait();
        }
        String item = buffer[out];
        out = (out + 1) % buffer.length;
        count--;
        System.out.println(Thread.currentThread().getName() + " consumed: " + item);

        notify();

        /*
        notifyAll();
        */
        return item;
    }
}
