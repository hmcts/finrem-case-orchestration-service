package uk.gov.hmcts.reform.finrem.caseorchestration.handler;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseFlagsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnStartDefaultValueService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnlineFormDocumentService;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.*;

public class SolicitorCreateContestedAboutToSubmitHandlerTest {

    public static final String AUTH_TOKEN = "tokien:)";
    private SolicitorCreateContestedAboutToSubmitHandler handler;


    @Mock
    FinremCaseDetailsMapper finremCaseDetailsMapper;
    @Mock
    OnlineFormDocumentService onlineFormDocumentService;
    @Mock
    CaseFlagsService caseFlagsService;
    @Mock
    IdamService idamService;


    @Before
    public void setup() {
        handler =  new SolicitorCreateContestedAboutToSubmitHandler(
            finremCaseDetailsMapper,
            onlineFormDocumentService,
            caseFlagsService,
            idamService
        );
    }

    @Test
    public void givenContestedCase_whenEventIsAmendAndCallbackIsSubmitted_thenHandlerCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.SOLICITOR_CREATE),
            is(false));
    }

    @Test
    public void givenContestedCase_whenEventIsAmend_thenHandlerCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.SOLICITOR_CREATE),
            is(true));
    }

    @Test
    public void handle() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseData responseCaseData = handler.handle(callbackRequest, AUTH_TOKEN).getData();
        assertEquals(NO_VALUE, responseCaseData.getCivilPartnership());
        assertEquals(TYPE_OF_APPLICATION_DEFAULT_TO, responseCaseData.getScheduleOneWrapper().getTypeOfApplication());
        assertEquals(NO_VALUE, responseCaseData.getPromptForUrgentCaseQuestion());
    }

    private CallbackRequest buildCallbackRequest() {
        Map<String, Object> caseData = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder().id(123L).data(caseData).build();
        return CallbackRequest.builder().eventId("SomeEventId").caseDetails(caseDetails).build();
    }
}