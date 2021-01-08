package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ContestedCaseOrderService;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@WebMvcTest(ContestedOrderController.class)
public class ContestedOrderControllerTest extends BaseControllerTest {

    @MockBean
    private ContestedCaseOrderService contestedCaseOrderService;

    private static final String SEND_ORDER_ENDPOINT = "/case-orchestration/contested/send-order";

    private void doCaseDataSetUpWithoutAnyHearingOrder() throws IOException, URISyntaxException {
        ObjectMapper objectMapper = new ObjectMapper();
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/final-order-for-stamping-without-hearing-order.json").toURI()));
    }

    @Test
    public void finalOrder400Error() throws Exception {
        doEmptyCaseDataSetUp();

        mvc.perform(post(SEND_ORDER_ENDPOINT)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void sendOrderSuccess() throws Exception {
        doCaseDataSetUpWithoutAnyHearingOrder();

        ResultActions result = mvc.perform(post(SEND_ORDER_ENDPOINT)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE));

        result.andExpect(status().isOk());
        result.andDo(print());

        verify(contestedCaseOrderService, times(1)).printAndMailHearingDocuments(any(), eq(AUTH_TOKEN));
        verify(contestedCaseOrderService, times(1)).printAndMailHearingDocuments(any(), eq(AUTH_TOKEN));
        verify(contestedCaseOrderService, times(1)).stampFinalOrder(any(), eq(AUTH_TOKEN));
    }
}
