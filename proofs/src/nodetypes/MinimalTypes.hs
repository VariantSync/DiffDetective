module MinimalTypes where

import VariationTree
import Logic
import Data.Maybe ( fromJust )

data MinimalTypes f = Artifact ArtifactReference | Mapping f

featureMappingOfParent :: NodeTypes t => VariationTree t f -> VTNode t f -> Maybe f
featureMappingOfParent tree node = featuremapping tree <$> parent tree node

instance NodeTypes MinimalTypes where
    makeArtifactLabel a = Artifact a
    makeMappingLabel f = Mapping f

    featuremapping tree node@(VTNode _ label) = case label of
        Artifact _ -> fromJust $ featureMappingOfParent tree node
        Mapping f -> f

instance Comparable f => Eq (MinimalTypes f) where
    x == y = case (x, y) of
        (Artifact a, Artifact b) -> a == b
        (Mapping a, Mapping b) -> lequivalent a b
        (_, _) -> False