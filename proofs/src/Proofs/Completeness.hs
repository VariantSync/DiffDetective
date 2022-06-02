module Proofs.Completeness where

{-
Definitions for fast lookup:

project :: Time -> VariationDiff l f -> VariationTree l f
project t (VariationDiff nodes edges delta) = VariationTree
    (filter (existsAtTime t . delta . Left)  nodes)
    (filter (existsAtTime t . delta . Right) edges)

existsAtTime :: Time -> DiffType -> Bool
existsAtTime BEFORE ADD = False
existsAtTime AFTER REM = False
existsAtTime _ _ = True
-}

{-
--- Proof of Completeness ---

Theorem:
    Any two Variation Trees
        a :: VariationTree l f
        a = VariationTree nodesBefore edgesBefore
        b :: VariationTree l f
        b = VariationTree nodesAfter edgesAfter
    can be diffed as a VariationTree by a at least one diffing function d such that the following equalities hold:

            project BEFORE (d a b) == a
            project AFTER  (d a b) == b

Proof:
    We prove that there exists at least one diffing function d satisfying the above equailities by showing that d = naiveDiff is a valid diffing function:

   project BEFORE (naiveDiff a b)
== // Substitute a b
    project BEFORE (naiveDiff (VariationTree nodesBefore edgesBefore) (VariationTree nodesAfter edgesAfter))
== // Substitute naiveDiff
    project BEFORE (VariationDiff (root : nodesWithoutRoot (nodesBefore <> nodesAfter)) (edgesBefore <> edgesAfter) delta)
== // Substitute project
    VariationTree
    (filter (existsAtTime BEFORE . delta . Left)  (root : nodesWithoutRoot (nodesBefore <> nodesAfter)))
    (filter (existsAtTime BEFORE . delta . Right) (edgesBefore <> edgesAfter))
== // By definition of delta we know that (forall e in edgesBefore. delta (Right e) == REM) and (forall e in edgesAfter. delta (Right e) == ADD)
   // By definition of existsAtTime we know that (existsAtTime BEFORE x) is true iff (x /= ADD).
   // Thus, exactly the edges in edgesBefore exist at time BEFORE.
   // We get:
    VariationTree
    (filter (existsAtTime BEFORE . delta . Left)  (root : nodesWithoutRoot (nodesBefore <> nodesAfter)))
    edgesBefore
== // Substitute nodesWithoutRoot
    VariationTree
    (filter (existsAtTime BEFORE . delta . Left)  (root : [n | n <- (nodesBefore <> nodesAfter), n /= root]))
    edgesBefore
== // By definition of delta we know that (forall n in nodesBefore. delta (Left n) == REM) and (forall n in nodesAfter. delta (Left n) == ADD) and delta (Left root) = NON.
   // By definition of existsAtTime we know that (existsAtTime BEFORE x) is true iff (x /= ADD).
   // Thus, all nodes in nodesBefore and the root exist at time BEFORE but not the nodes in nodesAfter.
   // We get:
    VariationTree
    (root : [n | n <- nodesBefore, n /= root])
    edgesBefore
== // Assuming that the root ways at the first index of nodesBefore, we get: (This assumption is unnecessary when considering the node list as a set)
    VariationTree
    nodesBefore
    edgesBefore
== a

where
    delta = \case
        Left node ->
            if node == root then
                NON
            else if node `elem` nodesBefore then
                REM
            else if node `elem` nodesAfter then
                ADD
            else
                error "Given node is not part of this Variation Diff!"
        Right edge ->
            if edge `elem` edgesBefore then
                REM
            else if edge `elem` edgesAfter then
                ADD
            else
                error "Given edge is not part of this Variation Diff!"

    The other proof for project AFTER  (naiveDiff a b) == b is analoguous, we just have to replace all occurences of BEFORE in the equations and the reasoning by AFTER.
    [ ]

Note that we cannot prove that all possible functions of type "VariationTree l f -> VariationTree l f -> VariationDiff l f" are valid diff as these functions could
create any resulting diff (for example an empty diff). In fact, the above equalities are axioms any diff has to fulfill to be valid and we showed that there exists at
least one valid diff.
-}
