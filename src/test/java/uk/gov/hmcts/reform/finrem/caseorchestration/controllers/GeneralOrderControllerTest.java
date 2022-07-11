package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralOrderService;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.feignError;

@WebMvcTest(GeneralOrderController.class)
public class GeneralOrderControllerTest extends BaseControllerTest {

    @MockBean private GeneralOrderService documentService;

    public String generateEndpoint() {
        return "/case-orchestration/documents/preview-general-order";
    }

    public String submitEndpoint() {
        return "/case-orchestration/submit-general-order";
    }

    @Test
    public void generateGeneralOrderSuccess() throws Exception {
        doValidCaseDataSetUp();

        mvc.perform(post(generateEndpoint())
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());
        verify(documentService, times(1))
            .createAndSetGeneralOrder(eq(AUTH_TOKEN), isA(FinremCaseDetails.class));
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
        doThrow(feignError()).when(documentService)
            .createAndSetGeneralOrder(eq(AUTH_TOKEN), isA(FinremCaseDetails.class));

        mvc.perform(post(generateEndpoint())
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isInternalServerError());
    }

    @Test
    public void submitGeneralOrderSuccess() throws Exception {
        doValidCaseDataSetUp();

        mvc.perform(post(submitEndpoint())
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());
        verify(documentService, times(1)).populateGeneralOrderCollection(any(FinremCaseDetails.class));
    }
}