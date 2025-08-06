import matplotlib.pyplot as plt

# Dane w formie tekstowej, tak jak podałeś
data = """
# Test 1: 10P 10B 5K
Bufor,Przekazane,Procent
0,342347,10.02
1,341252,9.98
2,341294,9.99
3,342064,10.01
4,342474,10.02
5,342004,10.01
6,341275,9.98
7,341730,10.00
8,341430,9.99
9,342103,10.01

# Test 2: 5P 10B 10K
Bufor,Przekazane,Procent
0,302536,9.99
1,302591,9.99
2,302643,10.00
3,303112,10.01
4,303023,10.01
5,303163,10.01
6,302133,9.98
7,303396,10.02
8,302010,9.98
9,303059,10.01

# Test 3: 10P 5B 10K
Bufor,Przekazane,Procent
0,436035,20.02
1,434858,19.96
2,435385,19.99
3,436302,20.03
4,435586,20.00

# Test 4: 5P 5B 5K
Bufor,Przekazane,Procent
0,294584,19.96
1,296105,20.06
2,295308,20.01
3,294299,19.94
4,295685,20.03

# Test 5: 5P 2B 5K
Bufor,Przekazane,Procent
0,407544,50.07
1,406481,49.93
"""

# Parsowanie danych
tests = data.strip().split("# Test")[1:]  # pomijamy pierwszy pusty element
for t in tests:
    lines = t.strip().splitlines()
    header = lines[0].split(":")[1].strip()  # np. "10P 10B 5K"
    bufory = []
    procenty = []
    for line in lines[2:]:
        if line.strip() == "":
            continue
        parts = line.split(",")
        bufory.append(int(parts[0]))
        procenty.append(float(parts[2]))
    
    # Rysowanie wykresu
    plt.figure(figsize=(8,5))
    plt.bar(bufory, procenty, color='skyblue')
    plt.xlabel("Bufor")
    plt.ylabel("Procent udziału")
    plt.ylim(0, max(procenty)*1.2)
    plt.title(f"Obciazenia_buforow_{header.replace(' ','_')}")
    plt.grid(axis='y', linestyle='--', alpha=0.7)
    
    # Zapis wykresu
    filename = f"Obciazenia_buforow_{header.replace(' ','_')}.png"
    plt.savefig(filename)
    plt.close()
    print(f"Wykres zapisano: {filename}")
