package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentchecker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.DocumentCheckContext;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentchecker.contentchecker.CaseNumberDocumentContentChecker;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentchecker.contentchecker.DocumentContentChecker;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentchecker.contentchecker.RespondentNameDocumentContentChecker;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentreader.PdfDocumentReader;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.util.TestResource.getPathFromResources;

@ExtendWith(MockitoExtension.class)
class PdfDocumentCheckerTest {

    @InjectMocks
    private PdfDocumentChecker underTest;

    @Mock
    private RespondentNameDocumentContentChecker respondentNameDocumentContentChecker;

    @Mock
    private CaseNumberDocumentContentChecker caseNumberDocumentContentChecker;

    @Mock
    private PdfDocumentReader pdfDocumentReader;

    @BeforeEach
    public void setUp() {
        List<DocumentContentChecker> documentContentCheckers = Arrays.asList(respondentNameDocumentContentChecker,
            caseNumberDocumentContentChecker);
        underTest = new PdfDocumentChecker(pdfDocumentReader, documentContentCheckers);
    }

    @ParameterizedTest
    @ValueSource(strings = {"test.pdf", "test.PDF", "test.PdF"})
    void testCanCheck_withPdfExtension(String filename) {
        CaseDocument caseDocument = new CaseDocument();
        caseDocument.setDocumentFilename(filename);

        assertThat(underTest.canCheck(caseDocument)).isTrue();
    }

    @Test
    void testCanCheck_withNonPdfExtension() {
        CaseDocument caseDocument = new CaseDocument();
        caseDocument.setDocumentFilename("test.docx");

        assertThat(underTest.canCheck(caseDocument)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void testGetWarnings_withValidContent(int testCase) throws DocumentContentCheckerException, IOException {
        when(pdfDocumentReader.getContent(any())).thenReturn(new String[0]);
        CaseDocument caseDocument = new CaseDocument();
        caseDocument.setDocumentFilename("generalOrder.pdf");
        FinremCaseDetails caseDetails = new FinremCaseDetails();

        byte[] documentBytes = Files.readAllBytes(getPathFromResources("fixtures/documentcontentvalidation/generalOrder.pdf"));

        when(respondentNameDocumentContentChecker.getWarning(eq(caseDetails), any(String[].class)))
            .thenReturn("Warning1");
        when(caseNumberDocumentContentChecker.getWarning(eq(caseDetails), any(String[].class)))
            .thenReturn(testCase == 1 ? null : "Warning2");

        List<String> warnings = underTest.getWarnings(DocumentCheckContext.builder()
            .caseDocument(caseDocument).bytes(documentBytes).caseDetails(caseDetails)
            .build());

        assertThat(warnings).hasSize(testCase == 1 ? 1 : 2).contains("Warning1");

        if (testCase == 0) {
            assertThat(warnings).hasSize(2).contains("Warning1", "Warning2");
        } else if (testCase == 1) {
            assertThat(warnings).hasSize(1).contains("Warning1");
        } else {
            fail("Unexpected number of warnings: " + warnings.size());
        }
    }

    @Test
    void testGetWarnings_whenGivenFileCannotBeRead() throws IOException {
        when(pdfDocumentReader.getContent(any())).thenThrow(new IOException("test"));
        CaseDocument caseDocument = new CaseDocument();
        caseDocument.setDocumentFilename("test.pdf");
        FinremCaseDetails caseDetails = new FinremCaseDetails();
        byte[] documentBytes = "Invalid content".getBytes();

        assertThatThrownBy(() -> underTest.getWarnings(DocumentCheckContext.builder()
            .caseDocument(caseDocument).bytes(documentBytes).caseDetails(caseDetails).build()))
            .isInstanceOf(DocumentContentCheckerException.class);
    }
}
