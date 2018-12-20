package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentRequest;

import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.caseDataWithMiniFormA;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.document;

@RunWith(MockitoJUnitRunner.class)
public class DocumentServiceTest {

    private static final String AUTH_TOKEN = "Bearer nkjnYBJB";

    @Mock
    private DocumentClient documentClient;

    @InjectMocks
    private DocumentService service;

    @Test
    public void generateMiniFormA() {
        when(documentClient.generatePDF(isA(DocumentRequest.class), eq(AUTH_TOKEN))).thenReturn(document());

        CaseDocument result = service.generateMiniFormA(AUTH_TOKEN, new CaseDetails());
        assertThat(result.getDocumentFilename(), Matchers.is(FILE_NAME));
        assertThat(result.getDocumentUrl(), Matchers.is(URL));
        assertThat(result.getDocumentBinaryUrl(), Matchers.is(BINARY_URL));
    }

    @Test
    public void deleteExistingMiniFormAToGenerateNew() {
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseDataWithMiniFormA());

        when(documentClient.generatePDF(isA(DocumentRequest.class), eq(AUTH_TOKEN))).thenReturn(document());

        CaseDocument result = service.generateMiniFormA(AUTH_TOKEN, caseDetails);
        assertThat(result.getDocumentFilename(), Matchers.is(FILE_NAME));
        assertThat(result.getDocumentUrl(), Matchers.is(URL));
        assertThat(result.getDocumentBinaryUrl(), Matchers.is(BINARY_URL));

        verify(documentClient).deleteDocument(URL, AUTH_TOKEN);
    }
}