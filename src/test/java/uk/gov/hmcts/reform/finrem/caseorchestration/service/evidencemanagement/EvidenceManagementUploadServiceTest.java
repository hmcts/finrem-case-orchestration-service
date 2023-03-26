package uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.evidence.FileUploadResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.IdamToken;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamAuthService;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EvidenceManagementUploadServiceTest {

    @Mock private CaseDocumentClient caseDocumentClient;
    @Mock private IdamAuthService idamAuthService;
    @InjectMocks
    private EvidenceManagementUploadService emUploadService;

    private UploadResponse uploadResponse;

    @Before
    public void setup() throws IOException {
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
    }

    @Test
    public void givenUploadResponseReturned_whenUploadIsCalled_thenExpectUploadToSucceed() {
        when(caseDocumentClient.uploadDocuments(any(), any(), any(), any(), any())).thenReturn(uploadResponse);
        List<FileUploadResponse> responses = emUploadService.upload(getMultipartFiles(), any(), authKey());
        assertTrue(responses.size() > 0);
    }

    @Test
    public void givenNotUploadResponseReturned_whenUploadIsCalled_thenExpectUploadToNotSucceed() {
        when(caseDocumentClient.uploadDocuments(any(), any(), any(), any(), any())).thenReturn(null);
        List<FileUploadResponse> responses = emUploadService.upload(getMultipartFiles(), any(), authKey());
        assertTrue(responses == null);
    }

    private static String authKey() {
        return "Bearer eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJkZGFjaW5hbWh1dXV0ZHBoOGNqMWg0NGM4MSIsInN1YiI6IjE5IiwiaWF0IjoxNT"
            + "IyNzkxMDQ1LCJleHAiOjE1MjI3OTQ2NDUsImRhdGEiOiJjYXNld29ya2VyLWRpdm9yY2UsY2FzZXdvcmtlcixjYXNld29ya2V"
            + "yLWRpdm9yY2UtbG9hMSxjYXNld29ya2VyLWxvYTEiLCJ0eXBlIjoiQUNDRVNTIiwiaWQiOiIxOSIsImZvcmVuYW1lIjoiQ2FzZV"
            + "dvcmtlclRlc3QiLCJzdXJuYW1lIjoiVXNlciIsImRlZmF1bHQtc2VydmljZSI6IkNDRCIsImxvYSI6MSwiZGVmYXVsdC11cmwiOi"
            + "JodHRwczovL2xvY2FsaG9zdDo5MDAwL3BvYy9jY2QiLCJncm91cCI6ImNhc2V3b3JrZXIifQ.y5tbI6Tg1bJLPkXm-nrI6D_FhM0pb"
            + "x72zDa1r7Qnp1M";
    }

    private List<MultipartFile> getMultipartFiles() {
        MockMultipartFile multipartFile = new MockMultipartFile("file", "JDP.pdf",
            "application/pdf", "This is a test pdf file".getBytes());
        return Collections.singletonList(multipartFile);
    }
}
