package com.example.etsi.vnfd.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Entry point.
 *
 * <pre>
 *   mvn spring-boot:run                                    # http://localhost:8080
 *   mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=9000
 *   mvn package &amp;&amp; java -jar target/etsi-vnfd-parser-demo-0.1.0-SNAPSHOT.jar
 * </pre>
 *
 * <p>Everything the library does is reachable from {@link ParseApi}, which knows nothing about HTTP.
 * {@link VnfdController} is the thin layer that turns a request into a call on it - so a host
 * application wiring the parser into its own controllers can copy ParseApi and ignore the rest.
 */
@SpringBootApplication
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    /**
     * Where the bundled packages are found.
     *
     * <p>A single bean rather than a component scan over {@link PackageCatalog}: locating the
     * repository walks the filesystem, and doing it once at startup means a wrong working directory
     * fails immediately with a clear message instead of on the first request.
     */
    @Bean
    public PackageCatalog packageCatalog() {
        return PackageCatalog.discover();
    }
}
