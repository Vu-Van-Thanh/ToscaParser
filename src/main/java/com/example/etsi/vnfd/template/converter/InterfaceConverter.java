package com.example.etsi.vnfd.template.converter;

import com.example.etsi.vnfd.template.ImplementationDefinition;
import com.example.etsi.vnfd.template.InterfaceAssignment;
import com.example.etsi.vnfd.template.NotificationAssignment;
import com.example.etsi.vnfd.template.OperationAssignment;
import com.example.etsi.vnfd.utils.Yamls;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads the {@code interfaces} block of a node template.
 *
 * <p>Handles both interface grammars. TOSCA 1.3 nests operations under an {@code operations}
 * keyname and notifications under {@code notifications}; TOSCA 1.2 and earlier place operations
 * directly under the interface. Both occur in real packages because SOL004 V5.1.1 clause 4.1.1
 * permits a CSAR to follow TOSCA Simple Profile YAML v1.1 or v1.3, and its clause 4.1.3.1 example
 * is written as {@code tosca_simple_yaml_1_2}.
 *
 * <p>Which grammar was used is recorded on the assignment rather than guessed at from the document
 * version, since a file may declare one version and be written in the other.
 */
final class InterfaceConverter {

    /** Interface-level keynames that are never operation names. */
    private static final List<String> RESERVED =
            Arrays.asList("type", "inputs", "operations", "notifications", "description", "metadata");

    private InterfaceConverter() {
    }

    static Map<String, InterfaceAssignment> readAll(Object block) {
        Map<String, InterfaceAssignment> out = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : Yamls.map(block).entrySet()) {
            out.put(e.getKey(), read(e.getKey(), e.getValue()));
        }
        return out;
    }

    private static InterfaceAssignment read(String name, Object body) {
        InterfaceAssignment iface = new InterfaceAssignment(name);
        Map<String, Object> map = Yamls.map(body);
        iface.setType(Yamls.string(map.get("type")));
        iface.inputs().putAll(Yamls.map(map.get("inputs")));

        boolean modernGrammar = map.containsKey("operations") || map.containsKey("notifications");
        iface.setGrammar(modernGrammar ? InterfaceAssignment.Grammar.TOSCA_1_3
                : InterfaceAssignment.Grammar.LEGACY);

        if (modernGrammar) {
            readOperations(iface, Yamls.map(map.get("operations")));
            readNotifications(iface, Yamls.map(map.get("notifications")));
        } else {
            // Legacy form: anything that is not a reserved keyname is an operation.
            Map<String, Object> operations = new LinkedHashMap<>();
            for (Map.Entry<String, Object> e : map.entrySet()) {
                if (!RESERVED.contains(e.getKey())) {
                    operations.put(e.getKey(), e.getValue());
                }
            }
            readOperations(iface, operations);
        }
        return iface;
    }

    private static void readOperations(InterfaceAssignment iface, Map<String, Object> block) {
        for (Map.Entry<String, Object> e : block.entrySet()) {
            OperationAssignment operation = new OperationAssignment(e.getKey());
            Map<String, Object> body = Yamls.map(e.getValue());
            if (body.isEmpty() && e.getValue() != null) {
                // Shorthand: "instantiate: my_script.sh" is an implementation, not an input block.
                operation.setImplementation(readImplementation(e.getValue()));
            } else {
                operation.setDescription(Yamls.string(body.get("description")));
                operation.inputs().putAll(Yamls.map(body.get("inputs")));
                operation.outputs().putAll(Yamls.map(body.get("outputs")));
                if (body.containsKey("implementation")) {
                    operation.setImplementation(readImplementation(body.get("implementation")));
                }
            }
            iface.operations().put(e.getKey(), operation);
        }
    }

    private static void readNotifications(InterfaceAssignment iface, Map<String, Object> block) {
        for (Map.Entry<String, Object> e : block.entrySet()) {
            NotificationAssignment notification = new NotificationAssignment(e.getKey());
            Map<String, Object> body = Yamls.map(e.getValue());
            if (body.isEmpty() && e.getValue() != null) {
                notification.setImplementation(readImplementation(e.getValue()));
            } else {
                notification.setDescription(Yamls.string(body.get("description")));
                notification.outputs().putAll(Yamls.map(body.get("outputs")));
                if (body.containsKey("implementation")) {
                    notification.setImplementation(readImplementation(body.get("implementation")));
                }
            }
            iface.notifications().put(e.getKey(), notification);
        }
    }

    /** {@code implementation} may be a bare artifact or file name, or a map with {@code primary}. */
    static ImplementationDefinition readImplementation(Object value) {
        ImplementationDefinition impl = new ImplementationDefinition();
        if (!Yamls.isMap(value)) {
            impl.setPrimary(Yamls.string(value));
            return impl;
        }
        Map<String, Object> map = Yamls.map(value);
        impl.setPrimary(Yamls.string(map.get("primary")));
        impl.dependencies().addAll(Yamls.stringList(map.get("dependencies")));
        impl.setTimeout(Yamls.integer(map.get("timeout")));
        impl.setOperationHost(Yamls.string(map.get("operation_host")));
        return impl;
    }
}
