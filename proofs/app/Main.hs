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
    editedCodeFragments = ["foo", "bar", "baz"], -- S in the paper
    editTypes = \case
        "foo" -> ADD
        "bar" -> REM
        "baz" -> NON
        otherwise -> error (otherwise++" is not part of this edit"),
    pc = \time code -> case code of
        "foo" -> featureA
        "bar" -> featureB
        "baz" -> featureD
        otherwise -> error (otherwise++" is not part of this edit")
}

testCase :: FeatureAnnotation f => Edit f -> IO ()
testCase edit = do
    putStrLn " === Given edit e ==="
    print edit
    let vdt = complete edit
    putStrLn " === Converted to VDT via (complete e) === "
    print vdt
    putStrLn "with nodes:"
    putStrLn $ printNodes vdt
    putStrLn " === Converted back via (sound (complete e)) === "
    let edit' = sound vdt
    print edit'

main :: IO ()
main = do
    testCase edit1
