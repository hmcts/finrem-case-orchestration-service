package uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest;

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentGenerationRequest;

import java.util.Collections;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

public class ContestedOnlineFormTest extends GenerateMiniFormATest {

    private static final String NOTIFY_CONTESTED_APPLICATION_ISSUED_CONTEXT_PATH = "/notify/contested/application-issued";

    @ClassRule
    public static WireMockClassRule notificationService = new WireMockClassRule(8086);

    @Override
    protected String apiUrl() {
        return "/case-orchestration/documents/generate-contested-mini-form-a";
    }

    @Override
    protected String getTestFixture() {
        return "/fixtures/contested/generate-contested-form-A.json";
    }

    @Override
    protected DocumentGenerationRequest documentRequest() {
        return DocumentGenerationRequest.builder()
            .template(documentConfiguration.getContestedMiniFormTemplate())
            .fileName(documentConfiguration.getContestedMiniFormFileName())
            .values(Collections.singletonMap("caseDetails",
                copyWithOptionValueTranslation(request.getCaseDetails())))
            .build();
    }

    @Test
    public void notifyContestedApplicationIssued() throws Exception {
        stubForNotification(NOTIFY_CONTESTED_APPLICATION_ISSUED_CONTEXT_PATH, HttpStatus.OK.value());
        generateDocumentServiceSuccessStub();
        idamServiceStub();
        generateDocument();
        verify(postRequestedFor(urlEqualTo(NOTIFY_CONTESTED_APPLICATION_ISSUED_CONTEXT_PATH))
            .withHeader(CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE)));
    }

    private void stubForNotification(String url, int value) {
        notificationService.stubFor(post(urlEqualTo(url))
            .withHeader(AUTHORIZATION, equalTo(AUTH_TOKEN))
            .withHeader(CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
            .willReturn(aResponse().withStatus(value)));
    }
}
