package app.school.administration.common.config.flyway;

import org.flywaydb.core.Flyway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

/**
 * ====================================================================================
 * CONFIGURATION: MasterFlywayConfig
 * ====================================================================================
 *
 * <h2>FUNCTIONALITY</h2>
 * Configures and executes Flyway database migrations for the central `master` PostgreSQL database schema at application startup.
 *
 * <h2>WHY IMPLEMENTED</h2>
 * Separate Spring profiles (`dev`/`local` vs `prod`/`live`) require different Flyway migration policies:
 * - Development/Local: Auto-creates schemas, repairs checksum mismatches (`flyway.repair()`), and disables strict migration validation (`validateOnMigrate(false)`).
 * - Production: Enforces strict migration checksum validation (`validateOnMigrate(true)`) to prevent undetected SQL script modifications.
 *
 * <h2>WHAT HAPPENS IF NOT IMPLEMENTED</h2>
 * - Database migration scripts will not execute automatically on application startup.
 * - Master tables (`user_table`, `role_table`, `institution_table`, `school_table`) will not exist, causing fatal application startup exceptions.
 * ====================================================================================
 */
@Configuration
public class MasterFlywayConfig {

    /**
     * Configures Flyway for local development environment with automatic repair and lax validation.
     *
     * @param dataSource application pooled data source
     * @return configured Flyway instance for master schema
     */
    @Profile({"dev", "local"})
    @Bean
    Flyway masterFlyway(DataSource dataSource) {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations(
                    "classpath:db/migration/master",
                    "classpath:db/migration/tenant"
                )
                .schemas("master")
                .createSchemas(true)
                .baselineOnMigrate(true)
                .baselineVersion("1")
                .validateOnMigrate(false)
                .table("flyway_schema_history")
                .load();
        flyway.repair();
        flyway.migrate();
        return flyway;
    }

    /**
     * Configures Flyway for production environment with strict validation checks.
     *
     * @param dataSource application pooled data source
     * @return configured Flyway instance for master schema
     */
    @Profile({"prod", "live"})
    @Bean
    Flyway masterFlywayInProd(DataSource dataSource) {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations(
                    "classpath:db/migration/master",
                    "classpath:db/migration/tenant"
                )
                .schemas("master")
                .createSchemas(true)
                .baselineOnMigrate(true)
                .baselineVersion("1")
                .validateOnMigrate(true)
                .table("flyway_schema_history")
                .load();
        flyway.migrate();
        return flyway;
    }

}

