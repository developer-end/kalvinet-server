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

/**
 * ====================================================================================
 * SERVICE: AuthServiceImpl
 * ====================================================================================
 *
 * <h2>FUNCTIONALITY</h2>
 * Implements authentication entry point workflows including user credentials authentication (`signIn`)
 * and token generation.
 *
 * <h2>WHY IMPLEMENTED</h2>
 * Encapsulates the core login workflow:
 * - Validates username and password against {@link UserServiceImpl}.
 * - Throws {@link BadCredentialsException} on authentication failure (preventing timing attacks).
 * - Issues Access and Refresh tokens via {@link JWTService} upon successful login.
 *
 * <h2>WHAT HAPPENS IF NOT IMPLEMENTED</h2>
 * Authentication endpoints (`/auth/signin`) will fail to process user credentials and generate authentication tokens.
 * ====================================================================================
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserServiceImpl userService;
    private final JWTService jwtService;

    /**
     * Authenticates user credentials and issues Access / Refresh tokens.
     *
     * @param dto sign-in request payload containing username and password
     * @return response DTO containing JWT access token and refresh token
     * @throws BadCredentialsException if username or password does not match
     */
    @Override
    public SignInResponseDTO signIn(SignInRequestDTO dto) {
        UserEntity user = userService.findByUsernameAndPassword(dto.username(), dto.password())
                .orElseThrow(() -> new BadCredentialsException("Username or password mismatch"));
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        return new SignInResponseDTO(accessToken, refreshToken);
    }

    /**
     * Registers a new user account.
     *
     * @param dto sign-up request payload
     * @return response DTO for sign-up completion
     */
    @Override
    public SignUpResponseDTO signUp(SignUpRequestDTO dto) {
        return null;
    }

}

