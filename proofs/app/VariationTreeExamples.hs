module VariationTreeExamples where

import VariationTree
import Propositions
import ExampleFeatures
import MainUtils

starters :: String -> String -> String -> VariationTree (PropositionalFormula String)
starters grass fire water = fromNodesAndEdges
    [
        makeRoot 0, 
        makeArtifact 1 grass,
        makeArtifact 4 fire,
        makeArtifact 7 water,
        makeMapping  11 fGrass,
        makeMapping  44 fFire,
        makeMapping  77 fWater
    ]
    [
        ( 1, 11),
        ( 4, 44),
        ( 7, 77),
        (11,  0),
        (44,  0),
        (77,  0)
    ]

kanto = starters "Bulbasaur" "Charmander" "Squirtle"
johto = starters "Chikorita" "Cyndaquil"  "Totodile"

showVariationTreeExamples :: IO ()
showVariationTreeExamples = do
    headline "Kanto Starters"
    print kanto
    linebreak
    headline "Johto Starters"
    print johto
