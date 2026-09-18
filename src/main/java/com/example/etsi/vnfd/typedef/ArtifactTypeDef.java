package com.example.etsi.vnfd.typedef;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * An {@code artifact_types} entry.
 *
 * <p>{@link #fileExt()} is not decoration. SOL001 V5.4.1 clause 6.3.3 declares
 * {@code tosca.artifacts.nfv.HelmChart} with {@code file_ext: [ tar, tar.gz, tgz ]}, and clause
 * 6.3.6 declares {@code VRDFile} with {@code [ yaml, json ]}. Without reading these the library
 * cannot tell a Helm chart artifact pointing at a chart from one pointing at a README, which is a
 * mistake a real package makes.
 */
public final class ArtifactTypeDef extends AbstractTypeDef {

    private String mimeType;
    private final List<String> fileExt = new ArrayList<>();

    public ArtifactTypeDef(String name) {
        super(name);
    }

    public Optional<String> mimeType() {
        return Optional.ofNullable(mimeType);
    }

    /** Permitted file extensions, without a leading dot. */
    public List<String> fileExt() {
        return fileExt;
    }

    /**
     * Whether a file name carries one of the permitted extensions.
     * Always true when the type declares none, since then nothing is constrained.
     */
    public boolean acceptsFileName(String fileName) {
        if (fileExt.isEmpty() || fileName == null) {
            return true;
        }
        String lower = fileName.toLowerCase(Locale.ROOT);
        return fileExt.stream().anyMatch(ext -> lower.endsWith("." + ext.toLowerCase(Locale.ROOT)));
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}
