package com.example.etsi.vnfd.typedef;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * An {@code interface_types} entry, e.g. {@code tosca.interfaces.nfv.Vnflcm}.
 *
 * <p>Both {@link #operations()} and {@link #notifications()} are held, because SOL001 V5.4.1 clause
 * 6.7.1 gives {@code Vnflcm} each: operations for the VNF LCM operations and their
 * {@code _start}/{@code _end} variants, notifications for the Change current VNF package flow.
 */
public final class InterfaceTypeDef extends AbstractTypeDef {

    private final Map<String, Object> inputs = new LinkedHashMap<>();
    private final Map<String, Object> operations = new LinkedHashMap<>();
    private final Map<String, Object> notifications = new LinkedHashMap<>();

    InterfaceTypeDef(String name) {
        super(name);
    }

    public Map<String, Object> inputs() {
        return inputs;
    }

    public Map<String, Object> operations() {
        return operations;
    }

    public Map<String, Object> notifications() {
        return notifications;
    }
}
