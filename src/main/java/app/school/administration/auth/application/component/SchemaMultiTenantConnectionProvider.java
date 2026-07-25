package app.school.administration.auth.application.component;

import lombok.RequiredArgsConstructor;
import org.hibernate.engine.jdbc.connections.spi.AbstractDataSourceBasedMultiTenantConnectionProviderImpl;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * ====================================================================================
 * COMPONENT: SchemaMultiTenantConnectionProvider
 * ====================================================================================
 *
 * <h2>FUNCTIONALITY</h2>
 * Provides database connection management for Hibernate's schema-based multi-tenancy implementation.
 * Integrates directly with the Spring-managed {@link DataSource} to provide database connections
 * when Hibernate executes SQL queries.
 *
 * <h2>WHY IMPLEMENTED</h2>
 * In PostgreSQL schema-based multi-tenancy (`spring.jpa.properties.hibernate.multiTenancy: SCHEMA`),
 * Hibernate needs a connection provider component to obtain database connections dynamically.
 * By extending {@link AbstractDataSourceBasedMultiTenantConnectionProviderImpl}, this bean supplies
 * the shared pooled database DataSource to Hibernate while schema switching is handled via SQL search_path
 * or tenant identifier resolution.
 *
 * <h2>WHAT HAPPENS IF NOT IMPLEMENTED</h2>
 * Hibernate will throw a `MultiTenancyStrategyException` or `Cannot get connection` error at application
 * startup because no `MultiTenantConnectionProvider` bean is registered to supply database connections.
 * ====================================================================================
 */
@Component
@RequiredArgsConstructor
public class SchemaMultiTenantConnectionProvider extends AbstractDataSourceBasedMultiTenantConnectionProviderImpl {

    private final DataSource dataSource;

    /**
     * Selects any available DataSource for un-tenanted operations (such as system startup checks).
     *
     * @return default application DataSource connection pool
     */
    @Override
    protected DataSource selectAnyDataSource() {
        return dataSource;
    }

    /**
     * Selects the DataSource connection pool for a specific tenant identifier.
     *
     * @param tenantIdentifier target tenant schema identifier
     * @return application DataSource connection pool
     */
    @Override
    protected DataSource selectDataSource(Object tenantIdentifier) {
        return dataSource;
    }

}

