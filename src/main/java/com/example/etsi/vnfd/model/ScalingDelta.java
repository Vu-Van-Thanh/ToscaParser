package com.example.etsi.vnfd.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@code ScalingDelta}, IFA011 V5.4.1 clause 7.1.10.4.
 *
 * <p>Clause 7.1.10.4.1: "the number of VNFC instances per VDU, the number of VIP CP instances and/or
 * the bitrate delta per virtual link that corresponds to a single scaling step for a particular
 * scaling aspect. When scaling out by one step, this delta is added to the resources of the VNF
 * instance, whereas when scaling in, this delta is removed." The same element also carries the
 * minimum size of the VNF, through {@code VnfDf.initialDelta}.
 *
 * <p>NOTE 1 of Table 7.1.10.4.2-1 requires at least one of {@code vduDelta} and
 * {@code virtualLinkBitRateDelta} - recorded here, not enforced, since a descriptor that breaks it
 * is reported rather than rejected.
 */
public final class ScalingDelta {

    private final String scalingDeltaId;
    private final List<VduLevel> vduDelta;
    private final List<VirtualLinkBitRateLevel> virtualLinkBitRateDelta;

    private ScalingDelta(Builder builder) {
        this.scalingDeltaId = builder.scalingDeltaId;
        this.vduDelta = Collections.unmodifiableList(new ArrayList<>(builder.vduDelta));
        this.virtualLinkBitRateDelta =
                Collections.unmodifiableList(new ArrayList<>(builder.virtualLinkBitRateDelta));
    }

    public static Builder builder(String scalingDeltaId) {
        return new Builder(scalingDeltaId);
    }

    /** Identifier of this scaling delta. Mandatory, 1. The key of the SOL001 {@code deltas} map. */
    public String getScalingDeltaId() {
        return scalingDeltaId;
    }

    /** VNFC instances per VDU created or removed by one step. 0..N. */
    public List<VduLevel> getVduDelta() {
        return vduDelta;
    }

    /** Bitrate added to or removed from virtual links by one step. 0..N. */
    public List<VirtualLinkBitRateLevel> getVirtualLinkBitRateDelta() {
        return virtualLinkBitRateDelta;
    }

    @Override
    public String toString() {
        return "ScalingDelta(" + scalingDeltaId + ", vduDelta=" + vduDelta.size() + ")";
    }

    /** Builder for {@link ScalingDelta}. */
    public static final class Builder {
        private final String scalingDeltaId;
        private final List<VduLevel> vduDelta = new ArrayList<>();
        private final List<VirtualLinkBitRateLevel> virtualLinkBitRateDelta = new ArrayList<>();

        private Builder(String scalingDeltaId) {
            this.scalingDeltaId = scalingDeltaId;
        }

        public Builder addVduDelta(VduLevel value) {
            vduDelta.add(value);
            return this;
        }

        public Builder addVirtualLinkBitRateDelta(VirtualLinkBitRateLevel value) {
            virtualLinkBitRateDelta.add(value);
            return this;
        }

        public ScalingDelta build() {
            return new ScalingDelta(this);
        }
    }
}
