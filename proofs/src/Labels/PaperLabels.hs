{-# LANGUAGE GADTs #-}
{-# LANGUAGE StandaloneDeriving #-}

module Labels.PaperLabels where

import VariationTree
import Feature.Logic
import Data.Maybe ( fromJust )

data PaperLabels f where
    Artifact :: ArtifactReference -> PaperLabels f
    Mapping :: Composable f => f -> PaperLabels f
    Else :: (Composable f, Negatable f) => PaperLabels f

deriving instance Show f => Show (PaperLabels f)

isElseNode :: VTNode PaperLabels f -> Bool
isElseNode (VTNode _ Else) = True
isElseNode _ = False

isMappingNode :: VTNode PaperLabels f -> Bool
isMappingNode (VTNode _ (Mapping _)) = True
isMappingNode _ = False

allElsesBelowIfs :: (HasNeutral f, Composable f) => WellformednessConstraint PaperLabels f
allElsesBelowIfs tree@(VariationTree nodes _) = and $ isBelowNonRootIf <$> filter isElseNode nodes
    where isBelowNonRootIf = (\n -> isMappingNode n && root /= n) . fromJust . parent tree

allMappingsHaveAtMostOneElse :: WellformednessConstraint PaperLabels f
allMappingsHaveAtMostOneElse tree@(VariationTree nodes _) = and $ hasAtMostOneElse <$> filter isMappingNode nodes
    where hasAtMostOneElse node = length (filter isElseNode $ children tree node) <= 1

instance VTLabel PaperLabels where
    makeArtifactLabel = Artifact
    makeMappingLabel = Mapping

    featuremapping tree node@(VTNode _ label) = case label of
        Artifact _ -> parentFM
        Mapping f -> f
        Else -> lnot parentFM
        where parentFM = fromJust $ featureMappingOfParent tree node
    presencecondition tree node@(VTNode _ label) = case label of
        Artifact _ -> parentPC
        Mapping f -> land [f, parentPC]
        Else -> land [featuremapping tree node, presencecondition tree $ getParent (getParent node)]
        where
            parentPC = fromJust $ presenceConditionOfParent tree node
            getParent = fromJust . parent tree
    
    wellformednessConstraints = [isTree, allElsesBelowIfs, allMappingsHaveAtMostOneElse]

instance Comparable f => Eq (PaperLabels f) where
    x == y = case (x, y) of
        (Artifact a, Artifact b) -> a == b
        (Mapping a, Mapping b) -> lequivalent a b
        (Else, Else) -> True
        (_, _) -> False

makeElse :: (Composable f, Negatable f) => UUID -> VTNode PaperLabels f
makeElse i = VTNode i Else

type DefaultVariationTree f = VariationTree PaperLabels f
type DefaultVTNode f = VTNode PaperLabels f
