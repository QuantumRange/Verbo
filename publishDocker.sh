#!/bin/bash

# Input
read -p "Enter version number: " version

read -p "verbo-$version.jar, is that correct? (y/n) " confirm

# Validation
if [ "$confirm" != "y" ]; then
  echo "Exiting script, please try again."
  exit 1
fi

if [ ! -f "verbo-$version.jar" ]; then
  echo "verbo-$version.jar does not exist. Exiting script."
  exit 1
fi

if [ -f verbo.jar ]; then
  rm verbo.jar
fi

# Build and Upload
sudo mv verbo-$version.jar verbo.jar

sudo docker rm verbo:v$version
sudo docker rm verbo:latest

sudo docker build -t verbo:v$version .
sudo docker build -t verbo:latest .

sudo docker tag verbo:v$version qrqrqr/verbo:v$version
sudo docker tag verbo:latest qrqrqr/verbo:latest

sudo docker push qrqrqr/verbo:v$version
sudo docker push qrqrqr/verbo:latest