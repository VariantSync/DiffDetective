package org.variantsync.diffdetective.util.argparse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArgParser {
    private final Map<String, Argument> arguments;

    public ArgParser(Argument... args) {
        arguments = new HashMap<>();

        for (Argument arg : args) {
            String argumentName;

            if (arg.name.length() == 1) {
                argumentName = "-";
            } else {
                argumentName = "--";
            }

            argumentName += arg.name;

            if (arguments.containsKey(argumentName)) {
                throw new IllegalArgumentException("Duplicate argument specification: Argument \"" + argumentName + "\" already exists!");
            } else {
                arguments.put(argumentName, arg);
            }
        }
    }

    public void parse(String[] args) {
        Argument currentArgument = null;
        List<Argument> handledArgs = new ArrayList<>();
        List<String> handledStrargs = new ArrayList<>();

        for (String strarg : args) {
            if (strarg.startsWith("-")) {
                Argument arg = arguments.get(strarg);

                if (arg != null) {
                    if (handledStrargs.contains(strarg)) {
                        throw new IllegalArgumentException("Duplicate specification of rgument \"" + strarg + "\"!");
                    }

                    currentArgument = arg;
                    handledStrargs.add(strarg);
                    handledArgs.add(currentArgument);

                    if (currentArgument.definedCallback != null) {
                        currentArgument.definedCallback.accept(null);
                    }
                } else {
                    StringBuilder errorMsg = new StringBuilder("Unknown argument \"" + strarg + "\" given!\nAvailable arguments are:");

                    for (Map.Entry<String, Argument> availableArg : arguments.entrySet()) {
                        errorMsg.append("\n").append(availableArg.getKey());
                    }

                    throw new IllegalArgumentException(errorMsg.toString());
                }
            } else {
                if (currentArgument != null && currentArgument.parameterCallback != null) {
                    currentArgument.parameterCallback.accept(strarg);
                } else {
                    throw new IllegalArgumentException("Unknown parameter \"" + strarg + "\" given! An argument (beginning with \"-\" or \"--\") has to be specified first");
                }
            }
        }

        for (Map.Entry<String, Argument> kv : this.arguments.entrySet()) {
            Argument arg = kv.getValue();
            if (arg.mandatory && !handledArgs.contains(arg)) {
                throw new IllegalArgumentException("Argument \"" + arg.name + "\" is mandatory but not specified!");
            }
        }
    }
}
