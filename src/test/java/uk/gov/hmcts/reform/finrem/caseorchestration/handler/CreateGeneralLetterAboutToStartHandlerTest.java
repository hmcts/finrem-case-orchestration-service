package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralLetterAddressToType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralLetterWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.anyString;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@RunWith(MockitoJUnitRunner.class)
public class CreateGeneralLetterAboutToStartHandlerTest {

    private CreateGeneralLetterAboutToStartHandler handler;

    @Mock
    private IdamService idamService;
    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    @Before
    public void setup() {
        handler =  new CreateGeneralLetterAboutToStartHandler(finremCaseDetailsMapper, idamService);
        when(idamService.getIdamFullName(anyString())).thenReturn("UserName");
    }

    @Test
    public void givenACcdCallbackCreateGeneralLetterAboutToStartHandler_WhenCanHandleCalled_thenHandlerCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.CREATE_GENERAL_LETTER),
            is(true));
    }

    @Test
    public void givenACcdCallbackCreateGeneralLetterAboutToStartHandlerJudge_WhenCanHandleCalled_thenHandlerCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.CREATE_GENERAL_LETTER_JUDGE),
            is(true));
    }
    @Test
    public void givenACcdCallbackAboutToSubmit_WhenCanHandleCalled_thenHandlerCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.CREATE_GENERAL_LETTER),
            is(false));
    }

    @Test
    public void givenACcdCallbackCallbackCreateGeneralLetterAboutToStartHandler_WhenHandle_thenClearData() {
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest();
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);
        assertNull(response.getData().getGeneralLetterWrapper().getGeneralLetterAddressTo());
        assertNull(response.getData().getGeneralLetterWrapper().getGeneralLetterRecipient());
        assertNull(response.getData().getGeneralLetterWrapper().getGeneralLetterRecipientAddress());
        assertEquals("UserName", response.getData().getGeneralLetterWrapper().getGeneralLetterCreatedBy());
        assertNull(response.getData().getGeneralLetterWrapper().getGeneralLetterBody());
        assertNull(response.getData().getGeneralLetterWrapper().getGeneralLetterPreview());
    }

    private FinremCallbackRequest buildFinremCallbackRequest() {
        FinremCaseData caseData = FinremCaseData.builder()
            .generalLetterWrapper(GeneralLetterWrapper.builder()
                .generalLetterAddressTo(GeneralLetterAddressToType.APPLICANT_SOLICITOR)
                .generalLetterRecipient("Test")
                .generalLetterRecipientAddress(Address.builder()
                    .addressLine1("line1")
                    .addressLine2("line2")
                    .country("country")
                    .postCode("AB1 1BC").build())
                .generalLetterCreatedBy("Test")
                .generalLetterBody("body")
                .generalLetterPreview(CaseDocument.builder().build())
                .build())
            .build();
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().id(123L).data(caseData).build();
        return FinremCallbackRequest.builder().eventType(EventType.CREATE_GENERAL_LETTER).caseDetails(caseDetails).build();
    }

}
