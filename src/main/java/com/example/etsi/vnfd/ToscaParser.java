package com.example.etsi.vnfd;

import com.example.etsi.vnfd.csar.DirectoryCsarReader;
import com.example.etsi.vnfd.map.ParseResult;
import com.example.etsi.vnfd.map.VnfdLoader;
import com.example.etsi.vnfd.template.ServiceToscaTemplate;
import com.example.etsi.vnfd.template.converter.YamlService;
import com.example.etsi.vnfd.validation.Findings;
import java.nio.file.Path;

/**
 * The library, in one call: an extracted VNF package directory to a VNFD.
 *
 * <p>Two stages sit behind it. The first reads the package - SOL004 V5.1.1 for the CSAR layout,
 * TOSCA Simple Profile YAML 1.3 and SOL001 V5.4.1 for what the files say - and the second
 * interprets what it read as the information model of IFA011 V5.4.1. Both report into the same
 * {@link Findings}, because a package can be faulted at either stage and a caller wants one list.
 *
 * <p>Callers who need the intermediate TOSCA view, or who want to feed the two stages separately,
 * can still use them directly; this is the path that covers the ordinary case.
 */
public final class ToscaParser {

    private ToscaParser() {
    }

    /**
     * Parses an extracted VNF package directory.
     *
     * @throws com.example.etsi.vnfd.map.VnfdParseException when the package cannot be read at all,
     *     as distinct from being read and found non-conformant, which is reported as findings
     */
    public static ParseResult parse(Path packageDir) {
        Findings findings = new Findings();
        ServiceToscaTemplate template =
                new YamlService().parse(new DirectoryCsarReader(packageDir), findings);
        return new VnfdLoader().load(template, findings);
    }
}
