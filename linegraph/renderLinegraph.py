import argparse
import matplotlib.pyplot as plt
import networkx as nx
import os
import re
import sys

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
POS_SCALING_Y = -1
FIG_WIDTH = 3.5
FIG_HEIGHT = 2.5
# FIG_WIDTH = 10
# FIG_HEIGHT = 10

# constants from our Java code
JAVA_TREE_NAME_SEPARATOR = "$$$"
JAVA_ID_LINE_NUMBER_OFFSET = 16
JAVA_ID_DIFF_TYPE_OFFSET = 8
JAVA_ID_DIFFLINE_FROM_OFFSET = 1

# export names
# DIR_SEPARATOR = "$"
DIR_SEPARATOR = "___"

# colour of a node shows diff type
DIFFTYPE_ADD_COLOR = 'green'
DIFFTYPE_REM_COLOR = 'red'
DIFFTYPE_NON_COLOR = '#d1d1e0' # light purple gray

# border colour of a node shows code type
CODE_TYPE_CODE_COLOR = '#3399ff'
CODE_TYPE_OTHER_COLOR = 'black'
TYPE_BORDER_SIZE = (8.0 / 7.0)

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

RELEASE_PATTERNS_CODE_PREFIX = "c"
RELEASE_PATTERNS_MACRO_PREFIX = "m"

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


def lineNoOfNode(v):
    # inverse of DiffNode::getID in our Java code
    # ((1 + fromLine) << ID_LINE_NUMBER_OFFSET) + diffType.ordinal()
    return (v >> JAVA_ID_LINE_NUMBER_OFFSET) - JAVA_ID_DIFFLINE_FROM_OFFSET

DIFFTYPE_ADD = "add"
DIFFTYPE_REM = "rem"
DIFFTYPE_NON = "non"
CODETYPE_CODE = "code"
CODETYPE_MACRO = "macro"

class Pattern:
    def __init__(self, name, id, difftype):
        self.name = name
        self.id = id
        self.difftype = difftype

class NodeData:
    def __init__(self):
        self.difftype = None
        self.codetype = None
        self.isroot = False
        self.label = "undefined label"

_ALL_PATTERN_NAMES = [
    "AddToPC", "AddWithMapping",
    "RemFromPC", "RemWithMapping",
    "Specialization", "Generalization", "Reconfiguration", "Refactoring", "Unchanged"
]
_ALL_PATTERN_DIFFTYPES = [
     DIFFTYPE_ADD, DIFFTYPE_ADD,
     DIFFTYPE_REM, DIFFTYPE_REM,
     DIFFTYPE_NON, DIFFTYPE_NON, DIFFTYPE_NON, DIFFTYPE_NON, DIFFTYPE_NON
]
ALL_PATTERNS = [Pattern(name, i, _ALL_PATTERN_DIFFTYPES[i]) for i,name in enumerate(_ALL_PATTERN_NAMES)]
ADD_PATTERNS = list(filter(lambda pattern: pattern.difftype == DIFFTYPE_ADD, ALL_PATTERNS))
REM_PATTERNS = list(filter(lambda pattern: pattern.difftype == DIFFTYPE_REM, ALL_PATTERNS))
NON_PATTERNS = list(filter(lambda pattern: pattern.difftype == DIFFTYPE_NON, ALL_PATTERNS))

def getPatternThat(predicate):
    matches = list(filter(predicate, ALL_PATTERNS))
    if len(matches) < 1:
#        raise Exception("There is no pattern with the name \"" + name + "\"!")
        return None
    if len(matches) > 1:
#        raise Exception("There is more than one pattern with the name \"" + name + "\"!")
        return None
    return matches[0]


def getPatternFromName(name):
    return getPatternThat(lambda pattern: pattern.name == name)


def getPatternFromId(id):
    intId = int(id)
    return getPatternThat(lambda pattern: pattern.id == intId)


def difftypeFromId(id):
    if id == 0:
        return DIFFTYPE_ADD
    elif id == 1:
        return DIFFTYPE_REM
    elif id == 2:
        return DIFFTYPE_NON
    raise Exception("Cannot compute difftype from id " + id + "!")


def codetypeFromId(id):
    if id == 0:
        return "if"
    elif id == 1:
        return "endif"
    elif id == 2:
        return "else"
    elif id == 3:
        return "elif"
    elif id == 4:
        return "code"
    elif id == 5:
        return "ROOT"
    raise Exception("Cannot compute difftype from id " + id + "!")


def parseNodeDefault(id, name):
    nameWithoutDiffType = name[4:]

    result = NodeData()
    isCode = nameWithoutDiffType.startswith("CODE")

    if name.startswith("NON"):
        result.difftype = DIFFTYPE_NON
    elif name.startswith("ADD"):
        result.difftype = DIFFTYPE_ADD
    elif name.startswith("REM"):
        result.difftype = DIFFTYPE_REM

    if isCode:
        result.codetype = CODETYPE_CODE
    else: # is if/else/elif
        result.codetype = CODETYPE_MACRO

    secondHyphenPos = findCharacterInStringGreedy(nameWithoutDiffType, '_')
    codetype = nameWithoutDiffType[:secondHyphenPos]
    isRoot = codetype.startswith("ROOT")
    isMacro = not isRoot and not isCode

    code = substringGraceful(nameWithoutDiffType, secondHyphenPos+1) # +1 to remove _ too
    # print(code)
    if len(code) > 0:
        # remove parenthesis ""
        code = code[1:len(code)-1]
        # print(code)
        if isMacro:
            code = '#' + code
    # prepend line number
    code = str(lineNoOfNode(id)) + ("\n" + code if len(code) > 0 else "")

    result.isroot = isRoot
    result.label = "ROOT" if isRoot else code
    return result


