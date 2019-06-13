package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GeneralLetterStartController.class)
public class GeneralLetterStartControllerTest extends BaseControllerTest {

    @MockBean
    private IdamService idamService;

    private String bearerToken = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJqc2NyMGE0M3JnMHU5aGZpNHRva21vdHJ"
           + "vOSIsInN1YiI6IjEiLCJpYXQiOjE1NjAyNDcyNzgsImV4cCI6MTU2MDI2NTI3OCwiZGF0YSI6ImNjZC1pbXBv"
           + "cnQsY2NkLWltcG9ydC1sb2EwIiwidHlwZSI6IkFDQ0VTUyIsImlkIjoiMSIsImZvcmVuYW1lIjoiSW50ZWdyY"
           + "XRpb24iLCJzdXJuYW1lIjoiVGVzdCIsImRlZmF1bHQtc2VydmljZSI6IlByb2JhdGUiLCJsb2EiOjAsImRlZm"
           + "F1bHQtdXJsIjoiaHR0cHM6Ly9sb2NhbGhvc3Q6OTAwMC9wb2MvcHJvYmF0ZSIsImdyb3VwIjoicHJvYmF0ZS1"
           + "wcml2YXRlLWJldGEifQ.sSeejKgphDGyKyNtw---nkFk5N_9iqWb2WYNHCiVRPY";

    @Test
    public void initialiseGeneralLetterPropertiesSuccess() throws Exception {
        generalLetterStartControllerSetUp();
        when(idamService.getIdamFullName(bearerToken)).thenReturn("Integration Test");

        mvc.perform(post("/case-orchestration/general-letter-start")
                .content(requestContent.toString())
                .header("Authorization", bearerToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.generalLetterAddressTo", is(nullValue())))
                .andExpect(jsonPath("$.data.generalLetterRecipient", is(nullValue())))
                .andExpect(jsonPath("$.data.generalLetterRecipientAddress", is(nullValue())))
                .andExpect(jsonPath("$.data.generalLetterCreatedBy", is("Integration Test")))
                .andExpect(jsonPath("$.data.generalLetterBody", is(nullValue())));
    }

    @Test
    public void initialiseGeneralLetterPropertiesBadRequest() throws Exception {
        doEmtpyCaseDataSetUp();

        mvc.perform(post("/case-orchestration/general-letter-start")
                .content(requestContent.toString())
                .header("Authorization", bearerToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void initialiseGeneralLetterPropertiesInternalServerError() throws Exception {
        generalLetterStartControllerSetUp();
        when(idamService.getIdamFullName(bearerToken))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        mvc.perform(post("/case-orchestration/general-letter-start")
                .content(requestContent.toString())
                .header("Authorization", bearerToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    private void generalLetterStartControllerSetUp() throws IOException, URISyntaxException {
        ObjectMapper objectMapper = new ObjectMapper();
        requestContent = objectMapper.readTree(new File(getClass()
                .getResource("/fixtures/general-letter.json").toURI()));
    }
}