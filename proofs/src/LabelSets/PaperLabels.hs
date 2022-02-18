{-# LANGUAGE GADTs #-}
{-# LANGUAGE StandaloneDeriving #-}

module LabelSets.PaperLabels where
    
import VariationTree
import Feature.Logic
import Data.Maybe ( fromJust )

data PaperLabels f where
    Artifact :: ArtifactReference -> PaperLabels f
    Mapping :: f -> PaperLabels f
    Else :: (Negatable f) => PaperLabels f

deriving instance Show f => Show (PaperLabels f)

instance VTLabel PaperLabels where
    makeArtifactLabel a = Artifact a
    makeMappingLabel f = Mapping f

    featuremapping tree node@(VTNode _ label) = case label of
        Artifact _ -> featureMappingOfParent
        Mapping f -> f
        Else -> lnot featureMappingOfParent
        where featureMappingOfParent = featuremapping tree $ fromJust $ parent tree node

instance Comparable f => Eq (PaperLabels f) where
    x == y = case (x, y) of
        (Artifact a, Artifact b) -> a == b
        (Mapping a, Mapping b) -> lequivalent a b
        (Else, Else) -> True
        (_, _) -> False

makeElse :: Negatable f => UUID -> VTNode PaperLabels f
makeElse i = VTNode i Else

type DefaultVariationTree f = VariationTree PaperLabels f
type DefaultVTNode f = VTNode PaperLabels f
