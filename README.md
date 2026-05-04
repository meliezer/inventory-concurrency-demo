# Inventory Concurrency Demo

This project demonstrates a common inventory consistency problem: concurrent order requests can produce lost updates and overselling unless explicit concurrency control is applied.

## Scenario

- Initial stock: 10 units
- Concurrent requests: 20
- Order size: 1 unit per request

## What the project shows

The test suite compares three scenarios:

1. **No locking**
    - Demonstrates inconsistent final stock caused by lost updates.

2. **Pessimistic locking**
    - Uses database row locking to serialize concurrent access.
    - Ensures correctness, but introduces blocking under contention.

3. **Optimistic locking with retry**
    - Uses entity versioning and retries on conflicting updates.
    - Ensures correctness without blocking, but may increase retry cost under contention.

## Tech stack

- Java 21
- Spring Boot
- Spring Data JPA
- PostgreSQL
- JUnit 5

## Run PostgreSQL with Docker

```bash
docker run --name inventory-db \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=inventory \
  -p 5432:5432 \
  -d postgres:15
```

## Related DevSecOps Showcase

This project focuses on concurrency control at the application level.

A complementary project demonstrates how the same service can be integrated into a production-style DevSecOps workflow using GitLab CI/CD:

👉 https://gitlab.com/menashe.eliezer/inventory-concurrency-devsecops-showcase

The GitLab showcase includes:
- Full CI/CD pipeline (build, test, security, container)
- PostgreSQL-backed integration testing in CI
- AI-assisted merge request review with automated feedback
- Documented design decisions and trade-offs

Together, these projects illustrate both **correct concurrent backend design** and **modern DevSecOps practices**.
