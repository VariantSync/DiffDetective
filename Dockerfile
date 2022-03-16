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

# TODO: Start main in DiffTreeMiner
# TODO: proofs: stack installieren, stack update, stack upgrade, stack run im ordner

# COMPILE STAGE
FROM alpine:3.14

# EXAMPLE: Prepare the compile environment. JDK is automatically installed
RUN apk add maven

# REQUIRED: Create and navigate to a working directory
WORKDIR /home/user

# EXAMPLE: If a local maven repository is used, you have to copy it here. If other libraries are required to compile, you have to
# also copy them at this point.
COPY local-maven-repo ./local-maven-repo

# EXAMPLE: Copy the source code
COPY src ./src
# EXAMPLE: Copy the pom.xml if Maven is used
COPY pom.xml .
# EXAMPLE: Execute the maven package process
RUN mvn package || exit

# ENVIRONMENT PREPARATION PHASE
FROM alpine:3.14

# REQUIRED: Create a user
RUN adduser --disabled-password  --home /home/user --gecos '' user

# REQUIRED: Install bash
RUN apk add --no-cache --upgrade bash

# EXAMPLE: Install Java
RUN apk add --update openjdk11 unzip

# EXAMPLE: Install all dependencies that are required to create plots using Python and Matplotlib
 RUN apk add --no-cache msttcorefonts-installer fontconfig
 RUN update-ms-fonts
 RUN apk add --no-cache tesseract-ocr python3 py3-pip py3-numpy && \
    pip3 install --upgrade pip setuptools wheel && \
    apk add --no-cache --virtual .build-deps gcc g++ zlib-dev make python3-dev py3-numpy-dev jpeg-dev && \
    pip3 install matplotlib && \
    apk del .build-deps
    
# REQUIRED: Change into the home directory
WORKDIR /home/user

# REQUIRED: Copy the docker resources
COPY docker-resources/* ./

# EXAMPLE: Copy the compiled JAR file from the first stage into the second stage
# Syntax: COPY --from=STAGE_ID SOURCE_PATH TARGET_PATH
COPY --from=0 /home/user/target ./target

# EXAMPLE: Copy possible input data
COPY experimental_subjects/* ./experimental_subjects/

# EXAMPLE: Copy possible evaluation scripts
COPY result_analysis_python ./result_analysis_python

# EXAMPLE: Unpack the experimental subjects
WORKDIR experimental_subjects
RUN unzip -o full_subjects.zip

# REQUIRED: Adjust permissions
RUN chown user:user /home/user -R
RUN chmod +x execute.sh
RUN chmod +x entrypoint.sh
RUN chmod +x fix-perms.sh

# EXAMPLE: List the content in the work dir
RUN ls -l

# REQUIRED: Set the entrypoint
ENTRYPOINT ["./entrypoint.sh", "./execute.sh"]

# REQUIRED: Set the user
USER user