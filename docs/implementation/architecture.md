# Architecture (server)

Base package: `app.school.administration`  
Config: `src/main/resources/application.yaml` (+ `application-local.yaml`)

```
src/main/java/app/school/administration/
  KalviNetApplication.java
  auth/
    api/controller|request|response
    application/component|constant|context|service|serviceimpl
    domain/model
    infrastructure/persistence/{entity,repository,projection}
  common/
    api/                    # health, request cancel
    application/            # JWT filter, exceptions, JWTService, AppBaseService, tenancy migrate
    config/{security,flyway,cache,websocket}
    domain/{model,repository}
    infrastucture/          # AuditableBaseEntity (typo in package name)
    utils/                  # AppApiVersion, AppModuleApi, AppAuthEndPoints, AppCommonEndPoint
  dashboard/                # dashboard config API
  modules/school/           # feature module template
```

## Layers

| Layer | Responsibility |
|---|---|
| `api` | Controllers + request/response DTOs |
| `application` | Services, filters, ThreadLocal contexts, constants |
| `domain` | Non-JPA models, repository interfaces (base) |
| `infrastructure` / `persistance` | JPA entities, Spring Data repos, projections |

## Runtime shape

- Servlet context: `/erp`
- Port: `8081`
- Profile: `local` loads DB + Google OAuth client settings
- Hibernate: `ddl-auto: none`, `multiTenancy: SCHEMA`
- Flyway Boot auto-config **off**; custom `MasterFlywayConfig` + `TenantMigrationService`

## Cross-cutting

| Concern | Location |
|---|---|
| Security filter chain | `common/config/security/SecurityConfig.java` |
| JWT filter | `common/application/component/JWTAuthFilter.java` |
| Exception mapping | `common/application/custom/exception/handler/GlobalExceptionHandler.java` |
| WebSocket `/ws` | `common/config/websocket/WebSocketConfig.java` |
| Cache | `common/config/cache/CaffeineCacheConfig.java` |

## Efficiency rule for agents

1. `AGENTS.md` table → exact file  
2. Edit shared source (constants, base service, security)  
3. Skip whole-project search unless the map misses the topic
