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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;


@RunWith(MockitoJUnitRunner.class)
public class UpdateCourtInfoAboutToSubmitHandlerTest {

    private UpdateCourtInfoAboutToSubmitHandler handler;

    @Mock
    private CourtDetailsMapper courtDetailsMapper;

    private ObjectMapper objectMapper;

    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
        finremCaseDetailsMapper = new FinremCaseDetailsMapper(objectMapper.registerModule(new JavaTimeModule()));
        handler = new UpdateCourtInfoAboutToSubmitHandler(finremCaseDetailsMapper, courtDetailsMapper);
    }

    @Test
    public void givenCase_whenEventIsGiveAllocationDirection_thenCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.UPDATE_COURT_INFO),
            is(true));
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

        verify(courtDetailsMapper).getCaseDetailsWithOnlyLatestAllocatedCourt(any(), any(), any());
    }
}