package com.example.etsi.vnfd.typedef;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

/**
 * The official SOL001 V5.4.1 type definitions, bundled on the classpath.
 *
 * <p>Needed because SOL001 V5.4.1 Annex B.2 NOTE 2 states that the type definitions file "may, but
 * need not, be included in the VNF Package". Packages routinely reference it without shipping it:
 * all three bundled examples import {@code etsi_nfv_sol001_vnfd_types.yaml} and none contains it.
 * Without a built-in copy, {@code tosca.nodes.nfv.VduCp} would have no resolvable parent and no
 * connection point could be recognised.
 *
 * <p>The common types file is loaded first because the VNFD types file imports it, and because
 * {@code tosca.nodes.nfv.Cp} - the parent of all five connection point types - lives there.
 */
public final class EtsiTypeCatalogue {

    private static final String BASE = "/etsi/sol001/v5.4.1/";
    private static final String COMMON_TYPES = "etsi_nfv_sol001_common_types.yaml";
    private static final String VNFD_TYPES = "etsi_nfv_sol001_vnfd_types.yaml";

    /** Loaded once: the files are a few hundred kilobytes and never change at runtime. */
    private static volatile Map<String, Map<String, Object>> cachedDocuments;

    private EtsiTypeCatalogue() {
    }

    /** File names of the bundled definitions, in the order they must be loaded. */
    public static List<String> fileNames() {
        return Arrays.asList(COMMON_TYPES, VNFD_TYPES);
    }

    /** The bundled documents, keyed by file name, in load order. */
    public static Map<String, Map<String, Object>> documents() {
        Map<String, Map<String, Object>> local = cachedDocuments;
        if (local == null) {
            synchronized (EtsiTypeCatalogue.class) {
                local = cachedDocuments;
                if (local == null) {
                    local = loadAll();
                    cachedDocuments = local;
                }
            }
        }
        return local;
    }

    /** Registers the bundled definitions into a builder, lowest precedence first. */
    public static void addTo(TypeRegistryBuilder builder) {
        for (Map.Entry<String, Map<String, Object>> e : documents().entrySet()) {
            builder.add(e.getValue(), "built-in:" + e.getKey());
        }
    }

    /** Whether a file name refers to one of the bundled ETSI definition files. */
    public static boolean isCatalogueFile(String reference) {
        if (reference == null) {
            return false;
        }
        String leaf = reference.substring(reference.lastIndexOf('/') + 1);
        return COMMON_TYPES.equals(leaf) || VNFD_TYPES.equals(leaf);
    }

    private static Map<String, Map<String, Object>> loadAll() {
        Map<String, Map<String, Object>> documents = new LinkedHashMap<>();
        for (String fileName : fileNames()) {
            documents.put(fileName, load(fileName));
        }
        return Collections.unmodifiableMap(documents);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> load(String fileName) {
        String resource = BASE + fileName;
        try (InputStream in = EtsiTypeCatalogue.class.getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalStateException(
                        "Bundled ETSI type definitions are missing from the classpath: " + resource);
            }
            LoaderOptions options = new LoaderOptions();
            options.setMaxAliasesForCollections(256);
            options.setNestingDepthLimit(128);
            Object loaded = new Yaml(new SafeConstructor(options))
                    .load(new String(readAll(in), StandardCharsets.UTF_8));
            if (!(loaded instanceof Map)) {
                throw new IllegalStateException("Bundled type definitions are not a mapping: " + resource);
            }
            Map<String, Object> out = new LinkedHashMap<>();
            for (Map.Entry<Object, Object> e : ((Map<Object, Object>) loaded).entrySet()) {
                out.put(String.valueOf(e.getKey()), e.getValue());
            }
            return out;
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read " + resource, e);
        }
    }

    private static byte[] readAll(InputStream in) throws IOException {
        java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
        byte[] chunk = new byte[8192];
        int read;
        while ((read = in.read(chunk)) != -1) {
            buffer.write(chunk, 0, read);
        }
        return buffer.toByteArray();
    }
}
