package org.example;
import org.jcsp.lang.*;

public class BufferCell implements CSProcess {
    private final One2OneChannelInt in;  // Kanał wejściowy (z poprzedniej komórki lub producenta)
    private final One2OneChannelInt out; // Kanał wyjściowy (do następnej komórki lub konsumenta)
    private final int id;

    public BufferCell(One2OneChannelInt in, One2OneChannelInt out, int id) {
        this.in = in;
        this.out = out;
        this.id = id;
    }

    public void run() {
        while (true) {
            // ]Odczyt elementu (blokuje, dopóki coś nie przyjdzie)
            int item = in.in().read();

            // ]Przekazanie elementu dalej (blokuje, dopóki następny proces nie odbierze)
            out.out().write(item);

            // Obsługa zakończenia (poison pill) dostalismy -1 to kończymy proces
            if (item == -1) {
                break;
            }
        }
        // System.out.println("BufferCell " + id + " finished.");
    }
}