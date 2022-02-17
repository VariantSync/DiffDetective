{-# LANGUAGE LambdaCase #-}

module VariationDiff where

import Data.List ( intercalate )

import VariationTree
import Time
import Logic

type Delta t f = Either (VTNode t f) (VTEdge t f) -> DiffType

data VariationDiff t f = VariationDiff {
    nodes :: [VTNode t f],
    edges :: [VTEdge t f],
    delta :: Delta t f
}

project :: Time -> VariationDiff t f -> VariationTree t f
project t diff = VariationTree {
    VariationTree.nodes = filter (existsAtTime t . delta diff . Left) (VariationDiff.nodes diff),
    VariationTree.edges = filter (existsAtTime t . delta diff . Right) (VariationDiff.edges diff)
}

-- We just assume that the UUIDs stored in both trees are unique (i.e., all ids in old are not in new and vice versa)
-- We further assume that the root has always UUID zero as it is constant.
-- Otherwise this function as well as the equality checks afterwards are tremendously more complex.
stupidDiff :: (Eq f, Logic f) => VariationTree PaperTypes f -> VariationTree PaperTypes f -> VariationDiff PaperTypes f
stupidDiff old new =
    let
        nodesBefore = nodesWithoutRoot old
        nodesAfter  = nodesWithoutRoot new
        edgesBefore = VariationTree.edges old
        edgesAfter  = VariationTree.edges new
    in
        VariationDiff {
            VariationDiff.nodes = root : nodesBefore <> nodesAfter,
            VariationDiff.edges = edgesBefore <> edgesAfter,
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
        }

-- This data type is just used for pretty printing
data EditedEdge t f = EditedEdge (VTNode t f) (VTNode t f) DiffType

fromEdge :: Delta t f -> VTEdge t f -> EditedEdge t f
fromEdge delta edge = EditedEdge (childNode edge) (childNode edge) (delta . Right $ edge)

instance Show (t f) => Show (EditedEdge t f) where
    show (EditedEdge from to delta) = mconcat [show from, " -", show delta, "-> ", show to]

instance Show (t f) => Show (VariationDiff t f) where
    show diff =
        "Variation Diff with edges {" ++ intercalate "\n  " ("":(
            show . fromEdge (VariationDiff.delta diff) <$> VariationDiff.edges diff
        )) ++ "\n}"