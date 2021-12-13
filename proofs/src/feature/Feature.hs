module Feature where

type Feature = String
class (Monoid f) => FeatureAnnotation f where
    implies :: f -> f -> Bool

    equivalent :: f -> f -> Bool
    equivalent a b = (a `implies` b) && (b `implies` a)

    {-
    Do we actually need eval?
    I mean, it makes total sense to have it from a practical point of view,
    but are we going to use it in our proofs?
    -}
    eval :: (Feature -> Bool) -> f -> Bool
