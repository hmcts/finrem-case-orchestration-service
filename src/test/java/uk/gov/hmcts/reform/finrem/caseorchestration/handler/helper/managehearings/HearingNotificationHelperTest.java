package uk.gov.hmcts.reform.finrem.caseorchestration.handler.helper.managehearings;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.managehearings.HearingNotificationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogs;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HearingNotificationHelperTest {

    @TestLogs
    private final TestLogger logs = new TestLogger(HearingNotificationHelper.class);

    // inkevct mocks?
    @InjectMocks
    private HearingNotificationHelper helper;

    @Test
    void shouldReturnCorrectValuesWhenShouldSendNotificationUsed() {
        Hearing hearing = mock(Hearing.class);

        when(hearing.getHearingNoticePrompt()).thenReturn(YesOrNo.NO);
        assertTrue(helper.shouldNotSendNotification(hearing));

        when(hearing.getHearingNoticePrompt()).thenReturn(YesOrNo.YES);
        assertFalse(helper.shouldNotSendNotification(hearing));

        when(hearing.getHearingNoticePrompt()).thenReturn(null);
        assertTrue(helper.shouldNotSendNotification(hearing));
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
    void shouldThrowExceptionWhenGetHearingInContextCalledForEmptyHearingCollection() {
        UUID hearingId = UUID.randomUUID();

        ManageHearingsWrapper wrapper = new ManageHearingsWrapper();
        wrapper.setWorkingHearingId(hearingId);

        FinremCaseData caseData = new FinremCaseData();
        caseData.setManageHearingsWrapper(wrapper);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                helper.getHearingInContext(caseData));

        assertTrue(exception.getMessage().contains(
                String.format("No hearings available to search for. Working hearing ID is: %s",
                        hearingId)));
    }

//    /**
//     * Required for Sonar Coverage. Replace with proper test when RESP_SOLICITOR switch case is implemented.
//     * Also write test for when RESP_SOLICITOR should not receive a notification.
//     */
//    @Test
//    void shouldSendCorrectNotificationForRespSolicitorParty() {
//        DynamicMultiSelectListElement party = new DynamicMultiSelectListElement();
//        party.setCode(CaseRole.RESP_SOLICITOR.getCcdCode());
//
//        FinremCaseDetails caseDetails = new FinremCaseDetails();
//        Hearing hearing = new Hearing();
//
//        helper.sendHearingCorrespondenceByParty(party, caseDetails, hearing, AUTH_TOKEN);
//
//        assertThat(logs.getInfos()).contains("Handling case: RESP_SOLICITOR, work to follow");
//    }

//    /**
//     * Required for Sonar Coverage. Replace with proper test when INTVR_SOLICITOR_1 switch case is implemented.
//     * Also write test for when INTVR_SOLICITOR_1 should not receive a notification.
//     */
//    @Test
//    void shouldSendCorrectNotificationForIntervener1Party() {
//        DynamicMultiSelectListElement party = new DynamicMultiSelectListElement();
//        party.setCode(CaseRole.INTVR_SOLICITOR_1.getCcdCode());
//
//        FinremCaseDetails caseDetails = new FinremCaseDetails();
//        Hearing hearing = new Hearing();
//
//        helper.sendHearingCorrespondenceByParty(party, caseDetails, hearing, AUTH_TOKEN);
//
//        assertThat(logs.getInfos()).contains("Handling case: INTVR_SOLICITOR_1, work to follow");
//    }

//    /**
//     * Required for Sonar Coverage. Replace with proper test when INTVR_SOLICITOR_2 switch case is implemented.
//     * Also write test for when INTVR_SOLICITOR_2 should not receive a notification.
//     */
//    @Test
//    void shouldSendCorrectNotificationForIntervener2Party() {
//        DynamicMultiSelectListElement party = new DynamicMultiSelectListElement();
//        party.setCode(CaseRole.INTVR_SOLICITOR_2.getCcdCode());
//
//        FinremCaseDetails caseDetails = new FinremCaseDetails();
//        Hearing hearing = new Hearing();
//
//        helper.sendHearingCorrespondenceByParty(party, caseDetails, hearing, AUTH_TOKEN);
//
//        assertThat(logs.getInfos()).contains("Handling case: INTVR_SOLICITOR_2, work to follow");
//    }

//    /**
//     * Required for Sonar Coverage. Replace with proper test when INTVR_SOLICITOR_3 switch case is implemented.
//     * Also write test for when INTVR_SOLICITOR_3 should not receive a notification.
//     */
//    @Test
//    void shouldSendCorrectNotificationForIntervener3Party() {
//        DynamicMultiSelectListElement party = new DynamicMultiSelectListElement();
//        party.setCode(CaseRole.INTVR_SOLICITOR_3.getCcdCode());
//
//        FinremCaseDetails caseDetails = new FinremCaseDetails();
//        Hearing hearing = new Hearing();
//
//        helper.sendHearingCorrespondenceByParty(party, caseDetails, hearing, AUTH_TOKEN);
//
//        assertThat(logs.getInfos()).contains("Handling case: INTVR_SOLICITOR_3, work to follow");
//    }

//    /**
//     * Required for Sonar Coverage. Replace with proper test when INTVR_SOLICITOR_4 switch case is implemented.
//     * Also write test for when INTVR_SOLICITOR_4 should not receive a notification.
//     */
//    @Test
//    void shouldSendCorrectNotificationForIntervener4Party() {
//        DynamicMultiSelectListElement party = new DynamicMultiSelectListElement();
//        party.setCode(CaseRole.INTVR_SOLICITOR_4.getCcdCode());
//
//        FinremCaseDetails caseDetails = new FinremCaseDetails();
//        Hearing hearing = new Hearing();
//
//        helper.sendHearingCorrespondenceByParty(party, caseDetails, hearing, AUTH_TOKEN);
//
//        assertThat(logs.getInfos()).contains("Handling case: INTVR_SOLICITOR_4, work to follow");
//    }
}
