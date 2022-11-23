package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingDocumentService;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FORM_C;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FORM_G;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_DATE;

@RunWith(MockitoJUnitRunner.class)
public class ReGenerateFormCAboutToSubmitHandlerTest {

    public static final String THERE_IS_NO_HEARING_ON_THE_CASE_ERROR_MESSAGE = "There is no hearing on the case";
    public static final String AUTH = "AUTH";
    public static final String NEW_FORM_C_FILE_NAME = "new form c";
    public static final String NEW_FORM_G_FILE_NAME = "new form g";
    private static final String OLD_FORM_C_FILE_NAME = "old form c";
    private static final String OLD_FORM_G_FILE_NAME = "old form g";

    @Mock
    private HearingDocumentService hearingDocumentService;

    @InjectMocks
    private ReGenerateFormCAboutToSubmitHandler reGenerateFormCAboutToSubmitHandler;

    @Test
    public void givenACcdCallbackContestedCase_WhenAnAboutToSubmitRegenFormC_thenHandlerCanHandle() {
        assertThat(reGenerateFormCAboutToSubmitHandler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.REGENERATE_FORM_C),
            is(true));
    }

    @Test
    public void givenACcdCallbackConsentedCase_WhenAnAboutToSubmitRegenFormC_thenHandlerCanNotHandle() {
        assertThat(reGenerateFormCAboutToSubmitHandler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.REGENERATE_FORM_C),
            is(false));
    }

    @Test
    public void givenACaseWithoutHearing_WhenAnAboutToSubmitRegenFormC_thenThrowError() {
        CallbackRequest callbackRequest =
            CallbackRequest.builder().caseDetails(getCaseDetailsTest()).caseDetailsBefore(getCaseDetailsTest()).build();

        AboutToStartOrSubmitCallbackResponse response = reGenerateFormCAboutToSubmitHandler.handle(callbackRequest, AUTH);

        assertThat(response.getErrors().get(0), is(THERE_IS_NO_HEARING_ON_THE_CASE_ERROR_MESSAGE));
    }

    @Test
    public void givenACaseWithHearingDate_WhenAnAboutToSubmitRegenFormC_thenThrowNoError() {
        CallbackRequest callbackRequest =
            CallbackRequest.builder().caseDetails(getCaseDetailsTest()).caseDetailsBefore(getCaseDetailsTest()).build();
        when(hearingDocumentService.generateHearingDocuments(any(), any())).thenReturn(new HashMap<>());
        callbackRequest.getCaseDetails().getData().put(HEARING_DATE, "2019-06-24");

        AboutToStartOrSubmitCallbackResponse response = reGenerateFormCAboutToSubmitHandler.handle(callbackRequest, AUTH);

        assertThat(response.getErrors(), is(nullValue()));
    }

    @Test
    public void givenACaseWithHearingDate_WhenAnAboutToSubmitRegenFormC_thenAddFormCAndG() {
        CallbackRequest callbackRequest =
            CallbackRequest.builder().caseDetails(getCaseDetailsTest()).caseDetailsBefore(getCaseDetailsTest()).build();
        when(hearingDocumentService.generateHearingDocuments(any(), any()))
            .thenReturn(getTestFormCAndG(NEW_FORM_C_FILE_NAME, NEW_FORM_G_FILE_NAME));
        callbackRequest.getCaseDetails().getData().put(HEARING_DATE, "2019-06-24");

        AboutToStartOrSubmitCallbackResponse response = reGenerateFormCAboutToSubmitHandler.handle(callbackRequest, AUTH);

        assertThat(((CaseDocument) response.getData().get(FORM_C)).getDocumentFilename(), is(NEW_FORM_C_FILE_NAME));
        assertThat(((CaseDocument) response.getData().get(FORM_G)).getDocumentFilename(), is(NEW_FORM_G_FILE_NAME));
    }

    @Test
    public void givenACaseWithPreviousFormCAndG_WhenAnAboutToSubmitRegenFormC_thenAddOldFormCAndRemoved() {
        CallbackRequest callbackRequest =
            CallbackRequest.builder().caseDetails(getCaseDetailsTest()).caseDetailsBefore(getCaseDetailsTest()).build();
        callbackRequest.getCaseDetails().getData().putAll(getTestFormCAndG(OLD_FORM_C_FILE_NAME, OLD_FORM_G_FILE_NAME));
        when(hearingDocumentService.generateHearingDocuments(any(), any()))
            .thenReturn(getTestFormCAndG(NEW_FORM_C_FILE_NAME, NEW_FORM_G_FILE_NAME));
        callbackRequest.getCaseDetails().getData().put(HEARING_DATE, "2019-06-24");

        AboutToStartOrSubmitCallbackResponse response = reGenerateFormCAboutToSubmitHandler.handle(callbackRequest, AUTH);

        assertThat(((CaseDocument) response.getData().get(FORM_C)).getDocumentFilename(), is(not(OLD_FORM_C_FILE_NAME)));
        assertThat(((CaseDocument) response.getData().get(FORM_G)).getDocumentFilename(), is(not(OLD_FORM_G_FILE_NAME)));
    }

    private Map<String, CaseDocument> getTestFormCAndG(String formCFileName, String formGFileName) {
        HashMap<String, CaseDocument> documents = new HashMap<>();
        documents.put(FORM_C, CaseDocument.builder().documentFilename(formCFileName).build());
        documents.put(FORM_G, CaseDocument.builder().documentFilename(formGFileName).build());
        return documents;
    }

    private CaseDetails getCaseDetailsTest() {
        Map<String, Object> caseData = new HashMap<>();
        return CaseDetails.builder().id(123L).data(caseData).build();
    }
}