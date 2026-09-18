package com.example.etsi.vnfd.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * {@code VirtualLinkBitRateLevel}, IFA011 V5.4.1 clause 7.1.10.5.
 *
 * <p>Bitrate requirements applicable to a virtual link instantiated from a particular
 * {@code VnfVirtualLinkDesc}. Clause 7.1.10.5.2 gives the same element two readings depending on
 * where it sits: "bitrate requirements for an instantiation level or bitrate delta for a scaling
 * step".
 */
public final class VirtualLinkBitRateLevel {

    private final String vnfVirtualLinkDescId;
    private final Map<String, Object> bitrateRequirements;

    private VirtualLinkBitRateLevel(String vnfVirtualLinkDescId,
            Map<String, Object> bitrateRequirements) {
        this.vnfVirtualLinkDescId = vnfVirtualLinkDescId;
        this.bitrateRequirements = bitrateRequirements == null
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(new LinkedHashMap<>(bitrateRequirements));
    }

    public static VirtualLinkBitRateLevel of(String vnfVirtualLinkDescId,
            Map<String, Object> bitrateRequirements) {
        return new VirtualLinkBitRateLevel(vnfVirtualLinkDescId, bitrateRequirements);
    }

    /** The virtual link descriptor these requirements apply to. Mandatory, 1. */
    public String getVnfVirtualLinkDescId() {
        return vnfVirtualLinkDescId;
    }

    /** Mandatory, 1. {@code LinkBitrateRequirements} as the descriptor wrote it. */
    public Map<String, Object> getBitrateRequirements() {
        return bitrateRequirements;
    }

    @Override
    public String toString() {
        return "VirtualLinkBitRateLevel(" + vnfVirtualLinkDescId + ")";
    }
}
