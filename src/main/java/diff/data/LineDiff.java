package diff.data;

import org.prop4j.Node;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Data class representing a single line in a short diff.
 *
 * @author Sören Viegener
 */
@Deprecated
public class LineDiff implements Serializable {
    private final Type type;
    private final List<Integer> correspondingLines;
    private final Node presenceCondition;

    public LineDiff(Type type) {
        this(type, null);
    }

    public LineDiff(Type type, Node presenceCondition, Integer... correspondingLines) {
        this.type = type;
        this.correspondingLines = new ArrayList<>(Arrays.asList(correspondingLines));
        this.presenceCondition = presenceCondition;
    }

    public void addCorrespondingLines(List<Integer> correspondingLine) {
        this.correspondingLines.addAll(correspondingLine);
    }

    public boolean sameDiff(LineDiff other){
        if(!this.type.equals(other.type)){
            return false;
        }

        if(this.presenceCondition == null){
            return other.presenceCondition == null;
        }else {
            return this.presenceCondition.equals(other.presenceCondition);
        }
    }

    public enum Type {
        ADD_IF("+if"),
        ADD_ENDIF("+endif"),
        ADD_ELSE("+else"),
        ADD_ELIF("+elif"),
        ADD_NORMAL("+normal"),
        REM_IF("-if"),
        REM_ENDIF("-endif"),
        REM_ELSE("-else"),
        REM_ELIF("-elif"),
        REM_NORMAL("-normal"),
        ANNOTATION("#"),
        NORMAL(".");

        public final String name;

        Type(String name) {
            this.name = name;
        }
    }

    // HELPER METHODS FOR TYPE

    public boolean isAdd() {
        return isAddIf() || isAddEndIf() || isAddElse() || isAddElif() || isAddNormal();
    }

    public boolean isAddIf() {
        return this.type == Type.ADD_IF;
    }

    public boolean isAddEndIf() {
        return this.type == Type.ADD_ENDIF;
    }

    public boolean isAddElse() {
        return this.type == Type.ADD_ELSE;
    }

    public boolean isAddElif() {
        return this.type == Type.ADD_ELIF;
    }

    public boolean isAddNormal() {
        return this.type == Type.ADD_NORMAL;
    }

    public boolean isRem() {
        return isRemIf() || isRemEndIf() || isRemElse() || isRemElif() || isRemNormal();
    }

    public boolean isRemIf() {
        return this.type == Type.REM_IF;
    }

    public boolean isRemEndIf() {
        return this.type == Type.REM_ENDIF;
    }

    public boolean isRemElse() {
        return this.type == Type.REM_ELSE;
    }

    public boolean isRemElif() {
        return this.type == Type.REM_ELIF;
    }

    public boolean isRemNormal() {
        return this.type == Type.REM_NORMAL;
    }

    public boolean isNormal() {
        return this.type == Type.NORMAL;
    }

    public boolean isAnnotation() {
        return this.type == Type.ANNOTATION;
    }

    // GETTERS

    public Type getType() {
        return type;
    }

    public List<Integer> getCorrespondingLines() {
        return correspondingLines;
    }

    public Node getPresenceCondition() {
        return presenceCondition;
    }

    // OTHERS

    @Override
    public String toString() {
        return String.format("%1$-10s", type.name) + " (PC: " + presenceCondition + ")";
    }

    public String toString(boolean shortString) {
        if (shortString) {
            return type.name;
        } else {
            return toString();
        }
    }
}
