import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ContextModel {

    @JsonProperty("schemaVersion")
    public int schemaVersion = 1;

    @JsonProperty("repoType")
    public String repoType;

    @JsonProperty("packs")
    public List<String> packs = new ArrayList<>();

    @JsonProperty("globals")
    public List<String> globals = new ArrayList<>();

    @JsonProperty("skills")
    public List<String> skills = new ArrayList<>();

    @JsonProperty("mcp")
    public McpConfig mcp;

    @JsonProperty("outputs")
    public OutputConfig outputs;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class McpConfig {
        @JsonProperty("toolset")
        public String toolset;

        @JsonProperty("servers")
        public List<String> servers = new ArrayList<>();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OutputConfig {
        @JsonProperty("copilot")
        public boolean copilot = true;

        @JsonProperty("claude")
        public boolean claude = true;

        @JsonProperty("codex")
        public boolean codex = true;

        @JsonProperty("vscodeMcp")
        public boolean vscodeMcp = true;
    }

    public static ContextModel load(Path path) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        ContextModel model = mapper.readValue(path.toFile(), ContextModel.class);
        if (model.mcp == null) model.mcp = new McpConfig();
        if (model.outputs == null) model.outputs = new OutputConfig();
        if (model.packs == null) model.packs = new ArrayList<>();
        if (model.globals == null) model.globals = new ArrayList<>();
        if (model.skills == null) model.skills = new ArrayList<>();
        if (model.mcp.servers == null) model.mcp.servers = new ArrayList<>();
        return model;
    }
}
