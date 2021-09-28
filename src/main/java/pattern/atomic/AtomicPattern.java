package pattern.atomic;

import analysis.data.PatternMatch;
import diff.difftree.DiffNode;
import pattern.EditPattern;

import java.util.List;

public abstract class AtomicPattern extends EditPattern<DiffNode> {

    @Override
    public abstract List<PatternMatch<DiffNode>> getMatches(DiffNode x);
}
