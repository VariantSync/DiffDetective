import argparse
import matplotlib.pyplot as plt
import networkx as nx
import os
import re
import sys

# + install graphviz on your system: https://www.graphviz.org/download/


# constants from our Java code
JAVA_TREE_NAME_SEPARATOR = "$$$"
JAVA_ID_LINE_NUMBER_OFFSET = 16
JAVA_ID_DIFF_TYPE_OFFSET = 8
JAVA_ID_DIFFLINE_FROM_OFFSET = 1

# export names
DIR_SEPARATOR = "$"

# colour of a node shows diff type
DIFFTYPE_ADD_COLOR = 'green'
DIFFTYPE_REM_COLOR = 'red'
DIFFTYPE_NON_COLOR = '#d1d1e0' # light purple gray

# border colour of a node shows code type
CODE_TYPE_CODE_COLOR = '#3399ff'
CODE_TYPE_OTHER_COLOR = 'black'

# drawing parameters
NODE_SIZE = 700
EDGE_SIZE = 0.5
ARROW_SIZE = 5
SHOW_LABELS = True
DPI = 300
POS_SCALING_X = 1
POS_SCALING_Y = 1

# other parameters
IS_PATTERN = False


def lineNoOfNode(v):
    # inverse of DiffNode::getID in our Java code
    # ((1 + fromLine) << ID_LINE_NUMBER_OFFSET) + diffType.ordinal()
    return (v >> JAVA_ID_LINE_NUMBER_OFFSET) - JAVA_ID_DIFFLINE_FROM_OFFSET

# the same as string.find but in case the char c is not found, returns len(s) instead of -1
def findCharacterInStringGreedy(s, c):
    index = s.find(c)
    if index == -1:
        index = len(s)
    return index


