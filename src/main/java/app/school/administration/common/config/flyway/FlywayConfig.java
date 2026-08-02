package app.school.administration.common.config.flyway;

import app.school.administration.auth.application.context.TenantContext;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class FlywayConfig {

    @Bean
    public DataSource dataSource(DataSourceProperties props) {
        String defaultPaths = "public";
        String tenant = TenantContext.getTenant();

        String searchPath = (tenant == null || tenant.isBlank())
                ? defaultPaths
                : tenant + "," + defaultPaths;

        HikariDataSource ds = props
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();

        ds.setConnectionInitSql("SET search_path TO " + searchPath);
        return ds;
    }

}
