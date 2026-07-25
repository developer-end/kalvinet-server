package app.school.administration.common.infrastucture.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * ====================================================================================
 * BASE ENTITY: AuditableBaseEntity
 * ====================================================================================
 *
 * <h2>FUNCTIONALITY</h2>
 * Abstract JPA superclass providing common audit fields (`created_at`, `updated_at`),
 * active status flag (`is_active`), and optimistic locking version counter (`version`).
 *
 * <h2>WHY IMPLEMENTED</h2>
 * Standardization and concurrency safety:
 * - {@link Version}: Enforces optimistic locking in JPA. Prevents silent data overwrites when two concurrent HTTP requests update the same row.
 * - {@link CreatedDate} / {@link LastModifiedDate}: Managed automatically by {@link AuditingEntityListener}.
 * - {@link PrePersist}: Ensures newly saved entities default to `active = true`.
 *
 * <h2>WHAT HAPPENS IF NOT IMPLEMENTED</h2>
 * - Audit fields would have to be duplicated across every entity class.
 * - Without optimistic locking, concurrent database updates will cause dirty writes and data loss.
 * ====================================================================================
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class AuditableBaseEntity {

    /**
     * Optimistic lock version counter incremented automatically by Hibernate on update.
     */
    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    /**
     * Status flag indicating whether the entity is active or deactivated.
     */
    @Column(name = "is_active", nullable = false)
    private boolean active;

    /**
     * Timestamp recording when the entity record was created.
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Timestamp recording when the entity record was last modified.
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * JPA lifecycle callback ensuring default active status before insert.
     */
    @PrePersist
    protected void setActive() {
        if (!this.active) {
            this.active = true;
        }
    }

    public void setActive(boolean active) {
        this.active = active;
    }

}

