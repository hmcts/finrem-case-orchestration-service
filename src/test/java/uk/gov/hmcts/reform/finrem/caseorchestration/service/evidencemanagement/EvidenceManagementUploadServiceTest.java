package uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
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

import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Paths.get;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EvidenceManagementUploadServiceTest {

    private static final String CASE_ID = "1234";
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

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setup() throws IOException {
        ReflectionTestUtils.setField(emUploadService,"documentManagementStoreUploadUrl", "emuri");
        when(authTokenGenerator.generate()).thenReturn("xxxx");
        when(idamAuthService.getUserDetails(authKey())).thenReturn(UserDetails.builder().id("19").build());
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

        when(idamAuthService.getIdamToken(any())).thenReturn(IdamToken.builder().build());
        when(featureToggleService.isSecureDocEnabled()).thenReturn(false);
    }

    @Test
    public void givenAuthKeyParamIsPassed_whenUploadIsCalled_thenExpectUploadToSucceed() {
        List<FileUploadResponse> responses = emUploadService.upload(getMultipartFiles(), CASE_ID,
            authKey());
        assertTrue(responses.size() > 0);
    }

    @Test
    public void givenAuthKeyParamIsPassed_whenUploadIsCalled_thenExpectEmRequestWith3Headers() {
        emUploadService.upload(getMultipartFiles(), CASE_ID, authKey());
        List<HttpEntity> allValues = httpEntityReqEntity.getAllValues();
        assertEquals(3, allValues.get(0).getHeaders().size());
    }

    @Test
    public void givenAuthKeyParamIsPassed_whenUploadIsCalled_thenExpectEmReqToHaveSecurityAuthHeader() {
        emUploadService.upload(getMultipartFiles(), CASE_ID, authKey());
        assertTrue(getEmRequestHeaders().containsKey("ServiceAuthorization"));
    }

    @Test
    public void givenAuthKeyParamIsPassed_whenUploadIsCalled_thenExpectEmReqToHaveUserIdHeader() {
        emUploadService.upload(getMultipartFiles(), CASE_ID, authKey());
        assertTrue(getEmRequestHeaders().containsKey("user-id"));
    }

    @Test
    public void givenAuthKeyParamIsPassed_whenUploadIsCalled_thenExpectEmReqToHaveValidContentTypeHeader() {
        emUploadService.upload(getMultipartFiles(), CASE_ID, authKey());
        assertEquals("multipart/form-data", getEmRequestHeaders().get("Content-Type").get(0));
    }

    @Test
    public void givenAuthKeyParamIsPassed_whenUploadIsCalled_thenExpectAuthKeyIsParsedForUserId() {
        emUploadService.upload(getMultipartFiles(), CASE_ID, authKey());
        assertEquals("19", getEmRequestHeaders().get("user-id").get(0));
    }

    @Test
    public void givenNullFileParamIsPassed_whenUploadIsCalled_thenExpectError() {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("files");
        emUploadService.upload(null, CASE_ID, authKey());
        httpEntityReqEntity.getAllValues();
    }

    @Test
    public void givenUploadResponseReturned_whenUploadIsCalled_thenExpectUploadToSucceed() {
        when(featureToggleService.isSecureDocEnabled()).thenReturn(true);
        when(caseDocumentClient.uploadDocuments(any(), any(), any(), any(), any())).thenReturn(uploadResponse);
        List<FileUploadResponse> responses = emUploadService.upload(getMultipartFiles(), CASE_ID, authKey());
        assertTrue(responses.size() > 0);
    }

    @Test
    public void givenNotUploadResponseReturned_whenUploadIsCalled_thenExpectUploadToNotSucceed() {
        when(featureToggleService.isSecureDocEnabled()).thenReturn(true);
        when(caseDocumentClient.uploadDocuments(any(), any(), any(), any(), any())).thenReturn(null);
        List<FileUploadResponse> responses = emUploadService.upload(getMultipartFiles(), CASE_ID, authKey());
        assertTrue(responses.isEmpty());
    }

    private static String authKey() {
        return "dummykey";
    }

    private List<MultipartFile> getMultipartFiles() {
        MockMultipartFile multipartFile = new MockMultipartFile("file", "JDP.pdf",
            "application/pdf", "This is a test pdf file".getBytes());
        return Collections.singletonList(multipartFile);
    }

    private void mockRestTemplate() throws IOException {
        this.httpEntityReqEntity = ArgumentCaptor.forClass(HttpEntity.class);
        when(restTemplate.postForObject(eq("emuri"), httpEntityReqEntity.capture(), any())).thenReturn(getResponse());
    }

    private HttpHeaders getEmRequestHeaders() {
        return httpEntityReqEntity.getAllValues().get(0).getHeaders();
    }

    private ObjectNode getResponse() throws IOException {
        final String response = new String(readAllBytes(get("src/test/resources/fixtures/fileuploadresponse.json")));
        return (ObjectNode) new ObjectMapper().readTree(response);
    }
}
