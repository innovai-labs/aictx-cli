# Troubleshooting

## Common Issues

### "Config not found: aictx-context.yaml"
Run `aictx init` first to create the config file in your repo root.

### "Cannot locate context library"
The CLI needs access to the context library from the aictx-cli repo. Solutions:
- Set `AICTX_CONTEXT_ROOT` environment variable to the path of the `context/` directory
- Ensure you installed via JBang from the git repo (the repo is cloned automatically)
- For local development, run from the repo root directory

### "Config schema version X is newer than supported"
Your config was created with a newer version of aictx. Run `aictx upgrade` to get the latest CLI.

### "pack not found: X"
The specified pack doesn't exist in the context library. Check available packs in `/context/packs/`.

### "skill not found: X"
The skill isn't found in global skills or any loaded pack's skills. Check:
- `/context/skills/` for global skills
- `/context/packs/<pack>/skills/` for pack-specific skills

### MCP servers not working
1. Check that the generated config file exists (`.mcp.json`, `.vscode/mcp.json`)
2. Verify the server URLs are correct (replace `{{...}}` placeholders with actual URLs)
3. Complete authentication through your tool's sign-in flow
4. Restart your editor/tool after config changes

### Generated files overwritten by accident
aictx only overwrites files that contain the `managed-by: aictx` marker. If you removed the marker, use `--force` to overwrite, or restore the file from git.

### JBang installation issues
```bash
# Install JBang (macOS)
brew install jbangdev/tap/jbang

# Install JBang (Linux/Windows)
curl -Ls https://sh.jbang.dev | bash -s - app setup

# Verify
jbang --version
```

### SSH authentication for private repo
Ensure your SSH key is configured:
```bash
ssh-add ~/.ssh/id_ed25519
ssh -T git@<git-host>
```
