# Architecture Overview

## What is aictx?

aictx is a centralized AI context management system. It provides:

1. **Context Packs**: Language/framework-specific instruction sets (Java, Flutter)
2. **Global Rules**: Reusable guidelines (PR review, testing, security)
3. **Skills**: Actionable patterns for common tasks (skeleton generation, review checklists)
4. **MCP Catalog**: Model Context Protocol server definitions and toolsets
5. **CLI Tool**: `aictx` command that generates tool-specific outputs from a single config

## How It Works

```
┌─────────────────────┐
│  aictx-context.yaml │  (in target repo)
│  - packs: [java]    │
│  - globals: [...]   │
│  - skills: [...]    │
│  - mcp: {...}       │
└────────┬────────────┘
         │
    aictx setup
         │
         ▼
┌─────────────────────────────────────────────┐
│  Context Library (this repo)                │
│  /context/packs/java/rules.md               │
│  /context/globals/pr-review.md              │
│  /context/skills/pr-review-checklist.md     │
│  /context/mcp/catalog.yaml                  │
└────────┬────────────────────────────────────┘
         │
    Generator
         │
         ▼
┌─────────────────────────────────────────────┐
│  Generated outputs (in target repo)         │
│  AGENTS.md                                  │
│  CLAUDE.md                                  │
│  .github/copilot-instructions.md            │
│  .github/instructions/*.instructions.md     │
│  .vscode/mcp.json                           │
│  .mcp.json                                  │
│  .codex/config.toml                         │
└─────────────────────────────────────────────┘
```

## Key Principles

- **Single source of truth**: All instruction content lives in `/context/`. Generated files reference or include this content.
- **No secrets**: The repo contains only URLs and logical names. Authentication is done by developers via their tool's sign-in flow.
- **Deterministic output**: Running `aictx setup` with the same config always produces the same output.
- **Additive merging**: Generated MCP configs preserve user-added entries; only aictx-managed entries are updated.
