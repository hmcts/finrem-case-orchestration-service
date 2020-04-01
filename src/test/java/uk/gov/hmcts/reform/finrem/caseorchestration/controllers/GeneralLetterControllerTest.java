package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import org.junit.Test;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralLetterService;

import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.DOC_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.feignError;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.generalLetterDataMap;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@WebMvcTest(GeneralLetterController.class)
public class GeneralLetterControllerTest extends BaseControllerTest {

    @MockBean
    private GeneralLetterService documentService;

    public String endpoint() {
        return "/case-orchestration/documents/general-letter";
    }

    @Test
    public void generateGeneralLetterSuccess() throws Exception {
        doValidCaseDataSetUp();
        whenServiceGeneratesDocument().thenReturn(generalLetterDataMap());

        mvc.perform(post(endpoint())
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.generalLetterCollection[0].id", is(notNullValue())))
                .andExpect(
                        jsonPath("$.data.generalLetterCollection[0].value.generatedLetter.document_url", is(DOC_URL)))
                .andExpect(
                        jsonPath("$.data.generalLetterCollection[0].value.generatedLetter.document_filename",
                                is(FILE_NAME)))
                .andExpect(
                        jsonPath("$.data.generalLetterCollection[0].value.generatedLetter.document_binary_url",
                                is(BINARY_URL)));

    }

    @Test
    public void generateGeneralLetter400Error() throws Exception {
        doEmptyCaseDataSetUp();

        mvc.perform(post(endpoint())
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void generateGeneralLetter500Error() throws Exception {
        doValidCaseDataSetUp();
        whenServiceGeneratesDocument().thenThrow(feignError());

        mvc.perform(post(endpoint())
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isInternalServerError());
    }

    private OngoingStubbing<Map<String, Object>> whenServiceGeneratesDocument() {
        return when(documentService.createGeneralLetter(eq(AUTH_TOKEN), isA(CaseDetails.class)));
    }
}