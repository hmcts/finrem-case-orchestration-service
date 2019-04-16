package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentGeneratorClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Document;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentRequest;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.DOC_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.doCaseDocumentAssert;

public class OnlineFormDocumentServiceTest {

    private DocumentGeneratorClient generatorClient;
    private DocumentConfiguration config;
    private ObjectMapper mapper = new ObjectMapper();

    private OnlineFormDocumentService service;

    @Before
    public void setUp() {
        config = new DocumentConfiguration();
        config.setMiniFormTemplate("template");
        config.setMiniFormFileName("file_name");

        Document document = new Document();
        document.setBinaryUrl(BINARY_URL);
        document.setFileName(FILE_NAME);
        document.setUrl(DOC_URL);

        generatorClient = Mockito.mock(DocumentGeneratorClient.class);
        when(generatorClient.generatePDF(isA(DocumentRequest.class), eq(AUTH_TOKEN))).thenReturn(document);

        service = new OnlineFormDocumentService(generatorClient, config, mapper);
    }

    @Test
    public void generateMiniFormA() {
        doCaseDocumentAssert(service.generateMiniFormA(AUTH_TOKEN, CaseDetails.builder().build()));
    }
}