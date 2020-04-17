package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RespondToOrderData;

import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.AMENDED_CONSENT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.nullToEmpty;

public class CommonConditions {
    public static boolean isApplicantRepresented(Map<String, Object> caseData) {
        return YES_VALUE.equalsIgnoreCase(nullToEmpty(caseData.get(APPLICANT_REPRESENTED)));
    }

    public static boolean isRespondentRepresented(Map<String, Object> caseData) {
        return isNotEmpty(RESP_SOLICITOR_NAME, caseData);
    }

    public static boolean isNotEmpty(String field, Map<String, Object> caseData) {
        return StringUtils.isNotEmpty(nullToEmpty(caseData.get(field)));
    }

    public static boolean isAmendedConsentOrderType(RespondToOrderData respondToOrderData) {
        return AMENDED_CONSENT_ORDER.equalsIgnoreCase(respondToOrderData.getRespondToOrder().getDocumentType());
    }
}
