# Module Architecture Specification: Dual Google OAuth2 & Username/Password Authentication

> **Status: target / aspirational.** Current implemented behavior (HS256 JWT, `public` schema, OAuth redirect without token issue) is documented in [`docs/APPLICATION.md`](../../APPLICATION.md) and [`docs/implementation/auth-security.md`](../../implementation/auth-security.md). Prefer those when coding; use this file as a design target only.

**Module**: `app.school.administration.auth`  
**Target Platform**: Web Application (Angular Frontend + Spring Boot 3.x Backend)  
**Schema**: `master` (PostgreSQL) — *design target; runtime migrations use `public`*  
**Security Standard**: RS256 Asymmetric JWT + SHA-256 Hashed Refresh Token Rotation (RTR)

---

## 1. Overview & Dual-Authentication Architecture

This specification documents the enterprise authentication subsystem for `kalvi-net-server`. It supports **Dual Authentication**:
1. **Username / Email + Password Login** (`POST /api/auth/login`)
2. **Google OAuth2 Login** (`/oauth2/authorization/google`)

### Dual Authentication & Account Linking Flow

To guarantee that users can **always fall back to traditional username/password authentication** if Google login fails (or vice versa), the system implements automatic **Account Linking**:

```
                                  ┌───────────────────────────┐
                                  │   User Login Attempt      │
                                  └─────────────┬─────────────┘
                                                │
                     ┌──────────────────────────┴──────────────────────────┐
                     ▼                                                     ▼
      [ Traditional Credentials ]                                [ Google OAuth2 Login ]
      POST /api/auth/login                                       GET /oauth2/authorization/google
                     │                                                     │
                     ▼                                                     ▼
      Validate Username + BCrypt Password                     Verify Google ID Token Claims
                     │                                                     │
                     └──────────────────────────┬──────────────────────────┘
                                                │
                                                ▼
                                    ┌───────────────────────┐
                                    │ User Lookup in DB     │
                                    │ (google_id OR email)  │
                                    └───────────┬───────────┘
                                                │
                        ┌───────────────────────┴───────────────────────┐
                        ▼                                               ▼
              [ User Exists ]                                  [ User Not Found ]
                        │                                               │
         ┌──────────────┴──────────────┐                 ┌──────────────┴──────────────┐
         ▼                             ▼                 ▼                             ▼
  (Password User)             (Google Linked)    Create UserEntity               Create OAuthAccount
  Link google_id +             Update last_login Set provider=GOOGLE             Assign ROLE_STUDENT
  Keep BCrypt Password!        & picture_url     Generate BCrypt fallback pass!  Save to DB
         │                             │                 │                             │
         └──────────────┬──────────────┴─────────────────┴─────────────────────────────┘
                        │
                        ▼
            Generate Custom RS256 Access Token (15m) + Opaque SHA-256 Hashed Refresh Token
```

#### Key Rules for Dual Authentication:
1. **Password Preservation**: When a user registers via traditional username/password, their BCrypt password hash is stored in `user_table.password`. If they later log in using Google with the same email address, the backend updates `user_table.google_id` and attaches an `OAuthAccountEntity`, but **NEVER erases or overwrites their existing password hash**.
2. **Fallback Availability**: Because the password hash remains untouched, the user can log in using **either** Google OAuth2 or their traditional username/password at any time.
3. **Google-First User Fallback**: When a user registers via Google first, a secure random BCrypt password hash is assigned. The user can optionally trigger "Forgot Password" or "Set Password" in their profile to set a local password, enabling dual login.

---

## 2. Database Schema (`master` Schema)

### SQL Migration Script (`V2__add_google_oauth2_and_refresh_tokens.sql`)

```sql
SET search_path TO master;

-- 1. Extend master.user_table for Google OAuth metadata while keeping password intact
ALTER TABLE master.user_table
    ADD COLUMN IF NOT EXISTS google_id VARCHAR(255) UNIQUE,
    ADD COLUMN IF NOT EXISTS provider VARCHAR(50) DEFAULT 'LOCAL',
    ADD COLUMN IF NOT EXISTS email_verified BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS picture_url VARCHAR(512),
    ADD COLUMN IF NOT EXISTS last_login TIMESTAMP WITH TIME ZONE;

CREATE INDEX IF NOT EXISTS idx_user_google_id ON master.user_table(google_id);

-- 2. Create Refresh Tokens table storing SHA-256 Hashed Tokens
CREATE TABLE IF NOT EXISTS master.refresh_tokens (
    token_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP WITH TIME ZONE,
    updated_by VARCHAR(100),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) 
        REFERENCES master.user_table(user_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_refresh_token_hash ON master.refresh_tokens(token_hash);
CREATE INDEX IF NOT EXISTS idx_refresh_token_user ON master.refresh_tokens(user_id);
```

