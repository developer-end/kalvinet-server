package app.school.administration.modules.featureconfig.persistance.entity;

import app.school.administration.common.infrastucture.persistence.entity.AuditableBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Getter
@Setter
@Entity
@DynamicUpdate
@DynamicInsert
@Table(name = "feature_role_grant_table", schema = "public")
public class FeatureRoleGrantEntity extends AuditableBaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "grant_id", nullable = false, updatable = false, unique = true)
    private UUID id;

    @Column(name = "config_id", nullable = false)
    private UUID configId;

    @Column(name = "role_code", nullable = false)
    private String roleCode;

    protected FeatureRoleGrantEntity() {
    }

    public FeatureRoleGrantEntity(UUID configId, String roleCode) {
        this.configId = configId;
        this.roleCode = roleCode;
    }
}
