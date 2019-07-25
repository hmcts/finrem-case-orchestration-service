package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ValidateHearingService;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.DOC_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.feignError;

@WebMvcTest(HearingDocumentController.class)
public class HearingDocumentControllerTest extends BaseControllerTest {
    private static final String GEN_DOC_URL = "/case-orchestration/documents/hearing";

    @MockBean
    private HearingDocumentService service;

    @MockBean
    private ValidateHearingService validateHearingService;

    @Before
    public void setUp()  {
        super.setUp();
        try {
            doRequestSetUp();
        } catch (Exception e) {
            fail(e.getMessage());
        }

        when(validateHearingService.validateHearingErrors(isA(CaseDetails.class))).thenReturn(ImmutableList.of());
        when(validateHearingService.validateHearingWarnings(isA(CaseDetails.class))).thenReturn(ImmutableList.of());
    }

    private void doRequestSetUp() throws IOException, URISyntaxException {
        ObjectMapper objectMapper = new ObjectMapper();
        requestContent = objectMapper.readTree(new File(getClass()
                .getResource("/fixtures/contested/validate-hearing-with-fastTrackDecision.json").toURI()));
    }

    @Test
    public void generateHearingDocumentHttpError400() throws Exception {
        mvc.perform(post(GEN_DOC_URL)
                .content("kwuilebge")
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void generateHearingDocumentFormC() throws Exception {
        when(service.generateHearingDocuments(eq(AUTH_TOKEN), isA(CaseDetails.class)))
                .thenReturn(ImmutableMap.of("formC", caseDocument()));

        mvc.perform(post(GEN_DOC_URL)
                .content(requestContent.toString())
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.formC.document_url", is(DOC_URL)))
                .andExpect(jsonPath("$.data.formC.document_filename", is(FILE_NAME)))
                .andExpect(jsonPath("$.data.formC.document_binary_url", is(BINARY_URL)));
    }

    @Test
    public void generateMiniFormAHttpError500() throws Exception {
        when(service.generateHearingDocuments(eq(AUTH_TOKEN), isA(CaseDetails.class)))
                .thenThrow(feignError());

        mvc.perform(post(GEN_DOC_URL)
                .content(requestContent.toString())
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }
}