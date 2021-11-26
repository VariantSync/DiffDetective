package main;

import diff.serialize.DiffTreeSerializeDebugData;

public record DiffTreeMiningResult(String lineGraph, int numTrees, DiffTreeSerializeDebugData debugData) {
}
