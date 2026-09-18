package com.example.etsi.vnfd.template;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A policy trigger: an event, an optional condition, and the activities to run.
 *
 * <p>SOL001 V5.4.1 uses this for {@code tosca.policies.nfv.VnfPackageChange}, where the event is a
 * {@code Vnflcm} notification such as
 * {@code tosca.interfaces.nfv.Vnflcm.change_current_package_notification} and the action is a
 * {@code call_operation} on a VNF-specific ChangeCurrentVnfPackage interface.
 */
public final class TriggerDefinition {

    private final String name;
    private String event;
    private String description;
    private final Map<String, Object> condition = new LinkedHashMap<>();
    private final List<ActivityDefinition> action = new ArrayList<>();

    public TriggerDefinition(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    /** The event that fires this trigger. */
    public String event() {
        return event;
    }

    public Optional<String> description() {
        return Optional.ofNullable(description);
    }

    public Map<String, Object> condition() {
        return condition;
    }

    /** Activities to perform, in order. */
    public List<ActivityDefinition> action() {
        return action;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return name + " on " + event;
    }
}
