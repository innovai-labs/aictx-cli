---
applyTo: "infra/**,terraform/**,deploy/**"
---
# Infrastructure Code Rules

- Use modules for reusable infrastructure components
- Pin provider and module versions explicitly
- Store state remotely with locking enabled
- Never hardcode secrets; use secret manager references
- Tag all resources with environment, team, and service name
- Use variables with descriptions and validation rules
- Review plan output before applying changes
- Keep blast radius small: separate state files per environment or component
- Document non-obvious infrastructure decisions in comments
