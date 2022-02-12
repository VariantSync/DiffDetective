module Conversion where

import Data.Maybe ( catMaybes )

import Definitions
import DiffTree
import Edit
import Feature
import Util

sound :: (FeatureAnnotation f) => VDT f -> Edit f
sound vdt = Edit {
    editedCodeFragments = catMaybes (codeOf <$> nodes vdt),
    editTypes = \code -> diffType (findNodeWithCode code vdt),
    pc = \time code -> pcInVDT time vdt (findNodeWithCode code vdt)
}

{- |
This function is an intermediate step in converting edits to VDTs.
For each code fragment s, a code node is created.
For each code fragment s, a mapping node is created for each time t at which s exists (e.g., ADD nodes only exist after the edit, see 'Definitions.existsAtTime').
Each mapping node represents exactly one presence condition.
The node of each code fragment s is then connected to its respective mapping nodes via 'VDTEdge's.

Example:
Given edited code fragments /[1, 2, 3]/
with diff types /d(1) = ADD/, /d(2) = REM/, and /d(3) = NON/
and presence conditions /PC_a(1) = X/, /PC_b(2) = Y/, /PC_b(3) = Y/, and /PC_a(3) = X/
then the following VDT Edites are constructed (note that presence conditions are only defined at times where the respective code fragments exist):
[
    VDTEdge {
        child  = VDTNode { label = Code "1", difftype = ADD},
        parent = VDTNode { label = Mapping X, difftype = NON},
        time   = AFTER
    },
    VDTEdge {
        child  = VDTNode { label = Code "2", difftype = REM},
        parent = VDTNode { label = Mapping Y, difftype = NON},
        time   = BEFORE
    },
    VDTEdge {
        child  = VDTNode { label = Code "3", difftype = NON},
        parent = VDTNode { label = Mapping X, difftype = NON},
        time   = BEFORE
    },
    VDTEdge {
        child  = VDTNode { label = Code "3", difftype = NON},
        parent = VDTNode { label = Mapping Y, difftype = NON},
        time   = AFTER
    },
]
-}
createPartialVDT :: (FeatureAnnotation f) => Edit f -> [VDTEdge f]
createPartialVDT edit =
    --- for each edited code fragment s
    editedCodeFragments edit >>= \s ->
        let --- create a code node
            d = editTypes edit s
            codenode = createCodeNode s d
        in
            {-
            1. Create a mapping node for the presence condition of s at each time s exists.
            2. Connect each created node with s, such that s is child and the mapping node is the parent.
            This will create at least 1 and at most 2 mapping nodes.
            -}
            [VDTEdge { child = codenode, parent = createMappingNode (pc edit t s) NON, time = t} | t <- always, existsAtTime t d]

complete :: (FeatureAnnotation f) => Edit f -> VDT f
complete edit =
    let
        codeToPCEdges = createPartialVDT edit

        root = createRoot
        macroNodes = removeDuplicates $ parent <$> codeToPCEdges
        codeNodes  = removeDuplicates $ child <$> codeToPCEdges
        -- create edges to the root from each macro node m (at all times t at which m exists).
        pcToRootEdges = [VDTEdge { child = m, parent = root, time = t} | m <- macroNodes, t <- fromDiffType (diffType m)]
        in
    VDT {
        nodes = root:(macroNodes++codeNodes),
        edges = codeToPCEdges++pcToRootEdges
    }

{-
I have to prove that sound . complete = id and complete . sound = id w.r.t. to isomorphism (implemented as Eq currently).

To prove completeness of DiffTrees we have to show that every edit can be expressed as a DiffTree.
To do so, we build an isomorphism 'complete :: Edit -> DiffTree'.
Just building a function Edit -> DiffTree is trivial (actually for any inhabited type), and does not prove that the produced DiffTree in fact represents the edit.
To prove that the produced DiffTree represents the edit, we show that we can retrieve the original edit from the DiffTree.
Thus we have to show that 'complete' is an isomorphism.
To do so, we want to show that
    'forall e :: Edit: sound (complete e) == e'

To prove soundness, we do the opposite.
We show that any DiffTree in fact represents an edit.
    'forall d :: DiffTree: complete (sound d) == d'

How to do this properly is the question.
Using Haskell+Agda? Using Idris? Using Pen&Paper?
-}

proofComplete :: FeatureAnnotation f => Edit f -> Bool
proofComplete edit = sound (complete edit) `Edit.isomorph` edit

proofSound :: FeatureAnnotation f => VDT f -> Bool
proofSound vdt = complete (sound vdt) `DiffTree.isomorph` vdt

{-
Theorem: forall e. proofComplete e
Proof:

proofComplete edit
    = sound (complete edit) `Edit.isomorph` edit
    = edit `Edit.isomorph` sound (complete edit) // TODO: Prove commutativity of Edit.isomorph
// let sce = sound (complete edit)
    = let
        editedCodeX = editedCodeFragments edit
        editedCodeY = editedCodeFragments sce
        in
    (editedCodeX == editedCodeY)
    && propertiesEqual (editTypes edit) (editTypes edit) editedCodeX
    && and (fmap (\s -> pcEqualsFor (editTypes edit s) edit sce s) editedCodeX)

Interleave:
editedCodeY = editedCodeFragments sce
            = editedCodeFragments (sound c) -- where c = complete edit
            =  editedCodeFragments $ Edit {
                editedCodeFragments = catMaybes (codeOf <$> nodes c),
                editTypes = \code -> diffType (findNodeWithCode code c),
                pc = \time code -> pcInVDT time c (findNodeWithCode code c)
            }
            = catMaybes (codeOf <$> nodes c)
            = catMaybes (codeOf <$> nodes (complete edit))
          * =
-}

{- *
nodes (complete edit) 
    = nodes $ let
        codeToPCEdges = createPartialVDT edit

        root = createRoot
        macroNodes = removeDuplicates $ parent <$> codeToPCEdges
        codeNodes  = removeDuplicates $ child <$> codeToPCEdges
        -- create edges to the root from each macro node m (at all times t at which m exists).
        pcToRootEdges = [VDTEdge { child = m, parent = root, time = t} | m <- macroNodes, t <- fromDiffType (diffType m)]
        in
    VDT {
        nodes = root:(macroNodes++codeNodes),
        edges = codeToPCEdges++pcToRootEdges
    }
    = let
        codeToPCEdges = createPartialVDT edit

        root = createRoot
        macroNodes = removeDuplicates $ parent <$> codeToPCEdges
        codeNodes  = removeDuplicates $ child <$> codeToPCEdges
        -- create edges to the root from each macro node m (at all times t at which m exists).
        pcToRootEdges = [VDTEdge { child = m, parent = root, time = t} | m <- macroNodes, t <- fromDiffType (diffType m)]
        in
        root:(macroNodes++codeNodes)
    = let
        codeToPCEdges = createPartialVDT edit
        in
        createRoot:((removeDuplicates $ parent <$> codeToPCEdges)++(removeDuplicates $ child <$> codeToPCEdges))

-- I think we will definitely need some pre and post conditions for complex functions here.
-- We now have to unfold createPartialVDT which is huge again.
-- It would be better if we could avoid some computations right away by knowing that codeOf works correctly (in terms of pre- and postconditions).
-- Then we could simplify all of this within the place where * is used directly perhaps.
-}