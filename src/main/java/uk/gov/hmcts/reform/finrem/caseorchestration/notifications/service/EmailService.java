package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
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

@Profile("!local")
@Service
@Slf4j
@RequiredArgsConstructor
@SuppressWarnings("java:S6857")
public class EmailService {

    protected final EmailClient emailClient;
    private final NotificationClientExceptionResolver notificationClientExceptionResolver;

    @Value("#{${uk.gov.notify.email.templates}}")
    private Map<String, String> emailTemplates;

    @Value("#{${uk.gov.notify.email.template.vars}}")
    private Map<String, Map<String, String>> emailTemplateVars;

    @Value("#{${uk.gov.notify.email.contestedContactEmails}}")
    private Map<String, Map<String, String>> contestedContactEmails;

    @Value("${finrem.manageCase.baseurl}")
    private String manageCaseBaseUrl;

    public static final String CONTESTED = "contested";
    public static final String CONSENTED = "consented";
    private static final String PHONE_OPENING_HOURS = "phoneOpeningHours";
    private static final String HEARING_DATE = "hearingDate";
    private static final String MANAGE_CASE_BASE_URL = "manageCaseBaseUrl";
    private static final String DEFAULT_LINK_TO_SMART_SURVEY = "http://www.smartsurvey.co.uk/s/KCECE/";

    /**
     * Orchestrates sending an email based on the provided notification request and template.
     * Note, this service is used when the active profile (@profile class annotation) is not 'local'.
     * LocalEmailService is used for local testing, when the active profile is local.
     *
     * @param notificationRequest the request containing details for the email
     * @param template            the email template to use
     */
    public void sendConfirmationEmail(NotificationRequest notificationRequest, EmailTemplateNames template) {
        Map<String, Object> templateVars = buildTemplateVars(notificationRequest, template.name());
        EmailToSend emailToSend = generateEmail(notificationRequest.getNotificationEmail(), template.name(),
            templateVars, notificationRequest.getEmailReplyToId());
        log.info("Sending confirmation email on Case ID : {} using template: {}", notificationRequest.getCaseReferenceNumber(), template.name());
        sendEmail(emailToSend, "send Confirmation email for " + template.name());
    }

    protected Map<String, Object> buildTemplateVars(NotificationRequest notificationRequest, String templateName) {
        Map<String, Object> templateVars = new HashMap<>();

        populateDefaultTemplateVarsByDefault(templateVars);
        populateCourtNameAndCourtEmailTemplateVars(templateVars, notificationRequest, templateName);
        populateTemplateVarsFromNotificationRequest(templateVars, notificationRequest);
        populateTemplateVarsDependsOnEmailTemplate(templateVars, notificationRequest, templateName);
        populateTemplateVarsFromApplicationProperties(templateVars, templateName);

        return templateVars;
    }

    protected void populateTemplateVarsFromNotificationRequest(Map<String, Object> templateVars, NotificationRequest notificationRequest) {
        templateVars.put("caseReferenceNumber", notificationRequest.getCaseReferenceNumber());
        templateVars.put("solicitorReferenceNumber", notificationRequest.getSolicitorReferenceNumber());
        templateVars.put("divorceCaseNumber", notificationRequest.getDivorceCaseNumber());
        templateVars.put("notificationEmail", notificationRequest.getNotificationEmail());
        templateVars.put("name", notificationRequest.getName());
        templateVars.put("applicantName", notificationRequest.getApplicantName());
        templateVars.put("respondentName", notificationRequest.getRespondentName());
        templateVars.put("hearingType", notificationRequest.getHearingType());
        if (CONSENTED.equals(notificationRequest.getCaseType())) {
            templateVars.put("caseOrderType", notificationRequest.getCaseOrderType());
            templateVars.put("camelCaseOrderType", notificationRequest.getCamelCaseOrderType());
        }
        templateVars.put("dateOfIssue", notificationRequest.getDateOfIssue());
    }

    protected void populateDefaultTemplateVarsByDefault(Map<String, Object> templateVars) {
        templateVars.put("linkToSmartSurvey", DEFAULT_LINK_TO_SMART_SURVEY);
    }

