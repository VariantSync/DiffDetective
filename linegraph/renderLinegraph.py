import networkx as nx
import matplotlib.pyplot as plt
import sys
import re


def load_as_line_graph(input_file):
    regex_header = r"t # (.*)"
    regex_node = r"v (\d+) (.+).*"
    regex_edge = r"e (\d+) (\d+) (.+).*"

    graphs = []

    # regex_embedding = r"#=> (\d+) .*"
    # support_set = set()

    with open(input_file, 'r') as input_graphs:
        lines = input_graphs.readlines()

    # if tlv header continue parsing

    if re.match(regex_header, lines[0]):
        pass
    else:
        print("Error parsing graph db. Expecting TLV.")
        return []

    graph = None
    for next_line in lines:
        match_header = re.match(regex_header, next_line)
        if match_header:
            if graph is not None:
                graphs.append(graph)
            graph = nx.DiGraph(label=match_header.group(1))
            continue

        match_node = re.match(regex_node, next_line)
        match_edge = re.match(regex_edge, next_line)
        # match_embedding = re.match(regex_embedding, next_line)

        if match_node:
            graph.add_node(int(match_node.group(1)), label=str(match_node.group(2)))
        elif match_edge:
            graph.add_edge(int(match_edge.group(1)), int(match_edge.group(2)), label=str(match_edge.group(3)))
        # elif match_embedding:
        #    support_set.add(int(match_embedding.group(1)))

    # Add also the last graph
    if graph is not None:
        graphs.append(graph)

    return graphs


# Plot graphs
def plot_graphs(S, file_path, labels=True):
    for i in range(len(S)):
        plt.clf()
        plt.figure(i)
        plt.margins(0.05, 0.05)
        pos = nx.spring_layout(S[i], scale=3)

        color_map = []
        for v, d in S[i].nodes(data=True):
            name = d['label']
            # print(v, name)
            if name.startswith("NON"):
                color_map.append('gray')
            elif name.startswith("ADD"):
                color_map.append('green')
            elif name.startswith("REM"):
                color_map.append('red')

        if labels:
            nx.draw(S[i], pos, node_size=500, node_color=color_map)
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


if __name__ == "__main__":
    infile = sys.argv[1]
    outfile = infile

    graphs = load_as_line_graph(infile)
    plot_graphs(graphs, outfile)
