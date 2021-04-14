parser grammar OldPatternParser;

options { tokenVocab=OldPatternLexer; }

patch: (pattern | fixAnnotation | NORMAL | ANNOTATION)* EOF;

pattern: addPattern | remPattern | otherPattern;

addPattern: addIfdef | addIfdefElse | addIfdefElif | addNormalCode;
remPattern: remIfdef | remIfdefElse | remIfdefElif | remNormalCode;
otherPattern: changeAnnotation;

patternContent: (anyNormal | pattern)*;
anyNormal: ADD_NORMAL | REM_NORMAL | NORMAL | ANNOTATION;

addIfdef: ADD_IF patternContent ADD_ENDIF;
addIfdefElse: ADD_IF thenContent=patternContent ADD_ELSE elseContent=patternContent ADD_ENDIF;
addIfdefElif: ADD_IF thenContent=patternContent (ADD_ELIF elifContent=patternContent)+ (ADD_ELSE elseContent=patternContent)? ADD_ENDIF;
addNormalCode: ADD_NORMAL;

remIfdef: REM_IF thenContent=patternContent REM_ENDIF;
remIfdefElse: REM_IF thenContent=patternContent REM_ELSE elseContent=patternContent REM_ENDIF;
remIfdefElif: REM_IF thenContent=patternContent (REM_ELIF elifContent=patternContent)+ (REM_ELSE elseContent=patternContent)? REM_ENDIF;
remNormalCode: REM_NORMAL;

changeAnnotation: changeIf | changeElse | changeElif | changeEndif;
changeIf: REM_IF changeIf* ADD_IF;
changeElse: REM_ELSE ADD_ELSE;
changeElif: REM_ELIF ADD_ELIF;
changeEndif: REM_ENDIF changeEndif* ADD_ENDIF;

fixAnnotation: addAnnotation | remAnnotation;
addAnnotation: ADD_IF | ADD_ELSE | ADD_ELIF | ADD_ENDIF;
remAnnotation: REM_IF | REM_ELSE | REM_ELIF | REM_ENDIF;




