package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import org.junit.Test;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralOrderService;

import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.DOC_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDataWithGeneralOrder;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.feignError;


@WebMvcTest(GeneralOrderController.class)
public class GeneralOrderControllerTest extends BaseControllerTest {

    @MockBean
    private GeneralOrderService documentService;

    public String generateEndpoint() {
        return "/case-orchestration/documents/preview-general-order";
    }

    public String submitEndpoint() {
        return "/case-orchestration/submit-general-order";
    }

    @Test
    public void generateGeneralOrderSuccess() throws Exception {
        doValidCaseDataSetUp();
        whenServiceGeneratesDocument().thenReturn(caseDataWithGeneralOrder());

        mvc.perform(post(generateEndpoint())
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.data.generalOrderPreviewDocument.document_url", is(DOC_URL)))
            .andExpect(
                jsonPath("$.data.generalOrderPreviewDocument.document_filename",
                    is(FILE_NAME)))
            .andExpect(
                jsonPath("$.data.generalOrderPreviewDocument.document_binary_url",
                    is(BINARY_URL)));

    }

    @Test
    public void generateGeneralOrder400Error() throws Exception {
        doEmptyCaseDataSetUp();

        mvc.perform(post(generateEndpoint())
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void generateGeneralOrder500Error() throws Exception {
        doValidCaseDataSetUp();
        whenServiceGeneratesDocument().thenThrow(feignError());

        mvc.perform(post(generateEndpoint())
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isInternalServerError());
    }

    @Test
    public void submitGeneralOrderSuccess() throws Exception {
        doValidCaseDataSetUp();
        whenServicePopulatesCollection().thenReturn(caseDataWithGeneralOrder());

        mvc.perform(post(submitEndpoint())
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());
        verify(documentService, times(1)).populateGeneralOrderCollection(any(CaseDetails.class));
    }

    private OngoingStubbing<Map<String, Object>> whenServiceGeneratesDocument() {
        return when(documentService.createGeneralOrder(eq(AUTH_TOKEN), isA(CaseDetails.class)));
    }

    private OngoingStubbing<Map<String, Object>> whenServicePopulatesCollection() {
        return when(documentService.populateGeneralOrderCollection(isA(CaseDetails.class)));
    }
}