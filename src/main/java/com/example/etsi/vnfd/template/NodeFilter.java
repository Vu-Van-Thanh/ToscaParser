package com.example.etsi.vnfd.template;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A {@code node_filter}, constraining which node may satisfy a requirement.
 *
 * <p>TOSCA 1.3 keyname, unused by SOL001 V5.4.1. Kept verbatim so it survives a round trip.
 */
public final class NodeFilter {

    private final List<Map<String, Object>> propertyConstraints = new ArrayList<>();
    private final Map<String, Object> capabilityConstraints = new LinkedHashMap<>();

    public List<Map<String, Object>> propertyConstraints() {
        return propertyConstraints;
    }

    public Map<String, Object> capabilityConstraints() {
        return capabilityConstraints;
    }
}
