package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.notificationrequest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ConsentedApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.CourtHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdate;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdateHistoryCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ListForHearingWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.SolicitorCaseDataKeysWrapper;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_OPENING_HOURS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService.nullToEmpty;

@Service
@Slf4j
public class FinremNotificationRequestMapper extends AbstractNotificationRequestMapper {

    private static final String RESPONDENT = "Respondent";
    private static final String CONSENTED = "consented";
    private static final String CONTESTED = "contested";
    private static final String EMPTY_STRING = "";

    private final ConsentedApplicationHelper consentedApplicationHelper;

    public FinremNotificationRequestMapper(NotificationRequestBuilderFactory notificationRequestBuilderFactory,
                                           ConsentedApplicationHelper consentedApplicationHelper) {
        super(notificationRequestBuilderFactory);
        this.consentedApplicationHelper = consentedApplicationHelper;
    }

    public NotificationRequest getNotificationRequestForRespondentSolicitor(FinremCaseDetails caseDetails) {
        return buildNotificationRequest(caseDetails, getRespondentSolicitorCaseData(caseDetails.getData()));
    }

    public NotificationRequest getNotificationRequestForRespondentSolicitor(FinremCaseDetails caseDetails, boolean isNotDigital) {
        return buildNotificationRequest(caseDetails, getRespondentSolicitorCaseData(caseDetails.getData(), isNotDigital));
    }

    public NotificationRequest getNotificationRequestForApplicantSolicitor(FinremCaseDetails caseDetails) {
        return buildNotificationRequest(caseDetails, getApplicantSolicitorCaseData(caseDetails.getData()));
    }

    public NotificationRequest getNotificationRequestForApplicantSolicitor(FinremCaseDetails caseDetails, boolean isNotDigital) {
        return buildNotificationRequest(caseDetails, getApplicantSolicitorCaseData(caseDetails.getData(), isNotDigital));
    }

    public NotificationRequest getNotificationRequestForIntervenerSolicitor(FinremCaseDetails caseDetails,
                                                                            SolicitorCaseDataKeysWrapper caseDataKeysWrapper) {
        return buildNotificationRequest(caseDetails, caseDataKeysWrapper);
    }

    public NotificationRequest getNotificationRequestForNoticeOfChange(FinremCaseDetails caseDetails) {
        return isRespondentSolicitorChangedOnLatestRepresentationUpdate(caseDetails)
            ? getNotificationRequestForRespondentSolicitor(caseDetails)
            : getNotificationRequestForApplicantSolicitor(caseDetails);
    }

    private SolicitorCaseDataKeysWrapper getApplicantSolicitorCaseData(FinremCaseData caseData) {
        return SolicitorCaseDataKeysWrapper.builder()
            .solicitorEmailKey(caseData.getAppSolicitorEmail())
            .solicitorNameKey(nullToEmpty(caseData.getAppSolicitorName()))
            .solicitorReferenceKey(nullToEmpty(caseData.getContactDetailsWrapper().getSolicitorReference()))
            .build();
    }

    private SolicitorCaseDataKeysWrapper getApplicantSolicitorCaseData(FinremCaseData caseData, boolean isNotDigital) {
        return SolicitorCaseDataKeysWrapper.builder()
            .solicitorEmailKey(caseData.getAppSolicitorEmail())
            .solicitorNameKey(nullToEmpty(caseData.getAppSolicitorName()))
            .solicitorReferenceKey(nullToEmpty(caseData.getContactDetailsWrapper().getSolicitorReference()))
            .solicitorIsNotDigitalKey(isNotDigital)
            .build();
    }

    private SolicitorCaseDataKeysWrapper getRespondentSolicitorCaseData(FinremCaseData caseData) {
        return SolicitorCaseDataKeysWrapper.builder()
            .solicitorEmailKey(caseData.getContactDetailsWrapper().getRespondentSolicitorEmail())
            .solicitorNameKey(nullToEmpty(caseData.getRespondentSolicitorName()))
            .solicitorReferenceKey(nullToEmpty(caseData.getContactDetailsWrapper().getRespondentSolicitorReference()))
            .build();
    }

