package uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest.notifications;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.CaseOrchestrationApplication;
import uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest.IntegrationTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.client.EmailClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserTokenProvider;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Live integration parity tests between fixture-rendered Notify previews and real Notify previews.
 */
@ContextConfiguration(classes = CaseOrchestrationApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnableScheduling
@Category(IntegrationTest.class)
@ActiveProfiles("local")
@Slf4j
public class EmailNotificationIntegrationTest extends BaseTest {

    @MockitoBean
    private SystemUserTokenProvider systemUserTokenProvider;

    private static final Pattern JSON_PATH_TOKEN =
        Pattern.compile("\\{\\{jsonPath\\s+request\\.body\\s+'([^']+)'\\s*}}");

    private static final String TEMPLATE_ID_TOKEN = "{{request.pathSegments.[2]}}";

    @Autowired
    private EmailClient emailClient;

    @Autowired
    private ObjectMapper objectMapper;

    // -------------------------
    // CONSENTED SCENARIOS
    // -------------------------

    @Test
    public void shouldMatchFrConsentedAssignedToJudgeStub() throws Exception {
        assertFixtureMatchesLiveNotify("fixtures/functional/consented/fr-consented-assigned-to-judge-stub.json");
    }

    @Test
    public void shouldMatchFrConsentedConsentOrderAvailableStub() throws Exception {
        assertFixtureMatchesLiveNotify("fixtures/functional/consented/fr-consented-consent-order-available-stub.json");
    }

    @Test
    public void shouldMatchFrConsentedHwfSuccessfulStub() throws Exception {
        assertFixtureMatchesLiveNotify("fixtures/functional/consented/fr-consented-hwf-successful-stub.json");
    }

    // -------------------------
    // CONTESTED SCENARIOS
    // -------------------------

    @Test
    public void shouldMatchFrBarristerAccessAddedApplicantStub() throws Exception {
        assertFixtureMatchesLiveNotify("fixtures/functional/contested/fr-barrister-access-added-applicant-stub.json");
    }

    @Test
    public void shouldMatchFrBarristerAccessAddedRespondentStub() throws Exception {
        assertFixtureMatchesLiveNotify("fixtures/functional/contested/fr-barrister-access-added-respondent-stub.json");
    }

    @Test
    public void shouldMatchFrContestOrderApprovedApplicantStub() throws Exception {
        assertFixtureMatchesLiveNotify("fixtures/functional/contested/fr-contest-order-approved-applicant-stub.json");
    }

    @Test
    public void shouldMatchFrContestOrderApprovedRespondentStub() throws Exception {
        assertFixtureMatchesLiveNotify("fixtures/functional/contested/fr-contest-order-approved-respondent-stub.json");
    }

    @Test
    public void shouldMatchFrContestedApplicationIssuedStub() throws Exception {
        assertFixtureMatchesLiveNotify("fixtures/functional/contested/fr-contested-application-issued-stub.json");
    }

    @Test
    public void shouldMatchFrContestedDraftOrderReadyForReviewAdminStub() throws Exception {
        assertFixtureMatchesLiveNotify("fixtures/functional/contested/fr-contested-draft-order-ready-for-review-admin-stub.json");
    }

    @Test
    public void shouldMatchFrContestedGeneralApplicationOutcomeStub() throws Exception {
        assertFixtureMatchesLiveNotify("fixtures/functional/contested/fr-contested-general-application-outcome-stub.json");
    }

    @Test
    public void shouldMatchFrContestedGeneralApplicationReferToJudgeStub() throws Exception {
        assertFixtureMatchesLiveNotify("fixtures/functional/contested/fr-contested-general-application-refer-to-judge-stub.json");
    }

    @Test
    public void shouldMatchFrContestedHearingNotificationApplicantSolicitorStub() throws Exception {
        assertFixtureMatchesLiveNotify("fixtures/functional/contested/fr-contested-hearing-notification-applicant-solicitor-stub.json");
    }

    @Test
    public void shouldMatchFrContestedHearingNotificationRespondentSolicitorStub() throws Exception {
        assertFixtureMatchesLiveNotify("fixtures/functional/contested/fr-contested-hearing-notification-respondent-solicitor-stub.json");
    }

    @Test
    public void shouldMatchFrContestedHwfSuccessfulStub() throws Exception {
        assertFixtureMatchesLiveNotify("fixtures/functional/contested/fr-contested-hwf-successful-stub.json");
    }

    @Test
    public void shouldMatchFrContestedVacateNotificationSolicitorStub() throws Exception {
        assertFixtureMatchesLiveNotify("fixtures/functional/contested/fr-contested-vacate-notification-solicitor-stub.json");
    }

    @Test
    public void shouldMatchFrIntervenerAddedEmailStub() throws Exception {
        assertFixtureMatchesLiveNotify("fixtures/functional/contested/fr-intervener-added-email-stub.json");
    }

    @Test
    public void shouldMatchFrIntervenerSolicitorAddedEmailStub() throws Exception {
        assertFixtureMatchesLiveNotify("fixtures/functional/contested/fr-intervener-solicitor-added-email-stub.json");
    }

    @Test
    public void shouldMatchFrIntervenerSolicitorRemovedEmailStub() throws Exception {
        assertFixtureMatchesLiveNotify("fixtures/functional/contested/fr-intervener-solicitor-removed-email-stub.json");
    }

    private void assertFixtureMatchesLiveNotify(String fixtureClasspath) throws Exception {
        Fixture fixture = readFixture(fixtureClasspath);
        assertThat(fixture).as("Fixture not parsed: " + fixtureClasspath).isNotNull();
        assertThat(fixture.templateId).as("templateId missing: " + fixtureClasspath).isNotBlank();
        assertThat(fixture.notificationRequest).as("notificationRequest missing: " + fixtureClasspath).isNotNull();
        assertThat(fixture.notificationRequest.personalisation).as("personalisation missing: " + fixtureClasspath).isNotNull();
        assertThat(fixture.response).as("response missing: " + fixtureClasspath).isNotNull();
        assertThat(fixture.response.bodyAsJson).as("response.bodyAsJson missing: " + fixtureClasspath).isNotNull();

        uk.gov.service.notify.TemplatePreview livePreview =
            emailClient.generateTemplatePreview(fixture.templateId, fixture.notificationRequest.personalisation);

        JsonNode expected = renderExpectedFromFixtureTemplate(fixture);

        // Keep subject strict (after normalization) to detect true template drift.
        String expectedSubject = normalize(expected.path("subject").asText(""));
        String liveSubject = normalize(livePreview.getSubject().orElse(""));
        log.info("expectedSubject: {} liveSubject: {}", expectedSubject, liveSubject);

        assertThat(liveSubject)
            .as("subject mismatch for fixture " + fixtureClasspath)
            .isEqualTo(expectedSubject);

        String liveBody = normalize(livePreview.getBody());
        String liveHtml = normalizeHtml(livePreview.getHtml().orElse(""));
        log.info("liveBody: {}", liveBody);
        log.info("liveHtml: {}", liveHtml);

        if (fixture.expectedContains != null && !fixture.expectedContains.isEmpty()) {
            String combined = normalize(liveSubject + "\n" + liveBody + "\n" + liveHtml);
            for (String expectedFragment : fixture.expectedContains) {
                log.info("expectedFragment: {}", expectedFragment);
                assertThat(combined)
                    .as("expectedContains fragment missing for fixture " + fixtureClasspath + ": " + expectedFragment)
                    .contains(normalize(expectedFragment));
            }
        } else {
            // Fallback mode for fixtures without expectedContains.
            String expectedBody = normalize(expected.path("body").asText(""));
            log.info("expectedBody: {}", expectedBody);
            assertThat(liveBody)
                .as("body prefix mismatch for fixture " + fixtureClasspath)
                .startsWith(expectedBody);
        }

        log.info("Fixture parity passed: {}", fixtureClasspath);
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value
            .replace("\r\n", "\n")
            .replace('\u00A0', ' ')  // nbsp
            .trim();
    }

    private String normalizeHtml(String value) {
        return normalize(value)
            .replace("&nbsp;", " ")
            .replaceAll("\\s+", " ");
    }

    private JsonNode renderExpectedFromFixtureTemplate(Fixture fixture) throws Exception {
        JsonNode templateNode = objectMapper.valueToTree(fixture.response.bodyAsJson);
        JsonNode requestNode = objectMapper.valueToTree(fixture.notificationRequest);

        String rendered = templateNode.toString().replace(TEMPLATE_ID_TOKEN, fixture.templateId);

        Matcher matcher = JSON_PATH_TOKEN.matcher(rendered);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String jsonPath = matcher.group(1);
            JsonNode valueNode = resolveSimplePath(requestNode, jsonPath);
            String replacement = (valueNode == null || valueNode.isMissingNode() || valueNode.isNull())
                ? ""
                : valueNode.asText();
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);

        return objectMapper.readTree(sb.toString());
    }

    /**
     * Supports simple fixture json paths like $.personalisation.caseReferenceNumber.
     */
    private JsonNode resolveSimplePath(JsonNode root, String jsonPath) {
        if (jsonPath == null || !jsonPath.startsWith("$.")) {
            return null;
        }
        String[] parts = jsonPath.substring(2).split("\\.");
        JsonNode current = root;
        for (String part : parts) {
            if (current == null) {
                return null;
            }
            current = current.path(part);
        }
        return current;
    }

    private Fixture readFixture(String classpathLocation) throws Exception {
        ClassPathResource resource = new ClassPathResource(classpathLocation);
        try (InputStream is = resource.getInputStream()) {
            String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return objectMapper.readValue(json, Fixture.class);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Fixture {
        @JsonProperty("templateId")
        private String templateId;

        @JsonProperty("notificationRequest")
        private NotificationRequest notificationRequest;

        @JsonProperty("response")
        private ResponseFixture response;

        @JsonProperty("expectedContains")
        private java.util.List<String> expectedContains;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class NotificationRequest {
        @JsonProperty("personalisation")
        private Map<String, Object> personalisation;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ResponseFixture {
        @JsonProperty("bodyAsJson")
        private Map<String, Object> bodyAsJson;
    }
}