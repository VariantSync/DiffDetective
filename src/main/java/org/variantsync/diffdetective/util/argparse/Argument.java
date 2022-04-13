package org.variantsync.diffdetective.util.argparse;

import java.util.function.Consumer;

public class Argument {
    public final static int Flag = 0;
    public final static int AnyNumberOfParameters = -1;
    public final static boolean IsOptional = false;
    public final static boolean IsMandatory = true;

    protected boolean mandatory;
    protected String name;
    protected int len;
    protected Consumer<Void> definedCallback;
    protected Consumer<String> parameterCallback;

    /**
     *
     * @param name
     * @param len Number of additional parameters (0: Argument is a plain flag; -1: arbitrary number of parameters)
     */
    public Argument(String name, int len, boolean isMandatory, Consumer<Void> ifDefined, Consumer<String> parameterCallback) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("name can neither be null nor empty! was: " + name);
        }

        if (name.length() == 1 && len != 0) {
            throw new IllegalArgumentException("Flags cannot have parameters! Flag \"" + name + "\" has " + len + " parameters but should have 0!");
        }

        this.name = name;
        this.len = len;
        this.mandatory = isMandatory;
        this.definedCallback = ifDefined;
        this.parameterCallback = parameterCallback;
    }

    public static Argument WithParameters(String name, int numParameters, boolean isMandatory, Consumer<Void> ifDefined, Consumer<String> parameterCallback) {
        return new Argument(name, numParameters, isMandatory, ifDefined, parameterCallback);
    }

    public static Argument WithAnyNumberOfParameters(String name, boolean isMandatory, Consumer<Void> ifDefined, Consumer<String> parameterCallback) {
        return new Argument(name, AnyNumberOfParameters, isMandatory, ifDefined, parameterCallback);
    }

    public static Argument Flag(char flag, Consumer<Void> ifDefined) {
        return new Argument("" + flag, Flag, IsOptional, ifDefined, null);
    }
}
