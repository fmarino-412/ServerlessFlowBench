#!/bin/bash
(cd cpu_test && sh compress.sh)
(cd latency_test && sh compress.sh)
cp cpu_test/cpu_test.zip ./cpu_test.zip
cp latency_test/latency_test.zip ./latency_test.zip