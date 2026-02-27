---
applyTo: "pubspec.yaml"
---
# Pubspec Dependency Rules

- Pin dependency versions using caret syntax: `^1.2.3` (not `any` or open ranges)
- Justify new dependencies in the PR description
- Prefer well-maintained packages with good pub.dev scores
- Check for breaking changes before upgrading major versions
- Keep dev_dependencies separate from runtime dependencies
- Run `flutter pub outdated` periodically to check for updates
- Remove unused dependencies; do not leave commented-out entries
- If a dependency requires platform-specific setup, document it in README
