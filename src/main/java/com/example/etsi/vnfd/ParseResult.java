package com.example.etsi.vnfd;

import com.example.etsi.vnfd.model.Vnfd;
import com.example.etsi.vnfd.template.ServiceToscaTemplate;
import com.example.etsi.vnfd.validation.Finding;
import com.example.etsi.vnfd.validation.Findings;
import com.example.etsi.vnfd.validation.Severity;
import java.util.List;

/**
 * What one parse produced.
 *
 * <p>Carries the TOSCA view beside the VNFD on purpose: when a value in the VNFD looks wrong, the
 * quickest way to settle it is to compare what was derived against what the descriptor actually
 * wrote, and that needs both in hand.
 */
public final class ParseResult {

    private final Vnfd vnfd;
    private final ServiceToscaTemplate template;
    private final List<Finding> findings;

    public ParseResult(Vnfd vnfd, ServiceToscaTemplate template, Findings findings) {
        this.vnfd = vnfd;
        this.template = template;
        this.findings = findings.asList();
    }

    /** The parsed VNFD. Always present; a package that could not be read throws instead. */
    public Vnfd getVnfd() {
        return vnfd;
    }

    /** The TOSCA view the VNFD was built from. */
    public ServiceToscaTemplate getTemplate() {
        return template;
    }

    /**
     * Everything noticed while reading the package, in the order it was noticed.
     *
     * <p>A descriptor that breaks a rule of SOL001 or IFA011 does not stop the parse: the VNFD is
     * still built and the violation reported here, so the host application decides what is fatal.
     * Only a package that cannot be read at all throws.
     */
    public List<Finding> getFindings() {
        return findings;
    }

    public List<Finding> getFindings(Severity severity) {
        List<Finding> out = new java.util.ArrayList<>();
        for (Finding finding : findings) {
            if (finding.severity() == severity) {
                out.add(finding);
            }
        }
        return out;
    }

    public boolean hasErrors() {
        return !getFindings(Severity.ERROR).isEmpty();
    }

    @Override
    public String toString() {
        return "ParseResult(" + vnfd.getVnfdId().orElse("?") + ")";
    }
}
