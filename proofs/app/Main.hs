{-# LANGUAGE LambdaCase #-}
module Main where

import Data.List
import qualified Data.Map.Strict as Map

import Definitions
import Edit
import Feature
import FeatureList
import Conversion
import DiffTree

featureUndef = Features ["unknown"]
featureA = Features ["A"]
featureB = Features ["B"]
featureC = Features ["C"]
featureD = Features ["D"]

edit1 :: Edit FeatureList
edit1 = fromList [
    ("Bulbasaur", ADD, featureUndef, featureA),
    ("Charmander", REM, featureB <> featureC, featureUndef),
    ("Squirtle", NON, featureD, featureD <> featureA)
    ]

edit2 :: Edit FeatureList
edit2 = fromList [
    ("Treecko", ADD, featureUndef, featureA),
    ("Torchic", REM, featureA, featureUndef),
    ("Mudkip", NON, featureA, featureA),
    ("Shroomish", NON, featureD <> featureC <> featureB, featureA)
    ]

testCase :: FeatureAnnotation f => Edit f -> IO ()
testCase edit = do
    putStrLn " === Given edit e ==="
    print edit
    let vdt = complete edit
    putStrLn " === Converted to VDT via (complete e) === "
    print vdt
    -- putStrLn "with nodes:"
    -- putStrLn $ printNodes vdt
    putStrLn " === Converted back via (sound (complete e)) === "
    let edit' = sound vdt
    print edit'
    if edit' == edit then
        putStrLn " ===> Edit remained intact! Great Success!"
    else do
        putStrLn "ERROR! Edit got changed! Proved sound . complete != id!"
        debugEqualityClauses edit edit'


-- featureTest :: FeatureAnnotation f => String -> String -> f -> f -> IO()
-- featureTest nameOfA nameOfB a b = do
--     putStrLn ("  " ++ nameOfA ++ " == " ++ nameOfB)
--     putStrLn ("= " ++ show a ++ " == " ++ show b)
--     putStrLn ("= " ++ show (equivalent a b))

-- featureTestCase1 :: IO()
-- featureTestCase1 = do
--     featureTest "A" "A" featureA featureA
--     featureTest "A" "B" featureA featureB
--     featureTest "(A<>B)" "(A<>B)" (featureA <> featureB) (featureA <> featureB)
--     featureTest "(A<>B)" "(B<>A)" (featureA <> featureB) (featureB <> featureA)
--     featureTest "(A <> C)" "B" (featureA <> featureC) featureB

main :: IO ()
main = do
    -- featureTestCase1
    putStrLn ""
    testCase edit1
    putStrLn ""
    testCase edit2
