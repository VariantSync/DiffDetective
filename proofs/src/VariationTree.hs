-- {-# OPTIONS_GHC -Wall -Werror -Wredundant-constraints #-}

module VariationTree where

import Feature.Logic

import Data.List
import Data.Maybe
-- import Data.Map

type UUID = Int
type ArtifactReference = String
type WellformednessConstraint l f = VariationTree l f -> Bool

-- getAllChildrenLists :: VariationTree l f -> Map (VTNode l f) [VTNode l f]
-- getAllChildrenLists (VariationTree _ edges) = Data.List.foldl processEdge empty edges
--     where
--         processEdge mp edge = insertWith (++) (parentNode edge) [childNode edge] mp

-- getReachableNodes :: VariationTree l f -> VTNode l f -> [VTNode l f]
-- getReachableNodes tree node =
--     let 
--         childrenMap = getAllChildrenLists tree
--         childrenOf node = fromMaybe [] (Data.Map.lookup node childrenMap)
--         in
--     node : childrenMap

isTree :: (VTLabel l, HasNeutral f, Composable f) => WellformednessConstraint l f
isTree tree@(VariationTree nodes edges) = and [
        root `elem` nodes,
        isNothing (parent tree root),
        nonRootNodesHaveExactlyOneParent
        -- allPathsStartAtRoot
    ]
    where
        nonRootNodesHaveExactlyOneParent = all hasExactlyOneParent (nodesWithoutRoot nodes)
        hasExactlyOneParent node = length (Data.List.filter (\edge -> childNode edge == node) edges) == 1
        -- allPathsStartAtRoot = getReachableNodes tree root == nodes

class VTLabel l where
    makeArtifactLabel :: ArtifactReference -> l f
    makeMappingLabel :: (Composable f) => f -> l f
    -- isMapping :: l f -> Bool

    featuremapping :: VariationTree l f -> VTNode l f -> f
    presencecondition :: VariationTree l f -> VTNode l f -> f

    wellformednessConstraints :: (HasNeutral f, Composable f) => [WellformednessConstraint l f]
    wellformednessConstraints = [isTree]

data VTNode l f = VTNode UUID (l f)

data VTEdge l f = VTEdge {
    childNode  :: VTNode l f,
    parentNode :: VTNode l f
}

data VariationTree l f = VariationTree [VTNode l f] [VTEdge l f]

getID :: VTNode l f -> UUID
getID (VTNode i _) = i

setID :: VTNode l f -> UUID -> VTNode l f
setID (VTNode _ l) i' = VTNode i' l

withId :: UUID -> [VTNode l f] -> Maybe (VTNode l f)
withId i = find ((i ==) . getID)

fromIds :: [VTNode l f] -> (UUID, UUID) -> VTEdge l f
fromIds nodeSet (from, to) = VTEdge {
    childNode  = fromJust (withId from nodeSet),
    parentNode = fromJust (withId to   nodeSet)
}

fromIndices :: [VTNode l f] -> (Int, Int) -> VTEdge l f
fromIndices nodeSet (from, to) = VTEdge {
    childNode  = nodeSet !! from,
    parentNode = nodeSet !! to
}

parent :: VariationTree l f -> VTNode l f -> Maybe (VTNode l f)
parent (VariationTree _ edges) v = fmap parentNode (find (\edge -> childNode edge == v) edges)

children :: VariationTree l f -> VTNode l f -> [VTNode l f]
children (VariationTree _ edges) node = childNode <$> Data.List.filter (\edge -> parentNode edge == node) edges

root :: (HasNeutral f, Composable f, VTLabel l) => VTNode l f
root = VTNode 0 (makeMappingLabel ltrue)

makeArtifact :: VTLabel l => UUID -> ArtifactReference -> VTNode l f
makeArtifact i a = VTNode i (makeArtifactLabel a)

makeMapping :: (Composable f, VTLabel l) => UUID -> f -> VTNode l f
makeMapping i f = VTNode i (makeMappingLabel f)

fromNodesAndEdges :: (HasNeutral f, Composable f, VTLabel l) => [VTNode l f] -> [VTEdge l f] -> VariationTree l f
fromNodesAndEdges nodeSet edgeSet =
    let
        allnodes = root:nodeSet
        in
    VariationTree allnodes edgeSet

fromNodesAndIDEdges :: (HasNeutral f, Composable f, VTLabel l) => [VTNode l f] -> [(UUID, UUID)] -> VariationTree l f
fromNodesAndIDEdges nodeSet edgeSet = fromNodesAndEdges nodeSet (fmap (fromIds $ root:nodeSet) edgeSet)

fromNodesAndEdgeIndices :: (HasNeutral f, Composable f, VTLabel l) => [VTNode l f] -> [(Int, Int)] -> VariationTree l f
fromNodesAndEdgeIndices nodeSet edgeSet = fromNodesAndEdges nodeSet (fmap (fromIndices $ root:nodeSet) edgeSet)

nodesWithoutRoot :: (HasNeutral f, Composable f, VTLabel l) => [VTNode l f] -> [VTNode l f]
nodesWithoutRoot nodes = [n | n <- nodes, n /= root] --delete (root tree) (nodes tree)

instance Eq (VTNode l f) where
    (VTNode name _) == (VTNode name' _) = name == name'

instance Ord (VTNode l f) where
    (VTNode i _) <= (VTNode j _) = i <= j

instance Eq (VTEdge l f) where
    e == e' = childNode e == childNode e' && parentNode e == parentNode e'

instance Eq (l f) => Eq (VariationTree l f) where
    (VariationTree nodes edges) == (VariationTree nodes' edges') = nodes == nodes' && edges == edges'

instance Show (l f) => Show (VTNode l f) where
    show (VTNode name label) = mconcat ["(", show label, ", ", show name, ")"]

instance Show (l f) => Show (VTEdge l f) where
    show edge = mconcat [show (childNode edge), " -> ", show (parentNode edge)]

instance Show (l f) => Show (VariationTree l f) where
    show (VariationTree _ edges) = "Variation Tree with edges {" ++ intercalate "\n  " ("":(show <$> edges)) ++ "\n}"

-- Some utility functions for labels
ofParent :: (VTNode t f -> f) -> VariationTree t f -> VTNode t f -> Maybe f
ofParent property tree node = property <$> parent tree node

featureMappingOfParent :: VTLabel t =>
    VariationTree t f -> VTNode t f -> Maybe f
featureMappingOfParent tree = ofParent (featuremapping tree) tree

presenceConditionOfParent :: VTLabel t =>
    VariationTree t f -> VTNode t f -> Maybe f
presenceConditionOfParent tree = ofParent (presencecondition tree) tree