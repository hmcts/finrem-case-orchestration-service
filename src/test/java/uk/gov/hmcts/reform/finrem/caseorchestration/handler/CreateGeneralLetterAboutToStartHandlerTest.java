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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDataConsented;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralLetterWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerFourWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOneWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerThreeWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerTwoWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER1;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER1_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER2;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER2_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER3;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER3_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER4;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER4_SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.OTHER_RECIPIENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_SOLICITOR;

@RunWith(MockitoJUnitRunner.class)
public class CreateGeneralLetterAboutToStartHandlerTest {
    private static final String APP_SOLICITOR_LABEL = "Applicant Solicitor";
    private static final String INTV1_LABEL = "Intervener 1";
    private static final String INTV2_LABEL = "Intervener 2";
    private static final String INTV3_LABEL = "Intervener 3";
    private static final String INTV4_LABEL = "Intervener 4";
    private static final String INTV1_SOLICITOR_LABEL = "Intervener 1 Solicitor";
    private static final String INTV2_SOLICITOR_LABEL = "Intervener 2 Solicitor";
    private static final String INTV3_SOLICITOR_LABEL = "Intervener 3 Solicitor";
    private static final String INTV4_SOLICITOR_LABEL = "Intervener 4 Solicitor";
    private static final String RESP_SOLICITOR_LABEL = "Respondent Solicitor";

    private CreateGeneralLetterAboutToStartHandler handler;

    @Mock
    private IdamService idamService;
    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    @Before
    public void setup() {
        handler = new CreateGeneralLetterAboutToStartHandler(finremCaseDetailsMapper, idamService);
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
    public void givenACcdCallbackCreateGeneralLetterAboutToStartHandler_WhenHandle_thenClearData() {
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest();
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);
        assertNull(response.getData().getGeneralLetterWrapper().getGeneralLetterAddressTo());
        assertNull(response.getData().getGeneralLetterWrapper().getGeneralLetterRecipient());
        assertNull(response.getData().getGeneralLetterWrapper().getGeneralLetterRecipientAddress());
        assertEquals("UserName", response.getData().getGeneralLetterWrapper().getGeneralLetterCreatedBy());
        assertNull(response.getData().getGeneralLetterWrapper().getGeneralLetterBody());
        assertNull(response.getData().getGeneralLetterWrapper().getGeneralLetterPreview());
        assertNull(response.getData().getGeneralLetterWrapper().getGeneralLetterUploadedDocument());
    }

    @Test
    public void givenACcdCallbackCreateGeneralLetterAboutToStartHandler_AndNoIntervenersPresent_WhenHandle_thenSetUpCorrectDynamicList() {
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest();
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);
        DynamicRadioListElement defaultSelectedOption = DynamicRadioListElement.builder().code(APPLICANT).label(APPLICANT).build();
        DynamicRadioList expectedRadioList = DynamicRadioList.builder().value(defaultSelectedOption)
            .listItems(getDynamicRadioListItems(false)).build();
        assertEquals(expectedRadioList, response.getData().getGeneralLetterWrapper().getGeneralLetterAddressee());
    }

    @Test
    public void givenACcdCallbackCreateGeneralLetterAboutToStartHandler_AndIntervenersPresent_WhenHandle_thenSetUpCorrectDynamicList() {
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest();
        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();
        addIntervenerWrappers(caseData);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);
        DynamicRadioListElement defaultSelectedOption = DynamicRadioListElement.builder().code(APPLICANT).label(APPLICANT).build();
        DynamicRadioList expectedRadioList = DynamicRadioList.builder().value(defaultSelectedOption)
            .listItems(getDynamicRadioListItems(true)).build();
        assertEquals(expectedRadioList, response.getData().getGeneralLetterWrapper().getGeneralLetterAddressee());
    }

    private void addIntervenerWrappers(FinremCaseData caseData) {
        List<IntervenerWrapper> intervenerWrappers = List.of(
            IntervenerOneWrapper.builder().build(), IntervenerTwoWrapper.builder().build(),
            IntervenerThreeWrapper.builder().build(), IntervenerFourWrapper.builder().build()
        );
        intervenerWrappers.forEach(wrapper -> {
            wrapper.setIntervenerName("intervener");
            wrapper.setIntervenerSolName("intervener sol");
        });
        caseData.setIntervenerOneWrapper((IntervenerOneWrapper) intervenerWrappers.get(0));
        caseData.setIntervenerTwoWrapper((IntervenerTwoWrapper) intervenerWrappers.get(1));
        caseData.setIntervenerThreeWrapper((IntervenerThreeWrapper) intervenerWrappers.get(2));
        caseData.setIntervenerFourWrapper((IntervenerFourWrapper) intervenerWrappers.get(3));
    }

    private List<DynamicRadioListElement> getDynamicRadioListItems(boolean addIntervenerListElements) {
        List<DynamicRadioListElement> listElements = new ArrayList<>(List.of(
                DynamicRadioListElement.builder().code(APPLICANT).label(APPLICANT).build(),
                DynamicRadioListElement.builder().code(APPLICANT_SOLICITOR).label(APP_SOLICITOR_LABEL).build(),
                DynamicRadioListElement.builder().code(RESPONDENT).label(RESPONDENT).build(),
                DynamicRadioListElement.builder().code(RESPONDENT_SOLICITOR).label(RESP_SOLICITOR_LABEL).build(),
                DynamicRadioListElement.builder().code(OTHER_RECIPIENT).label(OTHER_RECIPIENT).build()));
        if (addIntervenerListElements) {
            listElements.addAll(List.of(DynamicRadioListElement.builder().code(INTERVENER1).label(INTV1_LABEL).build(),
                DynamicRadioListElement.builder().code(INTERVENER1_SOLICITOR).label(INTV1_SOLICITOR_LABEL).build(),
                DynamicRadioListElement.builder().code(INTERVENER2).label(INTV2_LABEL).build(),
                DynamicRadioListElement.builder().code(INTERVENER2_SOLICITOR).label(INTV2_SOLICITOR_LABEL).build(),
                DynamicRadioListElement.builder().code(INTERVENER3).label(INTV3_LABEL).build(),
                DynamicRadioListElement.builder().code(INTERVENER3_SOLICITOR).label(INTV3_SOLICITOR_LABEL).build(),
                DynamicRadioListElement.builder().code(INTERVENER4).label(INTV4_LABEL).build(),
                DynamicRadioListElement.builder().code(INTERVENER4_SOLICITOR).label(INTV4_SOLICITOR_LABEL).build()
            ));
        }
        return listElements;
    }

    private FinremCallbackRequest buildFinremCallbackRequest() {
        FinremCaseData caseData = FinremCaseDataConsented.builder()
            .generalLetterWrapper(GeneralLetterWrapper.builder()
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
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().id(123L).data(caseData).caseType(CaseType.CONTESTED).build();
        return FinremCallbackRequest.builder().eventType(EventType.CREATE_GENERAL_LETTER).caseDetails(caseDetails).build();
    }

}
