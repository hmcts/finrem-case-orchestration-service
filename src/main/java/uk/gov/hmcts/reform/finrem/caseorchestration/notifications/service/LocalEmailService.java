package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.client.EmailClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailToSend;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.TemplatePreview;

import java.util.Map;

@Profile("local")
@Service
@Slf4j
@SuppressWarnings("java:S6857")
public class LocalEmailService extends EmailService {

    @Autowired
    public LocalEmailService(EmailClient emailClient, NotificationClientExceptionResolver exceptionResolver) {
        super(emailClient, exceptionResolver);
    }

    /**
     * Orchestrates previewing the email in the console, based on the provided notification request and template.
     * Note, this service is used when the active profile (@profile class annotation) is 'local'.
     * EmailService is used when the active profile is not 'local'.
     *
     * @param notificationRequest the request containing details for the email
     * @param template            the email template to use
     */
    @Override
    public void sendConfirmationEmail(NotificationRequest notificationRequest, EmailTemplateNames template) {
        Map<String, Object> templateVars = buildTemplateVars(notificationRequest, template.name());
        EmailToSend emailToSend = generateEmail(notificationRequest.getNotificationEmail(), template.name(),
                templateVars, notificationRequest.getEmailReplyToId());
        log.info("Creating a preview email for Case ID : {} using template: {}", notificationRequest.getCaseReferenceNumber(), template.name());
        previewEmail(emailToSend, "send Confirmation email for " + template.name());
    }

    /**
     * Uses emailClient to generate a preview of the email template.
     * @param emailToSend the email to send, containing template ID and fields
     * @param emailDescription a description of the email being previewed, for logging purposes
     */
    private void previewEmail(EmailToSend emailToSend, String emailDescription) {
        String templateId = emailToSend.getTemplateId();
        String referenceId = emailToSend.getReferenceId();
        try {
            log.info("Attempting to create a preview for {} with template {}. Reference ID: {}",
                    emailDescription, templateId, referenceId);
            TemplatePreview templatePreview = emailClient.generateTemplatePreview(
                    templateId,
                    emailToSend.getTemplateFields()
            );
            log.info("Preview successful. Rendered template:\n{}", templatePreview);
        } catch (NotificationClientException e) {
            log.warn("Failed to preview template '{}'. Reason: {}", templateId, e.getMessage(), e);
        }
    }
}
