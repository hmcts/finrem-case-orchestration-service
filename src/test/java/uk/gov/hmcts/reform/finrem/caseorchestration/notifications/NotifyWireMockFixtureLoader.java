package uk.gov.hmcts.reform.finrem.caseorchestration.notifications;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

/**
 * Utility for bootstrapping WireMock stubs from functional fixture JSON files.
 */
public final class NotifyWireMockFixtureLoader {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String FIXTURE_BASE = "fixtures/functional/";

    private NotifyWireMockFixtureLoader() {
    }

    /**
     * Registers fixture stubs without template-id substitution.
     *
     * @param fixtureFiles fixture file names
     * @throws Exception if loading/parsing fails
     */
    public static void registerFixtureStubs(List<String> fixtureFiles) throws Exception {
        for (String fixtureFile : fixtureFiles) {
            registerFixtureStub(fixtureFile);
        }
    }

    /**
     * Registers a single fixture stub without template-id substitution.
     *
     * @param fixtureFile fixture file name
     * @throws Exception if loading/parsing fails
     */
    public static void registerFixtureStub(String fixtureFile) throws Exception {
        JsonNode root = readFixture(fixtureFile);
        registerPostStub(root, fixtureFile, root.path("url").asText());
    }

    private static void registerPostStub(JsonNode root, String fixtureFile, String url) throws Exception {
        String method = root.path("httpMethod").asText("POST");
        int status = root.path("response").path("status").asInt(200);
        String contentType = root.path("response").path("contentType").asText("application/json");
        String body = OBJECT_MAPPER.writeValueAsString(root.path("response").path("bodyAsJson"));

        if (!"POST".equalsIgnoreCase(method)) {
            throw new IllegalArgumentException("Only POST supported currently: " + fixtureFile);
        }

        stubFor(post(urlEqualTo(url))
            .willReturn(aResponse()
                .withStatus(status)
                .withHeader("Content-Type", contentType)
                .withTransformers("response-template")
                .withBody(body)));
    }

    private static JsonNode readFixture(String fixtureFile) throws Exception {
        String path = FIXTURE_BASE + fixtureFile;
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(path)) {
            return OBJECT_MAPPER.readTree(Objects.requireNonNull(inputStream, "Missing fixture: " + path));
        }
    }
}