package uk.gov.hmcts.reform.finrem.caseorchestration.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ConsentedApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ContestedCourtHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Element;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdate;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.SolicitorCaseDataKeysWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_OPENING_HOURS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DIVORCE_CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_REJECT_REASON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_EMAIL_BODY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.REPRESENTATION_UPDATE_HISTORY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_REFERENCE;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationRequestMapper {

    protected static final String EMPTY_STRING = "";
    private static final String RESPONDENT = "Respondent";
    private static final String CONSENTED = "consented";
    private static final String CONTESTED = "contested";
    private final CaseDataService caseDataService;
    private final ConsentedApplicationHelper consentedApplicationHelper;
    private final ObjectMapper objectMapper;

    @Deprecated
    public NotificationRequest getNotificationRequestForRespondentSolicitor(CaseDetails caseDetails,
                                                                            Map<String, Object> interimHearingData) {
        return buildNotificationRequest(caseDetails, getCaseDataKeysForRespondentSolicitor(), interimHearingData);
    }

    public NotificationRequest getNotificationRequestForRespondentSolicitor(FinremCaseDetails caseDetails,
                                                                            Map<String, Object> interimHearingData) {
        return buildNotificationRequest(caseDetails, getCaseDataKeysForRespondentSolicitor(), interimHearingData);
    }

    public NotificationRequest getNotificationRequestForRespondentSolicitor(CaseDetails caseDetails) {
        return buildNotificationRequest(caseDetails, getCaseDataKeysForRespondentSolicitor());
    }

    @Deprecated
    public NotificationRequest getNotificationRequestForConsentApplicantSolicitor(CaseDetails caseDetails,
                                                                           Map<String, Object> hearingData) {
        return buildNotificationRequest(caseDetails, getConsentedCaseDataKeysForApplicantSolicitor(), hearingData);
    }

    public NotificationRequest getNotificationRequestForConsentApplicantSolicitor(FinremCaseDetails caseDetails,
                                                                                  Map<String, Object> hearingData) {
        return buildNotificationRequest(caseDetails, getConsentedCaseDataKeysForApplicantSolicitor(), hearingData);
    }

    public NotificationRequest getNotificationRequestForApplicantSolicitor(CaseDetails caseDetails,
                                                                           Map<String, Object> interimHearingData) {
        return buildNotificationRequest(caseDetails, getContestedCaseDataKeysForApplicantSolicitor(), interimHearingData);
    }

    public NotificationRequest getNotificationRequestForApplicantSolicitor(CaseDetails caseDetails) {
        return caseDataService.isConsentedApplication(caseDetails)
            ? buildNotificationRequest(caseDetails, getConsentedCaseDataKeysForApplicantSolicitor())
            : buildNotificationRequest(caseDetails, getContestedCaseDataKeysForApplicantSolicitor());
    }

    public NotificationRequest getNotificationRequestForNoticeOfChange(CaseDetails caseDetails) {
        return isRespondentSolicitorChangedOnLatestRepresentationUpdate(caseDetails)
            ? getNotificationRequestForRespondentSolicitor(caseDetails)
            : getNotificationRequestForApplicantSolicitor(caseDetails);
    }

    private SolicitorCaseDataKeysWrapper getContestedCaseDataKeysForApplicantSolicitor() {
        return SolicitorCaseDataKeysWrapper.builder()
            .solicitorEmailKey(CONTESTED_SOLICITOR_EMAIL)
            .solicitorNameKey(CONTESTED_SOLICITOR_NAME)
            .solicitorReferenceKey(SOLICITOR_REFERENCE)
            .build();
    }

    private SolicitorCaseDataKeysWrapper getConsentedCaseDataKeysForApplicantSolicitor() {
        return SolicitorCaseDataKeysWrapper.builder()
            .solicitorEmailKey(SOLICITOR_EMAIL)
            .solicitorNameKey(CONSENTED_SOLICITOR_NAME)
            .solicitorReferenceKey(SOLICITOR_REFERENCE)
            .build();
    }

    private SolicitorCaseDataKeysWrapper getCaseDataKeysForRespondentSolicitor() {
        return SolicitorCaseDataKeysWrapper.builder()
            .solicitorEmailKey(RESP_SOLICITOR_EMAIL)
            .solicitorNameKey(RESP_SOLICITOR_NAME)
            .solicitorReferenceKey(RESP_SOLICITOR_REFERENCE)
            .build();
    }

    private boolean isRespondentSolicitorChangedOnLatestRepresentationUpdate(CaseDetails caseDetails) {
        return getLastRepresentationUpdate(caseDetails).getParty().equals(RESPONDENT);
    }

    private RepresentationUpdate getLastRepresentationUpdate(CaseDetails caseDetails) {

        List<Element<RepresentationUpdate>> representationUpdates = objectMapper
            .convertValue(caseDetails.getData().get(REPRESENTATION_UPDATE_HISTORY), new TypeReference<>() {
            });

        return Collections.max(representationUpdates, Comparator.comparing(c -> c.getValue().getDate())).getValue();
    }

    private String getCaseType(CaseDetails caseDetails) {
        String caseType;
        if (caseDataService.isConsentedApplication(caseDetails)) {
            caseType = CONSENTED;
        } else {
            caseType = CONTESTED;
        }
        return caseType;
    }

    private NotificationRequest buildNotificationRequest(CaseDetails caseDetails,
                                                         SolicitorCaseDataKeysWrapper solicitorCaseDataKeysWrapper) {

        NotificationRequest notificationRequest = getNotificationCoreData(caseDetails, solicitorCaseDataKeysWrapper);

        if (caseDataService.isContestedApplication(caseDetails)) {
            String selectedCourt = ContestedCourtHelper.getSelectedFrc(caseDetails);
            notificationRequest.setSelectedCourt(selectedCourt);
            log.info("selectedCourt is {} for case ID: {}", selectedCourt, notificationRequest.getCaseReferenceNumber());
        }
        return notificationRequest;
    }

    @Deprecated
    private NotificationRequest buildNotificationRequest(CaseDetails caseDetails,
                                                         SolicitorCaseDataKeysWrapper solicitorCaseDataKeysWrapper,
                                                         Map<String, Object> interimHearingData) {
        NotificationRequest notificationRequest = getNotificationCoreData(caseDetails, solicitorCaseDataKeysWrapper);

        if (caseDataService.isConsentedApplication(caseDetails)) {
            notificationRequest.setSelectedCourt(ContestedCourtHelper.getSelectedHearingFrc(interimHearingData));
        }

        if (caseDataService.isContestedApplication(caseDetails)) {
            notificationRequest.setSelectedCourt(ContestedCourtHelper.getSelectedInterimHearingFrc(interimHearingData));
        }

        return notificationRequest;
    }

    private NotificationRequest buildNotificationRequest(FinremCaseDetails caseDetails,
                                                         SolicitorCaseDataKeysWrapper solicitorCaseDataKeysWrapper,
                                                         Map<String, Object> interimHearingData) {
        NotificationRequest notificationRequest = getNotificationCoreData(caseDetails, solicitorCaseDataKeysWrapper);

        if (caseDetails.getData().isConsentedApplication()) {
            notificationRequest.setSelectedCourt(ContestedCourtHelper.getSelectedHearingFrc(interimHearingData));
        }

        if (caseDetails.getData().isContestedApplication()) {
            notificationRequest.setSelectedCourt(ContestedCourtHelper.getSelectedInterimHearingFrc(interimHearingData));
        }

        return notificationRequest;
    }

    public NotificationRequest buildNotificationRequest(CaseDetails caseDetails, Barrister barrister) {

        String appName = caseDataService.buildFullName(caseDetails.getData(), APPLICANT_FIRST_MIDDLE_NAME, APPLICANT_LAST_NAME);
        return NotificationRequest.builder()
            .name(barrister.getName())
            .barristerReferenceNumber(barrister.getOrganisation().getOrganisationID())
            .caseReferenceNumber(caseDetails.getId().toString())
            .notificationEmail(barrister.getEmail())
            .applicantName(appName)
            .respondentName(caseDataService.buildFullRespondentName(caseDetails))
            .phoneOpeningHours(CTSC_OPENING_HOURS)
            .build();
    }

    @Deprecated
    private NotificationRequest getNotificationCoreData(CaseDetails caseDetails, SolicitorCaseDataKeysWrapper solicitorCaseDataKeysWrapper) {
        NotificationRequest notificationRequest = new NotificationRequest();
        Map<String, Object> caseData = caseDetails.getData();

        notificationRequest.setCaseReferenceNumber(Objects.toString(caseDetails.getId()));
        notificationRequest.setSolicitorReferenceNumber(Objects.toString(caseData.get(solicitorCaseDataKeysWrapper.getSolicitorReferenceKey()),
            EMPTY_STRING));
        notificationRequest.setDivorceCaseNumber(Objects.toString(caseData.get(DIVORCE_CASE_NUMBER)));
        notificationRequest.setName(Objects.toString(caseData.get(solicitorCaseDataKeysWrapper.getSolicitorNameKey())));
        notificationRequest.setNotificationEmail(Objects.toString(caseData.get(solicitorCaseDataKeysWrapper.getSolicitorEmailKey())));
        notificationRequest.setGeneralEmailBody(Objects.toString(caseData.get(GENERAL_EMAIL_BODY)));
        notificationRequest.setCaseType(getCaseType(caseDetails));
        notificationRequest.setPhoneOpeningHours(CTSC_OPENING_HOURS);
        notificationRequest.setGeneralApplicationRejectionReason(
            Objects.toString(caseDetails.getData().get(GENERAL_APPLICATION_REJECT_REASON), ""));
        String appName = caseDataService.buildFullName(caseDetails.getData(), APPLICANT_FIRST_MIDDLE_NAME, APPLICANT_LAST_NAME);
        notificationRequest.setApplicantName(Objects.toString(appName));
        if (caseDataService.isConsentedApplication(caseDetails)) {
            String respName = caseDataService.buildFullName(caseDetails.getData(),
                CONSENTED_RESPONDENT_FIRST_MIDDLE_NAME, CONSENTED_RESPONDENT_LAST_NAME);
            notificationRequest.setRespondentName(Objects.toString(respName));
            if (Boolean.TRUE.equals(consentedApplicationHelper.isVariationOrder(caseData))) {
                notificationRequest.setCaseOrderType("variation");
                notificationRequest.setCamelCaseOrderType("Variation");
            } else {
                notificationRequest.setCaseOrderType("consent");
                notificationRequest.setCamelCaseOrderType("Consent");
            }
            log.info("caseOrder Type is {} for case ID: {}", notificationRequest.getCaseOrderType(),
                notificationRequest.getCaseReferenceNumber());
        }

        if (caseDataService.isContestedApplication(caseDetails)) {
            String respName = caseDataService.buildFullName(caseDetails.getData(),
                CONTESTED_RESPONDENT_FIRST_MIDDLE_NAME, CONTESTED_RESPONDENT_LAST_NAME);
            notificationRequest.setRespondentName(Objects.toString(respName));
        }
        notificationRequest.setHearingType(Objects.toString(caseData.get(HEARING_TYPE), ""));
        return notificationRequest;
    }

    private NotificationRequest getNotificationCoreData(FinremCaseDetails caseDetails,
                                                        SolicitorCaseDataKeysWrapper solicitorCaseDataKeysWrapper) {
        NotificationRequest notificationRequest = new NotificationRequest();
        Map<String, Object> notificationRequestPayload = null;
        FinremCaseData data = caseDetails.getData();
        try {
            notificationRequestPayload =
                objectMapper.readValue(objectMapper.writeValueAsString(data), HashMap.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        notificationRequest.setCaseReferenceNumber(Objects.toString(caseDetails.getId()));
        notificationRequest.setSolicitorReferenceNumber(
            Objects.toString(notificationRequestPayload.get(solicitorCaseDataKeysWrapper.getSolicitorReferenceKey()),
            EMPTY_STRING));
        notificationRequest.setDivorceCaseNumber(
            Objects.toString(notificationRequestPayload.get(DIVORCE_CASE_NUMBER)));
        notificationRequest.setName(
            Objects.toString(notificationRequestPayload.get(solicitorCaseDataKeysWrapper.getSolicitorNameKey())));
        notificationRequest.setNotificationEmail(
            Objects.toString(notificationRequestPayload.get(solicitorCaseDataKeysWrapper.getSolicitorEmailKey())));
        notificationRequest.setGeneralEmailBody(
            Objects.toString(notificationRequestPayload.get(GENERAL_EMAIL_BODY)));
        notificationRequest.setCaseType(caseDetails.getCaseType().toString().toLowerCase());
        notificationRequest.setPhoneOpeningHours(CTSC_OPENING_HOURS);
        notificationRequest.setGeneralApplicationRejectionReason(
            Objects.toString(data.getGeneralApplicationWrapper().getGeneralApplicationRejectReason(),
                ""));
        notificationRequest.setApplicantName(data.getFullApplicantName());
        if (data.isConsentedApplication()) {
            notificationRequest.setRespondentName(data.getFullRespondentNameConsented());
            if (Boolean.TRUE.equals(consentedApplicationHelper.isVariationOrder(notificationRequestPayload))) {
                notificationRequest.setCaseOrderType("variation");
                notificationRequest.setCamelCaseOrderType("Variation");
            } else {
                notificationRequest.setCaseOrderType("consent");
                notificationRequest.setCamelCaseOrderType("Consent");
            }
            log.info("caseOrder Type is {} for case ID: {}", notificationRequest.getCaseOrderType(),
                notificationRequest.getCaseReferenceNumber());
        }

        if (data.isContestedApplication()) {
            notificationRequest.setRespondentName(data.getFullRespondentNameContested());
        }
        notificationRequest.setHearingType(Objects.toString(notificationRequestPayload.get(HEARING_TYPE), ""));
        return notificationRequest;
    }
}
