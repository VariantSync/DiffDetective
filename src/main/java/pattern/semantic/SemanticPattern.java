package pattern.semantic;

import analysis.data.PatternMatch;
import diff.difftree.DiffNode;
import pattern.EditPattern;

import java.util.List;

public abstract class SemanticPattern extends EditPattern<DiffNode> {

    @Override
    public abstract List<PatternMatch> getMatches(DiffNode annotationNode);
}
