package app.school.administration.auth.application.serviceimpl;

import app.school.administration.auth.domain.model.CustomUserDetails;
import app.school.administration.auth.infrastructure.persistence.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static app.school.administration.common.application.constant.CacheConstant.UNLESS_RESULT_IS_NULL;

/**
 * ====================================================================================
 * SERVICE: CustomUserDetailsServiceImpl
 * ====================================================================================
 *
 * <h2>FUNCTIONALITY</h2>
 * Implements Spring Security's {@link UserDetailsService} interface to load user credentials and granted authorities
 * from the database, wrapped with Caffeine caching (`auth_cache`).
 *
 * <h2>WHY IMPLEMENTED</h2>
 * Integrates application user entities with Spring Security authentication:
 * - {@code @Cacheable(value = "auth_cache", key = "#username")}: Caches user details in memory so every incoming request filter does not query PostgreSQL.
 * - Converts domain {@link UserEntity} into {@link CustomUserDetails}.
 *
 * <h2>WHAT HAPPENS IF NOT IMPLEMENTED</h2>
 * Spring Security's authentication manager will fail to load user principals during JWT filter processing, causing HTTP 500 or 401 exceptions.
 * ====================================================================================
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsServiceImpl implements UserDetailsService {

    public static final String cacheName = "auth_cache";
    private final UserServiceImpl userService;

    /**
     * Loads user security principal by username from database or Caffeine cache.
     *
     * @param username user login username
     * @return UserDetails principal
     * @throws UsernameNotFoundException if user record does not exist
     */
    @Override
    @Cacheable(value = cacheName, key = "#username", unless = UNLESS_RESULT_IS_NULL)
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userService.findByUsernameIgnoreCase(username);
        return new CustomUserDetails(user);
    }

}

