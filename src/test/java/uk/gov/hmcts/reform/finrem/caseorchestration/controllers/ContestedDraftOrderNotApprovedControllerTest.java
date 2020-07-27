package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ContestedDraftOrderNotApprovedService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_APPLICATION_NOT_APPROVED_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_APPLICATION_NOT_APPROVED_JUDGE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_APPLICATION_NOT_APPROVED_JUDGE_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_APPLICATION_NOT_APPROVED_PREVIEW_DOCUMENT;

@WebMvcTest(ContestedDraftOrderNotApprovedController.class)
public class ContestedDraftOrderNotApprovedControllerTest extends BaseControllerTest {

    @MockBean
    private IdamService idamService;

    @MockBean
    private ContestedDraftOrderNotApprovedService contestedDraftOrderNotApprovedService;

    public String startEndpoint() { return "/case-orchestration/contested-application-not-approved-start"; }
    public String previewEndpoint() {
        return "/case-orchestration/documents/preview-general-order";
    }
    public String submitEndpoint() {
        return "/case-orchestration/submit-general-order";
    }

    private String bearerToken = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJqc2NyMGE0M3JnMHU5aGZpNHRva21vdHJ"
        + "vOSIsInN1YiI6IjEiLCJpYXQiOjE1NjAyNDcyNzgsImV4cCI6MTU2MDI2NTI3OCwiZGF0YSI6ImNjZC1pbXBv"
        + "cnQsY2NkLWltcG9ydC1sb2EwIiwidHlwZSI6IkFDQ0VTUyIsImlkIjoiMSIsImZvcmVuYW1lIjoiSW50ZWdyY"
        + "XRpb24iLCJzdXJuYW1lIjoiVGVzdCIsImRlZmF1bHQtc2VydmljZSI6IlByb2JhdGUiLCJsb2EiOjAsImRlZm"
        + "F1bHQtdXJsIjoiaHR0cHM6Ly9sb2NhbGhvc3Q6OTAwMC9wb2MvcHJvYmF0ZSIsImdyb3VwIjoicHJvYmF0ZS1"
        + "wcml2YXRlLWJldGEifQ.sSeejKgphDGyKyNtw---nkFk5N_9iqWb2WYNHCiVRPY";

    @Test
    public void initialiseGeneralOrderPropertiesSuccess() throws Exception {
        refusalOrderStartControllerSetUp();
        when(idamService.getIdamFullName(bearerToken)).thenReturn("User Name");

        mvc.perform(post(startEndpoint())
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, bearerToken)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data." + CONTESTED_APPLICATION_NOT_APPROVED_JUDGE_TYPE, is(nullValue())))
            .andExpect(jsonPath("$.data." + CONTESTED_APPLICATION_NOT_APPROVED_DATE, is(nullValue())))
            .andExpect(jsonPath("$.data." + CONTESTED_APPLICATION_NOT_APPROVED_PREVIEW_DOCUMENT, is(nullValue())))
            .andExpect(jsonPath("$.data." + CONTESTED_APPLICATION_NOT_APPROVED_JUDGE_NAME, is("User Name")));
    }

    @Test
    public void initialiseRefusalOrderPropertiesBadRequest() throws Exception {
        doEmptyCaseDataSetUp();

        mvc.perform(post(startEndpoint())
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, bearerToken)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void initialiseRefusalOrderPropertiesInternalServerError() throws Exception {
        refusalOrderStartControllerSetUp();
        when(idamService.getIdamFullName(bearerToken))
            .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        mvc.perform(post(startEndpoint())
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, bearerToken)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isInternalServerError());
    }

    private void refusalOrderStartControllerSetUp() throws IOException, URISyntaxException {
        ObjectMapper objectMapper = new ObjectMapper();
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/refusal-order-contested.json").toURI()));
    }
}