package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.DraftOrdersNotificationRequestMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Region;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionNorthWestFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.State;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrderDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrderDocumentReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrdersReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrdersReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.AllocatedRegionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DefaultCourtListWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.RegionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.DraftOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.AMEND_CASE_CRON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.LiverpoolCourt.LIVERPOOL_CIVIL_FAMILY_COURT;

class DraftOrderReviewOverdueSendNotificationTaskTest {

    private DraftOrderReviewOverdueSendNotificationTask underTest;
    private CcdService ccdService;
    private SystemUserService systemUserService;
    private DraftOrderService draftOrderService;
    private NotificationService notificationService;
    private DraftOrdersNotificationRequestMapper notificationRequestMapper;

    private final FinremCaseDetailsMapper finremCaseDetailsMapper = new FinremCaseDetailsMapper(
        new ObjectMapper().registerModule(new JavaTimeModule()));

    @BeforeEach
    void setup() {
        ccdService = mock(CcdService.class);
        systemUserService = mock(SystemUserService.class);
        draftOrderService = mock(DraftOrderService.class);
        notificationService = mock(NotificationService.class);
        notificationRequestMapper = mock(DraftOrdersNotificationRequestMapper.class);

        underTest = new DraftOrderReviewOverdueSendNotificationTask(ccdService, systemUserService,
            finremCaseDetailsMapper, notificationService, draftOrderService, notificationRequestMapper);
        underTest.setTaskEnabled(true);
    }

    @Test
    void givenTaskNotEnabledWhenRunThenDoesNothing() {
        underTest.setTaskEnabled(false);
        underTest.run();

        verifyNoInteractions(ccdService);
        verifyNoInteractions(systemUserService);
        verifyNoInteractions(draftOrderService);
        verifyNoInteractions(notificationService);
        verifyNoInteractions(notificationRequestMapper);
    }

    @Test
    void givenNoCasesNeedUpdatingWhenRunThenNoUpdatesExecuted() {
        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);

        SearchResult searchResult = createSearchResult(Collections.emptyList());
        when(ccdService.esSearchCases(any(CaseType.class), anyString(), anyString())).thenReturn(searchResult);

        underTest.run();

