package com.example.etsi.vnfd.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reading loosely typed YAML into known Java types.
 *
 * <p>SnakeYAML hands back {@code Object}: a mapping, a sequence, a scalar, or null. Every stage that
 * touches a YAML document therefore asks the same four questions - is this a map, a string, a
 * boolean, a list - and each answer has the same right shape. A missing key yields an empty
 * collection rather than null, so callers loop instead of testing, and a scalar where a sequence was
 * expected becomes a one-element list, which is how TOSCA itself writes the short form.
 *
 * <p>Shared rather than repeated per package on purpose. These decisions - what an absent key means,
 * whether a lone scalar counts as a sequence - have to be the same in the descriptor reader and in
 * the type-definition reader, or the two disagree about the same file.
 */
public final class Yamls {

    private Yamls() {
    }

    /** A YAML mapping with its keys as strings, or an empty map if this is not a mapping. */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> map(Object value) {
        if (!(value instanceof Map)) {
            return Collections.emptyMap();
        }
        Map<String, Object> out = new LinkedHashMap<>();
        for (Map.Entry<Object, Object> e : ((Map<Object, Object>) value).entrySet()) {
            out.put(String.valueOf(e.getKey()), e.getValue());
        }
        return out;
    }

    public static String string(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    /** {@code null} when the value says nothing, so an absent flag stays distinct from false. */
    public static Boolean bool(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof CharSequence) {
            return Boolean.parseBoolean(value.toString().trim());
        }
        return null;
    }

    public static Integer integer(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof CharSequence) {
            try {
                return Integer.valueOf(value.toString().trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /** A YAML sequence, or a single value wrapped as a one-element list. */
    @SuppressWarnings("unchecked")
    public static List<Object> list(Object value) {
        if (value == null) {
            return Collections.emptyList();
        }
        if (value instanceof List) {
            return new ArrayList<>((List<Object>) value);
        }
        return Collections.singletonList(value);
    }

    /** A YAML sequence of scalars, as strings. */
    public static List<String> stringList(Object value) {
        List<String> out = new ArrayList<>();
        for (Object element : list(value)) {
            if (element != null) {
                out.add(String.valueOf(element));
            }
        }
        return out;
    }

    /** True when the value is a mapping with at least one entry. */
    public static boolean isMap(Object value) {
        return value instanceof Map && !((Map<?, ?>) value).isEmpty();
    }
}
