package uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamAuthService;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EvidenceManagementDownloadServiceTest {
    public static final String AUTH = "auth";

    @Mock private CaseDocumentClient caseDocumentClient;
    @Mock private IdamAuthService idamAuthService;

    @InjectMocks
    private EvidenceManagementDownloadService downloadService;

    @Test
    public void shouldCallCaseDocumentClient() {

        when(caseDocumentClient.getDocumentBinary(anyString(), anyString(), anyString()))
            .thenReturn(ResponseEntity.ok().build());

        byte[] response = downloadService.download("binaryString", AUTH);

        assertNotNull(response);
        verify(caseDocumentClient.getDocumentBinary(anyString(), anyString(), anyString()),
            times(1));
    }
}
