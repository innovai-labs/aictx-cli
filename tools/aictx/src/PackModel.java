import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PackModel {

    @JsonProperty("id")
    public String id;

    @JsonProperty("description")
    public String description;

    @JsonProperty("appliesToRepoTypes")
    public List<String> appliesToRepoTypes = new ArrayList<>();

    @JsonProperty("defaultGlobals")
    public List<String> defaultGlobals = new ArrayList<>();

    @JsonProperty("defaultSkills")
    public List<String> defaultSkills = new ArrayList<>();

    @JsonProperty("defaultMcpToolset")
    public String defaultMcpToolset;

    @JsonProperty("pathRules")
    public List<String> pathRules = new ArrayList<>();

    public static PackModel load(Path path) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return mapper.readValue(path.toFile(), PackModel.class);
    }
}
