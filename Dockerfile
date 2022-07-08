# syntax=docker/dockerfile:1
# ----------------------------
# This template sets up a Docker container using a multi-stage build and Alpine Linux.
# Alpine is a Linux-based OS embedded systems making it one of the smalles Docker images. It should be used if possible.
# The multi-stage build consists of two stages: The compile stage, and the environment preparation stage.
#
# The compile stage only installs the packages required to compile the source files of the prototype. The generated binaries
# are then copied to the environment during the environment preparation stage.
#
# The environment preparation stage is responsible for installing all dependencies and copying all files that are required
# to execute the Docker container.
#
# This template contains the commands that are required to compile and execute a Java prototype.
#
# Lines commented as `REQUIRED` should only be altered/removed if you know what you are doing.
# Lines commented as `EXAMPLE` contain commands that probably have to be adjusted
# ----------------------------


FROM alpine:3.15
# PACKAGE STAGE

# EXAMPLE: Prepare the compile environment. JDK is automatically installed
RUN apk add maven

# REQUIRED: Create and navigate to a working directory
WORKDIR /home/user

COPY local-maven-repo ./local-maven-repo

# EXAMPLE: Copy the source code
COPY src ./src
# EXAMPLE: Copy the pom.xml if Maven is used
COPY pom.xml .
# EXAMPLE: Execute the maven package process
RUN mvn package || exit

FROM alpine:3.15

# Create a user
RUN adduser --disabled-password  --home /home/sherlock --gecos '' sherlock

RUN apk add --no-cache --upgrade bash
RUN apk add --update openjdk17

# REQUIRED: Change into the home directory
WORKDIR /home/sherlock

# Copy the compiled JAR file from the first stage into the second stage
# Syntax: COPY --from=STAGE_ID SOURCE_PATH TARGET_PATH
WORKDIR /home/sherlock/holmes
COPY --from=0 /home/user/target/diffdetective-1.0.0-jar-with-dependencies.jar ./DiffDetective.jar
WORKDIR /home/sherlock

# Copy the setup
COPY docs holmes/docs

# Copy the docker resources
COPY docker/* ./
RUN mkdir DiffDetectiveMining

#  Adjust permissions
RUN chown sherlock:sherlock /home/sherlock -R
RUN chmod +x execute.sh
RUN chmod +x entrypoint.sh
RUN chmod +x fix-perms.sh

# EXAMPLE: List the content in the work dir
RUN ls -l

# REQUIRED: Set the entrypoint
ENTRYPOINT ["./entrypoint.sh", "./execute.sh"]

# REQUIRED: Set the user
USER sherlock