package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentGeneratorClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentOrderData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Document;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentRequest;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DocumentServiceTest {

    private static final String AUTH_TOKEN = "Bearer nkjnYBJB";
    private static final String URL = "http://url/file";
    private static final String BINARY_URL = URL + "/BINARY";
    private static final String FILE_NAME = "file";
    public static final String DOC_TYPE = "oder_type";

    @Mock
    private DocumentGeneratorClient documentGeneratorClient;

    @Mock
    private DocumentConfiguration documentConfiguration;

    @InjectMocks
    private DocumentService service;

    @Before
    public void setUp() {
        when(documentConfiguration.getMiniFormTemplate()).thenReturn("template");
        when(documentConfiguration.getMiniFormFileName()).thenReturn("file_name");
        when(documentConfiguration.getRejectedOrderTemplate()).thenReturn("test_template");
        when(documentConfiguration.getRejectedOrderFileName()).thenReturn("test_file");
        when(documentConfiguration.getRejectedOrderDocType()).thenReturn(DOC_TYPE);

        Document document = new Document();
        document.setBinaryUrl(BINARY_URL);
        document.setFileName(FILE_NAME);
        document.setUrl(URL);

        when(documentGeneratorClient.generatePDF(isA(DocumentRequest.class), eq(AUTH_TOKEN))).thenReturn(document);
    }

    @Test
    public void generateMiniFormA() {
        doCaseDocumentAssert(service.generateMiniFormA(AUTH_TOKEN, new CaseDetails()));
    }

    @Test
    public void generateConsentOrderNotApproved() {
        ConsentOrderData consentOrderData = service.generateConsentOrderNotApproved(AUTH_TOKEN, new CaseDetails());
        assertThat(consentOrderData.getConsentOrder().getDocumentType(), is(DOC_TYPE));

        doCaseDocumentAssert(consentOrderData.getConsentOrder().getDocumentLink());
    }

    private static void doCaseDocumentAssert(CaseDocument result) {
        assertThat(result.getDocumentFilename(), is(FILE_NAME));
        assertThat(result.getDocumentUrl(), is(URL));
        assertThat(result.getDocumentBinaryUrl(), is(BINARY_URL));
    }
}