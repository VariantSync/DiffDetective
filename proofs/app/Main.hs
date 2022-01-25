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

fFire = Features ["Fire"]
fGrass = Features ["Grass"]
fWater = Features ["Water"]

edit1 :: Edit FeatureList
edit1 = fromList
    [
        ("Bulbasaur",  ADD, featureUndef,         featureA),
        ("Charmander", REM, featureB <> featureC, featureUndef),
        ("Squirtle",   NON, featureD,             featureD <> featureA)
    ]

edit2 :: Edit FeatureList
edit2 = fromList
    [
        ("Treecko",   ADD, featureUndef,                     featureA),
        ("Torchic",   REM, featureA,                         featureUndef),
        ("Mudkip",    NON, featureA,                         featureA),
        ("Shroomish", NON, featureD <> featureC <> featureB, featureA)
    ]

vdt1 :: VDT FeatureList
vdt1 = fromNodesAndEdges
    [
        createRoot, -- 0
        createCodeNode "Chikorita" ADD, -- 1
        createCodeNode "Cyndaquil" REM, -- 2
        createCodeNode "Totodile"  NON, -- 3
        createMappingNode fGrass NON, -- 4
        createMappingNode fFire  REM, -- 5
        createMappingNode fWater ADD -- 6
    ]
    [
        (1, 4, AFTER),
        (2, 5, BEFORE),
        (3, 5, BEFORE),
        (3, 6, AFTER),
        (4, 0, BEFORE),
        (4, 0, AFTER),
        (5, 0, BEFORE),
        (6, 0, AFTER)
    ]

testCompleteness :: FeatureAnnotation f => Edit f -> IO ()
testCompleteness edit = do
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
    if edit' `Edit.isomorph` edit then
        putStrLn " ===> Edit remained intact! Great Success!"
    else do
        let errormsg = "ERROR! Edit got changed! Proved sound . complete != id!"
        putStrLn errormsg
        debugEqualityClauses edit edit'
        error errormsg

testSoundness :: FeatureAnnotation f => VDT f -> IO ()
testSoundness vdt = do
    putStrLn " === Given VDT v ==="
    print vdt
    let edit = sound vdt
    putStrLn " === Converted to Edt via (sound v) === "
    print edit
    putStrLn " === Converted back via (complete (sound v)) === "
    let vdt' = complete edit
    print vdt'
    if vdt `DiffTree.isomorph` vdt' then
        putStrLn " ===> VDT remained intact! Great Success!"
    else do
        error "ERROR! VDT got changed! Proved complete . sound != id!"

headlineMarker :: String
headlineMarker = ">>>>>>>"

headline :: String -> IO()
headline s = putStrLn $ headlineMarker ++ s ++ headlineMarker

linebreak :: IO ()
linebreak = putStrLn ""

main :: IO ()
main = do
    -- featureTestCase1
    headline "TESTING COMPLETENESS BEGIN"
    testCompleteness edit1
    linebreak
    testCompleteness edit2
    headline "TESTING COMPLETENESS END"
    linebreak
    headline "TESTING SOUNDNESS BEGIN"
    testSoundness vdt1
    headline "TESTING SOUNDNESS END"


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