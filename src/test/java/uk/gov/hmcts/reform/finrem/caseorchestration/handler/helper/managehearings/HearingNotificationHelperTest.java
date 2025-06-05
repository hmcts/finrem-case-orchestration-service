package uk.gov.hmcts.reform.finrem.caseorchestration.handler.helper.managehearings;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.managehearings.HearingNotificationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.ManageHearingsNotificationRequestMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogs;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class HearingNotificationHelperTest {

    @TestLogs
    private final TestLogger logs = new TestLogger(HearingNotificationHelper.class);

    @Mock
    private ManageHearingsNotificationRequestMapper notificationRequestMapper;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private HearingNotificationHelper helper;

    @Test
    void shouldReturnCorrectValuesWhenShouldSendNotificationUsed() {
        Hearing hearing = mock(Hearing.class);

        when(hearing.getHearingNoticePrompt()).thenReturn(YesOrNo.YES);
        assertTrue(helper.shouldSendNotification(hearing));

        when(hearing.getHearingNoticePrompt()).thenReturn(YesOrNo.NO);
        assertFalse(helper.shouldSendNotification(hearing));

        when(hearing.getHearingNoticePrompt()).thenReturn(null);
        assertFalse(helper.shouldSendNotification(hearing));
    }

    @Test
    void shouldReturnHearingInContextWhenIdMatches() {
        UUID hearingId = UUID.randomUUID();
        Hearing hearing = new Hearing();
        ManageHearingsWrapper wrapper = new ManageHearingsWrapper();
        wrapper.setHearings(List.of(new ManageHearingsCollectionItem(hearingId, hearing)));
        wrapper.setWorkingHearingId(hearingId);

        FinremCaseData caseData = new FinremCaseData();
        caseData.setManageHearingsWrapper(wrapper);

        Hearing result = helper.getHearingInContext(caseData);

        assertEquals(hearing, result);
    }

    @Test
    void shouldThrowExceptionWhenHearingIdNotFound() {
        UUID hearingId = UUID.randomUUID();
        UUID nonMatchingWorkingHearingId = UUID.randomUUID();

        Hearing hearing = new Hearing();
        ManageHearingsWrapper wrapper = new ManageHearingsWrapper();
        wrapper.setHearings(List.of(new ManageHearingsCollectionItem(hearingId, hearing)));
        wrapper.setWorkingHearingId(nonMatchingWorkingHearingId);

        FinremCaseData caseData = new FinremCaseData();
        caseData.setManageHearingsWrapper(wrapper);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                helper.getHearingInContext(caseData));

        assertTrue(exception.getMessage().contains("Hearing not found for the given ID"));
    }

    @Test
    void shouldSendHearingNotificationToApplicant() {
        FinremCaseDetails finremCaseDetails = new FinremCaseDetails();
        Hearing hearing = new Hearing();
        NotificationRequest notificationRequest = new NotificationRequest();

        when(notificationRequestMapper.buildHearingNotificationForApplicantSolicitor(finremCaseDetails, hearing))
                .thenReturn(notificationRequest);

        helper.sendHearingNotificationToApplicantSolicitor(finremCaseDetails, hearing);

        verify(notificationRequestMapper).buildHearingNotificationForApplicantSolicitor(finremCaseDetails, hearing);
        verify(notificationService).sendHearingNotificationToApplicant(notificationRequest);
    }

    @Test
    void shouldSendCorrectNotificationForAppSolicitorParty() {
        DynamicMultiSelectListElement party = new DynamicMultiSelectListElement();
        party.setCode(CaseRole.APP_SOLICITOR.getCcdCode());

        FinremCaseDetails caseDetails = new FinremCaseDetails();
        Hearing hearing = new Hearing();
        NotificationRequest notificationRequest = new NotificationRequest();

        when(notificationRequestMapper.buildHearingNotificationForApplicantSolicitor(caseDetails, hearing))
                .thenReturn(notificationRequest);

        helper.sendHearingNotificationsByParty(party, caseDetails, hearing);

        verify(notificationRequestMapper).buildHearingNotificationForApplicantSolicitor(caseDetails, hearing);
        verify(notificationService).sendHearingNotificationToApplicant(notificationRequest);
    }

    /**
     * Required for Sonar Coverage. Replace with proper test when RESP_SOLICITOR switch case is implemented.
     * Also write test for when RESP_SOLICITOR should not receive a notification.
     */
    @Test
    void shouldSendCorrectNotificationForRespSolicitorParty() {
        DynamicMultiSelectListElement party = new DynamicMultiSelectListElement();
        party.setCode(CaseRole.RESP_SOLICITOR.getCcdCode());

        FinremCaseDetails caseDetails = new FinremCaseDetails();
        Hearing hearing = new Hearing();

        helper.sendHearingNotificationsByParty(party, caseDetails, hearing);

        assertThat(logs.getInfos()).contains("Handling case: RESP_SOLICITOR, work to follow");
    }

    /**
     * Required for Sonar Coverage. Replace with proper test when INTVR_SOLICITOR_1 switch case is implemented.
     * Also write test for when INTVR_SOLICITOR_1 should not receive a notification.
     */
    @Test
    void shouldSendCorrectNotificationForIntervener1Party() {
        DynamicMultiSelectListElement party = new DynamicMultiSelectListElement();
        party.setCode(CaseRole.INTVR_SOLICITOR_1.getCcdCode());

        FinremCaseDetails caseDetails = new FinremCaseDetails();
        Hearing hearing = new Hearing();

        helper.sendHearingNotificationsByParty(party, caseDetails, hearing);

        assertThat(logs.getInfos()).contains("Handling case: INTVR_SOLICITOR_1, work to follow");
    }

    /**
     * Required for Sonar Coverage. Replace with proper test when INTVR_SOLICITOR_2 switch case is implemented.
     * Also write test for when INTVR_SOLICITOR_2 should not receive a notification.
     */
    @Test
    void shouldSendCorrectNotificationForIntervener2Party() {
        DynamicMultiSelectListElement party = new DynamicMultiSelectListElement();
        party.setCode(CaseRole.INTVR_SOLICITOR_2.getCcdCode());

        FinremCaseDetails caseDetails = new FinremCaseDetails();
        Hearing hearing = new Hearing();

        helper.sendHearingNotificationsByParty(party, caseDetails, hearing);

        assertThat(logs.getInfos()).contains("Handling case: INTVR_SOLICITOR_2, work to follow");
    }

    /**
     * Required for Sonar Coverage. Replace with proper test when INTVR_SOLICITOR_3 switch case is implemented.
     * Also write test for when INTVR_SOLICITOR_3 should not receive a notification.
     */
    @Test
    void shouldSendCorrectNotificationForIntervener3Party() {
        DynamicMultiSelectListElement party = new DynamicMultiSelectListElement();
        party.setCode(CaseRole.INTVR_SOLICITOR_3.getCcdCode());

        FinremCaseDetails caseDetails = new FinremCaseDetails();
        Hearing hearing = new Hearing();

        helper.sendHearingNotificationsByParty(party, caseDetails, hearing);

        assertThat(logs.getInfos()).contains("Handling case: INTVR_SOLICITOR_3, work to follow");
    }

    /**
     * Required for Sonar Coverage. Replace with proper test when INTVR_SOLICITOR_4 switch case is implemented.
     * Also write test for when INTVR_SOLICITOR_4 should not receive a notification.
     */
    @Test
    void shouldSendCorrectNotificationForIntervener4Party() {
        DynamicMultiSelectListElement party = new DynamicMultiSelectListElement();
        party.setCode(CaseRole.INTVR_SOLICITOR_4.getCcdCode());

        FinremCaseDetails caseDetails = new FinremCaseDetails();
        Hearing hearing = new Hearing();

        helper.sendHearingNotificationsByParty(party, caseDetails, hearing);

        assertThat(logs.getInfos()).contains("Handling case: INTVR_SOLICITOR_4, work to follow");
    }

    @Test
    void shouldThrowExceptionIfSendHearingNotificationsByPartyGivenUnexpectedCaseRole() {
        // Caseworker is not handled in the switch statement, so it should throw an exception
        DynamicMultiSelectListElement party = new DynamicMultiSelectListElement();
        party.setCode(CaseRole.CASEWORKER.getCcdCode());

        FinremCaseDetails caseDetails = new FinremCaseDetails();
        caseDetails.setId(12345L); // to check case reference in exception message

        Hearing hearing = new Hearing();

        // When
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            helper.sendHearingNotificationsByParty(party, caseDetails, hearing);
        });

        // Then
        assertTrue(
                exception.getMessage().contains(
                        String.format(
                                "Unexpected value: %s for case reference 12345",
                                CaseRole.CASEWORKER
                        )
                )
        );
    }
}
