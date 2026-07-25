package app.school.administration.common.config.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.metamodel.EntityType;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * ====================================================================================
 * CONFIGURATION: CaffeineCacheConfig
 * ====================================================================================
 *
 * <h2>FUNCTIONALITY</h2>
 * Configures the Spring Cache Abstraction powered by Caffeine in-memory cache. Automatically derives cache names
 * from JPA entity metamodel names and explicit keys (`auth_cache`, `roles_cache`), enforcing a 1-hour write expiration
 * and maximum capacity of 1000 items per cache.
 *
 * <h2>WHY IMPLEMENTED</h2>
 * Performance optimization:
 * - Frequently queried metadata (user security principals, roles, institution & school settings) are cached in memory.
 * - Prevents repetitive database queries during peak multi-tenant request volume.
 *
 * <h2>WHAT HAPPENS IF NOT IMPLEMENTED</h2>
 * - High database load and slow response times under heavy user traffic due to repeated SQL queries.
 * - `@Cacheable` and `@CacheEvict` annotations across services will fail to operate.
 * ====================================================================================
 */
@Configuration
@EnableCaching
public class CaffeineCacheConfig {

    private static final String cacheSuffix = "Cache";
    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Converts CamelCase entity names into snake_case cache name format with a `_cache` suffix.
     *
     * @param entityNameList list of entity names
     */
    public static void addSuffixAfter(List<String> entityNameList) {
        entityNameList.replaceAll(entityName -> convertStringFromCamelToSnake(entityName + cacheSuffix));
    }

    /**
     * Utility method converting CamelCase strings to snake_case.
     *
     * @param entityName camelCase string
     * @return snake_case string
     */
    public static String convertStringFromCamelToSnake(String entityName) {
        return entityName.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    /**
     * Inspects JPA metamodel entity names to dynamically generate registered cache names.
     *
     * @return list of snake_case cache names
     */
    public List<String> getEntityNames() {
        List<String> stringList = entityManager.getMetamodel().getEntities().stream().map(EntityType::getName).collect(Collectors.toList());
        stringList.add("Auth");
        stringList.add("Roles");
        addSuffixAfter(stringList);
        return stringList;
    }

    /**
     * Instantiates the primary {@link CacheManager} using Caffeine cache configuration settings.
     *
     * @return CaffeineCacheManager bean
     */
    @Bean
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();
        caffeineCacheManager.setCacheNames(getEntityNames());
        caffeineCacheManager.setCaffeine(Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).maximumSize(1000).recordStats());
        return caffeineCacheManager;
    }

    /**
     * Clears all registered Caffeine in-memory caches.
     */
    public void clearCaffeineCaches() {
        getEntityNames().forEach(this::clearCaffeineCacheByName);
    }

    /**
     * Evicts all entries from a specific cache by name.
     *
     * @param name target cache name
     */
    @CacheEvict(value = "#name", allEntries = true, condition = "#name != null")
    public void clearCaffeineCacheByName(String name) {

    }

}

