import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

public class VersionUtil {

    /**
     * Attempt to resolve the context root from the JBang cache or relative path.
     * When installed via JBang git, the repo is cloned and the context dir is at ../../context
     * relative to the tools/aictx directory. We try multiple strategies.
     */
    public static Path resolveContextRoot() {
        // Strategy 1: AICTX_CONTEXT_ROOT env var (for development)
        String envRoot = System.getenv("AICTX_CONTEXT_ROOT");
        if (envRoot != null) {
            Path p = Path.of(envRoot);
            if (Files.isDirectory(p)) return p;
        }

        // Strategy 2: relative to this source file's location (JBang clones the repo)
        // When JBang runs from git, the working dir is typically the JBang cache
        // but the source is in the cloned repo. We can detect via system property.
        String jbangDir = System.getProperty("jbang.dir");
        if (jbangDir != null) {
            // jbang.dir points to the directory containing the script
            Path scriptDir = Path.of(jbangDir);
            Path contextDir = scriptDir.resolve("../../context").normalize();
            if (Files.isDirectory(contextDir)) return contextDir;
        }

        // Strategy 3: check relative to CWD (for local development: jbang tools/aictx/aictx.java)
        Path localContext = Path.of("context");
        if (Files.isDirectory(localContext)) return localContext.toAbsolutePath();

        // Strategy 4: check relative to the script location via class resource
        try {
            Path classLocation = Path.of(VersionUtil.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
            // Navigate up from build/cache dir
            Path candidate = classLocation.getParent();
            for (int i = 0; i < 6; i++) {
                if (candidate == null) break;
                Path ctx = candidate.resolve("context");
                if (Files.isDirectory(ctx) && Files.exists(ctx.resolve("globals"))) {
                    return ctx;
                }
                candidate = candidate.getParent();
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    /**
     * Get the git ref of the installed version (if available from JBang metadata).
     */
    public static String getGitRef() {
        // Try to read from a .version file that could be generated during build
        try {
            Path versionFile = Path.of(System.getProperty("jbang.dir", "."), ".version");
            if (Files.exists(versionFile)) {
                return Files.readString(versionFile).trim();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * Fetch the latest SemVer tag from git remote.
     * Returns null if git is not available or remote is unreachable.
     */
    public static String fetchLatestTag() {
        try {
            // Try to get the remote URL from the script's repo
            ProcessBuilder pb = new ProcessBuilder("git", "ls-remote", "--tags", "--sort=-v:refname",
                    "origin");
            pb.redirectErrorStream(true);
            Process p = pb.start();
            String output = new String(p.getInputStream().readAllBytes());
            int exit = p.waitFor();

            if (exit != 0) {
                // Try without origin (may not be in a git context)
                return null;
            }

            return parseLatestTag(output);
        } catch (Exception e) {
            return null;
        }
    }

    static String parseLatestTag(String lsRemoteOutput) {
        Pattern tagPattern = Pattern.compile("refs/tags/(v\\d+\\.\\d+\\.\\d+)$", Pattern.MULTILINE);
        Matcher m = tagPattern.matcher(lsRemoteOutput);

        String latest = null;
        int[] latestParts = {0, 0, 0};

        while (m.find()) {
            String tag = m.group(1);
            int[] parts = parseSemVer(tag);
            if (parts != null && compareSemVer(parts, latestParts) > 0) {
                latest = tag;
                latestParts = parts;
            }
        }
        return latest;
    }

    static int[] parseSemVer(String tag) {
        Matcher m = Pattern.compile("v?(\\d+)\\.(\\d+)\\.(\\d+)").matcher(tag);
        if (m.matches()) {
            return new int[]{
                    Integer.parseInt(m.group(1)),
                    Integer.parseInt(m.group(2)),
                    Integer.parseInt(m.group(3))
            };
        }
        return null;
    }

    static int compareSemVer(int[] a, int[] b) {
        for (int i = 0; i < 3; i++) {
            if (a[i] != b[i]) return Integer.compare(a[i], b[i]);
        }
        return 0;
    }

    /**
     * Print an update hint if a newer version is available. Used by version command.
     */
    public static void printUpdateHintIfAvailable() {
        try {
            String latest = fetchLatestTag();
            if (latest != null) {
                String current = "v" + aictx.VERSION;
                if (!current.equals(latest)) {
                    int[] currentParts = parseSemVer(current);
                    int[] latestParts = parseSemVer(latest);
                    if (currentParts != null && latestParts != null && compareSemVer(latestParts, currentParts) > 0) {
                        System.out.println("\nUpdate available: " + current + " → " + latest);
                        System.out.println("Run: aictx upgrade");
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }
}
