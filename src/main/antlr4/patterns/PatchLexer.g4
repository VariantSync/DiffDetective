lexer grammar PatchLexer;

ADD: '+';
REM: '-';
NORMAL: ' ';

NEWLINE: ('\r\n'|'\n'|'\r');

IF: 'if' NEWLINE;
ELSE: 'else' NEWLINE;
ELIF: 'elif' NEWLINE;
ENDIF: 'endif' NEWLINE;
CODE: 'code' NEWLINE;

