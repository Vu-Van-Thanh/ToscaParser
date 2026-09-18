package com.example.etsi.vnfd.toscatype.node;

import com.example.etsi.vnfd.template.ArtifactDefinition;
import com.example.etsi.vnfd.template.InterfaceAssignment;
import com.example.etsi.vnfd.template.NodeFilter;
import com.example.etsi.vnfd.template.SourceRef;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * The keynames every TOSCA node template has, whatever its type.
 *
 * <p>TOSCA Simple Profile YAML 1.3 clause 3.8.1 gives a node template twelve keynames. Nine of them
 * have the same shape for every type and live here. The other three -
 * {@code properties}, {@code requirements} and {@code capabilities} - carry a different structure
 * per node type and are declared by each subclass: {@code Mciop} has no properties at all
 * (SOL001 V5.4.1 clause 6.8.14.2), {@code VNF} has {@code descriptor_id}, {@code VduCp} has
 * {@code layer_protocols}. Nothing useful could be said about them here.
 *
 * <p>This is a value holder bound straight from the declaration, not a view over one. A node
 * template <em>is</em> a node object; there is no second representation to keep in step.
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(of = {"key", "type"})
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class NfvNode {

    /**
     * The node template name, i.e. its key under {@code node_templates}. Set by the binder.
     *
     * <p>Carries meaning beyond identification: SOL001 clause 6.8.12.6 states that the node template
     * name of a {@code Vdu.OsContainer} "fulfils the purpose of the 'id' attribute of the
     * SwImageDesc information element".
     */
    @JsonIgnore
    private String key;

    /**
     * The ETSI type this node was recognised as, after walking {@code derived_from}. Set by the
     * binder, and usually different from {@link #type}: clause 6.11.2 requires the VNF node type to
     * be VNF-specific, so a descriptor writes {@code MyCompany.ExampleVNF}, not
     * {@code tosca.nodes.nfv.VNF}.
     */
    @JsonIgnore
    private String etsiType;

    /**
     * File and element this node was declared in. Set by the binder, so that a finding raised while
     * mapping can name the descriptor a reader has to open, not just the node.
     */
    @JsonIgnore
    private SourceRef source;

    /** The type as declared, which may be a vendor type derived from an ETSI one. */
    @JsonProperty("type")
    private String type;

    @JsonProperty("description")
    private String description;

    @JsonProperty("metadata")
    private Map<String, Object> metadata;

    /** SOL001 clause 6.11.2 d): a top-level VNF node template may carry {@code substitute}. */
    @JsonProperty("directives")
    private List<String> directives;

    /** {@code tosca.nodes.nfv.VNF} declares {@code scale_status} here. */
    @JsonProperty("attributes")
    private Map<String, Object> attributes;

    @JsonProperty("interfaces")
    private Map<String, InterfaceAssignment> interfaces;

    /**
     * Artifacts attached to the node template, keyed by the name its author chose.
     *
     * <p>SOL001 always speaks of the artifact <em>type</em> - clause 6.8.12.6 asks for "an artifact
     * of type tosca.artifacts.nfv.SwImage", clause 6.8.14.7 caps each type on an {@code Mciop} at
     * one - while the name is free. Selection by type is therefore a separate step, not a lookup
     * in this map.
     */
    @JsonProperty("artifacts")
    private Map<String, ArtifactDefinition> artifacts;

    /** TOSCA 1.3 keyname; SOL001 never uses it. Kept so a package carrying one is not silently altered. */
    @JsonProperty("node_filter")
    private NodeFilter nodeFilter;

    /** TOSCA 1.3 keyname; SOL001 never uses it. Kept for the same reason as {@link #nodeFilter}. */
    @JsonProperty("copy")
    private String copy;
}
