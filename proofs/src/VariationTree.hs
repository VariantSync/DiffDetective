module VariationTree where

import Logic

import Data.List (find, intercalate)
import Data.Maybe (fromJust)

type UUID = Int
type ArtifactReference = String

data Label f =
      Artifact ArtifactReference
    | Mapping f
    | Else
    deriving Show

data VTNode f = VTNode UUID (Label f)

data VTEdge f = VTEdge {
    child  :: VTNode f,
    parent :: VTNode f
}

data VariationTree f = VariationTree {
    nodes :: [VTNode f],
    edges :: [VTEdge f]
}

makeRoot :: Logic f => UUID -> VTNode f
makeRoot id = VTNode id (Mapping ltrue)

makeArtifact :: UUID -> ArtifactReference -> VTNode f
makeArtifact id a = VTNode id (Artifact a)

makeMapping :: UUID -> f -> VTNode f
makeMapping id f = VTNode id (Mapping f)

makeElse :: UUID -> VTNode f
makeElse id = VTNode id Else

getID :: VTNode f -> UUID
getID (VTNode i l) = i

withId :: UUID -> [VTNode f] -> Maybe (VTNode f)
withId id = find ((id ==) . getID)

fromIds :: [VTNode f] -> (UUID, UUID) -> VTEdge f
fromIds nodes (from, to) = VTEdge {
    child  = fromJust (withId from nodes),
    parent = fromJust (withId to   nodes)
}

fromNodesAndEdges :: [VTNode f] -> [(UUID, UUID)] -> VariationTree f
fromNodesAndEdges nodes edges = VariationTree {
    nodes = nodes,
    edges = fmap (fromIds nodes) edges
}

-- stupidDiff :: VariationTree f -> VariationTree f -> VariationDiff f
-- stupidDiff old new = ...


instance Eq f => Eq (Label f) where
    x == y = case (x, y) of
        (Artifact a, Artifact b) -> a == b
        (Mapping a, Mapping b) -> a == b
        (Else, Else) -> True
        (_, _) -> False

instance Eq f => Eq (VTNode f) where
    (VTNode name label) == (VTNode name' label') =
        name == name' && label == label'

instance Show f => Show (VTNode f) where
    show (VTNode name label) = mconcat ["(", show name, ", ", show label, ")"]

instance Show f => Show (VTEdge f) where
    show edge = mconcat [show (child edge), " -> ", show (parent edge)]

instance Show f => Show (VariationTree f) where
    show t = "Variation Tree with edges {" ++ intercalate "\n  " ("":(show <$> edges t)) ++ "\n}"