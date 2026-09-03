package com.project.tasktracker;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Base class for PostgreSQL-backed integration tests.
 *
 * <p>Uses the local PostgreSQL instance (running in Docker on port 5555)
 * that is already used for the main application.  This avoids requiring
 * Testcontainers to start an additional container, which can fail on
 * Windows with Docker Desktop 29.x due to Docker API version negotiation.
 *
 * <p>Tests must be self-contained: each test cleans up its own data in
 * {@code @BeforeEach} so that tests are isolated despite sharing a database.
 *
 * <p>The datasource properties are supplied via {@link DynamicPropertySource}
 * so that they override the application default (which also connects to the
 * same local Postgres instance).  The ddl-auto is set to {@code update} so
 * that the schema is preserved and new columns are added safely.
 *
 * <p>Integration tests verify actual PostgreSQL + Hibernate behaviour against
 * the same Postgres 15 image used by the application.
 */
@SpringBootTest
public abstract class AbstractIntegrationTest {

    /**
     * Connect to the local Docker PostgreSQL instance.
     * Uses the same credentials as the application default.
     * Tests rely on {@code @BeforeEach} cleanup to ensure isolation.
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",
                () -> "jdbc:postgresql://localhost:5555/tasktracker_db");
        registry.add("spring.datasource.username", () -> "root");
        registry.add("spring.datasource.password", () -> "password");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }
}
