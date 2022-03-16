#! /bin/bash

if [ "$1" == '' ]
then
  echo "Either fully run DiffDetective as presented in the paper (replication), run the proofs (proofs) or a do quick setup validation (validation)."
  echo "-- Examples --"
  echo "Run replication: './experiment.sh replication'"
  echo "Run proofs: './experiment.sh proofs'"
  echo "Validate the setup: './experiment.sh validation'"
  exit
fi
cd /home/sherlock || exit
if [ "$1" == 'replication' ]
then
    echo "Running full replication."
    cd holmes || exit
    java -jar DiffDetectiveRunner.jar
elif [ "$1" == 'validation' ]
then
    echo "Running a short validation."
    echo "NOT IMPLEMENTED ... EXIT"
elif [ "$1" == 'proofs' ]
then
    echo "Running the proofs"
    cd proofs || exit
    stack run
else
    echo "Either fully run DiffDetective as presented in the paper (replication), run the proofs (proof) or a do quick setup validation (validation)."
    echo "-- Examples --"
    echo "Run simulation: './experiment.sh replication'"
    echo "Validate the setup: './experiment.sh validation'"
    exit
fi