    protected void populateCourtNameAndCourtEmailTemplateVars(Map<String, Object> templateVars, NotificationRequest notificationRequest,
                                                              String templateName) {
        //contested emails notifications require the court information, consented does not
        if ((CONTESTED.equals(notificationRequest.getCaseType()) || EmailTemplateNames.FR_CONSENTED_LIST_FOR_HEARING
            .name().equals(templateName))
            && !isEmpty(notificationRequest.getSelectedCourt())) {
            Map<String, String> courtDetails = contestedContactEmails.get(notificationRequest.getSelectedCourt());

            templateVars.put("courtName", courtDetails.get("name"));
            templateVars.put("courtEmail", courtDetails.get("email"));
        }

        // Override court name/email address values if present in the request
        if (StringUtils.isNotBlank(notificationRequest.getContactCourtName())) {
            templateVars.put("courtName", notificationRequest.getContactCourtName());
        }
        if (StringUtils.isNotBlank(notificationRequest.getContactCourtEmail())) {
            templateVars.put("courtEmail", notificationRequest.getContactCourtEmail());
        }
    }

    protected void populateTemplateVarsDependsOnEmailTemplate(Map<String, Object> templateVars, NotificationRequest notificationRequest,
                                                              String templateName) {
        if (EmailTemplateNames.FR_ASSIGNED_TO_JUDGE.name().equals(templateName)) {
            templateVars.put("isNotDigital", notificationRequest.getIsNotDigital());
        }
        //general emails and transfer to local court emails are the only templates that require the generalEmailBody
        if (EmailTemplateNames.FR_CONSENT_GENERAL_EMAIL.name().equals(templateName)
            || EmailTemplateNames.FR_CONTESTED_GENERAL_EMAIL.name().equals(templateName)
            || EmailTemplateNames.FR_CONSENT_GENERAL_EMAIL_ATTACHMENT.name().equals(templateName)
            || EmailTemplateNames.FR_CONTESTED_GENERAL_EMAIL_ATTACHMENT.name().equals(templateName)
            || EmailTemplateNames.FR_TRANSFER_TO_LOCAL_COURT.name().equals(templateName)
            || EmailTemplateNames.FR_CONTESTED_GENERAL_APPLICATION_REFER_TO_JUDGE.name().equals(templateName)) {
            templateVars.put("generalEmailBody", notificationRequest.getGeneralEmailBody());
        }
        if (EmailTemplateNames.FR_CONSENT_GENERAL_EMAIL_ATTACHMENT.name().equals(templateName)
            || EmailTemplateNames.FR_CONTESTED_GENERAL_EMAIL_ATTACHMENT.name().equals(templateName)) {
            templateVars.put("link_to_file", preparedForEmailAttachment(notificationRequest.getDocumentContents()));
        }
        if (CONSENTED.equals(notificationRequest.getCaseType()) && !EmailTemplateNames.FR_CONSENT_ORDER_AVAILABLE_CTSC.name().equals(templateName)) {
            templateVars.put(PHONE_OPENING_HOURS, notificationRequest.getPhoneOpeningHours());
        }
        if (EmailTemplateNames.FR_REJECT_GENERAL_APPLICATION.name().equals(templateName)) {
            templateVars.put("generalApplicationRejectionReason", notificationRequest.getGeneralApplicationRejectionReason());
        }
        if (EmailTemplateNames.FR_BARRISTER_ACCESS_ADDED.name().equals(templateName)
            || EmailTemplateNames.FR_BARRISTER_ACCESS_REMOVED.name().equals(templateName)) {
            templateVars.put("BarristerReferenceNumber", notificationRequest.getBarristerReferenceNumber());
            templateVars.put(PHONE_OPENING_HOURS, notificationRequest.getPhoneOpeningHours());
        }
        if (EmailTemplateNames.FR_INTERVENER_ADDED_EMAIL.name().equals(templateName)
            || EmailTemplateNames.FR_INTERVENER_REMOVED_EMAIL.name().equals(templateName)
            || EmailTemplateNames.FR_CONSENTED_REPRESENTATIVE_STOP_REPRESENTING_INTERVENER.name().equals(templateName)
            || EmailTemplateNames.FR_CONTESTED_REPRESENTATIVE_STOP_REPRESENTING_INTERVENER.name().equals(templateName)) {
            templateVars.put("intervenerFullName", notificationRequest.getIntervenerFullName());
            templateVars.put("intervenerSolicitorReferenceNumber", notificationRequest.getIntervenerSolicitorReferenceNumber());
            templateVars.put(PHONE_OPENING_HOURS, notificationRequest.getPhoneOpeningHours());
        }
        if (EmailTemplateNames.FR_CONTESTED_DRAFT_ORDER_READY_FOR_REVIEW_JUDGE.name().equals(templateName)
            || EmailTemplateNames.FR_CONTESTED_DRAFT_ORDER_READY_FOR_REVIEW_ADMIN.name().equals(templateName)) {
            templateVars.put(HEARING_DATE, notificationRequest.getHearingDate());
            templateVars.put(MANAGE_CASE_BASE_URL, manageCaseBaseUrl);
        }
        if (EmailTemplateNames.FR_CONTESTED_VACATE_NOTIFICATION_SOLICITOR.name().equals(templateName)) {
            templateVars.put("vacatedHearingType", notificationRequest.getVacatedHearingType());
            templateVars.put("vacatedHearingDateTime", notificationRequest.getVacatedHearingDateTime());
        }
        if (EmailTemplateNames.FR_INTERVENER_SOLICITOR_ADDED_EMAIL.name().equals(templateName)
            || EmailTemplateNames.FR_INTERVENER_SOLICITOR_REMOVED_EMAIL.name().equals(templateName)) {
            templateVars.put("intervenerFullName", notificationRequest.getIntervenerFullName());
            templateVars.put("intervenerSolicitorReferenceNumber", notificationRequest.getIntervenerSolicitorReferenceNumber());
            templateVars.put("intervenerSolicitorFirm", notificationRequest.getIntervenerSolicitorFirm());
            templateVars.put(PHONE_OPENING_HOURS, notificationRequest.getPhoneOpeningHours());
        }
        if (EmailTemplateNames.FR_CONTESTED_DRAFT_ORDER_REVIEW_OVERDUE.name().equals(templateName)) {
            addDraftOrderReviewOverdueTemplateVars(notificationRequest, templateVars);
        }
        if (EmailTemplateNames.FR_CONTESTED_DRAFT_ORDER_OR_PSA_REFUSED.name().equals(templateName)) {
            addRefusedDraftOrderOrPsaTemplateVars(notificationRequest, templateVars);
        }
    }

