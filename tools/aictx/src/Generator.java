import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

/**
 * Generates tool-specific instruction files and MCP configs from a context model.
 */
public class Generator {

    private final ContextModel ctx;
    private final Path contextRoot;
    private final boolean dryRun;
    private final boolean force;
    private final boolean verbose;
    private final Path outputDir;
    private final List<String> generatedFiles = new ArrayList<>();

    // Marker used to identify aictx-managed content
    private static final String MANAGED_MARKER = "managed-by: aictx";

    public Generator(ContextModel ctx, Path contextRoot, boolean dryRun, boolean force, boolean verbose) {
        this.ctx = ctx;
        this.contextRoot = contextRoot;
        this.dryRun = dryRun;
        this.force = force;
        this.verbose = verbose;
        this.outputDir = Path.of("."); // current working directory
    }

    public void generate() throws IOException {
        // 1. Load packs
        List<PackModel> packs = loadPacks();

        // 2. Load globals content
        String globalsContent = loadGlobals();

        // 3. Load pack rules + repo instructions
        String packRulesContent = loadPackRules(packs);
        String repoInstructionsContent = loadRepoInstructions(packs);

        // 4. Load skills content
        String skillsContent = loadSkills(packs);

        // 5. Load MCP info
        McpCatalog catalog = loadMcpCatalog();
        List<String> serverNames = resolveServerNames(catalog, packs);
        String mcpDescription = buildMcpDescription(catalog, serverNames);

        // 6. Build skills description
        String skillsDescription = buildSkillsDescription();

        // 7. Generate AGENTS.md (canonical cross-tool file)
        generateAgentsMd(globalsContent, packRulesContent, repoInstructionsContent,
                mcpDescription, skillsDescription);

        // 8. Generate tool-specific outputs
        if (ctx.outputs.copilot) {
            generateCopilotInstructions();
            generateCopilotPathInstructions(packs);
        }

        if (ctx.outputs.claude) {
            generateClaudeMd();
        }

        if (ctx.outputs.codex) {
            generateCodexConfig(catalog, serverNames);
        }

        // 9. Generate MCP configs
        if (ctx.outputs.vscodeMcp) {
            generateVscodeMcp(catalog, serverNames);
        }

        if (ctx.outputs.claude) {
            generateClaudeMcp(catalog, serverNames);
        }
    }

    // ── Pack Loading ─────────────────────────────────────────────────────────

    private List<PackModel> loadPacks() throws IOException {
        List<PackModel> packs = new ArrayList<>();
        for (String packId : ctx.packs) {
            Path packYaml = contextRoot.resolve("packs/" + packId + "/pack.yaml");
            if (Files.exists(packYaml)) {
                packs.add(PackModel.load(packYaml));
                if (verbose) System.out.println("  Loaded pack: " + packId);
            } else {
                System.err.println("  Warning: pack not found: " + packId);
            }
        }
        return packs;
    }

    // ── Globals ──────────────────────────────────────────────────────────────

    private String loadGlobals() throws IOException {
        StringBuilder sb = new StringBuilder();
        for (String globalId : ctx.globals) {
            Path globalFile = contextRoot.resolve("globals/" + globalId + ".md");
            if (Files.exists(globalFile)) {
                sb.append(Files.readString(globalFile)).append("\n\n");
                if (verbose) System.out.println("  Loaded global: " + globalId);
            } else {
                System.err.println("  Warning: global not found: " + globalId);
            }
        }
        return sb.toString().trim();
    }

    // ── Pack Rules & Repo Instructions ───────────────────────────────────────

