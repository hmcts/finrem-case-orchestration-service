package uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.IdamToken;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamAuthService;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EvidenceManagementDownloadServiceTest {

    public static final String DOC_BINARY =
        "http://dm-store:8080/documents/d607c045-878e-475f-ab8e-b2f667d8af64/binary";
    public static final String AUTH = "auth";
    public static final String IDAM_OAUTH_TOKEN = "idamOauthToken";
    public static final String SERVICE_AUTH = "serviceAuth";
    private IdamToken idamToken;

    @Mock private CaseDocumentClient caseDocumentClient;
    @Mock private IdamAuthService idamAuthService;
    @InjectMocks
    private EvidenceManagementDownloadService downloadService;

    @Before
    public void setUp() {
        idamToken = IdamToken.builder()
            .idamOauth2Token(IDAM_OAUTH_TOKEN)
            .serviceAuthorization(SERVICE_AUTH)
            .build();
        when(idamAuthService.getIdamToken(any())).thenReturn(idamToken);
    }

    @Test
    public void shouldCallCaseDocumentClient() {

        when(caseDocumentClient.getDocumentBinary(anyString(), anyString(), anyString()))
            .thenReturn(ResponseEntity.ok().build());

        byte[] response = downloadService.download(DOC_BINARY, AUTH);

        assertNotNull(response);
        verify(caseDocumentClient, times(1))
            .getDocumentBinary(IDAM_OAUTH_TOKEN, SERVICE_AUTH, DOC_BINARY);
    }
}
