package com.example.etsi.vnfd.model.ext;

import java.util.Objects;
import java.util.Optional;

/**
 * Where the files of one MCIOP actually live in the VNF package.
 *
 * <p>[PROJECT-SPECIFIC] Not part of IFA011. The {@code MciopProfile} information element of IFA011
 * V5.4.1 clause 7.1.8.20 has six attributes and none of them is a file reference:
 * {@code mciopId} "identifies the MCIOP in the VNF package" without locating it.
 *
 * <p>A consumer that has to run {@code helm install {RELEASE} {CHART}} needs the chart path all the
 * same, so it is carried here rather than invented as an extra attribute on the standard element.
 */
public final class MciopArtifacts {

    private final String mciopId;
    private final String packagePath;
    private final String packageArtifactName;
    private final String packageArtifactType;
    private final String paramMappingScriptPath;
    private final String paramMappingRulePath;

    private MciopArtifacts(Builder builder) {
        this.mciopId = Objects.requireNonNull(builder.mciopId, "mciopId");
        this.packagePath = builder.packagePath;
        this.packageArtifactName = builder.packageArtifactName;
        this.packageArtifactType = builder.packageArtifactType;
        this.paramMappingScriptPath = builder.paramMappingScriptPath;
        this.paramMappingRulePath = builder.paramMappingRulePath;
    }

    public static Builder builder(String mciopId) {
        return new Builder(mciopId);
    }

    /** The MciopProfile this belongs to. */
    public String getMciopId() {
        return mciopId;
    }

    /** The Helm chart, resolved against the package root. */
    public Optional<String> getPackagePath() {
        return Optional.ofNullable(packagePath);
    }

    /** The artifact definition name the chart was declared under. */
    public Optional<String> getPackageArtifactName() {
        return Optional.ofNullable(packageArtifactName);
    }

    /** The artifact type, normally {@code tosca.artifacts.nfv.HelmChart}. */
    public Optional<String> getPackageArtifactType() {
        return Optional.ofNullable(packageArtifactType);
    }

    public Optional<String> getParamMappingScriptPath() {
        return Optional.ofNullable(paramMappingScriptPath);
    }

    public Optional<String> getParamMappingRulePath() {
        return Optional.ofNullable(paramMappingRulePath);
    }

    @Override
    public String toString() {
        return mciopId + " -> " + packagePath;
    }

    /** Builder for {@link MciopArtifacts}. */
    public static final class Builder {
        private final String mciopId;
        private String packagePath;
        private String packageArtifactName;
        private String packageArtifactType;
        private String paramMappingScriptPath;
        private String paramMappingRulePath;

        private Builder(String mciopId) {
            this.mciopId = mciopId;
        }

        public Builder packagePath(String value) {
            this.packagePath = value;
            return this;
        }

        public Builder packageArtifactName(String value) {
            this.packageArtifactName = value;
            return this;
        }

        public Builder packageArtifactType(String value) {
            this.packageArtifactType = value;
            return this;
        }

        public Builder paramMappingScriptPath(String value) {
            this.paramMappingScriptPath = value;
            return this;
        }

        public Builder paramMappingRulePath(String value) {
            this.paramMappingRulePath = value;
            return this;
        }

        public MciopArtifacts build() {
            return new MciopArtifacts(this);
        }
    }
}
