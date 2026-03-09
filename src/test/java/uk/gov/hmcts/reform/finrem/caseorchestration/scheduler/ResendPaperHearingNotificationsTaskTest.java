package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.JavaTimeConversionPattern;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.managehearings.HearingCorrespondenceHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CfcCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Court;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Region;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionLondonFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.State;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingMode;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingDocumentsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsAction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.PartyOnCase;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.PartyOnCaseCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.hearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.hearings.ManageHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.hearings.VacateOrAdjournedHearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.hearings.VacatedOrAdjournedHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DefaultCourtListWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.ApplicantPartyListener;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogs;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReference;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReferenceCsvLoader;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.AMEND_CASE_CRON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.INTERVENER_FOUR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.INTERVENER_ONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.INTERVENER_THREE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.INTERVENER_TWO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.RESPONDENT;

@ExtendWith(MockitoExtension.class)
class ResendPaperHearingNotificationsTaskTest {

    private static final String AUTH_TOKEN = "testAuthToken";
    private static final String REFERENCE = "1234567890123456";
    private static final String HEARING_NOTICE_TEST_ID = "hearingNotice";
    private static final String VACATE_NOTICE_TEST_ID = "VacateHearingNotice";
    private static final String MINI_FORM_A_TEST_ID = "miniFormA";

    /*
     * For hearings, Service are manually processing hearings with dates up to 23:59 2nd March '26.
     * For vacated hearings,  Service manually processing vacated notices for hearings with dates up to 23:59 2nd March '26.
     * If the hearing dates, on these vacated hearings, are after this date and the hearing was vacated when the bug was
     * live (20th Jan to 3rd Feb), then we post vacate notices again.
     */
    private static final LocalDate hearing_date_for_hearings_in_scope = LocalDate.of(2026, 3, 3);
    private static final LocalDate hearing_date_for_vacated_hearings_in_scope = LocalDate.of(2026, 3, 3);
    private static final LocalDate vacated_date_for_vacated_hearings_in_scope = LocalDate.of(2026, 1, 20);

    @TestLogs
    private final TestLogger logs = new TestLogger(ResendPaperHearingNotificationsTask.class);

    @InjectMocks
    private ResendPaperHearingNotificationsTask resendTask;
    @Mock
    private CaseReferenceCsvLoader caseReferenceCsvLoader;
    @Mock
    private CcdService ccdService;
    @Mock
    private SystemUserService systemUserService;
    private final FinremCaseDetailsMapper finremCaseDetailsMapper = new FinremCaseDetailsMapper(
        new ObjectMapper().registerModule(new JavaTimeModule()));
    @Mock
    private NotificationService notificationService;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    @Mock
    private HearingCorrespondenceHelper hearingCorrespondenceHelper;
    @Mock
    private ApplicantPartyListener applicantPartyListener;

    @BeforeEach
    void setUp() {
        resendTask = new ResendPaperHearingNotificationsTask(
            caseReferenceCsvLoader, ccdService, systemUserService,
            finremCaseDetailsMapper, notificationService,
            applicationEventPublisher, hearingCorrespondenceHelper);

        ReflectionTestUtils.setField(resendTask, "taskEnabled", true);
        ReflectionTestUtils.setField(resendTask, "csvFile", "resendPaperHearingNotifications-encrypted.csv");
        ReflectionTestUtils.setField(resendTask, "secret", "DUMMY_SECRET");
        ReflectionTestUtils.setField(resendTask, "caseTypeId", CaseType.CONTESTED.getCcdType());
        ReflectionTestUtils.setField(resendTask, "dryRun", false);
    }

    @Test
    void givenTaskNotEnabled_whenTaskRun_thenNoResend() {
        ReflectionTestUtils.setField(resendTask, "taskEnabled", false);
        resendTask.run();

        verifyNoInteractions(ccdService);
        verifyNoInteractions(caseReferenceCsvLoader);
        verifyNoInteractions(systemUserService);
        verifyNoInteractions(notificationService);
        verifyNoInteractions(hearingCorrespondenceHelper);
        verifyNoInteractions(applicantPartyListener);
    }

