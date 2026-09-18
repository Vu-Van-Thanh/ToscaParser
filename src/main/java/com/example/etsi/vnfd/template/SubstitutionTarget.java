package com.example.etsi.vnfd.template;

/**
 * One mapping entry of {@code substitution_mappings}, e.g.
 * {@code virtual_link_mgmt: [ WebCp, virtual_link ]}.
 *
 * <p>A named pair rather than a bare two-element list, because the two positions mean different
 * things and getting them the wrong way round is silent. The first names a node template inside
 * this service template, the second the requirement or capability of that node being exposed.
 *
 * <p>This is how a VNFD says which internal connection point becomes external: SOL001 V5.4.1
 * Table A.9.2-1 maps {@code VnfExtCpd} to a {@code VduCp} reached through substitution mappings,
 * so a connection point can be both an internal CP and an external one.
 */
public final class SubstitutionTarget {

    private final String nodeTemplateName;
    private final String targetName;

    public SubstitutionTarget(String nodeTemplateName, String targetName) {
        this.nodeTemplateName = nodeTemplateName;
        this.targetName = targetName;
    }

    /** The node template being exposed, e.g. {@code WebCp}. */
    public String nodeTemplateName() {
        return nodeTemplateName;
    }

    /** The requirement or capability of that node, e.g. {@code virtual_link}. */
    public String targetName() {
        return targetName;
    }

    @Override
    public String toString() {
        return "[" + nodeTemplateName + ", " + targetName + "]";
    }
}
