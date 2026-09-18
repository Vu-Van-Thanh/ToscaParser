package com.example.etsi.vnfd.toscatype.data;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.datatypes.nfv.AddressData} - SOL001 V5.4.1 clause 9.2.3.
 *
 * <p>The AddressData data type describes information about the addressing scheme and parameters applicable to a CP, as defined in ETSI GS NFV-IFA 011 [1].
 *
 * <p><b>Additional requirements</b> (clause 9.2.3): None.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AddressData {

    /** Describes the type of the address to be assigned to a connection point. The content type shall be aligned with the address type supported by the layerProtocol property of the connection point required: true. */
    @JsonProperty("address_type")
    private PropertyValue<String> addressType;

    /** Provides the information on the MAC addresses to be assigned to a connection point. */
    @JsonProperty("l2_address_data")
    private L2AddressData l2AddressData;

    /** Provides the information on the IP addresses to be assigned to a connection point */
    @JsonProperty("l3_address_data")
    private L3AddressData l3AddressData;

}
