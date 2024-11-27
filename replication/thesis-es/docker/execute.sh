#!/usr/bin/env bash

cd /home/sherlock/holmes || exit

echo "Running the experiment."
java -cp DiffDetective.jar org.variantsync.diffdetective.experiments.thesis-es.Main

echo "Collecting results."
cp -r results/* ../results/
echo "The results are located in the 'results' directory."

