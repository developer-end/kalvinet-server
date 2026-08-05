package app.school.administration.auth.infrastructure.persistence.repository;

import app.school.administration.auth.infrastructure.persistence.entity.UserEntity;
import app.school.administration.common.domain.repository.AppBaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends AppBaseRepository<UserEntity, UUID> {

    @Transactional(readOnly = true)
    Optional<UserEntity> findByUsernameIgnoreCase(String username);

    @Transactional(readOnly = true)
    Optional<UserEntity> findByUsernameAndPassword(String username, String password);

    @Transactional(readOnly = true)
    boolean existsByUsernameIgnoreCase(String username);

    @Transactional(readOnly = true)
    boolean existsByEmailIgnoreCase(String email);

    @Transactional(readOnly = true)
    @Query("""
            SELECT u FROM UserEntity u
            WHERE (:q IS NULL OR :q = ''
                OR lower(u.username) LIKE lower(concat('%', :q, '%'))
                OR lower(u.email) LIKE lower(concat('%', :q, '%'))
                OR lower(u.firstName) LIKE lower(concat('%', :q, '%'))
                OR lower(u.lastName) LIKE lower(concat('%', :q, '%')))
            """)
    Page<UserEntity> search(@Param("q") String q, Pageable pageable);
}
