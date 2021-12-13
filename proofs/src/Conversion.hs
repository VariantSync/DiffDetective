module Conversion where

import Data.Maybe ( mapMaybe )

import Names
import DiffTree
import Edit
import Feature

sound :: (FeatureAnnotation f) => DiffTree f -> Edit f
sound t = Edit {
    editedCodeFragments = mapMaybe codeOf (v t),
    pcInEdit = \time code -> pcInDiffTree time (findNodeWithCode code t),
    editTypes = \code -> d (findNodeWithCode code t)
}

transformPC :: EditPC f -> DiffTreePC f
transformPC = error "lol"

complete :: (FeatureAnnotation f) => Edit f -> DiffTree f
complete = error "not implemented"
--complete edit = DiffTree {
--    v = (\code -> createLeaf code (editTypes edit code) (computeParents)) <$> (editedCodeFragments edit)
--}

{-
sound (complete e) == e
(==) (sound (complete e)) e
-}

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
