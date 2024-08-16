package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler.removesolicitorfromcase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdDataStoreService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.AMEND_CASE_CRON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@ExtendWith(MockitoExtension.class)
class RemoveSolicitorFromCaseTaskTest {

    @InjectMocks
    private RemoveSolicitorFromCaseTask removeSolicitorFromCaseTask;

    @Mock
    private RemoveSolicitorFromCaseFileReader removeSolicitorFromCaseFileReader;
    @Mock
    private SystemUserService systemUserService;
    @Mock
    private CcdService ccdService;
    @Mock
    private CcdDataStoreService ccdDataStoreService;

    @Test
    void givenTaskNotEnabledWhenRunThenDoesNothing() {
        removeSolicitorFromCaseTask.setEnabled(false);

        removeSolicitorFromCaseTask.run();

        verifyNoInteractions(removeSolicitorFromCaseFileReader);
        verifyNoInteractions(systemUserService);
        verifyNoInteractions(ccdService);
        verifyNoInteractions(ccdDataStoreService);
    }

    @Test
    void givenNoRequestsWhenRunThenDoesNothing() throws IOException {
        removeSolicitorFromCaseTask.setEnabled(true);
        when(removeSolicitorFromCaseFileReader.read(anyString())).thenReturn(Collections.emptyList());

        removeSolicitorFromCaseTask.run();

        verify(removeSolicitorFromCaseFileReader, times(1)).read(anyString());
        verifyNoInteractions(systemUserService);
        verifyNoInteractions(ccdService);
        verifyNoInteractions(ccdDataStoreService);
    }

    @Test
    void givenRequestsWhenRunThenHandlesRequests() throws IOException {
        RemoveSolicitorFromCaseRequest request1 = new RemoveSolicitorFromCaseRequest("1", CONTESTED.getCcdType(),
            "user1", "[APPSOLICITOR]", "DFR-1");
        RemoveSolicitorFromCaseRequest request2 = new RemoveSolicitorFromCaseRequest("2", CONSENTED.getCcdType(),
            "user2", "[RESPSOLICITOR]", "DFR-2");
        when(removeSolicitorFromCaseFileReader.read(anyString())).thenReturn(List.of(request1, request2));

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);

        CaseDetails caseDetails1 = createCaseDetails("1");
        when(ccdService.getCaseByCaseId("1", CONTESTED, AUTH_TOKEN)).thenReturn(createSearchResult(caseDetails1));
        CaseDetails caseDetails2 = createCaseDetails("2");
        when(ccdService.getCaseByCaseId("2", CONSENTED, AUTH_TOKEN)).thenReturn(createSearchResult(caseDetails2));

        StartEventResponse startEventResponse1 = createStartEventResponse(caseDetails1);
        when(ccdService.startEventForCaseWorker(AUTH_TOKEN, "1", CONTESTED.getCcdType(), AMEND_CASE_CRON.getCcdType()))
            .thenReturn(startEventResponse1);
        StartEventResponse startEventResponse2 = createStartEventResponse(caseDetails1);
        when(ccdService.startEventForCaseWorker(AUTH_TOKEN, "2", CONSENTED.getCcdType(), AMEND_CASE_CRON.getCcdType()))
            .thenReturn(startEventResponse2);

        removeSolicitorFromCaseTask.setEnabled(true);

        removeSolicitorFromCaseTask.run();

        verify(ccdService, times(1)).startEventForCaseWorker(AUTH_TOKEN, "1", CONTESTED.getCcdType(),
            AMEND_CASE_CRON.getCcdType());
        verify(ccdService, times(1)).startEventForCaseWorker(AUTH_TOKEN, "2", CONSENTED.getCcdType(),
            AMEND_CASE_CRON.getCcdType());

        verify(ccdService, times(1)).submitEventForCaseWorker(startEventResponse1, AUTH_TOKEN, "1",
            CONTESTED.getCcdType(), AMEND_CASE_CRON.getCcdType(), "DFR-1",
            "Remove user user1 case role [APPSOLICITOR]");
        verify(ccdService, times(1)).submitEventForCaseWorker(startEventResponse2, AUTH_TOKEN, "2",
            CONSENTED.getCcdType(), AMEND_CASE_CRON.getCcdType(), "DFR-2",
            "Remove user user2 case role [RESPSOLICITOR]");

        verify(ccdDataStoreService).removeUserRole(caseDetails1, AUTH_TOKEN, "user1", "[APPSOLICITOR]");
        verify(ccdDataStoreService).removeUserRole(caseDetails2, AUTH_TOKEN, "user2", "[RESPSOLICITOR]");
    }

    @Test
    void givenNoSearchResultWhenRunThenThrowsException() throws IOException {
        RemoveSolicitorFromCaseRequest request1 = new RemoveSolicitorFromCaseRequest("1", CONTESTED.getCcdType(),
            "user1", "[APPSOLICITOR]", "DFR-1");
        when(removeSolicitorFromCaseFileReader.read(anyString())).thenReturn(List.of(request1));

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);

        SearchResult searchResult = SearchResult.builder()
            .total(0)
            .build();
        when(ccdService.getCaseByCaseId("1", CONTESTED, AUTH_TOKEN)).thenReturn(searchResult);
        removeSolicitorFromCaseTask.setEnabled(true);

        assertThatThrownBy(() -> removeSolicitorFromCaseTask.run())
            .isInstanceOf(RemoveSolicitorFromCaseException.class)
            .hasMessageMatching("Found 0 search results for case 1");
    }

    @Test
    void givenMoreThanOneSearchResultWhenRunThenThrowsException() throws IOException {
        RemoveSolicitorFromCaseRequest request1 = new RemoveSolicitorFromCaseRequest("1", CONTESTED.getCcdType(),
            "user1", "[APPSOLICITOR]", "DFR-1");
        when(removeSolicitorFromCaseFileReader.read(anyString())).thenReturn(List.of(request1, request1));

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);

        SearchResult searchResult = SearchResult.builder()
            .total(0)
            .build();
        when(ccdService.getCaseByCaseId("1", CONTESTED, AUTH_TOKEN)).thenReturn(searchResult);
        removeSolicitorFromCaseTask.setEnabled(true);

        assertThatThrownBy(() -> removeSolicitorFromCaseTask.run())
            .isInstanceOf(RemoveSolicitorFromCaseException.class)
            .hasMessageMatching("Found 0 search results for case 1");
    }

    private CaseDetails createCaseDetails(String caseReference) {
        return CaseDetails.builder()
            .id(Long.parseLong(caseReference))
            .build();
    }

    private SearchResult createSearchResult(CaseDetails caseDetails) {
        return SearchResult.builder()
            .cases(List.of(caseDetails))
            .total(1)
            .build();
    }

    private StartEventResponse createStartEventResponse(CaseDetails caseDetails) {
        return StartEventResponse.builder()
            .caseDetails(caseDetails)
            .eventId(AMEND_CASE_CRON.getCcdType())
            .token(AUTH_TOKEN)
            .build();
    }
}
