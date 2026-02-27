---
id: pr-review-checklist
description: Structured checklist for reviewing pull requests
whenToUse: When reviewing a PR, or when asked to review code changes
signals:
  - "review PR"
  - "code review"
  - "check this PR"
  - "review changes"
---
# PR Review Checklist

Use this checklist when reviewing pull requests:

## 1. Understanding
- [ ] Read the PR description and linked issue
- [ ] Understand the intent and scope of the change
- [ ] Check that the PR is appropriately sized (not too large)

## 2. Correctness
- [ ] Logic is correct and handles edge cases
- [ ] Error handling is appropriate
- [ ] No off-by-one errors, null pointer risks, or race conditions
- [ ] API contracts are respected (request/response shapes, status codes)

## 3. Security
- [ ] No secrets or credentials in the code
- [ ] Input is validated at boundaries
- [ ] No injection vulnerabilities (SQL, XSS, command)
- [ ] Authentication and authorization checks are present where needed

## 4. Testing
- [ ] New behavior has tests
- [ ] Tests cover happy path and error cases
- [ ] Tests are deterministic and isolated
- [ ] No test logic that depends on external services

## 5. Quality
- [ ] Code is readable and well-structured
- [ ] No unnecessary complexity or over-engineering
- [ ] Naming is clear and consistent
- [ ] No dead code, commented-out blocks, or leftover debug statements

## 6. Operations
- [ ] Logging is appropriate (not excessive, not missing)
- [ ] Database changes use migrations
- [ ] Configuration changes have defaults
- [ ] Backward compatibility is maintained (or breaking changes are documented)

## 7. Documentation
- [ ] Public APIs are documented
- [ ] README updated if user-facing behavior changed
- [ ] Changelog updated if applicable
