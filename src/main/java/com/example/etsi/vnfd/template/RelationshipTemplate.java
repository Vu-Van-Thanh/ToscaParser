package com.example.etsi.vnfd.template;

/**
 * A {@code topology_template.relationship_templates} entry.
 *
 * <p>Valid TOSCA 1.3 but unused by SOL001 V5.4.1, which expresses relationships inline on
 * requirements. Parsed and carried so a package that uses it is not rejected; nothing maps it into
 * the VNFD.
 */
public final class RelationshipTemplate extends EntityTemplate {

    public RelationshipTemplate(String name) {
        super(name);
    }
}
