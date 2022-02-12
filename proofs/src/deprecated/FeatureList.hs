module FeatureList where

import Feature ( FeatureAnnotation(implies), Feature )

import Util

newtype FeatureList = Features [Feature] deriving (Eq)

instance Show FeatureList where
    show (Features list) = show list

instance Semigroup FeatureList where
    Features a <> Features b = Features (a <> b)

instance Monoid FeatureList where
    mempty = Features []

instance FeatureAnnotation FeatureList where
    (Features a) `implies` (Features b) = b `isSubset` a
    -- eval assignment (Features f) = and (fmap assignment f)

