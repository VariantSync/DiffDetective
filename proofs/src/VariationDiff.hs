{-# LANGUAGE LambdaCase #-}

module VariationDiff where

import Data.List ( intercalate )

import VariationTree
import Time
import Feature.Logic

type Delta l f = Either (VTNode l f) (VTEdge l f) -> DiffType

data VariationDiff l f = VariationDiff [VTNode l f] [VTEdge l f] (Delta l f)

project :: Time -> VariationDiff l f -> VariationTree l f
project t (VariationDiff nodes edges delta) = VariationTree
    (filter (existsAtTime t . delta . Left)  nodes)
    (filter (existsAtTime t . delta . Right) edges)

-- We just assume that the UUIDs stored in both trees are unique (i.e., all ids in old are not in new and vice versa)
-- We further assume that the root has always UUID zero as it is constant.
-- Otherwise this function as well as the equality checks afterwards are tremendously more complex.
naiveDiff :: (HasNeutral f, Composable f, VTLabel l) => VariationTree l f -> VariationTree l f -> VariationDiff l f
naiveDiff (VariationTree nodesBefore edgesBefore) (VariationTree nodesAfter edgesAfter) =
    VariationDiff
    (root : nodesWithoutRoot (nodesBefore <> nodesAfter))
    (edgesBefore <> edgesAfter)
    delta
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

-- These data types are just used for pretty printing
data EditedNode l f = EditedNode (VTNode l f) DiffType
data EditedEdge l f = EditedEdge (EditedNode l f) (EditedNode l f) DiffType

fromNode :: Delta l f -> VTNode l f -> EditedNode l f
fromNode delta node = EditedNode node (delta . Left $ node)

fromEdge :: Delta l f -> VTEdge l f -> EditedEdge l f
fromEdge delta edge = EditedEdge (fromNode delta $ childNode edge) (fromNode delta $ parentNode edge) (delta . Right $ edge)

instance Show (l f) => Show (EditedNode l f) where
    show (EditedNode (VTNode name label) delta) = mconcat ["(", show delta, ", ", show label, ", ", show name, ")"]

instance Show (l f) => Show (EditedEdge l f) where
    show (EditedEdge from to delta) = mconcat [show from, " -", show delta, "-> ", show to]

instance Show (l f) => Show (VariationDiff l f) where
    show (VariationDiff _ edges delta) =
        "Variation Diff with edges {" ++ intercalate "\n  " ("":(
            show . fromEdge delta <$> edges
        )) ++ "\n}"
