package app.school.administration.auth.application.serviceimpl;

import app.school.administration.auth.domain.model.CustomUserDetails;
import app.school.administration.auth.infrastructure.persistence.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static app.school.administration.common.application.constant.CacheConstant.UNLESS_RESULT_IS_NULL;

/**
 * Loads authorities from the database (JWT proves identity; roles are not trusted from the
 * token alone for authorization). Evict {@link #evictByUsername(String)} after role
 * assignment so demotion/promotion takes effect on the next request.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsServiceImpl implements UserDetailsService {

    public static final String cacheName = "auth_cache";
    private final UserServiceImpl userService;

    @Override
    @Cacheable(value = cacheName, key = "#username", unless = UNLESS_RESULT_IS_NULL)
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userService.findByUsernameIgnoreCase(username);
        return new CustomUserDetails(user);
    }

    @CacheEvict(value = cacheName, key = "#username")
    public void evictByUsername(String username) {
        // annotation-driven eviction
    }
}