def parseNodeDebugAtomics(id, name):
    nameWithoutDiffType = name[4:]

    result = NodeData()
    isCode = True
    pattern = getPatternFromName(name)
    if pattern is not None:
        result.difftype = pattern.difftype
    else:
        isCode = False

    if result.difftype is None:
        if name.startswith("NON"):
            result.difftype = DIFFTYPE_NON
        elif name.startswith("ADD"):
            result.difftype = DIFFTYPE_ADD
        elif name.startswith("REM"):
            result.difftype = DIFFTYPE_REM

    if isCode:
        result.codetype = CODETYPE_CODE
    else: # is if/else/elif
        result.codetype = CODETYPE_MACRO

    secondHyphenPos = findCharacterInStringGreedy(nameWithoutDiffType, '_')
    codetype = nameWithoutDiffType[:secondHyphenPos]
    result.isroot = codetype.startswith("ROOT")
    isMacro = not result.isroot and not isCode
    result.label = "ROOT" if result.isroot else (codetype if isMacro else name)
    return result


def parseNodeReleaseAtomics(id, name):
    result = NodeData()

    if name.startswith(RELEASE_PATTERNS_CODE_PREFIX):
#         print("getPatternFromId(", name[len(RELEASE_PATTERNS_CODE_PREFIX):], ")")
        pattern = getPatternFromId(name[len(RELEASE_PATTERNS_CODE_PREFIX):])
#         print(name)
#         print(name[len(RELEASE_PATTERNS_CODE_PREFIX):])
#         print(pattern)
#         print()
        result.codetype = CODETYPE_CODE
        result.difftype = pattern.difftype
        result.label = pattern.name
    elif name.startswith(RELEASE_PATTERNS_MACRO_PREFIX):
        difftypeBegin = len(RELEASE_PATTERNS_MACRO_PREFIX)
        codetypeBegin = difftypeBegin + 1
        difftypeId = int(name[difftypeBegin:codetypeBegin])
        codetypeId = int(name[codetypeBegin:codetypeBegin+1])

        result.codetype = CODETYPE_MACRO
        result.difftype = difftypeFromId(difftypeId)
        result.label = codetypeFromId(codetypeId)
    else:
        raise Exception("Node " + name + " has unknown type. Expected prefix " + RELEASE_PATTERNS_CODE_PREFIX + " or " + RELEASE_PATTERNS_MACRO_PREFIX + " but was none.")

    return result

NODE_PARSER = parseNodeDefault


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
                outfilename = fileName.replace("/", DIR_SEPARATOR) + DIR_SEPARATOR + commitId

                if INDEX_OUTPUT_FILENAME:
                    outfilename = str(i) + DIR_SEPARATOR + outfilename
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
    plt.figure(0)
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

            if nodedata.difftype == DIFFTYPE_NON:
                node_colors.append(DIFFTYPE_NON_COLOR)
            elif nodedata.difftype == DIFFTYPE_ADD:
                node_colors.append(DIFFTYPE_ADD_COLOR)
            elif nodedata.difftype == DIFFTYPE_REM:
                node_colors.append(DIFFTYPE_REM_COLOR)

            if nodedata.codetype == CODETYPE_CODE:
                node_type_colors.append(CODE_TYPE_CODE_COLOR)
            else: # is if/else/elif
                node_type_colors.append(CODE_TYPE_OTHER_COLOR)

            d['label'] = nodedata.label

        edge_colors = []
        for _, _, d in difftree.edges.data():
            typeName = str(d['label'])
            if typeName == "a":
                edge_colors.append('#bbeb37')
            if typeName == "b":
                edge_colors.append('#ff9129')
            if typeName == "ba":
                edge_colors.append('black')

        # pos = nx.spring_layout(S[i], scale=3)
        # pos = nx.planar_layout(S[i], scale=3)

        # We have to do this to circumvent a bug:
        # https://networkx.org/documentation/stable/reference/generated/networkx.drawing.nx_pydot.pydot_layout.html
        H = nx.convert_node_labels_to_integers(difftree, label_attribute='label')
        H_layout = nx.drawing.nx_pydot.pydot_layout(H, prog=NODE_POSITION_LAYOUT, root=rootNode)
        pos = {H.nodes[n]['label']: p for n, p in H_layout.items()}

        new_pos = {}
        for k, v in pos.items():
            new_pos[k] = (POS_SCALING_X * v[0], POS_SCALING_Y * v[1])
        pos = new_pos

        # draw type borders
        nx.draw_networkx_nodes(difftree, pos,
                node_size=int(NODE_SIZE * TYPE_BORDER_SIZE),
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
    argparser.add_argument('--fontsize', nargs='?', default=3, type=int)
    argparser.add_argument('--dpi', nargs='?', default=300, type=int)
    argparser.add_argument('--scalex', nargs='?', default=POS_SCALING_X, type=int)
    argparser.add_argument('--scaley', nargs='?', default=POS_SCALING_Y, type=int)
    argparser.add_argument('--nolabels', action='store_const', const=True, default=False)
    argparser.add_argument('--recursive', action='store_const', const=True, default=False)
#     argparser.add_argument('--pattern', action='store_const', const=True, default=False)
    argparser.add_argument('--format', nargs='?', default="default", type=str)
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

    if args.format == "default":
        NODE_PARSER = parseNodeDefault
    elif args.format == "patternsdebug":
        NODE_PARSER = parseNodeDebugAtomics
    elif args.format == "patternsrelease":
        NODE_PARSER = parseNodeReleaseAtomics

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
