package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

public interface HasDocumentLink extends HasCaseDocument {

    CaseDocument getDocumentLink();
}
