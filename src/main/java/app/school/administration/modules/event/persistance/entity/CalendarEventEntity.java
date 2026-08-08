package app.school.administration.modules.event.persistance.entity;

import app.school.administration.common.infrastucture.persistence.entity.AuditableBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@DynamicUpdate
@DynamicInsert
@Table(name = "calendar_event_table", schema = "public",
        indexes = {
                @Index(name = "idx_event_scope_active", columnList = "scope, is_active"),
                @Index(name = "idx_event_institution", columnList = "institution_id, starts_at"),
                @Index(name = "idx_event_school", columnList = "school_id, starts_at")
        })
public class CalendarEventEntity extends AuditableBaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "event_id", nullable = false, updatable = false, unique = true)
    private UUID id;

    @NotBlank
    @Column(name = "scope", nullable = false, length = 20)
    private String scope;

    @NotNull
    @Column(name = "institution_id", nullable = false)
    private UUID institutionId;

    @Column(name = "school_id")
    private UUID schoolId;

    @NotBlank
    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "location")
    private String location;

    @NotNull
    @Column(name = "starts_at", nullable = false)
    private Instant startsAt;

    @Column(name = "ends_at")
    private Instant endsAt;

    @Column(name = "all_day", nullable = false)
    private boolean allDay;

    protected CalendarEventEntity() {
    }

    public CalendarEventEntity(String scope, UUID institutionId, UUID schoolId, String title) {
        this.scope = scope;
        this.institutionId = institutionId;
        this.schoolId = schoolId;
        this.title = title;
    }
}
