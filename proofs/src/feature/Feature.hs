module Feature where

class (Monoid f) => Feature f where
    implies :: f -> f -> Bool

type FeatureName = String
