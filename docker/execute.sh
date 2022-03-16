#! /bin/bash
# ------------------------
# You can freely adjust this script. Currently, it contains an example setup that offers three different execution options.
# This example assumes the execution of a JAR file
# ------------------------

if [ "$1" == '' ]
then
  echo "Either fully run the experiments as presented in the paper (replicate), evaluate the results (evaluate), or a do quick setup validation (validate)."
  echo "-- Bash Examples --"
  echo "Run simulation: './experiment.sh replicate'"
  echo "Evaluate all gathered results: './experiment.sh evaluate'"
  echo "Validate the setup: './experiment.sh validate'"
  exit
fi

echo "Starting $1"

patch --help || exit
cd /home/user || exit

echo "Copying jars"
  cp target/*-jar-with-dependencies* .
  echo ""

if [ "$1" == 'evaluate' ]
then
    echo "Running result evaluation"
    java -jar ResultEval-jar-with-dependencies.jar
    exit
else
  echo "Files in WORKDIR"
  ls -l
  echo ""

  if [ "$1" == 'replicate' ]
  then
      echo "Running full simulation."
      echo ""
      echo ""
      echo ""
      java -jar ExperimentRunner-jar-with-dependencies.jar
  elif [ "$1" == 'validate' ]
  then
      echo "Running a (hopefully) short validation."
      echo ""
      echo ""
      echo ""
      java -jar ExperimentRunner-jar-with-dependencies.jar
      echo "Running result evaluation"
      java -jar ResultEval-jar-with-dependencies.jar
  else
      echo "Either fully run the experiments as presented in the paper (replicate), evaluate the results (evaluate), or a do quick setup validation (validate)."
        echo "-- Bash Examples --"
        echo "Run simulation: './experiment.sh replicate'"
        echo "Evaluate all gathered results: './experiment.sh evaluate'"
        echo "Validate the setup: './experiment.sh validate'"
        exit
  fi
fi