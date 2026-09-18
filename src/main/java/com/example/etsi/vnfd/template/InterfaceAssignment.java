package com.example.etsi.vnfd.template;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * An interface assignment on a node template.
 *
 * <p>For a VNFD this is where lifecycle behaviour lives: SOL001 V5.4.1 clause 6.7.1 defines
 * {@code tosca.interfaces.nfv.Vnflcm} with an operation per VNF LCM operation plus
 * {@code _start}/{@code _end} preamble and postamble variants, and a set of notifications.
 *
 * <p>Two grammars are accepted. TOSCA 1.3 nests operations under an {@code operations} keyname;
 * TOSCA 1.2 and earlier place them directly under the interface. Both appear in practice because
 * SOL004 V5.1.1 clause 4.1.1 permits a CSAR to follow TOSCA Simple Profile YAML v1.1 or v1.3, and
 * its clause 4.1.3.1 example is written as {@code tosca_simple_yaml_1_2}. {@link #grammar()}
 * records which form was read.
 */
public final class InterfaceAssignment {

    /** Which interface grammar the declaring file used. */
    public enum Grammar {
        /** Operations nested under an {@code operations} keyname. */
        TOSCA_1_3,
        /** Operations directly under the interface, as in TOSCA 1.2 and earlier. */
        LEGACY
    }

    private final String name;
    private String type;
    private Grammar grammar = Grammar.TOSCA_1_3;
    private final Map<String, Object> inputs = new LinkedHashMap<>();
    private final Map<String, OperationAssignment> operations = new LinkedHashMap<>();
    private final Map<String, NotificationAssignment> notifications = new LinkedHashMap<>();

    public InterfaceAssignment(String name) {
        this.name = name;
    }

    /** Interface name as declared on the node template, e.g. {@code Vnflcm}. */
    public String name() {
        return name;
    }

    /** Interface type, e.g. {@code tosca.interfaces.nfv.Vnflcm}. May be absent on an assignment. */
    public Optional<String> type() {
        return Optional.ofNullable(type);
    }

    public Grammar grammar() {
        return grammar;
    }

    /** Interface-level inputs, shared by all operations. */
    public Map<String, Object> inputs() {
        return inputs;
    }

    public Map<String, OperationAssignment> operations() {
        return operations;
    }

    /**
     * Notifications. SOL001 V5.4.1 clause 6.7.1 gives {@code Vnflcm} three:
     * {@code change_current_package_notification} and its {@code _start} and {@code _end} variants.
     */
    public Map<String, NotificationAssignment> notifications() {
        return notifications;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setGrammar(Grammar grammar) {
        this.grammar = grammar;
    }

    @Override
    public String toString() {
        return name + operations.keySet();
    }
}
