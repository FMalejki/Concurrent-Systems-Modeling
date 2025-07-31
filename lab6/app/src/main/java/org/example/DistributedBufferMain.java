package org.example;
import org.jcsp.lang.*;

public class DistributedBufferMain {

    public static void main(String[] args) {
        int N = 10; // Rozmiar bufora (ilosc procesow)
        int ITEMS = 1000; // Liczba elementów do przesłania

        // sN+1 kanałów
        // Prod -> [B0] -> [B1] -> ... -> [BN-1] -> Cons
        One2OneChannelInt[] channels = new One2OneChannelInt[N + 1];
        
        // Inicjalizacja kanałów
        for (int i = 0; i < channels.length; i++) {
            channels[i] = Channel.one2oneInt();
        }

        // Tablica procesów do uruchomienia równoległego
        // 1 producent + 1 konsument + N komórek bufora
        CSProcess[] procList = new CSProcess[N + 2];

        // Producent (pisze do channels[0])
        procList[0] = new Producer(channels[0], ITEMS);

        // łańcuch Buforów
        // BufferCell i bierze z channels[i] i pisze do channels[i+1]
        for (int i = 0; i < N; i++) {
            procList[i + 1] = new BufferCell(channels[i], channels[i+1], i);
        }

        // Konsument (czyta z ostatniego kanału channels[N])
        procList[N + 1] = new Consumer(channels[N]);

        // Uruchomienie wszystkiego
        Parallel par = new Parallel(procList);
        par.run();
    }
}
