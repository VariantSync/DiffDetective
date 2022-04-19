#!/bin/bash
input_file = "$1"
output_file = "$2"

graphml2gv "$1" -o temp.dot
dot -Tpng temp.dot -o "$2"

#Clean-Up
rm temp.dot