        verify(ccdService, times(1)).esSearchCases(any(CaseType.class), anyString(), anyString());
        verifyNoMoreInteractions(ccdService);
        verifyNoInteractions(draftOrderService);
        verifyNoInteractions(notificationService);
        verifyNoInteractions(notificationRequestMapper);
    }

    @Test
    void givenSearchResultIsNotOverdueWhenRunThenNoUpdatesExecuted() {
        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);

        SearchResult searchResult = createSearchResult(List.of(createCaseData("1", LocalDate.of(2024, 11, 1))));
        when(ccdService.esSearchCases(any(CaseType.class), anyString(), anyString())).thenReturn(searchResult);

        underTest.run();

        verify(ccdService, times(1)).esSearchCases(any(CaseType.class), anyString(), anyString());
        verifyNoMoreInteractions(ccdService);
        verifyNoInteractions(notificationService);
        verifyNoInteractions(notificationRequestMapper);
    }

    @Test
    void givenSearchResultIsOverdueWhenRunThenUpdatesExecuted() {
        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);

        CaseDetails overdueCase = createCaseData("1", LocalDate.of(2024, 10, 20));
        CaseDetails notOverdueCase = createCaseData("2", LocalDate.of(2024, 11, 1));

        when(draftOrderService.isDraftOrderReviewOverdue(any(FinremCaseDetails.class), anyInt()))
            .thenReturn(true, false);
        DraftOrdersReview draftOrdersReview = new DraftOrdersReview();
        when(draftOrderService.getDraftOrderReviewOverdue(any(FinremCaseDetails.class), anyInt()))
            .thenReturn(List.of(draftOrdersReview));

        SearchResult searchResult = createSearchResult(List.of(overdueCase, notOverdueCase));
        when(ccdService.esSearchCases(any(CaseType.class), anyString(), anyString())).thenReturn(searchResult);

        when(ccdService.getCaseByCaseId("1", CaseType.CONTESTED, AUTH_TOKEN)).thenReturn(
            createSearchResult(List.of(overdueCase)));

        when(ccdService.startEventForCaseWorker(AUTH_TOKEN, "1", CONTESTED.getCcdType(),
            AMEND_CASE_CRON.getCcdType())).thenReturn(createStartEventResponse(overdueCase));

        when(notificationRequestMapper.buildCaseworkerDraftOrderReviewOverdue(any(FinremCaseDetails.class),
            any(DraftOrdersReview.class))).thenReturn(new NotificationRequest());

        underTest.run();

        verify(ccdService, times(1)).esSearchCases(any(CaseType.class), anyString(), anyString());
        verify(ccdService, times(1)).startEventForCaseWorker(AUTH_TOKEN, "1", CONTESTED.getCcdType(),
            AMEND_CASE_CRON.getCcdType());
        verify(notificationRequestMapper, times(1))
            .buildCaseworkerDraftOrderReviewOverdue(any(FinremCaseDetails.class), any(DraftOrdersReview.class));
        verify(notificationService, times(1)).sendDraftOrderReviewOverdueToCaseworker(any(NotificationRequest.class));
        verify(ccdService).submitEventForCaseWorker(any(StartEventResponse.class), eq(AUTH_TOKEN), eq("1"),
            eq(CONTESTED.getCcdType()), eq(AMEND_CASE_CRON.getCcdType()),
            eq("Draft order review overdue notification sent"), eq("Draft order review overdue notification sent"));
    }

    private SearchResult createSearchResult(List<CaseDetails> cases) {
        return SearchResult.builder()
            .cases(cases)
            .total(cases.size())
            .build();
    }

    private StartEventResponse createStartEventResponse(CaseDetails caseDetails) {
        return StartEventResponse.builder()
            .caseDetails(caseDetails)
            .build();
    }

    private CaseDetails createCaseData(String caseReference, LocalDate submittedDate) {
        DraftOrdersReview draftOrdersReview = createDraftOrdersReview(LocalDateTime.of(submittedDate,
            LocalTime.of(10, 10)));

        FinremCaseData caseData = FinremCaseData.builder()
            .ccdCaseType(CaseType.CONTESTED)
            .contactDetailsWrapper(ContactDetailsWrapper.builder()
                .applicantFmName("Charlie")
                .applicantLname("Hull")
                .respondentFmName("Stella")
                .respondentLname("Hull")
                .build())
            .regionWrapper(RegionWrapper.builder()
                .allocatedRegionWrapper(AllocatedRegionWrapper.builder()
                    .courtListWrapper(DefaultCourtListWrapper.builder()
                        .liverpoolCourtList(LIVERPOOL_CIVIL_FAMILY_COURT).build())
                    .regionList(Region.NORTHWEST)
                    .northWestFrcList(RegionNorthWestFrc.LIVERPOOL)
                    .build())
                .build())
            .draftOrdersWrapper(DraftOrdersWrapper.builder()
                .draftOrdersReviewCollection(List.of(
                    DraftOrdersReviewCollection.builder()
                        .value(draftOrdersReview)
                        .build()
                ))
                .build())
            .build();

        FinremCaseDetails caseDetails = FinremCaseDetails.builder()
            .id(Long.parseLong(caseReference))
            .caseType(CaseType.CONTESTED)
            .state(State.READY_FOR_HEARING)
            .data(caseData)
            .build();

        return finremCaseDetailsMapper.mapToCaseDetails(caseDetails);
    }

    private DraftOrdersReview createDraftOrdersReview(LocalDateTime submittedDate) {
        DraftOrderDocReviewCollection draftOrderDocReviewCollection = DraftOrderDocReviewCollection.builder()
            .value(DraftOrderDocumentReview.builder()
                .orderStatus(OrderStatus.TO_BE_REVIEWED)
                .submittedDate(submittedDate)
                .build()
            )
            .build();
        return DraftOrdersReview.builder()
            .hearingDate(LocalDate.of(2024,1,5))
            .hearingJudge("judge@test.com")
            .draftOrderDocReviewCollection(List.of(
                draftOrderDocReviewCollection
            ))
            .build();
    }
}
