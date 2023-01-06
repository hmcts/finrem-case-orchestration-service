package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnlineFormDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.nocworkflows.UpdateRepresentationWorkflowService;

import java.io.File;
import java.util.LinkedHashMap;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;

@WebMvcTest(RemoveApplicantDetailsController.class)
public class RemoveApplicantDetailsControllerTest extends BaseControllerTest {

    private static final String REMOVE_DETAILS_URL = "/case-orchestration/remove-details";
    public static final String AUTH_TOKEN = "tokien:)";
    @MockBean
    protected UpdateRepresentationWorkflowService handleNocWorkflowService;

    @MockBean
    protected OnlineFormDocumentService service;

    @MockBean
    protected FeatureToggleService featureToggleService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void shouldSuccessfullyRemoveApplicantSolicitorDetails() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/contested/amend-applicant-solicitor-details.json").toURI()));
        when(featureToggleService.isCaseworkerNoCEnabled()).thenReturn(true);
        when(service.generateContestedMiniFormA(any(), any())).thenReturn(TestSetUpUtils.caseDocument());

        mvc.perform(post(REMOVE_DETAILS_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.data.applicantRepresented", is(NO_VALUE)))
            .andExpect(jsonPath("$.data.applicantAddress", is(notNullValue())))
            .andExpect(jsonPath("$.data.applicantPhone", is("89897876765")))
            .andExpect(jsonPath("$.data.applicantEmail", is("email01@email.com")))
            .andExpect(jsonPath("$.data.applicantFMName", is("Poor")))
            .andExpect(jsonPath("$.data.applicantLName", is("Guy")))
            .andExpect(jsonPath("$.data.applicantAddressConfidential", is("Yes")))
            .andExpect(jsonPath("$.data.respondentAddressConfidential", is("No")))
            .andExpect(jsonPath("$.data.miniFormA", isA(LinkedHashMap.class)))
            .andExpect(jsonPath("$.data.applicantSolicitorName").doesNotExist())
            .andExpect(jsonPath("$.data.applicantSolicitorFirm").doesNotExist())
            .andExpect(jsonPath("$.data.solicitorReference").doesNotExist())
            .andExpect(jsonPath("$.data.applicantSolicitorAddress").doesNotExist())
            .andExpect(jsonPath("$.data.applicantSolicitorPhone").doesNotExist())
            .andExpect(jsonPath("$.data.applicantSolicitorEmail").doesNotExist())
            .andExpect(jsonPath("$.data.applicantSolicitorDXnumber").doesNotExist())
            .andExpect(jsonPath("$.data.applicantSolicitorConsentForEmails").doesNotExist());

        verify(service).generateContestedMiniFormA(any(), any());
    }


    @Test
    public void shouldSuccessfullyRemoveApplicantSolicitorDetails_respondentConfidentialAddressNotAmended() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/contested/amend-applicant-solicitor-details-res-untouched.json").toURI()));
        when(featureToggleService.isCaseworkerNoCEnabled()).thenReturn(true);
        when(service.generateContestedMiniFormA(any(), any())).thenReturn(TestSetUpUtils.caseDocument());

        mvc.perform(post(REMOVE_DETAILS_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.data.applicantRepresented", is(NO_VALUE)))
            .andExpect(jsonPath("$.data.applicantAddress", is(notNullValue())))
            .andExpect(jsonPath("$.data.applicantPhone", is("89897876765")))
            .andExpect(jsonPath("$.data.applicantEmail", is("email01@email.com")))
            .andExpect(jsonPath("$.data.applicantFMName", is("Poor")))
            .andExpect(jsonPath("$.data.applicantLName", is("Guy")))
            .andExpect(jsonPath("$.data.applicantAddressConfidential", is("Yes")))
            .andExpect(jsonPath("$.data.respondentAddressConfidential").doesNotExist())
            .andExpect(jsonPath("$.data.miniFormA", isA(LinkedHashMap.class)))
            .andExpect(jsonPath("$.data.applicantSolicitorName").doesNotExist())
            .andExpect(jsonPath("$.data.applicantSolicitorFirm").doesNotExist())
            .andExpect(jsonPath("$.data.solicitorReference").doesNotExist())
            .andExpect(jsonPath("$.data.applicantSolicitorAddress").doesNotExist())
            .andExpect(jsonPath("$.data.applicantSolicitorPhone").doesNotExist())
            .andExpect(jsonPath("$.data.applicantSolicitorEmail").doesNotExist())
            .andExpect(jsonPath("$.data.applicantSolicitorDXnumber").doesNotExist())
            .andExpect(jsonPath("$.data.applicantSolicitorConsentForEmails").doesNotExist());

        verify(service).generateContestedMiniFormA(any(), any());
    }

    @Test
    public void shouldSuccessfullyRemoveApplicantSolicitorDetails_applicantConfidentialAddressNotAmended() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/contested/amend-applicant-solicitor-details-app-untouched.json").toURI()));
        when(featureToggleService.isCaseworkerNoCEnabled()).thenReturn(true);
        when(service.generateContestedMiniFormA(any(), any())).thenReturn(TestSetUpUtils.caseDocument());

        mvc.perform(post(REMOVE_DETAILS_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.data.applicantRepresented", is(NO_VALUE)))
            .andExpect(jsonPath("$.data.applicantAddress", is(notNullValue())))
            .andExpect(jsonPath("$.data.applicantPhone", is("89897876765")))
            .andExpect(jsonPath("$.data.applicantEmail", is("email01@email.com")))
            .andExpect(jsonPath("$.data.applicantFMName", is("Poor")))
            .andExpect(jsonPath("$.data.applicantLName", is("Guy")))
            .andExpect(jsonPath("$.data.applicantAddressConfidential").doesNotExist())
            .andExpect(jsonPath("$.data.respondentAddressConfidential", is("Yes")))
            .andExpect(jsonPath("$.data.miniFormA", isA(LinkedHashMap.class)))
            .andExpect(jsonPath("$.data.applicantSolicitorName").doesNotExist())
            .andExpect(jsonPath("$.data.applicantSolicitorFirm").doesNotExist())
            .andExpect(jsonPath("$.data.solicitorReference").doesNotExist())
            .andExpect(jsonPath("$.data.applicantSolicitorAddress").doesNotExist())
            .andExpect(jsonPath("$.data.applicantSolicitorPhone").doesNotExist())
            .andExpect(jsonPath("$.data.applicantSolicitorEmail").doesNotExist())
            .andExpect(jsonPath("$.data.applicantSolicitorDXnumber").doesNotExist())
            .andExpect(jsonPath("$.data.applicantSolicitorConsentForEmails").doesNotExist());

        verify(service).generateContestedMiniFormA(any(), any());
    }

    @Test
    public void shouldSuccessfullyRemoveApplicantSolicitorDetails_bothConfidentialAddressNotAmended() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/contested/amend-applicant-solicitor-details-both-untouched.json").toURI()));
        when(featureToggleService.isCaseworkerNoCEnabled()).thenReturn(true);

        mvc.perform(post(REMOVE_DETAILS_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.data.applicantRepresented", is(NO_VALUE)))
            .andExpect(jsonPath("$.data.applicantAddress", is(notNullValue())))
            .andExpect(jsonPath("$.data.applicantPhone", is("89897876765")))
            .andExpect(jsonPath("$.data.applicantEmail", is("email01@email.com")))
            .andExpect(jsonPath("$.data.applicantFMName", is("Poor")))
            .andExpect(jsonPath("$.data.applicantLName", is("Guy")))
            .andExpect(jsonPath("$.data.applicantAddressConfidential").doesNotExist())
            .andExpect(jsonPath("$.data.respondentAddressConfidential").doesNotExist())
            .andExpect(jsonPath("$.data.applicantSolicitorName").doesNotExist())
            .andExpect(jsonPath("$.data.applicantSolicitorFirm").doesNotExist())
            .andExpect(jsonPath("$.data.solicitorReference").doesNotExist())
            .andExpect(jsonPath("$.data.applicantSolicitorAddress").doesNotExist())
            .andExpect(jsonPath("$.data.applicantSolicitorPhone").doesNotExist())
            .andExpect(jsonPath("$.data.applicantSolicitorEmail").doesNotExist())
            .andExpect(jsonPath("$.data.applicantSolicitorDXnumber").doesNotExist())
            .andExpect(jsonPath("$.data.applicantSolicitorConsentForEmails").doesNotExist());

        verify(service, never()).generateContestedMiniFormA(any(), any());
    }

}