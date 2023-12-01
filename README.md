# Paper Replication Package

This is the replication package for the paper "Explaining Edits to Variability Annotations in Evolving Software Product Lines".

It contains the adapted DiffDetective implementation used for the evaluation in the paper.
The main change is the edge-adding in the `PaperEvaluationTask`, where variation diffs get extended to edge-typed variation diffs with *Implication* and *Alternative* edges.

To replicate the evaluation, run the `main` method in `relationshipedges/Validation`
