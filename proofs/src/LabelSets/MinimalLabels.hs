module LabelSets.MinimalLabels where

import VariationTree
import Feature.Logic ( Comparable(lequivalent) )
import Data.Maybe ( fromJust )

data MinimalLabels f = Artifact ArtifactReference | Mapping f

featureMappingOfParent :: VTLabel t => VariationTree t f -> VTNode t f -> Maybe f
featureMappingOfParent tree node = featuremapping tree <$> parent tree node

instance VTLabel MinimalLabels where
    makeArtifactLabel a = Artifact a
    makeMappingLabel f = Mapping f

    featuremapping tree node@(VTNode _ label) = case label of
        Artifact _ -> fromJust $ featureMappingOfParent tree node
        Mapping f -> f

instance Comparable f => Eq (MinimalLabels f) where
    x == y = case (x, y) of
        (Artifact a, Artifact b) -> a == b
        (Mapping a, Mapping b) -> lequivalent a b
        (_, _) -> False