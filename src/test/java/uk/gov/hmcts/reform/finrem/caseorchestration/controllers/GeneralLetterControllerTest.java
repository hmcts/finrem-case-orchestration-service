package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralLetterService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
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

    public static final String GENERAL_LETTER_URL = "/case-orchestration/documents/general-letter";
    public static final String PREVIEW_GENERAL_LETTER_URL = "/case-orchestration/documents/preview-general-letter";
    public static final String START_GENERAL_LETTER_URL = "/case-orchestration/general-letter-start";

    @MockBean
    private GeneralLetterService generalLetterService;

    @MockBean
    private IdamService idamService;

    @Test
    public void generateGeneralLetterSuccess() throws Exception {
        doValidCaseDataSetUp();

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

        mvc.perform(post(GENERAL_LETTER_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void generateGeneralLetter500Error() throws Exception {
        doValidCaseDataSetUp();
        doThrow(feignError()).when(generalLetterService).createGeneralLetter(eq(AUTH_TOKEN), isA(CaseDetails.class));

        mvc.perform(post(GENERAL_LETTER_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void startGeneralLetterPropertiesSuccess() throws Exception {
        when(idamService.getIdamFullName(AUTH_TOKEN)).thenReturn("Integration Test");

        mvc.perform(post(START_GENERAL_LETTER_URL)
            .content(resourceContentAsString("/fixtures/general-letter.json"))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.generalLetterAddressTo", is(nullValue())))
            .andExpect(jsonPath("$.data.generalLetterRecipient", is(nullValue())))
            .andExpect(jsonPath("$.data.generalLetterRecipientAddress", is(nullValue())))
            .andExpect(jsonPath("$.data.generalLetterCreatedBy", is("Integration Test")))
            .andExpect(jsonPath("$.data.generalLetterBody", is(nullValue())));
    }

    @Test
    public void startGeneralLetterPropertiesBadRequest() throws Exception {
        doEmptyCaseDataSetUp();

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

        mvc.perform(post(START_GENERAL_LETTER_URL)
            .content(resourceContentAsString("/fixtures/general-letter.json"))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isInternalServerError());
    }

    @Test
    public void previewGeneralLetterSuccess() throws Exception {
        doValidCaseDataSetUp();

        mvc.perform(post(PREVIEW_GENERAL_LETTER_URL)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(generalLetterService, times(1)).previewGeneralLetter(any(), any());
    }
}
