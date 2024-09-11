package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnlineFormDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.nocworkflows.UpdateRepresentationWorkflowService;

import java.io.File;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@SpringBootTest
public class UpdateContactDetailsControllerTest extends BaseControllerTest {

    private static final String REMOVE_DETAILS_URL = "/case-orchestration/remove-details";

    @MockBean
    private UpdateRepresentationWorkflowService handleNocWorkflowService;

    @MockBean
    private OnlineFormDocumentService onlineFormDocumentService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void givenRespondentIsNotRepresented_whenRemoveDetailsIsCalled_thenRemoveRespondentSolicitorDetails() throws Exception {
        JsonNode requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/contested/respondent-not-represented.json").toURI()));
        when(onlineFormDocumentService.generateContestedMiniFormA(any(), any())).thenReturn(TestSetUpUtils.caseDocument());

        mvc.perform(MockMvcRequestBuilders.post(REMOVE_DETAILS_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.case_details.case_data.respondentAddress", is("123 Respondent St")))
            .andExpect(jsonPath("$.data.respondentPhone", is("07123456789")))
            .andExpect(jsonPath("$.data.respondentEmail", is("respondent@example.com")))
            .andExpect(jsonPath("$.data.rSolicitorName").doesNotExist())
            .andExpect(jsonPath("$.data.rSolicitorFirm").doesNotExist())
            .andExpect(jsonPath("$.data.rSolicitorAddress").doesNotExist())
            .andExpect(jsonPath("$.data.rSolicitorPhone").doesNotExist())
            .andExpect(jsonPath("$.data.rSolicitorEmail").doesNotExist())
            .andExpect(jsonPath("$.data.rSolicitorDXNumber").doesNotExist())
            .andExpect(jsonPath("$.data.rSolicitorConsentForEmails").doesNotExist());

        verify(onlineFormDocumentService).generateContestedMiniFormA(any(), any());
//        ArgumentCaptor<CaseDetails> caseDetailsArgument = ArgumentCaptor.forClass(CaseDetails.class);
//        verify(handleNocWorkflowService, times(1)).handleNoticeOfChangeWorkflow(caseDetailsArgument.capture(),
//            anyString(), any(CaseDetails.class));
    }
}