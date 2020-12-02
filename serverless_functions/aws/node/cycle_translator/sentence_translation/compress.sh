#!/bin/bash
zip -6 sentence_translation.zip *.js
zip -6 -g sentence_translation.zip package.json
zip -6 -g -r sentence_translation.zip node_modules