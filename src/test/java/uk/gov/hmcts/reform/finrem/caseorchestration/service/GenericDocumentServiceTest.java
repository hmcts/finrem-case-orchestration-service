package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentGenerationRequest;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.assertCaseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.defaultContestedCaseDetails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.document;

@ActiveProfiles("test-mock-feign-clients")
public class GenericDocumentServiceTest extends BaseServiceTest {

    @Autowired
    private GenericDocumentService genericDocumentService;
    @Autowired
    private DocumentClient documentClientMock;
    @MockBean
    protected DocumentHelper documentHelperMock;

    @Captor
    private ArgumentCaptor<DocumentGenerationRequest> documentGenerationRequestCaptor;

    @Test
    public void shouldStampDocument() {
        when(documentClientMock.stampDocument(any(), anyString())).thenReturn(document());

        CaseDocument stampDocument = genericDocumentService.stampDocument(caseDocument(), AUTH_TOKEN);

        assertCaseDocument(stampDocument);
        verify(documentClientMock, times(1)).stampDocument(any(), eq(AUTH_TOKEN));
    }

    @Test
    public void shouldAnnexStampDocument() {
        when(documentClientMock.annexStampDocument(any(), anyString())).thenReturn(document());

        CaseDocument stampDocument = genericDocumentService.annexStampDocument(caseDocument(), AUTH_TOKEN);

        assertCaseDocument(stampDocument);
        verify(documentClientMock, times(1)).annexStampDocument(any(), eq(AUTH_TOKEN));
    }

    @Test
    public void shouldGenerateDocument() {
        final String templateName = "template name";
        final String fileName = "file name";
        CaseDetails caseDetails = defaultContestedCaseDetails();

        when(documentClientMock.generatePdf(any(), anyString())).thenReturn(document());
        when(documentHelperMock.deepCopy(caseDetails, CaseDetails.class)).thenReturn(caseDetails);

        CaseDocument document = genericDocumentService.generateDocument(AUTH_TOKEN, caseDetails, templateName, fileName);

        assertCaseDocument(document);
        verify(documentClientMock, times(1)).generatePdf(documentGenerationRequestCaptor.capture(), eq(AUTH_TOKEN));

        assertThat(documentGenerationRequestCaptor.getValue().getTemplate(), is(templateName));
        assertThat(documentGenerationRequestCaptor.getValue().getFileName(), is(fileName));
        assertThat(documentGenerationRequestCaptor.getValue().getValues().get("caseDetails"), is(caseDetails));
    }

    @Test
    public void shouldDeleteDocument() {
        genericDocumentService.deleteDocument(caseDocument().getDocumentUrl(), AUTH_TOKEN);

        verify(documentClientMock, times(1)).deleteDocument(caseDocument().getDocumentUrl(), AUTH_TOKEN);
    }

    @Test
    public void shouldBulkPrintDocument() {
        genericDocumentService.bulkPrint(BulkPrintRequest.builder().build());

        verify(documentClientMock, times(1)).bulkPrint(BulkPrintRequest.builder().build());
    }
}
