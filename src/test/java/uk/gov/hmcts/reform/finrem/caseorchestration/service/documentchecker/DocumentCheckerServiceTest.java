package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentchecker;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDownloadService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.util.TestResource.AUTH_TOKEN;

@ExtendWith(SpringExtension.class)
class DocumentCheckerServiceTest extends BaseServiceTest {

    @Autowired
    private DocumentCheckerService documentCheckerService;

    @MockBean
    private DocxDocumentChecker docxDocumentChecker;

    @MockBean
    private DuplicateFilenameDocumentChecker duplicateFilenameDocumentChecker;

    @MockBean
    private EvidenceManagementDownloadService downloadService;

    @Test
    void testSingleWarningReturnedByDocxDocumentChecker() throws DocumentContentCheckerException {
        final CaseDocument caseDocument = buildCaseDocument();

        when(downloadService.download(any(), eq(AUTH_TOKEN))).thenReturn(new byte[]{});
        when(docxDocumentChecker.canCheck(caseDocument)).thenReturn(true);
        when(docxDocumentChecker.getWarnings(eq(caseDocument), any(), any())).thenReturn(List.of("docx warning"));

        List<String> actual = documentCheckerService.getWarnings(caseDocument, FinremCaseDetails.builder().build(), AUTH_TOKEN);
        assertThat(actual).containsExactly("docx warning");
    }

    @Test
    void testMultipleWarningsReturnedCheckers() throws DocumentContentCheckerException {
        final CaseDocument caseDocument = buildCaseDocument();

        when(downloadService.download(any(), eq(AUTH_TOKEN))).thenReturn(new byte[]{});
        when(docxDocumentChecker.canCheck(caseDocument)).thenReturn(true);
        when(docxDocumentChecker.getWarnings(eq(caseDocument), any(), any())).thenReturn(List.of("docx warning"));
        when(duplicateFilenameDocumentChecker.canCheck(caseDocument)).thenReturn(true);
        when(duplicateFilenameDocumentChecker.getWarnings(eq(caseDocument), any(), any())).thenReturn(List.of("duplicate warning"));

        List<String> actual = documentCheckerService.getWarnings(caseDocument, FinremCaseDetails.builder().build(), AUTH_TOKEN);
        assertThat(actual).contains("docx warning", "duplicate warning");
    }
}
