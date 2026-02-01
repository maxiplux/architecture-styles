## 2025-01-27 - Hardcoded Secrets & Test Isolation
**Vulnerability:** Hardcoded database credentials in `application.properties` and `compose.yaml`, and tests configured to use the main `application.properties` (potentially targeting production/dev DB).
**Learning:** Default project templates often include hardcoded secrets for convenience, which can leak into production. Missing test configuration forces tests to share the same environment as the application, increasing risk of data corruption or leakage.
**Prevention:** Use environment variable injection with defaults (e.g., `${VAR:default}`) for all sensitive configuration. Always configure a separate test database (e.g., H2) in `src/test/resources/application.properties` to ensure isolation.

## 2026-02-01 - Input Validation Bypass in Controllers
**Vulnerability:** `ProductController` manually mapped request parameters to variables instead of using a validated DTO, allowing unvalidated input (e.g., excessively long strings, invalid price ranges) to reach the service layer.
**Learning:** Using `@RequestParam` for complex filters often leads to missing validation because `@Valid` on the method parameters requires additional setup or explicit annotations for each param. DTOs are safer.
**Prevention:** Always bundle search filters into a DTO and use `@ParameterObject`, `@ModelAttribute`, and `@Valid` to enforce constraints centrally.
