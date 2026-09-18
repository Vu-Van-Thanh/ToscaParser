package com.example.etsi.vnfd.toscatype.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.datatypes.nfv.SizeRange} - SOL001 V5.4.1 clause 6.2.82.
 *
 * <p>The SizeRange data type is a refined list type that supports the specification of a range of scalar-unit.size values by indicating the lower boundary in the first element of the list and the upper boundary in the second element of the list. Simple-Profile-YAML-v1.3 [20].
 *
 * <p><b>Additional requirements</b> (clause 6.2.82): None.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SizeRange {

}
