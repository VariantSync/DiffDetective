import matplotlib as mpl
import matplotlib.pyplot as plt
import numpy
from matplotlib.ticker import FuncFormatter

from plotting.result_data import load_runtime_results

mpl.rcParams['pdf.fonttype'] = 42
# mpl.rcParams['font.sans-serif'] = ["Verdana", "Arial", "Helvetica", "Avant Garde", "sans-serif"]
mpl.rcParams['figure.dpi'] = 600
mpl.rcParams['savefig.format'] = "png"

legend_size = 13
title_size = 16
tick_size = 14
axis_label_size = 16

fig_width = 10
fig_height = 3


def commit_runtime(runtime_results: []):
    # Collect runtimes and convert them from milliseconds to minutes
    runtimes = [result.runtime // 60_000 for result in runtime_results]
    runtime_min = numpy.min(runtimes)
    runtime_max = numpy.max(runtimes)

    # Count how often the runtime is less than one minute, aka. our largest bin
    runtimes_below_one = len([x for x in runtimes if x < 1])

    # For printing of commits with the greatest runtime
    # largest_commits = [x for x in runtime_results if x.runtime // 60000 == runtime_max]
    # for commit in largest_commits:
    #     print("Dataset: %s  Commit: %s" % (commit.dataset, commit.commit_id))

    # Plot the data
    fig, ax = plt.subplots()
    # Manually set size to adjust aspect ratio
    fig.set_size_inches(fig_width, fig_height)
    ax.set_title("Histogram of required runtimes", fontsize=title_size)
    ax.hist(runtimes, bins=runtime_max + 1, histtype='stepfilled')

    # Format the x-axis
    plt.xlabel("Runtime in minutes", fontsize=axis_label_size)
    ax.tick_params(axis='x', labelsize=tick_size)
    plt.xlim(runtime_min, runtime_max + 1)

    # Format the y-axis
    plt.ylabel("#Commits (log)", fontsize=axis_label_size)
    ax.tick_params(axis='y', labelsize=tick_size)
    plt.yscale('log')

    # Add annotation
    t = ax.annotate(f'{runtimes_below_one:,.0f} commits require\nless than one minute', xy=(0, runtimes_below_one),
                    xytext=(5, 150_000),
                    arrowprops=dict(arrowstyle="->"),
                    bbox=dict(boxstyle="round", fc="w")
                    )

    t = ax.annotate(f'Two commits require\n{runtime_max:,.0f} minutes', xy=(runtime_max, 2),
                    xytext=(runtime_max - 20, 10),
                    arrowprops=dict(arrowstyle="->"),
                    bbox=dict(boxstyle="round", fc="w")
                    )

    def major_formatter(x, pos):
        return f'{x:,.0f}'

    ax.yaxis.set_major_formatter(FuncFormatter(major_formatter))

    plt.tight_layout()
    plt.savefig("commit_runtime.png")


# For debugging
if __name__ == "__main__":
    folder = "/data/m2/edit-patterns/results"
    print("Loading results...")
    results = load_runtime_results(folder)
    print("Plotting runtime histogram...")
    commit_runtime(results)
    print("Done.")
