package uk.gov.hmcts.reform.finrem.caseorchestration.handler.helper.managehearings;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ManageHearings.HearingNotificationHelper;
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

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class HearingNotificationHelperTest {

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

    @Test
    void shouldNotSendNotificationForNonAppSolicitorParty() {
        DynamicMultiSelectListElement party = new DynamicMultiSelectListElement();
        party.setCode(CaseRole.RESP_SOLICITOR.getCcdCode());

        FinremCaseDetails caseDetails = new FinremCaseDetails();
        Hearing hearing = new Hearing();
        NotificationRequest notificationRequest = new NotificationRequest();

        helper.sendHearingNotificationsByParty(party, caseDetails, hearing);

        verify(notificationRequestMapper, never()).buildHearingNotificationForApplicantSolicitor(caseDetails, hearing);
        verify(notificationService, never()).sendHearingNotificationToApplicant(notificationRequest);
    }
}
