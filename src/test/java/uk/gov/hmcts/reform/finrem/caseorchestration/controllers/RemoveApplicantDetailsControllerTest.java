package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import javax.ws.rs.core.MediaType;
import java.io.File;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.YES;

@WebMvcTest(RemoveApplicantDetailsController.class)
public class RemoveApplicantDetailsControllerTest extends BaseControllerTest {
    private static final String BEARER_TOKEN = "Bearer eyJhbGciOiJIUzI1NiJ9";
    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void shouldSuccessfullyRemoveApplicantSolicitorDetails() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
                .getResource("/fixtures/contested/amend-applicant-solicitor-details.json").toURI()));

        mvc.perform(post("/case-orchestration/remove-details")
                .content(requestContent.toString())
                .header("Authorization", BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.applicantRepresented", is(NO)))
                .andExpect(jsonPath("$.data.applicantAddress", is(notNullValue())))
                .andExpect(jsonPath("$.data.applicantPhone", is("89897876765")))
                .andExpect(jsonPath("$.data.applicantEmail", is("email01@email.com")))
                .andExpect(jsonPath("$.data.applicantFMName", is("Test")))
                .andExpect(jsonPath("$.data.applicantLName", is("Test")))

                .andExpect(jsonPath("$.data.applicantSolicitorName").doesNotExist())
                .andExpect(jsonPath("$.data.applicantSolicitorFirm").doesNotExist())
                .andExpect(jsonPath("$.data.solicitorReference").doesNotExist())
                .andExpect(jsonPath("$.data.applicantSolicitorAddress").doesNotExist())
                .andExpect(jsonPath("$.data.applicantSolicitorPhone").doesNotExist())
                .andExpect(jsonPath("$.data.applicantSolicitorEmail").doesNotExist())
                .andExpect(jsonPath("$.data.applicantSolicitorDXnumber").doesNotExist())
                .andExpect(jsonPath("$.data.applicantSolicitorConsentForEmails").doesNotExist());
    }

    @Test
    public void shouldSuccessfullyRemoveApplicantDetails() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
                .getResource("/fixtures/contested/amend-applicant-details.json").toURI()));

        mvc.perform(post("/case-orchestration/remove-details")
                .content(requestContent.toString())
                .header("Authorization", BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.applicantRepresented", is(YES)))
                .andExpect(jsonPath("$.data.applicantAddress") .doesNotExist())
                .andExpect(jsonPath("$.data.applicantPhone").doesNotExist())
                .andExpect(jsonPath("$.data.applicantEmail").doesNotExist())

                .andExpect(jsonPath("$.data.applicantFMName", is("Poor")))
                .andExpect(jsonPath("$.data.applicantLName", is("Guy")))
                .andExpect(jsonPath("$.data.applicantSolicitorName", is("SolName99")))
                .andExpect(jsonPath("$.data.applicantSolicitorFirm", is("SolFirm99")))
                .andExpect(jsonPath("$.data.solicitorReference", is("SolRef99")))
                .andExpect(jsonPath("$.data.applicantSolicitorAddress").exists())
                .andExpect(jsonPath("$.data.applicantSolicitorPhone", is("89897876765")))
                .andExpect(jsonPath("$.data.applicantSolicitorEmail", is("emailSol99@email.com")))
                .andExpect(jsonPath("$.data.applicantSolicitorDXnumber").exists())
                .andExpect(jsonPath("$.data.applicantSolicitorConsentForEmails", is("No")));
    }
}