#!/bin/bash

# Build the project
./gradlew clean build

# Create test images
java -cp build/classes/java/main dev.xerohero.TestImageGenerator

# Run the application with test images
java -jar build/libs/JImg2PDF-1.0-SNAPSHOT.jar output.pdf test1.png test2.png test3.png

echo "If successful, output.pdf has been created with the test images."
