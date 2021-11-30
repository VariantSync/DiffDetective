package diff.difftree.serialize;

import diff.difftree.DiffNode;
import diff.serialize.LineGraphExport;
import org.prop4j.Node;

@Deprecated
public class DiffNodeLineGraphExporter {
    private static String prettyPrintTypeAndMapping(DiffNode n) {
        String result = n.codeType.name;
        final Node fm = n.getDirectFeatureMapping();
        if (fm != null) {
            result += " " + fm;
        }
        return result;
    }

    private static String prettyPrintIfMacroOr(DiffNode n, String elseValue) {
        String result = "";
        if (n.codeType.isMacro()) {
            result += prettyPrintTypeAndMapping(n);
        } else {
            result += elseValue;
        }
        return result;
    }

    private static String getLabelForPatternMining(DiffNode n) {
        return switch(n.codeType) {
            case CODE -> n.getLabel();
            default -> n.diffType + "_" + n.getLabel();
        };
    }

    public static String toLineGraphFormat(DiffNode n, LineGraphExport.Options options) {
        return "v " + n.getID() + " " + switch (options.nodePrintStyle()) {
            case LabelOnly -> n.getLabel();
            case Type -> n.diffType + "_" + n.codeType;
            case Code -> "\"" + prettyPrintIfMacroOr(n, n.getLabel().trim()) + "\"";
            case Mappings -> n.diffType + "_" + n.codeType + "_\"" + prettyPrintIfMacroOr(n, "") + "\"";
            case Debug -> n.diffType + "_" + n.codeType + "_\"" + prettyPrintIfMacroOr(n, n.getLabel().trim()) + "\"";
            case Mining -> getLabelForPatternMining(n);
        };
    }
}
