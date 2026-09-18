package com.example.etsi.vnfd.model;

import com.example.etsi.vnfd.template.value.PropertyValue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * {@code Cpd}, IFA011 V5.4.1 clause 7.1.6.3.
 *
 * <p>Attributes common to every connection point descriptor. The specialisations below correspond
 * to distinct information elements rather than to variations of one, matching how IFA011 separates
 * {@code VduCpd}, {@code VnfExtCpd}, {@code VipCpd} and {@code VirtualCpd}.
 */
public abstract class Cpd {

    private final String cpdId;
    private final List<String> layerProtocol;
    private final PropertyValue<String> cpRole;
    private final PropertyValue<String> description;
    private final List<Map<String, Object>> protocol;
    private final PropertyValue<Boolean> trunkMode;

    protected Cpd(AbstractBuilder<?> builder) {
        this.cpdId = Objects.requireNonNull(builder.cpdId, "cpdId");
        this.layerProtocol = Collections.unmodifiableList(new ArrayList<>(builder.layerProtocol));
        this.cpRole = builder.cpRole;
        this.description = builder.description;
        this.protocol = Collections.unmodifiableList(new ArrayList<>(builder.protocol));
        this.trunkMode = builder.trunkMode;
    }

    /** Mandatory. Identifier of this connection point descriptor in the VNFD. */
    public String getCpdId() {
        return cpdId;
    }

    /** Protocols the connection point uses, e.g. ipv4. */
    public List<String> getLayerProtocol() {
        return layerProtocol;
    }

    /** Role in the traffic flow pattern; the TOSCA property is named {@code role}. */
    public Optional<PropertyValue<String>> getCpRole() {
        return Optional.ofNullable(cpRole);
    }

    public Optional<PropertyValue<String>> getDescription() {
        return Optional.ofNullable(description);
    }

    /** Addressing information, one entry per protocol. */
    public List<Map<String, Object>> getProtocol() {
        return protocol;
    }

    public Optional<PropertyValue<Boolean>> getTrunkMode() {
        return Optional.ofNullable(trunkMode);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + cpdId + ")";
    }

    /** Shared builder state for the connection point descriptors. */
    public abstract static class AbstractBuilder<T extends AbstractBuilder<T>> {
        private final String cpdId;
        private final List<String> layerProtocol = new ArrayList<>();
        private PropertyValue<String> cpRole;
        private PropertyValue<String> description;
        private final List<Map<String, Object>> protocol = new ArrayList<>();
        private PropertyValue<Boolean> trunkMode;

        protected AbstractBuilder(String cpdId) {
            this.cpdId = cpdId;
        }

        @SuppressWarnings("unchecked")
        protected T self() {
            return (T) this;
        }

        public T addLayerProtocol(String value) {
            layerProtocol.add(value);
            return self();
        }

        public T cpRole(PropertyValue<String> value) {
            this.cpRole = value;
            return self();
        }

        public T description(PropertyValue<String> value) {
            this.description = value;
            return self();
        }

        public T addProtocol(Map<String, Object> value) {
            protocol.add(value);
            return self();
        }

        public T trunkMode(PropertyValue<Boolean> value) {
            this.trunkMode = value;
            return self();
        }
    }
}
