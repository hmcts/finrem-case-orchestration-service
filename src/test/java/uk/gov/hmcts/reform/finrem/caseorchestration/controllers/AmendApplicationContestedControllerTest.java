package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import java.io.File;
import java.util.Objects;

import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;

@WebMvcTest(AmendApplicationContestedController.class)
public class AmendApplicationContestedControllerTest extends BaseControllerTest {

    private static final String AMEND_APPLICATION_APP_SOL_URL = "/case-orchestration/amend-application-validate-applicant-solicitor-address";
    private static final String AMEND_APPLICATION_APP_URL = "/case-orchestration/amend-application-validate-applicant-address";
    private static final String AMEND_APPLICATION_RES_SOL_URL = "/case-orchestration/amend-application-validate-respondent-solicitor";
    public static final String AUTH_TOKEN = "tokien:)";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void givenInvalidApplicantSolicitorPostcode_whenAmendApplication_thenReturnValidationError() throws Exception {
        requestContent = objectMapper.readTree(new File(Objects.requireNonNull(getClass()
            .getResource("/fixtures/contested/amend-applicant-solicitor-details-postcode-validation.json")).toURI()));

        mvc.perform(post(AMEND_APPLICATION_APP_SOL_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.errors[0]", is("Postcode field is required for applicant solicitor address.")));
    }

    @Test
    public void givenInvalidApplicantPostcode_whenAmendApplication_thenReturnValidationError() throws Exception {
        requestContent = objectMapper.readTree(new File(Objects.requireNonNull(getClass()
            .getResource("/fixtures/contested/amend-applicant-solicitor-details-postcode-validation.json")).toURI()));

        mvc.perform(post(AMEND_APPLICATION_APP_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.errors[0]", is("Postcode field is required for applicant address.")));
    }

    @Test
    public void givenInvalidRespondentSolicitorPostcode_whenAmendApplication_thenReturnValidationError() throws Exception {
        requestContent = objectMapper.readTree(new File(Objects.requireNonNull(getClass()
            .getResource("/fixtures/contested/amend-applicant-solicitor-details-postcode-validation.json")).toURI()));

        mvc.perform(post(AMEND_APPLICATION_RES_SOL_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.errors[0]", is("Postcode field is required for respondent solicitor address.")));
    }

    @Test
    public void givenInvalidRespondentPostcode_whenAmendApplication_thenReturnValidationError() throws Exception {
        requestContent = objectMapper.readTree(new File(Objects.requireNonNull(getClass()
            .getResource("/fixtures/contested/amend-applicant-solicitor-details-postcode-validation.json")).toURI()));

        ((ObjectNode) requestContent.get("case_details").get("case_data")).put("respondentRepresented", "No");


        mvc.perform(post(AMEND_APPLICATION_RES_SOL_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.errors[0]", is("Postcode field is required for respondent address.")));
    }

    @Test
    public void givenValidApplicantSolicitorPostcode_whenAmendApplication_thenReturnSuccess() throws Exception {
        requestContent = objectMapper.readTree(new File(Objects.requireNonNull(getClass()
            .getResource("/fixtures/contested/amend-applicant-solicitor-details-postcode-validation.json")).toURI()));

        ((ObjectNode) requestContent.get("case_details").get("case_data").get("applicantSolicitorAddress")).put("PostCode", "AB12 3CD");

        mvc.perform(post(AMEND_APPLICATION_APP_SOL_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errors", hasSize(0)));
    }

    @Test
    public void givenValidApplicantPostcode_whenAmendApplication_thenReturnSuccess() throws Exception {
        requestContent = objectMapper.readTree(new File(Objects.requireNonNull(getClass()
            .getResource("/fixtures/contested/amend-applicant-solicitor-details-postcode-validation.json")).toURI()));

        ((ObjectNode) requestContent.get("case_details").get("case_data").get("applicantAddress")).put("PostCode", "AB12 3CD");

        mvc.perform(post(AMEND_APPLICATION_APP_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errors", hasSize(0)));
    }

    @Test
    public void givenValidRespondentSolicitorPostcode_whenAmendApplication_thenReturnSuccess() throws Exception {
        requestContent = objectMapper.readTree(new File(Objects.requireNonNull(getClass()
            .getResource("/fixtures/contested/amend-applicant-solicitor-details-postcode-validation.json")).toURI()));

        ((ObjectNode) requestContent.get("case_details").get("case_data").get("rSolicitorAddress")).put("PostCode", "AB12 3CD");

        mvc.perform(post(AMEND_APPLICATION_RES_SOL_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errors", hasSize(0)));
    }

    @Test
    public void givenValidRespondentPostcode_whenAmendApplication_thenReturnSuccess() throws Exception {
        requestContent = objectMapper.readTree(new File(Objects.requireNonNull(getClass()
            .getResource("/fixtures/contested/amend-applicant-solicitor-details-postcode-validation.json")).toURI()));

        ((ObjectNode) requestContent.get("case_details").get("case_data")).put("respondentRepresented", "No");
        ((ObjectNode) requestContent.get("case_details").get("case_data").get("respondentAddress")).put("PostCode", "AB12 3CD");

        mvc.perform(post(AMEND_APPLICATION_RES_SOL_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errors", hasSize(0)));
    }
}