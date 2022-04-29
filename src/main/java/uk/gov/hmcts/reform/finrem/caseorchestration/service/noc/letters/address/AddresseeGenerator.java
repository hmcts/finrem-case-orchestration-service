package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.address;

import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

public interface AddresseeGenerator {

    Addressee generate(CaseDetails caseDetails);
}
