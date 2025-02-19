package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.GovNotifyAttachmentSizeExceededException;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class EmailServiceTestJunit5 {

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() throws Exception {
        Field field = EmailService.class.getDeclaredField("emailTemplateVars");
        field.setAccessible(true);
        field.set(emailService, Map.of());
    }

    @ParameterizedTest
    @EnumSource(value = EmailTemplateNames.class, names= {"FR_CONSENT_GENERAL_EMAIL_ATTACHMENT", "FR_CONTESTED_GENERAL_EMAIL_ATTACHMENT"})
    void shouldThrowAnExceptionForGeneralEmailAttachmentConsentedWithAttachmentExceeded2MB(EmailTemplateNames emailTemplateNames) {
        NotificationRequest notificationRequest = NotificationRequest.builder()
            .generalEmailBody("test email body")
            .documentContents(new byte[2*1024*1024 + 1])
            .build();

        String template = emailTemplateNames.name();
        assertThrows(GovNotifyAttachmentSizeExceededException.class, () -> emailService.buildTemplateVars(notificationRequest, template));
    }
}
