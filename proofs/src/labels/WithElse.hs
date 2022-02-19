{-# LANGUAGE GADTs #-}

module WithElse where

import VariationTree
import Data.Maybe (fromJust)
import Logic

data WithElif f where
    WEArtifact :: ArtifactReference -> WithElif f
    WEMapping :: Composable f => f -> WithElif f
    WEElse :: (Composable f, Negatable f) => WithElif f
    WEElif :: (Composable f, Negatable f) => f -> WithElif f

instance VTLabel WithElif where
    makeArtifactLabel a = WEArtifact a
    makeMappingLabel f = WEMapping f
    featuremapping tree node@(VTNode _ label) = case label of
        -- TODO: Can we directly say that we just invoke the functions of PaperLabels for WEArtifact and WEMapping?
        WEArtifact _ -> fromJust $ featureMappingOfParent tree node
        WEMapping f -> f
        WEElse ->            notTheOtherBranches tree node
        WEElif f -> land [f, notTheOtherBranches tree node]
    presencecondition tree node@(VTNode _ label) = case label of
        -- TODO: Can we directly say that we just invoke the functions of PaperLabels for WEArtifact and WEMapping?
        WEArtifact _ -> parentPC
        WEMapping f -> land [f, parentPC]
        WEElse ->   land [featuremapping tree node, presencecondition tree (getParent (correspondingIf tree node))]
        WEElif _ -> land [featuremapping tree node, presencecondition tree (getParent (correspondingIf tree node))] 
        where
            parentPC = fromJust $ presenceConditionOfParent tree node
            getParent = fromJust . parent tree

notTheOtherBranches :: (Composable f, Negatable f) => VariationTree WithElif f -> VTNode WithElif f -> f
notTheOtherBranches tree node = land $ lnot <$> branchesAbove tree node

branchesAbove :: VariationTree WithElif f -> VTNode WithElif f -> [f]
branchesAbove tree node = branches tree (fromJust (parent tree node))

branches :: VariationTree WithElif f -> VTNode WithElif f -> [f]
branches _ (VTNode _ (WEMapping f)) = [f]
branches tree node@(VTNode _ (WEElif f)) = f:branches tree (fromJust (parent tree node))
branches tree node = branches tree (fromJust (parent tree node))

correspondingIf :: VariationTree WithElif f -> VTNode WithElif f -> VTNode WithElif f
correspondingIf _ fi@(VTNode _ (WEMapping _)) = fi
correspondingIf tree node = correspondingIf tree . fromJust $ parent tree node
