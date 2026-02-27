import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class McpCatalog {

    @JsonProperty("servers")
    public List<McpServer> servers = new ArrayList<>();

    @JsonProperty("toolsets")
    public List<McpToolset> toolsets = new ArrayList<>();

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class McpServer {
        @JsonProperty("name")
        public String name;

        @JsonProperty("description")
        public String description;

        @JsonProperty("url")
        public String url;

        @JsonProperty("command")
        public String command;

        @JsonProperty("whenToUse")
        public String whenToUse;

        @JsonProperty("type")
        public String type; // "remote" or "stdio"
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class McpToolset {
        @JsonProperty("name")
        public String name;

        @JsonProperty("description")
        public String description;

        @JsonProperty("recommendedForRepoTypes")
        public List<String> recommendedForRepoTypes = new ArrayList<>();

        @JsonProperty("servers")
        public List<String> servers = new ArrayList<>();
    }

    public static McpCatalog load(Path path) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return mapper.readValue(path.toFile(), McpCatalog.class);
    }

    public McpServer findServer(String name) {
        return servers.stream()
                .filter(s -> s.name.equals(name))
                .findFirst()
                .orElse(null);
    }

    public McpToolset findToolset(String name) {
        return toolsets.stream()
                .filter(t -> t.name.equals(name))
                .findFirst()
                .orElse(null);
    }

    /**
     * Resolve the list of server names from toolset + explicit servers.
     */
    public List<String> resolveServerNames(String toolsetName, List<String> explicitServers) {
        Set<String> names = new LinkedHashSet<>();
        if (toolsetName != null) {
            McpToolset ts = findToolset(toolsetName);
            if (ts != null) {
                names.addAll(ts.servers);
            }
        }
        if (explicitServers != null) {
            names.addAll(explicitServers);
        }
        return new ArrayList<>(names);
    }
}
