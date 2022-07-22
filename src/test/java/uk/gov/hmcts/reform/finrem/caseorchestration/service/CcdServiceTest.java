package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.wrapper.IdamToken;
import uk.gov.hmcts.reform.finrem.ccd.domain.CaseType;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;

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
    private SystemUserService systemUserService;
    @InjectMocks
    private CcdService ccdService;

    @Test
    public void givenCallback_WhenExecuteEvent_ThenCcdApiCalled() {
        when(coreCaseDataApi.startEventForCaseWorker(any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(StartEventResponse.builder().build());
        when(systemUserService.getIdamToken(AUTH_TOKEN)).thenReturn(IdamToken.builder().build());

        ccdService.executeCcdEventOnCase(AUTH_TOKEN, buildCaseDetails(), EventType.CLOSE.getCcdType());

        verify(coreCaseDataApi).startEventForCaseWorker(any(), any(), any(), any(), any(), any(), any());
        verify(coreCaseDataApi).submitEventForCaseWorker(any(), any(), any(), any(), any(), any(), anyBoolean(), any());
    }

    private FinremCaseDetails buildCaseDetails() {
        FinremCaseData caseData = new FinremCaseData();
        return FinremCaseDetails.builder().caseType(CaseType.CONTESTED).id(123L).caseData(caseData).build();
    }

}