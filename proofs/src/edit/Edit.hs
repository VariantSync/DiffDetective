module Edit where

import Names
import Feature

type EditPC f = Time -> CodeFragment -> f

data Edit f = Edit {
    editedCodeFragments :: [CodeFragment], -- S in the paper
    editTypes :: CodeFragment -> DiffType,
    pcInEdit :: EditPC f   -- p_b and p_a in the paper
}

instance (FeatureAnnotation f) => Eq (Edit f) where
    a == b =
        let
            editedCodeA = editedCodeFragments a
            editedCodeB = editedCodeFragments b
            in
        -- edited code fragments have to be equal
        (editedCodeA == editedCodeB)
        -- the type of edit to each code fragment should be equal
        && (fmap (editTypes a) editedCodeA == fmap (editTypes b) editedCodeB)
        -- all presence condition should be equivalent
        -- TODO: Beautify the following
        && and (fmap (\code -> equivalent (pcInEdit a BEFORE code) (pcInEdit b BEFORE code)) editedCodeA)
        && and (fmap (\code -> equivalent (pcInEdit a AFTER code) (pcInEdit b AFTER code)) editedCodeA)
