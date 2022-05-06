package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_ROLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_FIRM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_FIRM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_PHONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_DX_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_FIRM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NOTIFICATIONS_EMAIL_CONSENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_PHONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_PHONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_REFERENCE;

public class NoCSolicitorDetailsHelper {

    public static Map<String, Object> removeSolicitorAddress(CaseDetails caseDetails, boolean isContested) {
        Map<String, Object> caseData = caseDetails.getData();

        if (caseData.get(CASE_ROLE).equals(APP_SOLICITOR_POLICY)) {
            removeApplicantSolicitorAddress(caseData, isContested);
        } else if (caseData.get(CASE_ROLE).equals(RESP_SOLICITOR_POLICY)) {
            removeRespondentSolicitorAddress(caseData);
        }

        caseData.put(CASE_ROLE, null);
        return caseData;
    }

    public static void removeApplicantSolicitorAddress(Map<String, Object> caseData, boolean isContested) {
        caseData.put(isContested ? CONTESTED_SOLICITOR_NAME : CONSENTED_SOLICITOR_NAME, null);
        caseData.put(isContested ? CONTESTED_SOLICITOR_FIRM : CONSENTED_SOLICITOR_FIRM, null);
        caseData.put(SOLICITOR_REFERENCE, null);
        caseData.put(isContested ? CONTESTED_SOLICITOR_ADDRESS : CONSENTED_SOLICITOR_ADDRESS, null);
        caseData.put(isContested ? CONTESTED_SOLICITOR_PHONE : SOLICITOR_PHONE, null);
        caseData.put(isContested ? CONTESTED_SOLICITOR_EMAIL : SOLICITOR_EMAIL, null);
        caseData.put(isContested ? APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONTESTED : APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONSENTED, null);

        if (!isContested) {
            caseData.put(APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONSENTED, null);
        }

    }

    public static void removeRespondentSolicitorAddress(Map<String, Object> caseData) {
        caseData.put(RESP_SOLICITOR_NAME, null);
        caseData.put(RESP_SOLICITOR_FIRM, null);
        caseData.put(RESP_SOLICITOR_REFERENCE, null);
        caseData.put(RESP_SOLICITOR_ADDRESS, null);
        caseData.put(RESP_SOLICITOR_PHONE, null);
        caseData.put(RESP_SOLICITOR_EMAIL, null);
        caseData.put(RESP_SOLICITOR_DX_NUMBER, null);
        caseData.put(RESP_SOLICITOR_NOTIFICATIONS_EMAIL_CONSENT, null);
    }
}
