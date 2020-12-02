#!/bin/bash
cd anger_detection
sh compress.sh

cd ../image_recognition
sh compress.sh

cd ..
mv anger_detection/anger_detection.zip ./anger_detection.zip
mv image_recognition/image_recognition.zip ./image_recognition.zip