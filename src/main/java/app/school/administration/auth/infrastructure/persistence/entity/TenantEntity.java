package app.school.administration.auth.infrastructure.persistence.entity;

import app.school.administration.auth.infrastructure.persistence.entity.embeddable.TenantId;
import app.school.administration.common.infrastucture.persistence.entity.AuditableBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Where;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@IdClass(TenantId.class)
@Table(name = "tenant_table", schema = "public",
        indexes = {
                @Index(name = "idx_tenant_table_active", columnList = "is_active"),
                @Index(name = "idx_tenant_table_active_opened_closed", columnList = "is_active, opened_date, closed_date")
        })
@Where(clause = "is_active = true")
@DynamicUpdate
public class TenantEntity extends AuditableBaseEntity {

    @Id
    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID id;

    @Id
    @Column(name = "opened_date", nullable = false, updatable = false)
    private Instant openedDate;

    @NotBlank
    @Column(name = "tenant_name", nullable = false, unique = true)
    private String tenantName;

    @Column(name = "closed_date", nullable = false)
    private Instant closedDate;

    @Column(name = "description")
    private String description;

    public TenantEntity() {
    }

}
