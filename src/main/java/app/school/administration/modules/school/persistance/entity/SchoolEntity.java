package app.school.administration.modules.school.persistance.entity;

import app.school.administration.common.infrastucture.persistence.entity.AuditableBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
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
@Table(name = "school_table", schema = "master",
        indexes = {@Index(name = "idx_school_table_active", columnList = "is_active")})
public class SchoolEntity extends AuditableBaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "school_id", nullable = false, updatable = false, unique = true)
    private UUID id;

    @NotBlank
    @Column(name = "school_name", nullable = false, unique = true)
    private String schoolName;

    protected SchoolEntity() {
    }

}
