#!/bin/bash

# Konfiguracja dat
start_date="2025-01-20"
end_date="2026-01-20"
target_commits=100

# Konwersja dat na sekundy (macOS format)
current=$(date -j -f "%Y-%m-%d" "$start_date" "+%s")
end=$(date -j -f "%Y-%m-%d" "$end_date" "+%s")

# Obliczanie średniego odstępu, aby rozłożyć 100 commitów w ciągu roku
# Rok ma ok. 31,5 mln sekund. 100 commitów to średnio jeden co 3.6 dnia.
interval=$(( (end - current) / target_commits ))

count=1

echo "Generowanie 100 commitów od $start_date do $end_date..."

while [ $count -le $target_commits ]; do
    # Dodajemy losowe wahanie do interwału ( +/- 1 dzień), żeby nie było idealnie równo
    fluctuation=$(( (RANDOM % 172800) - 86400 ))
    commit_time=$(( current + fluctuation ))
    
    # Formatowanie daty dla Gita
    # Losowa godzina pracy (08:00 - 20:00)
    rand_hour=$(( (RANDOM % 12) + 8 ))
    rand_min=$(( RANDOM % 60 ))
    rand_sec=$(( RANDOM % 60 ))
    
    formatted_date=$(date -r $commit_time "+%Y-%m-%dT$rand_hour:$rand_min:$rand_sec")

    # Tworzenie zmiany w pliku
    echo "Update nr $count - $formatted_date" >> progress_log.txt
    git add progress_log.txt

    # Commit
    GIT_AUTHOR_DATE="$formatted_date" GIT_COMMITTER_DATE="$formatted_date" \
    git commit -m "refactor: system optimization part $count" --quiet

    # Przesunięcie czasu do przodu o średni interwał
    current=$(( current + interval ))
    count=$(( count + 1 ))
done

echo "Sukces! Stworzono $target_commits commitów."
echo "Teraz użyj: git push origin main --force"