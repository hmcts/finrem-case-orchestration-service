package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.TestConstants;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.client.EmailClient;
import uk.gov.service.notify.NotificationClientException;

import java.lang.reflect.Field;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_HWF_SUCCESSFUL;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("local")
public class LocalEmailServiceTest {

    private NotificationRequest notificationRequest;

    @InjectMocks
    private LocalEmailService localEmailService;

    @Mock
    EmailClient mockEmailClient;

    /**
     * For each template tested, add to the temp maps for emailTemplateVars and emailTemplates.
     * Uses reflection to get the maps into the LocalEmailService instance.
     */
    @BeforeEach
    void setUp() throws Exception {

        notificationRequest = new NotificationRequest();

        Field templateVarsField = EmailService.class.getDeclaredField("emailTemplateVars");
        templateVarsField.setAccessible(true);
        templateVarsField.set(localEmailService, Map.of(
                "FR_HWF_SUCCESSFUL", Map.of("someKey", "someValue")
        ));

        Field templatesField = EmailService.class.getDeclaredField("emailTemplates");
        templatesField.setAccessible(true);
        templatesField.set(localEmailService, Map.of(
                "FR_HWF_SUCCESSFUL", "template-id-123"
        ));
    }

    /**
     * Simple test to check that the LocalEmailService will generate a preview of the email template.
     * Also checks that the LocalEmailService will never call sendEmail on the email client.
     *
     * @throws NotificationClientException if there is an issue with generateTemplatePreview.
     */
    @Test
    void sendConfirmationEmailGeneratesPreviewForLocalProfile()
            throws NotificationClientException {

        notificationRequest.setCaseType(TestConstants.CONSENTED);

        localEmailService.sendConfirmationEmail(notificationRequest, FR_HWF_SUCCESSFUL);

        verify(mockEmailClient).generateTemplatePreview(any(), any());
        verify(mockEmailClient, never()).sendEmail(any(), any(), any(), any());
    }
}
