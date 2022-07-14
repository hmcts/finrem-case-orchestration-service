package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralLetterService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.serialisation.FinremCallbackRequestDeserializer;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.feignError;

@WebMvcTest(GeneralLetterController.class)
public class GeneralLetterControllerTest extends BaseControllerTest {

    private static final String GENERAL_LETTER_URL = "/case-orchestration/documents/general-letter";
    private static final String PREVIEW_GENERAL_LETTER_URL = "/case-orchestration/documents/preview-general-letter";
    private static final String START_GENERAL_LETTER_URL = "/case-orchestration/general-letter-start";

    @MockBean
    private GeneralLetterService generalLetterService;

    @MockBean
    private IdamService idamService;

    @MockBean
    private FinremCallbackRequestDeserializer deserializer;

    @Test
    public void generateGeneralLetterSuccess() throws Exception {
        doValidCaseDataSetUp();
        when(deserializer.deserialize(any())).thenReturn(getCallbackRequest());

        mvc.perform(post(GENERAL_LETTER_URL)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(generalLetterService, times(1)).createGeneralLetter(any(), any());
    }

    @Test
    public void generateGeneralLetter400Error() throws Exception {
        doEmptyCaseDataSetUp();
        when(deserializer.deserialize(any())).thenReturn(getCallbackRequestEmptyCaseData());

        mvc.perform(post(GENERAL_LETTER_URL)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void generateGeneralLetter500Error() throws Exception {
        doValidCaseDataSetUp();
        when(deserializer.deserialize(any())).thenReturn(getCallbackRequest());
        doThrow(feignError()).when(generalLetterService).createGeneralLetter(eq(AUTH_TOKEN), isA(FinremCaseDetails.class));

        mvc.perform(post(GENERAL_LETTER_URL)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isInternalServerError());
    }

    @Test
    public void startGeneralLetterPropertiesSuccess() throws Exception {
        when(idamService.getIdamFullName(AUTH_TOKEN)).thenReturn("Integration Test");
        when(deserializer.deserialize(any()))
            .thenReturn(getCallbackRequest(resourceContentAsString("/fixtures/general-letter.json")));

        mvc.perform(post(START_GENERAL_LETTER_URL)
            .content(resourceContentAsString("/fixtures/general-letter.json"))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.generalLetterAddressTo").doesNotExist())
            .andExpect(jsonPath("$.data.generalLetterRecipient").doesNotExist())
            .andExpect(jsonPath("$.data.generalLetterRecipientAddress").doesNotExist())
            .andExpect(jsonPath("$.data.generalLetterCreatedBy", is("Integration Test")))
            .andExpect(jsonPath("$.data.generalLetterBody").doesNotExist());
    }

    @Test
    public void startGeneralLetterPropertiesBadRequest() throws Exception {
        doEmptyCaseDataSetUp();
        when(deserializer.deserialize(any())).thenReturn(getCallbackRequestEmptyCaseData());

        mvc.perform(post(START_GENERAL_LETTER_URL)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void startGeneralLetterPropertiesInternalServerError() throws Exception {
        when(idamService.getIdamFullName(AUTH_TOKEN))
            .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
        when(deserializer.deserialize(any()))
            .thenReturn(getCallbackRequest(resourceContentAsString("/fixtures/general-letter.json")));

        mvc.perform(post(START_GENERAL_LETTER_URL)
            .content(resourceContentAsString("/fixtures/general-letter.json"))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isInternalServerError());
    }

    @Test
    public void previewGeneralLetterSuccess() throws Exception {
        doValidCaseDataSetUp();
        when(deserializer.deserialize(any())).thenReturn(getCallbackRequest());

        mvc.perform(post(PREVIEW_GENERAL_LETTER_URL)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(generalLetterService, times(1)).previewGeneralLetter(any(), any());
    }
}
