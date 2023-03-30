package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.evidence.FileUploadResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDeleteService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementUploadService;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
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
    @Autowired private BulkPrintDocumentGeneratorService bulkPrintDocumentGeneratorService;

    @Autowired private BulkPrintDocumentService bulkPrintDocumentService;
    @Autowired private EvidenceManagementUploadService evidenceManagementUploadService;
    @Autowired private EvidenceManagementDeleteService evidenceManagementDeleteService;

    @Autowired private IdamAuthService idamAuthService;
    @Autowired private DocmosisPdfGenerationService docmosisPdfGenerationServiceMock;

    @Autowired private PdfStampingService pdfStampingServiceMock;
    @Captor
    private ArgumentCaptor<String> templateNameCaptor;

    @Test
    public void shouldStampDocument() throws Exception {
        when(pdfStampingServiceMock.stampDocument(document(), AUTH_TOKEN, false, StampType.FAMILY_COURT_STAMP, caseId))
            .thenReturn(document());

        CaseDocument stampDocument =
            genericDocumentService.stampDocument(caseDocument(), AUTH_TOKEN, StampType.FAMILY_COURT_STAMP, caseId);

        assertCaseDocument(stampDocument);
        verify(pdfStampingServiceMock, times(1))
            .stampDocument(document(), AUTH_TOKEN, false, StampType.FAMILY_COURT_STAMP, caseId);
    }

    @Test
    public void shouldAnnexStampDocument() {
        when(pdfStampingServiceMock.stampDocument(document(), AUTH_TOKEN, true, StampType.FAMILY_COURT_STAMP, caseId))
            .thenReturn(document());

        CaseDocument stampDocument = genericDocumentService
            .annexStampDocument(caseDocument(), AUTH_TOKEN, StampType.FAMILY_COURT_STAMP, caseId);

        assertCaseDocument(stampDocument);
        verify(pdfStampingServiceMock, times(1))
            .stampDocument(document(), AUTH_TOKEN, true, StampType.FAMILY_COURT_STAMP, caseId);
    }

    @Test
    public void shouldGenerateDocument() {
        final String templateName = "template name";
        final String fileName = "file name";

        when(evidenceManagementUploadService.upload(any(), any(), any()))
            .thenReturn(Collections.singletonList(
                FileUploadResponse.builder()
                    .fileName("app_docs.pdf")
                    .fileUrl("http://dm-store:8080/documents/d607c045-878e-475f-ab8e-b2f667d8af64")
                    .build()));
        when(docmosisPdfGenerationServiceMock
            .generateDocFrom(any(), any())).thenReturn("".getBytes(StandardCharsets.UTF_8));

        CaseDetails caseDetails = defaultContestedCaseDetails();
        CaseDocument document = genericDocumentService.generateDocument(AUTH_TOKEN, caseDetails, templateName, fileName);

        assertCaseDocument(document);
        verify(docmosisPdfGenerationServiceMock, times(1))
            .generateDocFrom(templateNameCaptor.capture(), any());


        assertThat(templateNameCaptor.getValue(), is(templateName));
        caseDetails.getData().put(DocumentHelper.CASE_NUMBER, caseDetails.getId());
    }

    @Test
    public void shouldDeleteDocument() {
        genericDocumentService.deleteDocument(caseDocument().getDocumentUrl(), AUTH_TOKEN);

        verify(evidenceManagementDeleteService, times(1)).delete(caseDocument().getDocumentUrl(), AUTH_TOKEN);
    }

    @Test
    public void shouldBulkPrintDocument() {
        genericDocumentService.bulkPrint(BulkPrintRequest.builder().build(), AUTH_TOKEN);

        verify(bulkPrintDocumentGeneratorService, times(1)).send(any(), any());
    }
}
