module Edit where

import Data.List

import Definitions
import Feature
import Util

data Edit f = Edit {
    editedCodeFragments :: [CodeFragment], -- S in the paper
    editTypes :: CodeFragment -> DiffType,
    pc :: Time -> CodeFragment -> f   -- PC_b and PC_a in the paper
}

pcEqualsFor :: FeatureAnnotation f => DiffType -> Edit f -> Edit f -> CodeFragment -> Bool
pcEqualsFor diffType x y s = and $ fmap
    (\t ->
        not (existsAtTime t diffType) ||
        equivalent (pc x t s) (pc y t s)
    )
    [BEFORE, AFTER]

instance Show f => Show (Edit f) where
    show e =
        "Edit {\n"
        ++ intercalate "\n" (fmap (\s ->
            let d = editTypes e s in
            unwords [
                "  ",
                show d,
                show s,
                "with PC_b =",
                if existsBefore d then show (pc e BEFORE s) else "undef",
                "and PC_a =",
                if existsAfter d then show (pc e AFTER s) else "undef"
            ]) (editedCodeFragments e))
        ++ "\n}"

instance (FeatureAnnotation f) => Eq (Edit f) where
    x == y =
        let
            editedCodeX = editedCodeFragments x
            editedCodeY = editedCodeFragments y
            in
        -- 1.) edited code fragments have to be equal
        (editedCodeX == editedCodeY) -- set equals
        -- 2.) the type of edit to each code fragment should be equal
        -- If the edited code fragements are equall, we can just look at one of the lists (lets say editedCodeX) from now on.
        && propertiesEqual (editTypes x) (editTypes y) editedCodeX
        -- 3.) all presence condition should be equivalent
        && and (fmap (\s -> pcEqualsFor (editTypes x s) x y s) editedCodeX)

debugEqualityClauses :: (FeatureAnnotation f) => Edit f -> Edit f -> IO ()
debugEqualityClauses x y = let
    editedCodeX = editedCodeFragments x
    editedCodeY = editedCodeFragments y in
        do
            putStrLn ("                                    (editedCodeX == editedCodeY) === " ++ show (editedCodeX == editedCodeY))
            putStrLn ("         propertiesEqual (editTypes x) (editTypes y) editedCodeX === " ++ show (propertiesEqual (editTypes x) (editTypes y) editedCodeX))
            putStrLn ("and (fmap (\\s -> pcEqualsFor (editTypes x s) x y s) editedCodeX) === " ++ show (and (fmap (\s -> pcEqualsFor (editTypes x s) x y s) editedCodeX)))