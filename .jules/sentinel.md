## 2026-01-20 - Hardcoded Database Secrets
**Vulnerability:** Hardcoded database password `secret` found in `application.properties`.
**Learning:** Hardcoding secrets prevents secure configuration management and increases risk of exposure.
**Prevention:** Use `${ENV_VAR:default}` syntax to allow environment variable overrides while maintaining local development convenience.
