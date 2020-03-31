
package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionCollectionData;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.doCaseDocumentAssert;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.document;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.pensionDocumentData;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

public class ConsentOrderApprovedDocumentServiceTest {

    @Mock
    private DocumentClient documentClientMock;

    private DocumentConfiguration config;

    private ObjectMapper mapper = new ObjectMapper();

    private ConsentOrderApprovedDocumentService service;

    @Before
    public void setUp() {
        config = new DocumentConfiguration();
        config.setApprovedConsentOrderTemplate("test_template");
        config.setApprovedConsentOrderFileName("test_file");

        config.setApprovedConsentOrderTemplateB("test_template2");
        config.setApprovedConsentOrderFileNameB("test_file2");
        documentClientMock = mock(DocumentClient.class);
        service = new ConsentOrderApprovedDocumentService(documentClientMock, config, mapper);
    }

    @Test
    public void shouldGenerateApprovedConsentOrderLetter() {
        Map<String, Object> caseData = ImmutableMap.of();

        when(documentClientMock.generatePdf(any(), anyString()))
                .thenReturn(document());

        CaseDocument caseDocument = service.generateApprovedConsentOrderLetter(
                CaseDetails.builder().data(caseData).build(), AUTH_TOKEN);

        doCaseDocumentAssert(caseDocument);
        verify(documentClientMock, times(1)).generatePdf(any(), anyString());
    }

    @Test
    public void shouldGenerateApprovedConsentOrderLetterB() {
        Map<String, Object> caseData = ImmutableMap.of();

        when(documentClientMock.generatePdf(any(), anyString()))
                .thenReturn(document());

        CaseDocument caseDocument = service.generateApprovedConsentOrderLetterB(
                CaseDetails.builder().data(caseData).build(), AUTH_TOKEN);

        doCaseDocumentAssert(caseDocument);
        verify(documentClientMock, times(1)).generatePdf(any(), anyString());
    }

    @Test
    public void shouldAnnexAndStampDocument() {
        CaseDocument caseDocument = caseDocument();

        when(documentClientMock.annexStampDocument(any(), anyString()))
                .thenReturn(document());

        CaseDocument annexStampDocument = service.annexStampDocument(caseDocument, AUTH_TOKEN);

        doCaseDocumentAssert(annexStampDocument);
        verify(documentClientMock, times(1)).annexStampDocument(any(), anyString());
    }

    @Test
    public void shouldStampDocument() {
        CaseDocument caseDocument = caseDocument();

        when(documentClientMock.stampDocument(any(), anyString()))
                .thenReturn(document());

        CaseDocument stampDocument = service.stampDocument(caseDocument, AUTH_TOKEN);

        doCaseDocumentAssert(stampDocument);
        verify(documentClientMock, times(1)).stampDocument(any(), anyString());
    }

    @Test
    public void shouldStampPensionDocuments() {
        List<PensionCollectionData> pensionDocuments = asList(pensionDocumentData(), pensionDocumentData());

        when(documentClientMock.stampDocument(any(), anyString()))
                .thenReturn(document());

        List<PensionCollectionData> stampPensionDocuments = service.stampPensionDocuments(pensionDocuments, AUTH_TOKEN);

        stampPensionDocuments.forEach(data -> doCaseDocumentAssert(data.getPensionDocumentData().getPensionDocument()));
        verify(documentClientMock, times(2)).stampDocument(any(), anyString());
    }
}
