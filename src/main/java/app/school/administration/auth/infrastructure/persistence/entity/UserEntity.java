package app.school.administration.auth.infrastructure.persistence.entity;

import app.school.administration.auth.infrastructure.persistence.entity.mapping.UserRoleEntity;
import app.school.administration.common.infrastucture.persistence.entity.AuditableBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.annotations.Where;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "user_table", schema = "public",
        indexes = {
                @Index(name = "idx_users_username", columnList = "username"),
                @Index(name = "idx_user_email", columnList = "email"),
                @Index(name = "idx_user_active", columnList = "is_active")
        })
@DynamicUpdate
public class UserEntity extends AuditableBaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "user_id", nullable = false, updatable = false, unique = true)
    private UUID id;
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;
    @Column(name = "email", nullable = false, unique = true)
    private String email;
    @Column(name = "username", nullable = false, unique = true, length = 150)
    private String username;
    @Column(name = "password", nullable = false)
    private String password;
    @Column(name = "mobile_no", length = 15)
    private String mobileNo;
    @OneToMany(
            mappedBy = "user",
            fetch = FetchType.EAGER,
            cascade = jakarta.persistence.CascadeType.ALL,
            orphanRemoval = true
    )
    @Where(clause = "is_active = true")
    private Set<UserRoleEntity> roles = new HashSet<>();
    @OneToMany(
            mappedBy = "user",
            fetch = FetchType.EAGER
    )
    private Set<OAuthAccountEntity> oAuthAccounts = new HashSet<>();

    /**
     * To prevent usage of user objects in the code except JPA and this package
     * (protected will accessible only for the package level and the sub class in any package)
     */
    protected UserEntity() {
    }

    public UserEntity(String firstName, String lastName, String email, String username, String password, String mobileNo) {
        this.firstName = firstName;
        this.lastName = lastName != null ? lastName : "";
        this.email = email;
        this.username = username;
        this.password = password;
        this.mobileNo = mobileNo;
    }

    // Domain-safe methods
    public void assignRole(RoleEntity role) {
        roles.add(new UserRoleEntity(this, role));
    }

}
