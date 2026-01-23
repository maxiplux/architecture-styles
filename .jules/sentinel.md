## 2025-11-25 - Hardcoded Secrets in Configuration
**Vulnerability:** Hardcoded database credentials (username/password) were found in `application.properties`.
**Learning:** Default configuration files often end up in production artifacts. Hardcoded secrets in these files are a high risk for data leakage.
**Prevention:** Use environment variables with default values (e.g., `${ENV_VAR:default}`) for local development convenience, but ensure production environments inject secure secrets.