---

## 3. Token Strategy: RS256 JWT & Hashed Refresh Token Rotation

1. **JWT Access Token**:
   - Algorithm: **RS256** (RSA 2048-bit Private/Public Key).
   - Expiration: **15 minutes**.
   - Claims: `sub` (User ID), `email`, `roles`, `firstName`, `lastName`, `iss`, `aud`, `exp`.
   - Verification: Microservices and Web Gateway verify tokens using **only the Public Key** (stateless, 0 DB queries).

2. **Refresh Token**:
   - Format: 64-byte CSPRNG random string (Opaque, non-JWT).
   - Storage: Only the **SHA-256 hash** is saved in `master.refresh_tokens`.
   - Expiration: 60 days.
   - **Rotation**: On `/api/auth/refresh`, the presented token is verified, immediately deleted, and a new token pair is issued.
   - **Reuse Detection**: If an already-deleted or revoked refresh token is presented, the system revokes **all** refresh tokens for that user ID to stop stolen token attacks.

---

## 4. Complete Code Implementation

### Account Linking `CustomOAuth2UserService.java`

```java
package app.school.administration.auth.infrastructure.security.oauth;

import app.school.administration.auth.application.constant.AuthProvider;
import app.school.administration.auth.infrastructure.persistence.entity.OAuthAccountEntity;
import app.school.administration.auth.infrastructure.persistence.entity.RoleEntity;
import app.school.administration.auth.infrastructure.persistence.entity.UserEntity;
import app.school.administration.auth.infrastructure.persistence.repository.OAuthAccountRepository;
import app.school.administration.auth.infrastructure.persistence.repository.RoleRepository;
import app.school.administration.auth.infrastructure.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final OAuthAccountRepository oAuthAccountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        return processOAuth2User(oAuth2User);
    }

    private OAuth2User processOAuth2User(OAuth2User oAuth2User) {
        String googleId = oAuth2User.getAttribute("sub");
        String email = oAuth2User.getAttribute("email");
        String firstName = oAuth2User.getAttribute("given_name");
        String lastName = oAuth2User.getAttribute("family_name");
        String picture = oAuth2User.getAttribute("picture");
        Boolean emailVerified = oAuth2User.getAttribute("email_verified");

        Optional<UserEntity> userOptional = userRepository.findByGoogleId(googleId)
                .or(() -> userRepository.findByEmail(email));

        UserEntity user;
        if (userOptional.isPresent()) {
            // Account Linking Path: Preserve existing BCrypt password so user can login with either method!
            user = userOptional.get();
            user.setLastLogin(Instant.now());
            if (user.getGoogleId() == null) {
                user.setGoogleId(googleId);
            }
            user.setPictureUrl(picture);
            user.setEmailVerified(Boolean.TRUE.equals(emailVerified));
        } else {
            // New Google User Provisioning
            user = new UserEntity();
            user.setGoogleId(googleId);
            user.setEmail(email);
            user.setUsername(email);
            user.setFirstName(firstName != null ? firstName : "GoogleUser");
            user.setLastName(lastName != null ? lastName : "");
            // Set random fallback password to allow password reset later
            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            user.setProvider(AuthProvider.GOOGLE);
            user.setEmailVerified(Boolean.TRUE.equals(emailVerified));
            user.setPictureUrl(picture);
            user.setLastLogin(Instant.now());

            RoleEntity studentRole = roleRepository.findByName("ROLE_STUDENT")
                    .orElseThrow(() -> new IllegalStateException("Default ROLE_STUDENT not found"));
            user.assignRole(studentRole);
        }

        userRepository.save(user);

        // Link to oauth_accounts table
        if (oAuthAccountRepository.findByProviderAndProviderUserId(AuthProvider.GOOGLE, googleId).isEmpty()) {
            OAuthAccountEntity oauthAccount = OAuthAccountEntity.builder()
                    .provider(AuthProvider.GOOGLE)
                    .providerUserId(googleId)
                    .user(user)
                    .build();
            oAuthAccountRepository.save(oauthAccount);
        }

        return oAuth2User;
    }
}
```

---

## 5. Deployment Setup Checklist

1. **Google Cloud Credentials**:
   - Redirect URI: `http://localhost:8080/login/oauth2/code/google` (Dev) / `https://api.kalvinet.com/login/oauth2/code/google` (Prod).
2. **OpenSSL RS256 Keys**:
   - Generate `private_key.pem` and `public_key.pem`. Store `private_key.pem` securely outside version control.
3. **HTTPS / SSL**:
   - Ensure TLS 1.3 is enabled on AWS Application Load Balancer (ALB).
