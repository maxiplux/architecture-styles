## 2025-05-24 - [CRITICAL] Hardcoded Database Credentials
**Vulnerability:** Found hardcoded `spring.datasource.password=secret` in `application.properties`.
**Learning:** Default configurations in version-controlled files often leak into production or unsafe environments.
**Prevention:** Use `${VAR_NAME:default}` syntax to allow environment variable overrides while keeping local defaults.
