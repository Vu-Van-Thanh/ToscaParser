package com.example.etsi.vnfd.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * {@code InstantiationLevel}, IFA011 V5.4.1 clause 7.1.8.7.
 *
 * <p>Note the attribute names, which are easy to get wrong. The identifier is {@code levelId}, not
 * {@code instantiationLevelId}; the latter is the name of the referencing attribute on
 * {@code VnfDf}, which is {@code defaultInstantiationLevelId}.
 *
 * <p>Both {@code description} and {@code vduLevel} are mandatory, and {@code vduLevel} has
 * cardinality 1..N. A level carrying neither is not a valid information element, which matters when
 * one has to be synthesised for a descriptor that declares no instantiation level policy.
 */
public final class InstantiationLevel {

    private final String levelId;
    private final String description;
    private final List<VduLevel> vduLevel;
    private final List<ScaleInfo> scaleInfo;
    private final List<VirtualLinkBitRateLevel> virtualLinkBitRateLevel;
    private final boolean synthesised;

    private InstantiationLevel(Builder builder) {
        this.levelId = Objects.requireNonNull(builder.levelId, "levelId");
        this.description = builder.description;
        this.vduLevel = Collections.unmodifiableList(new ArrayList<>(builder.vduLevel));
        this.scaleInfo = Collections.unmodifiableList(new ArrayList<>(builder.scaleInfo));
        this.virtualLinkBitRateLevel =
                Collections.unmodifiableList(new ArrayList<>(builder.virtualLinkBitRateLevel));
        this.synthesised = builder.synthesised;
    }

    public static Builder builder(String levelId) {
        return new Builder(levelId);
    }

    /** Mandatory. Uniquely identifies a level within the deployment flavour. */
    public String getLevelId() {
        return levelId;
    }

    /** Mandatory. */
    public String getDescription() {
        return description;
    }

    /** Mandatory, 1..N. Instance count of each VDU at this level. */
    public List<VduLevel> getVduLevel() {
        return vduLevel;
    }

    /** Scale level per aspect. Present when the VNF supports scaling. */
    public List<ScaleInfo> getScaleInfo() {
        return scaleInfo;
    }

    /**
     * Bitrate requirements for the virtual links at this level.
     *
     * <p>IFA011 V5.4.1 clause 7.1.8.7.2: {@code virtualLinkBitRateLevel}, M,0..N.
     */
    public List<VirtualLinkBitRateLevel> getVirtualLinkBitRateLevel() {
        return virtualLinkBitRateLevel;
    }

    /**
     * Whether this level was created by the parser rather than declared in the descriptor.
     *
     * <p>[MANO INTERPRETATION] IFA011 clause 7.1.8.2.2 requires at least one instantiation level,
     * but SOL001 expresses levels through optional policies. A descriptor that declares none still
     * has to yield a valid VNFD, so one level is synthesised - and flagged, so a consumer is never
     * misled into thinking the VNF provider stated it.
     */
    public boolean isSynthesised() {
        return synthesised;
    }

    @Override
    public String toString() {
        return levelId + vduLevel + (synthesised ? " (synthesised)" : "");
    }

    /** Builder for {@link InstantiationLevel}. */
    public static final class Builder {
        private final String levelId;
        private String description;
        private final List<VduLevel> vduLevel = new ArrayList<>();
        private final List<ScaleInfo> scaleInfo = new ArrayList<>();
        private final List<VirtualLinkBitRateLevel> virtualLinkBitRateLevel = new ArrayList<>();
        private boolean synthesised;

        private Builder(String levelId) {
            this.levelId = levelId;
        }

        public Builder description(String value) {
            this.description = value;
            return this;
        }

        public Builder addVduLevel(VduLevel value) {
            this.vduLevel.add(value);
            return this;
        }

        public Builder addVirtualLinkBitRateLevel(VirtualLinkBitRateLevel value) {
            virtualLinkBitRateLevel.add(value);
            return this;
        }

        public Builder addScaleInfo(ScaleInfo value) {
            this.scaleInfo.add(value);
            return this;
        }

        public Builder synthesised(boolean value) {
            this.synthesised = value;
            return this;
        }

        public InstantiationLevel build() {
            return new InstantiationLevel(this);
        }
    }
}
