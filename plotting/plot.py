import matplotlib as mpl
import matplotlib.pyplot as plt
import numpy
from matplotlib.ticker import FuncFormatter

from result_data import load_runtime_results

mpl.rcParams['pdf.fonttype'] = 42
# mpl.rcParams['font.sans-serif'] = ["Verdana", "Arial", "Helvetica", "Avant Garde", "sans-serif"]
mpl.rcParams['figure.dpi'] = 600
mpl.rcParams['savefig.format'] = "png"

legend_size = 16
title_size = 22
tick_size = 18
axis_label_size = 20
text_box_font_size = 16

fig_width = 10
fig_height = 3


def commit_runtime(runtime_results: [], annotate=False):
    # Collect runtimes and convert them from milliseconds to minutes
    runtimes = [result.runtime // 60_000 for result in runtime_results]
    runtime_min = numpy.min(runtimes)
    runtime_max = numpy.max(runtimes)
    runtimes = numpy.sort(runtimes)[::-1]

    # Count how often the runtime is less than one minute, aka. our largest bin
    runtimes_below_one = [x for x in runtimes if x < 1]
    runtimes_below_one = len(runtimes_below_one)

    # For printing of commits with the greatest runtime
    # largest_commits = [x for x in runtime_results if x.runtime // 60000 == runtime_max]
    # for commit in largest_commits:
    #     print("Dataset: %s  Commit: %s" % (commit.dataset, commit.commit_id))

    # Plot the data
    fig, ax = plt.subplots()
    # Manually set size to adjust aspect ratio
    fig.set_size_inches(fig_width, fig_height)
    # ax.set_title("Histogram of required runtimes", fontsize=title_size)
    ax.hist(runtimes, bins=runtime_max + 1, histtype='stepfilled')

    # Format the x-axis
    ax.tick_params(axis='x', labelsize=tick_size)
    plt.xlim(runtime_min, runtime_max + 1)
    plt.xlabel("Runtime in minutes", fontsize=axis_label_size)

    # Format the y-axis
    ax.tick_params(axis='y', labelsize=tick_size)
    plt.yscale('log')
    plt.ylabel("#Commits (log)", fontsize=axis_label_size)

    # Add annotation
    if annotate:
        ax.annotate(f'{runtimes_below_one:,.0f} commits require\nless than one minute.', xy=(0, runtimes_below_one),
                    xytext=(5, 40_000),
                    arrowprops=dict(arrowstyle="->"),
                    bbox=dict(boxstyle="round", fc="w"),
                    fontsize=text_box_font_size
                    )

        ax.annotate(f'Two commits require\n{runtime_max:,.0f} minutes.', xy=(runtime_max, 2),
                    xytext=(runtime_max - 30, 10),
                    arrowprops=dict(arrowstyle="->"),
                    bbox=dict(boxstyle="round", fc="w"),
                    fontsize=text_box_font_size
                    )

    def major_formatter(x, pos):
        return f'{x:,.0f}'

    ax.yaxis.set_major_formatter(FuncFormatter(major_formatter))

    plt.tight_layout()
    plt.savefig("runtime_histogram.png")
    plt.savefig("runtime_histogram.pdf")


# For debugging
if __name__ == "__main__":
    folder = "../results"
    print("Loading results...")
    results = load_runtime_results(folder)
    print("Plotting runtime histogram...")
    # commit_runtime(results, True)
    commit_runtime(results)
    print("Done.")
