module VariationTree where

import Logic

import Data.List (find, intercalate)
import Data.Maybe (fromJust)

type UUID = Int
type ArtifactReference = String

data Label f =
      Artifact ArtifactReference
    | Mapping f
    | Else
    deriving Show

data VTNode f = VTNode UUID (Label f)

data VTEdge f = VTEdge {
    child  :: VTNode f,
    parent :: VTNode f
}

data VariationTree f = VariationTree {
    nodes :: [VTNode f],
    edges :: [VTEdge f]
}

root :: Logic f => VTNode f
root = VTNode 0 (Mapping ltrue)

makeArtifact :: UUID -> ArtifactReference -> VTNode f
makeArtifact id a = VTNode id (Artifact a)

makeMapping :: UUID -> f -> VTNode f
makeMapping id f = VTNode id (Mapping f)

makeElse :: UUID -> VTNode f
makeElse id = VTNode id Else

getID :: VTNode f -> UUID
getID (VTNode i l) = i

setID :: VTNode f -> UUID -> VTNode f
setID (VTNode i l) i' = VTNode i' l

withId :: UUID -> [VTNode f] -> Maybe (VTNode f)
withId id = find ((id ==) . getID)

fromIds :: [VTNode f] -> (UUID, UUID) -> VTEdge f
fromIds nodes (from, to) = VTEdge {
    child  = fromJust (withId from nodes),
    parent = fromJust (withId to   nodes)
}

fromIndices :: [VTNode f] -> (Int, Int) -> VTEdge f
fromIndices nodes (from, to) = VTEdge {
    child  = nodes !! from,
    parent = nodes !! to
}

fromNodesAndEdges :: (Logic f) => [VTNode f] -> [VTEdge f] -> VariationTree f
fromNodesAndEdges nodes edges =
    let
        allnodes = root:nodes
        in
    VariationTree {
        nodes = allnodes,
        edges = edges
    }

fromNodesAndIDEdges :: (Logic f) => [VTNode f] -> [(UUID, UUID)] -> VariationTree f
fromNodesAndIDEdges nodes edges = fromNodesAndEdges nodes (fmap (fromIds $ root:nodes) edges)

fromNodesAndEdgeIndices :: (Logic f) => [VTNode f] -> [(Int, Int)] -> VariationTree f
fromNodesAndEdgeIndices nodes edges = fromNodesAndEdges nodes (fmap (fromIndices $ root:nodes) edges)

nodesWithoutRoot :: (Eq f, Logic f) => VariationTree f -> [VTNode f]
nodesWithoutRoot tree = [n | n <- nodes tree, n /= root] --delete (root tree) (nodes tree)

instance Eq f => Eq (Label f) where
    x == y = case (x, y) of
        (Artifact a, Artifact b) -> a == b
        (Mapping a, Mapping b) -> a == b
        (Else, Else) -> True
        (_, _) -> False

instance Eq f => Eq (VTNode f) where
    (VTNode name label) == (VTNode name' label') =
        name == name' && label == label'

instance Eq f => Eq (VTEdge f) where
    e == e' = child e == child e' && parent e == parent e'

instance Eq f => Eq (VariationTree f) where
    a == b = nodes a == nodes b && edges a == edges b

instance Show f => Show (VTNode f) where
    show (VTNode name label) = mconcat ["(", show name, ", ", show label, ")"]

instance Show f => Show (VTEdge f) where
    show edge = mconcat [show (child edge), " -> ", show (parent edge)]

instance Show f => Show (VariationTree f) where
    show t = "Variation Tree with edges {" ++ intercalate "\n  " ("":(show <$> edges t)) ++ "\n}"