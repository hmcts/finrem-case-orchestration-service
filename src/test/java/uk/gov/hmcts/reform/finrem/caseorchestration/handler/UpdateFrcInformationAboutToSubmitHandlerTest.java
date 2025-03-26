package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class UpdateFrcInformationAboutToSubmitHandlerTest {

    private UpdateFrcInformationAboutToSubmitHandler handler;

    @Mock
    private CourtDetailsMapper courtDetailsMapper;

    @Before
    public void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        FinremCaseDetailsMapper finremCaseDetailsMapper = new FinremCaseDetailsMapper(objectMapper.registerModule(new JavaTimeModule()));
        handler = new UpdateFrcInformationAboutToSubmitHandler(finremCaseDetailsMapper, courtDetailsMapper);
    }

    @Test
    public void givenCase_whenEventIsGiveAllocationDirection_thenCanHandle() {
        Assertions.assertCanHandle(handler, CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED,
            EventType.UPDATE_FRC_INFORMATION);
    }

    @Test
    public void givenCase_whenHandleAllocationDirection_thenCourtDetailsMapperCalled() {

        FinremCaseDetails caseDetails =
            FinremCaseDetails.builder().data(FinremCaseData.builder().build()).build();
        FinremCaseDetails caseDetailsBefore =
            FinremCaseDetails.builder().data(FinremCaseData.builder().build()).build();

        FinremCallbackRequest callbackRequest =
            FinremCallbackRequest.builder().caseDetails(caseDetails).caseDetailsBefore(caseDetailsBefore).build();

        handler.handle(callbackRequest, "AUTH");

        verify(courtDetailsMapper).getLatestAllocatedCourt(any(), any(), any());
    }
}
