{-# OPTIONS_GHC -Wall -Werror -Wredundant-constraints #-}

module VariationTree where

import Feature.Logic

import Data.List (find, intercalate)
import Data.Maybe (fromJust)

type UUID = Int
type ArtifactReference = String

class VTLabel t where
    makeArtifactLabel :: ArtifactReference -> t f
    makeMappingLabel :: f -> t f
    -- TODO: Move the type constraints down to the instances. This constraint is only required for some instances and other instances might require further constraints.
    featuremapping :: VariationTree t f -> VTNode t f -> f

data VTNode t f = VTNode UUID (t f)

data VTEdge t f = VTEdge {
    childNode  :: VTNode t f,
    parentNode :: VTNode t f
}

data VariationTree t f = VariationTree {
    nodes :: [VTNode t f],
    edges :: [VTEdge t f]
}

getID :: VTNode t f -> UUID
getID (VTNode i _) = i

setID :: VTNode t f -> UUID -> VTNode t f
setID (VTNode _ l) i' = VTNode i' l

withId :: UUID -> [VTNode t f] -> Maybe (VTNode t f)
withId i = find ((i ==) . getID)

fromIds :: [VTNode t f] -> (UUID, UUID) -> VTEdge t f
fromIds nodeSet (from, to) = VTEdge {
    childNode  = fromJust (withId from nodeSet),
    parentNode = fromJust (withId to   nodeSet)
}

fromIndices :: [VTNode t f] -> (Int, Int) -> VTEdge t f
fromIndices nodeSet (from, to) = VTEdge {
    childNode  = nodeSet !! from,
    parentNode = nodeSet !! to
}

parent :: VariationTree t f -> VTNode t f -> Maybe (VTNode t f)
parent tree node = parentNode <$> find (\edge -> childNode edge == node) (edges tree)

root :: (HasNeutral f, VTLabel t) => VTNode t f
root = VTNode 0 (makeMappingLabel ltrue)

makeArtifact :: VTLabel t => UUID -> ArtifactReference -> VTNode t f
makeArtifact i a = VTNode i (makeArtifactLabel a)

makeMapping :: VTLabel t => UUID -> f -> VTNode t f
makeMapping i f = VTNode i (makeMappingLabel f)

fromNodesAndEdges :: (HasNeutral f, VTLabel t) => [VTNode t f] -> [VTEdge t f] -> VariationTree t f
fromNodesAndEdges nodeSet edgeSet =
    let
        allnodes = root:nodeSet
        in
    VariationTree {
        nodes = allnodes,
        edges = edgeSet
    }

fromNodesAndIDEdges :: (HasNeutral f, VTLabel t) => [VTNode t f] -> [(UUID, UUID)] -> VariationTree t f
fromNodesAndIDEdges nodeSet edgeSet = fromNodesAndEdges nodeSet (fmap (fromIds $ root:nodeSet) edgeSet)

fromNodesAndEdgeIndices :: (HasNeutral f, VTLabel t) => [VTNode t f] -> [(Int, Int)] -> VariationTree t f
fromNodesAndEdgeIndices nodeSet edgeSet = fromNodesAndEdges nodeSet (fmap (fromIndices $ root:nodeSet) edgeSet)

nodesWithoutRoot :: (HasNeutral f, VTLabel t) => VariationTree t f -> [VTNode t f]
nodesWithoutRoot tree = [n | n <- nodes tree, n /= root] --delete (root tree) (nodes tree)

instance Eq (VTNode t f) where
    (VTNode name _) == (VTNode name' _) = name == name'

instance Eq (VTEdge t f) where
    e == e' = childNode e == childNode e' && parentNode e == parentNode e'

instance Eq (t f) => Eq (VariationTree t f) where
    a == b = nodes a == nodes b && edges a == edges b

instance Show (t f) => Show (VTNode t f) where
    show (VTNode name label) = mconcat ["(", show name, ", ", show label, ")"]

instance Show (t f) => Show (VTEdge t f) where
    show edge = mconcat [show (childNode edge), " -> ", show (parentNode edge)]

instance Show (t f) => Show (VariationTree t f) where
    show t = "Variation Tree with edges {" ++ intercalate "\n  " ("":(show <$> edges t)) ++ "\n}"
