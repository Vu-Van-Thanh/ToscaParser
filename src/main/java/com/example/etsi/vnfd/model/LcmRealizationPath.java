package com.example.etsi.vnfd.model;

/**
 * How the infrastructure is expected to realise a VDU.
 *
 * <p>Not an IFA011 attribute. It is a conclusion drawn from the descriptor, recorded because a
 * consumer cannot act on a VNFD without it and the information model offers no single field that
 * states it.
 *
 * <p>SOL018 V5.4.1 clause 6.2.1.1 explains why the distinction exists: "The CISM is required to
 * expose management service interfaces on different abstraction levels. One abstraction level are
 * the MCIOPs, the other abstraction level are the MCIOs." Which level applies to a given VDU
 * follows from whether an {@code MciopProfile} references it.
 *
 * <p>[MANO INTERPRETATION] Derived from IFA011 V5.4.1 clause 7.1.6.2.2 NOTE 10 together with
 * SOL018 clause 6.2.1.1; no clause states the mapping in these terms.
 */
public enum LcmRealizationPath {

    /**
     * An MciopProfile references this VDU, so lifecycle operations go through the MCIOP-level
     * interface of SOL018 clause 7: {@code helm install}, {@code upgrade}, {@code rollback},
     * {@code uninstall}, {@code status}.
     */
    MCIOP_CISM,

    /**
     * The VDU describes its containers directly and no MciopProfile references it, so the VNFM
     * builds the container objects from {@code OsContainerDesc} and drives them through the
     * MCIO-level interface of SOL018 clause 8.
     */
    DIRECT_MCIO_CISM,

    /**
     * Neither an {@code osContainerDesc} nor an associated {@code MciopProfile}, which IFA011
     * clause 7.1.6.2.2 NOTE 10 does not permit for a container-realised VDU. Recorded rather than
     * guessed at, and reported as a finding.
     */
    UNDETERMINED
}
