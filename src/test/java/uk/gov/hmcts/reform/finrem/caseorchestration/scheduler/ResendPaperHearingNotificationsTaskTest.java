package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.AMEND_CASE_CRON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@ExtendWith(MockitoExtension.class)
class ResendPaperHearingNotificationsTaskTest {

    private static final String AUTH_TOKEN = "testAuthToken";
    private static final String REFERENCE = "1234567890123456";
    private static final String VACATE_NOTICE_TEST_ID = "VacateHearingNotice";

    // For hearings, Service manually processing hearings with dates up to 23:59 22nd Feb '26
    private static final LocalDate hearing_date_for_hearings_in_scope = LocalDate.of(2026, 2, 23);;
    private static final LocalDate bug_for_hearing_outside_scope_date = LocalDate.of(2026, 2, 22);
    // For vacated hearings,  Service manually processing vacated notices for hearings with dates up to 23:59 19th Feb '26
    // If the hearing dates are later than this, and the hearing was vacated when the bug was live (20th Jan to 3rd Feb),
    // then we post vacate notices again.
    private static final LocalDate hearing_date_for_vacated_hearings_in_scope = LocalDate.of(2026, 2, 20);
    private static final LocalDate vacated_date_for_vacated_hearings_in_scope = LocalDate.of(2026, 1, 20);
    private static final LocalDate hearing_date_vacated_hearings_outside_scope = LocalDate.of(2026, 2, 19);
    private static final LocalDate vacated_date_vacated_hearings_outside_scope = LocalDate.of(2026, 2, 19);

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

        // reflection hack to set the @Value fields if needed
        ReflectionTestUtils.setField(resendTask, "taskEnabled", true);
        ReflectionTestUtils.setField(resendTask, "csvFile", "resendPaperHearingNotifications-encrypted.csv");
        ReflectionTestUtils.setField(resendTask, "secret", "DUMMY_SECRET");
        ReflectionTestUtils.setField(resendTask, "caseTypeId", CaseType.CONTESTED.getCcdType());
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

    // Todo -
    // hearings only - write a new set of tests for vacated.
    // in scope only - write a new set of tests for out of scope.
    // applicant only, you could parameterise for each party. Write one for test for all parties,
    @Test
    void givenApplicantHearingNoticeRequired_whenExecuteTask_andDateInScope_thenEventPublishedForPosting() {

        // Arrange
        UUID hearingId = UUID.randomUUID();
        UUID hearingItemId = UUID.randomUUID();
        ManageHearingsAction action = ManageHearingsAction.ADD_HEARING;

        PartyOnCase applicantParty = PartyOnCase.builder()
            .role(NotificationParty.APPLICANT.getRole())
            .label("ApplicantParty").build();

        List<PartyOnCaseCollectionItem> partiesOnCase = List.of(
            PartyOnCaseCollectionItem.builder()
                .value(applicantParty)
                .build()
        );

        CaseDocument hearingNotice = buildCaseDocument("hearingNotice");

        List<ManageHearingDocumentsCollectionItem> associatedHearingDocumentsCollection =
            buildAssociatedHearingDocumentCollection(hearingItemId, hearingNotice);

        FinremCaseData finremCaseData = buildFinremCaseData(
            hearingId,
            hearingItemId,
            action,
            hearing_date_for_hearings_in_scope,
            partiesOnCase,
            associatedHearingDocumentsCollection
        );

        FinremCaseDetails finremCaseDetails = buildCaseDetails(finremCaseData);

        CaseDetails caseDetails = finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails);

        mockApplicantSolicitorDigital(false);
        orchestrateCronMocks(caseDetails);

        // Act
        resendTask.run();