    private SolicitorCaseDataKeysWrapper getRespondentSolicitorCaseData(FinremCaseData caseData, boolean isNotDigital) {
        return SolicitorCaseDataKeysWrapper.builder()
            .solicitorEmailKey(caseData.getContactDetailsWrapper().getRespondentSolicitorEmail())
            .solicitorNameKey(nullToEmpty(caseData.getRespondentSolicitorName()))
            .solicitorReferenceKey(nullToEmpty(caseData.getContactDetailsWrapper().getRespondentSolicitorReference()))
            .solicitorIsNotDigitalKey(isNotDigital)
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
                                                         SolicitorCaseDataKeysWrapper caseDataKeysWrapper) {
        NotificationRequest notificationRequest = new NotificationRequest();
        FinremCaseData caseData = caseDetails.getData();
        notificationRequest.setCaseReferenceNumber(String.valueOf(caseDetails.getId()));
        notificationRequest.setSolicitorReferenceNumber(Objects.toString(caseDataKeysWrapper.getSolicitorReferenceKey(), EMPTY_STRING));
        notificationRequest.setDivorceCaseNumber(Objects.toString(caseData.getDivorceCaseNumber(), EMPTY_STRING));
        notificationRequest.setName(caseDataKeysWrapper.getSolicitorNameKey());
        notificationRequest.setNotificationEmail(caseDataKeysWrapper.getSolicitorEmailKey());
        notificationRequest.setCaseType(getCaseType(caseDetails));
        notificationRequest.setPhoneOpeningHours(CTSC_OPENING_HOURS);
        notificationRequest.setGeneralApplicationRejectionReason(
            Objects.toString(caseData.getGeneralApplicationWrapper().getGeneralApplicationRejectReason(), EMPTY_STRING));
        notificationRequest.setGeneralEmailBody(Objects.toString(caseData.getGeneralEmailWrapper().getGeneralEmailBody(), EMPTY_STRING));
        notificationRequest.setApplicantName(Objects.toString(caseData.getFullApplicantName()));
        if (caseData.isConsentedApplication()) {
            notificationRequest.setRespondentName(Objects.toString(caseData.getFullRespondentNameConsented()));
            setCaseOrderType(notificationRequest, caseData);
            log.info("caseOrder Type is {} for case ID: {}", notificationRequest.getCaseOrderType(),
                notificationRequest.getCaseReferenceNumber());
        }
        if (caseData.isContestedApplication()) {
            notificationRequest.setRespondentName(Objects.toString(caseData.getFullRespondentNameContested()));
            notificationRequest.setSelectedCourt(CourtHelper.getSelectedFrc(caseDetails));
            log.info("selectedCourt is {} for case ID: {}", notificationRequest.getSelectedCourt(),
                notificationRequest.getCaseReferenceNumber());
        }
        ListForHearingWrapper listForHearingWrapper = caseData.getListForHearingWrapper();
        notificationRequest.setHearingType(listForHearingWrapper.getHearingType() != null
            ? listForHearingWrapper.getHearingType().getId()
            : "");
        notificationRequest.setIsNotDigital(caseDataKeysWrapper.getSolicitorIsNotDigitalKey());

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

    /**
     * Builds a {@link NotificationRequest} for an intervener in a financial remedy case.
     *
     * <p>This method prepares the notification details such as the intervener's full name,
     * solicitor firm, solicitor reference number, recipient name and email, and party names
     * from the case. If the intervener organisation name is present, it is used as the
     * solicitor firm name; otherwise, it falls back to the solicitor firm string directly.</p>
     *
     * @param caseDetails      the details of the financial remedy case, including applicant and respondent names
     * @param intervenerDetails the details of the intervener, including organisation and solicitor information
     * @param recipientName     the name of the recipient for the notification (can be null or empty)
     * @param recipientEmail    the email address of the notification recipient
     * @param referenceNumber   the solicitor's reference number (can be null or empty)
     * @return a populated {@link NotificationRequest} containing the relevant notification fields
     */
    public NotificationRequest buildNotificationRequest(FinremCaseDetails caseDetails, IntervenerDetails intervenerDetails,
                                                        String recipientName, String recipientEmail, String referenceNumber) {
        String intvSolicitorFirm = intervenerDetails.getIntervenerSolicitorFirm();
        String organisationName = Optional.of(intervenerDetails)
            .map(IntervenerDetails::getIntervenerOrganisation)
            .map(OrganisationPolicy::getOrganisation)
            .map(Organisation::getOrganisationName)
            .orElse(null);

        return NotificationRequest.builder()
            .caseReferenceNumber(caseDetails.getId().toString())
            .intervenerFullName(intervenerDetails.getIntervenerName())
            .intervenerSolicitorFirm(organisationName == null ? intvSolicitorFirm : organisationName)
            .intervenerSolicitorReferenceNumber(nullToEmpty(referenceNumber))
            .name(nullToEmpty(recipientName))
            .notificationEmail(recipientEmail)
            .applicantName(caseDetails.getData().getFullApplicantName())
            .respondentName(caseDetails.getData().getRespondentFullName())
            .phoneOpeningHours(CTSC_OPENING_HOURS)
            .build();
    }

    /**
     * Builds a {@link NotificationRequest} for sending a general email notification.
     * @param caseDetails the case details
     * @return the populated notification request
     */
    public NotificationRequest getNotificationRequestForGeneralEmail(FinremCaseDetails caseDetails) {
        FinremCaseData caseData = caseDetails.getData();
        SolicitorCaseDataKeysWrapper solicitorCaseData = getApplicantSolicitorCaseData(caseData);

        return notificationRequestBuilder()
            .withCaseDefaults(caseDetails)
            .withSolicitorCaseData(solicitorCaseData)
            .notificationEmail(caseData.getGeneralEmailWrapper().getGeneralEmailRecipient())
            .generalEmailBody(caseData.getGeneralEmailWrapper().getGeneralEmailBody())
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
