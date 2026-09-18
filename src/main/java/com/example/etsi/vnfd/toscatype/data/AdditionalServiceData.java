package com.example.etsi.vnfd.toscatype.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.datatypes.nfv.AdditionalServiceData} - SOL001 V5.4.1 clause 6.2.66.
 *
 * <p>The AdditionalServiceData data type supports the specification of requirements related additional service data of the VirtualCp used to expose properties of the VirtualCp to NFV-MANO, as defined in ETSI GS NFV-IFA 011 [1].
 *
 * <p><b>Additional requirements</b> (clause 6.2.66): None.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AdditionalServiceData {

    /** Service port numbers exposed by the VirtualCp. required: true. */
    @JsonProperty("portData")
    private List<ServicePortData> portData;

    /** Service matching information exposed by the VirtualCp. */
    @JsonProperty("serviceData")
    private ServiceData serviceData;

}