    protected void populateTemplateVarsFromApplicationProperties(Map<String, Object> templateVars, String templateName) {
        Map<String, String> fromApplicationContextProperties = emailTemplateVars.get(templateName);
        if (fromApplicationContextProperties != null) {
            templateVars.putAll(fromApplicationContextProperties);
        }
    }

    private void addDraftOrderReviewOverdueTemplateVars(NotificationRequest notificationRequest,
                                                        Map<String, Object> templateVars) {
        templateVars.put(HEARING_DATE, notificationRequest.getHearingDate());
        templateVars.put("judgeName", notificationRequest.getJudgeName());
        templateVars.put("oldestDraftOrderDate", notificationRequest.getOldestDraftOrderDate());
    }

    private void addRefusedDraftOrderOrPsaTemplateVars(NotificationRequest notificationRequest,
                                                       Map<String, Object> templateVars) {
        templateVars.put(HEARING_DATE, notificationRequest.getHearingDate());
        templateVars.put("judgeFeedback", notificationRequest.getJudgeFeedback());
        templateVars.put("documentName", notificationRequest.getDocumentName());
    }

    protected EmailToSend generateEmail(String destinationAddress, String templateName,
                                        Map<String, Object> templateVars, String emailReplyToId) {
        String referenceId = UUID.randomUUID().toString();
        String templateId = emailTemplates.get(templateName);
        return new EmailToSend(destinationAddress, templateId, templateVars, referenceId, emailReplyToId);
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
                referenceId,
                emailToSend.getEmailReplyToId()
            );
            log.info("Sending email success. Reference ID: {}", referenceId);
        } catch (NotificationClientException e) {
            log.warn("Failed to send email. Reference ID: {}. Reason: {}", referenceId, e.getMessage(), e);
            notificationClientExceptionResolver.resolve(e);
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
}
