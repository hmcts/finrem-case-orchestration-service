package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentchecker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDownloadService;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogs;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;

@ExtendWith(MockitoExtension.class)
class DocumentCheckerServiceTest extends BaseServiceTest {

    @TestLogs
    private final TestLogger logs = new TestLogger(DocumentCheckerService.class);

    @InjectMocks
    private DocumentCheckerService underTest;

    @Mock
    private DocxDocumentChecker docxDocumentChecker;

    @Mock
    private DuplicateFilenameDocumentChecker duplicateFilenameDocumentChecker;

    @Mock
    private EvidenceManagementDownloadService downloadService;

    @BeforeEach
    public void setUp() {
        List<DocumentChecker> documentCheckers = Arrays.asList(docxDocumentChecker, duplicateFilenameDocumentChecker);
        underTest = new DocumentCheckerService(downloadService, documentCheckers);
    }

    @Test
    void testSingleWarningReturnedByDocxDocumentChecker() throws DocumentContentCheckerException {
        final CaseDocument caseDocument = caseDocument();

        when(downloadService.download(any(), eq(AUTH_TOKEN))).thenReturn(new byte[]{});
        when(docxDocumentChecker.canCheck(caseDocument)).thenReturn(true);
        when(docxDocumentChecker.getWarnings(eq(caseDocument), any(), any())).thenReturn(List.of("docx warning"));

        List<String> actual = underTest.getWarnings(caseDocument, FinremCaseDetails.builder().build(), AUTH_TOKEN);
        assertThat(actual).containsExactly("docx warning");
    }

    @Test
    void testMultipleWarningsReturnedCheckers() throws DocumentContentCheckerException {
        final CaseDocument caseDocument = caseDocument();

        when(downloadService.download(any(), eq(AUTH_TOKEN))).thenReturn(new byte[]{});
        when(docxDocumentChecker.canCheck(caseDocument)).thenReturn(true);
        when(docxDocumentChecker.getWarnings(eq(caseDocument), any(), any())).thenReturn(List.of("docx warning"));
        when(duplicateFilenameDocumentChecker.canCheck(caseDocument)).thenReturn(true);
        when(duplicateFilenameDocumentChecker.getWarnings(eq(caseDocument), any(), any())).thenReturn(List.of("duplicate warning"));

        List<String> actual = underTest.getWarnings(caseDocument, FinremCaseDetails.builder().build(), AUTH_TOKEN);
        assertThat(actual).contains("docx warning", "duplicate warning");
    }

    @Test
    void testIfDocxCheckerThrowDocumentContentCheckerException() throws DocumentContentCheckerException {
        final CaseDocument caseDocument = caseDocument();
        when(docxDocumentChecker.canCheck(caseDocument)).thenReturn(true);
        when(docxDocumentChecker.getWarnings(eq(caseDocument), any(), any()))
            .thenThrow(new DocumentContentCheckerException(new RuntimeException("test")));

        underTest.getWarnings(caseDocument, FinremCaseDetails.builder().build(), AUTH_TOKEN);
        assertThat(logs.getErrors()).isNotEmpty().contains("Unexpected error when getting warnings from " + DocxDocumentChecker.class.getName());
    }

    @Test
    void testNoDocumentCheckerCanCheck() {
        final CaseDocument caseDocument = caseDocument();
        when(docxDocumentChecker.canCheck(caseDocument)).thenReturn(false);
        when(duplicateFilenameDocumentChecker.canCheck(caseDocument)).thenReturn(false);

        assertThat(underTest.getWarnings(caseDocument, FinremCaseDetails.builder().build(), AUTH_TOKEN)).isEmpty();
    }
}
