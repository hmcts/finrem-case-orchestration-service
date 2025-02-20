package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.EmailAttachmentSizeExceededException;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.NotificationClientRuntimeException;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.lang.reflect.Field;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class EmailServiceTestJunit5 {

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() throws Exception {
        Field field = EmailService.class.getDeclaredField("emailTemplateVars");
        field.setAccessible(true);
        field.set(emailService, Map.of("FR_CONSENT_GENERAL_EMAIL_ATTACHMENT", Map.of()));
    }

    @ParameterizedTest
    @EnumSource(value = EmailTemplateNames.class, names = {"FR_CONSENT_GENERAL_EMAIL_ATTACHMENT", "FR_CONTESTED_GENERAL_EMAIL_ATTACHMENT"})
    void shouldThrowAnExceptionForGeneralEmailAttachment+WithAttachmentExceeded2MB(EmailTemplateNames emailTemplateNames) {
        NotificationRequest notificationRequest = NotificationRequest.builder()
            .generalEmailBody("test email body")
            .documentContents(new byte[2 * 1024 * 1024 + 1])
            .build();

        String template = emailTemplateNames.name();
        assertThrows(EmailAttachmentSizeExceededException.class, () -> emailService.buildTemplateVars(notificationRequest, template));
    }

    @Test
    void shouldThrowARunTimeExceptionWhenAttachDocumentFailure() {
        NotificationRequest notificationRequest = NotificationRequest.builder()
            .generalEmailBody("test email body")
            .documentContents(new byte[1])
            .build();

        try (MockedStatic<NotificationClient> mockedStatic = mockStatic(NotificationClient.class)) {
            mockedStatic.when(() -> NotificationClient.prepareUpload(any()))
                .thenThrow(new NotificationClientException("unexpectedNotificationClientException"));

            // Call the method that internally invokes prepareUpload
            NotificationClientRuntimeException exception =
                assertThrows(NotificationClientRuntimeException.class, () -> emailService.buildTemplateVars(notificationRequest,
                    "FR_CONSENT_GENERAL_EMAIL_ATTACHMENT"));
            assertThat(exception.getMessage()).isEqualTo("unexpectedNotificationClientException");
        }
    }
}
