package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.notificationrequest;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CourtDetailsConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ConsentedApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.CourtHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.SolicitorCaseDataKeysWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.EmailService;

import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_OPENING_HOURS;

@Component
@Scope(value = "prototype")
@RequiredArgsConstructor
public class NotificationRequestBuilder {

    private final CourtDetailsConfiguration courtDetailsConfiguration;
    private final ConsentedApplicationHelper consentedApplicationHelper;

    private String caseReferenceNumber;
    private String solicitorReferenceNumber;
    private String divorceCaseNumber;
    private String name;
    private String notificationEmail;
    private String selectedCourt;
    private String caseType;
    private String generalEmailBody;
    private String phoneOpeningHours;
    private String caseOrderType;
    private String camelCaseOrderType;
    private String generalApplicationRejectionReason;
    private String applicantName;
    private String respondentName;
    private String barristerReferenceNumber;
    private String hearingType;
    private String intervenerSolicitorReferenceNumber;
    private String intervenerFullName;
    private String intervenerSolicitorFirm;
    private byte[] documentContents;
    private Boolean isNotDigital;
    private String hearingDate;
    private String judgeName;
    private String oldestDraftOrderDate;
    private String judgeFeedback;
    private String documentName;
    private String contactCourtName;
    private String contactCourtEmail;
    private String emailReplyToId;

    /**
     * Sets default values for the NotificationRequestBuilder based on the provided case details.
     * This method sets the following fields:
     * <ul>
     * <li>applicantName</li>
     * <li>camelCaseOrderType</li>
     * <li>caseOrderType</li>
     * <li>caseReferenceNumber</li>
     * <li>caseType</li>
     * <li>contactCourtName</li>
     * <li>contactCourtEmail</li>
     * <li>divorceCaseNumber</li>
     * <li>emailReplyToId</li>
     * <li>phoneOpeningHours</li>
     * <li>respondentName</li>
     * <li>selectedCourt</li>
     * </ul>
     *
     * @param caseDetails the case details
     * @return the NotificationRequestBuilder instance with default values set
     */
    public NotificationRequestBuilder withCaseDefaults(FinremCaseDetails caseDetails) {
        FinremCaseData caseData = caseDetails.getData();

        caseReferenceNumber = String.valueOf(caseDetails.getId());
        applicantName = caseData.getFullApplicantName();
        caseType = CaseType.CONTESTED.equals(caseDetails.getCaseType()) ? EmailService.CONTESTED : EmailService.CONSENTED;
        divorceCaseNumber = caseData.getDivorceCaseNumber();
        phoneOpeningHours = CTSC_OPENING_HOURS;
        addSelectedCourtDetails(caseData);

        if (caseData.isConsentedApplication()) {
            setConsentedDefaults(caseData);
        }

        if (caseData.isContestedApplication()) {
            setContestedDefaults(caseDetails);
        }

        return this;
    }

    private void addSelectedCourtDetails(FinremCaseData caseData) {
        Optional.ofNullable(caseData.getSelectedAllocatedCourt())
            .map(courtDetailsConfiguration.getCourts()::get)
            .ifPresent(court -> {
                contactCourtName = court.getCourtName();
                contactCourtEmail = court.getEmail();
                emailReplyToId = court.getEmailReplyToId();
            });
    }

    private void setContestedDefaults(FinremCaseDetails caseDetails) {
        respondentName = caseDetails.getData().getRespondentFullName();
        selectedCourt = CourtHelper.getSelectedFrc(caseDetails);
    }

    private void setConsentedDefaults(FinremCaseData caseData) {
        respondentName = caseData.getFullRespondentNameConsented();

        if (consentedApplicationHelper.isVariationOrder(caseData)) {
            caseOrderType = "variation";
        } else {
            caseOrderType = "consent";
        }
        camelCaseOrderType = StringUtils.capitalize(caseOrderType);
    }

