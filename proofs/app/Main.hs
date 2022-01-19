{-# LANGUAGE LambdaCase #-}
module Main where

import Data.List

import Definitions
import Edit
import Feature
import FeatureList
import Conversion
import DiffTree

featureA = Features ["A"]
featureB = Features ["B"]
featureC = Features ["C"]
featureD = Features ["D"]

edit1 = Edit {
    editedCodeFragments = ["Bulbasaur", "Charmander", "Squirtle"], -- S in the paper
    editTypes = \case
        "Bulbasaur" -> ADD
        "Charmander" -> REM
        "Squirtle" -> NON
        otherwise -> error (otherwise++" is not part of this edit"),
    pc = \time code -> case code of
        "Bulbasaur" -> featureA
        "Charmander" -> featureB <> featureC
        "Squirtle" -> featureD
        otherwise -> error (otherwise++" is not part of this edit")
}

edit2 = Edit {
    editedCodeFragments = ["Charmander", "Squirtle", "Treecko", "Torchic"], -- S in the paper
    editTypes = \case
        "Charmander" -> ADD
        "Squirtle" -> REM
        "Treecko" -> NON
        "Torchic" -> NON
        otherwise -> error (otherwise++" is not part of this edit"),
    pc = \time code -> case code of
        "Charmander" -> featureA
        "Squirtle" -> featureA
        "Treecko" -> featureA
        "Torchic" -> featureA
        otherwise -> error (otherwise++" is not part of this edit")
}

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
