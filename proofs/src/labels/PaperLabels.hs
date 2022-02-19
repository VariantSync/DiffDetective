{-# LANGUAGE GADTs #-}
{-# LANGUAGE StandaloneDeriving #-}

module PaperLabels where
    
import VariationTree
import Logic
import Data.Maybe ( fromJust )

data PaperLabels f where
    Artifact :: ArtifactReference -> PaperLabels f
    Mapping :: Composable f => f -> PaperLabels f
    Else :: (Composable f, Negatable f) => PaperLabels f

deriving instance Show f => Show (PaperLabels f)

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
