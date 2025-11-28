package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.managehearing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.managehearings.HearingCorrespondenceHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.notificationrequest.ManageHearingsNotificationRequestMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.PartyOnCase;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.PartyOnCaseCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.hearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerFour;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOne;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerThree;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerTwo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.ManageHearingsDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
class ManageHearingsCorresponderTest {

    @Mock
    private HearingCorrespondenceHelper hearingCorrespondenceHelper;

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

    @Mock
    private GenericDocumentService genericDocumentService;

    @InjectMocks
    private ManageHearingsCorresponder corresponder;

    @TestLogs
    private final TestLogger logs = new TestLogger(ManageHearingsCorresponder.class);

    @Test
    void shouldProcessCorrespondenceWhenNotificationsRequiredAndPartiesExist() {
        // Arrange
        List<PartyOnCaseCollectionItem> partyList = buildPartiesList(Set.of(CaseRole.APP_SOLICITOR));
        Hearing hearing = mock(Hearing.class);
        when(hearing.getPartiesOnCase()).thenReturn(partyList);
        FinremCallbackRequest callbackRequest = callbackRequest();
        when(hearingCorrespondenceHelper.getHearingInContext(callbackRequest.getCaseDetails().getData())).thenReturn(hearing);
        when(hearingCorrespondenceHelper.shouldNotSendNotification(hearing)).thenReturn(false);

        // Act
        corresponder.sendHearingCorrespondence(callbackRequest, AUTH_TOKEN);

        // Assert that the first method used when processing applicant solicitor correspondence is called.
        verify(hearingCorrespondenceHelper).shouldEmailToApplicantSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    void shouldNotSendNotificationsWhenShouldNotSendNotificationTrue() {
        FinremCallbackRequest callbackRequest = callbackRequest();
        Hearing hearing = new Hearing();

        when(hearingCorrespondenceHelper.getHearingInContext(callbackRequest.getCaseDetails().getData())).thenReturn(hearing);
        when(hearingCorrespondenceHelper.shouldNotSendNotification(hearing)).thenReturn(true);

        corresponder.sendHearingCorrespondence(callbackRequest, AUTH_TOKEN);

        verify(hearingCorrespondenceHelper, never())
                .shouldEmailToApplicantSolicitor(callbackRequest.getCaseDetails());
    }

    @Test
    void shouldNotSendNotificationsNoPartiesSelected() {
        // Arrange
        FinremCallbackRequest callbackRequest = callbackRequest();

        List<PartyOnCaseCollectionItem> list = new ArrayList<>(List.of());

        Hearing hearing = mock(Hearing.class);
        when(hearing.getPartiesOnCase()).thenReturn(list);

        when(hearingCorrespondenceHelper.getHearingInContext(callbackRequest.getCaseDetails().getData())).thenReturn(hearing);
        when(hearingCorrespondenceHelper.shouldNotSendNotification(hearing)).thenReturn(false);

        // Act
        corresponder.sendHearingCorrespondence(callbackRequest, AUTH_TOKEN);

        verify(hearingCorrespondenceHelper, never())
                .shouldEmailToApplicantSolicitor(callbackRequest.getCaseDetails());
    }

    /**
     * Checks that sendHearingNotificationToParty is called with the right role.
     * - CaseRole is APP_SOLICITOR
     * - shouldNotSendNotification returns false
     * - shouldEmailToApplicantSolicitor returns true
     */
    @Test
    void shouldSendHearingNotificationsToApplicant() {
        // Setup
        List<PartyOnCaseCollectionItem> partyList = buildPartiesList(Set.of(CaseRole.APP_SOLICITOR));
        Hearing hearing = mock(Hearing.class);
        when(hearing.getPartiesOnCase()).thenReturn(partyList);
        FinremCallbackRequest callbackRequest = callbackRequest();
        NotificationRequest notificationRequest = new NotificationRequest();
        when(notificationRequestMapper.buildHearingNotificationForApplicantSolicitor(callbackRequest.getCaseDetails(),
                hearing)).thenReturn(notificationRequest);

        // Arrange
        when(hearingCorrespondenceHelper.getHearingInContext(callbackRequest.getCaseDetails().getData())).thenReturn(hearing);
        when(hearingCorrespondenceHelper.shouldNotSendNotification(hearing)).thenReturn(false);
        when(hearingCorrespondenceHelper.shouldEmailToApplicantSolicitor(callbackRequest.getCaseDetails())).thenReturn(true);

        // act
        corresponder.sendHearingCorrespondence(callbackRequest, AUTH_TOKEN);

        // Verify
        verify(notificationService).sendHearingNotificationToSolicitor(notificationRequest, CaseRole.APP_SOLICITOR.toString());
        verify(hearingCorrespondenceHelper, never()).shouldPostHearingNoticeOnly(any(), any());
        verify(hearingCorrespondenceHelper, never()).shouldPostAllHearingDocuments(any(), any());
        verify(bulkPrintService, never()).printApplicantDocuments((FinremCaseDetails) any(), any(), any());
    }

    /**
     * Checks that sendHearingNotificationToParty is called with the right role.
     * - CaseRole is RESP_SOLICITOR
     * - shouldNotSendNotification returns false
     * - shouldEmailToRespondentSolicitor returns true
     */
    @Test
    void shouldSendHearingNotificationsToRespondent() {
        // Setup
        List<PartyOnCaseCollectionItem> partyList = buildPartiesList(Set.of(CaseRole.RESP_SOLICITOR));
        Hearing hearing = mock(Hearing.class);
        when(hearing.getPartiesOnCase()).thenReturn(partyList);
        FinremCallbackRequest callbackRequest = callbackRequest();
        NotificationRequest notificationRequest = new NotificationRequest();
        when(notificationRequestMapper.buildHearingNotificationForRespondentSolicitor(callbackRequest.getCaseDetails(),
                hearing)).thenReturn(notificationRequest);

        // Arrange
        when(hearingCorrespondenceHelper.getHearingInContext(callbackRequest.getCaseDetails().getData())).thenReturn(hearing);
        when(hearingCorrespondenceHelper.shouldNotSendNotification(hearing)).thenReturn(false);
        when(hearingCorrespondenceHelper.shouldEmailToRespondentSolicitor(callbackRequest.getCaseDetails())).thenReturn(true);

        // act
        corresponder.sendHearingCorrespondence(callbackRequest, AUTH_TOKEN);

        // Verify
        verify(notificationService).sendHearingNotificationToSolicitor(notificationRequest, CaseRole.RESP_SOLICITOR.toString());
        verify(hearingCorrespondenceHelper, never()).shouldPostHearingNoticeOnly(any(), any());
        verify(hearingCorrespondenceHelper, never()).shouldPostAllHearingDocuments(any(), any());
        verify(bulkPrintService, never()).printRespondentDocuments((FinremCaseDetails) any(), any(), any());
    }

    /**
     * Checks that sendHearingNotificationToParty is called with the right Intervener role.
     */
    @Test
    void shouldSendHearingNotificationsToInterveners() {
        // Setup
        Set<CaseRole> caseRoles = Set.of(
            CaseRole.INTVR_SOLICITOR_1,
            CaseRole.INTVR_SOLICITOR_2,
            CaseRole.INTVR_SOLICITOR_3,
            CaseRole.INTVR_SOLICITOR_4
        );
        List<PartyOnCaseCollectionItem> partyList = buildPartiesList(caseRoles);

        FinremCallbackRequest callbackRequest = callbackRequest();
        FinremCaseData finremCaseData = callbackRequest.getCaseDetails().getData();
        YesOrNo makeRepresented = YesOrNo.YES;
        addIntervenersToCaseData(finremCaseData, makeRepresented);

        Hearing hearing = mock(Hearing.class);
        NotificationRequest notificationRequest = new NotificationRequest();

        // Arrange
        for (int i = 0; i < 4; i++) {
            when(notificationRequestMapper.buildHearingNotificationForIntervenerSolicitor(
                callbackRequest.getCaseDetails(), hearing, finremCaseData.getInterveners().get(i)))
                .thenReturn(notificationRequest);
        }
        when(hearing.getPartiesOnCase()).thenReturn(partyList);
        when(hearingCorrespondenceHelper.getHearingInContext(finremCaseData)).thenReturn(hearing);
        when(hearingCorrespondenceHelper.shouldNotSendNotification(hearing)).thenReturn(false);

        // Act
        corresponder.sendHearingCorrespondence(callbackRequest, AUTH_TOKEN);

        // Verify
        for (CaseRole role : caseRoles) {
            verify(notificationService).sendHearingNotificationToSolicitor(notificationRequest, role.toString());
        }
        verify(hearingCorrespondenceHelper, never()).shouldPostHearingNoticeOnly(any(), any());
        verify(hearingCorrespondenceHelper, never()).shouldPostAllHearingDocuments(any(), any());
        verify(bulkPrintService, never()).printIntervenerDocuments(any(), (FinremCaseDetails) any(), any(), any());
    }

    /**
     *  sendHearingCorrespondence needs a null check for each IntervenerWrapper.
     *  There if an opportunity to check and log if intervener addresses are missing.
     *  This checks that the logs are present.  And that the null checks are working.
     */
    @Test
    void shouldSendLogMissingIntervenerAddresses() {
        // Setup
        Set<CaseRole> caseRoles = Set.of(
            CaseRole.INTVR_SOLICITOR_1,
            CaseRole.INTVR_SOLICITOR_2,
            CaseRole.INTVR_SOLICITOR_3,
            CaseRole.INTVR_SOLICITOR_4
        );
        List<PartyOnCaseCollectionItem> partyList = buildPartiesList(caseRoles);

        FinremCallbackRequest callbackRequest = callbackRequest();
        FinremCaseData finremCaseData = callbackRequest.getCaseDetails().getData();
        Hearing hearing = mock(Hearing.class);

        // Arrange
        when(hearing.getPartiesOnCase()).thenReturn(partyList);
        when(hearingCorrespondenceHelper.getHearingInContext(finremCaseData)).thenReturn(hearing);
        when(hearingCorrespondenceHelper.shouldNotSendNotification(hearing)).thenReturn(false);

        // Act
        corresponder.sendHearingCorrespondence(callbackRequest, AUTH_TOKEN);

        // Verify
        assertThat(logs.getWarns()).contains(
            "Intervener One has no addresses for case ID: 123. Hearing correspondence not processed.",
            "Intervener Two has no addresses for case ID: 123. Hearing correspondence not processed.",
            "Intervener Three has no addresses for case ID: 123. Hearing correspondence not processed.",
            "Intervener Four has no addresses for case ID: 123. Hearing correspondence not processed."
        );
    }

    /**
     * Checks that sendHearingNotificationToApplicant is called when.
     * - CaseRole is APP_SOLICITOR
     * - shouldNotSendNotification returns false
     * - shouldPostToApplicant returns true
     * - shouldPostHearingNoticeOnly returns true
     */
    @Test
    void shouldPostHearingNoticeToApplicant() {
        // Setup
        List<PartyOnCaseCollectionItem> partyList = buildPartiesList(Set.of(CaseRole.APP_SOLICITOR));
        Hearing hearing = mock(Hearing.class);
        when(hearing.getPartiesOnCase()).thenReturn(partyList);
        FinremCallbackRequest callbackRequest = callbackRequest();
        when(manageHearingsDocumentService.getHearingNotice(callbackRequest.getCaseDetails())).thenReturn(new CaseDocument());

        // Arrange
        when(hearingCorrespondenceHelper.getHearingInContext(callbackRequest.getCaseDetails().getData())).thenReturn(hearing);
        when(hearingCorrespondenceHelper.shouldNotSendNotification(hearing)).thenReturn(false);
        when(hearingCorrespondenceHelper.shouldPostToApplicant(callbackRequest.getCaseDetails())).thenReturn(true);
        when(hearingCorrespondenceHelper.shouldPostHearingNoticeOnly(callbackRequest.getCaseDetails(), hearing)).thenReturn(true);
        when(manageHearingsDocumentService.getAdditionalHearingDocsFromWorkingHearing(
            callbackRequest.getCaseDetails().getData().getManageHearingsWrapper())).thenReturn(List.of(new CaseDocument()));

        // act
        corresponder.sendHearingCorrespondence(callbackRequest, AUTH_TOKEN);

        // Verify
        verify(notificationService, never()).sendHearingNotificationToSolicitor(any(), any());
        verify(bulkPrintService).printApplicantDocuments((FinremCaseDetails) any(), any(), any());
        verify(genericDocumentService, times(2)).convertDocumentIfNotPdfAlready(any(), any(), any());
        assertThat(logs.getInfos()).contains("Request sent to Bulk Print to post notice to the APP_SOLICITOR party. Request sent for case ID: 123");
    }

    /**
     * Checks that sendHearingNotificationToRespondent is called when.
     * - CaseRole is RESP_SOLICITOR
     * - shouldNotSendNotification returns false
     * - shouldPostToRespondent returns true
     * - shouldPostHearingNoticeOnly returns true
     */
    @Test
    void shouldPostHearingNoticeToRespondent() {
        // Setup
        List<PartyOnCaseCollectionItem> partyList = buildPartiesList(Set.of(CaseRole.RESP_SOLICITOR));
        Hearing hearing = mock(Hearing.class);
        when(hearing.getPartiesOnCase()).thenReturn(partyList);
        FinremCallbackRequest callbackRequest = callbackRequest();
        when(manageHearingsDocumentService.getHearingNotice(callbackRequest.getCaseDetails())).thenReturn(new CaseDocument());

        // Arrange
        when(hearingCorrespondenceHelper.getHearingInContext(callbackRequest.getCaseDetails().getData())).thenReturn(hearing);
        when(hearingCorrespondenceHelper.shouldNotSendNotification(hearing)).thenReturn(false);
        when(hearingCorrespondenceHelper.shouldPostToRespondent(callbackRequest.getCaseDetails())).thenReturn(true);
        when(hearingCorrespondenceHelper.shouldPostHearingNoticeOnly(callbackRequest.getCaseDetails(), hearing)).thenReturn(true);
        when(manageHearingsDocumentService.getAdditionalHearingDocsFromWorkingHearing(
            callbackRequest.getCaseDetails().getData().getManageHearingsWrapper())).thenReturn(List.of(new CaseDocument()));

        // act
        corresponder.sendHearingCorrespondence(callbackRequest, AUTH_TOKEN);

        // Verify
        verify(notificationService, never()).sendHearingNotificationToSolicitor(any(), any());
        verify(genericDocumentService, times(2)).convertDocumentIfNotPdfAlready(any(), any(), any());
        verify(bulkPrintService).printRespondentDocuments((FinremCaseDetails) any(), any(), any());
        assertThat(logs.getInfos()).contains("Request sent to Bulk Print to post notice to the RESP_SOLICITOR party. Request sent for case ID: 123");
    }

    /**
     * Checks that sendHearingNotificationToIntervener is called when.
     * - CaseRole is an Intervener role
     * - shouldNotSendNotification returns false
     * - shouldPostHearingNoticeOnly returns true
     */
    @Test
    void shouldPostHearingNoticeToIntervener() {
        // Setup
        Set<CaseRole> caseRoles = Set.of(
            CaseRole.INTVR_SOLICITOR_1,
            CaseRole.INTVR_SOLICITOR_2,
            CaseRole.INTVR_SOLICITOR_3,
            CaseRole.INTVR_SOLICITOR_4
        );
        List<PartyOnCaseCollectionItem> partyList = buildPartiesList(caseRoles);
        FinremCallbackRequest callbackRequest = callbackRequest();
        FinremCaseData finremCaseData = callbackRequest.getCaseDetails().getData();
        YesOrNo doNotMakeRepresented = YesOrNo.NO;
        addIntervenersToCaseData(finremCaseData, doNotMakeRepresented);
        Hearing hearing = mock(Hearing.class);

        // Arrange
        when(hearing.getPartiesOnCase()).thenReturn(partyList);
        when(manageHearingsDocumentService.getHearingNotice(callbackRequest.getCaseDetails())).thenReturn(new CaseDocument());
        when(hearingCorrespondenceHelper.getHearingInContext(callbackRequest.getCaseDetails().getData())).thenReturn(hearing);
        when(hearingCorrespondenceHelper.shouldNotSendNotification(hearing)).thenReturn(false);
        when(hearingCorrespondenceHelper.shouldPostHearingNoticeOnly(callbackRequest.getCaseDetails(), hearing)).thenReturn(true);
        when(manageHearingsDocumentService.getAdditionalHearingDocsFromWorkingHearing(
            callbackRequest.getCaseDetails().getData().getManageHearingsWrapper())).thenReturn(List.of(new CaseDocument()));

        // act
        corresponder.sendHearingCorrespondence(callbackRequest, AUTH_TOKEN);

        // Verify
        verify(notificationService, never()).sendHearingNotificationToSolicitor(any(), any());
        verify(genericDocumentService, times(8)).convertDocumentIfNotPdfAlready(any(), any(), any());
        verify(bulkPrintService, times(4)).printIntervenerDocuments(any(), any(FinremCaseDetails.class), any(), any());

        caseRoles.forEach(role -> {
            assertThat(logs.getInfos()).contains(
                "Request sent to Bulk Print to post notice to the " + role + " party. Request sent for case ID: 123");
        });
    }

    /**
     * Checks that sendHearingCorrespondence recognises that hearing documents should be posted to the applicant
     * - shouldNotSendNotification returns false
     * - shouldPostToApplicant returns true
     * - shouldPostHearingNoticeOnly returns false
     * - shouldPostAllHearingDocuments returns true
     * Check Bulk Print was called.
     * Confirms that the correct log message is generated.
     */
    @Test
    void shouldPostHearingDocumentsToApplicant() {
        // Setup
        List<PartyOnCaseCollectionItem> partyList = buildPartiesList(Set.of(CaseRole.APP_SOLICITOR));
        Hearing hearing = mock(Hearing.class);
        when(hearing.getPartiesOnCase()).thenReturn(partyList);
        FinremCallbackRequest callbackRequest = callbackRequest();
        when(manageHearingsDocumentService.getHearingDocumentsToPost(callbackRequest.getCaseDetails())).thenReturn(List.of(new CaseDocument()));

        // Arrange
        when(hearingCorrespondenceHelper.getHearingInContext(callbackRequest.getCaseDetails().getData())).thenReturn(hearing);
        when(hearingCorrespondenceHelper.shouldNotSendNotification(hearing)).thenReturn(false);
        when(hearingCorrespondenceHelper.shouldPostToApplicant(callbackRequest.getCaseDetails())).thenReturn(true);
        when(hearingCorrespondenceHelper.shouldPostHearingNoticeOnly(callbackRequest.getCaseDetails(), hearing)).thenReturn(false);
        when(hearingCorrespondenceHelper.shouldPostAllHearingDocuments(callbackRequest.getCaseDetails(), hearing)).thenReturn(true);
        when(manageHearingsDocumentService.getAdditionalHearingDocsFromWorkingHearing(
            callbackRequest.getCaseDetails().getData().getManageHearingsWrapper())).thenReturn(List.of(new CaseDocument()));

        // act
        corresponder.sendHearingCorrespondence(callbackRequest, AUTH_TOKEN);

        // Verify
        verify(notificationService, never()).sendHearingNotificationToSolicitor(any(), any());
        verify(genericDocumentService, times(2)).convertDocumentIfNotPdfAlready(any(), any(), any());
        verify(bulkPrintService).printApplicantDocuments((FinremCaseDetails) any(), any(), any());
        assertThat(logs.getInfos())
            .contains("Request sent to Bulk Print to post hearing documents to the APP_SOLICITOR party. Request sent for case ID: 123");
    }

    /**
     * Checks that sendHearingCorrespondence recognises that hearing documents should be posted to the respondent
     * - shouldNotSendNotification returns false
     * - shouldPostToRespondent returns true
     * - shouldPostHearingNoticeOnly returns false
     * - shouldPostAllHearingDocuments returns true
     * Check Bulk Print was called.
     * Confirms that the correct log message is generated.
     */
    @Test
    void shouldPostHearingDocumentsToRespondent() {
        // Setup
        List<PartyOnCaseCollectionItem> partyList = buildPartiesList(Set.of(CaseRole.RESP_SOLICITOR));
        Hearing hearing = mock(Hearing.class);
        when(hearing.getPartiesOnCase()).thenReturn(partyList);
        FinremCallbackRequest callbackRequest = callbackRequest();

        // Arrange
        when(hearingCorrespondenceHelper.getHearingInContext(callbackRequest.getCaseDetails().getData())).thenReturn(hearing);
        when(hearingCorrespondenceHelper.shouldNotSendNotification(hearing)).thenReturn(false);
        when(hearingCorrespondenceHelper.shouldPostToRespondent(callbackRequest.getCaseDetails())).thenReturn(true);
        when(hearingCorrespondenceHelper.shouldPostHearingNoticeOnly(callbackRequest.getCaseDetails(), hearing)).thenReturn(false);
        when(hearingCorrespondenceHelper.shouldPostAllHearingDocuments(callbackRequest.getCaseDetails(), hearing)).thenReturn(true);
        when(manageHearingsDocumentService.getHearingDocumentsToPost(callbackRequest.getCaseDetails())).thenReturn(List.of(new CaseDocument()));
        when(manageHearingsDocumentService.getAdditionalHearingDocsFromWorkingHearing(
            callbackRequest.getCaseDetails().getData().getManageHearingsWrapper())).thenReturn(List.of(new CaseDocument()));

        // act
        corresponder.sendHearingCorrespondence(callbackRequest, AUTH_TOKEN);

        // Verify
        verify(notificationService, never()).sendHearingNotificationToSolicitor(any(), any());
        verify(genericDocumentService, times(2)).convertDocumentIfNotPdfAlready(any(), any(), any());
        verify(bulkPrintService).printRespondentDocuments((FinremCaseDetails) any(), any(), any());
        assertThat(logs.getInfos())
            .contains("Request sent to Bulk Print to post hearing documents to the RESP_SOLICITOR party. Request sent for case ID: 123");
    }

    /**
     * Checks that sendHearingCorrespondence recognises that hearing documents should be posted to interveners
     * - shouldNotSendNotification returns false
     * - shouldPostHearingNoticeOnly returns false
     * - shouldPostAllHearingDocuments returns true
     * Check Bulk Print was called.
     * Confirms that the correct log message is generated.
     */
    @Test
    void shouldPostHearingDocumentsToInterveners() {
        // Setup
        Set<CaseRole> caseRoles = Set.of(
            CaseRole.INTVR_SOLICITOR_1,
            CaseRole.INTVR_SOLICITOR_2,
            CaseRole.INTVR_SOLICITOR_3,
            CaseRole.INTVR_SOLICITOR_4
        );
        List<PartyOnCaseCollectionItem> partyList = buildPartiesList(caseRoles);
        FinremCallbackRequest callbackRequest = callbackRequest();
        FinremCaseData finremCaseData = callbackRequest.getCaseDetails().getData();
        YesOrNo doNotMakeRepresented = YesOrNo.NO;
        addIntervenersToCaseData(finremCaseData, doNotMakeRepresented);
        Hearing hearing = mock(Hearing.class);
        when(hearing.getPartiesOnCase()).thenReturn(partyList);

        // Arrange
        when(hearingCorrespondenceHelper.getHearingInContext(callbackRequest.getCaseDetails().getData())).thenReturn(hearing);
        when(hearingCorrespondenceHelper.shouldNotSendNotification(hearing)).thenReturn(false);
        when(hearingCorrespondenceHelper.shouldPostHearingNoticeOnly(callbackRequest.getCaseDetails(), hearing)).thenReturn(false);
        when(hearingCorrespondenceHelper.shouldPostAllHearingDocuments(callbackRequest.getCaseDetails(), hearing)).thenReturn(true);
        when(manageHearingsDocumentService.getHearingDocumentsToPost(callbackRequest.getCaseDetails())).thenReturn(List.of(new CaseDocument()));
        when(manageHearingsDocumentService.getAdditionalHearingDocsFromWorkingHearing(
            callbackRequest.getCaseDetails().getData().getManageHearingsWrapper())).thenReturn(List.of(new CaseDocument()));

        // act
        corresponder.sendHearingCorrespondence(callbackRequest, AUTH_TOKEN);

        // Verify
        verify(notificationService, never()).sendHearingNotificationToSolicitor(any(), any());
        verify(genericDocumentService, times(8)).convertDocumentIfNotPdfAlready(any(), any(), any());
        verify(bulkPrintService, times(4)).printIntervenerDocuments(any(), (FinremCaseDetails) any(), any(), any());

        caseRoles.forEach(role -> {
            assertThat(logs.getInfos()).contains(
                "Request sent to Bulk Print to post hearing documents to the " + role + " party. Request sent for case ID: 123");
        });
    }

    /**
     * Checks that postHearingNoticeOnly handles a missing notice:
     * - CaseRole is APP_SOLICITOR
     * - shouldNotSendNotification returns false
     * - postingToApplicant returns true
     * - shouldSendHearingNoticeOnly returns true
     * - Hearing is intentionally missing the notice document.
     */
    @Test
    void sendPaperNoticeToApplicantShouldHandleMissingNotice() {
        // Setup
        List<PartyOnCaseCollectionItem> partyList = buildPartiesList(Set.of(CaseRole.APP_SOLICITOR));
        Hearing hearing = mock(Hearing.class);
        when(hearing.getPartiesOnCase()).thenReturn(partyList);
        FinremCallbackRequest callbackRequest = callbackRequest();
        when(manageHearingsDocumentService.getHearingNotice(callbackRequest.getCaseDetails())).thenReturn(null);

        // Arrange
        when(hearingCorrespondenceHelper.getHearingInContext(callbackRequest.getCaseDetails().getData())).thenReturn(hearing);
        when(hearingCorrespondenceHelper.shouldNotSendNotification(hearing)).thenReturn(false);
        when(hearingCorrespondenceHelper.shouldPostToApplicant(callbackRequest.getCaseDetails())).thenReturn(true);
        when(hearingCorrespondenceHelper.shouldPostHearingNoticeOnly(callbackRequest.getCaseDetails(), hearing)).thenReturn(true);

        // act
        corresponder.sendHearingCorrespondence(callbackRequest, AUTH_TOKEN);

        // Verify
        verify(notificationService, never()).sendHearingNotificationToSolicitor(any(), any());
        verify(genericDocumentService, never()).convertDocumentIfNotPdfAlready(any(), any(), any());
        assertThat(logs.getWarns()).contains("Hearing notice is null. No document sent to APP_SOLICITOR for case ID: 123");
    }

    /**
     * Checks that missing hearing documents are handled.
     */
    @Test
    void sendPaperNoticeToPartiesShouldHandleMissingHearingDocs() {
        // Setup
        Set<CaseRole> caseRoles = Set.of(
            CaseRole.APP_SOLICITOR,
            CaseRole.RESP_SOLICITOR
        );
        List<PartyOnCaseCollectionItem> partyList = buildPartiesList(caseRoles);
        Hearing hearing = mock(Hearing.class);
        when(hearing.getPartiesOnCase()).thenReturn(partyList);
        FinremCallbackRequest callbackRequest = callbackRequest();

        // Arrange
        when(hearingCorrespondenceHelper.getHearingInContext(callbackRequest.getCaseDetails().getData())).thenReturn(hearing);
        when(hearingCorrespondenceHelper.shouldNotSendNotification(hearing)).thenReturn(false);
        when(hearingCorrespondenceHelper.shouldPostToApplicant(callbackRequest.getCaseDetails())).thenReturn(true);
        when(hearingCorrespondenceHelper.shouldPostToRespondent(callbackRequest.getCaseDetails())).thenReturn(true);
        when(hearingCorrespondenceHelper.shouldPostHearingNoticeOnly(callbackRequest.getCaseDetails(), hearing)).thenReturn(false);
        when(hearingCorrespondenceHelper.shouldPostHearingNoticeOnly(callbackRequest.getCaseDetails(), hearing)).thenReturn(false);
        when(hearingCorrespondenceHelper.shouldPostAllHearingDocuments(callbackRequest.getCaseDetails(), hearing)).thenReturn(true);

        // act
        corresponder.sendHearingCorrespondence(callbackRequest, AUTH_TOKEN);

        // Verify
        verify(notificationService, never()).sendHearingNotificationToSolicitor(any(), any());
        verify(genericDocumentService, never()).convertDocumentIfNotPdfAlready(any(), any(), any());
        assertThat(Collections.frequency(
            logs.getWarns(),
            "No hearing documents found. No documents sent for case ID: 123")
        ).isEqualTo(2);
    }

    /**
     * Checks that postHearingNoticeOnly handles a missing notice:
     * - CaseRole is RESP_SOLICITOR
     * - shouldNotSendNotification returns false
     * - postingToApplicant returns true
     * - shouldSendHearingNoticeOnly returns true
     * - Hearing is intentionally missing the notice document.
     */
    @Test
    void sendPaperNoticeToRespondentShouldHandleMissingNotice() {
        // Setup
        List<PartyOnCaseCollectionItem> partyList = buildPartiesList(Set.of(CaseRole.RESP_SOLICITOR));
        Hearing hearing = mock(Hearing.class);
        when(hearing.getPartiesOnCase()).thenReturn(partyList);
        FinremCallbackRequest callbackRequest = callbackRequest();
        when(manageHearingsDocumentService.getHearingNotice(callbackRequest.getCaseDetails())).thenReturn(null);

        // Arrange
        when(hearingCorrespondenceHelper.getHearingInContext(callbackRequest.getCaseDetails().getData())).thenReturn(hearing);
        when(hearingCorrespondenceHelper.shouldNotSendNotification(hearing)).thenReturn(false);
        when(hearingCorrespondenceHelper.shouldPostToRespondent(callbackRequest.getCaseDetails())).thenReturn(true);
        when(hearingCorrespondenceHelper.shouldPostHearingNoticeOnly(callbackRequest.getCaseDetails(), hearing)).thenReturn(true);

        // act
        corresponder.sendHearingCorrespondence(callbackRequest, AUTH_TOKEN);

        // Verify
        verify(notificationService, never()).sendHearingNotificationToSolicitor(any(), any());
        verify(genericDocumentService, never()).convertDocumentIfNotPdfAlready(any(), any(), any());
        assertThat(logs.getWarns()).contains("Hearing notice is null. No document sent to RESP_SOLICITOR for case ID: 123");
    }

    /**
     * Checks that postHearingNoticeOnly handles a missing notice:
     * - CaseRole is RESP_SOLICITOR
     * - shouldNotSendNotification returns false
     * - postingToApplicant returns true
     * - shouldSendHearingNoticeOnly returns true
     * - Hearing is intentionally missing the notice document.
     */
    @Test
    void sendPaperNoticeToIntervenersShouldHandleMissingNotice() {
        // Setup
        Set<CaseRole> caseRoles = Set.of(
            CaseRole.INTVR_SOLICITOR_1,
            CaseRole.INTVR_SOLICITOR_2,
            CaseRole.INTVR_SOLICITOR_3,
            CaseRole.INTVR_SOLICITOR_4
        );
        List<PartyOnCaseCollectionItem> partyList = buildPartiesList(caseRoles);
        Hearing hearing = mock(Hearing.class);
        when(hearing.getPartiesOnCase()).thenReturn(partyList);
        FinremCallbackRequest callbackRequest = callbackRequest();
        FinremCaseData finremCaseData = callbackRequest.getCaseDetails().getData();
        YesOrNo makeUnrepresentedSoDocumentsPosted = YesOrNo.NO;
        addIntervenersToCaseData(finremCaseData, makeUnrepresentedSoDocumentsPosted);
        when(manageHearingsDocumentService.getHearingNotice(callbackRequest.getCaseDetails())).thenReturn(null);

        // Arrange
        when(hearingCorrespondenceHelper.getHearingInContext(callbackRequest.getCaseDetails().getData())).thenReturn(hearing);
        when(hearingCorrespondenceHelper.shouldNotSendNotification(hearing)).thenReturn(false);
        when(hearingCorrespondenceHelper.shouldPostHearingNoticeOnly(callbackRequest.getCaseDetails(), hearing)).thenReturn(true);

        // act
        corresponder.sendHearingCorrespondence(callbackRequest, AUTH_TOKEN);

        // Verify
        verify(notificationService, never()).sendHearingNotificationToSolicitor(any(), any());
        verify(genericDocumentService, never()).convertDocumentIfNotPdfAlready(any(), any(), any());
        caseRoles.forEach(
            role -> assertThat(logs.getWarns()).contains(
                "Hearing notice is null. No document sent to " + role + " for case ID: 123"
            )
        );
    }

    /**
     * Checks that sendHearingCorrespondence does not send any documents when no hearing documents are found.
     * - shouldNotSendNotification returns false
     * - postingToApplicant returns true
     * - shouldPostHearingNoticeOnly returns false
     * - shouldPostAllHearingDocuments returns true
     * Check bulk print was not called and a warning log is generated.
     */
    @Test
    void shouldLogWhenNoHearingDocumentsFoundToPost() {
        // Setup
        List<PartyOnCaseCollectionItem> partyList = buildPartiesList(Set.of(CaseRole.APP_SOLICITOR));
        Hearing hearing = mock(Hearing.class);
        when(hearing.getPartiesOnCase()).thenReturn(partyList);
        FinremCallbackRequest callbackRequest = callbackRequest();

        // Arrange
        when(manageHearingsDocumentService.getHearingDocumentsToPost(callbackRequest.getCaseDetails()))
            .thenReturn(List.of());

        when(hearingCorrespondenceHelper.getHearingInContext(callbackRequest.getCaseDetails().getData())).thenReturn(hearing);
        when(hearingCorrespondenceHelper.shouldNotSendNotification(hearing)).thenReturn(false);
        when(hearingCorrespondenceHelper.shouldPostToApplicant(callbackRequest.getCaseDetails())).thenReturn(true);
        when(hearingCorrespondenceHelper.shouldPostHearingNoticeOnly(callbackRequest.getCaseDetails(), hearing)).thenReturn(false);
        when(hearingCorrespondenceHelper.shouldPostAllHearingDocuments(callbackRequest.getCaseDetails(), hearing)).thenReturn(true);

        // Act
        corresponder.sendHearingCorrespondence(callbackRequest, AUTH_TOKEN);

        // Verify
        verify(notificationService, never()).sendHearingNotificationToSolicitor(any(), any());
        verify(genericDocumentService, never()).convertDocumentIfNotPdfAlready(any(), any(), any());
        verify(bulkPrintService, never()).printApplicantDocuments((FinremCaseDetails) any(), any(), any());
        verify(bulkPrintService, never()).printRespondentDocuments((FinremCaseDetails) any(), any(), any());

        assertThat(logs.getWarns())
            .contains("No hearing documents found. No documents sent for case ID: 123");
    }

    /**
     * Checks that sendHearingNotificationToApplicant throws IllegalStateException when illegal CaseRole is provided.
     * - CaseRole is CASEWORKER (which is not appropriate for hearing notifications)
     * - shouldNotSendNotification returns false
     */
    @Test
    void shouldThrowExceptionIfSendHearingNotificationsByPartyGivenUnexpectedCaseRole() {
        // Caseworker is not handled in the switch statement, so it should throw an exception

        // Setup
        List<PartyOnCaseCollectionItem> partyList = buildPartiesList(Set.of(CaseRole.CASEWORKER));
        Hearing hearing = mock(Hearing.class);
        when(hearing.getPartiesOnCase()).thenReturn(partyList);
        FinremCallbackRequest callbackRequest = callbackRequest();

        // Arrange
        when(hearingCorrespondenceHelper.getHearingInContext(callbackRequest.getCaseDetails().getData())).thenReturn(hearing);
        when(hearingCorrespondenceHelper.shouldNotSendNotification(hearing)).thenReturn(false);

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
     * Reusable test setup method to create a FinremCallbackRequest with a case ID and empty case data.
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
     * Reusable test setup method to build a list of PartyOnCaseCollection from a set of CaseRoles.
     */
    private List<PartyOnCaseCollectionItem> buildPartiesList(Set<CaseRole> caseRoles) {

        // build the list elements from the passed set of case roles
        List<PartyOnCaseCollectionItem> parties = new ArrayList<>();
        for (CaseRole role : caseRoles) {
            PartyOnCase party = PartyOnCase.builder()
                .role(role.getCcdCode())
                .label(role.name())
                .build();
            parties.add(PartyOnCaseCollectionItem.builder()
                .value(party)
                .build());
        }

        return parties;
    }

    /**
     * Just adds email and names needed for the test.
     *
     * @param finremCaseData the FinremCaseData object to which the interveners will be added
     */
    private void addIntervenersToCaseData(FinremCaseData finremCaseData, YesOrNo makeRepresented) {
        finremCaseData.setIntervenerOne(IntervenerOne.builder()
            .intervenerSolEmail("interver1@email.com")
            .intervenerSolName("Intervener Solicitor 1")
            .intervenerRepresented(makeRepresented)
            .build());
        finremCaseData.setIntervenerTwo(IntervenerTwo.builder()
            .intervenerSolEmail("interver2@email.com")
            .intervenerSolName("Intervener Solicitor 2")
            .intervenerRepresented(makeRepresented)
            .build());
        finremCaseData.setIntervenerThree(IntervenerThree.builder()
            .intervenerSolEmail("interver3@email.com")
            .intervenerSolName("Intervener Solicitor 3")
            .intervenerRepresented(makeRepresented)
            .build());
        finremCaseData.setIntervenerFour(IntervenerFour.builder()
            .intervenerSolEmail("interver4@email.com")
            .intervenerSolName("Intervener Solicitor 4")
            .intervenerRepresented(makeRepresented)
            .build());
    }
}