    /*
     * Checks all parties, within NotificationParty; Applicant, Respondent and all four Interveners.
     * Runs test twice.  Once with mini form A and additional documents. Once without.
     */
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void givenHearingNoticeRequired_whenExecuteTask_andDateInScope_thenEventPublishedForPosting(boolean extraDocuments) {
        // Arrange
        UUID hearingId = UUID.randomUUID();
        UUID hearingItemId = UUID.randomUUID();
        ManageHearingsAction action = ManageHearingsAction.ADD_HEARING;

        List<PartyOnCaseCollectionItem> partiesOnCase = buildPartiesOnCase(APPLICANT, RESPONDENT, INTERVENER_ONE,
            INTERVENER_TWO, INTERVENER_THREE, INTERVENER_FOUR);

        CaseDocument miniFormA = extraDocuments ? buildCaseDocument(MINI_FORM_A_TEST_ID) : null;
        when(hearingCorrespondenceHelper.getMiniFormAIfRequired(any(), any()))
            .thenReturn(Optional.ofNullable(miniFormA));

        CaseDocument hearingNotice = buildCaseDocument(HEARING_NOTICE_TEST_ID);
        List<ManageHearingDocumentsCollectionItem> associatedHearingDocumentsCollection =
            buildAssociatedHearingDocumentCollection(hearingItemId, hearingNotice);

        CaseDocument additionalHearingDoc =
            extraDocuments ? buildCaseDocument("additionalHearingDoc") : null;

        List<DocumentCollectionItem> additionalDocumentCollection =
            extraDocuments
                ? buildAdditionalDocumentCollection(hearingItemId, additionalHearingDoc)
                : List.of();

        List<ManageHearingsCollectionItem> hearings = buildHearingsCollection(
            hearingItemId,
            hearing_date_for_hearings_in_scope,
            partiesOnCase,
            additionalDocumentCollection
            );

        List<VacatedOrAdjournedHearingsCollectionItem> vacatedHearings = List.of();

        FinremCaseData finremCaseData = buildFinremCaseData(
            hearingId,
            action,
            associatedHearingDocumentsCollection,
            hearings,
            vacatedHearings
        );

        FinremCaseDetails finremCaseDetails = buildCaseDetails(finremCaseData);
        CaseDetails caseDetails = finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails);

        orchestrateCronMocks(caseDetails);

        // Act
        resendTask.run();

        // Assert
        assertThat(logs.getInfos()).contains(
            "Case ID: " + REFERENCE + " resending correspondence for 1 active hearings and 0 vacated hearings");
        assertThat(logs.getInfos()).contains(
            "Case ID: " + REFERENCE + " Sending active hearing correspondence with hearing date "
                + hearing_date_for_hearings_in_scope
                + ", for parties: " + List.of(APPLICANT, RESPONDENT, INTERVENER_ONE, INTERVENER_TWO, INTERVENER_THREE,
                INTERVENER_FOUR));

        List<CaseDocument> expectedList = extraDocuments
            ? List.of(hearingNotice, miniFormA, additionalHearingDoc) : List.of(hearingNotice);

