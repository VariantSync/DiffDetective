{- |
Description: Type class for reasoning on 'Logic's.
License: GNU LGPLv3
Maintainer: paul.bittner@uni-ulm.de
Type class for reasoning on 'Logic's.
-}
module Feature.Logic where

class Negatable f where
    -- | Negation of a logical formula.
    lnot :: f -> f

class HasNeutral f where
    ltrue :: f

class Composable f where
    land :: [f] -> f

class Comparable f where
    limplies :: f -> f -> Bool
    
    lequivalent :: f -> f -> Bool
    lequivalent a b = a `limplies` b && b `limplies` a

{- |
Type class to reason on logics.
-}
class (Negatable l, HasNeutral l, Composable l, Comparable l) => Logic l where
    lfalse :: l
    lfalse = lnot ltrue

    lor :: [l] -> l
    lor = lnot . land . map lnot
