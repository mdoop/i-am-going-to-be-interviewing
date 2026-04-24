#!/usr/bin/env bash
set -euo pipefail

JAVA_URL="${JAVA_URL:-https://interview-pricing-java.onrender.com}"
KOTLIN_URL="${KOTLIN_URL:-https://interview-pricing-kotlin.onrender.com}"
REQUESTS="${REQUESTS:-20}"

payload='{
  "sku": "SKU-RED-CHAIR",
  "storeId": "STORE-100",
  "channel": "ONLINE",
  "customerSegment": "LOYALTY",
  "quantity": 1,
  "couponCode": "SAVE15"
}'

for service_url in "$JAVA_URL" "$KOTLIN_URL"; do
  for i in $(seq 1 "$REQUESTS"); do
    curl -fsS -X POST "$service_url/v1/price-quotes" \
      -H 'Content-Type: application/json' \
      -H "X-Trace-Id: demo-$i" \
      -d "$payload" >/dev/null || true
  done
done

echo "Sent $REQUESTS requests to each service."
