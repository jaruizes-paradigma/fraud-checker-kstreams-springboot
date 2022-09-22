#!/bin/bash

echo "Generating movements....."
TODAY=$(date +"%Y-%m-%d")

#INITIAL: FIRST REGISTRATION OF SCHEMAS
curl --location --request POST 'http://localhost:8090/movement' --header 'Content-Type: application/json' --data-raw '{"id":"case_0-m1","card":"c0","amount":100.0,"origin": 1,"device": "shop-1","site": "","createdAt": "'$TODAY' 09:00:55 CEST"}'

#ATM & SHOP
curl --location --request POST 'http://localhost:8090/movement' --header 'Content-Type: application/json' --data-raw '{"id":"case_1-m1","card":"c1","amount":10.0,"origin": 1,"device": "atm-1","site": "","createdAt": "'$TODAY' 11:17:05 CEST"}'
curl --location --request POST 'http://localhost:8090/movement' --header 'Content-Type: application/json' --data-raw '{"id":"case_1-m2","card":"c1","amount":90.0,"origin": 1,"device": "atm-2","site": "","createdAt": "'$TODAY' 11:17:15 CEST"}'
curl --location --request POST 'http://localhost:8090/movement' --header 'Content-Type: application/json' --data-raw '{"id":"case_1-m3","card":"c1","amount":100.0,"origin": 2,"device": "shop-1","site": "","createdAt": "'$TODAY' 11:17:35 CEST"}'

#ONLINE
curl --location --request POST 'http://localhost:8090/movement' --header 'Content-Type: application/json' --data-raw '{"id":"case_2-m1","card":"c2","amount":10.0,"origin": 3,"site": "site1", "device": "", "createdAt": "'$TODAY' 11:47:05 CEST"}'
curl --location --request POST 'http://localhost:8090/movement' --header 'Content-Type: application/json' --data-raw '{"id":"case_2-m2","card":"c2","amount":90.0,"origin": 3,"site": "site2", "device": "", "createdAt": "'$TODAY' 11:47:15 CEST"}'
curl --location --request POST 'http://localhost:8090/movement' --header 'Content-Type: application/json' --data-raw '{"id":"case_2-m3","card":"c2","amount":200.0,"origin": 3,"site": "site3", "device": "", "createdAt": "'$TODAY' 11:47:35 CEST"}'
curl --location --request POST 'http://localhost:8090/movement' --header 'Content-Type: application/json' --data-raw '{"id":"case_2-m4","card":"c2","amount":100.0,"origin": 3,"site": "site4", "device": "", "createdAt": "'$TODAY' 11:47:55 CEST"}'

#NO FRAUD
curl --location --request POST 'http://localhost:8090/movement' --header 'Content-Type: application/json' --data-raw '{"id":"case_3-m1","card":"c2","amount":10.0,"origin": 3,"site": "site1", "device": "", "createdAt": "'$TODAY' 12:10:05 CEST"}'
curl --location --request POST 'http://localhost:8090/movement' --header 'Content-Type: application/json' --data-raw '{"id":"case_3-m2","card":"c2","amount":90.0,"origin": 3,"site": "site2", "device": "", "createdAt": "'$TODAY' 12:10:15 CEST"}'
curl --location --request POST 'http://localhost:8090/movement' --header 'Content-Type: application/json' --data-raw '{"id":"case_3-m3","card":"c2","amount":20.0,"origin": 3,"site": "site3", "device": "", "createdAt": "'$TODAY' 12:10:35 CEST"}'
curl --location --request POST 'http://localhost:8090/movement' --header 'Content-Type: application/json' --data-raw '{"id":"case_3-m4","card":"c2","amount":49.0,"origin": 3,"site": "site4", "device": "", "createdAt": "'$TODAY' 12:10:55 CEST"}'

curl --location --request POST 'http://localhost:8090/movement' --header 'Content-Type: application/json' --data-raw '{"id":"case_4-m1","card":"c1","amount":10.0,"origin": 1,"device": "atm-1","site": "","createdAt": "'$TODAY' 12:11:05 CEST"}'
curl --location --request POST 'http://localhost:8090/movement' --header 'Content-Type: application/json' --data-raw '{"id":"case_4-m2","card":"c1","amount":90.0,"origin": 1,"device": "atm-1","site": "","createdAt": "'$TODAY' 12:11:15 CEST"}'

curl --location --request POST 'http://localhost:8090/movement' --header 'Content-Type: application/json' --data-raw '{"id":"case_5-m1","card":"c3","amount":100.0,"origin": 2,"device": "shop-1","site": "","createdAt": "'$TODAY' 12:12:15 CEST"}'
curl --location --request POST 'http://localhost:8090/movement' --header 'Content-Type: application/json' --data-raw '{"id":"case_5-m2","card":"c3","amount":100.0,"origin": 2,"device": "shop-1","site": "","createdAt": "'$TODAY' 12:12:35 CEST"}'

#CLOSING ALL WINDOWS
curl --location --request POST 'http://localhost:8090/movement' --header 'Content-Type: application/json' --data-raw '{"id":"case_6-m1","card":"c4","amount":100.0,"origin": 1,"device": "shop-1","site": "","createdAt": "'$TODAY' 12:57:55 CEST"}'
curl --location --request POST 'http://localhost:8090/movement' --header 'Content-Type: application/json' --data-raw '{"id":"case_7-m2","card":"c4","amount":100.0,"origin": 3,"device": "","site": "site1","createdAt": "'$TODAY' 12:58:55 CEST"}'

echo ""
echo "-------------------------------"
echo "Movements topic: http://localhost:9081/ui/clusters/local/topics/movements"
echo "Fraud cases topic: http://localhost:9081/ui/clusters/local/topics/fraud-cases"
echo "-------------------------------"