package com.example.etsi.vnfd.demo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Serves the parsed VNFD over HTTP.
 *
 * <p>Uses the HTTP server built into the JDK, so the demo adds no dependency of its own beyond
 * Jackson's tree API. That matters here: the point of this project is to prove the library works
 * when consumed from outside, and a framework in the way would blur what the library itself does.
 *
 * <p>Single-threaded on purpose. Parsing is fast and the interesting use is stepping through a
 * request in a debugger, which a thread pool only makes harder to follow.
 */
public final class VnfdHttpServer {

    private final int port;
    private final PackageCatalog catalog;

    public VnfdHttpServer(int port, PackageCatalog catalog) {
        this.port = port;
        this.catalog = catalog;
    }

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new Router());
        server.setExecutor(null);
        server.start();

        System.out.println("ETSI VNFD parser demo");
        System.out.println("  repository  " + catalog.repoRoot());
        System.out.println("  packages    " + catalog.list().size());
        System.out.println("  listening   http://localhost:" + port);
        System.out.println();
        System.out.println("  GET /packages");
        System.out.println("  GET /parse?pkg=<name>      full result: vnfd + findings");
        System.out.println("  GET /parse?dir=<path>      any package directory on disk");
        System.out.println("  GET /vnfd?pkg=<name>       the VNFD alone");
        System.out.println("  GET /findings?pkg=<name>   findings alone, with clauses");
        System.out.println("  GET /debug?pkg=<name>      TOSCA layer, incl. unbound node templates");
        System.out.println("  GET /parse-all             one summary row per package");
        System.out.println();
        System.out.println("  add &pretty=1 to indent");
    }

    private final class Router implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            Map<String, String> query = parseQuery(exchange.getRequestURI().getRawQuery());
            boolean pretty = query.containsKey("pretty") && !"0".equals(query.get("pretty"));

            try {
                if (!"GET".equals(exchange.getRequestMethod())) {
                    respond(exchange, 405, error("Only GET is supported"), pretty);
                    return;
                }
                switch (path) {
                    case "/":
                    case "/health":
                        respond(exchange, 200, health(), pretty);
                        return;
                    case "/packages":
                        respond(exchange, 200, packages(), pretty);
                        return;
                    case "/parse-all":
                        respond(exchange, 200, parseAll(), pretty);
                        return;
                    case "/parse":
                    case "/vnfd":
                    case "/findings":
                    case "/debug":
                        handleOne(exchange, path, query, pretty);
                        return;
                    default:
                        respond(exchange, 404, error("No such endpoint: " + path), pretty);
                }
            } catch (RuntimeException e) {
                // A bug in the demo, not in the descriptor. Say which, rather than dropping the
                // connection and leaving the caller guessing.
                respond(exchange, 500, unexpected(e), pretty);
            }
        }

        private void handleOne(HttpExchange exchange, String path, Map<String, String> query,
                boolean pretty) throws IOException {
            Optional<Path> dir = resolve(query);
            if (!dir.isPresent()) {
                respond(exchange, 400, error(
                        "Pass ?pkg=<name> for a bundled package or ?dir=<path> for any directory"),
                        pretty);
                return;
            }
            if (!Files.isDirectory(dir.get())) {
                respond(exchange, 404, error("Not a directory: " + dir.get()), pretty);
                return;
            }

            ParseApi.Report report = ParseApi.parse(dir.get());
            ObjectNode body;
            switch (path) {
                case "/vnfd":
                    body = report.vnfdJson();
                    break;
                case "/findings":
                    body = report.findingsJson();
                    break;
                case "/debug":
                    body = report.debugJson();
                    break;
                default:
                    body = report.toJson();
            }
            // 422 for a package that cannot be read at all; a package that reads but breaks a rule
            // is a perfectly good 200 whose findings carry the verdict.
            respond(exchange, report.ok() ? 200 : 422, body, pretty);
        }

        private Optional<Path> resolve(Map<String, String> query) {
            String pkg = query.get("pkg");
            if (pkg != null && !pkg.isEmpty()) {
                return catalog.find(pkg).map(PackageCatalog.Entry::dir);
            }
            String dir = query.get("dir");
            if (dir != null && !dir.isEmpty()) {
                return Optional.of(Paths.get(dir));
            }
            return Optional.empty();
        }
    }

    // ------------------------------------------------------------------ endpoints

    private ObjectNode health() {
        ObjectNode n = VnfdJson.object();
        n.put("service", "etsi-vnfd-parser-demo");
        n.put("repository", catalog.repoRoot().toString());
        n.put("packages", catalog.list().size());
        return n;
    }

    private ObjectNode packages() {
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

    private ObjectNode parseAll() {
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

    private static ObjectNode error(String message) {
        ObjectNode n = VnfdJson.object();
        n.put("error", message);
        return n;
    }

    private static ObjectNode unexpected(RuntimeException e) {
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
        return n;
    }

    private static void respond(HttpExchange exchange, int status, JsonNode body, boolean pretty)
            throws IOException {
        byte[] bytes = VnfdJson.write(body, pretty).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    private static Map<String, String> parseQuery(String rawQuery) {
        Map<String, String> out = new LinkedHashMap<>();
        if (rawQuery == null || rawQuery.isEmpty()) {
            return out;
        }
        for (String pair : rawQuery.split("&")) {
            int eq = pair.indexOf('=');
            try {
                if (eq < 0) {
                    out.put(URLDecoder.decode(pair, "UTF-8"), "");
                } else {
                    out.put(URLDecoder.decode(pair.substring(0, eq), "UTF-8"),
                            URLDecoder.decode(pair.substring(eq + 1), "UTF-8"));
                }
            } catch (IOException e) {
                throw new IllegalArgumentException("Cannot decode query: " + rawQuery, e);
            }
        }
        return out;
    }
}
