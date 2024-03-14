# A simple script for deploying an update of the functjonal library to the local maven repository.
# The script takes a single argument, the path to the functjonal jar file to deploy.
# To update the repository, you may delete directory 'org/variantsync/functjonal'.
# Then run this script.
mvn deploy:deploy-file -DgroupId=org.variantsync -DartifactId=functjonal -Dversion=1.0-SNAPSHOT -Durl=file:../local-maven-repo/ -DrepositoryId=local-maven-repo -DupdateReleaseInfo=true -Dfile=$1
rm -rf ~/.m2/repository/org/variantsync/functjonal/
