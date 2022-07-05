#! /bin/bash

if [ "$1" == '' ]; then
  echo "Either fully run DiffDetective as presented in the paper (replication) or a do quick setup verification (verification)."
  echo "-- Examples --"
  echo "Run replication: './execute.sh replication'"
  echo "Validate the setup: './execute.sh verification'"
  exit
fi
cd /home/sherlock || exit
  cd holmes || exit

if [ "$1" == 'replication' ]; then
  echo "Running full replication. Depending on your system, this will require several hours or even a few days."
  java -cp DiffDetective.jar org.variantsync.diffdetective.validation.Validation
elif [ "$1" == 'verification' ]; then
  echo "Running a short verification."
  java -cp DiffDetective.jar org.variantsync.diffdetective.validation.Validation docs/verification/datasets.md
else
  echo ""
  echo "Running detection on a custom dataset with the input file $1"
  echo ""
  java -cp DiffDetective.jar org.variantsync.diffdetective.validation.Validation $1
fi
echo "Collecting results."
cp -r results/* ../results/
java -cp DiffDetective.jar org.variantsync.diffdetective.validation.FindMedianCommitTime ../results/validation/current
java -cp DiffDetective.jar org.variantsync.diffdetective.tablegen.MiningResultAccumulator ../results/validation/current ../results/validation/current
echo "The results are located in the 'results' directory."

