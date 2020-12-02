#!/bin/bash
zip -6 language_detection.zip *.js
zip -6 -g language_detection.zip package.json
zip -6 -g -r language_detection.zip node_modules