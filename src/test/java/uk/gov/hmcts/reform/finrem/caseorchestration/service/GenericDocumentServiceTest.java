package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.evidence.FileUploadResponse;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
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

    @Autowired private GenericDocumentService genericDocumentService;
    @Autowired private DocumentClient documentClientMock;

    @Autowired private EvidenceManagementUploadService evidenceManagementUploadService;

    @Autowired private IdamAuthService idamAuthService;
    @Autowired private DocmosisPdfGenerationService docmosisPdfGenerationServiceMock;

    @Captor
    private ArgumentCaptor<Map> mapCaptor;
    @Captor
    private ArgumentCaptor<String> templateNameCaptor;

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

        when(evidenceManagementUploadService.upload(any(), any()))
            .thenReturn(Collections.singletonList(
                FileUploadResponse.builder()
                    .fileName("app_docs.pdf")
                    .fileUrl("http://dm-store/lhjbyuivu87y989hijbb")
                    .build()));
        when(docmosisPdfGenerationServiceMock
            .generateDocFrom(any(), any())).thenReturn("".getBytes(StandardCharsets.UTF_8));

        CaseDocument document = genericDocumentService.generateDocument(AUTH_TOKEN, defaultContestedCaseDetails(), templateName, fileName);

        assertCaseDocument(document);
        verify(docmosisPdfGenerationServiceMock, times(1))
            .generateDocFrom(templateNameCaptor.capture(), mapCaptor.capture());

        assertThat(templateNameCaptor.getValue(), is(templateName));
        assertThat(mapCaptor.getValue().get("caseDetails"), is(defaultContestedCaseDetails()));
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
