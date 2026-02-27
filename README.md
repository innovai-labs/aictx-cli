# aictx — AI Context CLI

A centralized context management system that generates tool-specific instruction files and MCP configs for AI coding assistants (GitHub Copilot, Claude Code, OpenAI Codex) from a single configuration.

## Features

- **Context Packs**: Language-specific rules and conventions (Java, Flutter)
- **Global Rules**: Reusable guidelines for PR review, testing, security
- **Skills**: Actionable patterns for common development tasks
- **MCP Catalog**: Managed Model Context Protocol server configs
- **Multi-tool Output**: Generates configs for Copilot, Claude, and Codex from one source

## Install

### Prerequisites
- [JBang](https://www.jbang.dev/download/) installed
- SSH access to the repository

### macOS / Linux
```bash
jbang app install --name aictx \
  "git+ssh://git@<git-host>/<org>/aictx-cli.git#subdirectory=tools/aictx&ref=v0.1.0"
```

### Windows
```powershell
jbang app install --name aictx `
  "git+ssh://git@<git-host>/<org>/aictx-cli.git#subdirectory=tools/aictx&ref=v0.1.0"
```

### Verify
```bash
aictx version
```

## Quickstart

### 1. Initialize config
```bash
cd your-repo
aictx init --repo-type java-service   # or flutter-app
```

### 2. Edit config
Open `aictx-context.yaml` and adjust packs, globals, skills, and MCP settings.

### 3. Generate outputs
```bash
aictx setup
```

This generates:
- `AGENTS.md` — canonical cross-tool instructions
- `CLAUDE.md` — Claude Code instructions
- `.github/copilot-instructions.md` — Copilot instructions
- `.github/instructions/*.instructions.md` — path-scoped Copilot rules
- `.vscode/mcp.json` — VS Code MCP config
- `.mcp.json` — Claude MCP config
- `.codex/config.toml` — Codex config

### 4. Commit and authenticate
```bash
git add AGENTS.md CLAUDE.md .github/ .vscode/ .mcp.json .codex/
git commit -m "Add AI context configuration"
```
Then authenticate MCP servers via your tool's sign-in flow.

## Examples

### Java Service
```bash
aictx init --repo-type java-service
aictx setup
```

### Flutter App
```bash
aictx init --repo-type flutter-app
aictx setup
```

See `/examples/` for sample configurations.

## Commands

| Command | Description |
|---------|-------------|
| `aictx version` | Print CLI and schema version |
| `aictx init` | Create `aictx-context.yaml` template |
| `aictx setup` | Generate all output files |
| `aictx upgrade` | Print or execute upgrade command |
| `aictx update-check` | Check for newer versions |

### Flags
- `aictx init --repo-type <type>` — preselect repo type
- `aictx init --force` — overwrite existing config
- `aictx setup --config <path>` — custom config path
- `aictx setup --dry-run` — preview without writing
- `aictx setup --force` — overwrite non-managed files
- `aictx setup --verbose` — detailed output
- `aictx upgrade --execute` — run upgrade automatically

## Upgrade

```bash
# Check for updates
aictx update-check

# See upgrade command
aictx upgrade

# Execute upgrade
aictx upgrade --execute
```

## Documentation

- [Architecture Overview](DOCS/overview.md)
- [Developer Quickstart](DOCS/developer-quickstart.md)
- [Context Authoring](DOCS/context-authoring.md)
- [Packs](DOCS/packs.md)
- [Skills](DOCS/skills.md)
- [MCP Configuration](DOCS/mcp.md)
- [Troubleshooting](DOCS/troubleshooting.md)

## License

Private — internal use only.
