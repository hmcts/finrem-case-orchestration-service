package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

public interface DocumentMatcher {

    boolean match(CaseDocument targetDoc);
}
