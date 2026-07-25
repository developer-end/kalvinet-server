package app.school.administration.common.application.serviceimpl;

import app.school.administration.auth.infrastructure.persistence.entity.RoleEntity;
import app.school.administration.auth.infrastructure.persistence.entity.UserEntity;
import app.school.administration.auth.infrastructure.persistence.entity.mapping.UserRoleEntity;
import app.school.administration.common.application.custom.exception.TokenExpirationException;
import app.school.administration.common.domain.model.JWTProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * ====================================================================================
 * SERVICE: JWTService
 * ====================================================================================
 *
 * <h2>FUNCTIONALITY</h2>
 * Utility service for generating, parsing, validating, and resolving JSON Web Tokens (JWTs) using HMAC-SHA256 signing algorithms.
 *
 * <h2>WHY IMPLEMENTED</h2>
 * Essential component of the stateless authentication system:
 * - Generates short-lived Access Tokens containing user claims and assigned roles.
 * - Generates long-lived Refresh Tokens for token renewal.
 * - Parses and validates JWT signatures using HMAC keys configured in {@link JWTProperties}.
 * - Extracts `Bearer` authorization headers from HTTP servlet requests.
 *
 * <h2>WHAT HAPPENS IF NOT IMPLEMENTED</h2>
 * - The application cannot issue authentication tokens during user sign-in (`AuthServiceImpl.signIn`).
 * - Request validation filters (`JWTAuthFilter`) will be unable to verify incoming security credentials.
 * ====================================================================================
 */
@Service
@RequiredArgsConstructor
public class JWTService {

    private final JWTProperties jwtProperties;

    /**
     * Generates a signed JWT Access Token containing user subject and assigned roles.
     *
     * @param user target user entity
     * @return compact JWT access token string
     */
    public String generateAccessToken(UserEntity user) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("roles", user.getRoles().stream()
                        .map(UserRoleEntity::getRole)
                        .map(RoleEntity::getRoleCode)
                        .collect(Collectors.toSet())
                )
                .setIssuedAt(new Date())
                .setExpiration(Date.from(Instant.now().plus(jwtProperties.getAccessTokenExpireDays(), ChronoUnit.DAYS)))
                .signWith(Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Generates a signed JWT Refresh Token for session renewal.
     *
     * @param user target user entity
     * @return compact JWT refresh token string
     */
    public String generateRefreshToken(UserEntity user) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(Date.from(Instant.now().plus(jwtProperties.getRefreshTokenExpireMonths(), ChronoUnit.MONTHS)))
                .signWith(Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Resolves the raw JWT token string from the HTTP `Authorization` header.
     *
     * @param httpServletRequest current HTTP request
     * @return raw token string (excluding 'Bearer ' prefix) or null if header missing
     */
    public String tokenResolver(HttpServletRequest httpServletRequest) {
        String bearerToken = httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION);
        if (Objects.nonNull(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        } else {
            return null;
        }
    }

    /**
     * Parses all claims contained within a signed JWT token string.
     *
     * @param token compact JWT token
     * @return parsed Claims body
     */
    public Claims extractAllClaims(String token) {
        JwtParser jwtParser = Jwts.parser().setSigningKey(jwtProperties.getSecret());
        Jws<Claims> claimsJws = jwtParser.parseClaimsJws(token);
        return claimsJws.getBody();
    }

    /**
     * Checks if a token is valid (valid signature and not expired).
     *
     * @param refreshToken target JWT token
     * @return true if valid
     */
    public boolean isTokenValid(String refreshToken) {
        return (validateToken(refreshToken) && !isTokenExpired(refreshToken));
    }

    /**
     * Verifies if a token has passed its expiration timestamp.
     *
     * @param token target JWT token
     * @return true if expired or invalid
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = extractAllClaims(token).getExpiration();
            return expiration.before(new Date());
        } catch (ExpiredJwtException | TokenExpirationException e) {
            return true;
        }
    }

    /**
     * Validates cryptographic signature and structural integrity of a token.
     *
     * @param token target JWT token
     * @return true if signature matches and structural integrity is intact
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(jwtProperties.getSecret()).parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

}

