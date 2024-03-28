package uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest.createcase;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.CcdCallbackController;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.solicitorcreatecase.SolicitorCreateContestedAboutToSubmitHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CallbackDispatchService;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@ExtendWith(SpringExtension.class)
@WebMvcTest
@ContextConfiguration(classes = {
    CcdCallbackController.class,
    CreateCaseMandatoryDataTestConfiguration.class,
    CallbackDispatchService.class,
    SolicitorCreateContestedAboutToSubmitHandler.class,
    ObjectMapper.class, FinremCaseDetailsMapper.class
})
class CreateCaseMandatoryDataIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateCaseValidateMandatoryData() throws Exception {
        FinremCallbackRequest request = FinremCallbackRequest.builder()
            .eventType(EventType.SOLICITOR_CREATE)
            .caseDetails(FinremCaseDetails.builder()
                .caseType(CaseType.CONTESTED)
                .data(FinremCaseData.builder().build())
                .build())
            .build();

        mockMvc.perform(post("/case-orchestration/ccdAboutToSubmitEvent")
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", notNullValue()))
            .andExpect(jsonPath("$.warnings",empty()))
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.errors",
                hasItem("Applicant's Local Court is required. Update Please choose the Region in which the Applicant resides")));

    }
}
