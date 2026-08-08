package app.school.administration.modules.featureconfig.persistance.entity;

import app.school.administration.common.infrastucture.persistence.entity.AuditableBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Getter
@Setter
@Entity
@DynamicUpdate
@DynamicInsert
@Table(name = "feature_definition_table", schema = "public")
public class FeatureDefinitionEntity extends AuditableBaseEntity {

    @Id
    @Column(name = "feature_code", nullable = false)
    private String featureCode;

    @Column(name = "feature_name", nullable = false)
    private String featureName;

    @Column(name = "description")
    private String description;

    @Column(name = "default_enabled", nullable = false)
    private boolean defaultEnabled = true;

    protected FeatureDefinitionEntity() {
    }
}
