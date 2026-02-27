# CLAUDE.md — aictx-cli Repository Guide

## What is this repo?
aictx-cli is a monorepo containing:
- A centralized library of AI context packs, skills, and MCP catalog (`/context/`)
- A Java CLI tool (`/tools/aictx/`) built with Picocli and runnable via JBang
- Documentation and examples

## Running the CLI Locally
```bash
# From the repo root:
jbang tools/aictx/aictx.java version
jbang tools/aictx/aictx.java init --repo-type java-service
jbang tools/aictx/aictx.java setup --dry-run

# Set context root for development:
export AICTX_CONTEXT_ROOT=$(pwd)/context
```

## Repository Structure
```
/context/globals/      — Reusable rules (pr-review, testing, security)
/context/packs/        — Language packs (java, flutter) with rules, paths, skills
/context/skills/       — Global skills
/context/mcp/          — MCP server catalog and templates
/tools/aictx/          — CLI tool (aictx.java + src/)
/DOCS/                 — Documentation
/examples/             — Sample configs
```

## Rules for Contributors
- Keep the generator deterministic: same config → same output
- Do not hardcode company secrets, API keys, or environment-specific URLs in context files
- Use `{{PLACEHOLDER}}` for environment-specific values in MCP catalog
- Keep generated outputs concise; reference AGENTS.md instead of duplicating content
- Prefer additive changes; avoid breaking existing aictx-context.yaml schemas
- When adding new packs or skills, follow existing patterns and update DOCS
- Pack content lives in `/context/packs/<id>/` — one canonical source per rule
- Global rules live in `/context/globals/` — keep them language-agnostic
- Skills use YAML frontmatter with id, description, whenToUse, and signals

## Adding a New Pack
1. Create `/context/packs/<id>/pack.yaml` with pack metadata
2. Add `rules.md` and `repo-instructions.md`
3. Add path rules in `paths/` with `applyTo` frontmatter
4. Add skills in `skills/` with YAML frontmatter
5. Update documentation in `/DOCS/packs.md`

## Adding a New Skill
1. Create `<id>.skill.md` in `/context/skills/` (global) or `/context/packs/<pack>/skills/` (pack-specific)
2. Include frontmatter: id, description, whenToUse, signals
3. Keep content actionable and concise

## Build & Test
```bash
# Run CLI
jbang tools/aictx/aictx.java <command>

# Test with example config
cd /tmp && mkdir test-repo && cd test-repo
export AICTX_CONTEXT_ROOT=/path/to/aictx-cli/context
jbang /path/to/aictx-cli/tools/aictx/aictx.java init --repo-type java-service
jbang /path/to/aictx-cli/tools/aictx/aictx.java setup --verbose
```
