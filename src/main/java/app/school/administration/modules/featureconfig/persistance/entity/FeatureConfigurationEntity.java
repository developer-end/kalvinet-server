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
@Table(name = "feature_configuration_table", schema = "public")
public class FeatureConfigurationEntity extends AuditableBaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "config_id", nullable = false, updatable = false, unique = true)
    private UUID id;

    @Column(name = "feature_code", nullable = false)
    private String featureCode;

    @Column(name = "scope", nullable = false, length = 20)
    private String scope;

    @Column(name = "institution_id", nullable = false)
    private UUID institutionId;

    @Column(name = "school_id")
    private UUID schoolId;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    public FeatureConfigurationEntity() {
    }
}
