package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RespondToOrderData;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static uk.gov.hmcts.reform.bsp.common.mapper.AddressMapper.Field.LINE_1;
import static uk.gov.hmcts.reform.bsp.common.mapper.AddressMapper.Field.POSTCODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CASE_TYPE_ID_CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CASE_TYPE_ID_CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.PAPER_APPLICATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.AMENDED_CONSENT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPROVED_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENT_D81_QUESTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_CONSENT_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_RESPONSIBLE_FOR_DRAFTING_ORDER;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommonFunction {

    public static String nullToEmpty(Object o) {
        return o == null ? StringUtils.EMPTY : o.toString();
    }

    static final Function<List<Map>, Map>
        getLastMapValue = listMap ->
        listMap.stream().reduce((first, second) -> second).get();

    static final Function<List<Map>, Map>
        getFirstMapValue = listMap ->
        listMap.stream().findFirst().orElse(null);

    public static boolean addressLineOneAndPostCodeAreBothNotEmpty(Map address) {
        return  ObjectUtils.isNotEmpty(address)
            && StringUtils.isNotBlank((String) address.get(LINE_1))
            && StringUtils.isNotBlank((String) address.get(POSTCODE));
    }

    public static String buildFullName(Map<String, Object> caseData, String firstMiddleNameCcdFieldName, String lastNameCcdFieldName) {
        return (
            nullToEmpty((caseData.get(firstMiddleNameCcdFieldName))).trim()
                + " "
                + nullToEmpty((caseData.get(lastNameCcdFieldName))).trim()
        ).trim();
    }

    public static String buildFullApplicantName(CaseDetails caseDetails) {
        return buildFullName(caseDetails.getData(), APPLICANT_FIRST_MIDDLE_NAME, APPLICANT_LAST_NAME);
    }

    public static String buildFullRespondentName(CaseDetails caseDetails) {
        boolean isConsentedApplication = isConsentedApplication(caseDetails);
        return buildFullName(caseDetails.getData(),
            isConsentedApplication ? CONSENTED_RESPONDENT_FIRST_MIDDLE_NAME : CONTESTED_RESPONDENT_FIRST_MIDDLE_NAME,
            isConsentedApplication ? CONSENTED_RESPONDENT_LAST_NAME : CONTESTED_RESPONDENT_LAST_NAME);
    }

    public static boolean isApplicantRepresentedByASolicitor(Map<String, Object> caseData) {
        return YES_VALUE.equalsIgnoreCase(nullToEmpty(caseData.get(APPLICANT_REPRESENTED)));
    }

    public static boolean isApplicantSolicitorAgreeToReceiveEmails(CaseDetails caseDetails) {
        boolean isContestedApplication = isContestedApplication(caseDetails);
        Map<String, Object> caseData = caseDetails.getData();
        return (isContestedApplication && YES_VALUE.equalsIgnoreCase(nullToEmpty(caseData.get(APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONTESTED))))
            || (!isContestedApplication && YES_VALUE.equalsIgnoreCase(nullToEmpty(caseData.get(APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONSENTED))));
    }

    public static boolean isRespondentRepresentedByASolicitor(Map<String, Object> caseData) {
        return YES_VALUE.equalsIgnoreCase(nullToEmpty(caseData.get(CONSENTED_RESPONDENT_REPRESENTED)))
            || YES_VALUE.equalsIgnoreCase(nullToEmpty(caseData.get(CONTESTED_RESPONDENT_REPRESENTED)));
    }

    public static boolean isPaperApplication(Map<String, Object> caseData) {
        return YES_VALUE.equalsIgnoreCase(Objects.toString(caseData.get(PAPER_APPLICATION)));
    }

    public static boolean isConsentedInContestedCase(CaseDetails caseDetails) {
        return isContestedApplication(caseDetails) && caseDetails.getData().get(CONSENT_D81_QUESTION) != null;
    }

    public static boolean isNotEmpty(String field, Map<String, Object> caseData) {
        return StringUtils.isNotEmpty(nullToEmpty(caseData.get(field)));
    }

    public static boolean isAmendedConsentOrderType(RespondToOrderData respondToOrderData) {
        return AMENDED_CONSENT_ORDER.equalsIgnoreCase(respondToOrderData.getRespondToOrder().getDocumentType());
    }

    public static boolean isApplicantSolicitorResponsibleToDraftOrder(Map<String, Object> caseData) {
        return APPLICANT_SOLICITOR.equals(nullToEmpty(caseData.get(SOLICITOR_RESPONSIBLE_FOR_DRAFTING_ORDER)));
    }

    public static boolean isConsentedApplication(CaseDetails caseDetails) {
        return CASE_TYPE_ID_CONSENTED.equalsIgnoreCase(nullToEmpty(caseDetails.getCaseTypeId()));
    }

    public static boolean isContestedApplication(CaseDetails caseDetails) {
        return CASE_TYPE_ID_CONTESTED.equalsIgnoreCase(nullToEmpty(caseDetails.getCaseTypeId()));
    }

    public static boolean isContestedPaperApplication(CaseDetails caseDetails) {
        return isContestedApplication(caseDetails) && isPaperApplication(caseDetails.getData());
    }

    public static boolean isOrderApprovedCollectionPresent(Map<String, Object> caseData) {
        return isConsentedApprovedOrderCollectionPresent(caseData)
            || isContestedApprovedOrderCollectionPresent(caseData);
    }

    private static boolean isConsentedApprovedOrderCollectionPresent(Map<String, Object> caseData) {
        return caseData.get(APPROVED_ORDER_COLLECTION) != null && !((List<Map>) caseData.get(APPROVED_ORDER_COLLECTION)).isEmpty();
    }

    private static boolean isContestedApprovedOrderCollectionPresent(Map<String, Object> caseData) {
        return caseData.get(CONTESTED_CONSENT_ORDER_COLLECTION) != null && !((List<Map>) caseData.get(CONTESTED_CONSENT_ORDER_COLLECTION)).isEmpty();
    }

    public static boolean isDocumentPresentInCaseData(String documentName, CaseDetails caseDetails) {
        return caseDetails.getData().containsKey(documentName);
    }
}
