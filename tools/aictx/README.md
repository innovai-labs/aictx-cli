# aictx CLI Tool

## Overview
The `aictx` CLI is a Java application built with [Picocli](https://picocli.info/) and designed to run via [JBang](https://www.jbang.dev/). It reads `aictx-context.yaml` from a target repository and generates tool-specific instruction files and MCP configs.

## Architecture
```
aictx.java              — JBang entrypoint, Picocli command definitions
src/
  ContextModel.java     — YAML config model (aictx-context.yaml)
  PackModel.java        — Pack metadata model (pack.yaml)
  McpCatalog.java       — MCP catalog model (catalog.yaml)
  TemplateEngine.java   — Simple {{placeholder}} template renderer
  Generator.java        — Output generation logic
  VersionUtil.java      — Version checking and context root resolution
```

## Local Development

### Prerequisites
- Java 17+
- JBang installed (`brew install jbangdev/tap/jbang` or `curl -Ls https://sh.jbang.dev | bash`)

### Running
```bash
# From the repo root:
jbang tools/aictx/aictx.java version
jbang tools/aictx/aictx.java init
jbang tools/aictx/aictx.java setup --dry-run --verbose

# With explicit context root:
AICTX_CONTEXT_ROOT=./context jbang tools/aictx/aictx.java setup
```

### Dependencies
Managed via JBang `//DEPS` directives in `aictx.java`:
- `info.picocli:picocli:4.7.6` — CLI framework
- `com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.17.2` — YAML parsing
- `com.fasterxml.jackson.core:jackson-databind:2.17.2` — Object mapping

### Testing
```bash
# Manual verification:
cd /tmp && mkdir test-project && cd test-project
AICTX_CONTEXT_ROOT=/path/to/aictx-cli/context \
  jbang /path/to/aictx-cli/tools/aictx/aictx.java init --repo-type java-service
AICTX_CONTEXT_ROOT=/path/to/aictx-cli/context \
  jbang /path/to/aictx-cli/tools/aictx/aictx.java setup --verbose
ls -la  # Check generated files
cat AGENTS.md
```

### Adding Commands
1. Create a new `@Command`-annotated class implementing `Callable<Integer>` in `aictx.java`
2. Add the class to the `subcommands` array in the `@Command` annotation on the `aictx` class
3. Implement the `call()` method
