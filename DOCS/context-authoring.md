# Context Authoring Guide

## Adding a New Pack

1. Create a directory under `/context/packs/<pack-id>/`
2. Create `pack.yaml` with metadata:
   ```yaml
   id: my-pack
   description: Context pack for ...
   appliesToRepoTypes:
     - my-repo-type
   defaultGlobals:
     - pr-review
     - testing
   defaultSkills: []
   defaultMcpToolset: backend-default
   pathRules:
     - paths/some-rule.instructions.md
   ```
3. Create `rules.md` with architecture and coding rules
4. Create `repo-instructions.md` with build/test/run commands and project structure
5. Add path-scoped rules in `paths/` with `applyTo` frontmatter
6. Add pack-specific skills in `skills/`

## Adding a Global Rule

1. Create a markdown file in `/context/globals/<id>.md`
2. Write the rule content (no frontmatter needed)
3. Reference it in pack `defaultGlobals` or let users add it to their config

## Adding a Skill

Skills can be global (in `/context/skills/`) or pack-specific (in `/context/packs/<pack>/skills/`).

Each skill markdown file uses YAML frontmatter:
```yaml
---
id: my-skill
description: Short description
whenToUse: When the developer asks to ...
signals:
  - "keyword 1"
  - "keyword 2"
---
```

Keep skill content:
- Actionable (steps, not paragraphs)
- Concise (bullet rules + short code examples)
- Focused (one pattern per skill)

## Adding Path Rules

Path rules are Copilot-compatible instruction files with `applyTo` frontmatter:
```yaml
---
applyTo: "src/main/resources/db/**"
---
```

The `applyTo` value is a glob pattern. The generator copies these to `.github/instructions/`.
