module Time where

data Time = BEFORE | AFTER deriving (Eq, Show)
data DiffType = ADD | REM | NON deriving (Eq, Show)

always :: [Time]
always = [BEFORE, AFTER]

abbreviate :: Time -> String 
abbreviate BEFORE = "B"
abbreviate AFTER = "A"

timesOfExistence :: DiffType -> [Time]
timesOfExistence ADD = [AFTER]
timesOfExistence REM = [BEFORE]
timesOfExistence NON = always

existsAtTime :: Time -> DiffType -> Bool
existsAtTime BEFORE ADD = False
existsAtTime AFTER REM = False
existsAtTime _ _ = True

existsBefore :: DiffType -> Bool 
existsBefore = existsAtTime BEFORE

existsAfter :: DiffType -> Bool 
existsAfter = existsAtTime AFTER
