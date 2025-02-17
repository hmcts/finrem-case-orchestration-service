package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RespondToOrderData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RespondToOrderDocumentCollection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static uk.gov.hmcts.reform.bsp.common.mapper.AddressMapper.Field.LINE_1;
import static uk.gov.hmcts.reform.bsp.common.mapper.AddressMapper.Field.POSTCODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.PAPER_APPLICATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.GENERAL_ORDER_CONSENT_IN_CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.AMEND_CONSENT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_CONFIDENTIAL_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENT_D81_QUESTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENT_ORDER_FRC_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENT_ORDER_FRC_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENT_ORDER_FRC_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENT_ORDER_FRC_PHONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_ADDRESS_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_EMAIL_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_NAME_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_PHONE_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER1_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER1_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER2_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER2_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER3_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER3_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER4_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER4_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_CONFIDENTIAL_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NOTIFICATIONS_EMAIL_CONSENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_RESPONSIBLE_FOR_DRAFTING_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo.YES;

@Service
@Slf4j
@RequiredArgsConstructor
@SuppressWarnings("java:S3740")
public class CaseDataService {

    public final Function<List<Map>, Map> getLastMapValue = listMap -> listMap.stream().reduce((first, second) -> second).get();

    public final ObjectMapper objectMapper;

    public static String nullToEmpty(Object o) {
        return Objects.toString(o, "");
    }

    public boolean isRespondentSolicitorResponsibleToDraftOrder(Map<String, Object> caseData) {
        return RESPONDENT_SOLICITOR.equals(nullToEmpty(caseData.get(SOLICITOR_RESPONSIBLE_FOR_DRAFTING_ORDER)));
    }

    public void moveCollection(Map<String, Object> caseData, String sourceFieldName, String destinationFieldName) {
        if (canCollectionsBeCopiedFromTo(caseData, sourceFieldName, destinationFieldName)) {
            copyCollection(caseData, sourceFieldName, destinationFieldName, true);
            caseData.put(sourceFieldName, null);
        }
    }

    public void overwriteCollection(Map<String, Object> caseData, String sourceFieldName, String destinationFieldName) {
        copyCollection(caseData, sourceFieldName, destinationFieldName, false);
    }

    private void copyCollection(Map<String, Object> caseData, String sourceFieldName, String destinationFieldName, boolean appendToDestination) {
        if (canCollectionsBeCopiedFromTo(caseData, sourceFieldName, destinationFieldName)) {
            final List destinationList = new ArrayList();
            if (appendToDestination && caseData.get(destinationFieldName) != null) {
                destinationList.addAll((List) caseData.get(destinationFieldName));
            }
            destinationList.addAll((List) caseData.get(sourceFieldName));
            caseData.put(destinationFieldName, destinationList);
        }
    }

    @SuppressWarnings("java:S4201")
    private boolean canCollectionsBeCopiedFromTo(Map<String, Object> caseData, String sourceFieldName, String destinationFieldName) {
        return caseData.get(sourceFieldName) != null && (caseData.get(sourceFieldName) instanceof Collection)
            && (caseData.get(destinationFieldName) == null || (caseData.get(destinationFieldName) instanceof Collection));
    }

    public boolean addressLineOneAndPostCodeAreBothNotEmpty(Map address) {
        return ObjectUtils.isNotEmpty(address)
            && addressLineOneNotEmpty(address)
            && postcodeNotEmpty(address);
    }

    public boolean addressLineOneNotEmpty(Map address) {
        if (address != null) {
            return StringUtils.isNotBlank((String) address.get(LINE_1));
        }
        return false;
    }

    private boolean postcodeNotEmpty(Map address) {
        return StringUtils.isNotBlank((String) address.get(POSTCODE));
    }

    public String buildFullName(Map<String, Object> caseData, String firstMiddleNameCcdFieldName, String lastNameCcdFieldName) {
        return (
            nullToEmpty((caseData.get(firstMiddleNameCcdFieldName))).trim()
                + " "
                + nullToEmpty((caseData.get(lastNameCcdFieldName))).trim()
        ).trim();
    }

