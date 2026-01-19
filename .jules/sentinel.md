## 2026-01-19 - Hardcoded Secrets in Configuration
**Vulnerability:** Found plaintext database password `secret` hardcoded in `src/main/resources/application.properties`.
**Learning:** Developers often hardcode credentials for local development convenience, creating a risk of exposure in source control and potential use of weak defaults in production.
**Prevention:** Always use environment variable injection for sensitive configuration (e.g., `${POSTGRES_PASSWORD:default}`). This allows safe overrides in different environments without modifying the codebase.
