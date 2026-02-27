///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 17+
//DEPS info.picocli:picocli:4.7.6
//DEPS com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.17.2
//DEPS com.fasterxml.jackson.core:jackson-databind:2.17.2
//SOURCES src/ContextModel.java
//SOURCES src/PackModel.java
//SOURCES src/McpCatalog.java
//SOURCES src/TemplateEngine.java
//SOURCES src/Generator.java
//SOURCES src/VersionUtil.java

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.HelpCommand;

import java.io.*;
import java.nio.file.*;
import java.util.concurrent.Callable;

@Command(name = "aictx",
        mixinStandardHelpOptions = true,
        version = "aictx 0.1.0",
        description = "AI Context CLI — generates tool-specific instruction files and MCP configs from a central context library.",
        subcommands = {
                HelpCommand.class,
                AictxVersion.class,
                AictxInit.class,
                AictxSetup.class,
                AictxUpgrade.class,
                AictxUpdateCheck.class
        })
public class aictx implements Callable<Integer> {

    public static final String VERSION = "0.1.0";
    public static final int SCHEMA_VERSION = 1;

    @Override
    public Integer call() {
        CommandLine.usage(this, System.out);
        return 0;
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new aictx()).execute(args);
        System.exit(exitCode);
    }
}

// ── version ──────────────────────────────────────────────────────────────────

@Command(name = "version", description = "Print CLI version and context info")
class AictxVersion implements Callable<Integer> {

    @Override
    public Integer call() {
        System.out.println("aictx version " + aictx.VERSION);
        System.out.println("Schema version: " + aictx.SCHEMA_VERSION);
        String gitRef = VersionUtil.getGitRef();
        if (gitRef != null) {
            System.out.println("Git ref: " + gitRef);
        }
        // Check for updates
        VersionUtil.printUpdateHintIfAvailable();
        return 0;
    }
}

// ── init ─────────────────────────────────────────────────────────────────────

@Command(name = "init", description = "Create aictx-context.yaml in the current directory")
class AictxInit implements Callable<Integer> {

    @Option(names = {"--repo-type"}, description = "Preselect repo type: java-service, flutter-app")
    String repoType;

    @Option(names = {"--force"}, description = "Overwrite existing config")
    boolean force;

    @Override
    public Integer call() throws Exception {
        Path target = Path.of("aictx-context.yaml");
        if (Files.exists(target) && !force) {
            System.err.println("aictx-context.yaml already exists. Use --force to overwrite.");
            return 1;
        }

        String template = getInitTemplate(repoType);
        Files.writeString(target, template);
        System.out.println("Created aictx-context.yaml");
        System.out.println("Next: edit the file to select your pack and options, then run: aictx setup");
        return 0;
    }

    private String getInitTemplate(String repoType) {
        boolean isJava = "java-service".equals(repoType);
        boolean isFlutter = "flutter-app".equals(repoType);
        boolean neither = !isJava && !isFlutter;

        StringBuilder sb = new StringBuilder();
        sb.append("# aictx context configuration\n");
        sb.append("# Uncomment the options you want, then run: aictx setup\n");
        sb.append("schemaVersion: 1\n\n");

        // repoType
        if (isJava) {
            sb.append("repoType: java-service\n");
        } else if (isFlutter) {
            sb.append("repoType: flutter-app\n");
        } else {
            sb.append("# repoType: java-service\n");
            sb.append("# repoType: flutter-app\n");
        }
        sb.append("\n");

        // packs
        sb.append("packs:\n");
        sb.append(isJava || neither ? (isJava ? "  - java\n" : "  # - java\n") : "  # - java\n");
        sb.append(isFlutter || neither ? (isFlutter ? "  - flutter\n" : "  # - flutter\n") : "  # - flutter\n");
        sb.append("\n");

        // globals
        sb.append("globals:\n");
        sb.append("  - pr-review\n");
        sb.append("  - testing\n");
        sb.append("  # - security\n");
        sb.append("\n");

        // skills
        sb.append("skills:\n");
        sb.append("  - pr-review-checklist\n");
        if (isJava || neither) {
            sb.append(isJava ? "  - java-service-skeleton\n" : "  # - java-service-skeleton\n");
        }
        if (isFlutter || neither) {
            sb.append(isFlutter ? "  - flutter-feature-patterns\n" : "  # - flutter-feature-patterns\n");
        }
        sb.append("\n");

        // mcp
        sb.append("mcp:\n");
        if (isJava) {
            sb.append("  toolset: backend-default\n");
        } else if (isFlutter) {
            sb.append("  toolset: frontend-default\n");
        } else {
            sb.append("  # toolset: backend-default\n");
            sb.append("  # toolset: frontend-default\n");
        }
        sb.append("  # servers:\n");
        sb.append("  #   - internal-docs\n");
        sb.append("  #   - service-catalog\n");
        sb.append("  #   - runbooks\n");
        sb.append("\n");

        // outputs
        sb.append("outputs:\n");
        sb.append("  copilot: true\n");
        sb.append("  claude: true\n");
        sb.append("  codex: true\n");
        sb.append("  vscodeMcp: true\n");

        return sb.toString();
    }
}

