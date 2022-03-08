#!/usr/bin/env bash
echo ${TEST_URL}
echo "Run ZAP scan and generate reports"
zap-api-scan.py -t ${URL_FOR_SECURITY_SCAN}/v2/api-docs -f openapi -S -d -u ${SECURITY_RULES} -P 1001 -l FAIL --hook=zap_hooks.py -J report.json -r api-report.html
echo "Print alerts"
zap-cli --zap-url http://0.0.0.0 -p 1001 alerts -l Informational --exit-code False
export LC_ALL=C.UTF-8
export LANG=C.UTF-8
echo "Print zap.out logs:"
cat zap.out
echo "Copy artifacts for archiving"
cp /zap/api-report.html functional-output/
cat zap.out