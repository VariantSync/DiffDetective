# Problems with lattice

1. NODER_PARSER in Z. 247 in renderLineGraph
   - fehlt hier ein "global NODER_PARSER"?
   - Variable wird nicht übrschrieben?
   - [Tutorial: How to use global variables in Python](https://thispointer.com/python-how-to-use-global-variables-in-a-function/)

2. Hierarche von Teilgraphen geht nicht (TM)
networkx subgraph -> nicht so möglich, wie gewünscht (keine abgetrennten Teilgraphen und dazwischen keine Verbindungnen)
   - manuelle Position von Clustern bestimmen (graphviz, networtx)
   - [Dokueintrag zu `rank` u.ä.. Geht nur in dot (nicht in fdp)](https://graphviz.org/docs/attrs/rank/)
   - [Dokueintrag: Wie man sog. Cluster erstellt](https://graphviz.org/Gallery/undirected/fdpclust.html)
   - [Zusammenfassung (dot und anderes)](http://www.graphviz.org/pdf/dot.1.pdf)
   - [Graphviz in Python, mit Beispielen](https://graphviz.readthedocs.io/en/stable/examples.html)
   - [Tutorial: CausalNex plotting (networkx)](https://causalnex.readthedocs.io/en/latest/03_tutorial/03_plotting_tutorial.html)
   - [Zusammenfassung von Graphviz](https://pygraphviz.github.io/documentation/stable/pygraphviz.pdf)
