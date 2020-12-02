#!/bin/bash
zip -6 anger_detection.zip *.js
zip -6 -g anger_detection.zip package.json
zip -6 -g -r anger_detection.zip node_modules