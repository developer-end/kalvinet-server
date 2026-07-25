package app.school.administration.auth.api.controller;

import app.school.administration.auth.api.request.SignInRequestDTO;
import app.school.administration.auth.api.response.SignInResponseDTO;
import app.school.administration.auth.application.serviceimpl.AuthServiceImpl;
import app.school.administration.common.utils.AppAuthEndPoints;
import app.school.administration.common.utils.AppModuleApi;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ====================================================================================
 * REST CONTROLLER: AuthController
 * ====================================================================================
 *
 * <h2>FUNCTIONALITY</h2>
 * REST API controller handling authentication endpoints (e.g., `/api/v1/auth/signin`).
 *
 * <h2>WHY IMPLEMENTED</h2>
 * Exposes public REST API endpoints for user authentication:
 * - Validates input payloads using `@Validated @RequestBody`.
 * - Delegates authentication processing to {@link AuthServiceImpl}.
 * - Returns JWT access and refresh token response objects in HTTP 200 OK responses.
 *
 * <h2>WHAT HAPPENS IF NOT IMPLEMENTED</h2>
 * Front-end applications and mobile clients cannot invoke sign-in APIs or obtain JWT authentication tokens.
 * ====================================================================================
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(AppModuleApi.AUTH)
public class AuthController {

    private final AuthServiceImpl authService;

    /**
     * User sign-in REST API endpoint.
     *
     * @param dto sign-in request payload containing username and password
     * @return ResponseEntity containing JWT token response DTO
     */
    @PostMapping(AppAuthEndPoints.SIGN_IN)
    public ResponseEntity<SignInResponseDTO> signIn(@Validated @RequestBody SignInRequestDTO dto) {
        return ResponseEntity.ok(authService.signIn(dto));
    }

}

