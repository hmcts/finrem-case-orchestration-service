package uk.gov.hmcts.reform.finrem.caseorchestration.event.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.event.StopRepresentingClientEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.nocworkflows.UpdateRepresentationWorkflowService;

import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;

@ExtendWith(MockitoExtension.class)
class StopRepresentingClientEventHandlerTest {

    @Mock
    private UpdateRepresentationWorkflowService nocWorkflowService;

    private StopRepresentingClientEventHandler underTest;

    @BeforeEach
    public void setup() {
        underTest = new StopRepresentingClientEventHandler(nocWorkflowService);
    }

    @Test
    void testHandleEvent() {
        FinremCaseData caseData = FinremCaseData.builder().ccdCaseId(CASE_ID).build();
        FinremCaseData caseDataBefore = FinremCaseData.builder().ccdCaseId(CASE_ID).build();

        StopRepresentingClientEvent event = StopRepresentingClientEvent.builder()
            .data(caseData)
            .originalData(caseDataBefore)
            .userAuthorisation(AUTH_TOKEN)
            .build();

        underTest.handleEvent(event);

        verify(nocWorkflowService).handleNoticeOfChangeWorkflow(caseData, caseDataBefore, AUTH_TOKEN);
    }
}