    public String buildFullApplicantName(CaseDetails caseDetails) {
        return buildFullName(caseDetails.getData(), APPLICANT_FIRST_MIDDLE_NAME, APPLICANT_LAST_NAME);
    }

    public String buildFullApplicantName(FinremCaseDetails caseDetails) {
        return caseDetails.getData().getFullApplicantName();
    }

    public String buildFullRespondentName(CaseDetails caseDetails) {
        boolean isConsentedApplication = isConsentedApplication(caseDetails);
        return buildFullName(caseDetails.getData(),
            isConsentedApplication ? CONSENTED_RESPONDENT_FIRST_MIDDLE_NAME : CONTESTED_RESPONDENT_FIRST_MIDDLE_NAME,
            isConsentedApplication ? CONSENTED_RESPONDENT_LAST_NAME : CONTESTED_RESPONDENT_LAST_NAME);
    }

    public String buildFullRespondentName(FinremCaseDetails caseDetails) {
        return caseDetails.getData().getRespondentFullName();
    }

    public String buildFullIntervener1Name(CaseDetails caseDetails) {
        return buildFullName(caseDetails.getData(), INTERVENER1_FIRST_MIDDLE_NAME, INTERVENER1_LAST_NAME);
    }

    public String buildFullIntervener2Name(CaseDetails caseDetails) {
        return buildFullName(caseDetails.getData(), INTERVENER2_FIRST_MIDDLE_NAME, INTERVENER2_LAST_NAME);
    }

    public String buildFullIntervener3Name(CaseDetails caseDetails) {
        return buildFullName(caseDetails.getData(), INTERVENER3_FIRST_MIDDLE_NAME, INTERVENER3_LAST_NAME);
    }

    public String buildFullIntervener4Name(CaseDetails caseDetails) {
        return buildFullName(caseDetails.getData(), INTERVENER4_FIRST_MIDDLE_NAME, INTERVENER4_LAST_NAME);
    }

    /**
     * Sets PowerBI fields used for tracking and gather statistics.
     * Note that these tracking fields are used both consented and contested application
     * in-spite of the 'Consent_Order_FRC...' naming.
     *
     * @param caseDetails the case data as a {@code Map<String, Object>}
     */
    @SuppressWarnings("squid:CallToDeprecatedMethod")
    public void setFinancialRemediesCourtDetails(CaseDetails caseDetails) {
        Map<String, Object> courtDetails = CaseHearingFunctions.buildFrcCourtDetails(caseDetails.getData());
        caseDetails.getData().put(CONSENT_ORDER_FRC_NAME, courtDetails.get(COURT_DETAILS_NAME_KEY));
        caseDetails.getData().put(CONSENT_ORDER_FRC_ADDRESS, courtDetails.get(COURT_DETAILS_ADDRESS_KEY));
        caseDetails.getData().put(CONSENT_ORDER_FRC_EMAIL, courtDetails.get(COURT_DETAILS_EMAIL_KEY));
        caseDetails.getData().put(CONSENT_ORDER_FRC_PHONE, courtDetails.get(COURT_DETAILS_PHONE_KEY));
    }

    /**
     * Checks if the applicant is represented by a solicitor.
     *
     * @param caseData the case data as a {@code Map<String, Object>}
     * @return true if the applicant is represented by a solicitor, otherwise false
     * @deprecated since 11-December-2024. Use {@link #isApplicantRepresentedByASolicitor(FinremCaseData)}
     *             instead. This method is deprecated due to the transition to the {@code FinremCaseData} class,
     *             which provides a strongly typed representation of case data.
     */
    @Deprecated(since = "11-December-2024")
    public boolean isApplicantRepresentedByASolicitor(Map<String, Object> caseData) {
        return YES_VALUE.equalsIgnoreCase(nullToEmpty(caseData.get(APPLICANT_REPRESENTED)));
    }

    /**
     * Checks if the applicant is represented by a solicitor.
     *
     * <p>This method uses the {@link FinremCaseData} object to determine whether
     * the applicant has legal representation.</p>
     *
     * @param caseData the {@link FinremCaseData} containing the case details
     * @return true if the applicant is represented by a solicitor, otherwise false
     */
    public boolean isApplicantRepresentedByASolicitor(FinremCaseData caseData) {
        return caseData.isApplicantRepresentedByASolicitor();
    }

