# Developer Quickstart

## Contributing to this Repository

### Prerequisites
- Java 17+
- [JBang](https://www.jbang.dev/download/) installed

### Running the CLI Locally
```bash
# From the repo root:
jbang tools/aictx/aictx.java version

# Run init in a test directory:
cd /tmp/test-repo
jbang /path/to/aictx-cli/tools/aictx/aictx.java init --repo-type java-service

# Run setup (set AICTX_CONTEXT_ROOT to point to the context library):
export AICTX_CONTEXT_ROOT=/path/to/aictx-cli/context
jbang /path/to/aictx-cli/tools/aictx/aictx.java setup
```

### Repository Structure
```
/context/          # Canonical instruction content
  /globals/        # Reusable rules (pr-review, testing, security)
  /packs/          # Language packs (java, flutter)
  /skills/         # Global skills
  /mcp/            # MCP server catalog and templates
/tools/aictx/      # CLI tool (JBang + Picocli)
/DOCS/             # Documentation
/examples/         # Example configs
```

### Releasing a New Version
1. Update `VERSION` constant in `tools/aictx/aictx.java`
2. Update any schema changes and increment `SCHEMA_VERSION` if needed
3. Commit changes
4. Tag with SemVer: `git tag v0.2.0`
5. Push tag: `git push origin v0.2.0`

Developers can then upgrade: `aictx upgrade`
