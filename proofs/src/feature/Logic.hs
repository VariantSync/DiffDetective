{- |
Description: Type class for reasoning on 'Logic's.
License: GNU LGPLv3
Maintainer: paul.bittner@uni-ulm.de
Type class for reasoning on 'Logic's.
-}
module Logic where

{- |
Type class to reason on logics.
We use this type class to reason on propositional logic and the ternary logic by Sobocinski we use for feature trace recording.
-}
class Logic l where
    -- | The atomic value representing /true/ in this logic.
    ltrue :: l
    -- | The atomic value representing /false/ in this logic.
    lfalse :: l
    -- | A list of all atomic values of this logic. Default implementation comprises 'ltrue' and 'lfalse'.
    lvalues :: [l]
    lvalues = [lfalse, ltrue]

    -- | Negation of a logical formula.
    lnot :: l -> l
    -- lnot q = limplies q lfalse
    -- | Conjunction of a list of logical formulas.
    land :: [l] -> l
    -- land = lnot.lor.map lnot
    -- | Disjunction of a list of logical formulas.
    lor :: [l] -> l
    -- lor p q = limplies (limplies p q) q
    lor = lnot.land.map lnot
    -- | Implication between two logical formulas.
    -- The first argument @p@ is one the left side of the implication and the second argument 'q' is on the right (i.e., @p => q@).
    limplies :: l -> l -> l
    limplies p q = lor [lnot p, q]
    -- | Equivalence between two logical formulas.
    lequals :: l -> l -> l
    lequals p q = land [limplies p q, limplies q p]

    {- |
    Evaluates a logical formula.
    Arguments are
    
    (1) a function assigning variables to values
    (2) a formula to evaluate
    This function should return an element of 'lvalues'.
    -}
    leval :: (l -> l) -> l -> l
