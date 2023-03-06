package uk.gov.hmcts.reform.finrem.caseorchestration.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ConsentedApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ContestedCourtHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdate;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdateHistoryCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.SolicitorCaseDataKeysWrapper;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_OPENING_HOURS;

@Service
@Slf4j
@RequiredArgsConstructor
public class FinremNotificationRequestMapper {

    private static final String RESPONDENT = "Respondent";
    private static final String CONSENTED = "consented";
    private static final String CONTESTED = "contested";
    private final ConsentedApplicationHelper consentedApplicationHelper;
    protected static final String EMPTY_STRING = "";

    public NotificationRequest getNotificationRequestForRespondentSolicitor(FinremCaseDetails caseDetails) {
        return buildNotificationRequest(caseDetails, getRespondentSolicitorCaseData(caseDetails.getData()));
    }

    public NotificationRequest getNotificationRequestForApplicantSolicitor(FinremCaseDetails caseDetails) {
        return buildNotificationRequest(caseDetails, getApplicantSolicitorCaseData(caseDetails.getData()));
    }

    public NotificationRequest getNotificationRequestForNoticeOfChange(FinremCaseDetails caseDetails) {
        return isRespondentSolicitorChangedOnLatestRepresentationUpdate(caseDetails)
            ? getNotificationRequestForRespondentSolicitor(caseDetails)
            : getNotificationRequestForApplicantSolicitor(caseDetails);
    }

    private SolicitorCaseDataKeysWrapper getApplicantSolicitorCaseData(FinremCaseData caseData) {
        return SolicitorCaseDataKeysWrapper.builder()
            .solicitorEmailKey(caseData.getAppSolicitorEmail())
            .solicitorNameKey(caseData.getAppSolicitorName())
            .solicitorReferenceKey(caseData.getContactDetailsWrapper().getSolicitorReference())
            .build();
    }

    private SolicitorCaseDataKeysWrapper getRespondentSolicitorCaseData(FinremCaseData caseData) {
        return SolicitorCaseDataKeysWrapper.builder()
            .solicitorEmailKey(caseData.getContactDetailsWrapper().getRespondentSolicitorEmail())
            .solicitorNameKey(caseData.getRespondentSolicitorName())
            .solicitorReferenceKey(caseData.getContactDetailsWrapper().getRespondentSolicitorReference())
            .build();
    }

    private boolean isRespondentSolicitorChangedOnLatestRepresentationUpdate(FinremCaseDetails caseDetails) {
        return getLastRepresentationUpdate(caseDetails).getParty().equalsIgnoreCase(RESPONDENT);
    }

    private RepresentationUpdate getLastRepresentationUpdate(FinremCaseDetails caseDetails) {
        List<RepresentationUpdateHistoryCollection> representationUpdates =
            caseDetails.getData().getRepresentationUpdateHistory();

        return Collections.max(representationUpdates, Comparator.comparing(c -> c.getValue().getDate())).getValue();
    }

    private NotificationRequest buildNotificationRequest(FinremCaseDetails caseDetails,
                                                         SolicitorCaseDataKeysWrapper solicitorCaseDataKeysWrapper) {
        NotificationRequest notificationRequest = new NotificationRequest();
        FinremCaseData caseData = caseDetails.getData();
        notificationRequest.setCaseReferenceNumber(String.valueOf(caseDetails.getId()));
        notificationRequest.setSolicitorReferenceNumber(Objects.toString(solicitorCaseDataKeysWrapper.getSolicitorReferenceKey(), EMPTY_STRING));
        notificationRequest.setDivorceCaseNumber(Objects.toString(caseData.getDivorceCaseNumber(), EMPTY_STRING));
        notificationRequest.setName(solicitorCaseDataKeysWrapper.getSolicitorNameKey());
        notificationRequest.setNotificationEmail(solicitorCaseDataKeysWrapper.getSolicitorEmailKey());
        notificationRequest.setCaseType(getCaseType(caseDetails));
        notificationRequest.setGeneralEmailBody(Objects.toString(caseData.getGeneralEmailBody(), EMPTY_STRING));
        notificationRequest.setApplicantName(Objects.toString(caseData.getFullApplicantName()));
        if (caseData.isConsentedApplication()) {
            notificationRequest.setRespondentName(Objects.toString(caseData.getFullRespondentNameConsented()));
            setCaseOrderType(notificationRequest, caseData);
            log.info("caseOrder Type is {} for case ID: {}", notificationRequest.getCaseOrderType(),
                notificationRequest.getCaseReferenceNumber());
        }
        if (caseData.isContestedApplication()) {
            notificationRequest.setRespondentName(Objects.toString(caseData.getFullRespondentNameContested()));
            notificationRequest.setSelectedCourt(ContestedCourtHelper.getSelectedFrc(caseDetails));
            log.info("selectedCourt is {} for case ID: {}", notificationRequest.getSelectedCourt(),
                notificationRequest.getCaseReferenceNumber());
        }
        notificationRequest.setHearingType(caseData.getHearingType() != null ? caseData.getHearingType().getId() : "");

        return notificationRequest;
    }

    public NotificationRequest buildNotificationRequest(FinremCaseDetails caseDetails, Barrister barrister) {
        return NotificationRequest.builder()
            .name(barrister.getName())
            .barristerReferenceNumber(barrister.getOrganisation().getOrganisationID())
            .caseReferenceNumber(caseDetails.getId().toString())
            .notificationEmail(barrister.getEmail())
            .applicantName(caseDetails.getData().getFullApplicantName())
            .respondentName(caseDetails.getData().getRespondentFullName())
            .phoneOpeningHours(CTSC_OPENING_HOURS)
            .build();
    }

    private void setCaseOrderType(NotificationRequest notificationRequest, FinremCaseData caseData) {
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

    private String getCaseType(FinremCaseDetails caseDetails) {
        return caseDetails.getCaseType().equals(CaseType.CONSENTED) ? CONSENTED : CONTESTED;
    }

}
