module DiffTree where

import Data.List ( find, intercalate )

import Definitions
import Feature
import Util

----- VDT NODES

-- The label of diff tree nodes. Because we have sum types in haskell, we can directly encode the code type, too.
data Label f = Code CodeFragment | Mapping f deriving Show

data VDTNode f = Node {
    label :: Label f,
    diffType :: DiffType
}

instance Show f => Show (VDTNode f) where
    show v = mconcat ["(", show (diffType v), ", ", show (label v), ")"]

instance FeatureAnnotation f => Eq (Label f) where
    x == y = case (x, y) of
        (Code a, Code b) -> a == b
        (Mapping a, Mapping b) -> equivalent a b
        (_, _) -> False

instance FeatureAnnotation f => Eq (VDTNode f) where
    x == y = label x == label y && diffType x == diffType y

createRoot :: FeatureAnnotation f => VDTNode f
createRoot = Node {
    label = Mapping mempty,
    diffType = NON
}

createCodeNode :: CodeFragment -> DiffType -> VDTNode f
createCodeNode c d = Node {
    label = Code c,
    diffType = d
}

createMappingNode :: f -> DiffType -> VDTNode f
createMappingNode m d = Node {
    label = Mapping m,
    diffType = d
}

{- called F in the paper -}
featureOf :: (FeatureAnnotation f) => VDTNode f -> f
featureOf v = case label v of
    Code code -> mempty
    Mapping m  -> m

isCode :: VDTNode f -> Bool
isCode v = case label v of
    Code s -> True
    Mapping m -> False

hasCode :: CodeFragment -> VDTNode f -> Bool
hasCode code node = case label node of
    Code c -> c == code
    Mapping _ -> False

codeOf :: VDTNode f -> Maybe CodeFragment
codeOf v = case label v of
    Code c -> Just c
    Mapping _ -> Nothing

----- VDTS

data VDTEdge f = VDTEdge {
    child :: VDTNode f,
    parent :: VDTNode f,
    time :: Time
}

data VDT f = VDT {
    nodes :: [VDTNode f],
    edges :: [VDTEdge f]
}

fromIndices :: [VDTNode f] -> (Int, Int, Time) -> VDTEdge f
fromIndices nodes (from, to, time) = VDTEdge {
    child = nodes !! from,
    parent = nodes !! to,
    time = time
}

fromNodesAndEdges :: [VDTNode f] -> [(Int, Int, Time)] -> VDT f
fromNodesAndEdges nodes edges = VDT {
    nodes = nodes,
    edges = fmap (fromIndices nodes) edges
}

instance Show f => Show (VDTEdge f) where
    show edge = mconcat [show (child edge), " -", abbreviate (time edge), "-> ", show (parent edge)]

instance Show f => Show (VDT f) where
    show vdt = "VDT with edges {" ++ intercalate "\n  " ("":(show <$> edges vdt)) ++ "\n}"

parentEdge :: (FeatureAnnotation f) => VDT f -> VDTNode f -> Time -> Maybe (VDTEdge f)
parentEdge tree node t = find (\edge -> child edge == node && time edge == t) (edges tree)

parentOf :: (FeatureAnnotation f) => VDT f -> VDTNode f -> Time -> Maybe (VDTNode f)
parentOf tree node time = parent <$> parentEdge tree node time

pathToRoot :: (FeatureAnnotation f) => Time -> VDT f -> VDTNode f -> [VDTNode f]
pathToRoot time vdt node = node:(case parentOf vdt node time of
    Just p -> pathToRoot time vdt p
    Nothing -> []
    )

codenodes :: VDT f -> [VDTNode f]
codenodes v = filter isCode (nodes v)

{- called PC in the paper -}
pcInVDT :: (FeatureAnnotation f) => Time -> VDT f -> VDTNode f -> f
pcInVDT time vdt node = mconcat (featureOf <$> pathToRoot time vdt node)

findNodeWithCode :: CodeFragment -> VDT f -> VDTNode f
findNodeWithCode code vdt = case find (hasCode code) (nodes vdt) of
    Nothing -> error "Cannot compute pc of code that was not part of the initial VDT."
    Just node -> node

printNodes :: Show f => VDT f -> String 
printNodes vdt = intercalate "\n" (show <$> nodes vdt)

isomorph :: (FeatureAnnotation f) => VDT f -> VDT f -> Bool
x `isomorph` y = 
    -- 1.) edited code fragments have to be equal
    codenodes x == codenodes y -- should be set equals
    -- 2.) presence conditions have to be equal
    && and ((\t -> propertiesEqualUnder equivalent (pcInVDT t x) (pcInVDT t y) (codenodes x)) <$> always)
