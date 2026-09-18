package com.example.etsi.vnfd.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * {@code ScalingAspect}, IFA011 V5.4.1 clause 7.1.10.2.
 *
 * <p>One dimension along which the VNF scales horizontally, with the number of steps available and
 * the deltas applied at each.
 */
public final class ScalingAspect {

    private final String id;
    private final String name;
    private final String description;
    private final Integer maxScaleLevel;
    private final List<String> stepDeltas;

    private ScalingAspect(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "id");
        this.name = builder.name;
        this.description = builder.description;
        this.maxScaleLevel = builder.maxScaleLevel;
        this.stepDeltas = Collections.unmodifiableList(new ArrayList<>(builder.stepDeltas));
    }

    public static Builder builder(String id) {
        return new Builder(id);
    }

    /** Mandatory. Identifier of this aspect within the deployment flavour. */
    public String getId() {
        return id;
    }

    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    public Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }

    /** Total number of scaling steps that can be applied to this aspect. */
    public Optional<Integer> getMaxScaleLevel() {
        return Optional.ofNullable(maxScaleLevel);
    }

    /** Scaling delta identifiers, first entry corresponding to the first step. */
    public List<String> getStepDeltas() {
        return stepDeltas;
    }

    @Override
    public String toString() {
        return id + " (max " + maxScaleLevel + ")";
    }

    /** Builder for {@link ScalingAspect}. */
    public static final class Builder {
        private final String id;
        private String name;
        private String description;
        private Integer maxScaleLevel;
        private final List<String> stepDeltas = new ArrayList<>();

        private Builder(String id) {
            this.id = id;
        }

        public Builder name(String value) {
            this.name = value;
            return this;
        }

        public Builder description(String value) {
            this.description = value;
            return this;
        }

        public Builder maxScaleLevel(Integer value) {
            this.maxScaleLevel = value;
            return this;
        }

        public Builder addStepDelta(String value) {
            stepDeltas.add(value);
            return this;
        }

        public ScalingAspect build() {
            return new ScalingAspect(this);
        }
    }
}
