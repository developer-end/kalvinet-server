package app.school.administration.auth.infrastructure.persistence.entity;

import app.school.administration.common.infrastucture.persistence.entity.AuditableBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "role_table", schema = "public",
        indexes = {
                @Index(name = "idx_role_table_active", columnList = "is_active")
        })
@DynamicUpdate
@DynamicInsert
public class RoleEntity extends AuditableBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id", nullable = false, updatable = false, unique = true)
    private UUID id;
    @Column(name = "role_code", nullable = false, unique = true)
    private String roleCode;
    @Column(name = "role_name", nullable = false)
    private String roleName;
    @Column(name = "description")
    private String description;

    protected RoleEntity() {
    }

    public RoleEntity(String roleCode, String roleName, String description) {
        this.roleCode = roleCode;
        this.roleName = roleName;
        this.description = description;
    }

}
