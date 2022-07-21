package uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentGenerationRequest;

import java.util.Collections;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FORM_C;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.OUT_OF_FAMILY_COURT_RESOLUTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.ValidateHearingService.DATE_BETWEEN_6_AND_10_WEEKS;

public class HearingFastTrackDocumentTest extends AbstractDocumentTest {

    private static final String API_URL = "/case-orchestration/ccdAboutToSubmitEvent";

    @Override
    protected DocumentGenerationRequest documentRequest() {
        return DocumentGenerationRequest.builder()
            .template(documentConfiguration.getFormCFastTrackTemplate())
            .fileName(documentConfiguration.getFormCFileName())
            .values(Collections.singletonMap("caseDetails", request.getCaseDetails()))
            .build();
    }

    @Override
    protected String apiUrl() {
        return API_URL;
    }

    @Override
    protected String getTestFixture() {
        return "/fixtures/contested/validate-hearing-with-fastTrackDecision.json";
    }

    @Test
    public void generateFormC() throws Exception {
        generateDocumentServiceSuccessStub();
        generateOutOfFaimlyCourtResolutionDocumentServiceSuccessStub();

        webClient.perform(MockMvcRequestBuilders.post(API_URL)
                .content(objectMapper.writeValueAsString(request))
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(content().json(expectedCaseData()));
    }

    private String expectedCaseData() throws JsonProcessingException {
        CaseDetails caseDetails = request.getCaseDetails();
        caseDetails.getData().put(FORM_C, caseDocument());
        caseDetails.getData().put(OUT_OF_FAMILY_COURT_RESOLUTION, caseDocument());

        return objectMapper.writeValueAsString(
            AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDetails.getData())
                .errors(ImmutableList.of())
                .warnings(ImmutableList.of(DATE_BETWEEN_6_AND_10_WEEKS))
                .build());
    }
}
