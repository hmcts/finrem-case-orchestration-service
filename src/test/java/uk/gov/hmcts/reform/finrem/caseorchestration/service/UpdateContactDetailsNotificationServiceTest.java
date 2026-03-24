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
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.notificationrequest.FinremNotificationRequestMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdate;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdateHistoryCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.NocLetterNotificationService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOfRepresentationRequest.APPLICANT_PARTY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOfRepresentationRequest.RESPONDENT_PARTY;
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

    static Stream<Arguments> shouldUseConsentedNocTemplate_whenLastRepresentationUpdateIsRespondent() {
        return Stream.of(
            // Respondent changed, consented application
            Arguments.of(RESPONDENT_PARTY, NotificationParty.RESPONDENT_SOLICITOR_ONLY, true, true, FR_CONSENTED_NOC_CASEWORKER),

            // Applicant changed, consented application
            Arguments.of(APPLICANT_PARTY, NotificationParty.APPLICANT_SOLICITOR_ONLY, false, true, FR_CONSENTED_NOC_CASEWORKER),

            // Respondent changed, contested application
            Arguments.of(RESPONDENT_PARTY, NotificationParty.RESPONDENT_SOLICITOR_ONLY, true, false, FR_CONTESTED_NOC_CASEWORKER),

            // Applicant changed, contested application
            Arguments.of(APPLICANT_PARTY, NotificationParty.APPLICANT_SOLICITOR_ONLY, false, false, FR_CONTESTED_NOC_CASEWORKER)

        );
    }

    @ParameterizedTest
    @MethodSource
    void shouldUseConsentedNocTemplate_whenLastRepresentationUpdateIsRespondent(String party,
                                                                                NotificationParty notificationParty,
                                                                                boolean isRespondentSolicitorChanged,
                                                                                boolean isConsented,
                                                                                EmailTemplateNames emailTemplateNames) {
        FinremCaseDetails caseDetails = mock(FinremCaseDetails.class);
        FinremCaseData caseData = mock(FinremCaseData.class);

        when(caseData.getRepresentationUpdateHistory()).thenReturn(List.of(
            RepresentationUpdateHistoryCollection.builder()
                .value(RepresentationUpdate.builder()
                    .party(party)
                    .date(LocalDateTime.of(2026, 3, 24, 23, 58))
                    .build())
                .build(), // max
            RepresentationUpdateHistoryCollection.builder()
                .value(RepresentationUpdate.builder()
                    .party(RESPONDENT_PARTY)
                    .date(LocalDateTime.of(2026, 2, 24, 23, 58))
                    .build())
                .build()
        ));
        when(caseDetails.isConsentedApplication()).thenReturn(isConsented);
        when(caseDetails.getData()).thenReturn(caseData);

        // stub mapper
        NotificationRequest mockRequest = mock(NotificationRequest.class);
        when(finremNotificationRequestMapper.getNotificationRequestForNoticeOfChange(caseDetails, isRespondentSolicitorChanged))
            .thenReturn(mockRequest);

        // stub internal logic if needed (see note below)

        SendCorrespondenceEvent result =
            updateContactDetailsNotificationService.prepareNocEmailToLitigantSolicitor(caseDetails);

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
}
