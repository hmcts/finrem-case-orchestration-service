package uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.model.Classification;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.evidence.FileUploadResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.IdamToken;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamAuthService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.FinremDateUtils;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.text.SimpleDateFormat;
import java.util.List;

import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Paths.get;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestObjectMapperFactory.createObjectMapper;
import static uk.gov.hmcts.reform.finrem.caseorchestration.util.TestResource.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.util.TestResource.FILE_URL;

@ExtendWith(MockitoExtension.class)
class EvidenceManagementAuditServiceTest {

    private static final String AUTH = "auth";
    private static final String IDAM_OAUTH_TOKEN = "idamOauthToken";
    private static final String SERVICE_AUTH = "serviceAuth";

    @Mock
    private IdamAuthService idamAuthService;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private CaseDocumentClient caseDocumentClient;
    @InjectMocks
    private EvidenceManagementAuditService evidenceManagementAuditService;

    private IdamToken idamToken;

    @BeforeEach
    void setUp() {
        idamToken = IdamToken.builder()
            .idamOauth2Token(IDAM_OAUTH_TOKEN)
            .serviceAuthorization(SERVICE_AUTH)
            .build();
        lenient().when(idamAuthService.getIdamToken(any())).thenReturn(idamToken);
    }

    @Test
    void whenDocumentUrlNotFound_throwIllegalStateException() {
        when(idamAuthService.getUserDetails(any())).thenReturn(UserDetails.builder().build());
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(JsonNode.class))).thenReturn(invalidJsonNode());
        assertThrows(IllegalStateException.class, () -> evidenceManagementAuditService.audit(singletonList("mockFileUrl"), AUTH_TOKEN));
    }

    @Test
    void whenDmStoreAuditRequested_thenDocumentManagementResponseIsProcessed() {
        when(idamAuthService.getUserDetails(any())).thenReturn(UserDetails.builder().build());
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(JsonNode.class))).thenReturn(jsonNode());

        List<FileUploadResponse> response = evidenceManagementAuditService.audit(singletonList("mockFileUrl"), AUTH_TOKEN);

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().getFileName()).isEqualTo("PNGFile.png");
    }

    @SneakyThrows
    private ResponseEntity<JsonNode> jsonNode() {
        return ResponseEntity.ok().body(createObjectMapper()
            .readTree(new String(readAllBytes(get("src/test/resources/fixtures/fileauditresponse.json")))));
    }

    @SneakyThrows
    private ResponseEntity<JsonNode> invalidJsonNode() {
        return ResponseEntity.ok().body(createObjectMapper()
            .readTree(new String(readAllBytes(get("src/test/resources/fixtures/fileauditresponse-withinvaliddocument.json")))));
    }

    @Test
    void whenDmStoreAuditRequested_thenDocumentManagementResponseIsProcessedEvenLastupdatedByNotPresent() {
        when(idamAuthService.getUserDetails(any())).thenReturn(UserDetails.builder().build());
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(JsonNode.class)))
            .thenReturn(jsonNodePayload("/fileauditresponseV2.txt"));

        List<FileUploadResponse> response = evidenceManagementAuditService.audit(singletonList("mockFileUrl"), AUTH_TOKEN);

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().getFileName()).isEqualTo("PNGFile.png");
        assertThat(response.getFirst().getLastModifiedBy()).isEmpty();
    }

    @Test
    void whenAuditRequested_thenDocumentManagementResponseIsProcessedEvenCreatedByNotPresent() {
        when(idamAuthService.getUserDetails(any())).thenReturn(UserDetails.builder().build());
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(JsonNode.class)))
            .thenReturn(jsonNodePayload("/fileauditresponseV3.txt"));

        List<FileUploadResponse> response = evidenceManagementAuditService.audit(singletonList("mockFileUrl"), AUTH_TOKEN);

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().getFileName()).isEqualTo("PNGFile.png");
        assertThat(response.getFirst().getCreatedBy()).isEmpty();
        assertThat(response.getFirst().getLastModifiedBy()).isEmpty();
    }

    @SneakyThrows
    private ResponseEntity<JsonNode> jsonNodePayload(String payload) {
        return ResponseEntity.ok().body(createObjectMapper().readTree(new String(readAllBytes(get("src/test/resources"
            + payload)))));
    }

    @Test
    void whenSecDocAuditRequested_thenDocumentManagementResponseIsProcessed() {
        when(featureToggleService.isSecureDocEnabled()).thenReturn(true);
        when(caseDocumentClient.getMetadataForDocument(anyString(), anyString(), anyString()))
            .thenReturn(getDocumentMetadata());
        List<String> docUrls = List.of(FILE_URL);
        List<FileUploadResponse> response = evidenceManagementAuditService.audit(docUrls, AUTH);

        assertNotNull(response);
        assertThat(response).hasSize(1);
        assertThat(response.getFirst().getFileName()).isEqualTo("PNGFile.png");
        assertThat(response.getFirst().getFileUrl()).isEqualTo(FILE_URL);
        assertThat(response.getFirst().getMimeType()).isEqualTo("image/png");
        assertEquals(response.getFirst().getCreatedOn(), FinremDateUtils.getLocalDateTime("2020-12-08T16:27:46"));
        assertEquals(response.getFirst().getModifiedOn(), FinremDateUtils.getLocalDateTime("2020-12-08T16:27:46"));
    }

    @SneakyThrows
    private Document getDocumentMetadata() {
        Document.Links links = new Document.Links();
        links.self = new Document.Link();
        links.binary = new Document.Link();
        links.self.href = FILE_URL;
        links.binary.href = BINARY_URL;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        return Document.builder()
            .links(links)
            .originalDocumentName("PNGFile.png")
            .mimeType("image/png")
            .createdBy("d0859134-01ef-4183-8acc-aefd14cb4dcf")
            .lastModifiedBy("d0859134-01ef-4183-8acc-aefd14cb4dcf")
            .modifiedOn(formatter.parse("2020-12-08T16:27:46+0000"))
            .createdOn(formatter.parse("2020-12-08T16:27:46+0000"))
            .classification(Classification.RESTRICTED)
            .build();
    }
}
