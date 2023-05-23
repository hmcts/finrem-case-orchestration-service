package uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
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
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EvidenceManagementAuditServiceTest {

    public static final String DOC_URL = "http://dm-store:8080/documents/d607c045-878e-475f-ab8e-b2f667d8af64";
    public static final String AUTH = "auth";
    public static final String IDAM_OAUTH_TOKEN = "idamOauthToken";
    public static final String SERVICE_AUTH = "serviceAuth";
    public static final String DOC_UUID = "d607c045-878e-475f-ab8e-b2f667d8af64";

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

    @Before
    public void setUp() {
        idamToken = IdamToken.builder()
            .idamOauth2Token(IDAM_OAUTH_TOKEN)
            .serviceAuthorization(SERVICE_AUTH)
            .build();
        when(idamAuthService.getIdamToken(any())).thenReturn(idamToken);
    }

    @Test
    public void whenDmStoreAuditRequested_thenDocumentManagementResponseIsProcessed() {
        when(idamAuthService.getUserDetails(any())).thenReturn(UserDetails.builder().build());
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(JsonNode.class))).thenReturn(jsonNode());

        List<FileUploadResponse> response = evidenceManagementAuditService.audit(singletonList("mockFileUrl"), "mockToken");

        assertThat(response, hasSize(1));
        assertThat(response.get(0).getFileName(), is("PNGFile.png"));
    }

    @SneakyThrows
    private ResponseEntity<JsonNode> jsonNode() {
        return ResponseEntity.ok().body(new ObjectMapper()
            .readTree(new String(readAllBytes(get("src/test/resources/fixtures/fileauditresponse.json")))));
    }

    @Test
    public void whenDmStoreAuditRequested_thenDocumentManagementResponseIsProcessedEvenLastupdatedByNotPresent() {
        when(idamAuthService.getUserDetails(any())).thenReturn(UserDetails.builder().build());
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(JsonNode.class))).thenReturn(jsonNodeV2());

        List<FileUploadResponse> response = evidenceManagementAuditService.audit(singletonList("mockFileUrl"), "mockToken");

        assertThat(response, hasSize(1));
        assertThat(response.get(0).getFileName(), is("PNGFile.png"));
    }

    @SneakyThrows
    private ResponseEntity<JsonNode> jsonNodeV2() {
        return ResponseEntity.ok().body(new ObjectMapper().readTree(new String(readAllBytes(get("src/test/resources"
            + "/fileauditresponseV2.txt")))));
    }

    @Test
    public void whenSecDocAuditRequested_thenDocumentManagementResponseIsProcessed() {
        when(featureToggleService.isSecureDocEnabled()).thenReturn(true);
        when(caseDocumentClient.getMetadataForDocument(anyString(), anyString(), anyString()))
            .thenReturn(getDocumentMetadata());
        List<String> docUrls = List.of(DOC_URL);
        List<FileUploadResponse> response = evidenceManagementAuditService.audit(docUrls, AUTH);

        assertNotNull(response);
        assertThat(response, hasSize(1));
        assertThat(response.get(0).getFileName(), is("PNGFile.png"));
        assertThat(response.get(0).getFileUrl(), is(DOC_URL));
        assertThat(response.get(0).getMimeType(), is("image/png"));
        assertTrue(response.get(0).getCreatedOn().equals(FinremDateUtils.getLocalDateTime("2020-12-08T16:27:46")));
        assertTrue(response.get(0).getModifiedOn().equals(FinremDateUtils.getLocalDateTime("2020-12-08T16:27:46")));
    }

    @SneakyThrows
    private Document getDocumentMetadata() {
        Document.Links links = new Document.Links();
        links.self = new Document.Link();
        links.binary = new Document.Link();
        links.self.href = DOC_URL;
        links.binary.href = DOC_URL + "/binary";
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        Document document = Document.builder()
            .links(links)
            .originalDocumentName("PNGFile.png")
            .mimeType("image/png")
            .createdBy("d0859134-01ef-4183-8acc-aefd14cb4dcf")
            .lastModifiedBy("d0859134-01ef-4183-8acc-aefd14cb4dcf")
            .modifiedOn(formatter.parse("2020-12-08T16:27:46+0000"))
            .createdOn(formatter.parse("2020-12-08T16:27:46+0000"))
            .classification(Classification.RESTRICTED)
            .build();
        return document;
    }
}