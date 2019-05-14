package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.GlobalExceptionHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PBAValidationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ValidateHearingService;

import javax.ws.rs.core.MediaType;
import java.io.File;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(ValidateHearingController.class)
public class ValidateHearingControllerTest extends BaseControllerTest {
    private static final String BEARER_TOKEN = "Bearer eyJhbGciOiJIUzI1NiJ9";

    @MockBean
    private PBAValidationService pbaValidationService;

    @MockBean
    private ValidateHearingService validateHearingService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void shouldReturnBadRequestWhenCaseDataIsMissingInRequest() throws Exception {
        doEmtpyCaseDataSetUp();
        mvc.perform(post("/case-orchestration/validate-hearing")
                .content(requestContent.toString())
                .header("Authorization", BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(is(GlobalExceptionHandler.SERVER_ERROR_MSG)));
    }

    @Test
    public void shouldThrowErrorWhenIssueDateAndHearingDateAreEmpty() throws Exception {
        when(validateHearingService.validateHearingErrors(isA(CaseDetails.class)))
                .thenReturn(ImmutableList.of("Issue Date , fast track decision or hearingDate is empty"));

        requestContent = objectMapper.readTree(new File(getClass()
                .getResource("/fixtures/pba-validate.json").toURI()));
        mvc.perform(post("/case-orchestration/validate-hearing")
                .content(requestContent.toString())
                .header("Authorization", BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.errors[0]",
                        Matchers.is("Issue Date , fast track decision or hearingDate is empty")));
    }

    @Test
    public void shouldThrowWarningsWhenNotFastTrackDecision() throws Exception {
        when(validateHearingService.validateHearingErrors(isA(CaseDetails.class))).thenReturn(ImmutableList.of());
        when(validateHearingService.validateHearingWarnings(isA(CaseDetails.class)))
                .thenReturn(ImmutableList.of("Date of the hearing must be between 12 and 14 weeks."));

        requestContent = objectMapper.readTree(new File(getClass()
                .getResource("/fixtures/contested/validate-hearing-withoutfastTrackDecision.json").toURI()));
        mvc.perform(post("/case-orchestration/validate-hearing")
                .content(requestContent.toString())
                .header("Authorization", BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.warnings[0]",
                        Matchers.is("Date of the hearing must be between 12 and 14 weeks.")));
    }

    @Test
    public void shouldThrowWarningsWhenFastTrackDecision() throws Exception {
        when(validateHearingService.validateHearingErrors(isA(CaseDetails.class))).thenReturn(ImmutableList.of());
        when(validateHearingService.validateHearingWarnings(isA(CaseDetails.class)))
                .thenReturn(ImmutableList.of("Date of the Fast Track hearing must be between 6 and 10 weeks."));

        requestContent = objectMapper.readTree(new File(getClass()
                .getResource("/fixtures/contested/validate-hearing-with-fastTrackDecision.json").toURI()));
        mvc.perform(post("/case-orchestration/validate-hearing")
                .content(requestContent.toString())
                .header("Authorization", BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.warnings[0]",
                        Matchers.is("Date of the Fast Track hearing must be between 6 and 10 weeks.")));
    }

    @Test
    public void shouldSuccessfullyValidate() throws Exception {
        when(validateHearingService.validateHearingErrors(isA(CaseDetails.class))).thenReturn(ImmutableList.of());
        when(validateHearingService.validateHearingWarnings(isA(CaseDetails.class))).thenReturn(ImmutableList.of());

        requestContent = objectMapper.readTree(new File(getClass()
                .getResource("/fixtures/contested/validate-hearing-successfully.json").toURI()));
        mvc.perform(post("/case-orchestration/validate-hearing")
                .content(requestContent.toString())
                .header("Authorization", BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.warnings").doesNotExist());
    }
}