#!/bin/bash

# FeatureIDE
mvn deploy:deploy-file -DgroupId=de.ovgu -DartifactId=featureide.lib.fm -Dversion=3.8.1 -Durl=file:./local-maven-repo/ -DrepositoryId=local-maven-repo -DupdateReleaseInfo=true -Dfile=./lib/de.ovgu.featureide.lib.fm-v3.8.1.jar

# Functjonal
mvn deploy:deploy-file -DgroupId=org.variantsync -DartifactId=functjonal -Dversion=1.0-SNAPSHOT -Durl=file:./local-maven-repo/ -DrepositoryId=local-maven-repo -DupdateReleaseInfo=true -Dfile=./lib/functjonal-1.0-SNAPSHOT.jar

# sat4j
mvn deploy:deploy-file -DgroupId=org.sat4j -DartifactId=core -Dversion=2.3.5 -Durl=file:./local-maven-repo/ -DrepositoryId=local-maven-repo -DupdateReleaseInfo=true -Dfile=./lib/org.sat4j.core.jar
