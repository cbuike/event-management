# Event Categories - Spring Boot

This project implements a **REST API** to manage hierarchical event categories (infinite-depth tree).

## Tech Stack
- Java 17
- Docker
- Maven
- Spring Boot 3.1.5
- PostgreSQL 14
- JPA / Hibernate
- OpenAPI (Swagger UI)
- Unit Tests
- JaCoCo for coverage reports

## How to Run

1. Start the PostgreSQL database using Docker (run inside project directory):
   ```bash
   docker compose up -d
2. Run: `mvn clean install` to package and run test cases.
3. Run: `mvn spring-boot:run` or `java -jar target/event-management-0.0.1-SNAPSHOT.jar` to start the application.
4. Access API documentation (Swagger UI) at: http://localhost:8082/swagger-ui.html.
5. Goto `target/site/jacoco/index.html` for test coverage details.

### OPERATIONAL FLOW CHART [TD]

```mermaid
flowchart TD
    %% Create Category Flow
    A[Client Request] -->|"POST /api/v1/categories"| B(Add Controller)
    B --> C(Service: validate parent)
    C --> D(Repo: save Category)
    D --> E[Return 201 Created with Category Details]

    %% Fetch Subtree Flow
    F[Client Request] -->|"GET /api/v1/categories/{parentId}/subtree"| G(Fetch Controller)
    G --> H(Service: fetch subtree)
    H --> I(Repo: recursive CTE to get subtree)
    I --> J[Return subtree JSON with 200 OK Status]

    %% Move Category Flow
    U[Client Request] -->|"PUT /api/v1/categories/{subtreeId}/move"| V(Move Controller)
    V --> W(Service: validate src & target, check cycle)
    W --> X(Repo: update the parent_id of the the subtree)
    X --> Y[Return 200 OK]

    %% Delete Category Flow
    O[Client Request] -->|"DELETE /api/v1/categories/{categoryId}"| P(Delete Controller)
    P --> R(Service: check exists & delete subtree)
    R --> S(Repo: delete cascade or delete rows by ids)
    S --> T[Return 204 No Content]
```
