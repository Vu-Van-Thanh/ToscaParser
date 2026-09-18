package com.example.etsi.vnfd.toscatype.data;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.datatypes.nfv.LogicalNodeData} - SOL001 V5.4.1 clause 6.2.37.
 *
 * <p>The LogicalNodeData data type describes compute, memory and I/O requirements associated with a particular VDU.
 *
 * <p><b>Additional requirements</b> (clause 6.2.37): None. 6.2.38 Void.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LogicalNodeData {

    /** The logical node-level compute, memory and I/O requirements. A map of strings that contains a set of key-value pairs that describes hardware platform specific deployment requirements, including the number of CPU cores on this logical node, a memory configuration specific to a logical node or a requirement related to the association of an I/O device with the logical node. */
    @JsonProperty("logical_node_requirements")
    private Map<String, String> logicalNodeRequirements;

}
