package uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest.managebarrister;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.client.EmailClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseAssignedRoleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PrdOrganisationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.sendletter.api.SendLetterApi;

import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest.IntegrationTestUtils.givenCurrentUserHasName;
import static uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest.IntegrationTestUtils.givenCurrentUserHasRole;
import static uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest.IntegrationTestUtils.givenProfessionalUserWithEmail;
import static uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest.IntegrationTestUtils.givenSolicitorsHaveCaseAccess;
import static uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest.IntegrationTestUtils.givenSystemUserService;
import static uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest.IntegrationTestUtils.performAboutToStartCallback;
import static uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest.IntegrationTestUtils.performAboutToSubmitCallback;
import static uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest.IntegrationTestUtils.performMidEventCallback;
import static uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest.IntegrationTestUtils.performSubmittedCallback;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASEWORKER_ROLE_FIELD_SHOW_LABEL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_BARRISTER_ACCESS_ADDED;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class ManageBarristerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private CaseAssignedRoleService caseAssignedRoleService;
    @MockitoBean
    private SystemUserService systemUserService;
    @MockitoBean
    private PrdOrganisationService prdOrganisationService;
    @MockitoBean
    private AssignCaseAccessService assignCaseAccessService;
    @MockitoBean
    private IdamService idamService;
    @MockitoBean
    private EmailClient emailClient;
    @MockitoBean
    private SendLetterApi sendLetterApi;

    @Value("#{${uk.gov.notify.email.templates}}")
    private Map<String, String> emailTemplates;

    @ParameterizedTest
    @MethodSource
    void givenNoBarristersOnCase_whenAboutToStart_thenEventIsInitialised(CaseRole currentUserCaseRole,
                                                                         String expectedCaseUserRoleLabel) throws Exception {
        givenCurrentUserHasRole(caseAssignedRoleService, currentUserCaseRole);

        performAboutToStartCallback(mockMvc, jsonFile("no-barristers-caseworker-about-to-start.json"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.barristerParty").doesNotExist())
            .andExpect(jsonPath("$.data.currentUserCaseRole").value(currentUserCaseRole.getCcdCode()))
            .andExpect(jsonPath("$.data.currentUserCaseRoleLabel").value(expectedCaseUserRoleLabel))
            .andReturn();
    }

    private static Stream<Arguments> givenNoBarristersOnCase_whenAboutToStart_thenEventIsInitialised() {
        return Stream.of(
            Arguments.of(CaseRole.CASEWORKER, CASEWORKER_ROLE_FIELD_SHOW_LABEL),
            Arguments.of(CaseRole.APP_SOLICITOR, "APPSOLICITOR"),
            Arguments.of(CaseRole.RESP_SOLICITOR, "RESPSOLICITOR"),
            Arguments.of(CaseRole.INTVR_SOLICITOR_1, "INTVRSOLICITOR1"),
            Arguments.of(CaseRole.INTVR_SOLICITOR_2, "INTVRSOLICITOR2"),
            Arguments.of(CaseRole.INTVR_SOLICITOR_3, "INTVRSOLICITOR3"),
            Arguments.of(CaseRole.INTVR_SOLICITOR_4, "INTVRSOLICITOR4")
        );
    }

    @Test
    void givenCaseworkerAndNoBarristersOnCase_whenMidEvent_thenNoWarningsOrErrors() throws Exception {
        givenSolicitorsHaveCaseAccess(assignCaseAccessService, CaseRole.APP_SOLICITOR, CaseRole.RESP_SOLICITOR);
        givenCurrentUserHasRole(caseAssignedRoleService, CaseRole.CASEWORKER);
        givenProfessionalUserWithEmail(prdOrganisationService, "fr_applicant_barrister1@mailinator.com");
        givenSystemUserService(systemUserService);

        performMidEventCallback(mockMvc, jsonFile("no-barristers-caseworker-mid-event.json"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.barristerParty").value(BarristerParty.APPLICANT.getValue()))
            .andExpect(jsonPath("$.data.appBarristerCollection", hasSize(1)))
            .andExpect(jsonPath("$.warnings").isEmpty())
            .andExpect(jsonPath("$.errors").isEmpty())
            .andReturn();
    }

    @Test
    void givenCaseworkerAndNoBarristersOnCase_whenAboutToSubmitEvent_thenNoWarningsOrErrors() throws Exception {
        givenSolicitorsHaveCaseAccess(assignCaseAccessService, CaseRole.APP_SOLICITOR, CaseRole.RESP_SOLICITOR);
        givenCurrentUserHasRole(caseAssignedRoleService, CaseRole.CASEWORKER);
        givenCurrentUserHasName(idamService, "FR Caseworker");
        givenProfessionalUserWithEmail(prdOrganisationService, "fr_applicant_barrister1@mailinator.com");
        givenSystemUserService(systemUserService);

        performAboutToSubmitCallback(mockMvc, jsonFile("no-barristers-caseworker-about-to-submit.json"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.barristerParty").value(BarristerParty.APPLICANT.getValue()))
            .andExpect(jsonPath("$.data.appBarristerCollection", hasSize(1)))
            .andExpect(jsonPath("$.data.appBarristerCollection[0].value.userId").isNotEmpty())
            .andExpect(jsonPath("$.data.respBarristerCollection").doesNotExist())
            .andExpect(jsonPath("$.data.RepresentationUpdateHistory[0].value.party").value("Applicant"))
            .andExpect(jsonPath("$.data.RepresentationUpdateHistory[0].value.name").value("Frodo Baggins"))
            .andExpect(jsonPath("$.data.RepresentationUpdateHistory[0].value.by").value("FR Caseworker"))
            .andExpect(jsonPath("$.data.RepresentationUpdateHistory[0].value.added.name").value("Applicant Barrister"))
            .andExpect(jsonPath("$.data.RepresentationUpdateHistory[0].value.added.email")
                .value("fr_applicant_barrister1@mailinator.com"))
            .andExpect(jsonPath("$.warnings").isEmpty())
            .andExpect(jsonPath("$.errors").isEmpty())
            .andReturn();
    }

    @Test
    void givenCaseworkerAndNoBarristersOnCase_whenSubmittedEvent_thenSendsBarristerAddedEmail() throws Exception {
        givenCurrentUserHasRole(caseAssignedRoleService, CaseRole.CASEWORKER);

        performSubmittedCallback(mockMvc, jsonFile("no-barristers-caseworker-submitted.json"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.barristerParty").value(BarristerParty.APPLICANT.getValue()))
            .andExpect(jsonPath("$.data.appBarristerCollection", hasSize(1)))
            .andExpect(jsonPath("$.warnings").isEmpty())
            .andExpect(jsonPath("$.errors").isEmpty())
            .andReturn();

        String expectedTemplateId = emailTemplates.get(FR_BARRISTER_ACCESS_ADDED.name());
        String expectedEmailAddress = "fr_applicant_barrister1@mailinator.com";

        Mockito.verify(emailClient).sendEmail(eq(expectedTemplateId), eq(expectedEmailAddress),
            anyMap(), anyString(), isNull(String.class));

        Mockito.verifyNoInteractions(sendLetterApi);
    }

    private String jsonFile(String jsonFilename) {
        return "/fixtures/integrationtests/managebarrister/" + jsonFilename;
    }
}
