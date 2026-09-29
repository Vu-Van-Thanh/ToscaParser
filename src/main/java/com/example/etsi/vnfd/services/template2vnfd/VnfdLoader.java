package com.example.etsi.vnfd.services.template2vnfd;

import com.example.etsi.vnfd.model.LifeCycleManagementScript;
import com.example.etsi.vnfd.ParseResult;
import com.example.etsi.vnfd.model.Vnfd;
import com.example.etsi.vnfd.services.pkg2template.TypeReader;
import com.example.etsi.vnfd.services.template2vnfd.validator.SpecRuleValidator;
import com.example.etsi.vnfd.template.ServiceToscaTemplate;
import com.example.etsi.vnfd.template.ToscaDescriptorTemplate;
import com.example.etsi.vnfd.toscatype.node.Vnf;
import com.example.etsi.vnfd.validation.Findings;
import java.util.List;
import java.util.Optional;
import com.example.etsi.vnfd.services.template2vnfd.mappers.VnfHeaderMapper;
import com.example.etsi.vnfd.services.template2vnfd.mappers.LcmMapper;

/**
 * Turns a parsed TOSCA package into a VNFD.
 *
 * <p>The entry point of the second half of the library. {@code YamlService} answers what the package
 * says; this answers what it means. The split follows the two specifications - everything up to
 * {@link ServiceToscaTemplate} is TOSCA and SOL001 vocabulary, everything produced here is IFA011.
 *
 * <p>Each service template is one deployment flavour (SOL001 V5.4.1 clauses 6.11.2 and 6.11.3), so
 * flavours are mapped one at a time and their contributions merged by identifier: IFA011 clause
 * 7.1.2.2 keeps the descriptors themselves - vdu, osContainerDesc, swImageDesc, the connection
 * points - at VNFD level, shared by every flavour.
 */
public final class VnfdLoader {

    /** Parses a package, reporting only what this stage notices. */
    public ParseResult load(ServiceToscaTemplate template) {
        return load(template, new Findings());
    }

    /**
     * Parses into a collector the caller already holds, so package-level findings survive.
     *
     * <p>Reading the package and interpreting it are two stages with their own findings - a missing
     * manifest is noticed by {@code YamlService}, long before any VNFD exists. Passing the same
     * collector to both is what puts them in one result.
     */
    public ParseResult load(ServiceToscaTemplate template, Findings findings) {
        List<ToscaDescriptorTemplate> flavours = template.flavourTemplates();
        if (flavours.isEmpty()) {
            throw new VnfdParseException("The package contains no service template with a "
                    + "topology_template; there is nothing to parse into a VNFD");
        }

        TypeReader.Hierarchy hierarchy = new TypeReader.Hierarchy(template.typeRegistry());
        NodeBinder binder = new NodeBinder(hierarchy, findings);

        Vnfd.Builder builder = Vnfd.builder();
        VnfdElements merged = new VnfdElements();
        boolean headerRead = false;

        for (ToscaDescriptorTemplate flavour : flavours) {
            FlavourContext context = new FlavourContext(flavour, binder, hierarchy, findings);

            if (!headerRead) {
                headerRead = readHeader(context, builder, findings);
            }

            builder.addDf(FlavourProcessor.process(context, merged));
        }

        merged.applyTo(builder);

        Vnfd vnfd = builder.build();
        SpecRuleValidator.validateVnfd(vnfd, findings);
        return new ParseResult(vnfd, template, findings);
    }

    /**
     * The VNF header and the lifecycle scripts, read from the first flavour that declares a VNF node.
     *
     * <p>IFA011 clause 7.1.2.2 keeps lifeCycleManagementScript at VNFD level, and SOL001 clause
     * 6.11.2 gives every flavour template the same VNF node type, so the scripts are read once with
     * the header rather than once per flavour. Which flavour that is, is not fixed: it is the first
     * one that has a VNF node, and the file a C24 finding names is that flavour's file.
     *
     * @return whether the header was read, which latches it off for the flavours that follow
     */
    private boolean readHeader(FlavourContext context, Vnfd.Builder builder, Findings findings) {
        Optional<Vnf> vnf = context.vnf();
        if (!vnf.isPresent()) {
            return false;
        }
        VnfHeaderMapper.map(vnf.get(), builder);
        for (LifeCycleManagementScript script : LcmMapper.map(vnf.get())) {
            SpecRuleValidator.lifecycleScriptHasEvent(script, context.template().file(), findings);
            builder.addLifeCycleManagementScript(script);
        }
        return true;
    }
}
