﻿{- |
Description: A collection of utility functions.
License: GNU LGPLv3
Maintainer: paul.bittner@uni-ulm.de
A collection of utility functions.
-}
module Util where

-- | Returns the head of the given list as Just or Nothing, iff the list is empty.
safeHead :: [a] -> Maybe a
safeHead [] = Nothing
safeHead (x:_) = Just x

-- | Generates a string of /i/ spaces where /i/ is the given indent.
genIndent :: Int -> String
genIndent i = concat $ replicate i " "

prependMaybe :: Maybe a -> [a] -> [a]
prependMaybe m xs = case m of
    Just x -> x:xs
    Nothing -> xs

propertiesEqualUnder :: (b -> b -> Bool) -> (a -> b) -> (a -> b) -> [a] -> Bool 
propertiesEqualUnder eq first second xs = and (fmap (\s -> eq (first s) (second s)) xs)

propertiesEqual :: Eq b => (a -> b) -> (a -> b) -> [a] -> Bool
propertiesEqual = propertiesEqualUnder (==)

-- foreach :: [a] -> (a -> [b]) -> [b]
-- foreach = (>>=)