    /**
     * Sets the notification email destination to the court's email based on the selected allocated court in the case details.
     *
     * @param caseDetails the case details
     * @return the NotificationRequestBuilder instance
     */
    public NotificationRequestBuilder withCourtAsEmailDestination(FinremCaseDetails caseDetails) {
        String selectedAllocatedCourt = caseDetails.getData().getSelectedAllocatedCourt();
        notificationEmail = courtDetailsConfiguration.getCourts().get(selectedAllocatedCourt).getEmail();

        return this;
    }

    /**
     * Sets solicitor-related fields in the NotificationRequestBuilder using the provided solicitor case data.
     * This method sets the following fields:
     * <ul>
     * <li>isNotDigital</li>
     * <li>name</li>
     * <li>notificationEmail</li>
     * <li>solicitorReferenceNumber</li>
     * </ul>
     *
     * @param solicitorCaseData the solicitor case data wrapper
     * @return the NotificationRequestBuilder instance with solicitor data set
     */
    public NotificationRequestBuilder withSolicitorCaseData(SolicitorCaseDataKeysWrapper solicitorCaseData) {
        solicitorReferenceNumber = Objects.toString(solicitorCaseData.getSolicitorReferenceKey(), "");
        name = Objects.toString(solicitorCaseData.getSolicitorNameKey(), "");
        notificationEmail = Objects.toString(solicitorCaseData.getSolicitorEmailKey(), "");
        isNotDigital = solicitorCaseData.getSolicitorIsNotDigitalKey();

        return this;
    }

    /**
     * Builds a NotificationRequest object using the values set in the builder.
     *
     * @return a NotificationRequest
     */
    public NotificationRequest build() {
        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setCaseReferenceNumber(caseReferenceNumber);
        notificationRequest.setSolicitorReferenceNumber(solicitorReferenceNumber);
        notificationRequest.setDivorceCaseNumber(divorceCaseNumber);
        notificationRequest.setName(name);
        notificationRequest.setNotificationEmail(notificationEmail);
        notificationRequest.setSelectedCourt(selectedCourt);
        notificationRequest.setCaseType(caseType);
        notificationRequest.setGeneralEmailBody(generalEmailBody);
        notificationRequest.setPhoneOpeningHours(phoneOpeningHours);
        notificationRequest.setCaseOrderType(caseOrderType);
        notificationRequest.setCamelCaseOrderType(camelCaseOrderType);
        notificationRequest.setGeneralApplicationRejectionReason(generalApplicationRejectionReason);
        notificationRequest.setApplicantName(applicantName);
        notificationRequest.setRespondentName(respondentName);
        notificationRequest.setBarristerReferenceNumber(barristerReferenceNumber);
        notificationRequest.setHearingType(hearingType);
        notificationRequest.setIntervenerSolicitorReferenceNumber(intervenerSolicitorReferenceNumber);
        notificationRequest.setIntervenerFullName(intervenerFullName);
        notificationRequest.setIntervenerSolicitorFirm(intervenerSolicitorFirm);
        notificationRequest.setDocumentContents(documentContents);
        notificationRequest.setIsNotDigital(isNotDigital);
        notificationRequest.setHearingDate(hearingDate);
        notificationRequest.setJudgeName(judgeName);
        notificationRequest.setOldestDraftOrderDate(oldestDraftOrderDate);
        notificationRequest.setJudgeFeedback(judgeFeedback);
        notificationRequest.setDocumentName(documentName);
        notificationRequest.setContactCourtName(contactCourtName);
        notificationRequest.setContactCourtEmail(contactCourtEmail);
        notificationRequest.setEmailReplyToId(emailReplyToId);

        return notificationRequest;
    }

    public NotificationRequestBuilder caseReferenceNumber(String caseReferenceNumber) {
        this.caseReferenceNumber = caseReferenceNumber;
        return this;
    }

    public NotificationRequestBuilder solicitorReferenceNumber(String solicitorReferenceNumber) {
        this.solicitorReferenceNumber = solicitorReferenceNumber;
        return this;
    }

