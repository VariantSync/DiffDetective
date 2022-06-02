module VariationTreeExamples where

import Time
import VariationTree
import VariationDiff
import Labels.PaperLabels

import Feature.Logic
import Feature.Propositions
import ExampleFeatures
import MainUtils

import Control.Monad.State

genUUID :: State UUID UUID
genUUID = do
    uuid <- get
    put (uuid+1)
    return uuid

makeUniqueArtifact :: ArtifactReference -> State UUID (DefaultVTNode f)
makeUniqueArtifact a = flip makeArtifact a <$> genUUID
-- makeUniqueArtifact a = get >>= \id -> put (id+1) >>= \whatever -> return (makeArtifact id a)
-- makeUniqueArtifact a = State {runState = \s -> (s+1, makeArtifact (s+1) a) }

makeUniqueMapping :: (Composable f) => f -> State UUID (DefaultVTNode f)
makeUniqueMapping f = flip makeMapping  f <$> genUUID

makeUniqueElse :: (Negatable f, Composable f) => State UUID (DefaultVTNode f)
makeUniqueElse = makeElse <$> genUUID

genVariationTree :: (HasNeutral f, Composable f) => [State UUID (DefaultVTNode f)] -> [(Int, Int)] -> State UUID (DefaultVariationTree f)
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

assertion :: String -> String -> String -> Bool -> IO()
assertion message successMessage errorMessage val = printBlockIO message $
    if val then
        putStrLn $ feedback successMessage
    else do
        putStrLn $ feedback errorMessage
        error errorMessage
    where feedback = (<>) " ===> "

assertTrue :: String -> Bool -> IO ()
assertTrue message = assertion message "True" "False"

assertEquals :: Eq a => String -> a -> a -> IO ()
assertEquals message a b = assertion message "Elements equal! Great Success!" "ERROR!" (a == b)

showVariationTreeExamples :: IO ()
showVariationTreeExamples =
    let
        (kanto, next) = runState kantoFactory 1
        (johto,    _) = runState johtoFactory next

        diff = naiveDiff kanto johto

        projectedKanto = project BEFORE diff
        projectedJohto = project AFTER  diff
        in
            do
                printBlock "Kanto Starters" kanto
                printBlock "Johto Starters" johto
                printBlock "Naive Diff" diff
                printBlock "Projected Kanto" projectedKanto
                printBlock "Projected Johto" projectedJohto
                assertEquals "Assert(Kanto == Projected Kanto)" kanto projectedKanto
                assertEquals "Assert(Johto == Projected Johto)" johto projectedJohto
