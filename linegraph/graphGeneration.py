#!/bin/python3

# constants from our Java code
JAVA_TREE_NAME_SEPARATOR = "$$$"
JAVA_ID_LINE_NUMBER_OFFSET = 6 #16
JAVA_ID_DIFF_TYPE_OFFSET = 3 #8
JAVA_ID_DIFFLINE_FROM_OFFSET = 1

# export names
# DIR_SEPARATOR = "$"
DIR_SEPARATOR = "___"

COLORFUL_DELETED = '#A00000'
COLORFUL_INSERTED = '#00A000'
COLORFUL_MACRO = '#579'

# colour of a node shows diff type
DIFFTYPE_ADD_COLOR = 'green' #COLORFUL_INSERTED #
DIFFTYPE_REM_COLOR = 'red' #COLORFUL_DELETED #'#ff9129'
DIFFTYPE_NON_COLOR = '#d1d1e0' # light purple gray

# border colour of a node shows code type
CODE_TYPE_CODE_COLOR = 'black'
CODE_TYPE_OTHER_COLOR = '#3399ff'#COLORFUL_MACRO#
TYPE_BORDER_SIZE = (8.0 / 7.0)

# colour of edges
EDGE_ADD_COLOR = '#bbeb37'
EDGE_REM_COLOR = '#ff9129'
EDGE_NON_COLOR = 'black'

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

def edgeColour(edge):
    if edge.startswith("a"):
        return EDGE_ADD_COLOR
    elif edge.startswith("ba"):
        return EDGE_NON_COLOR
    elif edge.startswith("b"):
        return EDGE_REM_COLOR
    else:
        raise Exception("Cannot parse edge label " + edge)