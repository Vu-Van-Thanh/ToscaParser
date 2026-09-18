package com.example.etsi.vnfd.toscatype.artifact;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.example.etsi.vnfd.template.value.Quantity;
import com.example.etsi.vnfd.toscatype.data.ChecksumData;
import com.example.etsi.vnfd.toscatype.node.EtsiNodeType;
import com.example.etsi.vnfd.typedef.EtsiTypes;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.artifacts.nfv.SwImage} - SOL001 V5.4.1 clause 6.3.1.
 *
 * <p>The SwImage artifact describes the software image which is directly loaded on the virtualisation container realizing of the VDU or is to be loaded on a virtual storage resource, as defined in ETSI GS NFV-IFA 011 [1].
 *
 * <p>Represents the {@code SwImageDesc} information element of IFA011 V5.4.1 clause 7.1.6.5 (SOL001 V5.4.1 Table 6.1-1).
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@EtsiNodeType(EtsiTypes.ARTIFACT_SW_IMAGE)
public class SwImage extends NfvArtifact {

    @JsonProperty("properties")
    private Properties properties;

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Properties {

        /** Name of this software image required: true. */
        @JsonProperty("name")
        private PropertyValue<String> name;

        /** Version of this software image required: true. */
        @JsonProperty("version")
        private PropertyValue<String> version;

        /** Provider of this software image */
        @JsonProperty("provider")
        private PropertyValue<String> provider;

        /** Checksum of the software image file */
        @JsonProperty("checksum")
        private ChecksumData checksum;

        /** The container format describes the container file format in which software image is provided required: true. */
        @JsonProperty("container_format")
        private PropertyValue<String> containerFormat;

        /** The disk format of a software image is the format of the underlying disk image */
        @JsonProperty("disk_format")
        private PropertyValue<String> diskFormat;

        /** The minimal disk size requirement for this software image */
        @JsonProperty("min_disk")
        private PropertyValue<Quantity> minDisk;

        /** The minimal RAM requirement for this software image */
        @JsonProperty("min_ram")
        private PropertyValue<Quantity> minRam;

        /** The size of this software image */
        @JsonProperty("size")
        private PropertyValue<Quantity> size;

        /** Identifies the operating system used in the software image */
        @JsonProperty("operating_system")
        private PropertyValue<String> operatingSystem;

        /** Identifies the virtualisation environments (e.g. hypervisor) compatible with this software image */
        @JsonProperty("supported_virtualisation_environments")
        private List<String> supportedVirtualisationEnvironments;

    }

}
