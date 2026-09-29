package com.example.etsi.vnfd.toscatype.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.datatypes.nfv.VnfcAdditionalConfigurableProperties} - SOL001 V5.4.1 clause 6.2.11.
 *
 * <p>The VnfcAdditionalConfigurableProperties type is an empty base type for deriving data types for describing additional configurable properties for a given VNFC.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VnfcAdditionalConfigurableProperties {

}
