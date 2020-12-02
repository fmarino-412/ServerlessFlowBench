#!/bin/bash
zip -6 image_recognition.zip ./*.js
zip -6 -g image_recognition.zip package.json
zip -6 -g -r image_recognition.zip node_modules