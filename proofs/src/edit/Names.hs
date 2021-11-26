module Names where

type CodeFragment = String
data Time = BEFORE | AFTER
data DiffType = ADD | REM | NON deriving Eq
