package com.example.etsi.vnfd;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The prose SOL001 attaches to a type, keyed by type name.
 *
 * <p>The type definitions file gives names, types and constraints but no clause numbers, no
 * "Additional Requirements" and no statement of which IFA011 information element a type becomes.
 * All of that lives only in the specification text, so it is extracted from the PDF once and read
 * here when the model classes are generated.
 */
final class Sol001Doc {

    private static final JsonNode ROOT = load("/sol001_clauses.json");
    private static final JsonNode IFA = load("/ifa011_map.json");

    private Sol001Doc() {
    }

    private static JsonNode load(String resource) {
        try (InputStream in = Sol001Doc.class.getResourceAsStream(resource)) {
            return new ObjectMapper().readTree(in);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Clause that defines the type, e.g. {@code 6.8.13}. */
    static String clause(String toscaType) {
        JsonNode n = ROOT.path(toscaType).path("clause");
        return n.isMissingNode() ? "" : n.asText();
    }

    /** The javadoc lines for a type: clause header, description, additional requirements. */
    static List<String> javadoc(String toscaType) {
        List<String> out = new ArrayList<>();
        JsonNode n = ROOT.path(toscaType);
        String clause = n.path("clause").asText("");
        out.add("{@code " + toscaType + "}"
                + (clause.isEmpty() ? "." : " - SOL001 V5.4.1 clause " + clause + "."));
        String desc = n.path("description").asText("").trim();
        if (!desc.isEmpty()) {
            out.add("");
            out.add(desc);
        }
        JsonNode ifa = IFA.path(toscaType);
        String element = ifa.path("element").asText("");
        if (!element.isEmpty()) {
            String ieClause = ifa.path("clause").asText("");
            out.add("");
            out.add("Represents the {@code " + element + "} information element of IFA011 V5.4.1"
                    + (ieClause.isEmpty() ? "" : " clause " + ieClause)
                    + " (SOL001 V5.4.1 Table 6.1-1).");
        }
        String ifaNote = ifa.path("note").asText("").trim();
        if (!ifaNote.isEmpty()) {
            out.add("");
            out.add(ifaNote);
        }

        String additional = n.path("additional").asText("").trim();
        if (!additional.isEmpty()) {
            out.add("");
            out.add("<b>Additional requirements</b> (clause " + clause + "): " + additional);
        }
        return out;
    }
}
