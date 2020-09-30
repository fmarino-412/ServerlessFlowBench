#!/bin/zsh
cd $(dirname $0)
rm image_recognition.zip
rm anger_detection.zip
zip image_recognition.zip -r ./image_recognition/node_modules ./image_recognition/index.js ./image_recognition/package.json
zip anger_detection.zip -r ./anger_detection/node_modules ./anger_detection/index.js ./anger_detection/package.json