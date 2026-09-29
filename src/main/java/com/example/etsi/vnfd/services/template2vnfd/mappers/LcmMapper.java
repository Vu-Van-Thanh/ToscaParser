package com.example.etsi.vnfd.services.template2vnfd.mappers;

import com.example.etsi.vnfd.model.LifeCycleManagementScript;
import com.example.etsi.vnfd.services.template2vnfd.VnfdUtils;
import com.example.etsi.vnfd.template.ImplementationDefinition;
import com.example.etsi.vnfd.template.InterfaceAssignment;
import com.example.etsi.vnfd.template.OperationAssignment;
import com.example.etsi.vnfd.toscatype.artifact.HelmParamMappingScript;
import com.example.etsi.vnfd.toscatype.node.Vnf;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * SOL001 V5.4.1 clause 6.7.1.1 interface {@code tosca.interfaces.nfv.Vnflcm} to IFA011 V5.4.1
 * clause 7.1.13.2 {@code LifeCycleManagementScript}.
 *
 * <p>Only an operation carrying an {@code implementation} becomes a script. Declaring
 * {@code inputs} on an operation states the shape of its parameters, not that anything runs -
 * IFA011 clause 7.1.13.2 makes {@code script} M,1, so an operation with no implementation has
 * nothing to put there. Every bundled MCIOP package declares {@code instantiate.inputs} and no
 * implementation, and none of them should produce a script.
 *
 * <p>[ASSUMPTION] The operation-to-event table below. SOL001 clause 6.7.1.1 names the operations and
 * IFA011 clause 7.1.13.2 names the events, but neither prints a lookup between them; the names line
 * up closely enough to map by pattern, which is a judgement and is labelled as one.
 *
 * <p>SOL001 forms each pre- and post-amble as {@code <base>_start} and {@code <base>_end}. A base
 * operation with no suffix is not an internal VNFM lifecycle event at all: IFA011 describes those
 * values as "external stimulus detected on a VNFM reference point", i.e. the receipt of the request.
 */
public final class LcmMapper {

    /** The eighteen internal events of IFA011 Table 7.1.13.2-1, keyed by SOL001 operation base. */
    private static final Map<String, String> EVENT_BASE;

    static {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("instantiate", "INSTANTIATION");
        m.put("scale", "SCALING");
        m.put("scale_to_level", "SCALING_TO_LEVEL");
        m.put("heal", "HEALING");
        m.put("terminate", "TERMINATION");
        m.put("change_flavour", "VNF_FLAVOR_CHANGE");
        m.put("operate", "VNF_OPERATION_CHANGE");
        m.put("change_external_connectivity", "VNF_EXT_CONN_CHANGE");
        m.put("modify_information", "VNFINFO_MODIFICATION");
        m.put("create_snapshot", "VNF_SNAPSHOT_CREATION");
        m.put("revert_to_snapshot", "VNF_SNAPSHOT_REVERTINGTO");
        m.put("change_current_package", "CHANGE_CURRENT_VNF_PACKAGE");
        EVENT_BASE = Collections.unmodifiableMap(m);
    }

    private LcmMapper() {
    }

    /** Every implemented operation of the VNF node template, as a script. */
    public static List<LifeCycleManagementScript> map(Vnf vnf) {
        List<LifeCycleManagementScript> out = new ArrayList<>();
        if (vnf == null || vnf.getInterfaces() == null) {
            return out;
        }
        for (Map.Entry<String, InterfaceAssignment> iface : vnf.getInterfaces().entrySet()) {
            for (Map.Entry<String, OperationAssignment> op : iface.getValue().operations().entrySet()) {
                script(iface.getKey(), op.getKey(), op.getValue()).ifPresent(out::add);
            }
        }
        return out;
    }

    private static Optional<LifeCycleManagementScript> script(String interfaceName,
            String operationName, OperationAssignment operation) {
        Optional<ImplementationDefinition> implementation = operation.implementation();
        if (!implementation.isPresent()) {
            return Optional.empty();
        }
        String primary = implementation.get().primary();
        if (primary == null || primary.isEmpty()) {
            return Optional.empty();
        }

        LifeCycleManagementScript.Builder builder = LifeCycleManagementScript.builder(
                VnfdUtils.lcmScriptId(interfaceName, operationName));
        builder.script(primary);
        // [ASSUMPTION] scriptDsl is M,1 in IFA011 and SOL001 declares no language on a Vnflcm
        // operation, unlike HelmParamMappingScript (clause 6.3.4). The file extension is all the
        // descriptor offers.
        dslOf(primary).ifPresent(builder::scriptDsl);
        eventOf(operationName).ifPresent(builder::addEvent);
        builder.scriptInput(operation.inputs());
        return Optional.of(builder.build());
    }

    /**
     * The IFA011 event an operation name stands for.
     *
     * <p>Empty for a base operation: those correspond to the external stimuli IFA011 lists
     * separately, and inventing an EVENT_ value for them would state more than the specification
     * does. The script is still produced - IFA011 NOTE 1 wants at least one of event or
     * lcmTransitionEvent, and reporting that gap is rule C24's job, not this mapper's.
     */
    private static Optional<String> eventOf(String operationName) {
        if (operationName.endsWith("_start")) {
            String base = operationName.substring(0, operationName.length() - "_start".length());
            return Optional.ofNullable(EVENT_BASE.get(base)).map(e -> "EVENT_START_" + e);
        }
        if (operationName.endsWith("_end")) {
            String base = operationName.substring(0, operationName.length() - "_end".length());
            return Optional.ofNullable(EVENT_BASE.get(base)).map(e -> "EVENT_END_" + e);
        }
        return Optional.empty();
    }

    /** [ASSUMPTION] The interpreter implied by the file extension. */
    private static Optional<String> dslOf(String path) {
        String lower = path.toLowerCase(java.util.Locale.ROOT);
        if (lower.endsWith(".sh") || lower.endsWith(".bash")) {
            return Optional.of("bash");
        }
        if (lower.endsWith(".py")) {
            return Optional.of("python");
        }
        return Optional.empty();
    }
}
