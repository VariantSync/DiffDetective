#! /bin/bash

if [ "$1" == '' ]; then
  echo "Either fully run DiffDetective as presented in the paper (replication), run the proofs (proofs) or a do quick setup validation (validation)."
  echo "-- Examples --"
  echo "Run replication: './experiment.sh replication'"
  echo "Run proofs: './experiment.sh proofs'"
  echo "Validate the setup: './experiment.sh validation'"
  exit
fi
cd /home/sherlock || exit
if [ "$1" == 'replication' ] || [ "$1" == 'validation' ]; then
  cd holmes || exit
  if [ "$1" == 'replication' ]; then
    echo "Running full replication. Depending on your system, this will require several hours or even a few days."
    java -jar DiffDetectiveRunner.jar docs/datasets.md
  elif [ "$1" == 'validation' ]; then
    echo "Running a short validation."
    java -jar DiffDetectiveRunner.jar docs/validation.md
  fi
  echo "Starting evaluation."
  cp -r results/* ../results/
  java -cp DiffDetectiveRunner.jar mining.FindMedianCommitTime ../results/difftrees
  java -cp DiffDetectiveRunner.jar mining.tablegen.MiningResultAccumulator ../results/difftrees ../results/difftrees
  echo "Finished replication. The results are located in the 'results' directory."
elif [ "$1" == 'proofs' ]; then
  echo "Running the proofs"
  cd proofs || exit
  stack run
else
  echo "INVALID ARGUMENT: $1"
  echo "Either fully run DiffDetective as presented in the paper (replication), run the proofs (proofs) or a do quick setup validation (validation)."
  echo "-- Examples --"
  echo "Run replication: './experiment.sh replication'"
  echo "Run proofs: './experiment.sh proofs'"
  echo "Validate the setup: './experiment.sh validation'"
  exit
fi
