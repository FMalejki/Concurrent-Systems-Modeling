import re

input_file = "output.csv"
output_main = "results.csv"
output_buffers = "buffers.csv"

main_rows = []
buffer_rows = []
is_main_section = False
is_buffer_section = False

with open(input_file, "r", encoding="utf-8") as f:
    for line in f:
        line=line.strip()

        if line.startswith("=== DANE CSV"):
            is_main_section = True
            is_buffer_section = False
            continue

        if line.startswith("=== ROZKŁAD"):
            is_buffer_section = True
            is_main_section = False
            continue

        # zakończenie sekcji
        if line.startswith("==="):
            is_main_section = False
            is_buffer_section = False

        if is_main_section and re.match(r'^\d', line):
            # usuwamy spacje tylko wokół separatorów
            fixed = line.replace(",", ".")  # zamiana przecinka na kropkę
            # teraz separator zmienimy na semicolon
            fixed = fixed.replace(".", ",", fixed.count('.') - line.count(','))  
            main_rows.append(line)

        if is_buffer_section and re.match(r'^\d', line):
            buffer_rows.append(line)

# zapis plików
with open(output_main, "w", encoding="utf-8") as f:
    f.write("P,B,K,t,czas_ms,wyprodukowano,skonsumowano,przepustowosc,srednia,odchylenie,wsp_zmiennosci\n")
    for row in main_rows:
        f.write(row + "\n")

with open(output_buffers, "w", encoding="utf-8") as f:
    f.write("Bufor,Przekazane,Procent\n")
    for row in buffer_rows:
        f.write(row + "\n")

print("Wyodrębniono dane CSV.")
