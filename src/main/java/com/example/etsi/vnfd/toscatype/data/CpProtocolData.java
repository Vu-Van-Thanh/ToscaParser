package com.example.etsi.vnfd.toscatype.data;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.datatypes.nfv.CpProtocolData} - SOL001 V5.4.1 clause 9.2.6.
 *
 * <p>The CpProtocolData data type describes and associates the protocol layer that a CP uses together with other protocol and connection point information.
 *
 * <p><b>Additional requirements</b> (clause 9.2.6): None.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CpProtocolData {

    /** One of the values of the property layer_protocols of the CP required: true. */
    @JsonProperty("associated_layer_protocol")
    private PropertyValue<String> associatedLayerProtocol;

    /** Provides information on the addresses to be assigned to the CP */
    @JsonProperty("address_data")
    private List<AddressData> addressData;

}
