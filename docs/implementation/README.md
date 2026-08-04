# Implementation index

Canonical agent entry: [`AGENTS.md`](../../AGENTS.md) at repo root — **universal for all AI agents / IDEs**.  
**Overall application (living):** [`../APPLICATION.md`](../APPLICATION.md) — update on every feature/behavior change.  
Doc hub: [`../README.md`](../README.md)  
Database deep guide: [`../../DATABASE_README.md`](../../DATABASE_README.md)

Optional IDE mirrors (not the source of truth): `.cursor/rules/`, `CLAUDE.md`, `.github/copilot-instructions.md`

Use these notes instead of scanning the whole server tree.

| Doc | Covers |
|---|---|
| [../APPLICATION.md](../APPLICATION.md) | Full server overview, APIs, gaps |
| [architecture.md](./architecture.md) | Packages, layering, config, WebSocket |
| [auth-security.md](./auth-security.md) | JWT, OAuth2, public endpoints, SecurityConfig |
| [multi-tenancy.md](./multi-tenancy.md) | TenantContext, Hibernate SCHEMA, connection routing |
| [database.md](./database.md) | Flyway public/tenant, entities, Docker Postgres |

## Maintenance

When you introduce a shared pattern **or change functionality**:

1. Update [`docs/APPLICATION.md`](../APPLICATION.md) (required)
2. Add/update a row in `AGENTS.md` if edit paths changed
3. Update the matching deep-dive doc here
4. Optionally refresh IDE mirrors under `.cursor/rules/` if present — keep them short and pointing at `docs/`
