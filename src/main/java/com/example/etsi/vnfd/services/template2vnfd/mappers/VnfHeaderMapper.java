package com.example.etsi.vnfd.services.template2vnfd.mappers;

import com.example.etsi.vnfd.model.Vnfd;
import com.example.etsi.vnfd.services.template2vnfd.VnfdUtils;
import com.example.etsi.vnfd.template.value.PropertyValue;
import com.example.etsi.vnfd.toscatype.node.Vnf;
import java.util.function.Consumer;

/**
 * The VNF-level identification attributes, SOL001 V5.4.1 clause 6.8.1 to IFA011 clause 7.1.2.2.
 *
 * <p>Each is a plain String in the model rather than a {@code PropertyValue}: SOL001 Table 5.9-2
 * permits {@code get_input} only on {@code flavour_id}, {@code modifiable_attributes} and
 * {@code configurable_properties}, so an identifier that is not a literal is a descriptor defect,
 * and rendering the expression text as if it were an id would be worse than leaving it unset.
 *
 * <p>Values may be assigned on the node template or left to the {@code default} of the node type -
 * SOL001 Annex A.23 does the latter for everything but {@code flavour_description}. Both arrive here
 * the same way, because the binder lays the type defaults underneath before binding.
 */
public final class VnfHeaderMapper {

    private VnfHeaderMapper() {
    }

    public static void map(Vnf vnf, Vnfd.Builder builder) {
        Vnf.Properties p = vnf.getProperties();
        if (p == null) {
            return;
        }
        literal(p.getDescriptorId(), builder::vnfdId);
        literal(p.getProvider(), builder::vnfProvider);
        literal(p.getProductName(), builder::vnfProductName);
        literal(p.getSoftwareVersion(), builder::vnfSoftwareVersion);
        literal(p.getDescriptorVersion(), builder::vnfdVersion);
        literal(p.getProductInfoName(), builder::vnfProductInfoName);
        literal(p.getProductInfoDescription(), builder::vnfProductInfoDescription);
        literal(p.getExtInvariantId(), builder::vnfdExtInvariantId);
        literal(p.getDefaultLocalizationLanguage(), builder::defaultLocalizationLanguage);

        VnfdUtils.orEmpty(p.getVnfmInfo()).forEach(builder::addVnfmInfo);
        VnfdUtils.orEmpty(p.getLocalizationLanguages())
                .forEach(builder::addLocalizationLanguage);
    }

    private static void literal(PropertyValue<String> value, Consumer<String> sink) {
        if (value != null && value.isResolved()) {
            value.resolved().ifPresent(sink);
        }
    }
}
