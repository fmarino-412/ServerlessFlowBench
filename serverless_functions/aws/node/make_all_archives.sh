#!/bin/zsh
cd $(dirname $0)
sh ./basic_test_composition/make_archives.sh
sh ./face_recognition/make_archives.sh
sh ./memory_test/make_archive.sh