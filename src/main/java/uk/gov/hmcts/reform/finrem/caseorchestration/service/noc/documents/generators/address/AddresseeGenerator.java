package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.generators.address;

import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.finrem.ccd.domain.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;

public interface AddresseeGenerator {

    Addressee generate(FinremCaseDetails caseDetails, ChangedRepresentative changedRepresentative, String party);
}
