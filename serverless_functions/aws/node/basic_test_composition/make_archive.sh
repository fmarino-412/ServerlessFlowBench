#!/bin/zsh
cd $(dirname $0)
rm cpu_test.zip
rm latency_test.zip
zip cpu_test.zip ./cpu_test/index.js ./cpu_test/package.json
zip latency_test.zip ./latency_test/index.js ./latency_test/package.json
sh ./cpu_test/make_archive.sh
sh ./latency_test/make_archive.sh