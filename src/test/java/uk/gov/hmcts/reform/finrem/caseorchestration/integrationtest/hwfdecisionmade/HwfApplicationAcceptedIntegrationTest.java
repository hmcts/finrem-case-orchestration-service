package uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest.hwfdecisionmade;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.client.EmailClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseAssignedRoleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.DocmosisPdfGenerationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;

import java.util.Map;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest.IntegrationTestUtils.givenSearchUserRoles;
import static uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest.IntegrationTestUtils.performCallback;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_HWF_SUCCESSFUL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_HWF_SUCCESSFUL;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class HwfApplicationAcceptedIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private CaseAssignedRoleService caseAssignedRoleService;
    @MockitoBean
    private SystemUserService systemUserService;
    @MockitoBean
    private AssignCaseAccessService assignCaseAccessService;
    @MockitoBean
    private EmailClient emailClient;
    @MockitoBean
    private DocmosisPdfGenerationService  docmosisPdfGenerationService;
    @MockitoBean
    private IdamService idamService;

    @Value("#{${uk.gov.notify.email.templates}}")
    private Map<String, String> emailTemplates;

    @Test
    void givenContestedCaseWhenHandleThenSendsEmail() throws Exception {
        givenSearchUserRoles(assignCaseAccessService, CaseRole.APP_SOLICITOR);

        performCallback(mockMvc, "/case-orchestration/notify/hwf-successful", jsonFile("contested-submitted.json"))
            .andExpect(status().isOk());

        String expectedTemplateId = emailTemplates.get(FR_CONTESTED_HWF_SUCCESSFUL.name());
        String expectedEmailAddress = "fr_applicant_solicitor1@mailinator.com";

        Mockito.verify(emailClient).sendEmail(eq(expectedTemplateId), eq(expectedEmailAddress),
            anyMap(), anyString(), isNull(String.class));

    }

    @Test
    void givenConsentedCaseWhenHandleThenSendsEmail() throws Exception {
        givenSearchUserRoles(assignCaseAccessService, CaseRole.APP_SOLICITOR);
        byte[] expectedBytes = new byte[] {1, 2, 3, 4};
        when(docmosisPdfGenerationService.generateDocFrom(anyString(),anyMap())).thenReturn(expectedBytes);

        performCallback(mockMvc, "/case-orchestration/notify/hwf-successful", jsonFile("consented-submitted.json"))
            .andExpect(status().isOk());

        String expectedTemplateId = emailTemplates.get(FR_HWF_SUCCESSFUL.name());
        String expectedEmailAddress = "fr_applicant_solicitor1@mailinator.com";

        Mockito.verify(emailClient).sendEmail(eq(expectedTemplateId), eq(expectedEmailAddress),
            anyMap(), anyString(), isNull(String.class));

    }

    private String jsonFile(String jsonFilename) {
        return "/fixtures/integrationtests/HwfDecisionMade/" + jsonFilename;
    }
}
