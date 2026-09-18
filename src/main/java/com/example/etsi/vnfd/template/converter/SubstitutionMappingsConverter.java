package com.example.etsi.vnfd.template.converter;

import com.example.etsi.vnfd.template.PropertyFilter;
import com.example.etsi.vnfd.template.SubstitutionMappings;
import com.example.etsi.vnfd.template.SubstitutionTarget;
import com.example.etsi.vnfd.utils.Yamls;
import java.util.List;
import java.util.Map;

/**
 * Reads {@code substitution_mappings}.
 *
 * <p>Two grammars for the flavour identifier are accepted. The current one is
 * {@code substitution_filter}, which SOL001 V5.4.1 clause 6.11.2 describes as carrying "a
 * flavour_id property and its value ... which identifies the DF corresponding to this low level
 * template". The older {@code properties} form is read as well, because clause 6.11.2 NOTE 1
 * records that the grammar changed at version 3.3.1 and that the previous form is still to be
 * handled; a caller can tell the two apart and report the deprecated one.
 */
final class SubstitutionMappingsConverter {

    private SubstitutionMappingsConverter() {
    }

    static SubstitutionMappings read(Object block) {
        SubstitutionMappings mappings = new SubstitutionMappings();
        Map<String, Object> map = Yamls.map(block);
        mappings.setNodeType(Yamls.string(map.get("node_type")));

        readSubstitutionFilter(mappings, map.get("substitution_filter"));
        mappings.propertyMappings().putAll(Yamls.map(map.get("properties")));
        readTargets(mappings.requirements(), map.get("requirements"));
        readTargets(mappings.capabilities(), map.get("capabilities"));
        mappings.attributes().putAll(Yamls.map(map.get("attributes")));
        mappings.interfaces().putAll(InterfaceConverter.readAll(map.get("interfaces")));
        return mappings;
    }

    /**
     * The filter is a map with a {@code properties} sequence of single-entry maps, each naming a
     * property and its constraints: {@code properties: [ flavour_id: { equal: simple } ]}.
     */
    private static void readSubstitutionFilter(SubstitutionMappings mappings, Object block) {
        if (block == null) {
            return;
        }
        Map<String, Object> filter = Yamls.map(block);
        for (Object entry : Yamls.list(filter.get("properties"))) {
            for (Map.Entry<String, Object> e : Yamls.map(entry).entrySet()) {
                PropertyFilter propertyFilter = new PropertyFilter(e.getKey());
                if (Yamls.isMap(e.getValue())) {
                    propertyFilter.constraints().putAll(Yamls.map(e.getValue()));
                } else {
                    // Bare value is equivalent to an equal constraint.
                    propertyFilter.constraints().put("equal", e.getValue());
                }
                mappings.substitutionFilter().add(propertyFilter);
            }
        }
    }

    /**
     * Each mapping is a two-element sequence: the node template being exposed, then the requirement
     * or capability of that node, e.g. {@code virtual_link_mgmt: [ WebCp, virtual_link ]}.
     */
    private static void readTargets(Map<String, SubstitutionTarget> into, Object block) {
        for (Map.Entry<String, Object> e : Yamls.map(block).entrySet()) {
            List<String> pair = Yamls.stringList(e.getValue());
            if (pair.isEmpty()) {
                continue;
            }
            String nodeTemplateName = pair.get(0);
            String targetName = pair.size() > 1 ? pair.get(1) : null;
            into.put(e.getKey(), new SubstitutionTarget(nodeTemplateName, targetName));
        }
    }
}
