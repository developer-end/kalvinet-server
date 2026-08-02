package app.school.administration.common.application.serviceimpl;

import app.school.administration.auth.infrastructure.persistence.entity.TenantEntity;
import app.school.administration.auth.infrastructure.persistence.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * ====================================================================================
 * SERVICE: TenantMigrationService
 * ====================================================================================
 *
 * <h2>FUNCTIONALITY</h2>
 * Programmatically triggers Flyway migrations for newly provisioned tenant database schemas in PostgreSQL.
 * Automatically creates the PostgreSQL schema if missing, runs scripts from 'classpath:db/migration/tenant',
 * and inserts a management record into 'public.tenant_table' if not already registered.
 *
 * <h2>WHY IMPLEMENTED</h2>
 * When a new tenant (e.g. 'tenant2026sep', 'tenant_school_01') is created via API or requested,
 * its PostgreSQL database schema must be created, migrated, and registered in public catalog.
 * ====================================================================================
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TenantMigrationService {

    private final DataSource dataSource;
    private final TenantRepository tenantRepository;

    /**
     * Executes Flyway database migration scripts against a specific target schema (e.g., "tenant2026sep").
     * If the schema is missing in PostgreSQL, Flyway creates it automatically (.createSchemas(true)) and
     * runs all scripts in 'classpath:db/migration/tenant'.
     * Additionally, if the schema is not yet registered in public.tenant_table, an entry is automatically created.
     *
     * @param schema target PostgreSQL schema name to migrate
     */
    @Transactional
    public void migrateTenant(String schema) {
        if (schema == null || schema.isBlank() || "public".equalsIgnoreCase(schema) || "master".equalsIgnoreCase(schema)) {
            return;
        }

        log.info("Provisioning & running Flyway migration for tenant schema: {}", schema);
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .schemas(schema)
                .locations("classpath:db/migration/tenant")
                .createSchemas(true)
                .baselineOnMigrate(true)
                .table("flyway_schema_history")
                .load();
        flyway.repair();
        flyway.migrate();

        // Ensure tenant entry exists in public.tenant_table for efficient catalog management
        ensureTenantCatalogEntryExists(schema);
    }

    private void ensureTenantCatalogEntryExists(String schema) {
        try {
            if (!tenantRepository.existsByTenantName(schema)) {
                log.info("Creating automatic management record entry in public.tenant_table for schema: {}", schema);
                TenantEntity tenantEntity = new TenantEntity();
                tenantEntity.setId(UUID.randomUUID());
                tenantEntity.setTenantName(schema);
                tenantEntity.setOpenedDate(Instant.now());
                tenantEntity.setClosedDate(Instant.now().plus(365, ChronoUnit.DAYS));
                tenantEntity.setDescription("Auto-registered tenant schema entry: " + schema);
                tenantEntity.setActive(true);

                tenantRepository.save(tenantEntity);
                log.info("Successfully registered tenant schema {} in public.tenant_table", schema);
            }
        } catch (Exception e) {
            log.error("Failed to insert tenant catalog entry for schema: {}. Error: {}", schema, e.getMessage());
        }
    }

}

