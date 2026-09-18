package com.example.etsi.vnfd.typedef;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@code data_types} entry, e.g. {@code tosca.datatypes.nfv.VduProfile}.
 *
 * <p>SOL001 V5.4.1 defines most VNFD structure through data types rather than node properties, so
 * these carry a large share of the model: {@code VduProfile}, {@code McioIdentificationData},
 * {@code InstantiationLevel}, {@code VduLevel} and so on.
 */
public final class DataTypeDef extends AbstractTypeDef {

    private final List<Constraint> constraints = new ArrayList<>();

    public DataTypeDef(String name) {
        super(name);
    }

    /** Constraints applying to the data type as a whole, used by derived scalar types. */
    public List<Constraint> constraints() {
        return constraints;
    }
}
