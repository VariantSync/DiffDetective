Denke mit Dot (Graphviz) kann man das eventuell hinbekommen. Also man müsste jetzt quasi in die nodes den entsprechenden graphen von dir rendern.

## Was ist der Unterschied zwischen lattice.lg und patterns.lg?
lattice referenziert die patterns und beschreibt die subgraph relation zwischen den patterns.

## Open questions?

- Was ist T?
- Sind Lattices vs Subgraphs verbuggt?

## 1.4.22
1. NODER_PARSER in Z. 247 in renderLineGraph
   - fehlt hier ein "global NODER_PARSER"?
   - Variable wird nicht übrschrieben?
   - <https://thispointer.com/python-how-to-use-global-variables-in-a-function/>

2. Hierarche von Teilgraphen nicht
   - https://graphviz.org/docs/attrs/rank/
   - https://graphviz.org/Gallery/undirected/fdpclust.html
   - http://www.graphviz.org/pdf/dot.1.pdf
   - https://graphviz.readthedocs.io/en/stable/examples.html
 
   Interessante Seiten:
   - https://causalnex.readthedocs.io/en/latest/03_tutorial/03_plotting_tutorial.html
   - https://pygraphviz.github.io/documentation/stable/pygraphviz.pdf
