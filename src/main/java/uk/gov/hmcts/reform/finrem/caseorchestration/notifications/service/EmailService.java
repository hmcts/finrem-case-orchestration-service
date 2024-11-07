package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.client.EmailClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailToSend;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;


@Service
@Slf4j
@RequiredArgsConstructor
@SuppressWarnings("java:S6857")
public class EmailService {

    private final EmailClient emailClient;

    @Value("#{${uk.gov.notify.email.templates}}")
    private Map<String, String> emailTemplates;

    @Value("#{${uk.gov.notify.email.template.vars}}")
    private Map<String, Map<String, String>> emailTemplateVars;

    @Value("#{${uk.gov.notify.email.contestedContactEmails}}")
    private Map<String, Map<String, String>> contestedContactEmails;

    private static final String CONTESTED = "contested";
    private static final String CONSENTED = "consented";
    private static final String FR_ASSIGNED_TO_JUDGE = "FR_ASSIGNED_TO_JUDGE";
    private static final String CONTESTED_GENERAL_EMAIL = "FR_CONTESTED_GENERAL_EMAIL";
    private static final String CONTESTED_GENERAL_EMAIL_ATTACHMENT = "FR_CONTESTED_GENERAL_EMAIL_ATTACHMENT";
    private static final String CONSENT_GENERAL_EMAIL = "FR_CONSENT_GENERAL_EMAIL";
    private static final String CONSENT_GENERAL_EMAIL_ATTACHMENT = "FR_CONSENT_GENERAL_EMAIL_ATTACHMENT";
    private static final String TRANSFER_TO_LOCAL_COURT = "FR_TRANSFER_TO_LOCAL_COURT";
    private static final String GENERAL_APPLICATION_REFER_TO_JUDGE = "FR_CONTESTED_GENERAL_APPLICATION_REFER_TO_JUDGE";
    public static final String FR_CONSENT_ORDER_AVAILABLE_CTSC = "FR_CONSENT_ORDER_AVAILABLE_CTSC";
    public static final String GENERAL_APPLICATION_REJECTED = "FR_REJECT_GENERAL_APPLICATION";
    public static final String BARRISTER_ACCESS_ADDED = "FR_BARRISTER_ACCESS_ADDED";
    public static final String BARRISTER_ACCESS_REMOVED = "FR_BARRISTER_ACCESS_REMOVED";
    public static final String CONSENTED_LIST_FOR_HEARING = "FR_CONSENTED_LIST_FOR_HEARING";
    public static final String INTERVENER_ADDED_EMAIL = "FR_INTERVENER_ADDED_EMAIL";
    public static final String INTERVENER_SOLICITOR_ADDED_EMAIL = "FR_INTERVENER_SOLICITOR_ADDED_EMAIL";
    public static final String INTERVENER_REMOVED_EMAIL = "FR_INTERVENER_REMOVED_EMAIL";
    public static final String INTERVENER_SOLICITOR_REMOVED_EMAIL = "FR_INTERVENER_SOLICITOR_REMOVED_EMAIL";
    private static final String PHONE_OPENING_HOURS = "phoneOpeningHours";

    public void sendConfirmationEmail(NotificationRequest notificationRequest, EmailTemplateNames template) {
        Map<String, Object> templateVars = buildTemplateVars(notificationRequest, template.name());
        EmailToSend emailToSend = generateEmail(notificationRequest.getNotificationEmail(), template.name(),
            templateVars);
        log.info("Sending confirmation email on Case ID : {} using template: {}", notificationRequest.getCaseReferenceNumber(), template.name());
        sendEmail(emailToSend, "send Confirmation email for " + template.name());
    }