    /**
     * Checks if the applicant's solicitor agrees to receive emails.
     *
     * <p>This method determines the solicitor's agreement based on the type of application
     * (contested or consented) and the respective case data fields.</p>
     *
     * @param caseDetails the {@link CaseDetails} containing the case data
     * @return true if the applicant's solicitor agrees to receive emails, otherwise false
     * @deprecated since 11-December-2024. Use {@link #isApplicantSolicitorAgreeToReceiveEmails(FinremCaseDetails)}
     *             instead. This method is deprecated due to the migration to the {@code FinremCaseDetails} class,
     *             which provides enhanced type safety and consistency with the updated data model.
     */
    @Deprecated(since = "11-December-2024")
    public boolean isApplicantSolicitorAgreeToReceiveEmails(CaseDetails caseDetails) {
        boolean isContestedApplication = isContestedApplication(caseDetails);
        Map<String, Object> caseData = caseDetails.getData();
        return (isContestedApplication && YES_VALUE.equalsIgnoreCase(nullToEmpty(caseData.get(APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONTESTED))))
            || (!isContestedApplication && YES_VALUE.equalsIgnoreCase(nullToEmpty(caseData.get(APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONSENTED))));
    }

    /**
     * Checks if the applicant's solicitor agrees to receive emails.
     *
     * <p>This method determines the solicitor's agreement based on the type of application
     * (contested or consented) and the respective case data fields.</p>
     *
     * @param caseDetails the {@link FinremCaseDetails} containing the case data
     * @return true if the applicant's solicitor agrees to receive emails, otherwise false
     */
    public boolean isApplicantSolicitorAgreeToReceiveEmails(FinremCaseDetails caseDetails) {
        boolean isContestedApplication = isContestedApplication(caseDetails);
        FinremCaseData caseData = caseDetails.getData();
        return (isContestedApplication && YES == caseData.getContactDetailsWrapper().getApplicantSolicitorConsentForEmails())
            || (!isContestedApplication && YES == caseData.getContactDetailsWrapper().getSolicitorAgreeToReceiveEmails());
    }

    public boolean isApplicantSolicitorEmailPopulated(CaseDetails caseDetails) {
        boolean isContestedApplication = isContestedApplication(caseDetails);
        Map<String, Object> caseData = caseDetails.getData();
        return (isContestedApplication && isNotEmpty(CONTESTED_SOLICITOR_EMAIL, caseData))
            || (!isContestedApplication && isNotEmpty(SOLICITOR_EMAIL, caseData));
    }

    public boolean isRespondentSolicitorAgreeToReceiveEmails(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();
        return YES_VALUE.equalsIgnoreCase(nullToEmpty(caseData.get(RESP_SOLICITOR_NOTIFICATIONS_EMAIL_CONSENT)));
    }

    /**
     * Checks if the respondent is represented by a solicitor.
     *
     * <p>This method evaluates the representation status of the respondent by checking
     * both consented and contested application fields in the case data map.</p>
     *
     * @param caseData the case data as a {@code Map<String, Object>}
     * @return true if the respondent is represented by a solicitor, otherwise false
     * @deprecated since 11-December-2024. Use {@link #isRespondentRepresentedByASolicitor(FinremCaseData)} instead.
     *             This method is deprecated due to the transition to the {@code FinremCaseData} class, which provides a
     *             strongly typed data structure for better consistency and maintainability.
     */
    @Deprecated(since = "11-December-2024")
    public boolean isRespondentRepresentedByASolicitor(Map<String, Object> caseData) {
        return YES_VALUE.equalsIgnoreCase(nullToEmpty(caseData.get(CONSENTED_RESPONDENT_REPRESENTED)))
            || YES_VALUE.equalsIgnoreCase(nullToEmpty(caseData.get(CONTESTED_RESPONDENT_REPRESENTED)));
    }

