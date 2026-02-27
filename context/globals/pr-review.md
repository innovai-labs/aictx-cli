# PR Review Guidelines

## Before Submitting
- All tests pass locally and in CI
- No unrelated changes included
- Commit messages are clear and descriptive
- Branch is up to date with the target branch

## Security Checklist
- No secrets, tokens, or credentials committed
- No hardcoded environment-specific values
- Input validation on all public API boundaries
- SQL queries use parameterized statements
- Dependencies are from trusted sources with no known CVEs

## Quality Checklist
- Code is readable without excessive comments
- Public APIs have documentation
- Error handling is explicit, not swallowed
- No TODO/FIXME without a linked issue
- Logging is meaningful (not excessive, not missing)

## Backward Compatibility
- API changes are backward compatible or clearly documented as breaking
- Database migrations are reversible where possible
- Configuration changes have sensible defaults
- Feature flags protect risky rollouts

## Testing
- New behavior has corresponding tests
- Edge cases and error paths are covered
- Tests are deterministic (no flakes)
- Integration tests clean up after themselves
