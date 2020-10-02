package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ValidateHearingService;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.feignError;

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
        requestContent = objectMapper.readTree(new File(getClass()
                .getResource("/fixtures/contested/validate-hearing-with-fastTrackDecision.json").toURI()));
    }

    @Test
    public void generateHearingDocumentHttpError400() throws Exception {
        mvc.perform(post(GEN_DOC_URL)
                .content("kwuilebge")
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void generateHearingDocumentFormC() throws Exception {
        when(service.generateHearingDocuments(eq(AUTH_TOKEN), isA(CaseDetails.class)))
                .thenReturn(ImmutableMap.of("formC", caseDocument()));

        mvc.perform(post(GEN_DOC_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.formC.document_url", is(DOC_URL)))
                .andExpect(jsonPath("$.data.formC.document_filename", is(FILE_NAME)))
                .andExpect(jsonPath("$.data.formC.document_binary_url", is(BINARY_URL)));

        verify(service, never()).sendFormCAndGForBulkPrint(any(), any());
    }

    @Test
    public void generateHearingDocumentPaperApplication() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/contested/validate-hearing-with-fastTrackDecision-paperApplication.json").toURI()));

        when(service.generateHearingDocuments(eq(AUTH_TOKEN), isA(CaseDetails.class)))
            .thenReturn(ImmutableMap.of("formC", caseDocument()));

        mvc.perform(post(GEN_DOC_URL)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.formC.document_url", is(DOC_URL)))
            .andExpect(jsonPath("$.data.formC.document_filename", is(FILE_NAME)))
            .andExpect(jsonPath("$.data.formC.document_binary_url", is(BINARY_URL)));

        verify(service, times(1)).sendFormCAndGForBulkPrint(isA(CaseDetails.class), eq(AUTH_TOKEN));
    }

    @Test
    public void generateMiniFormAHttpError500() throws Exception {
        when(service.generateHearingDocuments(eq(AUTH_TOKEN), isA(CaseDetails.class)))
                .thenThrow(feignError());

        mvc.perform(post(GEN_DOC_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void generateAdditionalHearingDocumentSuccess() throws Exception {
        doValidCaseDataSetUpForAdditionalHearing();

        mvc.perform(post(GEN_DOC_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());

        verify(service, times(1)).createAndSendAdditionalHearingDocuments(any(), any());
    }
}