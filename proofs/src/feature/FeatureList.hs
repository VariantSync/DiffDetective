module FeatureList where

import Data.List.Ordered

import Feature

newtype FeatureList = Features [FeatureName]

instance Semigroup FeatureList where
    Features a <> Features b = Features (a ++ b)

instance Monoid FeatureList where
    mempty = Features []

instance Feature FeatureList where
    (Features a) `implies` (Features b) = b `subset` a
