package app.school.administration.auth.infrastructure.persistence.entity;

import app.school.administration.auth.application.constant.AuthProvider;
import app.school.administration.common.infrastucture.persistence.entity.AuditableBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.annotations.Where;

import java.util.UUID;

@Entity
@Table(
        name = "oauth_accounts", schema = "public",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_provider_user",
                        columnNames = {"provider", "provider_user_id"}
                ),
                @UniqueConstraint(
                        name = "uq_provider_user_id",
                        columnNames = {"provider", "user_id"}
                )
        },
        indexes = {
                @Index(name = "idx_oauth_user", columnList = "user_id"),
                @Index(name = "idx_oauth_provider_active", columnList = "is_active")
        }
)
@Getter
@Setter
@AllArgsConstructor
@Builder
@DynamicUpdate
@DynamicInsert
@Where(clause = "is_active = true")
public class OAuthAccountEntity extends AuditableBaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "oauth_id", nullable = false, updatable = false, unique = true)
    private UUID id;
    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false)
    private AuthProvider provider;
    @Column(name = "provider_user_id", nullable = false)
    private String providerUserId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    protected OAuthAccountEntity() {
    }

}

