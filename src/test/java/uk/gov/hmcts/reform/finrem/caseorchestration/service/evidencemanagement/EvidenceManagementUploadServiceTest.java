package uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.evidence.FileUploadResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.IdamToken;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamAuthService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Paths.get;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestObjectMapperFactory.createObjectMapper;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@ExtendWith(MockitoExtension.class)
class EvidenceManagementUploadServiceTest {

    @Mock
    private RestTemplate restTemplate;
    @Mock
    private CaseDocumentClient caseDocumentClient;
    @Mock
    private IdamAuthService idamAuthService;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private FeatureToggleService featureToggleService;
    @InjectMocks
    private EvidenceManagementUploadService emUploadService;

    private UploadResponse uploadResponse;

    private ArgumentCaptor<HttpEntity> httpEntityReqEntity;

    @BeforeEach
    void setup() throws IOException {
        ReflectionTestUtils.setField(emUploadService,"documentManagementStoreUploadUrl", "emuri");
        lenient().when(authTokenGenerator.generate()).thenReturn("xxxx");
        lenient().when(idamAuthService.getUserDetails(AUTH_TOKEN)).thenReturn(UserDetails.builder().id("19").build());
        mockRestTemplate();

        Document.Links links = new Document.Links();
        links.binary = new Document.Link();
        links.self = new Document.Link();
        Document document = Document.builder()
            .createdOn(new Date())
            .modifiedOn(new Date())
            .links(links)
            .build();
        uploadResponse =
            new UploadResponse(Collections.singletonList(
                document));

        lenient().when(idamAuthService.getIdamToken(any())).thenReturn(IdamToken.builder().build());
        when(featureToggleService.isSecureDocEnabled()).thenReturn(false);
    }

    @Test
    void givenAuthKeyParamIsPassed_whenUploadIsCalled_thenExpectUploadToSucceed() {
        List<FileUploadResponse> responses = emUploadService.upload(getMultipartFiles(), CONTESTED,
            AUTH_TOKEN);
        assertFalse(responses.isEmpty());
    }

    @Test
    void givenAuthKeyParamIsPassed_whenUploadIsCalled_thenExpectEmRequestWith3Headers() {
        emUploadService.upload(getMultipartFiles(), CONTESTED, AUTH_TOKEN);
        List<HttpEntity> allValues = httpEntityReqEntity.getAllValues();
        assertEquals(3, allValues.getFirst().getHeaders().size());
    }

    @Test
    void givenAuthKeyParamIsPassed_whenUploadIsCalled_thenExpectEmReqToHaveSecurityAuthHeader() {
        emUploadService.upload(getMultipartFiles(), CONTESTED, AUTH_TOKEN);
        assertTrue(getEmRequestHeaders().containsKey("ServiceAuthorization"));
    }

    @Test
    void givenAuthKeyParamIsPassed_whenUploadIsCalled_thenExpectEmReqToHaveUserIdHeader() {
        emUploadService.upload(getMultipartFiles(), CONTESTED, AUTH_TOKEN);
        assertTrue(getEmRequestHeaders().containsKey("user-id"));
    }

    @Test
    void givenAuthKeyParamIsPassed_whenUploadIsCalled_thenExpectEmReqToHaveValidContentTypeHeader() {
        emUploadService.upload(getMultipartFiles(), CONTESTED, AUTH_TOKEN);
        assertEquals("multipart/form-data", Objects.requireNonNull(getEmRequestHeaders().get("Content-Type")).getFirst());
    }

    @Test
    public void givenAuthKeyParamIsPassed_whenUploadIsCalled_thenExpectAuthKeyIsParsedForUserId() {
        emUploadService.upload(getMultipartFiles(), CONTESTED, AUTH_TOKEN);
        assertEquals("19", Objects.requireNonNull(getEmRequestHeaders().get("user-id")).getFirst());
    }

    @Test
    void givenNullFileParamIsPassed_whenUploadIsCalled_thenExpectError() {
        assertThrows(NullPointerException.class, () -> emUploadService.upload(null, CONTESTED, AUTH_TOKEN));
    }

    @Test
    void givenUploadResponseReturned_whenUploadIsCalled_thenExpectUploadToSucceed() {
        when(featureToggleService.isSecureDocEnabled()).thenReturn(true);
        when(caseDocumentClient.uploadDocuments(any(), any(), any(), any(), any())).thenReturn(uploadResponse);
        List<FileUploadResponse> responses = emUploadService.upload(getMultipartFiles(), CONTESTED, AUTH_TOKEN);
        assertFalse(responses.isEmpty());
    }

    @Test
    void givenNotUploadResponseReturned_whenUploadIsCalled_thenExpectUploadToNotSucceed() {
        when(featureToggleService.isSecureDocEnabled()).thenReturn(true);
        when(caseDocumentClient.uploadDocuments(any(), any(), any(), any(), any())).thenReturn(null);
        List<FileUploadResponse> responses = emUploadService.upload(getMultipartFiles(), CONTESTED, AUTH_TOKEN);
        assertTrue(responses.isEmpty());
    }

    private List<MultipartFile> getMultipartFiles() {
        MockMultipartFile multipartFile = new MockMultipartFile("file", "JDP.pdf",
            "application/pdf", "This is a test pdf file".getBytes());
        return Collections.singletonList(multipartFile);
    }

    private void mockRestTemplate() throws IOException {
        this.httpEntityReqEntity = ArgumentCaptor.forClass(HttpEntity.class);
        lenient().when(restTemplate.postForObject(eq("emuri"), httpEntityReqEntity.capture(), any())).thenReturn(getResponse());
    }

    private HttpHeaders getEmRequestHeaders() {
        return httpEntityReqEntity.getAllValues().getFirst().getHeaders();
    }

    private ObjectNode getResponse() throws IOException {
        final String response = new String(readAllBytes(get("src/test/resources/fixtures/fileuploadresponse.json")));
        return (ObjectNode) createObjectMapper().readTree(response);
    }
}
