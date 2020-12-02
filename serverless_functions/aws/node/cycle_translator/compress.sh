#!/bin/bash
cd language_detection
sh compress.sh

cd ../loop_controller
sh compress.sh

cd ../sentence_translation
sh compress.sh

cd ../translation_logger
sh compress.sh

cd ..
mv language_detection/language_detection.zip ./language_detection.zip
mv loop_controller/loop_controller.zip ./loop_controller.zip
mv sentence_translation/sentence_translation.zip ./sentence_translation.zip
mv translation_logger/translation_logger.zip ./translation_logger.zip