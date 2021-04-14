parser grammar PatchParser;

options { tokenVocab=PatchLexer; }

patch: content* EOF;

codeLine: (ADD | REM | NORMAL) CODE;

content: codeLine | annotation;

annotation:
    addIf ADD ENDIF
    | remIf REM ENDIF
    | addIf remIf NORMAL ENDIF
    | remIf addIf NORMAL ENDIF
    | normalIf NORMAL ENDIF
    | addElse | addElif | remElse | remElif | normalElse | normalElif;

addIf: ADD IF content*;
remIf: REM IF content*;
normalIf: NORMAL IF content*;

addElse: ADD ELSE content*;
remElse: REM ELSE content*;
normalElse: NORMAL ELSE content*;

addElif: ADD ELIF content*;
remElif: REM ELIF content*;
normalElif: NORMAL ELIF content*;