        // Assert
        assertThat(logs.getInfos()).contains(
            "Case ID: 1234567890123456 resending correspondence for 1 active hearings and 0 vacated hearings");
        assertThat(logs.getInfos()).contains(
            "Case ID: 1234567890123456 Sending active hearing correspondence with hearing date "
                + hearing_date_for_hearings_in_scope
                + ", for parties: [APPLICANT]");

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
            .containsExactly(AUTH_TOKEN, List.of(NotificationParty.APPLICANT), List.of(hearingNotice), null, null);
    }

    // Todo - FIX THIS
    // hearings only - write a new set of tests for vacated.
    // in scope only - write a new set of tests for out of scope.
    // applicant only, you could parameterise for each party. Write one for test for all parties,
    @Test
    void givenApplicantVacateNoticeRequired_whenExecuteTask_andDateInScope_thenEventPublishedForPosting() {

        // Arrange
        UUID hearingId = UUID.randomUUID();
        UUID hearingItemId = UUID.randomUUID();
        ManageHearingsAction action = ManageHearingsAction.VACATE_HEARING;

        PartyOnCase applicantParty = PartyOnCase.builder()
            .role(NotificationParty.APPLICANT.getRole())
            .label("ApplicantParty").build();

        List<PartyOnCaseCollectionItem> partiesOnCase = List.of(
            PartyOnCaseCollectionItem.builder()
                .value(applicantParty)
                .build()
        );

        CaseDocument vacateNotice = buildCaseDocument(VACATE_NOTICE_TEST_ID);

        List<ManageHearingDocumentsCollectionItem> associatedHearingDocumentsCollection =
            buildAssociatedHearingDocumentCollection(hearingItemId, vacateNotice);

        FinremCaseData finremCaseData = buildFinremCaseDataVacated(
            hearingId,
            hearingItemId,
            action,
            vacated_date_for_vacated_hearings_in_scope,
            hearing_date_for_vacated_hearings_in_scope,
            partiesOnCase,
            associatedHearingDocumentsCollection
        );

        FinremCaseDetails finremCaseDetails = buildCaseDetails(finremCaseData);

        CaseDetails caseDetails = finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails);

        mockApplicantSolicitorDigital(false);
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
        assertThat(logs.getInfos()).contains("Case ID: 1234567890123456 resending correspondence for 0 active hearings and 1 vacated hearings");
        assertThat(logs.getInfos()).contains("Case ID: 1234567890123456 Sending vacated hearing correspondence with Vacated date: " + vacated_date_for_vacated_hearings_in_scope + " and hearing date: " + hearing_date_for_vacated_hearings_in_scope + ", for parties: [APPLICANT]");

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
            .containsExactly(AUTH_TOKEN, List.of(NotificationParty.APPLICANT), List.of(vacateNotice), null, null);
    }

    @Test
    void givenRespondentActiveHearingPostalRequired_whenExecuteTask_thenRespondentNotificationTriggered() {
       //TODO
    }

    @Test
    void givenIntervenerActiveHearingPostalRequired_whenExecuteTask_thenIntervenerNotificationTriggered() {
      // TODO, repeat for all four
    }


    // Test setup needs a list of hearings and vacated hearings
    // look at getFilteredHearings and populate lists with date range.
    // Check no hearings outside date range included.
    // Test log warn("Case ID: {} does not have hearings or vacated hearings within the cut off to post")
    // isApplicantPostalRequired, Test all parties for success and not, depending if post required
    // check miniforma
    // Seems like proof of test success is a log saying "Notification event received for party {} on case {}"
    // and ("Case ID: {} Sending active hearing correspondence with hearing date {}, for parties: {}" or "Case ID: {} Sending vacated hearing correspondence with Vacated date: {}. and hearing date: {}, for parties: {}"
    // Check "Case ID: {} resending correspondence for {} active hearings and {} vacated hearings"
    // Test that cover each of the is[party]PostalRequired methods, true and false
    // Check that additional documents added.
    // test isUpdate required - hopefully using log.
    // log.error("Case ID: {} with hearing date {}, for parties: {} contained no hearing documents.",
    // The vacated filters have extra dates that could mean a case is out of scope.  Consider.



    // Todo - below are copied and handy - consider adding to UTIL

    private void mockApplicantSolicitorDigital(boolean applicantSolicitorDigital) {
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any(FinremCaseDetails.class)))
            .thenReturn(applicantSolicitorDigital);
    }

    private void mockRespondentSolicitorDigital(boolean respondentSolicitorDigital) {
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(any(FinremCaseDetails.class)))
            .thenReturn(respondentSolicitorDigital);
    }

    private void mockLoadCaseReferenceList() {
        CaseReference caseReference = new CaseReference();
        caseReference.setCaseReference(REFERENCE);
        when(caseReferenceCsvLoader.loadCaseReferenceList("resendPaperHearingNotifications-encrypted.csv", "DUMMY_SECRET"))
            .thenReturn(List.of(caseReference));
    }

    private void mockStartEvent(CaseDetails caseDetails) {
        StartEventResponse startEventResponse = StartEventResponse.builder()
            .caseDetails(caseDetails)
            .build();

        when(ccdService.startEventForCaseWorker(AUTH_TOKEN, REFERENCE, CONTESTED.getCcdType(),
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

    private FinremCaseData buildFinremCaseData(UUID hearingId, UUID hearingItemId, ManageHearingsAction action,
                                               LocalDate hearingDate, List<PartyOnCaseCollectionItem> partiesOnCase,
                                               List<ManageHearingDocumentsCollectionItem> associatedHearingDocumentsCollection) {
        return FinremCaseData.builder()
            .ccdCaseType(CaseType.CONTESTED)
            .manageHearingsWrapper(ManageHearingsWrapper
                .builder()
                .manageHearingsActionSelection(action)
                .workingHearingId(hearingId)
                .hearings(List.of(
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
                            .additionalHearingDocs(null)
                            .build())
                        .build()
                ))
                .hearingDocumentsCollection(associatedHearingDocumentsCollection)
                .build())
            .miniFormA(null)
            .build();
    }

    // todo - consider one case data build method, passing hearing or vacated hearing collections.
    // differences are its a vacated or adj item, and date is different
    private FinremCaseData buildFinremCaseDataVacated(UUID hearingId, UUID hearingItemId, ManageHearingsAction action,
                                               LocalDate vacatedOrAdjournedDate, LocalDate hearingDate,
                                               List<PartyOnCaseCollectionItem> partiesOnCase,
                                               List<ManageHearingDocumentsCollectionItem> associatedHearingDocumentsCollection) {
        return FinremCaseData.builder()
            .ccdCaseType(CaseType.CONTESTED)
            .manageHearingsWrapper(ManageHearingsWrapper
                .builder()
                .manageHearingsActionSelection(action)
                .workingHearingId(hearingId)
                .vacatedOrAdjournedHearings(List.of(
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
                ))
                .hearingDocumentsCollection(associatedHearingDocumentsCollection)
                .build())
            .miniFormA(null)
            .build();
    }
}
