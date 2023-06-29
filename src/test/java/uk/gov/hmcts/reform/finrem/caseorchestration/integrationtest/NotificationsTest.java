package uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.CaseOrchestrationApplication;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignmentUserRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignmentUserRolesResource;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.EmailService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PrdOrganisationService;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_ASSIGNED_TO_JUDGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONSENTED_NOTICE_OF_CHANGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONSENT_ORDER_AVAILABLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONSENT_ORDER_NOT_APPROVED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_HWF_SUCCESSFUL;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = CaseOrchestrationApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.properties")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Slf4j
@Category(IntegrationTest.class)
public class NotificationsTest extends BaseTest {

    @MockBean
    GenericDocumentService genericDocumentService;
    @MockBean
    BulkPrintService bulkPrintService;
    @MockBean
    PrdOrganisationService prdOrganisationService;
    @MockBean
    AssignCaseAccessService assignCaseAccessService;
    @MockBean
    EmailService emailService;

    private static final String HWF_SUCCESSFUL_URL = "/case-orchestration/notify/hwf-successful";
    private static final String CONSENT_ORDER_AVAILABLE_URL = "/case-orchestration/notify/consent-order-available";
    private static final String CONSENT_ORDER_NOT_APPROVED_URL = "/case-orchestration/notify/order-not-approved";
    private static final String ASSIGNED_TO_JUDGE_URL = "/case-orchestration/notify/assign-to-judge";
    private static final String NOTICE_OF_CHANGE_URL = "/case-orchestration/notify/notice-of-change";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc webClient;

    @ClassRule
    public static WireMockClassRule notificationService = new WireMockClassRule(8086);

    private CallbackRequest request;

    @Before
    public void setUp() throws IOException {
        try (InputStream resourceAsStream = getClass().getResourceAsStream("/fixtures/consented-ccd-request-with-solicitor-agreed-to-emails.json")) {
            request = objectMapper.readValue(resourceAsStream, CallbackRequest.class);
        }
    }

    @Test
    public void notifyHwfSuccessful() throws Exception {
        when(assignCaseAccessService.searchUserRoles(any())).thenReturn(CaseAssignmentUserRolesResource.builder()
            .caseAssignmentUserRoles(List.of(CaseAssignmentUserRole.builder().caseRole(APP_SOLICITOR_POLICY).build()))
            .build());
        webClient.perform(MockMvcRequestBuilders.post(HWF_SUCCESSFUL_URL)
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(content().json(expectedCaseData()));
        Mockito.verify(emailService).sendConfirmationEmail(any(), eq(FR_HWF_SUCCESSFUL));
    }

    @Test
    public void notifyConsentOrderAvailable() throws Exception {
        when(assignCaseAccessService.searchUserRoles(any())).thenReturn(CaseAssignmentUserRolesResource.builder()
            .caseAssignmentUserRoles(List.of(CaseAssignmentUserRole.builder().caseRole(APP_SOLICITOR_POLICY).build()))
            .build());
        webClient.perform(MockMvcRequestBuilders.post(CONSENT_ORDER_AVAILABLE_URL)
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(content().json(expectedCaseData()));
        Mockito.verify(emailService).sendConfirmationEmail(any(), eq(FR_CONSENT_ORDER_AVAILABLE));

    }

    @Test
    public void notifyConsentOrderNotApproved() throws Exception {
        when(assignCaseAccessService.searchUserRoles(any())).thenReturn(CaseAssignmentUserRolesResource.builder()
            .caseAssignmentUserRoles(List.of(CaseAssignmentUserRole.builder().caseRole(APP_SOLICITOR_POLICY).build()))
            .build());
        webClient.perform(MockMvcRequestBuilders.post(CONSENT_ORDER_NOT_APPROVED_URL)
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(content().json(expectedCaseData()));
        Mockito.verify(emailService).sendConfirmationEmail(any(), eq(FR_CONSENT_ORDER_NOT_APPROVED));
    }

    @Test
    public void notifyAssignToJudge() throws Exception {
        when(assignCaseAccessService.searchUserRoles(any())).thenReturn(CaseAssignmentUserRolesResource.builder()
            .caseAssignmentUserRoles(List.of(CaseAssignmentUserRole.builder().caseRole(APP_SOLICITOR_POLICY).build()))
            .build());
        when(assignCaseAccessService.searchUserRoles(any())).thenReturn(CaseAssignmentUserRolesResource.builder()
            .caseAssignmentUserRoles(List.of(CaseAssignmentUserRole.builder().caseRole(RESP_SOLICITOR_POLICY).build()))
            .build());
        webClient.perform(MockMvcRequestBuilders.post(ASSIGNED_TO_JUDGE_URL)
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(content().json(expectedCaseData()));
        Mockito.verify(emailService).sendConfirmationEmail(any(), eq(FR_ASSIGNED_TO_JUDGE));
    }

    @Test
    public void notifyNoticeOfChange() throws Exception {
        when(assignCaseAccessService.searchUserRoles(any())).thenReturn(CaseAssignmentUserRolesResource.builder()
            .caseAssignmentUserRoles(List.of(CaseAssignmentUserRole.builder().caseRole(APP_SOLICITOR_POLICY).build()))
            .build());
        webClient.perform(MockMvcRequestBuilders.post(NOTICE_OF_CHANGE_URL)
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(content().json(expectedCaseData()));
        Mockito.verify(emailService).sendConfirmationEmail(any(), eq(FR_CONSENTED_NOTICE_OF_CHANGE));
    }

    private String expectedCaseData() throws JsonProcessingException {
        CaseDetails caseDetails = request.getCaseDetails();
        return objectMapper.writeValueAsString(AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData()).build());
    }

}
