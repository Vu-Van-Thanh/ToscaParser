package com.example.etsi.vnfd.utils;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.error.YAMLException;

/**
 * Loads a TOSCA YAML file into plain Java collections.
 *
 * <p>Uses SnakeYAML's {@code SafeConstructor}, which will not instantiate arbitrary classes named
 * by the document. A VNF package is supplied by a third party, so a descriptor must never be able
 * to decide what objects the parser constructs.
 *
 * <p>Mappings come back as {@code LinkedHashMap}, preserving declaration order. Order is carried
 * all the way to the parsed VNFD so that output is stable between runs and therefore comparable.
 */
public final class ToscaYamlLoader {

    /** Bounds document nesting, so a hostile package cannot exhaust the stack. */
    private static final int MAX_ALIASES = 256;
    private static final int MAX_NESTING_DEPTH = 128;

    private ToscaYamlLoader() {
    }

    /**
     * Parses YAML into a map.
     *
     * @param file package-internal path, used only for the error message
     * @throws ToscaYamlException when the document is not valid YAML or is not a mapping; either
     *     makes the file unusable, as distinct from merely non-conformant
     */
    public static Map<String, Object> loadMapping(String file, byte[] content) {
        Object loaded;
        try {
            loaded = newYaml().load(new String(content, StandardCharsets.UTF_8));
        } catch (YAMLException e) {
            throw new ToscaYamlException("Cannot parse YAML in " + file + ": " + e.getMessage(), e);
        }
        if (loaded == null) {
            return new LinkedHashMap<>();
        }
        if (!(loaded instanceof Map)) {
            throw new ToscaYamlException(
                    "Expected a YAML mapping at the top level of " + file + " but found "
                            + loaded.getClass().getSimpleName());
        }
        return asStringKeyedMap(loaded);
    }

    private static Yaml newYaml() {
        LoaderOptions options = new LoaderOptions();
        options.setAllowDuplicateKeys(false);
        options.setMaxAliasesForCollections(MAX_ALIASES);
        options.setNestingDepthLimit(MAX_NESTING_DEPTH);
        return new Yaml(new SafeConstructor(options));
    }

    /**
     * Re-keys a YAML mapping to {@code String}, preserving order.
     * YAML permits non-string keys; TOSCA does not use them, so they are stringified rather than
     * rejected.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> asStringKeyedMap(Object value) {
        if (!(value instanceof Map)) {
            return Collections.emptyMap();
        }
        Map<String, Object> out = new LinkedHashMap<>();
        for (Map.Entry<Object, Object> e : ((Map<Object, Object>) value).entrySet()) {
            out.put(String.valueOf(e.getKey()), e.getValue());
        }
        return out;
    }
}
