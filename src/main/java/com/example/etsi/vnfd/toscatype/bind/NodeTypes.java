package com.example.etsi.vnfd.toscatype.bind;

import com.example.etsi.vnfd.toscatype.node.Certificate;
import com.example.etsi.vnfd.toscatype.node.DeployableModule;
import com.example.etsi.vnfd.toscatype.node.Mciop;
import com.example.etsi.vnfd.toscatype.node.NfvNode;
import com.example.etsi.vnfd.toscatype.node.VduCp;
import com.example.etsi.vnfd.toscatype.node.VduOsContainer;
import com.example.etsi.vnfd.toscatype.node.VduOsContainerDeployableUnit;
import com.example.etsi.vnfd.toscatype.node.VduSubCp;
import com.example.etsi.vnfd.toscatype.node.VduVirtualBlockStorage;
import com.example.etsi.vnfd.toscatype.node.VduVirtualFileStorage;
import com.example.etsi.vnfd.toscatype.node.VduVirtualObjectStorage;
import com.example.etsi.vnfd.toscatype.node.VipCp;
import com.example.etsi.vnfd.toscatype.node.VirtualCp;
import com.example.etsi.vnfd.toscatype.node.Vnf;
import com.example.etsi.vnfd.toscatype.node.VnfExtCp;
import com.example.etsi.vnfd.toscatype.node.VnfVirtualLink;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The node classes this library binds.
 *
 * <p>An explicit list rather than a classpath scan: scanning needs a library this project does not
 * carry, and a list of class literals costs one line per type while still keeping the ETSI type
 * name in the class itself, where {@code @EtsiNodeType} declares it. Order does not matter - the
 * resolver picks the nearest ancestor, so {@code VduSubCp} wins over {@code VduCp} over {@code Cp}
 * without anyone stating a precedence.
 */
public final class NodeTypes {

    /** Every SOL001 node type mapped in the CNF scope. */
    public static final List<Class<? extends NfvNode>> ALL = Collections.unmodifiableList(
            Arrays.<Class<? extends NfvNode>>asList(
                    Vnf.class,
                    VduOsContainerDeployableUnit.class,
                    VduOsContainer.class,
                    Mciop.class,
                    VduSubCp.class,
                    VduCp.class,
                    VnfExtCp.class,
                    VipCp.class,
                    VirtualCp.class,
                    VnfVirtualLink.class,
                    VduVirtualBlockStorage.class,
                    VduVirtualObjectStorage.class,
                    VduVirtualFileStorage.class,
                    DeployableModule.class,
                    Certificate.class));

    private NodeTypes() {
    }
}
