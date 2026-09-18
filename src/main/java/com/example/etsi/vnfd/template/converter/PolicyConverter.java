package com.example.etsi.vnfd.template.converter;

import com.example.etsi.vnfd.template.ActivityDefinition;
import com.example.etsi.vnfd.template.PolicyDefinition;
import com.example.etsi.vnfd.template.SourceRef;
import com.example.etsi.vnfd.template.TriggerDefinition;
import com.example.etsi.vnfd.utils.Yamls;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Reads {@code topology_template.policies}, a sequence of single-entry maps.
 */
final class PolicyConverter {

    private PolicyConverter() {
    }

    static List<PolicyDefinition> readAll(Object block, String file) {
        List<PolicyDefinition> out = new ArrayList<>();
        for (Object entry : Yamls.list(block)) {
            for (Map.Entry<String, Object> e : Yamls.map(entry).entrySet()) {
                out.add(read(e.getKey(), e.getValue(), file));
            }
        }
        return out;
    }

    private static PolicyDefinition read(String name, Object body, String file) {
        PolicyDefinition policy = new PolicyDefinition(name);
        Map<String, Object> map = Yamls.map(body);
        policy.setSource(SourceRef.of(file, name));
        policy.setType(Yamls.string(map.get("type")));
        policy.setDescription(Yamls.string(map.get("description")));
        policy.metadata().putAll(Yamls.map(map.get("metadata")));
        policy.properties().putAll(Yamls.map(map.get("properties")));
        policy.targets().addAll(Yamls.stringList(map.get("targets")));
        readTriggers(policy, map.get("triggers"));
        return policy;
    }

    private static void readTriggers(PolicyDefinition policy, Object block) {
        for (Map.Entry<String, Object> e : Yamls.map(block).entrySet()) {
            TriggerDefinition trigger = new TriggerDefinition(e.getKey());
            Map<String, Object> body = Yamls.map(e.getValue());
            trigger.setEvent(Yamls.string(body.get("event")));
            trigger.setDescription(Yamls.string(body.get("description")));
            trigger.condition().putAll(Yamls.map(body.get("condition")));
            for (Object activity : Yamls.list(body.get("action"))) {
                readActivities(trigger, activity);
            }
            policy.triggers().put(e.getKey(), trigger);
        }
    }

    private static void readActivities(TriggerDefinition trigger, Object activity) {
        for (Map.Entry<String, Object> a : Yamls.map(activity).entrySet()) {
            trigger.action().add(new ActivityDefinition(kindOf(a.getKey()), a.getKey(), a.getValue()));
        }
    }

    private static ActivityDefinition.Kind kindOf(String key) {
        switch (key) {
            case "call_operation":
                return ActivityDefinition.Kind.CALL_OPERATION;
            case "set_state":
                return ActivityDefinition.Kind.SET_STATE;
            case "inline":
                return ActivityDefinition.Kind.INLINE;
            default:
                return ActivityDefinition.Kind.UNKNOWN;
        }
    }
}
