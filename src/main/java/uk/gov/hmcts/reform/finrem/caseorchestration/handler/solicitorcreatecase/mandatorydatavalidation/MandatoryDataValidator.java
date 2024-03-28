package uk.gov.hmcts.reform.finrem.caseorchestration.handler.solicitorcreatecase.mandatorydatavalidation;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;

import java.util.List;

interface MandatoryDataValidator {

    List<String> validate(FinremCaseData caseData);
}
