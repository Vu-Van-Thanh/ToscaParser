package com.example.etsi.vnfd.toscatype.data;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.datatypes.nfv.ServicePortData} - SOL001 V5.4.1 clause 6.2.65.
 *
 * <p>The ServicePortData data type supports the specification of requirements describing port properties exposed by VirtualCp, as defined in ETSI GS NFV-IFA 011 [1].
 *
 * <p><b>Additional requirements</b> (clause 6.2.65): None.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServicePortData {

    /** The name of the port exposed by the VirtualCp. required: true. */
    @JsonProperty("name")
    private PropertyValue<String> name;

    /** The L4 protocol for this port exposed by the VirtualCp. required: true. */
    @JsonProperty("protocol")
    private PropertyValue<String> protocol;

    /** The L4 port number exposed by the VirtualCp. required: true. */
    @JsonProperty("port")
    private PropertyValue<Integer> port;

    /** Specifies whether the port attribute value is allowed to be configurable. required: true. */
    @JsonProperty("portConfigurable")
    private PropertyValue<Boolean> portConfigurable;

}
