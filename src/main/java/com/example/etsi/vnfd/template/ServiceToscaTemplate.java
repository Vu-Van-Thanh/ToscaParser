package com.example.etsi.vnfd.template;

import com.example.etsi.vnfd.csar.CsarReader;
import com.example.etsi.vnfd.csar.ToscaMeta;
import com.example.etsi.vnfd.typedef.TypeRegistry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Everything read out of one VNF package: its metadata, its service template files, and the type
 * registry they resolve against.
 *
 * <p>This is the boundary between reading and interpreting. Producing it involves no knowledge of
 * NFV beyond locating files; turning it into a VNFD is a separate step, so the same parsed package
 * can be inspected as TOSCA and as an information model.
 */
public final class ServiceToscaTemplate {

    private final CsarReader csar;
    private final ToscaMeta meta;
    private final List<ToscaDescriptorTemplate> descriptorTemplates;
    private final TypeRegistry typeRegistry;

    public ServiceToscaTemplate(CsarReader csar, ToscaMeta meta,
                                List<ToscaDescriptorTemplate> descriptorTemplates,
                                TypeRegistry typeRegistry) {
        this.csar = csar;
        this.meta = meta;
        this.descriptorTemplates = Collections.unmodifiableList(new ArrayList<>(descriptorTemplates));
        this.typeRegistry = typeRegistry;
    }

    /** The package these templates came from; artifacts are resolved against it. */
    public CsarReader csar() {
        return csar;
    }

    public ToscaMeta meta() {
        return meta;
    }

    /** Service template files, entry template first. */
    public List<ToscaDescriptorTemplate> descriptorTemplates() {
        return descriptorTemplates;
    }

    /** Types from the package plus the built-in ETSI catalogue. */
    public TypeRegistry typeRegistry() {
        return typeRegistry;
    }

    /** The entry template named by {@code Entry-Definitions}. */
    public Optional<ToscaDescriptorTemplate> entryTemplate() {
        return descriptorTemplates.stream()
                .filter(t -> t.file().equals(meta.entryDefinitions()))
                .findFirst();
    }

    /**
     * Templates that represent a deployment flavour, i.e. everything except the top-level template
     * of a two-level VNFD.
     *
     * <p>In the single-template design of SOL001 V5.4.1 clause 6.11.3 the entry template is itself
     * the only flavour, so it appears here.
     */
    public List<ToscaDescriptorTemplate> flavourTemplates() {
        return descriptorTemplates.stream()
                .filter(t -> !t.isTopLevel())
                .filter(t -> t.topologyTemplate().isPresent())
                .collect(Collectors.toList());
    }

    /** The top-level template, present only in the two-level design of clause 6.11.2. */
    public Optional<ToscaDescriptorTemplate> topLevelTemplate() {
        return descriptorTemplates.stream()
                .filter(ToscaDescriptorTemplate::isTopLevel)
                .findFirst();
    }

    /** True when the package uses the two-level design of SOL001 clause 6.11.2. */
    public boolean isTwoLevelDesign() {
        return topLevelTemplate().isPresent();
    }

    @Override
    public String toString() {
        return "ServiceToscaTemplate(" + csar.name() + ", "
                + descriptorTemplates.size() + " template(s))";
    }
}
