import argparse
import matplotlib.pyplot as plt
import networkx as nx
import os
import re
import sys

import graphGeneration as g

## settings for running example
# for diff
# NODE_POSITION_LAYOUT = "circo"
# POS_SCALING_X = 1
# POS_SCALING_Y = 0.5
# plt.margins(x=0.11)
# for code
# NODE_POSITION_LAYOUT = "sfdp"
# POS_SCALING_X = 1
# POS_SCALING_Y = 0.5
# plt.margins(x=0.11)

# + install graphviz on your system: https://www.graphviz.org/download/

# format
OUTPUT_FORMAT = ".png"
# OUTPUT_FORMAT = ".svg"
# OUTPUT_FORMAT = ".pdf"

# NODE_POSITION_LAYOUT = "dot"

# NODE_POSITION_LAYOUT = "circo"
NODE_POSITION_LAYOUT = "sfdp"
# NODE_POSITION_LAYOUT = "neato"
# NODE_POSITION_LAYOUT = "fdp"
# NODE_POSITION_LAYOUT = "twopi"
# NODE_POSITION_LAYOUT = "osage"
# NODE_POSITION_LAYOUT = "patchwork"
POS_SCALING_X = 1
POS_SCALING_Y = 0.5
FIG_WIDTH = 13.5
FIG_HEIGHT = 12.5
# FIG_WIDTH = 10
# FIG_HEIGHT = 10

# drawing parameters
NODE_SIZE = 700
EDGE_SIZE = 0.5
ARROW_SIZE = 5
SHOW_LABELS = True
DPI = 300
FONT_SIZE = 3

# other parameters
WITH_TITLE = True
INDEX_OUTPUT_FILENAME = False
LINE_NO_OFFSET = 0


NODE_PARSER = g.parseNodeDefault


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

            graphName = str(match_header.group(1))
            graphNameSplitted = graphName.split(g.JAVA_TREE_NAME_SEPARATOR, 2)
            if len(graphNameSplitted) > 1:
                fileName = graphNameSplitted[0]
                commitId = graphNameSplitted[1]
                graphTitle = fileName + "\n" + commitId
                outfilename = fileName.replace("/", g.DIR_SEPARATOR) + g.DIR_SEPARATOR + commitId

                if INDEX_OUTPUT_FILENAME:
                    outfilename = str(i) + g.DIR_SEPARATOR + outfilename
            else:
                fileName = os.path.basename(input_file)
                commitId = None
                graphTitle = graphName
                outfilename = graphName
            graph = nx.DiGraph(name=graphTitle, filename=fileName, commitid=commitId, outfilename=outfilename)
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
def plot_graphs(S, exportDir):
    # plt.figure(0, figsize=(FIG_WIDTH,2.5))
#     plt.figure(0, figsize=(FIG_WIDTH, FIG_HEIGHT))
    fig = plt.figure(0)
    for i in range(len(S)):
        difftree = S[i]

        # print("Render tree", difftree.name.replace("\n", JAVA_TREE_NAME_SEPARATOR))

        plt.clf()
        if WITH_TITLE:
            plt.title(S[i].name)

        node_colors = []
        node_type_colors = []
        rootNode = None
        for v, d in difftree.nodes(data=True):
            name = d['label']
            nodedata = NODE_PARSER(v, name)

            if nodedata.isroot:
                rootNode = v

            if nodedata.difftype == g.DIFFTYPE_NON:
                node_colors.append(g.DIFFTYPE_NON_COLOR)
            elif nodedata.difftype == g.DIFFTYPE_ADD:
                node_colors.append(g.DIFFTYPE_ADD_COLOR)
            elif nodedata.difftype == g.DIFFTYPE_REM:
                node_colors.append(g.DIFFTYPE_REM_COLOR)
            else:
                raise Exception("Could not determine color of node " + str(nodedata.difftype))

            if nodedata.codetype == g.CODETYPE_CODE:
                node_type_colors.append(g.CODE_TYPE_CODE_COLOR)
            else: # is if/else/elif
                node_type_colors.append(g.CODE_TYPE_OTHER_COLOR)

            d['label'] = nodedata.label

        edge_colors = []
        for _, _, d in difftree.edges.data():
            typeName = str(d['label'])
            edge_colors.append(g.edgeColour(typeName))

        # pos = nx.spring_layout(S[i], scale=3)
        # pos = nx.planar_layout(S[i], scale=3)

        # We have to do this to circumvent a bug:
        # https://networkx.org/documentation/stable/reference/generated/networkx.drawing.nx_pydot.pydot_layout.html
        H = nx.convert_node_labels_to_integers(difftree, label_attribute='label')
        H_layout = nx.drawing.nx_pydot.pydot_layout(H, prog=NODE_POSITION_LAYOUT, root=rootNode)
        pos = {H.nodes[n]['label']: p for n, p in H_layout.items()}

        # To scale our graph on the y axis, we have to specify a fixed interval for the y axis in the plot.
        # Otherwise, matplotlib will automatically re-scale the axis once we shifted or scaled the nodes positions.
        # Matplotlib does this to always capture our data best.
        # However, to scale we thus specify explicitly the interval for each axis. We do it here for the y axis.

        # 1: Compute the lower and upper bound of node positions (ymin and ymax).
        ymin = 999999999999999999999999999999999999999
        ymax = -ymin
        for k, v in pos.items():
            y = v[1]
            if y < ymin:
                ymin = y
            if y > ymax:
                ymax = y
        # 2: Compute the center between the bounds and compute how far the center is away from the lower and upper bound
        ycenter = (ymax + ymin) / 2
        yhalf = (ymax - ymin) / 2

        if yhalf == 0:
            yhalf = ycenter / 2

        # 3: shift our nodes and normalize their position on the y scale
        new_pos = {}
        for k, v in pos.items():
            new_pos[k] = (POS_SCALING_X * v[0], POS_SCALING_Y * (v[1] - ycenter))
        pos = new_pos

        # 4: fix the axis interval
        axes = fig.get_axes()[0]
        axes.set_ylim([-yhalf, +yhalf])

        # draw type borders
        nx.draw_networkx_nodes(difftree, pos,
                node_size=int(NODE_SIZE * g.TYPE_BORDER_SIZE),
                node_color=node_type_colors)

        # draw nodes
        if SHOW_LABELS:
            node_labels = dict([(v, d['label']) for v, d in difftree.nodes(data=True)])
            nx.draw(difftree,
                    pos,
                    node_size=NODE_SIZE,
                    node_color=node_colors,
                    width=EDGE_SIZE,
                    arrowsize=ARROW_SIZE,
                    edge_color=edge_colors,
                    font_size=FONT_SIZE,
                    labels=node_labels,
                    bbox=dict(facecolor="white", edgecolor='black', linewidth=0.3, boxstyle='round,pad=0.2', linestyle='solid'))
        else:
            nx.draw(difftree, pos,
                    node_size=NODE_SIZE,
                    node_color=node_colors,
                    width=EDGE_SIZE,
                    arrowsize=ARROW_SIZE,
                    edge_color=edge_colors,
                    font_size=FONT_SIZE)

        save_path = os.path.join(exportDir, difftree.graph['outfilename'] + OUTPUT_FORMAT)

        # Save
        print("Exporting", save_path)
        plt.tight_layout()
        plt.margins(x=0.11) # This is to prevent labels being cut off. Don't ask me what it does or how it works but it does work.
        plt.savefig(save_path, format="PNG", dpi=DPI, bbox_inches='tight')


