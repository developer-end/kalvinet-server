package app.school.administration.modules.institution.persistance.entity;

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
@Table(name = "institution_table", schema = "public",
        indexes = {@Index(name = "idx_institution_table_active", columnList = "is_active")})
public class InstitutionEntity extends AuditableBaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "institution_id", nullable = false, updatable = false, unique = true)
    private UUID id;

    /** Enforces a single catalog row (always 1). */
    @Column(name = "singleton_key", nullable = false, unique = true)
    private short singletonKey = 1;

    @NotBlank
    @Column(name = "institution_name", nullable = false)
    private String institutionName;

    @Column(name = "legal_name")
    private String legalName;

    @Column(name = "registration_number")
    private String registrationNumber;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "address_line1")
    private String addressLine1;

    @Column(name = "address_line2")
    private String addressLine2;

    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;

    @Column(name = "country")
    private String country;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "website")
    private String website;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "description")
    private String description;

    protected InstitutionEntity() {
    }
}
