package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.address;

import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

public interface AddresseeGenerator {
    AddresseeDetails generate(CaseDetails caseDetails);

    AddresseeDetails generate(FinremCaseDetails caseDetails);
}