def render(pathIn, outDir):
    graphs = load_as_line_graph(pathIn)
    plot_graphs(graphs, outDir)


def getAllFilesInDirectoryRecusivelyThat(dirname, condition):
    # Get the list of all files in directory tree at given path
    listOfFiles = list()
    for (dirpath, dirnames, filenames) in os.walk(dirname):
        listOfFiles += [os.path.join(dirpath, file) for file in filenames if condition(file)]
    return listOfFiles

if __name__ == "__main__":
    argparser = argparse.ArgumentParser(description="Render DiffTrees specified in linegraph files (.lg).")
    argparser.add_argument('infile')
    argparser.add_argument('--nodesize', nargs='?', default=700, type=int)
    argparser.add_argument('--edgesize', nargs='?', default=1.0, type=float)
    argparser.add_argument('--arrowsize', nargs='?', default=10, type=int)
    argparser.add_argument('--fontsize', nargs='?', default=3, type=int)
    argparser.add_argument('--dpi', nargs='?', default=300, type=int)
    argparser.add_argument('--scalex', nargs='?', default=POS_SCALING_X, type=int)
    argparser.add_argument('--scaley', nargs='?', default=POS_SCALING_Y, type=int)
    argparser.add_argument('--nolabels', action='store_const', const=True, default=False)
    argparser.add_argument('--recursive', action='store_const', const=True, default=False)
#     argparser.add_argument('--pattern', action='store_const', const=True, default=False)
    argparser.add_argument('--format', nargs='?', default="default", type=str)
    argparser.add_argument('--startlineno', nargs='?', default=LINE_NO_OFFSET, type=int)
    args = argparser.parse_args()

    infile = args.infile
    NODE_SIZE = args.nodesize
    SHOW_LABELS = not args.nolabels
    DPI = args.dpi
    POS_SCALING_X = args.scalex
    POS_SCALING_Y = args.scaley
    EDGE_SIZE = args.edgesize
    ARROW_SIZE = args.arrowsize
    FONT_SIZE = args.fontsize
    LINE_NO_OFFSET = args.startlineno

#     print("args.format", args.format)
    if args.format == "default":
        NODE_PARSER = g.parseNodeDefault
    elif args.format == "patternsdebug":
        NODE_PARSER = g.parseNodeDebugAtomics
    elif args.format == "patternsrelease":
        NODE_PARSER = g.parseNodeReleaseAtomics

    if os.path.isfile(infile):
        print("Render file", infile)
        outdir = os.path.dirname(infile)
        render(infile, outdir)
    elif os.path.isdir(infile):
        print("Render files in directory", infile, ("recursively" if args.recursive else "not recursively"))
        if args.recursive:
            infiles = getAllFilesInDirectoryRecusivelyThat(infile, lambda f: f.endswith(".lg"))
        else:
            infiles = [f for f in list(map(lambda x : os.path.join(infile, x), os.listdir(infile))) if os.path.isfile(f) and f.endswith(".lg")]
        for file in infiles:
            print("Render file", file)
            outdir = os.path.dirname(file)
            render(file, outdir)
    else:
        print("Given arg " + infile + " is neither a file nor a directory!")
        sys.exit(1)

    sys.exit(0)
