package app.school.administration.auth.application.serviceimpl;

import app.school.administration.auth.api.request.SignInRequestDTO;
import app.school.administration.auth.api.request.SignUpRequestDTO;
import app.school.administration.auth.api.response.SignInResponseDTO;
import app.school.administration.auth.api.response.SignUpResponseDTO;
import app.school.administration.auth.application.service.AuthService;
import app.school.administration.auth.infrastructure.persistence.entity.UserEntity;
import app.school.administration.common.application.serviceimpl.JWTService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import app.school.administration.auth.infrastructure.persistence.entity.RoleEntity;
import app.school.administration.auth.infrastructure.persistence.entity.mapping.UserRoleEntity;
import app.school.administration.auth.infrastructure.persistence.repository.RoleRepository;
import app.school.administration.auth.infrastructure.persistence.repository.UserRepository;
import app.school.administration.auth.infrastructure.persistence.repository.UserRoleRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

/**
 * ====================================================================================
 * SERVICE: AuthServiceImpl
 * ====================================================================================
 *
 * <h2>FUNCTIONALITY</h2>
 * Implements authentication entry point workflows including user credentials authentication (`signIn`),
 * user registration (`signUp`), and token generation.
 * ====================================================================================
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserServiceImpl userService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final JWTService jwtService;
    private final PasswordEncoder passwordEncoder;

    /**
     * Authenticates user credentials and issues Access / Refresh tokens.
     *
     * @param dto sign-in request payload containing username, hashed password, and hashed role
     * @return response DTO containing JWT access token and refresh token
     * @throws BadCredentialsException if username, password, or portal role does not match
     */
    @Override
    @Transactional(readOnly = true)
    public SignInResponseDTO signIn(SignInRequestDTO dto) {
        UserEntity user = userRepository.findByUsernameIgnoreCase(dto.username())
                .orElseGet(() -> userService.findByUsernameAndPassword(dto.username(), dto.password())
                        .orElseThrow(() -> new BadCredentialsException("Username or password mismatch")));

        if (!passwordEncoder.matches(dto.password(), user.getPassword()) && !dto.password().equals(user.getPassword())) {
            throw new BadCredentialsException("Username or password mismatch");
        }

        if (dto.role() != null && !dto.role().isBlank()) {
            String targetRoleCode = resolveRoleCodeFromHash(dto.role());
            boolean hasRole = user.getRoles().stream()
                    .anyMatch(r -> r.getRole() != null && targetRoleCode.equalsIgnoreCase(r.getRole().getRoleCode()));
            if (!hasRole && !user.getRoles().isEmpty()) {
                throw new BadCredentialsException("URL mismatch for login: User is not authorized for this portal");
            }
        }

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        return new SignInResponseDTO(accessToken, refreshToken);
    }

    /**
     * Registers a new user account.
     *
     * @param dto sign-up request payload containing user details and hashed role
     * @return response DTO for sign-up completion
     */
    @Override
    @Transactional
    public SignUpResponseDTO signUp(SignUpRequestDTO dto) {
        if (userRepository.existsByUsernameIgnoreCase(dto.username())) {
            throw new IllegalArgumentException("Username is already taken");
        }
        if (userRepository.existsByEmailIgnoreCase(dto.email())) {
            throw new IllegalArgumentException("Email address is already registered");
        }

        String roleCode = resolveRoleCodeFromHash(dto.role());
        String encodedPassword = passwordEncoder.encode(dto.password());

        UserEntity user = new UserEntity(
                dto.firstName(),
                dto.lastName(),
                dto.email(),
                dto.username(),
                encodedPassword,
                dto.mobileNo()
        );

        RoleEntity roleEntity = roleRepository.findByRoleCodeIgnoreCase(roleCode).orElse(null);
        if (roleEntity != null) {
            user.assignRole(roleEntity);
        }

        user = userService.save(user);

        String readableRole = roleCode.replace("ROLE_", "").toLowerCase();
        String formattedRole = Character.toUpperCase(readableRole.charAt(0)) + readableRole.substring(1);

        return new SignUpResponseDTO(
                "Successfully registered with KalviNet as " + formattedRole + " user"
        );
    }

    private String resolveRoleCodeFromHash(String hashedRole) {
        if (hashedRole == null || hashedRole.isBlank()) {
            return "ROLE_USER";
        }
        List<RoleEntity> allRoles = roleRepository.findAll();
        for (RoleEntity roleEntity : allRoles) {
            String candidate = roleEntity.getRoleCode();
            if (candidate != null && (sha256(candidate).equalsIgnoreCase(hashedRole) || candidate.equalsIgnoreCase(hashedRole))) {
                return candidate;
            }
        }
        throw new IllegalArgumentException("URL mismatch for login or account creation: Invalid role portal prefix");
    }

    private static String sha256(String data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error computing SHA-256 hash", e);
        }
    }

}

