# Java vs Kotlin Comparison Guide

This file maps the main Java implementation files to their Kotlin counterparts and highlights the interview-relevant differences.

## Entry Point

- Java: `pricing-service-java/src/main/java/com/example/retail/javaapp/PricingServiceJavaApplication.java`
- Kotlin: `pricing-service-kotlin/src/main/kotlin/com/example/retail/kotlinapp/PricingServiceKotlinApplication.kt`

What to call out:

- Java uses an explicit `main` class and `SpringApplication.run(...)`.
- Kotlin uses a compact top-level `main` function with `runApplication`.

## Controllers

- Java: `controller/PriceQuoteController.java`, `controller/PromotionAdminController.java`
- Kotlin: `controller/PriceQuoteController.kt`, `controller/PromotionAdminController.kt`

What to call out:

- Java uses explicit constructors and block-bodied methods.
- Kotlin uses primary constructor injection and single-expression controller methods.

## Cache Layer

- Java: `cache/RedisBackedQuoteCache.java`, `cache/QuoteComputation.java`, `cache/CacheKey.java`
- Kotlin: `cache/RedisBackedQuoteCache.kt`, `cache/QuoteComputation.kt`, `cache/CacheKey.kt`

What to call out:

- Java uses a `record` for `QuoteComputation`; Kotlin uses a `data class`.
- Java returns `Optional`; Kotlin returns nullable values and uses `?.let { ... }`.
- Kotlin keeps utility behavior in an `object`; Java uses a `final` utility class.

## Service Logic

- Java: `service/PriceQuoteService.java`, `service/PriceQuoteCalculator.java`
- Kotlin: `service/PriceQuoteService.kt`, `service/PriceQuoteCalculator.kt`

What to call out:

- Java leans on streams, helper methods, and explicit types.
- Kotlin compresses branching with `when`, `fold`, `distinctBy`, and expression-bodied helpers.
- Kotlin null handling is more concise, but Java interop with shared record types still requires method-style access like `request.sku()`.

## Repositories

- Java: `repository/JdbcProductCatalogRepository.java`, `repository/JdbcPromotionRepository.java`
- Kotlin: `repository/JdbcProductCatalogRepository.kt`, `repository/JdbcPromotionRepository.kt`

What to call out:

- Java uses verbose row-mapper lambdas and explicit interfaces.
- Kotlin reduces ceremony around collection handling and `firstOrNull()`.

## Error Handling

- Java: `error/ApiExceptionHandler.java`, `error/ProductNotFoundException.java`
- Kotlin: `error/ApiExceptionHandler.kt`, `error/ProductNotFoundException.kt`

What to call out:

- Kotlin’s compact exception class and map building are noticeably shorter.
- Java is more explicit and sometimes easier to scan for teams that prefer rigid structure.

## Tests

- Java: `src/test/java/.../PriceQuoteCalculatorTest.java`, `PriceQuoteControllerIT.java`
- Kotlin: `src/test/kotlin/.../PriceQuoteCalculatorTest.kt`, `PriceQuoteControllerIT.kt`

What to call out:

- Java tests use classic JUnit + AssertJ + MockMvc patterns.
- Kotlin tests use JUnit with Kotlin-friendly syntax, multiline JSON strings, and the Spring MockMvc Kotlin DSL.

## Good Interview Narrative

Use this repo to explain:

1. The HTTP contract is intentionally identical so behavior comparison stays fair.
2. The Kotlin version is shorter, but the Java version may feel more familiar in mixed enterprise teams.
3. The shared contract module mirrors a real codebase where multiple services depend on stable API types.
4. You understand not just syntax differences, but operational behavior: caching, observability, validation, and promotion precedence.

