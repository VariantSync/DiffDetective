package pattern.semantic;

import analysis.data.PatternMatch;
import diff.Lines;
import diff.difftree.DiffNode;
import evaluation.FeatureContext;
import org.prop4j.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AddIfdefElifSemanticPattern extends SemanticPattern{

    public static final String PATTERN_NAME = "AddIfdefElifSEM";

    public AddIfdefElifSemanticPattern() {
        this.name = PATTERN_NAME;
    }

    /*
    DETECTION:
        added if node
        has an added code child
        has an added elif child
            which has an added code child
            if it also has elif/else children
                they also need to have an added code child
     */
    @Override
    public Optional<PatternMatch<DiffNode>> match(DiffNode annotationNode) {
        if(annotationNode.isAdd() && annotationNode.isIf()){
            boolean addedCodeInIf = false;
            DiffNode elifNode = null;
            for(DiffNode child : annotationNode.getAllChildren()){
                if(child.isCode() && child.isAdd()){
                    addedCodeInIf = true;
                }
                if(child.isElif() && child.isAdd()){
                    elifNode = child;
                }
            }

            List<Node> mappings = new ArrayList<>();
            mappings.add(annotationNode.getAfterFeatureMapping());
            if(elifNode == null || !addedCodeInIf || !isValidElif(elifNode, mappings)){
                return Optional.empty();
            }

            final Lines diffLines = annotationNode.getLinesInDiff();
            return Optional.of(new PatternMatch<>(this,
                    diffLines.getFromInclusive(), diffLines.getToExclusive(),
                    mappings.toArray(new Node[0])
            ));
        }

        return Optional.empty();
    }

    private boolean isValidElif(DiffNode elifNode, List<Node> mappings) {
        boolean addedCode = false;
        DiffNode nextNode = null;

        for(DiffNode child : elifNode.getAllChildren()){
            if(child.isCode() && child.isAdd()){
                addedCode = true;
            }
            if((child.isElif() || child.isElse()) && child.isAdd()){
                nextNode = child;
            }
        }
        if(addedCode && nextNode != null){
            mappings.add(elifNode.getAfterFeatureMapping());
            return isValidElif(nextNode, mappings);
        }

        return addedCode;
    }

    @Override
    public FeatureContext[] getFeatureContexts(PatternMatch<DiffNode> patternMatch) {
        FeatureContext[] featureContexts = new FeatureContext[patternMatch.getFeatureMappings().length];
        for (int i = 0; i < featureContexts.length; i++) {
            featureContexts[i] = new FeatureContext(patternMatch.getFeatureMappings()[i]);
        }
        return featureContexts;
    }
}