// ── setup ────────────────────────────────────────────────────────────────────

@Command(name = "setup", description = "Generate instruction files and MCP configs from aictx-context.yaml")
class AictxSetup implements Callable<Integer> {

    @Option(names = {"--config"}, description = "Path to config file", defaultValue = "aictx-context.yaml")
    String configPath;

    @Option(names = {"--dry-run"}, description = "Print what would change without writing")
    boolean dryRun;

    @Option(names = {"--force"}, description = "Overwrite managed sections")
    boolean force;

    @Option(names = {"--verbose"}, description = "Print detailed output")
    boolean verbose;

    @Override
    public Integer call() throws Exception {
        Path config = Path.of(configPath);
        if (!Files.exists(config)) {
            System.err.println("Config not found: " + configPath);
            System.err.println("Run 'aictx init' first to create a config file.");
            return 1;
        }

        ContextModel ctx;
        try {
            ctx = ContextModel.load(config);
        } catch (Exception e) {
            System.err.println("Failed to parse config: " + e.getMessage());
            return 1;
        }

        if (ctx.schemaVersion > aictx.SCHEMA_VERSION) {
            System.err.println("Config schema version " + ctx.schemaVersion + " is newer than supported (" + aictx.SCHEMA_VERSION + ").");
            System.err.println("Run 'aictx upgrade' to get the latest CLI.");
            return 1;
        }

        Path contextRoot = VersionUtil.resolveContextRoot();
        if (contextRoot == null) {
            System.err.println("Cannot locate context library. Ensure aictx is installed via JBang from the git repo.");
            return 1;
        }

        Generator gen = new Generator(ctx, contextRoot, dryRun, force, verbose);
        gen.generate();

        if (!dryRun) {
            System.out.println("\nSetup complete. Generated files:");
        } else {
            System.out.println("\nDry run complete. Would generate:");
        }
        gen.printSummary();

        System.out.println("\nNext steps:");
        System.out.println("  - Review generated files and commit them");
        System.out.println("  - Authenticate MCP servers in VS Code / Claude / Codex if prompted");
        return 0;
    }
}

// ── upgrade ──────────────────────────────────────────────────────────────────

@Command(name = "upgrade", description = "Upgrade aictx to the latest version")
class AictxUpgrade implements Callable<Integer> {

    @Option(names = {"--execute"}, description = "Execute the upgrade command (default: just print it)")
    boolean execute;

    @Override
    public Integer call() throws Exception {
        String latestTag = VersionUtil.fetchLatestTag();
        if (latestTag == null) {
            System.out.println("Could not determine latest version.");
            System.out.println("Manual upgrade:");
            System.out.println("  jbang app install --fresh --force --name aictx \\");
            System.out.println("    \"git+ssh://git@<git-host>/<org>/aictx-cli.git#subdirectory=tools/aictx&ref=<version>\"");
            return 1;
        }

        String currentVersion = "v" + aictx.VERSION;
        if (currentVersion.equals(latestTag)) {
            System.out.println("Already at latest version: " + latestTag);
            return 0;
        }

        String cmd = "jbang app install --fresh --force --name aictx " +
                "\"git+ssh://git@<git-host>/<org>/aictx-cli.git#subdirectory=tools/aictx&ref=" + latestTag + "\"";

        if (execute) {
            System.out.println("Upgrading to " + latestTag + "...");
            ProcessBuilder pb = new ProcessBuilder("sh", "-c", cmd);
            pb.inheritIO();
            Process p = pb.start();
            int exitCode = p.waitFor();
            if (exitCode == 0) {
                System.out.println("Upgraded to " + latestTag);
            } else {
                System.err.println("Upgrade failed (exit code " + exitCode + ")");
            }
            return exitCode;
        } else {
            System.out.println("Update available: " + currentVersion + " → " + latestTag);
            System.out.println("Run this command to upgrade:");
            System.out.println("  " + cmd);
            System.out.println("\nOr run: aictx upgrade --execute");
            return 0;
        }
    }
}

// ── update-check ─────────────────────────────────────────────────────────────

@Command(name = "update-check", description = "Check if a newer version is available")
class AictxUpdateCheck implements Callable<Integer> {

    @Override
    public Integer call() {
        String latestTag = VersionUtil.fetchLatestTag();
        if (latestTag == null) {
            System.out.println("Could not check for updates (git not available or remote unreachable).");
            return 1;
        }

        String currentVersion = "v" + aictx.VERSION;
        if (currentVersion.equals(latestTag)) {
            System.out.println("Up to date: " + currentVersion);
        } else {
            System.out.println("Update available: " + currentVersion + " → " + latestTag);
            System.out.println("Run: aictx upgrade");
        }
        return 0;
    }
}
