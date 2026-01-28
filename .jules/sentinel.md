## 2025-01-27 - Hardcoded Secrets & Test Isolation
**Vulnerability:** Hardcoded database credentials in `application.properties` and `compose.yaml`, and tests configured to use the main `application.properties` (potentially targeting production/dev DB).
**Learning:** Default project templates often include hardcoded secrets for convenience, which can leak into production. Missing test configuration forces tests to share the same environment as the application, increasing risk of data corruption or leakage.
**Prevention:** Use environment variable injection with defaults (e.g., `${VAR:default}`) for all sensitive configuration. Always configure a separate test database (e.g., H2) in `src/test/resources/application.properties` to ensure isolation.

## 2025-01-28 - Inventory Race Condition
**Vulnerability:** The `OrderService` checked stock levels and decremented them without optimistic or pessimistic locking. In a high-concurrency environment (e.g., two users buying the last item simultaneously), this would lead to a "Lost Update" anomaly and overselling.
**Learning:** `@Transactional` ensures atomicity but does not prevent concurrent transactions from reading the same initial state and overwriting each other's changes unless the isolation level is SERIALIZABLE (which hurts performance).
**Prevention:** Add a `@Version` field to critical entities (like `Product`) to enable JPA Optimistic Locking. This ensures that if the data changes between read and write, the transaction fails safely with `ObjectOptimisticLockingFailureException`.
