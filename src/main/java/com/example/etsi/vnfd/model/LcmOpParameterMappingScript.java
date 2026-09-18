package com.example.etsi.vnfd.model;

import java.util.Objects;
import java.util.Optional;

/**
 * {@code LcmOpParameterMappingScript}, IFA011 V5.4.1 clause 7.1.20.
 *
 * <p>A script the VNFM runs before issuing a command towards the CISM or the VIM. Clause 7.1.20.1
 * is precise about when: "only executed for VNF LCM operations that trigger the invocation of a
 * command towards CISM or VIM, except the Query VNF operation", and "a script is executed for each
 * MCIOP or virtualised resource descriptor involved in the VNF LCM operation". A VDU deployed
 * without an MCIOP has no such step at all.
 *
 * <p>{@link #getInvocationArity()} exists because two calling conventions are in play for the same
 * information element. SOL001 clause 6.3.4.1 defines three ordered parameters for a
 * {@code HelmParamMappingScript}, while IFA011 clause 7.1.20.1 lists four for the generic form; its
 * NOTE 1 reconciles them by allowing the first to be omitted when the information is in the second.
 * A consumer cannot invoke the script correctly without knowing which applies.
 */
public final class LcmOpParameterMappingScript {

    /** Which artifact type the script came from, and therefore how it is invoked. */
    public enum ScriptKind {
        /** {@code tosca.artifacts.nfv.HelmParamMappingScript}, SOL001 clause 6.3.4. */
        HELM_PARAM_MAPPING,
        /** {@code tosca.artifacts.nfv.LcmOpParameterMappingScript}, SOL001 clause 6.3.7. */
        LCM_OP_PARAM_MAPPING
    }

    private final String lcmOpParameterMappingScriptId;
    private final String script;
    private final String scriptDsl;
    private final ScriptKind kind;
    private final int invocationArity;

    private LcmOpParameterMappingScript(String id, String script, String scriptDsl, ScriptKind kind,
                                        int invocationArity) {
        this.lcmOpParameterMappingScriptId = Objects.requireNonNull(id, "id");
        this.script = script;
        this.scriptDsl = scriptDsl;
        this.kind = kind;
        this.invocationArity = invocationArity;
    }

    public static LcmOpParameterMappingScript of(String id, String script, String scriptDsl,
                                                 ScriptKind kind, int invocationArity) {
        return new LcmOpParameterMappingScript(id, script, scriptDsl, kind, invocationArity);
    }

    /** Mandatory. [ASSUMPTION] Taken from the artifact definition name. */
    public String getLcmOpParameterMappingScriptId() {
        return lcmOpParameterMappingScriptId;
    }

    /** The script file, resolved against the package root. Its contents are never read. */
    public Optional<String> getScript() {
        return Optional.ofNullable(script);
    }

    /** Mandatory. The language the script is written in, bash or python. */
    public Optional<String> getScriptDsl() {
        return Optional.ofNullable(scriptDsl);
    }

    /** [PROJECT-SPECIFIC] Which artifact type supplied this script. */
    public ScriptKind getKind() {
        return kind;
    }

    /** [PROJECT-SPECIFIC] Number of ordered parameters the script expects. */
    public int getInvocationArity() {
        return invocationArity;
    }

    @Override
    public String toString() {
        return lcmOpParameterMappingScriptId + " (" + scriptDsl + ", arity " + invocationArity + ")";
    }
}
