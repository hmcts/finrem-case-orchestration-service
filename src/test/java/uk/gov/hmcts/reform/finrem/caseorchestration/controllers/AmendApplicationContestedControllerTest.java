package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import java.io.File;

import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;

@WebMvcTest(AmendApplicationContestedController.class)
public class AmendApplicationContestedControllerTest extends BaseControllerTest {

    private static final String AMEND_APPLICATION_APP_SOL_URL = "/case-orchestration/amend-application-app-sol";
    private static final String AMEND_APPLICATION_APP_URL = "/case-orchestration/amend-application-app";
    private static final String AMEND_APPLICATION_RES_SOL_URL = "/case-orchestration/amend-application-res-sol";
    public static final String AUTH_TOKEN = "tokien:)";

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void shouldValidateApplicantSolicitorPostcodeDetails() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/contested/amend-applicant-solicitor-details-postcode-validation.json").toURI()));

        mvc.perform(post(AMEND_APPLICATION_APP_SOL_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.errors[0]", is("Postcode field is required for applicant solicitor address.")));
    }

    @Test
    public void shouldValidateApplicantPostcodeDetails() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/contested/amend-applicant-solicitor-details-postcode-validation.json").toURI()));

        mvc.perform(post(AMEND_APPLICATION_APP_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.errors[0]", is("Postcode field is required for applicant address.")));
    }

    @Test
    public void shouldValidateRespondentSolicitorPostcodeDetails() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/contested/amend-applicant-solicitor-details-postcode-validation.json").toURI()));

        mvc.perform(post(AMEND_APPLICATION_RES_SOL_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.errors[0]", is("Postcode field is required for respondent solicitor address.")));
    }

    @Test
    public void shouldValidateRespondentPostcodeDetails() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/contested/amend-applicant-solicitor-details-postcode-validation.json").toURI()));

        ((ObjectNode) requestContent.get("case_details").get("case_data")).put("respondentRepresented", "No");


        mvc.perform(post(AMEND_APPLICATION_RES_SOL_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.errors[0]", is("Postcode field is required for respondent address.")));
    }
}