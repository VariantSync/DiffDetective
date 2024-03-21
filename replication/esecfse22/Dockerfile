# syntax=docker/dockerfile:1

FROM alpine:3.15
# PACKAGE STAGE

# Prepare the compile environment. JDK is automatically installed
RUN apk add maven

# Create and navigate to a working directory
WORKDIR /home/user

COPY local-maven-repo ./local-maven-repo

# Copy the source code
COPY src ./src
# Copy the pom.xml if Maven is used
COPY pom.xml .
# Execute the maven package process
RUN mvn package || exit

FROM alpine:3.15

# Create a user
RUN adduser --disabled-password  --home /home/sherlock --gecos '' sherlock

RUN apk add --no-cache --upgrade bash
RUN apk add --update openjdk17

# Change into the home directory
WORKDIR /home/sherlock

# Copy the compiled JAR file from the first stage into the second stage
# Syntax: COPY --from=STAGE_ID SOURCE_PATH TARGET_PATH
WORKDIR /home/sherlock/holmes
COPY --from=0 /home/user/target/diffdetective-*-jar-with-dependencies.jar ./DiffDetective.jar
WORKDIR /home/sherlock
RUN mkdir results

# Copy the setup
COPY docs holmes/docs

# Copy the docker resources
COPY docker/* ./
COPY replication/esecfse22/docker/* ./
RUN mkdir DiffDetectiveMining

#  Adjust permissions
RUN chown sherlock:sherlock /home/sherlock -R
RUN chmod +x execute.sh
RUN chmod +x entrypoint.sh
RUN chmod +x fix-perms.sh

# Set the entrypoint
ENTRYPOINT ["./entrypoint.sh", "./execute.sh"]

# Set the user
USER sherlock