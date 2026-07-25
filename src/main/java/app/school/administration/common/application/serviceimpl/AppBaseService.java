package app.school.administration.common.application.serviceimpl;

import app.school.administration.common.application.custom.exception.EntityNotFoundedException;
import app.school.administration.common.domain.repository.AppBaseRepository;
import app.school.administration.common.infrastucture.persistence.entity.AuditableBaseEntity;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * ====================================================================================
 * BASE SERVICE: AppBaseService
 * ====================================================================================
 *
 * <h2>FUNCTIONALITY</h2>
 * Generic abstract base service class providing reusable CRUD, activation toggle, dirty checking,
 * and Spring Data projection lookup operations for entities extending {@link AuditableBaseEntity}.
 *
 * <h2>WHY IMPLEMENTED</h2>
 * Design patterns and code efficiency:
 * - Eliminates repetitive CRUD boilerplate across domain services (`SchoolServiceImpl`, `InstitutionServiceImpl`).
 * - Enforces standardized exception handling (throwing {@link EntityNotFoundedException} on missing records).
 * - Enforces transaction management rules (`@Transactional(readOnly = true)` for reads, `@Transactional` for writes).
 *
 * <h2>WHAT HAPPENS IF NOT IMPLEMENTED</h2>
 * Every domain service implementation must write redundant database lookup, save, update, activate, and exception handling logic.
 * ====================================================================================
 *
 * @param <T> entity type extending {@link AuditableBaseEntity}
 * @param <UUID> entity primary key identifier type
 */
@Service
public abstract class AppBaseService<T extends AuditableBaseEntity, UUID> {

    /**
     * Subclasses implement this method to supply their specific JPA repository instance.
     *
     * @return concrete {@link AppBaseRepository} instance
     */
    protected abstract AppBaseRepository<T, UUID> getJpaRepository();

    /**
     * Finds an entity by ID within a read-only transaction.
     *
     * @param uuid primary key identifier
     * @return found entity instance
     * @throws EntityNotFoundedException if record does not exist
     */
    @Transactional(readOnly = true)
    public T appFindById(@Validated @NotNull UUID uuid) {
        return this.getJpaRepository().findById(uuid).orElseThrow(() -> new EntityNotFoundedException((java.util.UUID) uuid));
    }

    /**
     * Retrieves an entity projection by ID within a read-only transaction.
     *
     * @param uuid primary key identifier
     * @param projection target projection interface class
     * @param <P> projection type
     * @return projection object instance
     */
    @Transactional(readOnly = true)
    public <P> P appFindByIdProjection(@Validated @NotNull UUID uuid, Class<P> projection) {
        return getJpaRepository().findById(uuid, projection).orElseThrow(() -> new EntityNotFoundedException((java.util.UUID) uuid));
    }

    /**
     * Fetches an entity by ID for modification within an active transaction session.
     *
     * @param uuid primary key identifier
     * @return managed entity instance
     */
    public T appFindByIdWithDirtyCheck(@Validated @NotNull UUID uuid) {
        return this.getJpaRepository().findById(uuid).orElseThrow(() -> new EntityNotFoundedException((java.util.UUID) uuid));
    }

    /**
     * Saves a new entity record within a transactional context.
     *
     * @param t entity instance to save
     * @return saved entity instance
     */
    @Transactional
    public T appSave(@Validated @NotNull T t) {
        return this.getJpaRepository().save(t);
    }

    /**
     * Saves a collection of entity records in batch.
     *
     * @param t list of entities to save
     * @return list of saved entities
     */
    @Transactional
    public List<T> appSaveAll(@Validated @NotNull List<T> t) {
        return this.getJpaRepository().saveAll(t);
    }

    /**
     * Updates an existing entity record using property copying.
     *
     * @param t entity object containing updated properties
     * @param uuid target entity primary key ID
     * @return updated entity instance
     */
    @Transactional
    public T appUpdate(@Validated @NotNull T t, @NotNull UUID uuid) {
        T t1 = appFindByIdWithDirtyCheck(uuid);
        BeanUtils.copyProperties(t, t1);
        return t1;
    }

    /**
     * Deactivates an entity record (`is_active = false`).
     *
     * @param uuid target entity primary key ID
     * @return deactivated entity instance
     */
    @Transactional
    public T appDeActivate(@Validated @NotNull UUID uuid) {
        T t = appFindByIdWithDirtyCheck(uuid);
        t.setActive(false);
        return t;
    }

    /**
     * Activates an entity record (`is_active = true`).
     *
     * @param uuid target entity primary key ID
     * @return activated entity instance
     */
    @Transactional
    public T appActivate(@Validated @NotNull UUID uuid) {
        T t = appFindByIdWithDirtyCheck(uuid);
        t.setActive(true);
        return t;
    }

}

