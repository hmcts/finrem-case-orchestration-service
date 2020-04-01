package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.GlobalExceptionHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PBAValidationService;

import java.io.File;

import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@WebMvcTest(PBAValidateController.class)
public class PBAValidateControllerTest extends BaseControllerTest {

    private static final String PBA_NUMBER = "PBA123";
    private static final String PBA_VALIDATE_URL = "/case-orchestration/pba-validate";

    @MockBean
    private PBAValidationService pbaValidationService;

    @Test
    public void shouldReturnBadRequestWhenCaseDataIsMissingInRequest() throws Exception {
        doEmptyCaseDataSetUp();
        mvc.perform(post(PBA_VALIDATE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(is(GlobalExceptionHandler.SERVER_ERROR_MSG)));
    }

    private void doValidatePBASetUp(boolean isValidPBA) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        requestContent = objectMapper.readTree(new File(getClass()
                .getResource("/fixtures/pba-validate.json").toURI()));

        when(pbaValidationService.isValidPBA(AUTH_TOKEN, PBA_NUMBER)).thenReturn(isValidPBA);
    }

    private void doHWFSetUp() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        requestContent = objectMapper.readTree(new File(getClass()
                .getResource("/fixtures/hwf.json").toURI()));
    }

    @Test
    public void shouldNotDoPBAValidationWhenPaymentIsDoneWithHWF() throws Exception {
        doHWFSetUp();
        mvc.perform(post(PBA_VALIDATE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors", is(emptyOrNullString())))
                .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
        verify(pbaValidationService, never()).isValidPBA(anyString(), anyString());
    }

    @Test
    public void shouldReturnErrorWhenPbaValidationFails() throws Exception {
        doValidatePBASetUp(false);
        mvc.perform(post(PBA_VALIDATE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
        verify(pbaValidationService, times(1)).isValidPBA(AUTH_TOKEN, PBA_NUMBER);
    }

    @Test
    public void shouldDoPbaValidation() throws Exception {
        doValidatePBASetUp(true);
        mvc.perform(post(PBA_VALIDATE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors", is(emptyOrNullString())))
                .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
        verify(pbaValidationService, times(1)).isValidPBA(AUTH_TOKEN, PBA_NUMBER);
    }
}