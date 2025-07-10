# Lab1 notes
## Monitory
    - Monitor to mechanizm synchronizacji, który pilnuje eby tylko jeden wątek naraz mógł wykonywać określony fragment kodu (sekcji krytycznej) 
    - Czeka i powiadamia inne wątki (wymusza na nich to) kiedy coś się zmieni
    - słowo synchronized, kazdy obiek ma wbudowany monitor
    - chroni przed race conditions
## wait()
    - wątek zatrzymuje się, zwalnia blokadę monitora, budzony po notify/notifyAll
## notify()/notifyAll()
    - notify() - budzi jeden wątek na tym samym monitorze (czekający wait())
    - notifyAll() - budzi wszystkie wątki na tym samym obiekcie (wait())
## przykład:
    - program producent - konsumer