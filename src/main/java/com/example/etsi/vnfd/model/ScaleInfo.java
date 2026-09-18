package com.example.etsi.vnfd.model;

import java.util.Objects;

/**
 * {@code ScaleInfo}, IFA011 V5.4.1 clause 7.1.8.8.
 *
 * <p>The scale level of one scaling aspect at a given instantiation level.
 */
public final class ScaleInfo {

    private final String aspectId;
    private final int scaleLevel;

    private ScaleInfo(String aspectId, int scaleLevel) {
        this.aspectId = Objects.requireNonNull(aspectId, "aspectId");
        this.scaleLevel = scaleLevel;
    }

    public static ScaleInfo of(String aspectId, int scaleLevel) {
        return new ScaleInfo(aspectId, scaleLevel);
    }

    /** Mandatory. References the scaling aspect. */
    public String getAspectId() {
        return aspectId;
    }

    /** Mandatory. The scale level reached for that aspect. */
    public int getScaleLevel() {
        return scaleLevel;
    }

    @Override
    public String toString() {
        return aspectId + "=" + scaleLevel;
    }
}
