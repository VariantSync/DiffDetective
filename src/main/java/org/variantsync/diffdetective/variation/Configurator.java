package org.variantsync.diffdetective.variation;

public final class Configurator<Domain, L extends Label> {
    private ObjectLanguage<Domain, L> objectLanguage;

    public Configurator(ObjectLanguage<Domain, L> objectlanguage) {
        this.objectlanguage = objectlanguage;
    }

    public Domain configure(VariationTree<L> tree, Configuration configuration) {
        return objectLanguage.assemble(configureToVariant(tree, configuration));
    }

    public Variant<L> configureToVariant(VariationTree<L> tree, Configuration configuration) {
        return new Variant(configureToVariant(tree.root(), configuration), tree.getSource());
    }

    private VariationNode<L> configureToVariant(TreeNode<?, L> node, Configuration configuration) {
        var resultNode = node.shallowToVariationTree();
        for (var child : node.getChildren()) {
            if (child.isElseIf() || child.isElse()) {
                continue;
            }

            if (configuration.includes(child.getPresenceCondition())) {
                resultNode.addChild(configureToVariant(child, configuration));
            } else if (child.isConditionalAnnotation()) {
                findAndConfigureElse(child, configuration, resultNode);
            }
        }
        return resultNode;
    }

    private void findAndConfigureElse(VariationNode<?, L> conditionalNode, Configuration configuration, VariationNode destination) {
        for (var child : conditionalNode.getChildren()) {
            if (child.isElseIf() || child.isElse()) {
                if (configuration.includes(child.getPresenceCondition())) {
                    destination.addChild(configureToVariant(child, configuration));
                } else if (child.isElseIf()) {
                    findAndConfigureElse(child, configuration, destination);
                }
            }
        }
    }

    public Set<String> allFeaturesOf(VariationNode<?, L> node) {
        var features = new HashSet<String>();

        node.forAll(child -> {
            features.addAll(child.getFeatureMapping().getUniqueContainedFeatures());
        });

        return features;
    }

    public Stream<Configuration> allConfigs(VariationNode<?, L> node) {
        throw new NotImplementedException();
    }

    public Stream<Variant<L>> allVariantsOf(VariationNode<?, L> node) {
        return allConfigs(node).map(configuration -> configureToVariant(node, configuration));
    }
}
