package app.school.administration.common.application.component;

import app.school.administration.auth.application.constant.AuthConstant;
import app.school.administration.auth.application.serviceimpl.CustomUserDetailsServiceImpl;
import app.school.administration.auth.domain.model.CustomUserDetails;
import app.school.administration.common.application.custom.exception.InvalidTokenException;
import app.school.administration.common.application.serviceimpl.AppContextService;
import app.school.administration.common.application.serviceimpl.JWTService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * ====================================================================================
 * FILTER: JWTAuthFilter
 * ====================================================================================
 *
 * <h2>FUNCTIONALITY</h2>
 * A Spring HTTP filter extending {@link OncePerRequestFilter} that intercepts incoming HTTP requests,
 * extracts the JWT Bearer token from headers, validates token integrity/expiration via {@link JWTService},
 * loads {@link CustomUserDetails}, populates Spring's {@link SecurityContextHolder}, and initializes application contexts
 * via {@link AppContextService}.
 *
 * <h2>WHY IMPLEMENTED</h2>
 * Essential for stateless JWT authentication:
 * - Intercepts requests once per request cycle (`OncePerRequestFilter`).
 * - Excludes public endpoints automatically via {@link #shouldNotFilter(HttpServletRequest)}.
 * - Hydrates both Spring Security contexts and application-specific thread contexts (`UserContext`, `TenantContext`).
 *
 * <h2>WHAT HAPPENS IF NOT IMPLEMENTED</h2>
 * - Authenticated requests will lack identity context, causing all secured API endpoints to fail authorization (HTTP 401).
 * - Multi-tenant schema routing cannot identify the user's active tenant.
 * ====================================================================================
 */
@Component
@RequiredArgsConstructor
public class JWTAuthFilter extends OncePerRequestFilter {

    private final JWTService jwtService;
    private final CustomUserDetailsServiceImpl userDetailsService;
    private final AppContextService appContextService;

    /**
     * Determines whether the filter should be bypassed for public routes.
     *
     * @param request current HTTP servlet request
     * @return true if request URI matches a public endpoint prefix in {@link AuthConstant#PUBLIC_ENDPOINTS}
     */
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        return AuthConstant.PUBLIC_ENDPOINTS.stream().anyMatch(requestURI::startsWith);
    }

    /**
     * Core filter execution method processing JWT validation and context hydration.
     *
     * @param request current HTTP servlet request
     * @param response current HTTP servlet response
     * @param filterChain servlet filter chain
     * @throws ServletException if a servlet processing error occurs
     * @throws IOException if an I/O exception occurs
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        final String ACCESS_TOKEN = jwtService.tokenResolver(request);

        if (ACCESS_TOKEN == null) {
            filterChain.doFilter(request, response);
            return;
        }
        String username = jwtService.extractAllClaims(ACCESS_TOKEN).getSubject();

        try {
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                CustomUserDetails customUserDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(username);
                if (jwtService.isTokenValid(ACCESS_TOKEN)) {
                    var authToken = new UsernamePasswordAuthenticationToken(
                            customUserDetails, null, customUserDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    appContextService.createAuthContexts(customUserDetails);
                } else {
                    throw new InvalidTokenException();
                }
            }
        } catch (JwtException e) {
            SecurityContextHolder.clearContext();
            appContextService.clearAuthContext();
            throw new BadCredentialsException("Invalid token");
        }

        filterChain.doFilter(request, response);
    }
}

