package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.notificationrequest.FinremNotificationRequestMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.NocLetterNotificationService;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONSENTED_NOC_CASEWORKER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_NOC_CASEWORKER;

@ExtendWith(MockitoExtension.class)
class UpdateContactDetailsNotificationServiceTest {

    @Mock
    private FinremNotificationRequestMapper finremNotificationRequestMapper;

    @Mock
    private NocLetterNotificationService nocLetterNotificationService;

    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    @InjectMocks
    private UpdateContactDetailsNotificationService updateContactDetailsNotificationService;

    @Test
    void shouldReturnTrue_whenRepresentativeChangeIsYes() {
        FinremCaseData caseData = mock(FinremCaseData.class);
        ContactDetailsWrapper contactDetailsWrapper = spy(ContactDetailsWrapper.builder().build());
        when(caseData.getContactDetailsWrapper()).thenReturn(contactDetailsWrapper);
        when(contactDetailsWrapper.getUpdateIncludesRepresentativeChange()).thenReturn(YesOrNo.YES);

        boolean result = updateContactDetailsNotificationService.requiresNotifications(caseData);

        assertTrue(result);
    }

    @ParameterizedTest
    @NullSource
    @EnumSource(value = YesOrNo.class, names = "NO")
    void shouldReturnFalse_whenRepresentativeChangeIsNo(YesOrNo updateIncludesRepresentativeChange) {
        FinremCaseData caseData = mock(FinremCaseData.class);
        ContactDetailsWrapper contactDetailsWrapper = spy(ContactDetailsWrapper.builder().build());
        when(caseData.getContactDetailsWrapper()).thenReturn(contactDetailsWrapper);
        when(contactDetailsWrapper.getUpdateIncludesRepresentativeChange()).thenReturn(updateIncludesRepresentativeChange);

        boolean result = updateContactDetailsNotificationService.requiresNotifications(caseData);

        assertFalse(result);
    }

    static Stream<Arguments> shouldUseProperNocTemplate() {
        return Stream.of(
            // Respondent changed, consented application
            Arguments.of(NotificationParty.RESPONDENT_SOLICITOR_ONLY, true, true, FR_CONSENTED_NOC_CASEWORKER),

            // Applicant changed, consented application
            Arguments.of(NotificationParty.APPLICANT_SOLICITOR_ONLY, false, true, FR_CONSENTED_NOC_CASEWORKER),

            // Respondent changed, contested application
            Arguments.of(NotificationParty.RESPONDENT_SOLICITOR_ONLY, true, false, FR_CONTESTED_NOC_CASEWORKER),

            // Applicant changed, contested application
            Arguments.of(NotificationParty.APPLICANT_SOLICITOR_ONLY, false, false, FR_CONTESTED_NOC_CASEWORKER)
        );
    }

    @ParameterizedTest
    @MethodSource
    void shouldUseProperNocTemplate(NotificationParty notificationParty,
                                    boolean isRespondentSolicitorChanged, boolean isConsented,
                                    EmailTemplateNames emailTemplateNames) {
        FinremCaseDetails caseDetails = mock(FinremCaseDetails.class);
        when(caseDetails.isConsentedApplication()).thenReturn(isConsented);

        // stub mapper
        NotificationRequest mockRequest = mock(NotificationRequest.class);
        when(finremNotificationRequestMapper.getNotificationRequestForNoticeOfChange(caseDetails, isRespondentSolicitorChanged))
            .thenReturn(mockRequest);

        SendCorrespondenceEvent result =
            updateContactDetailsNotificationService.prepareNocEmailToNewSolicitor(caseDetails, isRespondentSolicitorChanged);

        assertAll(
            () -> assertThat(result).extracting(SendCorrespondenceEvent::getEmailTemplate)
                .isEqualTo(emailTemplateNames),
            () -> assertThat(result)
                .extracting(
                    SendCorrespondenceEvent::getEmailTemplate,
                    SendCorrespondenceEvent::getCaseDetails,
                    SendCorrespondenceEvent::getEmailNotificationRequest,
                    SendCorrespondenceEvent::getNotificationParties)
                .containsExactly(emailTemplateNames, caseDetails, mockRequest, List.of(notificationParty))
        );
    }

    @Test
    void shouldSendNocLetterToLitigants() {
        // Arrange
        FinremCaseDetails finremCaseDetails = mock(FinremCaseDetails.class);
        FinremCaseDetails finremCaseDetailsBefore = mock(FinremCaseDetails.class);

        CaseDetails mappedCaseDetails = mock(CaseDetails.class);
        CaseDetails mappedCaseDetailsBefore = mock(CaseDetails.class);

        when(finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails)).thenReturn(mappedCaseDetails);
        when(finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetailsBefore)).thenReturn(mappedCaseDetailsBefore);

        // Act
        updateContactDetailsNotificationService.sendNocLetterToLitigants(
            finremCaseDetails, finremCaseDetailsBefore, AUTH_TOKEN
        );

        // Assert
        verify(nocLetterNotificationService)
            .sendNoticeOfChangeLetters(mappedCaseDetails, mappedCaseDetailsBefore, AUTH_TOKEN);
    }
}
