# Test cases of WONTFIX behaviour

## Sophisticated comment parsing
The test cases 01 and 02 would require the removal of comments before checking
if a line is a macro. The test cases 03 and 04 would required the detection of
multi-line macros which span multiple lines.

All of these cases require more engineering than they are worth because they
should be pretty rare in practice.
