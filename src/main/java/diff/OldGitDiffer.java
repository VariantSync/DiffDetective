package diff;

import diff.data.LineDiff;
import org.pmw.tinylog.Logger;
import org.prop4j.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Deprecated
public class OldGitDiffer {

    final String ifRegex = "^[+-]?\\s*#\\s*if.*$";
    final String endifRegex = "^[+-]?\\s*#\\s*endif.*$";
    final String elseRegex = "^[+-]?\\s*#\\s*else.*$";
    final String elifRegex = "^[+-]?\\s*#\\s*elif.*$";

    final String annotationRegex = "^[+-]?\\s*#\\s*(if|endif|else|elif).*$";

    public static final String EQUAL_PLACEHOLDER = "__eq__";


    private void simplifyShortDiff(List<LineDiff> shortDiff) {
        List<LineDiff> toRemove = new ArrayList<>();
        LineDiff last = shortDiff.get(0);
        for (int i = 1; i < shortDiff.size(); i++) {
            LineDiff current = shortDiff.get(i);
            if (last.sameDiff(current)) {
                last.addCorrespondingLines(current.getCorrespondingLines());
                toRemove.add(current);
            } else {
                last = current;
            }
        }
        shortDiff.removeAll(toRemove);
    }

    private void parseLine(String line, List<Node> presenceConditions) {
        if (line.matches(ifRegex)) {
            presenceConditions.add(getPresenceCondition(line));

        } else if (line.matches(elseRegex)) {
            if (!presenceConditions.isEmpty()) {
                Node ifPresenceCondition = presenceConditions.remove(presenceConditions.size() - 1);
                presenceConditions.add(new Not(ifPresenceCondition));
            } else {
                presenceConditions.add(new Literal("__INVALID_ELSE__"));
            }

        } else if (line.matches(elifRegex)) {
            Node elifPresenceCondition = getPresenceCondition(line);
            if (!presenceConditions.isEmpty()) {
                Node ifPresenceCondition = presenceConditions.remove(presenceConditions.size() - 1);
                presenceConditions.add(new And(
                        new Not(ifPresenceCondition),
                        elifPresenceCondition
                ));
            } else {
                presenceConditions.add(elifPresenceCondition);
            }

        } else if (line.matches(endifRegex)) {
            if (!presenceConditions.isEmpty()) {
                presenceConditions.remove(presenceConditions.size() - 1);
            }
        }
    }

    private LineDiff.Type getLineDiffType(String line) {
        if (line.startsWith("+")) {
            if (line.matches(ifRegex)) {
                return LineDiff.Type.ADD_IF;

            } else if (line.matches(endifRegex)) {
                return LineDiff.Type.ADD_ENDIF;

            } else if (line.matches(elseRegex)) {
                return LineDiff.Type.ADD_ELSE;

            } else if (line.matches(elifRegex)) {
                return LineDiff.Type.ADD_ELIF;

            } else {
                return LineDiff.Type.ADD_NORMAL;
            }
        } else if (line.startsWith("-")) {
            if (line.matches(ifRegex)) {
                return LineDiff.Type.REM_IF;

            } else if (line.matches(endifRegex)) {
                return LineDiff.Type.REM_ENDIF;

            } else if (line.matches(elseRegex)) {
                return LineDiff.Type.REM_ELSE;

            } else if (line.matches(elifRegex)) {
                return LineDiff.Type.REM_ELIF;

            } else {
                return LineDiff.Type.REM_NORMAL;
            }
        } else {
            if (line.matches(annotationRegex)) {
                return LineDiff.Type.ANNOTATION;
            } else {
                return LineDiff.Type.NORMAL;
            }
        }
    }

    private Node getPresenceCondition(String line) {

        String presenceConditionString = getPresenceConditionString(line);

        Node node = null;
        if (presenceConditionString != null) {
            NodeReader nodeReader = new NodeReader();
            nodeReader.activateJavaSymbols();
            node = nodeReader.stringToNode(presenceConditionString);
        } else {
            presenceConditionString = "__INVALID_ANNOTATION__";
        }

        if (node == null) {
            Logger.warn("Could not parse presence condition for line \"{}\"", line);
            node = new Literal(presenceConditionString);
        }

        // negate for ifndef
        if (line.contains("ifndef")) {
            node = new Not(node);
        }

        return node;

    }


    private String getPresenceConditionString(String line) {

        // ^[+-]?\s*#\s*(if|ifdef|ifndef|elif)(\s+(.*)|\((.*)\))$
        String regex = "^[+-]?\\s*#\\s*(if|ifdef|ifndef|elif)(\\s+(.*)|\\((.*)\\))$";
        Pattern regexPattern = Pattern.compile(regex);
        Matcher matcher = regexPattern.matcher(line);

        String presenceCondition;
        if (matcher.find()) {
            if (matcher.group(3) != null) {
                presenceCondition = matcher.group(3);
            } else {
                presenceCondition = matcher.group(4);
            }
        } else {
            return null;
        }

        // remove comments
        presenceCondition = presenceCondition.split("//")[0];
        presenceCondition = presenceCondition.replaceAll("/\\*.*\\*/", "");

        // remove whitespace
        presenceCondition = presenceCondition.trim();

        // remove defined(), ENABLED() and DISABLED()
        presenceCondition = presenceCondition.replaceAll("defined\\s*\\(([^)]*)\\)", "$1");
        presenceCondition = presenceCondition.replaceAll("defined ", " ");
        presenceCondition = presenceCondition.replaceAll("ENABLED\\s*\\(([^)]*)\\)", "$1");
        presenceCondition = presenceCondition.replaceAll("DISABLED\\s*\\(([^)]*)\\)", "!($1)");

        // remove whitespace

        presenceCondition = presenceCondition.replaceAll("\\s", "");

        // remove parentheses from custom cpp functions such as MB() or PIN_EXISTS()
        presenceCondition = presenceCondition.replaceAll("(\\w+)\\((\\w*)\\)", "$1__$2");

        // replace all "=="'s with a placeholder because NodeReader parses these
        presenceCondition = presenceCondition.replaceAll("==", EQUAL_PLACEHOLDER);

        return presenceCondition;
    }
}
