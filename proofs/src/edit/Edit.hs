module Edit where

import Names

type EditPC f = Time -> CodeFragment -> f

data Edit f = Edit {
    editedCodeFragments :: [CodeFragment], -- S in the paper
    editTypes :: CodeFragment -> DiffType,
    pcInEdit :: EditPC f   -- p_b and p_a in the paper
}
