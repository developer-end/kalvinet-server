# Auth & security

## Source of truth

| Concern | Path |
|---|---|
| Filter chain / CORS / OAuth2 client | `common/config/security/SecurityConfig.java` |
| Public path prefixes | `auth/application/constant/AuthConstant.java` |
| Sign in / sign up | `auth/api/controller/AuthController.java`, `auth/application/serviceimpl/AuthServiceImpl.java` |
| JWT create/parse | `common/application/serviceimpl/JWTService.java` |
| Bearer filter | `common/application/component/JWTAuthFilter.java` |
| UserDetails load | `auth/application/serviceimpl/CustomUserDetailsServiceImpl.java` |
| OAuth success | `auth/application/component/OAuth2SuccessHandler.java` |
| Path constants | `common/utils/AppAuthEndPoints.java`, `AppModuleApi.java` |
| JWT properties | `application.yaml` → `security.jwt.*` |

## Password auth

| Intent | Method + path (under `/erp`) |
|---|---|
| Sign in | `POST /api/v1/auth/signIn` |
| Sign up | `POST /api/v1/auth/signUp` |
| Sign out | **Not implemented** (`AppAuthEndPoints.SIGN_OUT` only) |
| Refresh | **Not implemented** (`AppAuthEndPoints.REFRESH` only) |

Tokens: HS256 access + refresh JWTs from `JWTService`. Claims include username (`sub`) and roles.

## Google OAuth2 (current behavior)

1. Client opens `GET /erp/oauth2/authorization/google`
2. Spring OAuth2 Client completes Google sign-in → `/erp/login/oauth2/code/google` (Spring Security default path; do not rename)
3. `OAuth2SuccessHandler` redirects to `{security.oauth2.frontend-url}/signin?oauth_success=true&email=&name=`
4. **No JWT is issued** and account linking via `OAuthAccountEntity` is not completed in the success handler

Client config: `application-local.yaml` → `spring.security.oauth2.client.registration.google`

## Public endpoints

Prefixes in `AuthConstant.PUBLIC_ENDPOINTS` are `permitAll` (relative to context path as matched by Spring Security):

- `/api/v1/auth`, `/api/v1/oAuth`, `/api/v1/school`, `/api/v1/tenant`
- `/api/v1/dashboard`, `/ws`, `/v3/api-docs`, `/swagger-ui`
- `/api/requests`, `/api/health`

Treat new APIs as **authenticated** unless there is a clear anonymous need.

## Spec vs code

`docs/modules/auth/oauth2-google-jwt-architecture.md` describes RS256, refresh-token rotation, and a `master` schema. That is a **target architecture**, not the implemented HS256 + `public` schema stack. Prefer `docs/APPLICATION.md` for current truth.

## Gaps to remember

- Plaintext password equality fallback in sign-in path
- Broad public CRUD on school/tenant/oAuth/dashboard
- JWT filter `shouldNotFilter` uses request URI — verify against `/erp` prefix when changing public paths
- Hardcoded JWT secret in YAML for local; use env for shared/prod environments
