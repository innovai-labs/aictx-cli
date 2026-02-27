---
applyTo: "db/migration/**,db/migrations/**,src/main/resources/db/**"
---
# Database Migration Rules

- Never modify an existing migration that has been applied to any environment
- Always create a new migration file with the next sequential version number
- Use descriptive names: `V003__add_user_email_index.sql`
- Each migration should be atomic: one logical change per file
- Include both up and down logic where the tool supports it
- Test migrations against a copy of production-like data before merging
- Large data migrations should be done in batches to avoid locking
- Add comments explaining WHY a migration exists, not just WHAT it does
