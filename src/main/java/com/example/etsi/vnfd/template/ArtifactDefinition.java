package com.example.etsi.vnfd.template;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * An artifact attached to a node template.
 *
 * <p>Artifacts carry information the VNFD genuinely needs, not just payload: SOL001 V5.4.1 clause
 * 6.8.12.6 puts the software image of a {@code Vdu.OsContainer} here and states that the node
 * template name "fulfils the purpose of the 'id' attribute of the SwImageDesc information element";
 * clause 6.8.14.7 puts the Helm chart and its parameter mapping files on an {@code Mciop}.
 *
 * <p>{@link #file()} is the path exactly as written, usually relative to the declaring service
 * template; {@link #resolvedFile()} holds it resolved against the package root once the reader has
 * done so.
 */
public final class ArtifactDefinition {

    private final String name;
    private String type;
    private String file;
    private String resolvedFile;
    private String repository;
    private String description;
    private String deployPath;
    private String artifactVersion;
    private String checksum;
    private String checksumAlgorithm;
    private final Map<String, Object> properties = new LinkedHashMap<>();
    private SourceRef source;

    public ArtifactDefinition(String name) {
        this.name = name;
    }

    /** Artifact definition name, e.g. {@code sw_image}, {@code web_helm_chart}. */
    public String name() {
        return name;
    }

    /** Artifact type, e.g. {@code tosca.artifacts.nfv.HelmChart}. */
    public String type() {
        return type;
    }

    /** The path as written in the descriptor, e.g. {@code ../Artifacts/Charts/chart.tgz}. */
    public String file() {
        return file;
    }

    /**
     * The path resolved against the package root, e.g. {@code Artifacts/Charts/chart.tgz}.
     * Empty until the reader resolves it.
     */
    public Optional<String> resolvedFile() {
        return Optional.ofNullable(resolvedFile);
    }

    public Optional<String> repository() {
        return Optional.ofNullable(repository);
    }

    public Optional<String> description() {
        return Optional.ofNullable(description);
    }

    public Optional<String> deployPath() {
        return Optional.ofNullable(deployPath);
    }

    public Optional<String> artifactVersion() {
        return Optional.ofNullable(artifactVersion);
    }

    /**
     * The TOSCA {@code checksum} keyname of the artifact definition.
     *
     * <p>Not to be confused with the {@code checksum} <em>property</em> of
     * {@code tosca.artifacts.nfv.SwImage} (SOL001 V5.4.1 clause 6.3.1), which is a
     * {@code ChecksumData} structure found in {@link #properties()}. Same word, different place,
     * different shape.
     */
    public Optional<String> checksum() {
        return Optional.ofNullable(checksum);
    }

    public Optional<String> checksumAlgorithm() {
        return Optional.ofNullable(checksumAlgorithm);
    }

    /** Artifact property assignments, e.g. the {@code language} of a HelmParamMappingScript. */
    public Map<String, Object> properties() {
        return properties;
    }

    public SourceRef source() {
        return source;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public void setResolvedFile(String resolvedFile) {
        this.resolvedFile = resolvedFile;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDeployPath(String deployPath) {
        this.deployPath = deployPath;
    }

    public void setArtifactVersion(String artifactVersion) {
        this.artifactVersion = artifactVersion;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public void setChecksumAlgorithm(String checksumAlgorithm) {
        this.checksumAlgorithm = checksumAlgorithm;
    }

    public void setSource(SourceRef source) {
        this.source = source;
    }

    @Override
    public String toString() {
        return name + "(" + type + "): " + file;
    }
}
