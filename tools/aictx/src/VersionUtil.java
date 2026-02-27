import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class VersionUtil {

    static final String GITHUB_REPO = "innovai-labs/aictx-cli";
    static final String GITHUB_BRANCH = "main";

    public static Path getContextCacheDir() {
        return Path.of(System.getProperty("user.home"), ".aictx", "context");
    }

    /**
     * Resolve the context root using multiple strategies:
     * 1. AICTX_CONTEXT_ROOT env var (explicit override)
     * 2. ./context relative to CWD (local development from repo root)
     * 3. ~/.aictx/context/ cache (auto-downloaded from GitHub)
     */
    public static Path resolveContextRoot() {
        // Strategy 1: AICTX_CONTEXT_ROOT env var (for development / explicit override)
        String envRoot = System.getenv("AICTX_CONTEXT_ROOT");
        if (envRoot != null) {
            Path p = Path.of(envRoot);
            if (Files.isDirectory(p)) return p;
        }

        // Strategy 2: check relative to CWD (for local development: jbang tools/aictx/aictx.java)
        Path localContext = Path.of("context");
        if (Files.isDirectory(localContext) && Files.exists(localContext.resolve("globals"))) {
            return localContext.toAbsolutePath();
        }

        // Strategy 3: cached context at ~/.aictx/context/
        Path cachedContext = getContextCacheDir();
        if (isValidContextDir(cachedContext)) {
            return cachedContext;
        }

        // Strategy 4: auto-download from GitHub
        try {
            System.out.println("Context library not found locally. Downloading from GitHub...");
            downloadContext(cachedContext);
            if (isValidContextDir(cachedContext)) {
                System.out.println("Context library cached at " + cachedContext);
                return cachedContext;
            }
        } catch (Exception e) {
            System.err.println("Failed to download context library: " + e.getMessage());
        }

        return null;
    }

    static boolean isValidContextDir(Path dir) {
        return Files.isDirectory(dir) && Files.exists(dir.resolve("globals"));
    }

    /**
     * Download the context directory from the GitHub repo archive and extract it to targetDir.
     */
    public static void downloadContext(Path targetDir) throws Exception {
        String url = "https://github.com/" + GITHUB_REPO + "/archive/refs/heads/" + GITHUB_BRANCH + ".zip";

        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();
        HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

        if (response.statusCode() != 200) {
            throw new IOException("HTTP " + response.statusCode() + " fetching context library from " + url);
        }

        // Clean target dir if it exists (for updates)
        if (Files.isDirectory(targetDir)) {
            try (var walk = Files.walk(targetDir)) {
                walk.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
            }
        }
        Files.createDirectories(targetDir);

        try (ZipInputStream zis = new ZipInputStream(response.body())) {
            ZipEntry entry;
            String contextPrefix = null;

            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();

                // Detect the context/ prefix from the archive (e.g., "aictx-cli-main/context/")
                if (contextPrefix == null) {
                    int slash = name.indexOf('/');
                    if (slash > 0) {
                        contextPrefix = name.substring(0, slash + 1) + "context/";
                    }
                }

                if (contextPrefix == null || !name.startsWith(contextPrefix)) {
                    zis.closeEntry();
                    continue;
                }

                String relativePath = name.substring(contextPrefix.length());
                if (relativePath.isEmpty()) {
                    zis.closeEntry();
                    continue;
                }

                Path outPath = targetDir.resolve(relativePath);
                if (entry.isDirectory()) {
                    Files.createDirectories(outPath);
                } else {
                    Files.createDirectories(outPath.getParent());
                    Files.copy(zis, outPath, StandardCopyOption.REPLACE_EXISTING);
                }
                zis.closeEntry();
            }
        }
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
