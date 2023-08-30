package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.address;

import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.AddresseeDetails;

public interface LetterAddresseeGenerator {
    AddresseeDetails generate(CaseDetails caseDetails);

    AddresseeDetails generate(FinremCaseDetails caseDetails);
}
