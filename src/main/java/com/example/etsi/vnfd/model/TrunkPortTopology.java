package com.example.etsi.vnfd.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@code TrunkPortTopology}, IFA011 V5.4.1 clause 7.1.6.11.
 *
 * <p>Clause 7.1.6.11.1: the logical topology between an intCpd in trunk mode, describing a trunk
 * port, and the other intCpds describing subports of the same trunk. It is what lets a consumer ask
 * for a trunk resource and put each connection point into it as parent port or as subport.
 *
 * <p>An attribute of {@link Vdu} - {@code trunkPort}, 0..N in Table 7.1.6.2.2-1 - assembled here
 * from the connection points, since SOL001 states the relation on the VduSubCp
 * ({@code trunk_binding}) rather than on the VDU.
 */
public final class TrunkPortTopology {

    private final String parentPortCpd;
    private final List<Subport> subportList;

    private TrunkPortTopology(String parentPortCpd, List<Subport> subportList) {
        this.parentPortCpd = parentPortCpd;
        this.subportList = Collections.unmodifiableList(new ArrayList<>(subportList));
    }

    public static TrunkPortTopology of(String parentPortCpd, List<Subport> subportList) {
        return new TrunkPortTopology(parentPortCpd, subportList);
    }

    /** The VduCpd instantiating the parent port. Mandatory, 1. */
    public String getParentPortCpd() {
        return parentPortCpd;
    }

    /** The subports of this trunk. Mandatory, 1..N. */
    public List<Subport> getSubportList() {
        return subportList;
    }

    @Override
    public String toString() {
        return "TrunkPortTopology(" + parentPortCpd + ", " + subportList.size() + " subport(s))";
    }
}
