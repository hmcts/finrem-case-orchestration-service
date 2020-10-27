package uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentGenerationRequest;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.ValidateHearingService.DATE_BETWEEN_6_AND_10_WEEKS;

public class HearingFastTrackDocumentTest extends AbstractDocumentTest {

    private static final String SUBMIT_SCHEDULING_AND_LISTING_DETAILS = "/case-orchestration/submit-scheduling-and-listing-details";
    private static final String VALIDATE_HEARING =  "/case-orchestration/validate-hearing";

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
        return SUBMIT_SCHEDULING_AND_LISTING_DETAILS;
    }

    @Override
    protected String getTestFixture() {
        return "/fixtures/contested/validate-hearing-with-fastTrackDecision.json";
    }

    @Test
    public void generateFormC() throws Exception {
        generateDocumentServiceSuccessStub();

        webClient.perform(MockMvcRequestBuilders.post(apiUrl())
            .content(objectMapper.writeValueAsString(request))
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(content().json(expectedCaseData()));
    }

    @Test
    public void whenValidatingHearingData_ExpectWarningForHearingDate() throws Exception {
        webClient.perform(MockMvcRequestBuilders.post(VALIDATE_HEARING)
            .content(objectMapper.writeValueAsString(request))
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(APPLICATION_JSON_VALUE)
            .accept(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(content().json(expectedValidateHearingResponseData(emptyList(), ImmutableList.of(DATE_BETWEEN_6_AND_10_WEEKS)), true));
    }

    private String expectedCaseData() throws JsonProcessingException {
        Map<String, Object> caseData = request.getCaseDetails().getData();
        caseData.put("formC", caseDocument());

        return objectMapper.writeValueAsString(
            AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseData)
                .build());
    }

    private String expectedValidateHearingResponseData(List<String> errors, List<String> warnings) throws JsonProcessingException {
        return objectMapper.writeValueAsString(
            AboutToStartOrSubmitCallbackResponse.builder()
                .errors(errors)
                .warnings(warnings)
                .build());
    }
}
