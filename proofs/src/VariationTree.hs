{-# OPTIONS_GHC -Wall -Werror -Wredundant-constraints #-}

module VariationTree where

import Logic

import Data.List (find, intercalate)
import Data.Maybe (fromJust)

type UUID = Int
type ArtifactReference = String

class NodeTypes t where
    featuremapping :: Logic f => VariationTree t f -> VTNode t f -> f

data PaperTypes f =
      Artifact ArtifactReference
    | Mapping f
    | Else
    deriving Show

data WithElif f =
      WEArtifact ArtifactReference
    | WEMapping f
    | WEElse
    | WEElif f
    deriving Show

instance NodeTypes PaperTypes where
    featuremapping tree node@(VTNode i label) = case label of
        Artifact _ -> featuremapping tree $ fromJust $ parent tree node
        Mapping f -> f
        Else -> lnot $ featuremapping tree $ fromJust $ parent tree node

instance NodeTypes WithElif where
    featuremapping tree node@(VTNode i label) = case label of
        -- TODO: Can we directly say that we just invoke the functions of PaperTypes for WEArtifact and WEMapping?
        WEArtifact a -> featuremapping tree $ fromJust $ parent tree node
        WEMapping f -> f
        WEElse ->            notTheOtherBranches
        WEElif f -> land [f, notTheOtherBranches]
        where
            notTheOtherBranches = land $ lnot <$> branches tree (fromJust (parent tree node))

            branches :: VariationTree WithElif f -> VTNode WithElif f -> [f]
            branches _ (VTNode _ (WEMapping f)) = [f]
            branches tree node@(VTNode _ (WEElif f)) = f:branches tree (fromJust (parent tree node))
            branches tree node = branches tree (fromJust (parent tree node))


data VTNode t f = VTNode UUID (t f)

data VTEdge t f = VTEdge {
    childNode  :: VTNode t f,
    parentNode :: VTNode t f
}

data VariationTree t f = VariationTree {
    nodes :: [VTNode t f],
    edges :: [VTEdge t f]
}

type DefaultVariationTree f = VariationTree PaperTypes f

getID :: VTNode t f -> UUID
getID (VTNode i l) = i

setID :: VTNode t f -> UUID -> VTNode t f
setID (VTNode i l) i' = VTNode i' l

withId :: UUID -> [VTNode t f] -> Maybe (VTNode t f)
withId id = find ((id ==) . getID)

fromIds :: [VTNode t f] -> (UUID, UUID) -> VTEdge t f
fromIds nodes (from, to) = VTEdge {
    childNode  = fromJust (withId from nodes),
    parentNode = fromJust (withId to   nodes)
}

fromIndices :: [VTNode t f] -> (Int, Int) -> VTEdge t f
fromIndices nodes (from, to) = VTEdge {
    childNode  = nodes !! from,
    parentNode = nodes !! to
}

parent :: VariationTree t f -> VTNode t f -> Maybe (VTNode t f)
parent tree node = parentNode <$> find (\edge -> childNode edge == node) (edges tree)

instance Logic f => Eq (PaperTypes f) where
    x == y = case (x, y) of
        (Artifact a, Artifact b) -> a == b
        (Mapping a, Mapping b) -> lequivalent a b
        (Else, Else) -> True
        (_, _) -> False

instance Eq (VTNode t f) where
    (VTNode name label) == (VTNode name' label') = name == name'

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

--- Default implementation


root :: Logic f => VTNode PaperTypes f
root = VTNode 0 (Mapping ltrue)

makeArtifact :: UUID -> ArtifactReference -> VTNode PaperTypes f
makeArtifact id a = VTNode id (Artifact a)

makeMapping :: UUID -> f -> VTNode PaperTypes f
makeMapping id f = VTNode id (Mapping f)

makeElse :: UUID -> VTNode PaperTypes f
makeElse id = VTNode id Else

fromNodesAndEdges :: (Logic f) => [VTNode PaperTypes f] -> [VTEdge PaperTypes f] -> VariationTree PaperTypes f
fromNodesAndEdges nodes edges =
    let
        allnodes = root:nodes
        in
    VariationTree {
        nodes = allnodes,
        edges = edges
    }

fromNodesAndIDEdges :: (Logic f) => [VTNode PaperTypes f] -> [(UUID, UUID)] -> VariationTree PaperTypes f
fromNodesAndIDEdges nodes edges = fromNodesAndEdges nodes (fmap (fromIds $ root:nodes) edges)

fromNodesAndEdgeIndices :: (Logic f) => [VTNode PaperTypes f] -> [(Int, Int)] -> VariationTree PaperTypes f
fromNodesAndEdgeIndices nodes edges = fromNodesAndEdges nodes (fmap (fromIndices $ root:nodes) edges)

nodesWithoutRoot :: (Eq f, Logic f) => VariationTree PaperTypes f -> [VTNode PaperTypes f]
nodesWithoutRoot tree = [n | n <- nodes tree, n /= root] --delete (root tree) (nodes tree)