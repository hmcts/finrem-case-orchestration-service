package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentchecker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralOrderWrapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DuplicateFilenameDocumentCheckerTest {

    private static final String WARNING = "A document with this filename already exists on the case";

    @Mock
    private CaseDocument inptuCaseDocument;
    @Mock
    private FinremCaseDetails caseDetails;
    @Mock
    private FinremCaseData caseData;
    @Mock
    private CaseDocument additionalDocument;
    @Mock
    private GeneralOrderWrapper generalOrderWrapper;
    @Mock
    private CaseDocument generalOrderDocument;

    @InjectMocks
    private DuplicateFilenameDocumentChecker underTest;

    @BeforeEach
    public void setUp() {
        underTest = new DuplicateFilenameDocumentChecker();
        when(inptuCaseDocument.getDocumentFilename()).thenReturn("inputFilename");
    }

    @Test
    void testCanCheck_alwaysReturnsTrue() {
        assertThat(underTest.canCheck(new CaseDocument())).isTrue();
        assertThat(underTest.canCheck(null)).isTrue();
        assertThat(underTest.canCheck(inptuCaseDocument)).isTrue();
    }

    @Test
    void testGetWarnings_NoDuplicate() throws DocumentContentCheckerException {
        when(caseDetails.getData()).thenReturn(caseData);
        when(caseData.getAdditionalDocument()).thenReturn(additionalDocument);
        when(caseData.getGeneralOrderWrapper()).thenReturn(generalOrderWrapper);
        when(generalOrderDocument.getDocumentFilename()).thenReturn("generalOrderDocumentFilename");

        when(generalOrderWrapper.getGeneralOrderLatestDocument()).thenReturn(generalOrderDocument);
        when(additionalDocument.getDocumentFilename()).thenReturn("additionalDocumentFilename");

        List<String> warnings = underTest.getWarnings(inptuCaseDocument, new byte[0], caseDetails);

        assertThat(warnings).isEmpty();
    }

    @Test
    void testGetWarnings_NoDuplicateWhenAdditionalDocumentIsNull() throws DocumentContentCheckerException {
        when(caseDetails.getData()).thenReturn(caseData);
        when(caseData.getAdditionalDocument()).thenReturn(null);
        when(caseData.getGeneralOrderWrapper()).thenReturn(generalOrderWrapper);
        when(generalOrderDocument.getDocumentFilename()).thenReturn("generalOrderDocumentFilename");

        when(generalOrderWrapper.getGeneralOrderLatestDocument()).thenReturn(generalOrderDocument);

        List<String> warnings = underTest.getWarnings(inptuCaseDocument, new byte[0], caseDetails);

        assertThat(warnings).isEmpty();
    }

    @Test
    void testGetWarnings_DuplicateInAdditionalDocument() throws DocumentContentCheckerException {
        when(caseDetails.getData()).thenReturn(caseData);
        when(caseData.getAdditionalDocument()).thenReturn(additionalDocument);
        when(additionalDocument.getDocumentFilename()).thenReturn("inputFilename");

        List<String> warnings = underTest.getWarnings(inptuCaseDocument, new byte[0], caseDetails);

        assertThat(warnings).hasSize(1).containsExactly(WARNING);
    }

    @Test
    void testGetWarnings_DuplicateInGeneralOrderDocument() throws DocumentContentCheckerException {
        when(caseDetails.getData()).thenReturn(caseData);
        when(caseData.getAdditionalDocument()).thenReturn(additionalDocument);
        when(additionalDocument.getDocumentFilename()).thenReturn("additionalDocumentFilename");
        when(generalOrderDocument.getDocumentFilename()).thenReturn("inputFilename");
        when(generalOrderWrapper.getGeneralOrderLatestDocument()).thenReturn(generalOrderDocument);
        when(caseData.getGeneralOrderWrapper()).thenReturn(generalOrderWrapper);

        List<String> warnings = underTest.getWarnings(inptuCaseDocument, new byte[0], caseDetails);

        assertThat(warnings).hasSize(1).containsExactly(WARNING);
    }

    @Test
    void testGetWarnings_NoCaseData() throws DocumentContentCheckerException {
        List<String> warnings = underTest.getWarnings(inptuCaseDocument, new byte[0], FinremCaseDetails.builder()
            .data(FinremCaseData.builder().build())
            .build());

        assertThat(warnings).isEmpty();
    }
}
