package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.managehearing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.managehearings.HearingNotificationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.ManageHearingsNotificationRequestMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.ManageHearingsDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogs;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
class ManageHearingsCorresponderTest {

    @Mock
    private HearingNotificationHelper hearingNotificationHelper;

    @Mock
    private ManageHearingsNotificationRequestMapper notificationRequestMapper;

    @Mock
    private NotificationService notificationService;

    @Mock
    private BulkPrintService bulkPrintService;

    @Mock
    private ManageHearingsDocumentService manageHearingsDocumentService;

    @Mock
    private DocumentHelper documentHelper;

    @InjectMocks
    private ManageHearingsCorresponder corresponder;

    @TestLogs
    private final TestLogger logs = new TestLogger(ManageHearingsCorresponder.class);

    @Test
    void shouldProcessCorrespondenceWhenNotificationsRequiredAndPartiesExist() {
        // Arrange
        DynamicMultiSelectList partyList = buildPartiesList(Set.of(CaseRole.APP_SOLICITOR));
        Hearing hearing = mock(Hearing.class);
        when(hearing.getPartiesOnCaseMultiSelectList()).thenReturn(partyList);
        FinremCallbackRequest callbackRequest = callbackRequest();
        when(hearingNotificationHelper.getHearingInContext(callbackRequest.getCaseDetails().getData())).thenReturn(hearing);
        when(hearingNotificationHelper.shouldNotSendNotification(hearing)).thenReturn(false);

        // Act
        corresponder.sendHearingCorrespondence(callbackRequest, AUTH_TOKEN);

        // Assert that the first method used when processing applicant solicitor correspondence is called.
        verify(hearingNotificationHelper).emailingToApplicantSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    void shouldNotSendNotificationsWhenShouldNotSendNotificationTrue() {
        FinremCallbackRequest callbackRequest = callbackRequest();
        Hearing hearing = new Hearing();

        when(hearingNotificationHelper.getHearingInContext(callbackRequest.getCaseDetails().getData())).thenReturn(hearing);
        when(hearingNotificationHelper.shouldNotSendNotification(hearing)).thenReturn(true);

        corresponder.sendHearingCorrespondence(callbackRequest, AUTH_TOKEN);

        verify(hearingNotificationHelper, never())
                .emailingToApplicantSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    void shouldNotSendNotificationsNoPartiesSelected() {
        // Arrange
        FinremCallbackRequest callbackRequest = callbackRequest();

        DynamicMultiSelectList list = new DynamicMultiSelectList();
        list.setValue(List.of());

        Hearing hearing = mock(Hearing.class);
        when(hearing.getPartiesOnCaseMultiSelectList()).thenReturn(list);

        when(hearingNotificationHelper.getHearingInContext(callbackRequest.getCaseDetails().getData())).thenReturn(hearing);
        when(hearingNotificationHelper.shouldNotSendNotification(hearing)).thenReturn(false);

        // Act
        corresponder.sendHearingCorrespondence(callbackRequest, AUTH_TOKEN);

        verify(hearingNotificationHelper, never())
                .emailingToApplicantSolicitor(callbackRequest.getCaseDetails());
    }

    /**
     * Checks that sendHearingNotificationToApplicant is called when:
     * - CaseRole is APP_SOLICITOR
     * - shouldNotSendNotification returns false
     * - emailingToApplicantSolicitor returns true
     */
    @Test
    void shouldSendHearingNotificationToApplicant() {
        // Setup
        DynamicMultiSelectList partyList = buildPartiesList(Set.of(CaseRole.APP_SOLICITOR));
        Hearing hearing = mock(Hearing.class);
        when(hearing.getPartiesOnCaseMultiSelectList()).thenReturn(partyList);
        FinremCallbackRequest callbackRequest = callbackRequest();
        NotificationRequest notificationRequest = new NotificationRequest();
        when(notificationRequestMapper.buildHearingNotificationForApplicantSolicitor(callbackRequest.getCaseDetails(),
                hearing)).thenReturn(notificationRequest);

        // Arrange
        when(hearingNotificationHelper.getHearingInContext(callbackRequest.getCaseDetails().getData())).thenReturn(hearing);
        when(hearingNotificationHelper.shouldNotSendNotification(hearing)).thenReturn(false);
        when(hearingNotificationHelper.emailingToApplicantSolicitor(callbackRequest.getCaseDetails())).thenReturn(true);

        // act
        corresponder.sendHearingCorrespondence(callbackRequest, AUTH_TOKEN);

        // Verify
        verify(notificationService).sendHearingNotificationToApplicant(notificationRequest);
        verify(hearingNotificationHelper, never()).shouldSendHearingNoticeOnly(any(), any());
        verify(bulkPrintService, never()).printApplicantDocuments((FinremCaseDetails) any(), any(), any());
        assertThat(logs.getInfos()).contains("Notification for applicant solicitor. Request sent for case ID: 123");
    }

    /**
     * Checks that sendHearingNotificationToApplicant is called when:
     * - CaseRole is APP_SOLICITOR
     * - shouldNotSendNotification returns false
     * - postingToApplicant returns true
     * - shouldSendHearingNoticeOnly returns true
     */
    @Test
    void shouldSendPaperNoticeToApplicant() {
        // Setup
        DynamicMultiSelectList partyList = buildPartiesList(Set.of(CaseRole.APP_SOLICITOR));
        Hearing hearing = mock(Hearing.class);
        when(hearing.getPartiesOnCaseMultiSelectList()).thenReturn(partyList);
        FinremCallbackRequest callbackRequest = callbackRequest();
        when(manageHearingsDocumentService.getHearingNotice(callbackRequest.getCaseDetails())).thenReturn(new CaseDocument());

        // Arrange
        when(hearingNotificationHelper.getHearingInContext(callbackRequest.getCaseDetails().getData())).thenReturn(hearing);
        when(hearingNotificationHelper.shouldNotSendNotification(hearing)).thenReturn(false);
        when(hearingNotificationHelper.postingToApplicant(callbackRequest.getCaseDetails())).thenReturn(true);
        when(hearingNotificationHelper.shouldSendHearingNoticeOnly(callbackRequest.getCaseDetails(), hearing)).thenReturn(true);

        // act
        corresponder.sendHearingCorrespondence(callbackRequest, AUTH_TOKEN);

        // Verify
        verify(notificationService, never()).sendHearingNotificationToApplicant(any());
        verify(bulkPrintService).printApplicantDocuments((FinremCaseDetails) any(), any(), any());
        assertThat(logs.getInfos()).contains("Posting notice to applicant solicitor. Request sent for case ID: 123");
    }

    /**
     * Checks that sendHearingNotificationToApplicant handles a missing notice:
     * - CaseRole is APP_SOLICITOR
     * - shouldNotSendNotification returns false
     * - postingToApplicant returns true
     * - shouldSendHearingNoticeOnly returns true
     * - Hearing is intentionally missing the notice document.
     */
    @Test
    void sendPaperNoticeToApplicantShouldHandleMissingNotice() {
        // Setup
        DynamicMultiSelectList partyList = buildPartiesList(Set.of(CaseRole.APP_SOLICITOR));
        Hearing hearing = mock(Hearing.class);
        when(hearing.getPartiesOnCaseMultiSelectList()).thenReturn(partyList);
        FinremCallbackRequest callbackRequest = callbackRequest();
        when(manageHearingsDocumentService.getHearingNotice(callbackRequest.getCaseDetails())).thenReturn(null);

        // Arrange
        when(hearingNotificationHelper.getHearingInContext(callbackRequest.getCaseDetails().getData())).thenReturn(hearing);
        when(hearingNotificationHelper.shouldNotSendNotification(hearing)).thenReturn(false);
        when(hearingNotificationHelper.postingToApplicant(callbackRequest.getCaseDetails())).thenReturn(true);
        when(hearingNotificationHelper.shouldSendHearingNoticeOnly(callbackRequest.getCaseDetails(), hearing)).thenReturn(true);

        // act
        corresponder.sendHearingCorrespondence(callbackRequest, AUTH_TOKEN);

        // Verify
        verify(notificationService, never()).sendHearingNotificationToApplicant(any());
        assertThat(logs.getWarns()).contains("Hearing notice is null. No document sent for case ID: 123");
    }

    /**
     * Checks that sendHearingNotificationToApplicant throws IllegalStateException when illegal CaseRole is provided:
     * - CaseRole is CASEWORKER (which is not appropriate for hearing notifications)
     * - shouldNotSendNotification returns false
     */
    @Test
    void shouldThrowExceptionIfSendHearingNotificationsByPartyGivenUnexpectedCaseRole() {
        // Caseworker is not handled in the switch statement, so it should throw an exception

        // Setup
        DynamicMultiSelectList partyList = buildPartiesList(Set.of(CaseRole.CASEWORKER));
        Hearing hearing = mock(Hearing.class);
        when(hearing.getPartiesOnCaseMultiSelectList()).thenReturn(partyList);
        FinremCallbackRequest callbackRequest = callbackRequest();

        // Arrange
        when(hearingNotificationHelper.getHearingInContext(callbackRequest.getCaseDetails().getData())).thenReturn(hearing);
        when(hearingNotificationHelper.shouldNotSendNotification(hearing)).thenReturn(false);

        // Act
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            corresponder.sendHearingCorrespondence(callbackRequest, AUTH_TOKEN);
        });

        // Assert
        assertTrue(
            exception.getMessage().contains(
                String.format(
                    "Unexpected value: %s for case reference 123",
                    CaseRole.CASEWORKER
                )
            )
        );
    }

    /**
     * @return a FinremCallbackRequest with a case ID and empty case data.
     */
    private FinremCallbackRequest callbackRequest() {
        return FinremCallbackRequest
                .builder()
                .caseDetails(FinremCaseDetails.builder().id(123L)
                        .data(new FinremCaseData()).build())
                .build();
    }

    /**
     * Reusable test setup method to build a DynamicMultiSelectList from a set of CaseRoles
     */
    private DynamicMultiSelectList buildPartiesList(Set<CaseRole> caseRoles) {

        // build the list elements from the passed set of case roles
        List<DynamicMultiSelectListElement> elements = new ArrayList<>();
        for (CaseRole role : caseRoles) {
            DynamicMultiSelectListElement element = new DynamicMultiSelectListElement();
            element.setCode(role.getCcdCode());
            elements.add(element);
        }

        // create a DynamicMultiSelectList and set the value to the list of elements
        DynamicMultiSelectList list = new DynamicMultiSelectList();
        list.setValue(elements);
        return list;
    }
}
