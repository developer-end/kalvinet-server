package app.school.administration.auth.application.serviceimpl;

import app.school.administration.auth.api.request.SignInRequestDTO;
import app.school.administration.auth.api.request.SignUpRequestDTO;
import app.school.administration.auth.api.response.SignInResponseDTO;
import app.school.administration.auth.api.response.SignUpResponseDTO;
import app.school.administration.auth.application.constant.RoleAssignmentPolicy;
import app.school.administration.auth.application.service.AuthService;
import app.school.administration.auth.infrastructure.persistence.entity.RoleEntity;
import app.school.administration.auth.infrastructure.persistence.entity.UserEntity;
import app.school.administration.auth.infrastructure.persistence.repository.RoleRepository;
import app.school.administration.auth.infrastructure.persistence.repository.UserRepository;
import app.school.administration.common.application.serviceimpl.JWTService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authentication entry point: unified sign-in (no portal role required) and sign-up
 * that always assigns {@link RoleAssignmentPolicy#ROLE_USER}.
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserServiceImpl userService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JWTService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public SignInResponseDTO signIn(SignInRequestDTO dto) {
        UserEntity user = userRepository.findByUsernameIgnoreCase(dto.username())
                .orElseGet(() -> userService.findByUsernameAndPassword(dto.username(), dto.password())
                        .orElseThrow(() -> new BadCredentialsException("Username or password mismatch")));

        if (!passwordEncoder.matches(dto.password(), user.getPassword()) && !dto.password().equals(user.getPassword())) {
            throw new BadCredentialsException("Username or password mismatch");
        }

        // Portal/role query field is ignored — access is resolved from the user's assigned roles.
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        return new SignInResponseDTO(accessToken, refreshToken);
    }

    @Override
    @Transactional
    public SignUpResponseDTO signUp(SignUpRequestDTO dto) {
        if (userRepository.existsByUsernameIgnoreCase(dto.username())) {
            throw new IllegalArgumentException("Username is already taken");
        }
        if (userRepository.existsByEmailIgnoreCase(dto.email())) {
            throw new IllegalArgumentException("Email address is already registered");
        }

        // Defense in depth: always USER regardless of any role field the client sends.
        String roleCode = RoleAssignmentPolicy.ROLE_USER;
        String encodedPassword = passwordEncoder.encode(dto.password());

        UserEntity user = new UserEntity(
                dto.firstName(),
                dto.lastName(),
                dto.email(),
                dto.username(),
                encodedPassword,
                dto.mobileNo()
        );

        RoleEntity roleEntity = roleRepository.findByRoleCodeIgnoreCase(roleCode)
                .orElseThrow(() -> new IllegalStateException("Default role ROLE_USER is not seeded"));
        user.assignRole(roleEntity);
        userService.save(user);

        return new SignUpResponseDTO(
                "Successfully registered with KalviNet. Your account is pending role assignment."
        );
    }
}