    protected Map<String, Object> buildTemplateVars(NotificationRequest notificationRequest, String templateName) {
        Map<String, Object> templateVars = new HashMap<>();

        templateVars.put("caseReferenceNumber", notificationRequest.getCaseReferenceNumber());
        templateVars.put("solicitorReferenceNumber", notificationRequest.getSolicitorReferenceNumber());
        templateVars.put("divorceCaseNumber", notificationRequest.getDivorceCaseNumber());
        templateVars.put("notificationEmail", notificationRequest.getNotificationEmail());
        templateVars.put("name", notificationRequest.getName());
        templateVars.put("applicantName", notificationRequest.getApplicantName());
        templateVars.put("respondentName", notificationRequest.getRespondentName());
        templateVars.put("hearingType", notificationRequest.getHearingType());

        //contested emails notifications require the court information, consented does not
        if ((CONTESTED.equals(notificationRequest.getCaseType())
            || CONSENTED_LIST_FOR_HEARING.equals(templateName)) && !isEmpty(notificationRequest.getSelectedCourt())) {
            Map<String, String> courtDetails = contestedContactEmails.get(notificationRequest.getSelectedCourt());

            templateVars.put("courtName", courtDetails.get("name"));
            templateVars.put("courtEmail", courtDetails.get("email"));
        }

        if (FR_ASSIGNED_TO_JUDGE.equals(templateName)) {
            templateVars.put("isNotDigital", notificationRequest.getIsNotDigital());
        }

        //general emails and transfer to local court emails are the only templates that require the generalEmailBody
        if (CONSENT_GENERAL_EMAIL.equals(templateName)
            || CONTESTED_GENERAL_EMAIL.equals(templateName)
            || CONSENT_GENERAL_EMAIL_ATTACHMENT.equals(templateName)
            || CONTESTED_GENERAL_EMAIL_ATTACHMENT.equals(templateName)
            || TRANSFER_TO_LOCAL_COURT.equals(templateName)
            || GENERAL_APPLICATION_REFER_TO_JUDGE.equals(templateName)) {
            templateVars.put("generalEmailBody", notificationRequest.getGeneralEmailBody());
        }
        if (CONSENT_GENERAL_EMAIL_ATTACHMENT.equals(templateName)
            || CONTESTED_GENERAL_EMAIL_ATTACHMENT.equals(templateName)) {
            templateVars.put("link_to_file", preparedForEmailAttachment(notificationRequest.getDocumentContents()));
        }

        if (CONSENTED.equals(notificationRequest.getCaseType()) && !FR_CONSENT_ORDER_AVAILABLE_CTSC.equals(templateName)) {
            templateVars.put(PHONE_OPENING_HOURS, notificationRequest.getPhoneOpeningHours());
        }

        if (CONSENTED.equals(notificationRequest.getCaseType())) {
            templateVars.put("caseOrderType", notificationRequest.getCaseOrderType());
            templateVars.put("camelCaseOrderType", notificationRequest.getCamelCaseOrderType());
        }

        if (GENERAL_APPLICATION_REJECTED.equals(templateName)) {
            templateVars.put("generalApplicationRejectionReason", notificationRequest.getGeneralApplicationRejectionReason());
        }

        if (BARRISTER_ACCESS_ADDED.equals(templateName) || BARRISTER_ACCESS_REMOVED.equals(templateName)) {
            templateVars.put("BarristerReferenceNumber", notificationRequest.getBarristerReferenceNumber());
            templateVars.put(PHONE_OPENING_HOURS, notificationRequest.getPhoneOpeningHours());
        }
        if (INTERVENER_ADDED_EMAIL.equals(templateName) || INTERVENER_REMOVED_EMAIL.equals(templateName)) {
            templateVars.put("intervenerFullName", notificationRequest.getIntervenerFullName());
            templateVars.put("intervenerSolicitorReferenceNumber", notificationRequest.getIntervenerSolicitorReferenceNumber());
            templateVars.put(PHONE_OPENING_HOURS, notificationRequest.getPhoneOpeningHours());
        }
        if (EmailTemplateNames.FR_CONTESTED_DRAFT_ORDER_READY_FOR_REVIEW_JUDGE.name().equals(templateName)) {
            addJudgeReadyToReviewTemplateVars(notificationRequest, templateVars);
        }

        setIntervenerSolicitorDetails(notificationRequest, templateName, templateVars);

        templateVars.putAll(emailTemplateVars.get(templateName));
        return templateVars;
    }

    private void setIntervenerSolicitorDetails(NotificationRequest notificationRequest, String templateName, Map<String, Object> templateVars) {
        if (INTERVENER_SOLICITOR_ADDED_EMAIL.equals(templateName) || INTERVENER_SOLICITOR_REMOVED_EMAIL.equals(templateName)) {
            templateVars.put("intervenerFullName", notificationRequest.getIntervenerFullName());
            templateVars.put("intervenerSolicitorReferenceNumber", notificationRequest.getIntervenerSolicitorReferenceNumber());
            templateVars.put("intervenerSolicitorFirm", notificationRequest.getIntervenerSolicitorFirm());
            templateVars.put(PHONE_OPENING_HOURS, notificationRequest.getPhoneOpeningHours());
        }
    }

    private EmailToSend generateEmail(String destinationAddress,
                                      String templateName,
                                      Map<String, Object> templateVars) {
        String referenceId = UUID.randomUUID().toString();
        String templateId = emailTemplates.get(templateName);
        return new EmailToSend(destinationAddress, templateId, templateVars, referenceId);
    }

    private void sendEmail(EmailToSend emailToSend, String emailDescription) {
        String templateId = emailToSend.getTemplateId();
        String referenceId = emailToSend.getReferenceId();
        try {
            log.info("Attempting to send {} email with template {}. Reference ID: {}", emailDescription, templateId, referenceId);
            emailClient.sendEmail(
                templateId,
                emailToSend.getEmailAddress(),
                emailToSend.getTemplateFields(),
                referenceId
            );
            log.info("Sending email success. Reference ID: {}", referenceId);
        } catch (NotificationClientException e) {
            log.warn("Failed to send email. Reference ID: {}. Reason:", referenceId, e);
        }
    }

    private JSONObject preparedForEmailAttachment(final byte[] documentContents) {
        try {
            if (documentContents != null) {
                return NotificationClient.prepareUpload(documentContents);
            }
        } catch (NotificationClientException e) {
            log.warn("Failed to attach document to email", e);
        }
        return null;
    }

    private void addJudgeReadyToReviewTemplateVars(NotificationRequest notificationRequest,
                                                   Map<String, Object> templateVars) {
        templateVars.put("hearingDate", notificationRequest.getHearingDate());
    }

}
