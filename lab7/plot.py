import matplotlib.pyplot as plt
import numpy as np

# Dane CSV
data = [
    {"P":10,"B":10,"K":5,"przepustowosc":341319,"wyprodukowano":3425817},
    {"P":5,"B":10,"K":10,"przepustowosc":302374,"wyprodukowano":3031160},
    {"P":10,"B":5,"K":10,"przepustowosc":217534,"wyprodukowano":2179929},
    {"P":5,"B":5,"K":5,"przepustowosc":147406,"wyprodukowano":1479336},
    {"P":5,"B":2,"K":5,"przepustowosc":80318,"wyprodukowano":815025},
]

# Przygotowanie danych
labels = [f"{d['P']}P-{d['B']}B-{d['K']}K" for d in data]
przepustowosc = [d['przepustowosc'] for d in data]
wyprodukowano = [d['wyprodukowano'] for d in data]

x = np.arange(len(labels))
width = 0.4

plt.figure(figsize=(10,6))
plt.bar(x - width/2, przepustowosc, width, label='Przepustowość (elem/sek)', color='skyblue')
plt.bar(x + width/2, wyprodukowano, width, label='Wyprodukowane elementy', color='orange')

plt.xlabel('Konfiguracja P-B-K')
plt.ylabel('Liczby')
plt.title('Porównanie wydajności wszystkich testów')
plt.xticks(x, labels)
plt.legend()
plt.grid(axis='y', linestyle='--', alpha=0.7)
plt.tight_layout()

plt.savefig("Porownanie_testow.png")
plt.show()
