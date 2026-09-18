package com.example.etsi.vnfd.toscatype.node;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The ETSI node type a class stands for.
 *
 * <p>Deliberately not {@code @JsonTypeName}. Jackson resolves a subtype by matching the type string
 * exactly, and a VNFD almost never writes the ETSI name: SOL001 V5.4.1 clause 6.11.2 requires the
 * VNF node type to be derived from {@code tosca.nodes.nfv.VNF}, so Annex A.23 writes
 * {@code type: MyCompany.ExampleVNF} and the bundled packages write
 * {@code type: ExampleCorp.SimpleWebCnf.1_0}. Matching on the literal name finds nothing.
 *
 * <p>The binder instead asks the type hierarchy which ETSI type the declared one derives from, and
 * picks the class annotated with it. Nothing else is needed here: whether a property is required,
 * what constraints it carries and what default it falls back to are all read from the type
 * definitions at run time, so copying them into annotations would only create a second source that
 * drifts when ETSI publishes a new version.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EtsiNodeType {

    /** The ETSI type name, from {@code EtsiTypes}. */
    String value();
}
