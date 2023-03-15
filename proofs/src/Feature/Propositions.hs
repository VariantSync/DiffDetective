{- |
Description: Definition and operations of propositional logic.
License: GNU LGPLv3
Maintainer: paul.bittner@uni-ulm.de
Definition and operations of propositional logic.
-}
module Feature.Propositions where

import Feature.Logic
import Data.List ( intercalate )

-- | Sum type similar to a grammar for building propositional formulas.
data PropositionalFormula a =
      PTrue
    | PFalse
    | PVariable a
    | PNot (PropositionalFormula a)
    | PAnd [PropositionalFormula a]
    | POr [PropositionalFormula a]
    deriving (Eq)


instance Negatable (PropositionalFormula a) where
    -- | Negation of a logical formula.
    lnot PTrue = PFalse
    lnot PFalse = PTrue
    lnot p = PNot p

instance HasNeutral (PropositionalFormula a) where
    ltrue = PTrue

instance Composable (PropositionalFormula a) where
    land [] = PTrue
    land l = PAnd l

instance Eq a => Comparable (PropositionalFormula a) where
    -- We only check for syntactic equality for now but this should be TAUT(a => b) in fact.
    limplies a b = a == b

instance Eq a => Logic (PropositionalFormula a) where

instance Functor PropositionalFormula where
    fmap _ PTrue = PTrue
    fmap _ PFalse = PFalse
    fmap f (PVariable a) = PVariable (f a)
    fmap f (PNot p) = PNot (fmap f p)
    fmap f (PAnd c) = PAnd (fmap (fmap f) c)
    fmap f (POr c) = POr (fmap (fmap f) c)

-- | An assignment for propositional formulas, assigns variables to boolean values.
type Assignment a = a -> Bool -- Maybe this should better be implemented as a map as an Assignment always is a partial function.

-- | Evaluates a propositional formula with the given variable assignment.
eval :: Assignment a -> PropositionalFormula a -> Bool
eval _ PTrue = True
eval _ PFalse = False
eval config (PVariable x) = config x
eval config (PNot x) = not $ eval config x
eval config (PAnd cs) = and $ fmap (eval config) cs
eval config (POr cs) = or $ fmap (eval config) cs

-- | Converts boolean values to their respective symbols in our definition of propositional logic.
liftBool :: Bool -> PropositionalFormula a
liftBool True = PTrue
liftBool False = PFalse

-- | Returns /true/ iff the given propositional formula is the value 'PTrue'.
isPTrue :: PropositionalFormula a -> Bool
isPTrue PTrue = True
isPTrue _ = False

-- | Returns /true/ iff the given propositional formula is the value 'PFalse'.
isPFalse :: PropositionalFormula a -> Bool
isPFalse PFalse = True
isPFalse _ = False

-- | Returns /true/ iff the given propositional formula is a literal.
isLiteral :: PropositionalFormula a -> Bool
isLiteral PTrue = True
isLiteral PFalse = True
isLiteral (PVariable _) = True
isLiteral (PNot f) = isLiteral f
isLiteral _ = False

-- | UTF-8 characters for printing propositional operators.
-- instance Show a => Show (PropositionalFormula a) where
--     show PTrue = "⊤"
--     show PFalse = "⊥"
--     show (PVariable v) = show v
--     show (PNot p) = "¬"++show p
--     show (PAnd cs) = "("++(intercalate " ∧ " $ map show cs)++")"
--     show (POr cs) = "("++(intercalate " ∨ " $ map show cs)++")"

-- | ASCII characters for printing propositional operators.
instance Show a => Show (PropositionalFormula a) where
    show PTrue = "true"
    show PFalse = "false"
    show (PVariable v) = show v
    show (PNot p) = "!"++show p
    show (PAnd cs) = "("++(intercalate " & " $ map show cs)++")"
    show (POr cs) = "("++(intercalate " | " $ map show cs)++")"

-- -- | This visualisation is for debugging as it shows the exact expression tree.
-- instance Show a => Show (PropositionalFormula a) where
--     show PTrue = "(PTrue)"
--     show PFalse = "(PFalse)"
--     show (PVariable v) = "(PVariable "++show v++")"
--     show (PNot p) = "(PNot "++show p++")"
--     show (PAnd cs) = "(PAnd ["++(intercalate ", " $ map show cs)++"])"
--     show (POr cs) = "(POr ["++(intercalate ", " $ map show cs)++"])"