    public NotificationRequestBuilder divorceCaseNumber(String divorceCaseNumber) {
        this.divorceCaseNumber = divorceCaseNumber;
        return this;
    }

    public NotificationRequestBuilder name(String name) {
        this.name = name;
        return this;
    }

    public NotificationRequestBuilder notificationEmail(String notificationEmail) {
        this.notificationEmail = notificationEmail;
        return this;
    }

    public NotificationRequestBuilder selectedCourt(String selectedCourt) {
        this.selectedCourt = selectedCourt;
        return this;
    }

    public NotificationRequestBuilder caseType(String caseType) {
        this.caseType = caseType;
        return this;
    }

    public NotificationRequestBuilder generalEmailBody(String generalEmailBody) {
        this.generalEmailBody = generalEmailBody;
        return this;
    }

    public NotificationRequestBuilder phoneOpeningHours(String phoneOpeningHours) {
        this.phoneOpeningHours = phoneOpeningHours;
        return this;
    }

    public NotificationRequestBuilder caseOrderType(String caseOrderType) {
        this.caseOrderType = caseOrderType;
        return this;
    }

    public NotificationRequestBuilder camelCaseOrderType(String camelCaseOrderType) {
        this.camelCaseOrderType = camelCaseOrderType;
        return this;
    }

    public NotificationRequestBuilder generalApplicationRejectionReason(String generalApplicationRejectionReason) {
        this.generalApplicationRejectionReason = generalApplicationRejectionReason;
        return this;
    }

    public NotificationRequestBuilder applicantName(String applicantName) {
        this.applicantName = applicantName;
        return this;
    }

    public NotificationRequestBuilder respondentName(String respondentName) {
        this.respondentName = respondentName;
        return this;
    }

    public NotificationRequestBuilder barristerReferenceNumber(String barristerReferenceNumber) {
        this.barristerReferenceNumber = barristerReferenceNumber;
        return this;
    }

    public NotificationRequestBuilder hearingType(String hearingType) {
        this.hearingType = hearingType;
        return this;
    }

    public NotificationRequestBuilder intervenerSolicitorReferenceNumber(String intervenerSolicitorReferenceNumber) {
        this.intervenerSolicitorReferenceNumber = intervenerSolicitorReferenceNumber;
        return this;
    }

    public NotificationRequestBuilder intervenerFullName(String intervenerFullName) {
        this.intervenerFullName = intervenerFullName;
        return this;
    }

    public NotificationRequestBuilder intervenerSolicitorFirm(String intervenerSolicitorFirm) {
        this.intervenerSolicitorFirm = intervenerSolicitorFirm;
        return this;
    }

    public NotificationRequestBuilder documentContents(byte[] documentContents) {
        this.documentContents = documentContents;
        return this;
    }

    public NotificationRequestBuilder isNotDigital(Boolean isNotDigital) {
        this.isNotDigital = isNotDigital;
        return this;
    }

    public NotificationRequestBuilder hearingDate(String hearingDate) {
        this.hearingDate = hearingDate;
        return this;
    }

    public NotificationRequestBuilder judgeName(String judgeName) {
        this.judgeName = judgeName;
        return this;
    }

    public NotificationRequestBuilder oldestDraftOrderDate(String oldestDraftOrderDate) {
        this.oldestDraftOrderDate = oldestDraftOrderDate;
        return this;
    }

    public NotificationRequestBuilder judgeFeedback(String judgeFeedback) {
        this.judgeFeedback = judgeFeedback;
        return this;
    }

    public NotificationRequestBuilder documentName(String documentName) {
        this.documentName = documentName;
        return this;
    }

    public NotificationRequestBuilder contactCourtName(String contactCourtName) {
        this.contactCourtName = contactCourtName;
        return this;
    }

    public NotificationRequestBuilder contactCourtEmail(String contactCourtEmail) {
        this.contactCourtEmail = contactCourtEmail;
        return this;
    }

    public NotificationRequestBuilder emailReplyToId(String emailReplyToId) {
        this.emailReplyToId = emailReplyToId;
        return this;
    }
}
