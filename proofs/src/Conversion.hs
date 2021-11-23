module Conversion where

import Data.Maybe ( mapMaybe )

import Names
import DiffTree
import Edit
import Feature

sound :: (Feature f) => DiffTree f -> Edit f
sound t = Edit {
    editedCodeFragments = mapMaybe codeOf (v t),
    pcInEdit = \time code -> pcInDiffTree time (findNodeWithCode code t),
    editTypes = \code -> d (findNodeWithCode code t)
}

transformPC :: EditPC f -> DiffTreePC f
transformPC = error "lol"

complete :: (Feature f) => Edit f -> DiffTree f
complete = error "not implemented"
--complete edit = DiffTree {
--    v = (\code -> createLeaf code (editTypes edit code) (computeParents)) <$> (editedCodeFragments edit)
--}

{- Actually, I have to prove that sound . complete = complete . sound = id. -}
