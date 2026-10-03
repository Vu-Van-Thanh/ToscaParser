package com.example.etsi.vnfd.services.template2vnfd;

import com.example.etsi.vnfd.model.Vdu;
import com.example.etsi.vnfd.model.VnfDf;
import com.example.etsi.vnfd.services.template2vnfd.mappers.DeploymentFlavourMapper;
import com.example.etsi.vnfd.services.template2vnfd.mappers.NodeMapper;
import com.example.etsi.vnfd.services.template2vnfd.mappers.NodeMappers;
import com.example.etsi.vnfd.services.template2vnfd.mappers.StorageMapper;
import com.example.etsi.vnfd.services.template2vnfd.mappers.SwImageMapper;
import com.example.etsi.vnfd.services.template2vnfd.mappers.VnfExtCpdMapper;
import com.example.etsi.vnfd.services.template2vnfd.validator.SpecRuleValidator;
import com.example.etsi.vnfd.toscatype.node.Cp;
import com.example.etsi.vnfd.toscatype.node.NfvNode;
import com.example.etsi.vnfd.toscatype.node.VduOsContainer;
import com.example.etsi.vnfd.toscatype.node.VduOsContainerDeployableUnit;
import com.example.etsi.vnfd.toscatype.node.VnfExtCp;
import com.example.etsi.vnfd.typedef.EtsiTypes;
import java.util.ArrayList;
import java.util.List;

/**
 * Maps one deployment flavour into the shared pool, and returns the {@code VnfDf} for it.
 *
 * <p>The ordinary node types are driven from {@link NodeMappers#ALL}: each says which type it
 * reads, how it names the element and where the element lands, so this class does not name them one
 * by one. Three of the elements this pool holds are not ordinary in that sense, and each gets its own
 * {@code collect...} method, with the reason above it.
 *
 * <p>The collect methods write to disjoint maps of the pool, so their order relative to one another
 * is free. What is fixed is the order <em>within</em> a map, because a pool map keeps insertion order
 * and that is the order the VNFD lists that element type in.
 */
final class FlavourProcessor {

    private FlavourProcessor() {
    }

    /** Maps every node of one flavour into the shared pool, validates it, and returns its VnfDf. */
    static VnfDf process(FlavourContext context, VnfdElements pool) {
        for (NodeMapper<?, ?> mapper : NodeMappers.ALL) {
            apply(mapper, context, pool);
        }
        collectSwImageDescs(context, pool);
        collectVnfExtCpds(context, pool);
        collectVirtualStorageDescs(context, pool);

        // Vnf, Mciop and DeployableModule are absent on purpose: the first becomes the VNFD header,
        // the other two belong to the deployment flavour, so none of them is a VNFD-level element
        // this pool holds.

        List<Vdu> flavourVdus = flavourVdus(context, pool);
        VnfDf df = DeploymentFlavourMapper.map(context, pool);
        SpecRuleValidator.validateFlavour(df, flavourVdus, context.findings());
        return df;
    }

    /**
     * One entry per node type: every node of that type, mapped and contributed.
     *
     * <p>Generic so that {@code N} and {@code E} stay tied together across the three calls - the
     * wildcards in {@link NodeMappers#ALL} are captured here and nowhere else.
     */
    private static <N extends NfvNode, E> void apply(
            NodeMapper<N, E> mapper, FlavourContext context, VnfdElements pool) {
        for (N node : context.nodesOf(mapper.nodeType())) {
            mapper.contribute(pool, mapper.id(node), mapper.map(node, context));
        }
    }

    /**
     * SwImageDesc is read from an artifact OF a node rather than from a node, so it has no node type
     * to be driven by. SOL001 clause 6.8.12.6 also caps it at one per container, which
     * {@link SpecRuleValidator} has to count - the mapper is only ever handed the one that was chosen.
     */
    private static void collectSwImageDescs(FlavourContext context, VnfdElements pool) {
        for (VduOsContainer container : context.containers().values()) {
            SpecRuleValidator.swImage(container, context);
            context.artifactOfType(container, EtsiTypes.ARTIFACT_SW_IMAGE).ifPresent(image ->
                    pool.addSwImageDesc(VnfdUtils.nodeId(container),
                            SwImageMapper.map(image, container)));
        }
    }

    /**
     * One information element, two grammars. SOL001 clause 6.8.2.8 lets a service template expose a
     * VduCp through substitution_mappings instead of declaring a VnfExtCp node, and both become a
     * VnfExtCpd. They share one pass because they share one list, and that is what keeps them in
     * declaration order rather than grouped by grammar.
     */
    private static void collectVnfExtCpds(FlavourContext context, VnfdElements pool) {
        for (Cp cp : context.connectionPoints().values()) {
            if (cp instanceof VnfExtCp) {
                pool.addVnfExtCpd(VnfdUtils.nodeId(cp), VnfExtCpdMapper.fromNode((VnfExtCp) cp));
            } else if (context.isExternallyExposed(cp.getKey())) {
                pool.addVnfExtCpd(VnfdUtils.nodeId(cp), VnfExtCpdMapper.fromExposed(cp));
            }
        }
    }

    /**
     * SOL001 gives block, object and file storage three node types with no common supertype, so
     * there is no single type to ask for. Asking for the three separately would group the result by
     * type instead of by declaration order, so every node is offered and the mapper answers for the
     * three it knows.
     */
    private static void collectVirtualStorageDescs(FlavourContext context, VnfdElements pool) {
        for (NfvNode node : context.nodes()) {
            StorageMapper.map(node).ifPresent(desc -> pool.addVirtualStorageDesc(desc.getId(), desc));
        }
    }

    /**
     * The VDUs of this flavour, as the VNFD holds them.
     *
     * <p>Read back out of the pool rather than collected while mapping, because they are not always
     * the same objects: {@code SpecRuleValidator.validateFlavour} asks C28 about the VNFD-level VDU,
     * and for one a later flavour redeclares that is the object the earlier flavour contributed.
     */
    private static List<Vdu> flavourVdus(FlavourContext context, VnfdElements pool) {
        List<Vdu> out = new ArrayList<>();
        for (VduOsContainerDeployableUnit vdu : context.vdus()) {
            out.add(pool.vdu(VnfdUtils.nodeId(vdu)));
        }
        return out;
    }
}
