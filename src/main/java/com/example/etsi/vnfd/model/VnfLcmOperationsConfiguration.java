package com.example.etsi.vnfd.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * {@code VnfLcmOperationsConfiguration}, IFA011 V5.4.1 clause 7.1.5.
 *
 * <p>Configuration affecting how each VNF LCM operation is invoked. Clause 7.1.5.2.2 lists twelve
 * per-operation sub-elements, each optional.
 *
 * <p>Two sources feed it, per SOL001 V5.4.1 Table A.9.2-1, which maps this element to "property of
 * VNF node type with data type tosca.datatypes.nfv.VnfLcmOperationsConfiguration and/or inputs
 * additional_parameters of the corresponding operation in the Vnflcm interface". The second source
 * is easy to miss: an operation declaring only {@code additional_parameters} produces no lifecycle
 * script, but it does produce operation parameters here. For an instantiate operation those land in
 * {@code InstantiateVnfOpConfig.parameter}, clause 7.1.5.3.2.
 *
 * <p>Held on the deployment flavour rather than the VNFD, which is where IFA011 clause 7.1.8.2.2
 * puts it, even though TOSCA declares it as a VNF node property. One service template is one
 * flavour, so the two agree.
 */
public final class VnfLcmOperationsConfiguration {

    private final Map<String, Map<String, Object>> opConfigs;

    private VnfLcmOperationsConfiguration(Map<String, Map<String, Object>> opConfigs) {
        this.opConfigs = Collections.unmodifiableMap(new LinkedHashMap<>(opConfigs));
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Per-operation configuration, keyed by IFA011 attribute name such as
     * {@code instantiateVnfOpConfig} or {@code scaleVnfOpConfig}.
     */
    public Map<String, Map<String, Object>> getOpConfigs() {
        return opConfigs;
    }

    /** Configuration of one operation, empty when the descriptor states none. */
    public Map<String, Object> getOpConfig(String attributeName) {
        return opConfigs.getOrDefault(attributeName, Collections.emptyMap());
    }

    public boolean isEmpty() {
        return opConfigs.isEmpty();
    }

    @Override
    public String toString() {
        return "VnfLcmOperationsConfiguration" + opConfigs.keySet();
    }

    /** Builder for {@link VnfLcmOperationsConfiguration}. */
    public static final class Builder {
        private final Map<String, Map<String, Object>> opConfigs = new LinkedHashMap<>();

        private Builder() {
        }

        /** Merges values into one operation configuration, keeping what is already there. */
        public Builder mergeOpConfig(String attributeName, Map<String, Object> values) {
            if (values == null || values.isEmpty()) {
                return this;
            }
            opConfigs.computeIfAbsent(attributeName, key -> new LinkedHashMap<>()).putAll(values);
            return this;
        }

        public VnfLcmOperationsConfiguration build() {
            return new VnfLcmOperationsConfiguration(opConfigs);
        }
    }
}
