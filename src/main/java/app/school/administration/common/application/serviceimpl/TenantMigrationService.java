package app.school.administration.common.application.serviceimpl;

import lombok.RequiredArgsConstructor;
import org.flywaydb.core.Flyway;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;

/**
 * ====================================================================================
 * SERVICE: TenantMigrationService
 * ====================================================================================
 *
 * <h2>FUNCTIONALITY</h2>
 * Programmatically triggers Flyway migrations for newly provisioned tenant database schemas in PostgreSQL.
 *
 * <h2>WHY IMPLEMENTED</h2>
 * When a new school or tenant registers in the system, its dedicated PostgreSQL database schema must be created
 * and populated with initial tables. `TenantMigrationService` runs Flyway programmatically against the target schema name.
 *
 * <h2>WHAT HAPPENS IF NOT IMPLEMENTED</h2>
 * New tenant onboarding will fail because newly created database schemas will remain empty without required database tables.
 * ====================================================================================
 */
@Service
@RequiredArgsConstructor
public class TenantMigrationService {

    private final DataSource dataSource;

    /**
     * Executes Flyway database migration scripts against a specific target schema.
     *
     * @param schema target PostgreSQL schema name to migrate
     */
    public void migrateTenant(String schema) {
        Flyway.configure()
                .dataSource(dataSource)
                .schemas(schema)
                .locations("classpath:db/migration/master")
                .baselineOnMigrate(true)
                .table("flyway_schema_history")
                .load()
                .migrate();
    }

}

