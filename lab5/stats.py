import matplotlib.pyplot as plt
import numpy as np
from collections import OrderedDict

# dane wyciągnięte z notes.txt
sections = OrderedDict([
    ("2 Conditions (starvation)", {
        "producers": {
            "Producer-1": 3970, "Producer-2": 3969, "Producer-3": 3971,
            "Producer-4": 3972, "Producer-5": 3972, "Producer-6": 3970
        },
        "consumers": {
            "Consumer-1": 5937, "Consumer-2": 5881,
            "Consumer-3": 5942, "Consumer-4": 6064
        }
    }),
    ("Three-lock statistics", {
        "producers": {
            "Producer-1": 2576, "Producer-2": 2566, "Producer-3": 2568,
            "Producer-4": 2567, "Producer-5": 2567, "Producer-6": 2607
        },
        "consumers": {
            "Consumer-1": 3859, "Consumer-2": 3864,
            "Consumer-3": 3855, "Consumer-4": 3863
        }
    }),
    ("4 Conditions (non-starvation)", {
        "producers": {
            "Producer-1": 2522, "Producer-2": 2531, "Producer-3": 2531,
            "Producer-4": 2536, "Producer-5": 2526, "Producer-6": 2523
        },
        "consumers": {
            "Consumer-1": 3791, "Consumer-2": 3791,
            "Consumer-3": 3794, "Consumer-4": 3783
        }
    })
])

producer_color = "#1f77b4"  # blue
consumer_color = "#ff7f0e"  # orange

for idx, (title, data) in enumerate(sections.items(), start=1):
    # order threads: producers P1..P6 then consumers C1..C4
    prod_keys = [f"Producer-{i}" for i in range(1,7)]
    cons_keys = [f"Consumer-{i}" for i in range(1,5)]
    labels = prod_keys + cons_keys
    values = [data["producers"].get(k, 0) for k in prod_keys] + [data["consumers"].get(k, 0) for k in cons_keys]
    colors = [producer_color]*len(prod_keys) + [consumer_color]*len(cons_keys)

    # totals
    total_produced = sum(data["producers"].values())
    total_consumed = sum(data["consumers"].values())

    fig, (ax1, ax2) = plt.subplots(1,2, figsize=(14,5), gridspec_kw={'width_ratios':[4,1]})
    fig.suptitle(title, fontsize=14)

    # bar chart per thread
    x = np.arange(len(labels))
    ax1.bar(x, values, color=colors)
    ax1.set_xticks(x)
    ax1.set_xticklabels(labels, rotation=45, ha='right')
    ax1.set_ylabel("Count")
    ax1.set_title("Per-thread counts (producers vs consumers)")

    # annotate each bar with value (small)
    for i, v in enumerate(values):
        ax1.text(i, v + max(values)*0.01, f"{v:,}", ha='center', va='bottom', fontsize=8, rotation=90)

    # totals subplot
    ax2.bar(["Produced","Consumed"], [total_produced, total_consumed], color=[producer_color, consumer_color])
    ax2.set_title("Totals")
    for i, v in enumerate([total_produced, total_consumed]):
        ax2.text(i, v + max(total_produced, total_consumed)*0.01, f"{v:,}", ha='center', va='bottom', fontsize=10)

    plt.tight_layout(rect=[0, 0.03, 1, 0.95])
    outname = f"stats_plot_{idx}.png"
    plt.savefig(outname, dpi=150)
    print(f"Saved {outname}")
    plt.show()