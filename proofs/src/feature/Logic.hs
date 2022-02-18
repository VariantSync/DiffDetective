{- |
Description: Type class for reasoning on 'Logic's.
License: GNU LGPLv3
Maintainer: paul.bittner@uni-ulm.de
Type class for reasoning on 'Logic's.
-}
module Logic where

class Negatable n where
    -- | Negation of a logical formula.
    lnot :: n -> n

class HasNeutral n where
    ltrue :: n

class Composable n where
    land :: [n] -> n

class Comparable n where
    limplies :: n -> n -> Bool
    
    lequivalent :: n -> n -> Bool
    lequivalent a b = a `limplies` b && b `limplies` a

{- |
Type class to reason on logics.
-}
class (Negatable l, HasNeutral l, Composable l, Comparable l) => Logic l where
    lfalse :: l
    lfalse = lnot ltrue

    lor :: [l] -> l
    lor = lnot . land . map lnot

--- Interoperability

-- instance Composable l => Semigroup l where
--     a <> b = land [a, b]
