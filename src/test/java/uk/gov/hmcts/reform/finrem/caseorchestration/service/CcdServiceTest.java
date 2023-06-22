package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.CaseEventsApi;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.IdamToken;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CcdServiceTest {

    public static final String AUTH_TOKEN = "tokien:)";

    @Mock
    private CoreCaseDataApi coreCaseDataApi;
    @Mock
    private CaseEventsApi caseEventsApi;
    @Mock
    private IdamAuthService idamAuthService;
    @InjectMocks
    private CcdService ccdService;

    @Test
    public void givenCallback_WhenExecuteEvent_ThenCcdApiCalled() {
        when(coreCaseDataApi.startEventForCaseWorker(any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(StartEventResponse.builder().caseDetails(buildCaseDetails()).build());
        when(idamAuthService.getIdamToken(AUTH_TOKEN)).thenReturn(IdamToken.builder().build());

        ccdService.executeCcdEventOnCase(AUTH_TOKEN, buildCaseDetails(), EventType.CLOSE.getCcdType());

        verify(coreCaseDataApi).startEventForCaseWorker(any(), any(), any(), any(), any(), any(), any());
        verify(coreCaseDataApi).submitEventForCaseWorker(any(), any(), any(), any(), any(), any(), anyBoolean(), any());
    }

    @Test
    public void givenCallback_WhenExecuteGetEvents_ThenCcdApiCalled() {
        when(caseEventsApi.findEventDetailsForCase(any(), any(), any(), any(), any(), any()))
            .thenReturn(any());
        when(idamAuthService.getIdamToken(AUTH_TOKEN)).thenReturn(IdamToken.builder().build());

        ccdService.getCcdEventDetailsOnCase(AUTH_TOKEN, buildCaseDetails(), EventType.CLOSE.getCcdType());

        verify(caseEventsApi).findEventDetailsForCase(any(), any(), any(), any(), any(), any());
    }

    @Test
    public void shouldReturnCaseDetailsByCaseId() {
        CaseDetails caseDetails = buildCaseDetails();
        when(coreCaseDataApi.searchCases(any(), any(), any(), any())).thenReturn(SearchResult.builder()
            .cases(List.of(caseDetails)).build());
        when(idamAuthService.getIdamToken(AUTH_TOKEN)).thenReturn(IdamToken.builder().build());

        SearchResult result = ccdService.getCaseByCaseId("123", CaseType.CONTESTED, AUTH_TOKEN);

        verify(coreCaseDataApi).searchCases(any(), any(), any(), any());
    }

    private CaseDetails buildCaseDetails() {
        Map<String, Object> caseData = new HashMap<>();
        return CaseDetails.builder().id(123L).data(caseData).build();
    }

}