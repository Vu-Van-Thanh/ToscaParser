package com.example.etsi.vnfd.services.template2vnfd.mappers;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Every node type that maps to exactly one VNFD-level information element.
 *
 * <p>One line each. A new ETSI node type that behaves this way is a new {@link NodeMapper} and a
 * line here; nothing else in the library has to know it exists.
 *
 * <p>What is deliberately absent is as much of the story as what is here. {@code SwImageDesc} is
 * read from an artifact rather than a node, {@code VnfExtCpd} has two grammars that share one list,
 * the three storage node types have no common supertype, and the VNF node becomes a header rather
 * than an element. Those are driven directly by {@code FlavourProcessor}, each with its reason
 * written above it.
 */
public final class NodeMappers {

    public static final List<NodeMapper<?, ?>> ALL = Collections.unmodifiableList(Arrays.asList(
            new VduMapper(),
            new OsContainerMapper(),
            new VduCpdMapper(),
            new VipCpdMapper(),
            new VirtualCpdMapper(),
            new VirtualLinkMapper(),
            new CertificateMapper()));

    private NodeMappers() {
    }
}
