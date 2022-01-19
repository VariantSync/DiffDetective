module Edit where

import Definitions
import Feature
import Util

data Edit f = Edit {
    editedCodeFragments :: [CodeFragment], -- S in the paper
    editTypes :: CodeFragment -> DiffType,
    pc :: Time -> CodeFragment -> f   -- PC_b and PC_a in the paper
}

instance (FeatureAnnotation f) => Eq (Edit f) where
    x == y =
        let
            editedCodeX = editedCodeFragments x
            editedCodeY = editedCodeFragments y
            in
        -- 1.) edited code fragments have to be equal
        (editedCodeX == editedCodeY) -- set equals
        -- 2.) the type of edit to each code fragment should be equal
        -- If the edited code fragements are equall, we can just look at one of the lists (lets say editedCodeX) from now on.
        && propertiesEqual (editTypes x) (editTypes y) editedCodeX
        -- 3.) all presence condition should be equivalent
        && propertiesEqualUnder equivalent (pc x BEFORE) (pc y BEFORE) editedCodeX
        && propertiesEqualUnder equivalent (pc x AFTER)  (pc y AFTER)  editedCodeX
