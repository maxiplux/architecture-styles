## 2025-02-18 - [CRITICAL] Hardcoded Database Secrets
**Vulnerability:** The database password was hardcoded in `application.properties` as `spring.datasource.password=secret`. This exposes sensitive credentials in the source code.
**Learning:** Hardcoded credentials in source control create a high risk of credential leakage.
**Prevention:** Always use environment variable substitution for secrets (e.g., `${POSTGRES_PASSWORD:default}`).
