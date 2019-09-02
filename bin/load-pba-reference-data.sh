serviceToken="$(./idam-service-token.sh rd_professional_api)"

echo ${serviceToken}

curl -X POST http://localhost:8090/v1/organisations \
-H "Content-Type: application/json" \
-H "ServiceAuthorization: Bearer ${serviceToken}" \
-d '{"name": "TEST ORG5","status": "ACTIVE","sraId": "1115","sraRegulated": true,"companyNumber": "1110115","companyUrl": "http://testorg5.co.uk","superUser": {"firstName": "Tester5","lastName": "Tester5","email": "henry_fr_harper@yahoo.com"},"paymentAccount": ["PBAtpc1011"]}'
