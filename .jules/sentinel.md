## 2025-01-27 - Hardcoded Secrets & Test Isolation
**Vulnerability:** Hardcoded database credentials in `application.properties` and `compose.yaml`, and tests configured to use the main `application.properties` (potentially targeting production/dev DB).
**Learning:** Default project templates often include hardcoded secrets for convenience, which can leak into production. Missing test configuration forces tests to share the same environment as the application, increasing risk of data corruption or leakage.
**Prevention:** Use environment variable injection with defaults (e.g., `${VAR:default}`) for all sensitive configuration. Always configure a separate test database (e.g., H2) in `src/test/resources/application.properties` to ensure isolation.

## 2025-01-31 - Missing HTTP Security Headers
**Vulnerability:** The application was missing standard HTTP security headers (X-Frame-Options, X-Content-Type-Options, CSP, etc.), making it vulnerable to Clickjacking, MIME sniffing, and XSS attacks.
**Learning:** Spring Boot (without Spring Security) does not include these headers by default. A custom Filter is required to enforce them.
**Prevention:** Implement a `OncePerRequestFilter` to explicitly set `X-Frame-Options`, `X-Content-Type-Options`, `Content-Security-Policy`, and strict `Cache-Control` headers for all responses.