        ArgumentCaptor<SendCorrespondenceEvent> captor = ArgumentCaptor.forClass(SendCorrespondenceEvent.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue())
            .extracting(
                SendCorrespondenceEvent::getAuthToken,
                SendCorrespondenceEvent::getNotificationParties,
                SendCorrespondenceEvent::getDocumentsToPost,
                SendCorrespondenceEvent::getEmailNotificationRequest,
                SendCorrespondenceEvent::getEmailTemplate
            )
            .containsExactly(AUTH_TOKEN, List.of(APPLICANT, RESPONDENT, INTERVENER_ONE, INTERVENER_TWO,
                INTERVENER_THREE, INTERVENER_FOUR), expectedList, null, null);
    }

    // Checks all parties, within NotificationParty; Applicant, Respondent and all four Interveners.
    // The Hearing date for the vacated hearing must be hearing_date_for_vacated_hearings_in_scope.
    // 20th Jan 2026 is the earliest vacated date we consider for processing (bug went in).
    // 2nd Feb 2026 is the latest vacated date we consider for processing (bug fixed on the 3rd).
    @ParameterizedTest
    @ValueSource(strings = {"2026 1 20", "2026 2 2"})
    void givenVacateNoticeRequired_whenExecuteTask_andDateInScope_thenEventPublishedForPosting(
        @JavaTimeConversionPattern("uuuu M d") LocalDate boundaryVacatedDate) {
        // Arrange
        UUID hearingId = UUID.randomUUID();
        UUID hearingItemId = UUID.randomUUID();
        ManageHearingsAction action = ManageHearingsAction.ADJOURN_OR_VACATE_HEARING;

        List<PartyOnCaseCollectionItem> partiesOnCase = buildPartiesOnCase(APPLICANT, RESPONDENT, INTERVENER_ONE,
            INTERVENER_TWO, INTERVENER_THREE, INTERVENER_FOUR);

        CaseDocument vacateNotice = buildCaseDocument(VACATE_NOTICE_TEST_ID);

        List<ManageHearingDocumentsCollectionItem> associatedHearingDocumentsCollection =
            buildAssociatedHearingDocumentCollection(hearingItemId, vacateNotice);

        List<VacatedOrAdjournedHearingsCollectionItem> vacatedHearings =
            buildVacatedHearingsCollection(hearingItemId,
                boundaryVacatedDate,
                hearing_date_for_vacated_hearings_in_scope,
                partiesOnCase);

        List<ManageHearingsCollectionItem> hearings = List.of();

        FinremCaseData finremCaseData = buildFinremCaseData(
            hearingId,
            action,
            associatedHearingDocumentsCollection,
            hearings,
            vacatedHearings
        );

        FinremCaseDetails finremCaseDetails = buildCaseDetails(finremCaseData);

        CaseDetails caseDetails = finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails);

        orchestrateCronMocks(caseDetails);

        // Mock getting the Vacate Hearing Notice.
        when(hearingCorrespondenceHelper.getCaseDocumentByTypeAndHearingUuid(
            CaseDocumentType.VACATE_HEARING_NOTICE,
            finremCaseData.getManageHearingsWrapper(),
            hearingItemId))
            .thenReturn(buildCaseDocument(VACATE_NOTICE_TEST_ID));

        // Act
        resendTask.run();

        // Assert
        assertThat(logs.getInfos()).contains("Case ID: "
            + REFERENCE
            + " resending correspondence for 0 active hearings and 1 vacated hearings");
        assertThat(logs.getInfos()).contains("Case ID: "
            + REFERENCE
            + " Sending vacated hearing correspondence with Vacated date: "
            + boundaryVacatedDate
            + " and hearing date: "
            + hearing_date_for_vacated_hearings_in_scope + ", for parties: "
            + List.of(APPLICANT, RESPONDENT, INTERVENER_ONE, INTERVENER_TWO, INTERVENER_THREE, INTERVENER_FOUR));

        ArgumentCaptor<SendCorrespondenceEvent> captor = ArgumentCaptor.forClass(SendCorrespondenceEvent.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue())
            .extracting(
                SendCorrespondenceEvent::getAuthToken,
                SendCorrespondenceEvent::getNotificationParties,
                SendCorrespondenceEvent::getDocumentsToPost,
                SendCorrespondenceEvent::getEmailNotificationRequest,
                SendCorrespondenceEvent::getEmailTemplate
            )
            .containsExactly(AUTH_TOKEN, List.of(APPLICANT, RESPONDENT, INTERVENER_ONE, INTERVENER_TWO,
                INTERVENER_THREE, INTERVENER_FOUR), List.of(vacateNotice), null, null);
    }

    /*
     * Checks that when both hearing and vacate notices are in scope, both events are published for posting
     * As this test covers most logs, also checks that during a dry run, the logs indicate that.
     */
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void givenHearingAndVacatedNotices_whenExecuteTask_andDateInScope_thenBothEventsPublishedForPosting(boolean dryRun) {
        // Arrange
        ReflectionTestUtils.setField(resendTask, "dryRun", dryRun);
        UUID hearingId = UUID.randomUUID();
        UUID hearingItemId = UUID.randomUUID();
        ManageHearingsAction action = ManageHearingsAction.ADJOURN_OR_VACATE_HEARING;

        List<PartyOnCaseCollectionItem> partiesOnCase = buildPartiesOnCase(APPLICANT, RESPONDENT, INTERVENER_ONE, INTERVENER_TWO,
            INTERVENER_THREE, INTERVENER_FOUR);

        List<VacatedOrAdjournedHearingsCollectionItem> vacatedHearings =
            buildVacatedHearingsCollection(hearingItemId,
                vacated_date_for_vacated_hearings_in_scope,
                hearing_date_for_vacated_hearings_in_scope,
                partiesOnCase);

        List<DocumentCollectionItem> additionalDocumentCollection = List.of();

        List<ManageHearingsCollectionItem> hearings =
            buildHearingsCollection(hearingItemId,
                hearing_date_for_hearings_in_scope,
                partiesOnCase,
                additionalDocumentCollection
            );

        List<ManageHearingDocumentsCollectionItem> associatedHearingDocumentsCollection =
            buildAssociatedHearingDocumentCollection(hearingItemId, buildCaseDocument(HEARING_NOTICE_TEST_ID));

        FinremCaseData finremCaseData = buildFinremCaseData(
            hearingId,
            action,
            associatedHearingDocumentsCollection,
            hearings,
            vacatedHearings
        );

        FinremCaseDetails finremCaseDetails = buildCaseDetails(finremCaseData);
        CaseDetails caseDetails = finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails);

        orchestrateCronMocks(caseDetails);

        // Act
        resendTask.run();

        // Assert
        List<String> expectedLogs = getExpectedLogsWhenDryRunOrNot(dryRun);
        if (dryRun) {
            assertThat(logs.getTraces()).containsAll(expectedLogs);
            verifyNoInteractions(applicationEventPublisher);
        } else {
            assertThat(logs.getInfos()).containsAll(expectedLogs);
        }
    }

    private static List<String> getExpectedLogsWhenDryRunOrNot(boolean dryRun) {
        String dryRunPrefixIfNeeded = dryRun ? "[DRY RUN] " : "";
        return List.of(
            dryRunPrefixIfNeeded
                + "Case ID: "
                + REFERENCE
                + " resending correspondence for 1 active hearings and 1 vacated hearings",
            dryRunPrefixIfNeeded
                + "Case ID: "
                + REFERENCE
                + " Sending active hearing correspondence with hearing date "
                + hearing_date_for_hearings_in_scope
                + ", for parties: "
                + List.of(APPLICANT, RESPONDENT, INTERVENER_ONE, INTERVENER_TWO, INTERVENER_THREE, INTERVENER_FOUR),
            dryRunPrefixIfNeeded
                + "Case ID: "
                + REFERENCE
                + " Sending vacated hearing correspondence with Vacated date: "
                + vacated_date_for_vacated_hearings_in_scope
                + " and hearing date: "
                + hearing_date_for_vacated_hearings_in_scope
                + ", for parties: "
                + List.of(APPLICANT, RESPONDENT, INTERVENER_ONE, INTERVENER_TWO, INTERVENER_THREE, INTERVENER_FOUR));
    }

    /*
     * CsvSource contains dates that mean that the subject cases don't process and a warning is logged.
     *
     * serviceManualHearingNoticeCutOff is '2026 3 2'.
     * This means, for hearings, we should NOT process anything with a hearing date earlier than that.
     *
     * serviceManualVacatedNoticeCutOff is '2026 3 2'.
     * This means, for vacated hearings, we should NOT process anything with a hearing date earlier than that.
     *
     * bugIntroductionDate is '2026 1 20'.
     * This means, for vacated hearings, we should NOT process anything with a vacated date earlier than that.
     *
     * bugFixDate is '2026 2 3'.
     * This means, for vacated hearings, we should NOT process anything with a vacated date later than that.
     *
     * Asserts that under these conditions, the right warning is logged.
    */
    @ParameterizedTest
    @CsvSource(value = {
        "'2026 3 1', '2026 3 1', '2026 1 19'", // Hearing dates too early (service sending), and vacated date before bug
        "'2026 02 28', '2026 2 28', '2026 2 3'" // Hearing dates too early (service sending), and vacated date after bug
    })
    void givenNoCandidateHearings_whenIsUpdatedRequired_thenWarnLogged(
        @JavaTimeConversionPattern("uuuu M d") LocalDate hearingDateForHearingTooEarly,
        @JavaTimeConversionPattern("uuuu M d") LocalDate hearingDateForVacatedHearingTooEarly,
        @JavaTimeConversionPattern("uuuu M d") LocalDate vacatedDate) {

        // Arrange
        UUID hearingId = UUID.randomUUID();
        UUID hearingItemId = UUID.randomUUID();
        ManageHearingsAction action = ManageHearingsAction.ADJOURN_OR_VACATE_HEARING;

        List<PartyOnCaseCollectionItem> partiesOnCase = buildPartiesOnCase(APPLICANT, RESPONDENT, INTERVENER_ONE,
            INTERVENER_TWO, INTERVENER_THREE, INTERVENER_FOUR);

        List<VacatedOrAdjournedHearingsCollectionItem> vacatedHearings =
            buildVacatedHearingsCollection(hearingItemId,
                vacatedDate,
                hearingDateForVacatedHearingTooEarly,
                partiesOnCase);

        List<DocumentCollectionItem> additionalDocumentCollection = List.of();

        List<ManageHearingsCollectionItem> hearings =
            buildHearingsCollection(hearingItemId,
                hearingDateForHearingTooEarly,
                partiesOnCase,
                additionalDocumentCollection
            );

        List<ManageHearingDocumentsCollectionItem> associatedHearingDocumentsCollection = List.of();

        FinremCaseData finremCaseData = buildFinremCaseData(
            hearingId,
            action,
            associatedHearingDocumentsCollection,
            hearings,
            vacatedHearings
        );

        FinremCaseDetails finremCaseDetails = buildCaseDetails(finremCaseData);
        CaseDetails caseDetails = finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails);

        orchestrateCronMocks(caseDetails);

        // Act
        resendTask.run();

        // Assert that all the scenarios show the logs to indicate that the subjects are out of scope.
        assertThat(logs.getWarns()).contains("Case ID: "
            + REFERENCE
            + " does not have hearings or vacated hearings within the cut off to post");
    }

    // Checks that no hearing documents raises the correct error.
    @Test
    void givenNoHearingDocuments_whenExecuteTask_thenErrorLogged() {

        // Arrange
        UUID hearingId = UUID.randomUUID();
        UUID hearingItemId = UUID.randomUUID();
        ManageHearingsAction action = ManageHearingsAction.ADD_HEARING;

        List<PartyOnCaseCollectionItem> partiesOnCase = buildPartiesOnCase(APPLICANT, RESPONDENT, INTERVENER_ONE,
            INTERVENER_TWO, INTERVENER_THREE, INTERVENER_FOUR);
        List<DocumentCollectionItem> additionalDocumentCollection = List.of();

        List<ManageHearingsCollectionItem> hearings = buildHearingsCollection(
            hearingItemId,
            hearing_date_for_hearings_in_scope,
            partiesOnCase,
            additionalDocumentCollection
        );

        List<VacatedOrAdjournedHearingsCollectionItem> vacatedHearings = List.of();
        List<ManageHearingDocumentsCollectionItem> associatedHearingDocumentsCollection = List.of();

        FinremCaseData finremCaseData = buildFinremCaseData(
            hearingId,
            action,
            associatedHearingDocumentsCollection,
            hearings,
            vacatedHearings
        );

        FinremCaseDetails finremCaseDetails = buildCaseDetails(finremCaseData);
        CaseDetails caseDetails = finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails);

        orchestrateCronMocks(caseDetails);

        // Act
        resendTask.run();

        // Assert
        assertThat(logs.getErrors()).contains(
            "Case ID: " + REFERENCE + " with hearing date "
                + hearing_date_for_hearings_in_scope
                + ", for parties: "
                + List.of(APPLICANT, RESPONDENT, INTERVENER_ONE, INTERVENER_TWO, INTERVENER_THREE, INTERVENER_FOUR)
                + " contained no hearing documents.");
        verifyNoInteractions(hearingCorrespondenceHelper);
    }

    private void mockLoadCaseReferenceList() {
        CaseReference caseReference = new CaseReference();
        caseReference.setCaseReference(REFERENCE);
        when(caseReferenceCsvLoader.loadCaseReferenceList(
            "resendPaperHearingNotifications-encrypted.csv",
            "DUMMY_SECRET"))
            .thenReturn(List.of(caseReference));
    }

    private void mockStartEvent(CaseDetails caseDetails) {
        StartEventResponse startEventResponse = StartEventResponse.builder()
            .caseDetails(caseDetails)
            .build();

        lenient().when(ccdService.startEventForCaseWorker(AUTH_TOKEN, REFERENCE, CONTESTED.getCcdType(),
            AMEND_CASE_CRON.getCcdType())).thenReturn(startEventResponse);
    }

    private void mockSystemUserToken() {
        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
    }

    private void mockSearchCases(CaseDetails caseDetails) {
        SearchResult searchResult = SearchResult.builder()
            .cases(List.of(caseDetails))
            .total(1)
            .build();
        when(ccdService.getCaseByCaseId(REFERENCE, CaseType.CONTESTED, AUTH_TOKEN)).thenReturn(searchResult);
    }

    // Runs the common methods needed to mock a CRON job.
    private void orchestrateCronMocks(CaseDetails caseDetails) {
        mockLoadCaseReferenceList();
        mockSystemUserToken();
        mockSearchCases(caseDetails);
        mockStartEvent(caseDetails);
    }

    private FinremCaseDetails buildCaseDetails(FinremCaseData finremCaseData) {
        return FinremCaseDetails.builder()
            .data(finremCaseData)
            .id(Long.parseLong(REFERENCE))
            .caseType(CaseType.CONTESTED)
            .state(State.APPLICATION_ISSUED)
            .build();
    }

    private CaseDocument buildCaseDocument(String description) {
        return CaseDocument.builder()
            .documentFilename(description + " filename")
            .documentUrl(description + "url")
            .documentBinaryUrl(description + "binary")
            .build();
    }

    private List<ManageHearingDocumentsCollectionItem> buildAssociatedHearingDocumentCollection(
        UUID hearingItemId, CaseDocument caseDocument) {

        return List.of(
            ManageHearingDocumentsCollectionItem.builder()
                .value(ManageHearingDocument.builder()
                    .hearingId(hearingItemId)
                    .hearingDocument(caseDocument)
                    .build())
                .build()
        );
    }

    private List<DocumentCollectionItem> buildAdditionalDocumentCollection(
        UUID hearingItemId, CaseDocument caseDocument) {
        return List.of(
            DocumentCollectionItem.builder()
                .id(hearingItemId)
                .value(caseDocument)
                .build()
        );
    }

    private FinremCaseData buildFinremCaseData(UUID hearingId, ManageHearingsAction action,
                                               List<ManageHearingDocumentsCollectionItem> associatedHearingDocumentsCollection,
                                               List<ManageHearingsCollectionItem> hearings,
                                               List<VacatedOrAdjournedHearingsCollectionItem> vacatedHearings) {
        return FinremCaseData.builder()
            .ccdCaseType(CaseType.CONTESTED)
            .manageHearingsWrapper(ManageHearingsWrapper.builder()
                .manageHearingsActionSelection(action)
                .workingHearingId(hearingId)
                .hearings(hearings)
                .vacatedOrAdjournedHearings(vacatedHearings)
                .hearingDocumentsCollection(associatedHearingDocumentsCollection)
                .build())
            .miniFormA(null)
            .build();
    }

    private List<VacatedOrAdjournedHearingsCollectionItem> buildVacatedHearingsCollection(
        UUID hearingItemId, LocalDate vacatedOrAdjournedDate, LocalDate hearingDate, List<PartyOnCaseCollectionItem> partiesOnCase) {
        return List.of(
            VacatedOrAdjournedHearingsCollectionItem
                .builder()
                .id(hearingItemId)
                .value(VacateOrAdjournedHearing
                    .builder()
                    .vacatedOrAdjournedDate(vacatedOrAdjournedDate)
                    .hearingType(HearingType.APPEAL_HEARING)
                    .hearingDate(hearingDate)
                    .hearingTime("10:00 AM")
                    .hearingTimeEstimate("2 hours")
                    .hearingMode(HearingMode.IN_PERSON)
                    .additionalHearingInformation("Additional Info")
                    .hearingCourtSelection(Court.builder()
                        .region(Region.LONDON)
                        .londonList(RegionLondonFrc.LONDON)
                        .courtListWrapper(DefaultCourtListWrapper.builder()
                            .cfcCourtList(CfcCourt.BROMLEY_COUNTY_COURT_AND_FAMILY_COURT)
                            .build())
                        .build())
                    .partiesOnCase(partiesOnCase)
                    .additionalHearingDocs(null)
                    .build())
                .build()
        );
    }

    private List<ManageHearingsCollectionItem> buildHearingsCollection(
        UUID hearingItemId, LocalDate hearingDate, List<PartyOnCaseCollectionItem> partiesOnCase,
        List<DocumentCollectionItem> additionalDocumentCollection) {
        return List.of(
            ManageHearingsCollectionItem
                .builder()
                .id(hearingItemId)
                .value(Hearing
                    .builder()
                    .hearingType(HearingType.APPEAL_HEARING)
                    .hearingDate(hearingDate)
                    .hearingTime("10:00 AM")
                    .hearingTimeEstimate("2 hours")
                    .hearingMode(HearingMode.IN_PERSON)
                    .additionalHearingInformation("Additional Info")
                    .hearingCourtSelection(Court.builder()
                        .region(Region.LONDON)
                        .londonList(RegionLondonFrc.LONDON)
                        .courtListWrapper(DefaultCourtListWrapper.builder()
                            .cfcCourtList(CfcCourt.BROMLEY_COUNTY_COURT_AND_FAMILY_COURT)
                            .build())
                        .build())
                    .partiesOnCase(partiesOnCase)
                    .additionalHearingDocs(additionalDocumentCollection)
                    .build())
                .build()
        );
    }

    private List<PartyOnCaseCollectionItem> buildPartiesOnCase(NotificationParty... parties) {
        return Stream.of(parties)
            .map(p -> PartyOnCase.builder().role(p.getRole()).build())
            .map(poc -> PartyOnCaseCollectionItem.builder().value(poc).build())
            .toList();
    }
}
