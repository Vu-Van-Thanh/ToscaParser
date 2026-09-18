package com.example.etsi.vnfd.demo;

import com.example.etsi.vnfd.model.Vdu;
import com.example.etsi.vnfd.validation.Finding;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Entry point.
 *
 * <p>No arguments starts the HTTP server; an argument parses that one package and prints the result,
 * which is the quickest way to try a descriptor without a client.
 *
 * <pre>
 *   mvn exec:java                                   # server on :8080
 *   mvn exec:java -Dexec.args="--port 9000"
 *   mvn exec:java -Dexec.args="ExampleCorp_SimpleWebCnf_vnf_pkg"
 *   mvn exec:java -Dexec.args="D:/some/other/vnf_pkg --json"
 * </pre>
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) throws Exception {
        int port = 8080;
        boolean json = false;
        String target = null;

        for (int i = 0; i < args.length; i++) {
            String a = args[i];
            if ("--port".equals(a) && i + 1 < args.length) {
                port = Integer.parseInt(args[++i]);
            } else if ("--json".equals(a)) {
                json = true;
            } else if ("--help".equals(a) || "-h".equals(a)) {
                usage();
                return;
            } else {
                target = a;
            }
        }

        if (target == null) {
            new VnfdHttpServer(port, PackageCatalog.discover()).start();
            return;
        }
        parseOne(target, json);
    }

    private static void parseOne(String target, boolean json) {
        PackageCatalog catalog = PackageCatalog.discover();
        Path dir = Paths.get(target);
        if (!Files.isDirectory(dir)) {
            Optional<PackageCatalog.Entry> entry = catalog.find(target);
            if (!entry.isPresent()) {
                System.err.println("No such package: " + target);
                System.err.println("Known packages:");
                catalog.list().forEach(e -> System.err.println("  " + e));
                System.exit(2);
                return;
            }
            dir = entry.get().dir();
        }

        ParseApi.Report report = ParseApi.parse(dir);
        if (json) {
            System.out.println(VnfdJson.write(report.toJson(), true));
            return;
        }

        System.out.println("package  " + dir);
        if (!report.ok()) {
            System.out.println("FAILED   " + report.failure().get());
            System.exit(1);
            return;
        }

        System.out.println();
        System.out.println("--- VNFD ---");
        System.out.println(VnfdJson.write(report.vnfdJson(), true));

        System.out.println();
        System.out.printf("--- FINDINGS (%d) ---%n", report.findings().size());
        for (Finding f : report.findings()) {
            System.out.printf("%-5s %-8s %-46s %s%n",
                    f.severity(), f.ruleId(), f.clause(), f.message());
            f.source().ifPresent(s -> System.out.println("                 at " + s));
        }

        System.out.println();
        System.out.println("--- LCM REALIZATION ---");
        for (Vdu vdu : report.vnfd().getVdu()) {
            System.out.printf("%-24s %s%n", vdu.getVduId(), vdu.getLcmRealizationPath());
        }
    }

    private static void usage() {
        System.out.println("Usage:");
        System.out.println("  (no args)              start the HTTP server on :8080");
        System.out.println("  --port <n>             start the HTTP server on another port");
        System.out.println("  <package-name|dir>     parse one package and print the result");
        System.out.println("  <package> --json       print the full JSON instead of the summary");
    }
}
