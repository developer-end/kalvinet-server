package app.school.administration.auth.application.component;

import app.school.administration.common.application.serviceimpl.JWTService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JWTService jwtService;

    @Value("${security.oauth2.frontend-url:http://localhost:4200}")
    private String configuredFrontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        log.info("Google OAuth2 Authentication successful for user: {}", authentication.getName());

        String email = "";
        String name = "";

        if (authentication.getPrincipal() instanceof OAuth2User oauth2User) {
            email = oauth2User.getAttribute("email");
            name = oauth2User.getAttribute("name");
        }

        // Dynamically resolve frontend base URL (supporting environment config & proxy headers)
        String baseUrl = resolveFrontendBaseUrl(request);

        String redirectUrl = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/login")
                .queryParam("oauth_success", "true")
                .queryParam("email", email != null ? email : "")
                .queryParam("name", name != null ? name : "")
                .build()
                .encode()
                .toUriString();

        log.info("Redirecting OAuth user to frontend URL: {}", redirectUrl);
        response.sendRedirect(redirectUrl);
    }

    /**
     * Loosely coupled resolution of the frontend URL across environments (local, staging, production).
     */
    private String resolveFrontendBaseUrl(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        String origin = request.getHeader("Origin");

        if (origin != null && !origin.isBlank()) {
            return origin;
        } else if (referer != null && !referer.isBlank()) {
            try {
                java.net.URI uri = new java.net.URI(referer);
                return uri.getScheme() + "://" + uri.getAuthority();
            } catch (Exception ignored) {
                // Fallback to configured property if URI parsing fails
            }
        }
        return configuredFrontendUrl;
    }

}