    private String loadPackRules(List<PackModel> packs) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (PackModel pack : packs) {
            Path rulesFile = contextRoot.resolve("packs/" + pack.id + "/rules.md");
            if (Files.exists(rulesFile)) {
                sb.append(Files.readString(rulesFile)).append("\n\n");
            }
        }
        return sb.toString().trim();
    }

    private String loadRepoInstructions(List<PackModel> packs) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (PackModel pack : packs) {
            Path file = contextRoot.resolve("packs/" + pack.id + "/repo-instructions.md");
            if (Files.exists(file)) {
                sb.append(Files.readString(file)).append("\n\n");
            }
        }
        return sb.toString().trim();
    }

    // ── Skills ───────────────────────────────────────────────────────────────

    private String loadSkills(List<PackModel> packs) throws IOException {
        StringBuilder sb = new StringBuilder();
        Set<String> loaded = new HashSet<>();
        for (String skillId : ctx.skills) {
            if (loaded.contains(skillId)) continue;
            Path skillFile = findSkillFile(skillId, packs);
            if (skillFile != null && Files.exists(skillFile)) {
                sb.append(Files.readString(skillFile)).append("\n\n");
                loaded.add(skillId);
                if (verbose) System.out.println("  Loaded skill: " + skillId);
            } else {
                System.err.println("  Warning: skill not found: " + skillId);
            }
        }
        return sb.toString().trim();
    }

    private Path findSkillFile(String skillId, List<PackModel> packs) {
        // Check global skills first
        Path global = contextRoot.resolve("skills/" + skillId + ".skill.md");
        if (Files.exists(global)) return global;

        // Check pack skills
        for (PackModel pack : packs) {
            Path packSkill = contextRoot.resolve("packs/" + pack.id + "/skills/" + skillId + ".skill.md");
            if (Files.exists(packSkill)) return packSkill;
        }

        return null;
    }

    private String buildSkillsDescription() {
        if (ctx.skills.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        sb.append("## Available Skills\n\n");
        sb.append("The following skills are available. Use them when the situation matches:\n\n");
        for (String skillId : ctx.skills) {
            sb.append("- **").append(skillId).append("**");
            // Try to read the skill's whenToUse from frontmatter
            String whenToUse = readSkillWhenToUse(skillId);
            if (whenToUse != null) {
                sb.append(": ").append(whenToUse);
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private String readSkillWhenToUse(String skillId) {
        try {
            Path skillFile = findSkillFile(skillId, loadPacks());
            if (skillFile == null) return null;
            String content = Files.readString(skillFile);
            // Simple frontmatter parsing
            if (content.startsWith("---")) {
                int end = content.indexOf("---", 3);
                if (end > 0) {
                    String frontmatter = content.substring(3, end);
                    for (String line : frontmatter.split("\n")) {
                        line = line.trim();
                        if (line.startsWith("whenToUse:")) {
                            return line.substring("whenToUse:".length()).trim();
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    // ── MCP ──────────────────────────────────────────────────────────────────

    private McpCatalog loadMcpCatalog() throws IOException {
        Path catalogPath = contextRoot.resolve("mcp/catalog.yaml");
        if (Files.exists(catalogPath)) {
            return McpCatalog.load(catalogPath);
        }
        return new McpCatalog();
    }

    private List<String> resolveServerNames(McpCatalog catalog, List<PackModel> packs) {
        String toolset = ctx.mcp.toolset;
        // If no toolset specified, try to use pack default
        if (toolset == null) {
            for (PackModel pack : packs) {
                if (pack.defaultMcpToolset != null) {
                    toolset = pack.defaultMcpToolset;
                    break;
                }
            }
        }
        return catalog.resolveServerNames(toolset, ctx.mcp.servers);
    }

    private String buildMcpDescription(McpCatalog catalog, List<String> serverNames) {
        if (serverNames.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        sb.append("## MCP Servers\n\n");
        sb.append("The following MCP servers are configured. Use them when appropriate:\n\n");
        for (String name : serverNames) {
            McpCatalog.McpServer server = catalog.findServer(name);
            if (server != null) {
                sb.append("- **").append(server.name).append("**: ").append(server.description).append("\n");
                sb.append("  - When to use: ").append(server.whenToUse).append("\n");
            } else {
                sb.append("- **").append(name).append("**\n");
            }
        }
        return sb.toString();
    }

    // ── AGENTS.md ────────────────────────────────────────────────────────────

    private void generateAgentsMd(String globals, String packRules,
                                   String repoInstructions, String mcpDesc,
                                   String skillsDesc) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("<!-- ").append(MANAGED_MARKER).append(" -->\n");
        sb.append("# Project Instructions\n\n");
        sb.append("This file is auto-generated by `aictx setup`. Do not edit managed sections manually.\n\n");

        if (!repoInstructions.isEmpty()) {
            sb.append(repoInstructions).append("\n\n");
        }

        if (!packRules.isEmpty()) {
            sb.append(packRules).append("\n\n");
        }

        if (!globals.isEmpty()) {
            sb.append("---\n\n");
            sb.append(globals).append("\n\n");
        }

        if (!mcpDesc.isEmpty()) {
            sb.append("---\n\n");
            sb.append(mcpDesc).append("\n\n");
        }

        if (!skillsDesc.isEmpty()) {
            sb.append("---\n\n");
            sb.append(skillsDesc).append("\n");
        }

        writeFile("AGENTS.md", sb.toString());
    }

    // ── Copilot ──────────────────────────────────────────────────────────────

    private void generateCopilotInstructions() throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("<!-- ").append(MANAGED_MARKER).append(" -->\n");
        sb.append("# Copilot Instructions\n\n");
        sb.append("Refer to [AGENTS.md](../AGENTS.md) for detailed project instructions.\n\n");
        sb.append("## Key Constraints\n\n");

        // Include the most critical rules inline
        for (String packId : ctx.packs) {
            Path rulesFile = contextRoot.resolve("packs/" + packId + "/rules.md");
            if (Files.exists(rulesFile)) {
                String rules = Files.readString(rulesFile);
                // Extract just the first section's bullet points for a concise summary
                String[] lines = rules.split("\n");
                int count = 0;
                for (String line : lines) {
                    if (line.startsWith("- ") && count < 8) {
                        sb.append(line).append("\n");
                        count++;
                    }
                }
            }
        }

        sb.append("\n## MCP Tools & Skills\n\n");
        sb.append("See AGENTS.md for the full list of available MCP servers and skills.\n");

        ensureDir(".github");
        writeFile(".github/copilot-instructions.md", sb.toString());
    }

    private void generateCopilotPathInstructions(List<PackModel> packs) throws IOException {
        ensureDir(".github/instructions");

        for (PackModel pack : packs) {
            for (String pathRule : pack.pathRules) {
                Path ruleFile = contextRoot.resolve("packs/" + pack.id + "/" + pathRule);
                if (Files.exists(ruleFile)) {
                    String content = Files.readString(ruleFile);
                    String filename = Path.of(pathRule).getFileName().toString();
                    writeFile(".github/instructions/" + filename, content);
                }
            }
        }
    }

    // ── Claude ───────────────────────────────────────────────────────────────

    private void generateClaudeMd() throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("<!-- ").append(MANAGED_MARKER).append(" -->\n");
        sb.append("# Claude Code Instructions\n\n");
        sb.append("Read and follow [AGENTS.md](AGENTS.md) for all project rules, conventions, and instructions.\n\n");

        // Pack-specific notes
        for (String packId : ctx.packs) {
            if ("java".equals(packId)) {
                sb.append("## Java Service Notes\n\n");
                sb.append("- Build: `./gradlew build` or `./mvnw package`\n");
                sb.append("- Test: `./gradlew test` or `./mvnw test`\n");
                sb.append("- Format: `./gradlew spotlessApply`\n\n");
            } else if ("flutter".equals(packId)) {
                sb.append("## Flutter Notes\n\n");
                sb.append("- Dependencies: `flutter pub get`\n");
                sb.append("- Test: `flutter test`\n");
                sb.append("- Analyze: `dart analyze`\n");
                sb.append("- Format: `dart format .`\n\n");
            }
        }

        sb.append("## MCP Servers\n\n");
        sb.append("MCP servers are configured in `.mcp.json`. See AGENTS.md for when to use each server.\n");

        writeFile("CLAUDE.md", sb.toString());
    }

    private void generateClaudeMcp(McpCatalog catalog, List<String> serverNames) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"mcpServers\": {\n");

        List<String> entries = new ArrayList<>();
        for (String name : serverNames) {
            McpCatalog.McpServer server = catalog.findServer(name);
            if (server == null) continue;

            StringBuilder entry = new StringBuilder();
            entry.append("    \"").append(name).append("\": {\n");
            entry.append("      \"managedBy\": \"aictx\",\n");

            if ("stdio".equals(server.type)) {
                entry.append("      \"type\": \"stdio\",\n");
                entry.append("      \"command\": \"").append(escapeJson(server.command)).append("\"\n");
            } else {
                entry.append("      \"type\": \"remote\",\n");
                String url = server.url != null ? server.url : "TODO: configure URL";
                entry.append("      \"url\": \"").append(escapeJson(url)).append("\"\n");
            }
            entry.append("    }");
            entries.add(entry.toString());
        }

        sb.append(String.join(",\n", entries)).append("\n");
        sb.append("  }\n");
        sb.append("}\n");

        writeFile(".mcp.json", sb.toString());
    }

    // ── VS Code MCP ──────────────────────────────────────────────────────────

    private void generateVscodeMcp(McpCatalog catalog, List<String> serverNames) throws IOException {
        ensureDir(".vscode");

        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"servers\": {\n");

        List<String> entries = new ArrayList<>();
        for (String name : serverNames) {
            McpCatalog.McpServer server = catalog.findServer(name);
            if (server == null) continue;

            StringBuilder entry = new StringBuilder();
            entry.append("    \"").append(name).append("\": {\n");
            entry.append("      \"managedBy\": \"aictx\",\n");

            if ("stdio".equals(server.type)) {
                entry.append("      \"type\": \"stdio\",\n");
                entry.append("      \"command\": \"").append(escapeJson(server.command)).append("\"\n");
            } else {
                entry.append("      \"type\": \"remote\",\n");
                String url = server.url != null ? server.url : "TODO: configure URL";
                entry.append("      \"url\": \"").append(escapeJson(url)).append("\"\n");
            }
            entry.append("    }");
            entries.add(entry.toString());
        }

        sb.append(String.join(",\n", entries)).append("\n");
        sb.append("  }\n");
        sb.append("}\n");

        writeFile(".vscode/mcp.json", sb.toString());
    }

    // ── Codex ────────────────────────────────────────────────────────────────

    private void generateCodexConfig(McpCatalog catalog, List<String> serverNames) throws IOException {
        ensureDir(".codex");

        StringBuilder sb = new StringBuilder();
        sb.append("# Codex configuration\n");
        sb.append("# ").append(MANAGED_MARKER).append("\n\n");

        if (!serverNames.isEmpty()) {
            sb.append("# MCP servers (authenticate via your tool's sign-in flow)\n");
            for (String name : serverNames) {
                McpCatalog.McpServer server = catalog.findServer(name);
                if (server != null) {
                    sb.append("# ").append(name).append(": ").append(server.description).append("\n");
                }
            }
        }

        sb.append("\n# See AGENTS.md for project instructions and conventions.\n");

        writeFile(".codex/config.toml", sb.toString());
    }

    // ── File I/O ─────────────────────────────────────────────────────────────

    private void writeFile(String relativePath, String content) throws IOException {
        Path target = outputDir.resolve(relativePath);

        if (dryRun) {
            generatedFiles.add("[dry-run] " + relativePath);
            if (verbose) {
                System.out.println("  Would write: " + relativePath + " (" + content.length() + " bytes)");
            }
            return;
        }

        // Check if file exists and is not managed by aictx
        if (Files.exists(target) && !force) {
            String existing = Files.readString(target);
            if (!existing.contains(MANAGED_MARKER)) {
                System.err.println("  Skipped (not managed by aictx, use --force): " + relativePath);
                return;
            }
        }

        Files.writeString(target, content);
        generatedFiles.add(relativePath);
        if (verbose) {
            System.out.println("  Wrote: " + relativePath);
        }
    }

    private void ensureDir(String relativePath) throws IOException {
        Path dir = outputDir.resolve(relativePath);
        if (!dryRun) {
            Files.createDirectories(dir);
        }
    }

    public void printSummary() {
        for (String file : generatedFiles) {
            System.out.println("  " + file);
        }
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
