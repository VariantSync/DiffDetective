module DiffTree where

import Data.List ( find )

import Names
import Feature ( Feature, FeatureAnnotation )

data CodeType = CODE | MAPPING
data Label f = Leaf CodeFragment | Inner f
type DiffTreePC f = Time -> DiffTreeNode f -> f

data DiffTreeNode f = DiffTreeNode {
    label :: Label f,
    codeType :: CodeType,
    diffType :: DiffType,
    parent :: Time -> Maybe (DiffTreeNode f)
}

data DiffTree f = DiffTree {
    v :: [DiffTreeNode f] -- TODO: This format is shit. We need a generic tree structure. Either store only leaves or make a rose tree with a parent function.
}

{- called F in the paper -}
featureOf :: (FeatureAnnotation f) => DiffTreeNode f -> f
featureOf v = case label v of
    Leaf code -> mempty
    Inner fm  -> fm

{- called pc in the paper -}
pcInDiffTree :: (FeatureAnnotation f) => DiffTreePC f
pcInDiffTree t v = case parent v t of
    Nothing -> featureOf v
    Just p -> mappend (featureOf v) (pcInDiffTree t p)

codeOf :: DiffTreeNode f -> Maybe CodeFragment
codeOf v = case label v of
    Leaf c -> Just c
    Inner _ -> Nothing

equalsCodeFragment :: CodeFragment -> DiffTreeNode f -> Bool
equalsCodeFragment code node = case label node of
    Leaf c -> c == code
    Inner _ -> False

findNodeWithCode :: CodeFragment -> DiffTree f -> DiffTreeNode f
findNodeWithCode code tree = case find (equalsCodeFragment code) (v tree) of
            Nothing -> error "Cannot compute pc of code that was not part of the initial DiffTree."
            Just node -> node

createLeaf :: CodeFragment -> DiffType -> (Time -> Maybe (DiffTreeNode f)) -> DiffTreeNode f
createLeaf code diffType parents = DiffTreeNode {
    label = Leaf code,
    codeType = CODE,
    diffType = diffType,
    parent = parents
}
