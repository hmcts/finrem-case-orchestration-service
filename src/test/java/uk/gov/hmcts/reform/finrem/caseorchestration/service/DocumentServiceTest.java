package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentGeneratorClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Document;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentRequest;

import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DocumentServiceTest {

    private static final String AUTH_TOKEN = "Bearer nkjnYBJB";
    private static final String URL = "http://url/file";
    private static final String FILE_NAME = "file";

    @Mock
    private DocumentGeneratorClient documentGeneratorClient;

    @InjectMocks
    private DocumentService service;

    @Test
    public void generateMiniFormA() {
        Document document = Document.builder().fileName(FILE_NAME).url(URL).build();
        when(documentGeneratorClient.generatePDF(isA(DocumentRequest.class), eq(AUTH_TOKEN))).thenReturn(document);

        CaseDocument result = service.generateMiniFormA(AUTH_TOKEN, new CaseDetails());
        assertThat(result.getDocumentFilename(), Matchers.is(FILE_NAME));
        assertThat(result.getDocumentUrl(), Matchers.is(URL));
    }
}