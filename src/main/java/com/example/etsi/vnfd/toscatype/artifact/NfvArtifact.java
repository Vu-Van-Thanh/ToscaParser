package com.example.etsi.vnfd.toscatype.artifact;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * The keynames every TOSCA artifact definition has, whatever its type.
 *
 * <p>TOSCA Simple Profile YAML 1.3 clause 3.6.7. {@code properties} differs per artifact type and is
 * declared by each subclass.
 *
 * <p>SOL001 puts real VNFD content here rather than in node properties: the software image of a
 * {@code Vdu.OsContainer} (clause 6.8.12.6) and the Helm chart plus its parameter mapping files on
 * an {@code Mciop} (clause 6.8.14.7). Selection is always by artifact <em>type</em> - the name is
 * the VNFD author's choice.
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(of = {"key", "type", "file"})
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class NfvArtifact {

    /** The artifact definition name, e.g. {@code sw_image}. Set by the binder. */
    @JsonIgnore
    private String key;

    /** The ETSI type recognised after walking {@code derived_from}. Set by the binder. */
    @JsonIgnore
    private String etsiType;

    @JsonProperty("type")
    private String type;

    /** The file reference as written, usually relative to the declaring service template. */
    @JsonProperty("file")
    private String file;

    /** The same reference resolved against the package root. Set by the binder. */
    @JsonIgnore
    private String resolvedFile;

    @JsonProperty("repository")
    private String repository;

    @JsonProperty("description")
    private String description;

    @JsonProperty("deploy_path")
    private String deployPath;

    @JsonProperty("artifact_version")
    private String artifactVersion;

    /**
     * TOSCA 1.3 artifact keyname - a bare string beside {@code file}.
     *
     * <p>Not the same thing as the {@code checksum} <em>property</em> of
     * {@code tosca.artifacts.nfv.SwImage} (SOL001 clause 6.3.1), which is a {@code ChecksumData}
     * structure under {@code properties}. Same word, different place.
     */
    @JsonProperty("checksum")
    private String checksum;

    @JsonProperty("checksum_algorithm")
    private String checksumAlgorithm;
}
