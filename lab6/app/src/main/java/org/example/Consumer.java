
package org.example;
import org.jcsp.lang.*;

public class Consumer implements CSProcess {
    private final One2OneChannelInt in;

    public Consumer(One2OneChannelInt in) {
        this.in = in;
    }

    public void run() {
        long startTime = System.nanoTime(); // czas
        int count = 0;
        
        while (true) {
            int item = in.in().read(); // Odbiór z ostatniej komórki bufora
            
            if (item == -1) {
                break; // kuniec (poison pill)
            }
            
            count++;
            // konsumocja
            // System.out.println("Consumer received: " + item);
        }
        
        long endTime = System.nanoTime();
        System.out.println("Consumer finished. Items: " + count);
        System.out.println("Time: " + (endTime - startTime) / 1_000_000.0 + " ms");
    }
}