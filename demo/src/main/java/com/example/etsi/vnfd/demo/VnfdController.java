package com.example.etsi.vnfd.demo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * The HTTP face of {@link ParseApi}.
 *
 * <p>Thin on purpose: each method resolves a package directory, calls the library, and returns what
 * came back. No parsing decision is made here, so a host application that wires the parser into its
 * own controllers can take {@link ParseApi} and leave this behind.
 *
 * <p>Status codes carry the library's own distinction. A package that cannot be read at all is
 * <b>422</b>; a package that reads but breaks a rule of SOL001 or IFA011 is <b>200</b>, with the
 * findings carrying the verdict - because whether a violation is fatal is the caller's decision, not
 * the parser's.
 */
@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class VnfdController {

    private final PackageCatalog catalog;

    public VnfdController(PackageCatalog catalog) {
        this.catalog = catalog;
    }

    /** Where the packages were found and how many there are. */
    @GetMapping({"/", "/health"})
    public ObjectNode health() {
        ObjectNode n = VnfdJson.object();
        n.put("service", "etsi-vnfd-parser-demo");
        n.put("repository", catalog.repoRoot().toString());
        n.put("packages", catalog.list().size());
        return n;
    }

    /** Every bundled package, positives and negatives. */
    @GetMapping("/packages")
    public ObjectNode packages() {
        ObjectNode n = VnfdJson.object();
        ArrayNode a = VnfdJson.array();
        for (PackageCatalog.Entry e : catalog.list()) {
            ObjectNode item = VnfdJson.object();
            item.put("name", e.name());
            item.put("kind", e.kind().name().toLowerCase());
            item.put("path", e.dir().toString());
            a.add(item);
        }
        n.put("count", a.size());
        n.set("packages", a);
        return n;
    }

    /** The full answer: {@code { package, hasErrors, vnfd, findings }}. */
    @GetMapping("/parse")
    public ResponseEntity<JsonNode> parse(
            @RequestParam(required = false) String pkg,
            @RequestParam(required = false) String dir) {
        return respond(pkg, dir, ParseApi.Report::toJson);
    }

    /** The VNFD alone, for diffing one run against another. */
    @GetMapping("/vnfd")
    public ResponseEntity<JsonNode> vnfd(
            @RequestParam(required = false) String pkg,
            @RequestParam(required = false) String dir) {
        return respond(pkg, dir, ParseApi.Report::vnfdJson);
    }

    /** Findings alone, each with the clause that justifies it. */
    @GetMapping("/findings")
    public ResponseEntity<JsonNode> findings(
            @RequestParam(required = false) String pkg,
            @RequestParam(required = false) String dir) {
        return respond(pkg, dir, ParseApi.Report::findingsJson);
    }

    /**
     * The TOSCA layer, before any of it became IFA011.
     *
     * <p>The useful half is {@code unboundNodeTemplates}: a node template whose type resolves to no
     * ETSI ancestor is dropped by the binder silently - no finding, no log line - which is the
     * quietest way for a descriptor to lose content.
     */
    @GetMapping("/debug")
    public ResponseEntity<JsonNode> debug(
            @RequestParam(required = false) String pkg,
            @RequestParam(required = false) String dir) {
        return respond(pkg, dir, ParseApi.Report::debugJson);
    }

    /** One summary row per package, for comparing them side by side. */
    @GetMapping("/parse-all")
    public ObjectNode parseAll() {
        ObjectNode n = VnfdJson.object();
        ArrayNode rows = VnfdJson.array();
        for (PackageCatalog.Entry e : catalog.list()) {
            ObjectNode row = ParseApi.parse(e.dir()).summaryJson();
            row.put("kind", e.kind().name().toLowerCase());
            rows.add(row);
        }
        n.put("count", rows.size());
        n.set("results", rows);
        return n;
    }

    // ------------------------------------------------------------------ plumbing

    private ResponseEntity<JsonNode> respond(String pkg, String dir,
            java.util.function.Function<ParseApi.Report, ObjectNode> view) {
        Optional<Path> resolved = resolve(pkg, dir);
        if (!resolved.isPresent()) {
            return ResponseEntity.badRequest().body(error(
                    "Pass ?pkg=<name> for a bundled package or ?dir=<path> for any directory"));
        }
        if (!Files.isDirectory(resolved.get())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(error("Not a directory: " + resolved.get()));
        }
        ParseApi.Report report = ParseApi.parse(resolved.get());
        return ResponseEntity
                .status(report.ok() ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY)
                .body(view.apply(report));
    }

    private Optional<Path> resolve(String pkg, String dir) {
        if (pkg != null && !pkg.isEmpty()) {
            return catalog.find(pkg).map(PackageCatalog.Entry::dir);
        }
        if (dir != null && !dir.isEmpty()) {
            return Optional.of(Paths.get(dir));
        }
        return Optional.empty();
    }

    private static ObjectNode error(String message) {
        ObjectNode n = VnfdJson.object();
        n.put("error", message);
        return n;
    }

    /**
     * A bug in the demo, not in the descriptor - say which.
     *
     * <p>Anything the library throws for a bad package is already caught by {@link ParseApi} and
     * returned as a 422, so reaching here means something else went wrong and the stack trace is
     * worth more than a blank 500.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<JsonNode> unexpected(RuntimeException e) {
        ObjectNode n = VnfdJson.object();
        n.put("error", "Unexpected failure in the demo");
        n.put("type", e.getClass().getName());
        n.put("message", String.valueOf(e.getMessage()));
        ArrayNode trace = VnfdJson.array();
        StackTraceElement[] frames = e.getStackTrace();
        for (int i = 0; i < Math.min(frames.length, 12); i++) {
            trace.add(frames[i].toString());
        }
        n.set("stackTrace", trace);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(n);
    }
}
