# A simple script for deploying an update of the functjonal library to the local maven repository
mvn deploy:deploy-file -DgroupId=org.variantsync -DartifactId=functjonal -Dversion=1.0-SNAPSHOT -Durl=file:../local-maven-repo/ -DrepositoryId=local-maven-repo -DupdateReleaseInfo=true -Dfile=../src/main/resources/lib/functjonal-1.0-SNAPSHOT.jar
rm -rf ~/.m2/repository/org/variantsync/functjonal/