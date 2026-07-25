package app.school.administration.common.config.security;

import app.school.administration.auth.application.constant.AuthConstant;
import app.school.administration.auth.application.serviceimpl.CustomUserDetailsServiceImpl;
import app.school.administration.common.application.component.JWTAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.stream.Collectors;

/**
 * ====================================================================================
 * CONFIGURATION: SecurityConfig
 * ====================================================================================
 *
 * <h2>FUNCTIONALITY</h2>
 * Configures the Spring Security filter chain, stateless authentication policy, public/private route authorizations,
 * JWT filter integration, method-level security (`@PreAuthorize`), and password encoding standards.
 *
 * <h2>WHY IMPLEMENTED</h2>
 * Essential for securing the application APIs:
 * - Stateless Session Policy: Ensures no HTTP session state is stored on the server (ideal for REST APIs).
 * - Public Endpoint Exemption: Permits unauthenticated access to authentication routes defined in {@link AuthConstant#PUBLIC_ENDPOINTS}.
 * - JWT Filter Binding: Registers {@link JWTAuthFilter} before {@link UsernamePasswordAuthenticationFilter} to process Bearer tokens.
 * - Password Hashing: Uses {@link BCryptPasswordEncoder} to hash passwords securely.
 *
 * <h2>WHAT HAPPENS IF NOT IMPLEMENTED</h2>
 * - APIs will either be completely unsecured (exposing private data) or blocked by Spring Security defaults (HTTP 401/403).
 * - Passwords would be checked in plain text, presenting a critical security vulnerability.
 * ====================================================================================
 */
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JWTAuthFilter jwtAuthFilter;
    private final CustomUserDetailsServiceImpl customUserDetailsServiceImpl;

    /**
     * Builds and registers the primary Spring {@link SecurityFilterChain}.
     *
     * @param httpSecurity Spring HttpSecurity builder object
     * @return constructed SecurityFilterChain bean
     * @throws Exception if security configuration encounters an error
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(httpSecuritySessionManagementConfigurer ->
                        httpSecuritySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorizationManagerRequestMatcherRegistry ->
                        authorizationManagerRequestMatcherRegistry
                                .requestMatchers(AuthConstant.PUBLIC_ENDPOINTS.stream()
                                        .map(m -> m.concat("/**"))
                                        .collect(Collectors.toSet()).toArray(new String[0])
                                ).permitAll()
                                .anyRequest().authenticated())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .userDetailsService(customUserDetailsServiceImpl)
                .httpBasic(Customizer.withDefaults());
        return httpSecurity.build();
    }

    /**
     * Configures the application-wide password encoder using BCrypt strong hashing.
     *
     * @return PasswordEncoder instance using BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Exposes Spring Security's {@link AuthenticationManager} bean for authentication processing.
     *
     * @param config Spring AuthenticationConfiguration instance
     * @return AuthenticationManager instance
     * @throws Exception if authentication manager cannot be retrieved
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

}

