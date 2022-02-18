{-# LANGUAGE GADTs #-}

module PaperTypes where
    
import VariationTree
import Logic
import Data.Maybe ( fromJust )

data PaperTypes f where
    Artifact :: ArtifactReference -> PaperTypes f
    Mapping :: f -> PaperTypes f
    Else :: (Negatable f) => PaperTypes f

instance NodeTypes PaperTypes where
    makeArtifactLabel a = Artifact a
    makeMappingLabel f = Mapping f

    featuremapping tree node@(VTNode _ label) = case label of
        Artifact _ -> featureMappingOfParent
        Mapping f -> f
        Else -> lnot featureMappingOfParent
        where featureMappingOfParent = featuremapping tree $ fromJust $ parent tree node

instance Comparable f => Eq (PaperTypes f) where
    x == y = case (x, y) of
        (Artifact a, Artifact b) -> a == b
        (Mapping a, Mapping b) -> lequivalent a b
        (Else, Else) -> True
        (_, _) -> False

makeElse :: Negatable f => UUID -> VTNode PaperTypes f
makeElse i = VTNode i Else

type DefaultVariationTree f = VariationTree PaperTypes f
type DefaultVTNode f = VTNode PaperTypes f
