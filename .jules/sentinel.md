## 2026-01-18 - Hardcoded Secrets in Configuration
**Vulnerability:** Found hardcoded database credentials (`myuser`/`secret`) in `src/main/resources/application.properties`.
**Learning:** Hardcoded credentials in source control can lead to unauthorized access if the repository is leaked or accessed by unauthorized personnel. Even default/dev credentials should be overridable.
**Prevention:** Always use environment variable injection for sensitive configuration (e.g., `${DB_PASSWORD:default}`). This allows secrets to be managed by the deployment environment (e.g., K8s secrets, Vault) without changing the code.
