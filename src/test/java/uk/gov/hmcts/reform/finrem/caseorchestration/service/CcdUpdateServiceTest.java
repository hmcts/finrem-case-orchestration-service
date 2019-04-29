package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;

import java.io.File;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.ccd.client.model.StartEventResponse.builder;

public class CcdUpdateServiceTest extends BaseServiceTest {
    @Autowired
    private CcdUpdateService ccdUpdateService;
    @MockBean
    private CoreCaseDataApi coreCaseDataApi;
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private static final String AUTH_TOKEN = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJkOG1vbTM3cDR1aTBzc3FpbTBpNzVv"
            + "aTFnNSIsInN1YiI6IjMyIiwiaWF0IjoxNTU2NTI3MDA4LCJleHAiOjE1NTY1NTU4MDgsImRhdGEiOiJjYXNld29ya2VyLWRpdm9yY2Us"
            + "Y2FzZXdvcmtlci1kaXZvcmNlLWZpbmFuY2lhbHJlbWVkeS1jb3VydGFkbWluLGNhc2V3b3JrZXIsY2FzZXdvcmtlci1kaXZvcmNlLWxv"
            + "YTEsY2FzZXdvcmtlci1kaXZvcmNlLWZpbmFuY2lhbHJlbWVkeS1jb3VydGFkbWluLWxvYTEsY2FzZXdvcmtlci1sb2ExIiwidHlwZS"
            + "I6IkFDQ0VTUyIsImlkIjoiMzIiLCJmb3JlbmFtZSI6IkNhc2V3b3JrZXIiLCJzdXJuYW1lIjoiRlIiLCJk"
            + "ZWZhdWx0LXNlcnZpY2UiOiJDQ0QiLCJsb2EiOjEsImRlZmF1bHQtdXJsIjoiaHR0cHM6Ly9sb2NhbGhvc3Q6OTAwM"
            + "C9wb2MvY2NkIiwiZ3JvdXAiOiJjYXNld29ya2VyIn0.MEpcTAjAAxD-ME2DrKbYBvngqpneUKN35LO6WPBs1wk";

    private static final String INVALID_TOKEN = "Bearer eyJhbGciOiJIUzI1NiJ9";

    protected CallbackRequest callbackRequest;
    private StartEventResponse startEventResponse;

    @Before
    public void setupCaseData() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        callbackRequest = mapper.readValue(new File(getClass()
                        .getResource("/fixtures/contested/history-events.json")
                        .toURI()),
                CallbackRequest.class);
    }


    @Test
    public void shouldCreateEvent() {
        stubForCcdEventsForCaseWorker();
        CaseDetails caseDetails = ccdUpdateService.createEvent(AUTH_TOKEN, callbackRequest.getCaseDetails(),
                "FR_issueApplication",
                "Issue Application", "Event Desc");
        Assert.assertNotNull(caseDetails);
        verifyInteractions();
    }

    @Test
    public void shouldThrowIllegalStateException() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("JWT is not valid");
        CaseDetails caseDetails = ccdUpdateService.createEvent(INVALID_TOKEN, callbackRequest.getCaseDetails(),
                "FR_issueApplication",
                "Issue Application", "Event Desc");

        verifyNoMoreInteractions(coreCaseDataApi);
    }

    private void verifyInteractions() {
        verify(coreCaseDataApi).startEventForCaseWorker(any(String.class), any(), any(), any(), anyString(),
                anyString(), anyString());
        verify(coreCaseDataApi, times(1)).submitEventForCaseWorker(any(), any(), any(),
                anyString(), anyString(), anyString(), any(Boolean.class), any(CaseDataContent.class));
    }

    private void stubForCcdEventsForCaseWorker() {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        startEventResponse = builder().token("AAAA").build();
        when(coreCaseDataApi.startEventForCaseWorker(any(String.class), any(), any(), any(), anyString(), anyString(),
                anyString())).thenReturn(startEventResponse);
        when(coreCaseDataApi.submitEventForCaseWorker(any(), any(), any(),
                anyString(), anyString(), anyString(), any(Boolean.class), any(CaseDataContent.class)))
                .thenReturn(caseDetails);
    }
}