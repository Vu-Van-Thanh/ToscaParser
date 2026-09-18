package com.example.etsi.vnfd.model;

import com.example.etsi.vnfd.template.value.PropertyValue;
import java.util.Optional;

/**
 * {@code Subport}, IFA011 V5.4.1 clause 7.1.6.12.
 *
 * <p>Clause 7.1.6.12.1: "the information used for the subport of a trunk parent port". Comes from a
 * {@code tosca.nodes.nfv.VduSubCp} node template, SOL001 V5.4.1 clause 6.8.11, whose
 * {@code trunk_binding} requirement names the parent.
 */
public final class Subport {

    private final String subportCpd;
    private final PropertyValue<String> segmentationType;
    private final PropertyValue<Integer> segmentationId;

    private Subport(String subportCpd, PropertyValue<String> segmentationType,
            PropertyValue<Integer> segmentationId) {
        this.subportCpd = subportCpd;
        this.segmentationType = segmentationType;
        this.segmentationId = segmentationId;
    }

    public static Subport of(String subportCpd, PropertyValue<String> segmentationType,
            PropertyValue<Integer> segmentationId) {
        return new Subport(subportCpd, segmentationType, segmentationId);
    }

    /** The internal VDU CPD instantiating this subport. Mandatory, 1. */
    public String getSubportCpd() {
        return subportCpd;
    }

    /**
     * Encapsulation type for traffic through this subport. 0..1.
     *
     * <p>IFA011 values are VLAN, VXLAN, NVGRE and INHERIT, and "cardinality 0 means default value
     * VLAN is used" - recorded rather than filled in, since an absent value and an explicit VLAN
     * are different statements by the descriptor author.
     */
    public Optional<PropertyValue<String>> getSegmentationType() {
        return Optional.ofNullable(segmentationType);
    }

    /** Segmentation ID for the subport. Mandatory, 1. */
    public Optional<PropertyValue<Integer>> getSegmentationId() {
        return Optional.ofNullable(segmentationId);
    }

    @Override
    public String toString() {
        return "Subport(" + subportCpd + ")";
    }
}
