package uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.postalhearingdocuments;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

import java.util.List;

@FunctionalInterface
public interface PostalHearingDocumentSupplier {
    List<CaseDocument> get(FinremCaseDetails finremCaseDetails);
}
