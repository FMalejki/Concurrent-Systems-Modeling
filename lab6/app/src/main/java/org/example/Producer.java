package org.example;
import org.jcsp.lang.*;

public class Producer implements CSProcess {
    private final One2OneChannelInt out;
    private final int itemsToProduce;

    public Producer(One2OneChannelInt out, int n) {
        this.out = out;
        this.itemsToProduce = n;
    }

    public void run() {
        for (int i = 0; i < itemsToProduce; i++) {
            int item = (int)(Math.random() * 100) + 1;
            out.out().write(item); // Wysyłanie do pierwszej komórki bufora (B0)
            // System.out.println("Producer sent: " + item);
        }
        out.out().write(-1); // kuniec (poison pill)
        System.out.println("Producer finished.");
    }
}

