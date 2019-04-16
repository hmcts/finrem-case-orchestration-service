package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentGeneratorClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Document;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentRequest;

import java.util.Map;
import java.util.concurrent.CompletionException;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.DOC_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.doCaseDocumentAssert;

public class HearingDocumentServiceTest {

    private DocumentGeneratorClient generatorClient;
    private DocumentConfiguration config;
    private ObjectMapper mapper = new ObjectMapper();

    private HearingDocumentService service;

    @Before
    public void setUp() {
        config = new DocumentConfiguration();
        config.setFormCFastTrackTemplate("firstTrackTemplate");
        config.setFormCNonFastTrackTemplate("nonFastfirstTrackTemplate");
        config.setFormGTemplate("formGTemplate");
        config.setFormCFileName("Form-C.pdf");
        config.setFormGFileName("Form-G.pdf");
        config.setMiniFormFileName("file_name");

        generatorClient = Mockito.mock(DocumentGeneratorClient.class);
        when(generatorClient.generatePDF(isA(DocumentRequest.class), eq(AUTH_TOKEN))).thenReturn(document());

        service = new HearingDocumentService(generatorClient, config, mapper);
    }

    @Test(expected = IllegalArgumentException.class)
    public void fastTrackDecisionNotSupplied() {
        CaseDetails caseDetails = CaseDetails.builder().data(ImmutableMap.of()).build();
        service.generateHearingDocuments(AUTH_TOKEN, caseDetails);
    }

    @Test
    public void generateFastTrackFormC() {
        Map<String, Object> result = service.generateHearingDocuments(AUTH_TOKEN, makeItFastTrackDecisionCase());
        doCaseDocumentAssert((CaseDocument) result.get("formC"));
    }

    @Test
    public void generateNonFastTrackFormCAndFormG() {
        Map<String, Object> result = service.generateHearingDocuments(AUTH_TOKEN, makeItNonFastTrackDecisionCase());
        doCaseDocumentAssert((CaseDocument) result.get("formC"));
        doCaseDocumentAssert((CaseDocument) result.get("formG"));
    }

    @Test(expected = CompletionException.class)
    public void unsuccessfulGenerateHearingDocuments() {
        when(generatorClient.generatePDF(isA(DocumentRequest.class), eq(AUTH_TOKEN))).thenThrow(new RuntimeException());
        service.generateHearingDocuments(AUTH_TOKEN, makeItNonFastTrackDecisionCase());
    }

    private CaseDetails makeItNonFastTrackDecisionCase() {
        return caseDetails("No");
    }

    private CaseDetails makeItFastTrackDecisionCase() {
        return caseDetails("Yes");
    }

    private CaseDetails caseDetails(String isFastTrackDecision) {
        Map<String, Object> caseData = ImmutableMap.of("fastTrackDecision", isFastTrackDecision);
        return CaseDetails.builder().data(caseData).build();
    }

    private Document document() {
        Document document = new Document();
        document.setBinaryUrl(BINARY_URL);
        document.setFileName(FILE_NAME);
        document.setUrl(DOC_URL);
        return document;
    }
}