package com.example.etsi.vnfd.typedef;

import java.util.ArrayList;
import java.util.List;

/** A {@code capability_types} entry, e.g. {@code tosca.capabilities.nfv.AssociableVdu}. */
public final class CapabilityTypeDef extends AbstractTypeDef {

    private final List<String> validSourceTypes = new ArrayList<>();

    public CapabilityTypeDef(String name) {
        super(name);
    }

    /** Node types permitted to require this capability. */
    public List<String> validSourceTypes() {
        return validSourceTypes;
    }
}
