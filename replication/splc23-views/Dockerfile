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

RUN apk add --no-cache tesseract-ocr python3 py3-pip py3-numpy py3-pandas py3-scipy && \
    pip3 install --upgrade pip setuptools wheel && \
    apk add --no-cache --virtual .build-deps gcc g++ zlib-dev make python3-dev py3-numpy-dev jpeg-dev && \
    pip3 install Jinja2 && \
    apk del .build-deps

# Change into the home directory
WORKDIR /home/sherlock

# Copy the compiled JAR file from the first stage into the second stage
# Syntax: COPY --from=STAGE_ID SOURCE_PATH TARGET_PATH
WORKDIR /home/sherlock/holmes
COPY --from=0 /home/user/target/diffdetective-2.1.0-jar-with-dependencies.jar ./DiffDetective.jar
WORKDIR /home/sherlock

# Copy the setup
COPY docs holmes/docs

# Copy the docker resources
COPY docker/* ./
COPY replication/splc23-views/docker/* ./

RUN mkdir DiffDetectiveMining
RUN mkdir results

#  Adjust permissions
RUN chown sherlock:sherlock /home/sherlock -R
RUN chmod +x execute.sh
RUN chmod +x entrypoint.sh
RUN chmod +x fix-perms.sh

# Set the entrypoint
ENTRYPOINT ["./entrypoint.sh", "./execute.sh"]

# Set the user
USER sherlock