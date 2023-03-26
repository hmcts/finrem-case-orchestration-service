package uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.IdamToken;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamAuthService;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EvidenceManagementDeleteServiceTest {

    public static final String DOC_URL = "http://dm-store:8080/documents/d607c045-878e-475f-ab8e-b2f667d8af64";
    public static final String AUTH = "auth";
    public static final String IDAM_OAUTH_TOKEN = "idamOauthToken";
    public static final String SERVICE_AUTH = "serviceAuth";
    public static final String DOC_UUID = "d607c045-878e-475f-ab8e-b2f667d8af64";
    @Mock
    private IdamAuthService idamAuthService;
    @Mock
    private CaseDocumentClient caseDocumentClient;
    @InjectMocks
    private EvidenceManagementDeleteService emDeleteService;
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
    public void givenUploadResponseReturned_whenUploadIsCalled_thenExpectUploadToSucceed() {

        emDeleteService.delete(DOC_URL, AUTH);

        verify(idamAuthService, times(1)).getIdamToken(AUTH);
        verify(caseDocumentClient, times(1))
            .deleteDocument(IDAM_OAUTH_TOKEN, SERVICE_AUTH, UUID.fromString(DOC_UUID),true);
    }
}
