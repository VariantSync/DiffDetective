module Definitions where
import Data.List

type CodeFragment = String
data Time = BEFORE | AFTER deriving (Eq, Show)
data DiffType = ADD | REM | NON deriving (Eq, Show)

fromDiffType :: DiffType -> [Time]
fromDiffType ADD = [AFTER]
fromDiffType REM = [BEFORE]
fromDiffType NON = [BEFORE, AFTER]

existsAtTime :: Time -> DiffType -> Bool
existsAtTime BEFORE ADD = False
existsAtTime AFTER REM = False
existsAtTime _ _ = True