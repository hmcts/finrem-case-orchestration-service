package uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.CaseOrchestrationApplication;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.NotifyWireMockFixtureLoader;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.client.EmailClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamAuthService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PrdOrganisationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors.CheckSolicitorIsDigitalService;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Functional coverage for Notify preview payloads sent by COS using WireMock fixtures.
 */
@ContextConfiguration(classes = CaseOrchestrationApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Category(IntegrationTest.class)
@ActiveProfiles("local")
public class EmailNotificationFunctionalTest extends BaseTest {

    private static final int NOTIFY_PORT = 8086;

    @ClassRule
    public static WireMockClassRule notifyService = new WireMockClassRule(NOTIFY_PORT);

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmailClient emailClient;

    @Value("#{${uk.gov.notify.email.templates}}")
    private Map<String, String> emailTemplates;

    @MockitoBean
    private GenericDocumentService genericDocumentService;
    @MockitoBean
    private BulkPrintService bulkPrintService;
    @MockitoBean
    private PrdOrganisationService prdOrganisationService;
    @MockitoBean
    private CheckSolicitorIsDigitalService checkSolicitorIsDigitalService;
    @MockitoBean
    private IdamAuthService idamAuthService;

    @DynamicPropertySource
    static void notifyProperties(DynamicPropertyRegistry registry) {
        registry.add("uk.gov.notify.api.baseUrl", () -> "http://localhost:" + NOTIFY_PORT);
    }

    /**
     * Loads all fixture stubs under classpath:fixtures/functional.
     *
     * @throws Exception when fixture loading fails
     */
    @Before
    public void setUp() throws Exception {
        notifyService.resetAll();
        configureFor("localhost", NOTIFY_PORT);
        NotifyWireMockFixtureLoader.registerFixtureStubs(functionalFixtureFiles());
    }

    /**
     * Verifies preview request for FR_CONTESTED_HWF_SUCCESSFUL.
     *
     * @throws Exception when request parsing fails
     */
    @Test
    public void shouldPreviewFrContestedHwfSuccessfulTemplate() throws Exception {
        Map<String, Object> personalisation = basePersonalisation("1789470157672643");
        assertPreviewCall("FR_CONTESTED_HWF_SUCCESSFUL", personalisation);
    }

    /**
     * Verifies preview request for FR_CONTESTED_APPLICATION_ISSUED.
     *
     * @throws Exception when request parsing fails
     */
    @Test
    public void shouldPreviewFrContestedApplicationIssuedTemplate() throws Exception {
        Map<String, Object> personalisation = basePersonalisation("1789550062490957");
        assertPreviewCall("FR_CONTESTED_APPLICATION_ISSUED", personalisation);
    }

    /**
     * Verifies preview request for FR_CONTEST_ORDER_APPROVED_APPLICANT.
     *
     * @throws Exception when request parsing fails
     */
    @Test
    public void shouldPreviewFrContestOrderApprovedApplicantTemplate() throws Exception {
        Map<String, Object> personalisation = basePersonalisation("1789038571945893");
        personalisation.put("yourReference", "MNT12345");
        assertPreviewCall("FR_CONTEST_ORDER_APPROVED_APPLICANT", personalisation);
    }

    /**
     * Verifies preview request for FR_CONTEST_ORDER_APPROVED_RESPONDENT.
     *
     * @throws Exception when request parsing fails
     */
    @Test
    public void shouldPreviewFrContestOrderApprovedRespondentTemplate() throws Exception {
        Map<String, Object> personalisation = basePersonalisation("1790237505587220");
        personalisation.put("solicitorReferenceNumber", "");
        personalisation.put("notificationEmail", "fr_respondent_solicitor1@mailinator.com");
        personalisation.put("name", "Sauron");

        assertPreviewCall("FR_CONTEST_ORDER_APPROVED_RESPONDENT", personalisation);
    }

    /**
     * Verifies preview request for FR_CONTESTED_HEARING_NOTIFICATION_SOLICITOR.
     *
     * @throws Exception when request parsing fails
     */
    @Test
    public void shouldPreviewFrContestedHearingNotificationSolicitorTemplate() throws Exception {
        Map<String, Object> personalisation = basePersonalisation("1790009654387573");
        personalisation.put("hearingType", "First Directions Appointment (FDA)");
        personalisation.put("courtName", "London FRC");
        personalisation.put("courtEmail", "FRCLondon@justice.gov.uk");
        personalisation.put("respondentName", "Gollum");
        personalisation.put("applicantName", "Baggins");
        personalisation.put("divorceCaseNumber", null);
        personalisation.put("contactNumber", "0300 123 5577");

        assertPreviewCall("FR_CONTESTED_HEARING_NOTIFICATION_SOLICITOR", personalisation);
    }

    /**
     * Verifies preview request for FR_CONTESTED_DRAFT_ORDER_READY_FOR_REVIEW_ADMIN.
     *
     * @throws Exception when request parsing fails
     */
    @Test
    public void shouldPreviewFrContestedDraftOrderReadyForReviewAdminTemplate() throws Exception {
        Map<String, Object> personalisation = basePersonalisation("1790012345678901");
        personalisation.put("notificationEmail", "admin@email.com");
        personalisation.put("hearingDate", "1 January 2024");
        personalisation.put("manageCaseBaseUrl", "http://localhost:3000");

        assertPreviewCall("FR_CONTESTED_DRAFT_ORDER_READY_FOR_REVIEW_ADMIN", personalisation);
    }


    /**
     * Verifies preview request for FR_CONTESTED_GENERAL_APPLICATION_OUTCOME.
     *
     * @throws Exception when request parsing fails
     */
    @Test
    public void shouldPreviewFrContestedGeneralApplicationOutcomeTemplate() throws Exception {
        Map<String, Object> personalisation = basePersonalisation("1790181252240783");
        personalisation.put("courtName", "Birmingham FRC");

        assertPreviewCall("FR_CONTESTED_GENERAL_APPLICATION_OUTCOME", personalisation);
    }

    /**
     * Verifies preview request for FR_INTERVENER_SOLICITOR_ADDED_EMAIL.
     *
     * @throws Exception when request parsing fails
     */
    @Test
    public void shouldPreviewFrIntervenerSolicitorAddedEmailTemplate() throws Exception {
        Map<String, Object> personalisation = new LinkedHashMap<>();
        personalisation.put("intervenerSolicitorReferenceNumber", "Finrem1OrgReference");
        personalisation.put("frEmail", "contactFinancialRemedy@justice.gov.uk");
        personalisation.put("linkToSmartSurvey", "http://www.smartsurvey.co.uk/s/KCECE/");
        personalisation.put("intervenerSolicitorFirm", "FinRem-1-Org");
        personalisation.put("name", "FinRem-1-Org");
        personalisation.put("phoneOpeningHours", "from 8am to 6pm, Monday to Friday");
        personalisation.put("respondentName", "Smeagol Gollum");
        personalisation.put("applicantName", "Frodo Baggins");
        personalisation.put("intervenerFullName", "intApp1");
        personalisation.put("notificationEmail", "fr_applicant_solicitor1@mailinator.com");
        personalisation.put("caseReferenceNumber", "1790181835590837");

        assertPreviewCall("FR_INTERVENER_SOLICITOR_ADDED_EMAIL", personalisation);
    }

    private Map<String, Object> basePersonalisation(String caseReferenceNumber) {
        Map<String, Object> personalisation = new LinkedHashMap<>();
        personalisation.put("solicitorReferenceNumber", "Y707HZM");
        personalisation.put("hearingType", "");
        personalisation.put("courtName", "Coventry Combined Court Centre");
        personalisation.put("courtEmail", "FRCBirmingham@justice.gov.uk");
        personalisation.put("linkToSmartSurvey", "http://www.smartsurvey.co.uk/s/KCECE/");
        personalisation.put("name", "Bilbo Baggins");
        personalisation.put("respondentName", "Smeagol Gollum");
        personalisation.put("applicantName", "Frodo Baggins");
        personalisation.put("divorceCaseNumber", "LV12D12345");
        personalisation.put("notificationEmail", "fr_applicant_solicitor1@mailinator.com");
        personalisation.put("frEmail", "contactFinancialRemedy@justice.gov.uk");
        personalisation.put("contactNumber", "0300 303 0642");
        personalisation.put("caseReferenceNumber", caseReferenceNumber);
        return personalisation;
    }

    private void assertPreviewCall(String templateKey, Map<String, Object> personalisation) throws Exception {
        String templateId = emailTemplates.get(templateKey);
        assertThat(templateId).as("Missing template mapping for " + templateKey).isNotBlank();

        emailClient.generateTemplatePreview(templateId, personalisation);

        String path = "/v2/template/" + templateId + "/preview";
        List<LoggedRequest> requests = notifyService.findAll(postRequestedFor(urlPathEqualTo(path)));
        assertThat(requests).hasSize(1);

        JsonNode payload = objectMapper.readTree(requests.getFirst().getBodyAsString());
        JsonNode actualPersonalisation = payload.path("personalisation");
        assertThat(actualPersonalisation.isMissingNode()).isFalse();

        personalisation.forEach((key, value) -> {
            JsonNode node = actualPersonalisation.path(key);
            if (value == null) {
                assertThat(node.isNull() || node.isMissingNode())
                    .as("Personalisation field mismatch: " + key)
                    .isTrue();
            } else {
                assertThat(node.asText())
                    .as("Personalisation field mismatch: " + key)
                    .isEqualTo(String.valueOf(value));
            }
        });
    }

    private List<String> functionalFixtureFiles() throws IOException {
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:fixtures/functional/*.json");
        return Arrays.stream(resources)
            .map(Resource::getFilename)
            .filter(Objects::nonNull)
            .sorted()
            .toList();
    }
}
