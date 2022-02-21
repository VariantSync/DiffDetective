import matplotlib.pyplot as plt
import networkx as nx
from networkx.drawing.nx_agraph import graphviz_layout

# Plot graphs
def plot_graphs(S, file_path, labels=True):
    for i in range(len(S)):
        plt.clf()
        plt.figure(i)
        plt.margins(0.05, 0.05)
        pos = nx.spring_layout(S[i], scale=3)

        if labels:
            nx.draw(S[i], pos, node_size=500)
            node_labels = dict([(v, d['label']) for v, d in S[i].nodes(data=True)])
            y_off = 0.02
            nx.draw_networkx_labels(S[i], pos={k: ([v[0], v[1] + y_off]) for k, v in pos.items()}, font_size=6,
                                    labels=node_labels)
            nx.draw_networkx_edge_labels(S[i], pos, font_size=6)
        else:
            nx.draw(S[i], pos, node_size=20)

        if len(S) > 1:
            save_path = file_path + "_" + str(i) + ".png"
        else:
            save_path = file_path + ".png"

        # Save
        plt.savefig(save_path, format="PNG")

def plot_graph_dot(G, file_path, labels=True):
    # TODO assert graphviz installed

    # same layout using matplotlib with no labels
    plt.title(G.name)
    pos = graphviz_layout(G, prog='dot')
    nx.draw(G, pos, with_labels=labels, arrows=True)
    plt.savefig(file_path)