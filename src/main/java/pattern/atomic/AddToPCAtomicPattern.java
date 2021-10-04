package pattern.atomic;

import analysis.data.PatternMatch;
import diff.Lines;
import diff.difftree.DiffNode;
import evaluation.FeatureContext;
import org.prop4j.Node;

import java.util.Optional;

public class AddToPCAtomicPattern extends AtomicPattern{
    public static final String PATTERN_NAME = "AddToPC";

    public AddToPCAtomicPattern() {
        this.name = PATTERN_NAME;
    }

    @Override
    public Optional<PatternMatch<DiffNode>> match(DiffNode codeNode) {
        if(codeNode.isAdd() && !codeNode.getAfterParent().isAdd()){
            final Node fm = codeNode.getAfterParent().getAfterFeatureMapping();
            final Lines diffLines = codeNode.getLinesInDiff();

            return Optional.of(new PatternMatch<>(this,
                    diffLines.getFromInclusive(), diffLines.getToExclusive(), fm
            ));
        }

        return Optional.empty();
    }

    @Override
    public FeatureContext[] getFeatureContexts(PatternMatch<DiffNode> patternMatch) {
        return new FeatureContext[]{
                new FeatureContext(patternMatch.getFeatureMappings()[0])
        };
    }
}
