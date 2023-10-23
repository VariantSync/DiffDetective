#!/bin/bash
# Call this script from the repository's root directory './scripts/generateANTLRClasses.sh'

# Define the paths and package information
GRAMMAR_DIR="src/main/resources/grammars"
GRAMMAR_FILE="CExpression.g4"
PACKAGE="org.variantsync.diffdetective.feature.antlr"
OUTPUT_DIR="$(pwd)/src/main/java/org/variantsync/diffdetective/feature/antlr"

cd $GRAMMAR_DIR

# Call ANTLR to generate the lexer and parser
antlr4 -o $OUTPUT_DIR -package "$PACKAGE" "$GRAMMAR_FILE"

# Provide feedback to the user
if [ $? -eq 0 ]; then
    echo "ANTLR Lexer and Parser generated successfully in '$OUTPUT_DIR/$PACKAGE'."
else
    echo "ANTLR generation and compilation failed."
fi
