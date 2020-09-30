#!/bin/zsh
cd $(dirname $0)
rm cpu_test.zip
rm latency_test.zip
zip cpu_test.zip ./cpu_test/cpu_test.py
zip latency_test.zip ./latency_test/latency_test.py
sh ./cpu_test/make_archive.sh
sh ./latency_test/make_archive.sh