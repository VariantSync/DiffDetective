package main.java.org.variantsync.diffdetective.variation.tree;

public class GumTreeDiffer<Domain, L extends Label> extends Differ<Domain, L> {
    public GumTreeDiffer(ObjectLanguage<Domain, Label> objectLanguage) {
        super(objectLanguage);
    }

    @Override
    public VariationTree<L> diffVariants(Map<Configuration, Variant<L>> variants) {
        if (variants.size() != 2) {
            throw new NotImplementedException();
        }

        VariationDiff<L> diff = Construction.diffUsingMatching(variants.get(0).tree(), variants.get(1).tree());
        return unshare(diff.getRoot(), variants.get(0).configuration(), variants.get(0).configuration(), NON);
    }

    private VariationTreeNode<L> unshare(DiffNode<L> node, Configuration[] configurations, DiffType existence) {
        if (node.getDiffType() == existence || existence != NON) {
            var result = new VariationTreeNode<>(
                node.getNodeType(),
                node.getFeatureMapping(),
                node.getLineRange(),
                node.getLabel()
            );

            for (var child : node.getAllChildren()) {
                result.addChild(unshare(child, configurations, existence));
            }

            return result;
        } else {
            var annotation = new TreeNode<>(
                NodeType.IF,
                configuration[existence == ADD ? 0 : 1].toFormula(),
                new LineRange(),
                node.getLabel().newInstance()
            );

            // TODO throw exception if moved child detected

            annotation.addChild(unshare, configurations, existence);
            return annotation;
        }
    }
}
