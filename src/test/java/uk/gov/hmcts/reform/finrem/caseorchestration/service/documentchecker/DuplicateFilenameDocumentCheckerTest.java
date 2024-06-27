package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentchecker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedGeneralOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedGeneralOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralOrderCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OtherDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OtherDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionTypeCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralOrderWrapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DuplicateFilenameDocumentCheckerTest {

    private static final String WARNING = "A document with this filename already exists on the case";

    private static final String DUPLICATED_FILENAME = "newFilename.pdf";

    private static final CaseDocument DUPLICATED_CASE_DOCUMENT = CaseDocument.builder().documentFilename(DUPLICATED_FILENAME).build();

    @InjectMocks
    private DuplicateFilenameDocumentChecker underTest;

    private static void assertDuplicateFilenameWarning(List<String> warnings) {
        assertThat(warnings).hasSize(1).containsExactly(WARNING);
    }

    @BeforeEach
    public void setUp() {
        underTest = new DuplicateFilenameDocumentChecker();
    }

    @Test
    void testCanCheck_alwaysReturnsTrue() {
        assertThat(underTest.canCheck(new CaseDocument())).isTrue();
        assertThat(underTest.canCheck(null)).isTrue();
        assertThat(underTest.canCheck(DUPLICATED_CASE_DOCUMENT)).isTrue();
    }

    @Test
    void testGetWarnings_NoDuplicate() throws DocumentContentCheckerException {
        List<String> warnings = underTest.getWarnings(DUPLICATED_CASE_DOCUMENT, new byte[0],
            FinremCaseDetails.builder()
                .data(FinremCaseData.builder()
                    .additionalDocument(CaseDocument.builder().documentFilename("additionalDocument").build())
                    .generalOrderWrapper(GeneralOrderWrapper.builder()
                        .generalOrderLatestDocument(CaseDocument.builder().documentFilename("generalOrderLatestDocument").build())
                        .build())
                    .build())
                .build(),
            FinremCaseDetails.builder().build());

        assertThat(warnings).isEmpty();
    }

    @Test
    void testGetWarnings_duplicateInGeneralOrderWrapper_generalOrderLatestDocument() throws DocumentContentCheckerException {
        List<String> warnings = underTest.getWarnings(DUPLICATED_CASE_DOCUMENT, new byte[0],
            FinremCaseDetails.builder()
                .data(FinremCaseData.builder()
                    .generalOrderWrapper(GeneralOrderWrapper.builder()
                        .generalOrderLatestDocument(DUPLICATED_CASE_DOCUMENT)
                        .build())
                    .build())
                .build(),
            FinremCaseDetails.builder().build());
        assertDuplicateFilenameWarning(warnings);
    }

    @Test
    void testGetWarnings_duplicateInGeneralOrderWrapper_generalOrderPreviewDocument() throws DocumentContentCheckerException {
        List<String> warnings = underTest.getWarnings(DUPLICATED_CASE_DOCUMENT, new byte[0],
            FinremCaseDetails.builder()
                .data(FinremCaseData.builder()
                    .generalOrderWrapper(GeneralOrderWrapper.builder()
                        .generalOrderPreviewDocument(DUPLICATED_CASE_DOCUMENT)
                        .build())
                    .build())
                .build(),
            FinremCaseDetails.builder().build());

        assertDuplicateFilenameWarning(warnings);
    }

    @Test
    void testGetWarnings_duplicateInGeneralOrderWrapper_generalOrders() throws DocumentContentCheckerException {
        List<String> warnings = underTest.getWarnings(DUPLICATED_CASE_DOCUMENT, new byte[0],
            FinremCaseDetails.builder()
                .data(FinremCaseData.builder()
                    .generalOrderWrapper(GeneralOrderWrapper.builder()
                        .generalOrders(List.of(ContestedGeneralOrderCollection.builder()
                            .value(ContestedGeneralOrder.builder()
                                .additionalDocument(DUPLICATED_CASE_DOCUMENT)
                                .build())
                            .build()))
                        .build())
                    .build())
                .build(),
            FinremCaseDetails.builder().build());

        assertDuplicateFilenameWarning(warnings);
    }

    @Test
    void testGetWarnings_duplicateInGeneralOrderWrapper_generalOrdersConsent() throws DocumentContentCheckerException {
        List<String> warnings = underTest.getWarnings(DUPLICATED_CASE_DOCUMENT, new byte[0],
            FinremCaseDetails.builder()
                .data(FinremCaseData.builder()
                    .generalOrderWrapper(GeneralOrderWrapper.builder()
                        .generalOrdersConsent(List.of(ContestedGeneralOrderCollection.builder()
                            .value(ContestedGeneralOrder.builder()
                                .additionalDocument(DUPLICATED_CASE_DOCUMENT)
                                .build())
                            .build()))
                        .build())
                    .build())
                .build(),
            FinremCaseDetails.builder().build());

        assertDuplicateFilenameWarning(warnings);
    }

    @Test
    void testGetWarnings_duplicateInGeneralOrderWrapper_generalOrderCollection() throws DocumentContentCheckerException {
        List<String> warnings = underTest.getWarnings(DUPLICATED_CASE_DOCUMENT, new byte[0],
            FinremCaseDetails.builder()
                .data(FinremCaseData.builder()
                    .generalOrderWrapper(GeneralOrderWrapper.builder()
                        .generalOrderCollection(List.of(GeneralOrderCollectionItem.builder()
                            .generalOrder(GeneralOrder.builder()
                                .generalOrderDocumentUpload(DUPLICATED_CASE_DOCUMENT)
                                .build())
                            .build()))
                        .build())
                    .build())
                .build(),
            FinremCaseDetails.builder().build());

        assertDuplicateFilenameWarning(warnings);
    }

    @Test
    void testGetWarnings_duplicateInPensionCollection() throws DocumentContentCheckerException {
        List<String> warnings = underTest.getWarnings(DUPLICATED_CASE_DOCUMENT, new byte[0],
            FinremCaseDetails.builder()
                .data(FinremCaseData.builder()
                    .pensionCollection(List.of(PensionTypeCollection.builder()
                            .typedCaseDocument(PensionType.builder()
                                .pensionDocument(DUPLICATED_CASE_DOCUMENT)
                                .build())
                        .build()))
                    .build())
                .build(),
            FinremCaseDetails.builder().build());

        assertDuplicateFilenameWarning(warnings);
    }

    @Test
    void testGetWarnings_duplicateInOtherDocumentsCollection() throws DocumentContentCheckerException {
        List<String> warnings = underTest.getWarnings(DUPLICATED_CASE_DOCUMENT, new byte[0],
            FinremCaseDetails.builder()
                .data(FinremCaseData.builder()
                    .otherDocumentsCollection(List.of(OtherDocumentCollection.builder()
                        .value(OtherDocument.builder()
                            .uploadedDocument(DUPLICATED_CASE_DOCUMENT)
                            .build())
                        .build()))
                    .build())
                .build(),
            FinremCaseDetails.builder().build());

        assertDuplicateFilenameWarning(warnings);
    }
}
