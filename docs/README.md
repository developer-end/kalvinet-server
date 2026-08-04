# KalviNet Server — Documentation Hub

**Open access.** All files under `docs/` and root `AGENTS.md` are plain Markdown.  
Any AI agent, IDE assistant, or human may **read and edit** them. Nothing here is Cursor-only or locked.

| Document | Purpose |
|---|---|
| **[APPLICATION.md](./APPLICATION.md)** | Living overview — APIs, auth, tenancy, gaps (**update on every feature change**) |
| **[../AGENTS.md](../AGENTS.md)** | Universal agent/IDE guide — task → exact file paths |
| **[implementation/](./implementation/)** | Deep-dive notes (architecture, auth/security, multi-tenancy, database) |
| **[CONTRIBUTING.md](./CONTRIBUTING.md)** | Contribution & module boundaries |
| **[modules/](./modules/)** | Module-specific guides (e.g. OAuth architecture) |
| **[../DATABASE_README.md](../DATABASE_README.md)** | Detailed DB architecture, Flyway, Docker Postgres |

## For AI agents & IDEs (Cursor, VS Code, Copilot, Claude, Windsurf, JetBrains, etc.)

1. Start with **`AGENTS.md`** (repo root) for where to edit.
2. Read **`docs/APPLICATION.md`** for current server behavior.
3. Use **`docs/implementation/`** only when you need deeper detail.
4. After any behavior change: update **`docs/APPLICATION.md`** (bump *Last reviewed*) in the same change.

Optional IDE mirrors (same content, not a second source of truth):

- `.cursor/rules/` — Cursor convenience mirrors of `AGENTS.md` / docs
- `.github/copilot-instructions.md` — GitHub Copilot entrypoint
- `CLAUDE.md` — Claude Code / compatible agents entrypoint

Do **not** put product truth only inside vendor-specific folders. Prefer `docs/` + `AGENTS.md`.