    /**
     * Checks if the respondent is represented by a solicitor.
     *
     * <p>This method evaluates the representation status of the respondent by checking
     * both consented and contested application fields in the {@link FinremCaseData}.</p>
     *
     * @param caseData the {@link FinremCaseData} containing the case details
     * @return true if the respondent is represented by a solicitor, otherwise false
     */
    public boolean isRespondentRepresentedByASolicitor(FinremCaseData caseData) {
        return YES == caseData.getContactDetailsWrapper().getConsentedRespondentRepresented()
            || YES == caseData.getContactDetailsWrapper().getContestedRespondentRepresented();
    }

    public boolean isPaperApplication(Map<String, Object> caseData) {
        return YES_VALUE.equalsIgnoreCase(Objects.toString(caseData.get(PAPER_APPLICATION)));
    }

    public boolean isPaperApplication(FinremCaseData caseData) {
        return YES_VALUE.equalsIgnoreCase(Objects.toString(caseData.getPaperApplication()));
    }

    /**
     * Please upgrade your code.
     * This method will be removed in future versions.
     *
     * <p>Use @link isConsentedInContestedCase(FinremCaseDetails caseDetails) instead </p>
     *
     * @return boolean to be return
     * @deprecated deprecated since 05-Sep-2023
     */
    @Deprecated(since = "15-Feb-2023")
    @SuppressWarnings("java:S1133")
    public boolean isConsentedInContestedCase(CaseDetails caseDetails) {
        return isContestedApplication(caseDetails) && caseDetails.getData().get(CONSENT_D81_QUESTION) != null;
    }

    public boolean isConsentedInContestedCase(FinremCaseDetails caseDetails) {
        return isContestedApplication(caseDetails) && caseDetails.getData().getConsentOrderWrapper().getConsentD81Question() != null;
    }

    public boolean hasConsentOrder(FinremCaseData caseData) {
        return caseData.getConsentOrderWrapper().getConsentD81Question() != null;
    }

    public boolean isConsentInContestedGeneralOrderEvent(EventType eventId) {
        return GENERAL_ORDER_CONSENT_IN_CONTESTED.equals(eventId);
    }

    public boolean isNotEmpty(String field, Map<String, Object> caseData) {
        return StringUtils.isNotEmpty(nullToEmpty(caseData.get(field)));
    }

    public boolean isAmendedConsentOrderType(RespondToOrderData respondToOrderData) {
        return AMEND_CONSENT_ORDER.equalsIgnoreCase(respondToOrderData.getRespondToOrder().getDocumentType());
    }

    public boolean isAmendedConsentOrderTypeFR(RespondToOrderDocumentCollection respondToOrderDocumentCollection) {
        return AMEND_CONSENT_ORDER.equalsIgnoreCase(respondToOrderDocumentCollection.getValue().getDocumentType().getValue());
    }

    public boolean isApplicantSolicitorResponsibleToDraftOrder(Map<String, Object> caseData) {
        return APPLICANT_SOLICITOR.equals(nullToEmpty(caseData.get(SOLICITOR_RESPONSIBLE_FOR_DRAFTING_ORDER)));
    }

    public boolean isConsentedApplication(CaseDetails caseDetails) {
        return CaseType.CONSENTED.getCcdType().equalsIgnoreCase(nullToEmpty(caseDetails.getCaseTypeId()));
    }

    public boolean isConsentedApplication(FinremCaseDetails caseDetails) {
        return CaseType.CONSENTED.getCcdType().equalsIgnoreCase(nullToEmpty(caseDetails.getCaseType().getCcdType()));
    }

    /**
     * Please upgrade your code.
     * This method will be removed in future versions.
     *
     * <p>Use @link isContestedApplication(FinremCaseDetails caseDetails) instead </p>
     *
     * @return boolean to be return
     * @deprecated deprecated since 05-Sep-2023
     */
    @Deprecated(since = "05-Sep-2023")
    @SuppressWarnings("java:S1133")
    public boolean isContestedApplication(CaseDetails caseDetails) {
        return CaseType.CONTESTED.getCcdType().equalsIgnoreCase(nullToEmpty(caseDetails.getCaseTypeId()));
    }

    public boolean isContestedApplication(FinremCaseDetails caseDetails) {
        return CaseType.CONTESTED.getCcdType().equalsIgnoreCase(nullToEmpty(caseDetails.getCaseType().getCcdType()));
    }

    public boolean isContestedPaperApplication(CaseDetails caseDetails) {
        return isContestedApplication(caseDetails) && isPaperApplication(caseDetails.getData());
    }

