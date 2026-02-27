# MCP Configuration

## Overview

MCP (Model Context Protocol) allows AI coding assistants to interact with external tools and services. aictx manages MCP server configuration for VS Code (Copilot), Claude Code, and Codex.

## How It Works

1. The MCP catalog (`/context/mcp/catalog.yaml`) defines available servers and toolsets
2. `aictx-context.yaml` selects a toolset and optional additional servers
3. `aictx setup` generates tool-specific MCP config files:
   - `.vscode/mcp.json` for VS Code / GitHub Copilot
   - `.mcp.json` for Claude Code
   - `.codex/config.toml` for OpenAI Codex

## Authentication

**Important**: aictx never stores secrets. MCP server URLs are configured in the generated files, but authentication is handled by each developer through their tool's sign-in flow:

- **VS Code**: MCP servers prompt for authentication when first used
- **Claude Code**: Use the tool's built-in authentication
- **Codex**: Configure authentication per tool documentation

## Toolsets

Toolsets are named bundles of MCP servers:

- **backend-default**: context7, internal-docs, service-catalog, postgres
- **frontend-default**: context7, internal-docs, dart-mcp
- **full-stack**: All servers

## Customizing

In `aictx-context.yaml`:
```yaml
mcp:
  toolset: backend-default    # Use a predefined toolset
  servers:                     # Add extra servers
    - runbooks
```

## Merging Behavior

When generating MCP configs:
- Entries with `"managedBy": "aictx"` are updated
- User-added entries are preserved
- Unknown entries are never deleted
