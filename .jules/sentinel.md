## 2026-01-22 - [Fixed Hardcoded Secrets and Missing Test DB Config]
**Vulnerability:** Hardcoded database credentials (password/username) in `application.properties` and missing H2 configuration for tests.
**Learning:** Hardcoded credentials are a critical risk. The absence of test-specific configuration can mask environment issues or lead to tests attempting to connect to production databases.
**Prevention:** Always use environment variables for sensitive configuration with optional defaults. Ensure tests have an isolated environment (like H2) and explicit configuration (like disabling Docker Compose support) to avoid external dependency issues.
