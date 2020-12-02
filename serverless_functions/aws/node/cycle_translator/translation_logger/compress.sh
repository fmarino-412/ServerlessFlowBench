#!/bin/bash
zip -6 translation_logger.zip *.js
zip -6 -g translation_logger.zip package.json
zip -6 -g -r translation_logger.zip node_modules