# Retail Pricing Comparison

This repo contains two nearly identical Spring Boot services that solve the same retail pricing problem:

- `pricing-service-java`
- `pricing-service-kotlin`

Both services expose the same REST API, seed the same pricing and promotion data, and produce the same business results so you can compare Java and Kotlin directly.

## What The Services Do

Each service computes a retail price quote for a SKU by combining:

- base pricing by `sku` and `storeId`
- promotion precedence
- coupon eligibility
- quantity discounts
- fulfillment enrichment
- cache-backed hot-path reads

The project also includes:

- actuator health and Prometheus metrics
- JSON structured logs for Kibana-style analysis
- Postgres and Valkey local infra via Docker Compose
- Grafana and Prometheus monitoring scaffolding
- Maven multi-module build

## Modules

- `shared-contract`: shared request/response and domain contract types
- `pricing-service-java`: Java 21 + Spring Boot implementation
- `pricing-service-kotlin`: Kotlin + Spring Boot implementation

## Run Locally

### Default zero-dependency mode

Each service defaults to an in-memory H2 database and local fallback cache so you can run it without external infrastructure.

```bash
mvn -pl pricing-service-java spring-boot:run
mvn -pl pricing-service-kotlin spring-boot:run
```

Ports:

- Java service: `8081`
- Kotlin service: `8082`

### Docker-backed mode

Start the shared local dependencies:

```bash
docker compose up -d
```

Then run either service with the `docker` profile:

```bash
mvn -pl pricing-service-java spring-boot:run -Dspring-boot.run.profiles=docker
mvn -pl pricing-service-kotlin spring-boot:run -Dspring-boot.run.profiles=docker
```

## API Examples

### Quote request

```bash
curl -X POST http://localhost:8081/v1/price-quotes \
  -H 'Content-Type: application/json' \
  -H 'X-Trace-Id: demo-java-trace' \
  -d '{
    "sku": "SKU-RED-CHAIR",
    "storeId": "STORE-100",
    "channel": "ONLINE",
    "customerSegment": "LOYALTY",
    "quantity": 1,
    "couponCode": "SAVE15"
  }'
```

Switch to port `8082` for the Kotlin service.

### Promotion import

```bash
curl -X POST http://localhost:8081/v1/promotions/import \
  -H 'Content-Type: application/json' \
  -d '{
    "promotions": [
      {
        "id": "FLASH-LAMP-10",
        "sku": "SKU-BLUE-LAMP",
        "description": "Flash lamp promo",
        "type": "AMOUNT_OFF",
        "amountOff": 10.00,
        "percentOff": null,
        "minQuantity": 1,
        "customerSegment": "ALL",
        "channel": "ANY",
        "couponCode": null,
        "startsAt": "2025-01-01T00:00:00Z",
        "endsAt": "2030-01-01T00:00:00Z",
        "priority": 95
      }
    ]
  }'
```

## Testing

Run the full test suite from the repo root:

```bash
mvn test
```

## Observability

- Health: `GET /actuator/health`
- Prometheus: `GET /actuator/prometheus`
- Metrics to look for:
  - `pricing.quote.latency`
  - `pricing.cache.hit`
  - `pricing.cache.miss`
  - `pricing.cache.fallback`

## Interview Talking Points

- Compare Java records and service classes with Kotlin data classes and expression-heavy service code.
- Explain why both services keep the same HTTP contract while using different language idioms internally.
- Walk through the Redis fallback path and how it protects quote reads during dependency issues.
- Use the Grafana/Prometheus setup to talk about latency, cache hit rate, and production debugging.

