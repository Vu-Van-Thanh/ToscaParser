package com.example.etsi.vnfd.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * {@code LifeCycleManagementScript}, IFA011 V5.4.1 clause 7.1.13.
 *
 * <p>Produced only from a {@code Vnflcm} operation that declares an {@code implementation}. An
 * operation declaring just {@code inputs.additional_parameters} is not a script: that feeds
 * {@code VnfLcmOperationsConfiguration} instead, per SOL001 V5.4.1 Table A.9.2-1. The distinction
 * matters in practice, since all three bundled example packages declare the latter and none the
 * former, and so yield no lifecycle scripts at all.
 *
 * <p>Clause 7.1.13.2 NOTE 1 requires at least one of {@code event} and {@code lcmTransitionEvent}.
 * The {@code event} enumeration separates two groups: lifecycle events the VNFM raises internally,
 * named {@code EVENT_START_*} and {@code EVENT_END_*}, and external stimuli such as receipt of an
 * instantiation request. A TOSCA operation named {@code instantiate_start} belongs to the first, a
 * bare {@code instantiate} to the second.
 */
public final class LifeCycleManagementScript {

    private final String lcmScriptId;
    private final List<String> event;
    private final List<String> lcmTransitionEvent;
    private final String script;
    private final String scriptDsl;
    private final Map<String, Object> scriptInput;

    private LifeCycleManagementScript(Builder builder) {
        this.lcmScriptId = Objects.requireNonNull(builder.lcmScriptId, "lcmScriptId");
        this.event = Collections.unmodifiableList(new ArrayList<>(builder.event));
        this.lcmTransitionEvent =
                Collections.unmodifiableList(new ArrayList<>(builder.lcmTransitionEvent));
        this.script = builder.script;
        this.scriptDsl = builder.scriptDsl;
        this.scriptInput = builder.scriptInput;
    }

    public static Builder builder(String lcmScriptId) {
        return new Builder(lcmScriptId);
    }

    /** Identifier for referencing this script from elsewhere. */
    public String getLcmScriptId() {
        return lcmScriptId;
    }

    /** Lifecycle events or external stimuli that trigger this script. */
    public List<String> getEvent() {
        return event;
    }

    /** Transition events that no enumerated value covers. */
    public List<String> getLcmTransitionEvent() {
        return lcmTransitionEvent;
    }

    /** Mandatory. The script file, resolved against the package root. */
    public Optional<String> getScript() {
        return Optional.ofNullable(script);
    }

    /**
     * Mandatory. The language the script is written in.
     *
     * <p>[ASSUMPTION] Derived from the implementation artifact type where one is declared, and from
     * the file extension otherwise. SOL001 gives no rule for determining it.
     */
    public Optional<String> getScriptDsl() {
        return Optional.ofNullable(scriptDsl);
    }

    /** Parameters passed to the script in addition to those of the operation request. */
    public Map<String, Object> getScriptInput() {
        return scriptInput == null ? Collections.emptyMap() : scriptInput;
    }

    @Override
    public String toString() {
        return lcmScriptId + " on " + event;
    }

    /** Builder for {@link LifeCycleManagementScript}. */
    public static final class Builder {
        private final String lcmScriptId;
        private final List<String> event = new ArrayList<>();
        private final List<String> lcmTransitionEvent = new ArrayList<>();
        private String script;
        private String scriptDsl;
        private Map<String, Object> scriptInput;

        private Builder(String lcmScriptId) {
            this.lcmScriptId = lcmScriptId;
        }

        public Builder addEvent(String value) {
            event.add(value);
            return this;
        }

        public Builder addLcmTransitionEvent(String value) {
            lcmTransitionEvent.add(value);
            return this;
        }

        public Builder script(String value) {
            this.script = value;
            return this;
        }

        public Builder scriptDsl(String value) {
            this.scriptDsl = value;
            return this;
        }

        public Builder scriptInput(Map<String, Object> value) {
            this.scriptInput = value;
            return this;
        }

        public LifeCycleManagementScript build() {
            return new LifeCycleManagementScript(this);
        }
    }
}
