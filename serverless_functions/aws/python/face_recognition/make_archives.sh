#!/bin/zsh
cd $(dirname $0)
rm image_recognition.zip
rm anger_detection.zip
zip image_recognition.zip ./image_recognition/image_recognition.py
zip anger_detection.zip -r ./anger_detection/anger_detection.py