def substringGraceful(s, fromIndex):
    if fromIndex >= len(s):
        return ""
    return s[fromIndex:]


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
            graphNameSplitted = graphName.split(JAVA_TREE_NAME_SEPARATOR, 2)
            if len(graphNameSplitted) > 1:
                fileName = graphNameSplitted[0]
                commitId = graphNameSplitted[1]
                graphTitle = fileName + "\n" + commitId
            else:
                fileName = os.path.basename(input_file)
                commitId = "unknown"
                graphTitle = fileName
                if IS_PATTERN:
                    graphTitle = "Pattern\n" + graphTitle
            graph = nx.DiGraph(name=graphTitle, filename=fileName, commitid=commitId)
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
    plt.figure(0)
    for i in range(len(S)):
        difftree = S[i]

        # print("Render tree", difftree.name.replace("\n", JAVA_TREE_NAME_SEPARATOR))

        plt.clf()
        plt.margins(0.05, 0.05)
        plt.title(S[i].name)
        # pos = nx.spring_layout(S[i], scale=3)
        # pos = nx.planar_layout(S[i], scale=3)
        # pos = nx.nx_agraph.pygraphviz_layout(S[i], prog='dot')
        # pos = graphviz_layout(S[i], prog='dot')

        # We have to do this to circumvent a bug:
        # https://networkx.org/documentation/stable/reference/generated/networkx.drawing.nx_pydot.pydot_layout.html
        H = nx.convert_node_labels_to_integers(difftree, label_attribute='label')
        H_layout = nx.drawing.nx_pydot.pydot_layout(H, prog="sfdp") #sfdp
        pos = {H.nodes[n]['label']: p for n, p in H_layout.items()}

        node_colors = []
        node_type_colors = []
        for v, d in difftree.nodes(data=True):
            name = d['label']
            # print(v, name)
            if name.startswith("NON"):
                node_colors.append(DIFFTYPE_NON_COLOR)
            elif name.startswith("ADD"):
                node_colors.append(DIFFTYPE_ADD_COLOR)
            elif name.startswith("REM"):
                node_colors.append(DIFFTYPE_REM_COLOR)

            # remove metadata and " " from node labels
            nameWithoutDiffType = name[4:]

            if nameWithoutDiffType.startswith("CODE"):
                node_type_colors.append(CODE_TYPE_CODE_COLOR)
            else: # is if/else/elif
                node_type_colors.append(CODE_TYPE_OTHER_COLOR)

            isroot = nameWithoutDiffType.startswith("ROOT")
            ismacro = not isroot and not nameWithoutDiffType.startswith("CODE")
            # print("nameWithoutDiffType:", nameWithoutDiffType)
            # print("findCharacterInStringGreedy(", nameWithoutDiffType, ", '_'):", findCharacterInStringGreedy(nameWithoutDiffType, '_'))
            code = substringGraceful(nameWithoutDiffType, findCharacterInStringGreedy(nameWithoutDiffType, '_')+1) # +1 tp remove _ too
            # print(code)
            if len(code) > 0:
                # remove parenthesis
                code = code[1:len(code)-1]
                # print(code)
                if ismacro:
                    code = '#' + code
                # print(code)
            # prepend line number
            if IS_PATTERN:
                code = ""
            else:
                code = str(lineNoOfNode(v)) + ("\n" + code if len(code) > 0 else "")
            # print(code)
            # print("")
            d['label'] = "ROOT" if isroot else code

        edge_colors = []
        for _, _, d in difftree.edges.data():
            typeName = str(d['label'])
            if typeName == "a":
                edge_colors.append('#bbeb37')
            if typeName == "b":
                edge_colors.append('#ff9129')
            if typeName == "ba":
                edge_colors.append('black')

        new_pos = {}
        for k, v in pos.items():
            new_pos[k] = (POS_SCALING_X * v[0], POS_SCALING_Y * v[1])
        pos = new_pos

        # draw type borders
        nx.draw_networkx_nodes(difftree, pos,
                node_size=int(NODE_SIZE * (8.0 / 7.0)),
                node_color=node_type_colors)

        # draw nodes
        if SHOW_LABELS:
            node_labels = dict([(v, d['label']) for v, d in difftree.nodes(data=True)])
            nx.draw(difftree, pos,
                    node_size=NODE_SIZE,
                    node_color=node_colors,
                    width=EDGE_SIZE,
                    arrowsize=ARROW_SIZE,
                    edge_color=edge_colors,
                    font_size=3,
                    labels=node_labels,
                    bbox=dict(facecolor="white", edgecolor='black', boxstyle='round,pad=0.1', linestyle=''))
        else:
            nx.draw(difftree, pos,
                    node_size=NODE_SIZE,
                    node_color=node_colors,
                    width=EDGE_SIZE,
                    arrowsize=ARROW_SIZE,
                    edge_color=edge_colors,
                    font_size=3)

        outfilename = difftree.graph['filename'].replace("/", DIR_SEPARATOR)
        if not IS_PATTERN:
            outfilename += DIR_SEPARATOR + difftree.graph['commitid']
        outfilename += ".png"
        save_path = os.path.join(exportDir, outfilename)

        # Save
        print("Exporting", save_path)
        plt.savefig(save_path, format="PNG", dpi=DPI)


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
    argparser.add_argument('--dpi', nargs='?', default=300, type=int)
    argparser.add_argument('--scalex', nargs='?', default=1, type=int)
    argparser.add_argument('--scaley', nargs='?', default=1, type=int)
    argparser.add_argument('--nolabels', action='store_const', const=True, default=False)
    argparser.add_argument('--recursive', action='store_const', const=True, default=False)
    argparser.add_argument('--pattern', action='store_const', const=True, default=False)
    args = argparser.parse_args()

    infile = args.infile
    NODE_SIZE = args.nodesize
    SHOW_LABELS = not args.nolabels
    DPI = args.dpi
    POS_SCALING_X = args.scalex
    POS_SCALING_Y = args.scaley
    EDGE_SIZE = args.edgesize
    ARROW_SIZE = args.arrowsize
    IS_PATTERN = args.pattern

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