    public boolean isOrderApprovedCollectionPresent(FinremCaseData caseData) {
        return isConsentedApprovedOrderCollectionPresent(caseData)
            || isContestedApprovedOrderCollectionPresent(caseData);
    }

    private boolean isConsentedApprovedOrderCollectionPresent(FinremCaseData caseData) {
        return caseData.getApprovedOrderCollection() != null && !caseData.getApprovedOrderCollection().isEmpty();
    }

    private boolean isContestedApprovedOrderCollectionPresent(FinremCaseData caseData) {
        return caseData.getConsentOrderWrapper().getContestedConsentedApprovedOrders() != null
            && !caseData.getConsentOrderWrapper().getContestedConsentedApprovedOrders().isEmpty();
    }

    /**
     * Please upgrade your code.
     * This method will be removed in future versions.
     *
     * <p>Use @link isApplicantAddressConfidential(FinremCaseData caseData) instead </p>
     *
     * @return boolean to be returned
     * @deprecated deprecated since 05-Sep-2023
     */
    @Deprecated(since = "05-september-2023")
    @SuppressWarnings("java:S1133")
    public boolean isApplicantAddressConfidential(Map<String, Object> caseData) {
        return isAddressConfidential(caseData, APPLICANT_CONFIDENTIAL_ADDRESS);
    }

    public boolean isApplicantAddressConfidential(FinremCaseData caseData) {
        return caseData.getContactDetailsWrapper().getApplicantAddressHiddenFromRespondent() != null
            && caseData.getContactDetailsWrapper().getApplicantAddressHiddenFromRespondent().isYes();
    }

    /**
     * Please upgrade your code.
     * This method will be removed in future versions.
     *
     * <p>Use @link isRespondentAddressConfidential(FinremCaseData caseData) instead </p>
     *
     * @return boolean to be returned
     * @deprecated deprecated since 05-Sep-2023
     */
    @Deprecated(since = "05-september-2023")
    @SuppressWarnings("java:S1133")
    public boolean isRespondentAddressConfidential(Map<String, Object> caseData) {
        return isAddressConfidential(caseData, RESPONDENT_CONFIDENTIAL_ADDRESS);
    }

    public boolean isRespondentAddressConfidential(FinremCaseData caseData) {
        return caseData.getContactDetailsWrapper().getRespondentAddressHiddenFromApplicant() != null
            && caseData.getContactDetailsWrapper().getRespondentAddressHiddenFromApplicant().isYes();
    }

    private boolean isAddressConfidential(Map<String, Object> caseData, String address) {
        return YES_VALUE.equalsIgnoreCase(Objects.toString(caseData.get(address)));
    }

    public boolean isContestedOrderNotApprovedCollectionPresent(FinremCaseData caseData) {
        return caseData.getConsentOrderWrapper().getConsentedNotApprovedOrders() != null
            && !caseData.getConsentOrderWrapper().getConsentedNotApprovedOrders().isEmpty();
    }

    @SuppressWarnings("java:S112")
    public Map<String, Object> getPayloadOffFinremCaseData(FinremCaseData data) {
        Map<String, Object> notificationRequestPayload;
        try {
            notificationRequestPayload =
                objectMapper.readValue(objectMapper.writeValueAsString(data), HashMap.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error while converting finrem case details pojo into map payload", e);
        }
        return notificationRequestPayload;
    }

    public boolean isLitigantRepresented(CaseDetails caseDetails, boolean isApplicant) {
        String isRepresented = (String) caseDetails.getData().get(getLitigantRepresentedKey(caseDetails, isApplicant));

        return YES_VALUE.equalsIgnoreCase(isRepresented);
    }

    private String getLitigantRepresentedKey(CaseDetails caseDetails, boolean isApplicant) {
        return isApplicant ? APPLICANT_REPRESENTED : getRespondentRepresentedKey(caseDetails);
    }

    private String getRespondentRepresentedKey(CaseDetails caseDetails) {
        return isConsentedApplication(caseDetails)
            ? CONSENTED_RESPONDENT_REPRESENTED : CONTESTED_RESPONDENT_REPRESENTED;
    }
}
