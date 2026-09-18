package com.example.etsi.vnfd.toscatype.data;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.datatypes.nfv.IpAllocationPool} - SOL001 V5.4.1 clause 6.2.17.
 *
 * <p>The IpAllocationPool data type specifies a range of IP addresses.
 *
 * <p><b>Additional requirements</b> (clause 6.2.17): None.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class IpAllocationPool {

    /** The IP address to be used as the first one in a pool of addresses derived from the cidr block full IP range required: true. */
    @JsonProperty("start_ip_address")
    private PropertyValue<String> startIpAddress;

    /** The IP address to be used as the last one in a pool of addresses derived from the cidr block full IP range required: true. */
    @JsonProperty("end_ip_address")
    private PropertyValue<String> endIpAddress;

}
