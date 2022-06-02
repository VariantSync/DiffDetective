{-# LANGUAGE GADTs #-}

module Labels.WithElif where

import VariationTree
import Data.Maybe (fromJust)
import Feature.Logic

data WithElif f where
    Artifact :: ArtifactReference -> WithElif f
    Mapping :: Composable f => f -> WithElif f
    Else :: (Composable f, Negatable f) => WithElif f
    Elif :: (Composable f, Negatable f) => f -> WithElif f

instance VTLabel WithElif where
    makeArtifactLabel = Artifact
    makeMappingLabel = Mapping

    featuremapping tree node@(VTNode _ label) = case label of
        Artifact _ -> fromJust $ featureMappingOfParent tree node
        Mapping f -> f
        Else ->            notTheOtherBranches tree node
        Elif f -> land [f, notTheOtherBranches tree node]

    presencecondition tree node@(VTNode _ label) = case label of
        Artifact _ -> parentPC
        Mapping f -> land [f, parentPC]
        Else ->   land [
            featuremapping tree node,
            presencecondition tree (getParent (correspondingIf tree node))]
        Elif _ -> land [
            featuremapping tree node,
            presencecondition tree (getParent (correspondingIf tree node))] 
        where
            parentPC = fromJust $ presenceConditionOfParent tree node
            getParent = fromJust . parent tree

notTheOtherBranches :: (Composable f, Negatable f) =>
    VariationTree WithElif f -> VTNode WithElif f -> f
notTheOtherBranches tree node = land $ lnot <$> branchesAbove tree node

branchesAbove :: VariationTree WithElif f -> VTNode WithElif f -> [f]
branchesAbove tree node = branches tree (fromJust (parent tree node))

branches :: VariationTree WithElif f -> VTNode WithElif f -> [f]
branches _ (VTNode _ (Mapping f)) = [f]
branches tree node@(VTNode _ (Elif f)) = f : branchesAbove tree node
branches tree node = branchesAbove tree node

correspondingIf :: VariationTree WithElif f ->
                   VTNode WithElif f ->
                   VTNode WithElif f
correspondingIf _ fi@(VTNode _ (Mapping _)) = fi
correspondingIf tree node = correspondingIf tree . fromJust $ parent tree node
