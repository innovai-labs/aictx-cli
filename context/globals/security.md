# Security Guidelines

## Secrets Management
- Never commit secrets, API keys, tokens, or passwords
- Use environment variables or secret management services
- Rotate credentials regularly; do not share across environments
- Add sensitive file patterns to `.gitignore`

## Input Validation
- Validate all external input at system boundaries
- Reject invalid input early with clear error messages
- Sanitize data before use in queries, templates, or commands
- Use allowlists over denylists where possible

## Least Privilege
- Services should have minimal required permissions
- Database users should have scoped access (no superuser in production)
- API tokens should be scoped to needed operations
- File permissions should be restrictive by default

## Dependency Security
- Pin dependency versions; avoid floating ranges in production
- Regularly audit dependencies for known vulnerabilities
- Prefer well-maintained libraries with active security response
- Remove unused dependencies

## Common Vulnerabilities
- Prevent SQL injection: use parameterized queries
- Prevent XSS: escape output in templates
- Prevent command injection: avoid shell execution with user input
- Prevent path traversal: validate and canonicalize file paths
- Use HTTPS for all external communication
