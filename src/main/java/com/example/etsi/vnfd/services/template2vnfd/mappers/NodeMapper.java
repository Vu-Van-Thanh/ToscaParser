package com.example.etsi.vnfd.services.template2vnfd.mappers;

import com.example.etsi.vnfd.services.template2vnfd.FlavourContext;
import com.example.etsi.vnfd.services.template2vnfd.VnfdElements;
import com.example.etsi.vnfd.toscatype.node.NfvNode;

/**
 * One SOL001 node type to one IFA011 information element.
 *
 * <p>Four questions, because those are the four things that differ between one node type and the
 * next: which type is read, what the identifier rule is, how the element is built, and where it
 * lands in the VNFD. A reader who wants to know what happens to a {@code VduCp} opens
 * {@code VduCpdMapper} and finds all four in one screen.
 *
 * <p>Not every mapper fits. A node that becomes two elements, an element read from an artifact
 * rather than a node, a node type with no common supertype - those are called directly by
 * {@code FlavourProcessor}, each with the reason written above it. The interface is worth having
 * because it makes the ordinary case uniform, and that is only visible while the extraordinary
 * ones stay outside it.
 *
 * @param <N> the SOL001 node type this reads
 * @param <E> the IFA011 information element it produces
 */
public interface NodeMapper<N extends NfvNode, E> {

    /** The node type to read. Every node of this type in the flavour is offered to {@link #map}. */
    Class<N> nodeType();

    /**
     * The identifier the element is known by.
     *
     * <p>See {@code VnfdUtils} for which of these rules the specification states outright and which
     * this library assumes.
     */
    String id(N node);

    /** SOL001 to IFA011, for one node. */
    E map(N node, FlavourContext context);

    /** Where the element belongs in the VNFD. */
    void contribute(VnfdElements pool, String id, E element);
}
