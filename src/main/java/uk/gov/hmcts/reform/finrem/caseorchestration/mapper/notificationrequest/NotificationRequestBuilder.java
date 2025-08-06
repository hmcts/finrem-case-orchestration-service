package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.notificationrequest;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.EmailService;

@Component
@Scope(value = "prototype")
public class NotificationRequestBuilder {
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

    /**
     * Sets default values for the NotificationRequestBuilder based on the provided case details.
     * This method initializes the builder with values derived from the case details, such as case reference.
     * @param caseDetails the case details
     * @return the NotificationRequestBuilder instance with default values set
     */
    public NotificationRequestBuilder withDefaults(FinremCaseDetails caseDetails) {
        caseReferenceNumber = String.valueOf(caseDetails.getId());
        applicantName = caseDetails.getData().getFullApplicantName();
        respondentName = caseDetails.getData().getRespondentFullName();
        caseOrderType = caseDetails.getCaseType().getCcdType();
        caseType = CaseType.CONTESTED.equals(caseDetails.getCaseType()) ? EmailService.CONTESTED : EmailService.CONSENTED;

        return this;
    }

    /**
     * Builds a NotificationRequest object using the values set in the builder.
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
}
