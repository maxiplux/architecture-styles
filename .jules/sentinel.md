## 2025-05-20 - Hardcoded Database Credentials & Test Isolation
**Vulnerability:** Hardcoded database password (`secret`) found in `application.properties` and `compose.yaml`.
**Learning:** The project used hardcoded values for convenience in local development but failed to support environment variable overrides, exposing the default secret in all environments. Additionally, tests relied on a live database, causing failures in restricted environments.
**Prevention:** Always use property injection with defaults (e.g., `${POSTGRES_PASSWORD:secret}`) in Spring Boot and Docker Compose. Configure tests to use an in-memory database (H2) to ensure they are isolated and environment-independent.
