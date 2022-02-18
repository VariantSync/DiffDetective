module VariationTreeExamples where

import Time
import VariationTree
import VariationDiff
import LabelSets.PaperLabels

import Feature.Logic
import Feature.Propositions
import ExampleFeatures
import MainUtils

import Control.Monad.State

genUUID :: State UUID UUID
genUUID = do
    id <- get
    put (id+1)
    return id

makeUniqueArtifact :: ArtifactReference -> State UUID (DefaultVTNode f)
makeUniqueArtifact a = flip makeArtifact a <$> genUUID
-- makeUniqueArtifact a = get >>= \id -> put (id+1) >>= \whatever -> return (makeArtifact id a)
-- makeUniqueArtifact a = State {runState = \s -> (s+1, makeArtifact (s+1) a) }

makeUniqueMapping :: f -> State UUID (DefaultVTNode f)
makeUniqueMapping f = flip makeMapping  f <$> genUUID

makeUniqueElse :: Negatable f => State UUID (DefaultVTNode f)
makeUniqueElse = makeElse <$> genUUID

genVariationTree :: (HasNeutral f) => [State UUID (DefaultVTNode f)] -> [(Int, Int)] -> State UUID (DefaultVariationTree f)
genVariationTree stateNodes edges = sequence stateNodes >>= \nodes -> return $ fromNodesAndEdgeIndices nodes edges

starters :: String -> String -> String -> State UUID (DefaultVariationTree (PropositionalFormula String))
starters grass fire water = genVariationTree
    [
        makeUniqueArtifact grass,
        makeUniqueArtifact fire,
        makeUniqueArtifact water,
        makeUniqueMapping fGrass,
        makeUniqueMapping fFire,
        makeUniqueMapping fWater
    ]
    [
        (1, 4),
        (2, 5),
        (3, 6),
        (4, 0),
        (5, 0),
        (6, 0)
    ]

kantoFactory = starters "Bulbasaur" "Charmander" "Squirtle"
johtoFactory = starters "Chikorita" "Cyndaquil"  "Totodile"

printBlockIO :: String -> IO () -> IO ()
printBlockIO title inner = do
    headline title
    inner
    linebreak

printBlock :: Show a => String -> a -> IO ()
printBlock title content = printBlockIO title (print content)

assertEquals :: Eq a => String -> a -> a -> IO ()
assertEquals message a b = printBlockIO message $
    if a == b then
        putStrLn " ===> Elements equal! Great Success!"
    else do
        let errormsg = "ERROR!"
        putStrLn errormsg
        error errormsg

showVariationTreeExamples :: IO ()
showVariationTreeExamples =
    let
        (kanto, next) = runState kantoFactory 1
        (johto,    _) = runState johtoFactory next

        diff = stupidDiff kanto johto

        projectedKanto = project BEFORE diff
        projectedJohto = project AFTER  diff
        in
            do
                printBlock "Kanto Starters" kanto
                printBlock "Johto Starters" johto
                printBlock "Stupid Diff" diff
                printBlock "Projected Kanto" projectedKanto
                printBlock "Projected Johto" projectedJohto
                assertEquals "Assert(Kanto == Projected Kanto)" kanto projectedKanto
                assertEquals "Assert(Johto == Projected Johto)" johto projectedJohto
