package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentchecker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AdditionalHearingDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AdditionalHearingDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AmendedConsentOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AmendedConsentOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConfidentialUploadedDocumentData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedGeneralOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedGeneralOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationItems;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralOrderCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingBulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingBulkPrintDocumentsData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerHearingNotice;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerHearingNoticeCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrderRefusalCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrderRefusalHolder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OtherDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OtherDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionTypeCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RefusalOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RefusalOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RespondToOrderDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RespondToOrderDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ScannedDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ScannedDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadAdditionalDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadAdditionalDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadConfidentialDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadConsentOrderDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadConsentOrderDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadGeneralDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadGeneralDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftDirectionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralOrderWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.InterimWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.UploadCaseDocumentWrapper;

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

    @Test
    void testGetWarnings_duplicateInAdditionalCicDocuments() throws DocumentContentCheckerException {
        List<String> warnings = underTest.getWarnings(DUPLICATED_CASE_DOCUMENT, new byte[0],
            FinremCaseDetails.builder()
                .data(FinremCaseData.builder()
                    .additionalCicDocuments(List.of(DocumentCollection.builder()
                        .value(DUPLICATED_CASE_DOCUMENT)
                        .build()))
                    .build())
                .build(),
            FinremCaseDetails.builder().build());

        assertDuplicateFilenameWarning(warnings);
    }

    @Test
    void testGetWarnings_duplicateInOrderRefusalCollection() throws DocumentContentCheckerException {
        List<String> warnings = underTest.getWarnings(DUPLICATED_CASE_DOCUMENT, new byte[0],
            FinremCaseDetails.builder()
                .data(FinremCaseData.builder()
                    .orderRefusalCollection(List.of(OrderRefusalCollection.builder()
                        .value(OrderRefusalHolder.builder()
                            .orderRefusalDocs(DUPLICATED_CASE_DOCUMENT)
                            .build())
                        .build()))
                    .build())
                .build(),
            FinremCaseDetails.builder().build());

        assertDuplicateFilenameWarning(warnings);
    }

    @Test
    void testGetWarnings_duplicateInOrderRefusalCollectionNew() throws DocumentContentCheckerException {
        List<String> warnings = underTest.getWarnings(DUPLICATED_CASE_DOCUMENT, new byte[0],
            FinremCaseDetails.builder()
                .data(FinremCaseData.builder()
                    .orderRefusalCollectionNew(List.of(OrderRefusalCollection.builder()
                        .value(OrderRefusalHolder.builder()
                            .orderRefusalDocs(DUPLICATED_CASE_DOCUMENT)
                            .build())
                        .build()))
                    .build())
                .build(),
            FinremCaseDetails.builder().build());

        assertDuplicateFilenameWarning(warnings);
    }

    @Test
    void testGetWarnings_duplicateInOrderRefusalOnScreen() throws DocumentContentCheckerException {
        List<String> warnings = underTest.getWarnings(DUPLICATED_CASE_DOCUMENT, new byte[0],
            FinremCaseDetails.builder()
                .data(FinremCaseData.builder()
                    .orderRefusalOnScreen(OrderRefusalHolder.builder()
                        .orderRefusalDocs(DUPLICATED_CASE_DOCUMENT)
                        .build())
                    .build())
                .build(),
            FinremCaseDetails.builder().build());

        assertDuplicateFilenameWarning(warnings);
    }

    @Test
    void testGetWarnings_duplicateInUploadConsentOrderDocuments() throws DocumentContentCheckerException {
        List<String> warnings = underTest.getWarnings(DUPLICATED_CASE_DOCUMENT, new byte[0],
            FinremCaseDetails.builder()
                .data(FinremCaseData.builder()
                    .uploadConsentOrderDocuments(List.of(UploadConsentOrderDocumentCollection.builder()
                        .value(UploadConsentOrderDocument.builder()
                            .documentLink(DUPLICATED_CASE_DOCUMENT)
                            .build())
                        .build()))
                    .build())
                .build(),
            FinremCaseDetails.builder().build());

        assertDuplicateFilenameWarning(warnings);
    }

    @Test
    void testGetWarnings_duplicateInUploadOrder() throws DocumentContentCheckerException {
        List<String> warnings = underTest.getWarnings(DUPLICATED_CASE_DOCUMENT, new byte[0],
            FinremCaseDetails.builder()
                .data(FinremCaseData.builder()
                    .uploadOrder(List.of(UploadOrderCollection.builder()
                        .value(UploadOrder.builder()
                            .documentLink(DUPLICATED_CASE_DOCUMENT)
                            .build())
                        .build()))
                    .build())
                .build(),
            FinremCaseDetails.builder().build());

        assertDuplicateFilenameWarning(warnings);
    }

    @Test
    void testGetWarnings_duplicateInUploadDocuments() throws DocumentContentCheckerException {
        List<String> warnings = underTest.getWarnings(DUPLICATED_CASE_DOCUMENT, new byte[0],
            FinremCaseDetails.builder()
                .data(FinremCaseData.builder()
                    .uploadDocuments(List.of(UploadDocumentCollection.builder()
                        .value(UploadDocument.builder()
                            .documentLink(DUPLICATED_CASE_DOCUMENT)
                            .build())
                        .build()))
                    .build())
                .build(),
            FinremCaseDetails.builder().build());

        assertDuplicateFilenameWarning(warnings);
    }

    @Test
    void testGetWarnings_duplicateInRespondToOrderDocuments() throws DocumentContentCheckerException {
        List<String> warnings = underTest.getWarnings(DUPLICATED_CASE_DOCUMENT, new byte[0],
            FinremCaseDetails.builder()
                .data(FinremCaseData.builder()
                    .respondToOrderDocuments(List.of(RespondToOrderDocumentCollection.builder()
                        .value(RespondToOrderDocument.builder()
                            .documentLink(DUPLICATED_CASE_DOCUMENT)
                            .build())
                        .build()))
                    .build())
                .build(),
            FinremCaseDetails.builder().build());

        assertDuplicateFilenameWarning(warnings);
    }

    @Test
    void testGetWarnings_duplicateInAmendedConsentOrderCollection() throws DocumentContentCheckerException {
        List<String> warnings = underTest.getWarnings(DUPLICATED_CASE_DOCUMENT, new byte[0],
            FinremCaseDetails.builder()
                .data(FinremCaseData.builder()
                    .amendedConsentOrderCollection(List.of(AmendedConsentOrderCollection.builder()
                        .value(AmendedConsentOrder.builder()
                            .amendedConsentOrder(DUPLICATED_CASE_DOCUMENT)
                            .build())
                        .build()))
                    .build())
                .build(),
            FinremCaseDetails.builder().build());

        assertDuplicateFilenameWarning(warnings);
    }

    @Test
    void testGetWarnings_duplicateInScannedDocuments() throws DocumentContentCheckerException {
        List<String> warnings = underTest.getWarnings(DUPLICATED_CASE_DOCUMENT, new byte[0],
            FinremCaseDetails.builder()
                .data(FinremCaseData.builder()
                    .scannedDocuments(List.of(ScannedDocumentCollection.builder()
                        .value(ScannedDocument.builder()
                            .url(DUPLICATED_CASE_DOCUMENT)
                            .build())
                        .build()))
                    .build())
                .build(),
            FinremCaseDetails.builder().build());

        assertDuplicateFilenameWarning(warnings);
    }

    @Test
    void testGetWarnings_duplicateInApprovedOrderCollection_orderLetter() throws DocumentContentCheckerException {
        List<String> warnings = underTest.getWarnings(DUPLICATED_CASE_DOCUMENT, new byte[0],
            FinremCaseDetails.builder()
                .data(FinremCaseData.builder()
                    .approvedOrderCollection(List.of(ConsentOrderCollection.builder()
                        .approvedOrder(ApprovedOrder.builder()
                            .orderLetter(DUPLICATED_CASE_DOCUMENT)
                            .build())
                        .build()))
                    .build())
                .build(),
            FinremCaseDetails.builder().build());

        assertDuplicateFilenameWarning(warnings);
    }

    @Test
    void testGetWarnings_duplicateInApprovedOrderCollection_consentOrder() throws DocumentContentCheckerException {
        List<String> warnings = underTest.getWarnings(DUPLICATED_CASE_DOCUMENT, new byte[0],
            FinremCaseDetails.builder()
                .data(FinremCaseData.builder()
                    .approvedOrderCollection(List.of(ConsentOrderCollection.builder()
                        .approvedOrder(ApprovedOrder.builder()
                            .consentOrder(DUPLICATED_CASE_DOCUMENT)
                            .build())
                        .build()))
                    .build())
                .build(),
            FinremCaseDetails.builder().build());

        assertDuplicateFilenameWarning(warnings);
    }

    @Test
    void testGetWarnings_duplicateInApprovedOrderCollection_pensionDocuments() throws DocumentContentCheckerException {
        List<String> warnings = underTest.getWarnings(DUPLICATED_CASE_DOCUMENT, new byte[0],
            FinremCaseDetails.builder()
                .data(FinremCaseData.builder()
                    .approvedOrderCollection(List.of(ConsentOrderCollection.builder()
                        .approvedOrder(ApprovedOrder.builder()
                            .pensionDocuments(List.of(PensionTypeCollection.builder()
                                .typedCaseDocument(PensionType.builder()
                                    .pensionDocument(DUPLICATED_CASE_DOCUMENT)
                                    .build())
                                .build()))
                            .build())
                        .build()))
                    .build())
                .build(),
            FinremCaseDetails.builder().build());

        assertDuplicateFilenameWarning(warnings);
    }

    @Test
    void testGetWarnings_duplicateInScannedD81s() throws DocumentContentCheckerException {
        List<String> warnings = underTest.getWarnings(DUPLICATED_CASE_DOCUMENT, new byte[0],
            FinremCaseDetails.builder()
                .data(FinremCaseData.builder()
                    .scannedD81s(List.of(DocumentCollection.builder()
                        .value(DUPLICATED_CASE_DOCUMENT)
                        .build()))
                    .build())
                .build(),
            FinremCaseDetails.builder().build());

        assertDuplicateFilenameWarning(warnings);
    }

    @Test
    void testGetWarnings_duplicateInAdditionalHearingDocuments() throws DocumentContentCheckerException {
        List<String> warnings = underTest.getWarnings(DUPLICATED_CASE_DOCUMENT, new byte[0],
            FinremCaseDetails.builder()
                .data(FinremCaseData.builder()
                    .additionalHearingDocuments(List.of(AdditionalHearingDocumentCollection.builder()
                        .value(AdditionalHearingDocument.builder()
                            .document(DUPLICATED_CASE_DOCUMENT)
                            .build())
                        .build()))
                    .build())
                .build(),
            FinremCaseDetails.builder().build());

        assertDuplicateFilenameWarning(warnings);
    }

    @Test
    void testGetWarnings_duplicateInHearingNoticeDocumentPack() throws DocumentContentCheckerException {
        List<String> warnings = underTest.getWarnings(DUPLICATED_CASE_DOCUMENT, new byte[0],
            FinremCaseDetails.builder()
                .data(FinremCaseData.builder()
                    .hearingNoticeDocumentPack(List.of(DocumentCollection.builder()
                        .value(DUPLICATED_CASE_DOCUMENT)
                        .build()))
                    .build())
                .build(),
            FinremCaseDetails.builder().build());

        assertDuplicateFilenameWarning(warnings);
    }

    @Test
    void testGetWarnings_duplicateInHearingNoticesDocumentCollection() throws DocumentContentCheckerException {
        List<String> warnings = underTest.getWarnings(DUPLICATED_CASE_DOCUMENT, new byte[0],
            FinremCaseDetails.builder()
                .data(FinremCaseData.builder()
                    .hearingNoticesDocumentCollection(List.of(DocumentCollection.builder()
                        .value(DUPLICATED_CASE_DOCUMENT)
                        .build()))
                    .build())
                .build(),
            FinremCaseDetails.builder().build());

        assertDuplicateFilenameWarning(warnings);
    }

    @Test
    void testGetWarnings_duplicateInUploadGeneralDocuments() throws DocumentContentCheckerException {
        List<String> warnings = underTest.getWarnings(DUPLICATED_CASE_DOCUMENT, new byte[0],
            FinremCaseDetails.builder()
                .data(FinremCaseData.builder()
                    .uploadGeneralDocuments(List.of(UploadGeneralDocumentCollection.builder()
                        .value(UploadGeneralDocument.builder()
                            .documentLink(DUPLICATED_CASE_DOCUMENT)
                            .build())
                        .build()))
                    .build())
                .build(),
            FinremCaseDetails.builder().build());

        assertDuplicateFilenameWarning(warnings);
    }

    @Test
    void testGetWarnings_duplicateInUploadHearingOrder() throws DocumentContentCheckerException {
        List<String> warnings = underTest.getWarnings(DUPLICATED_CASE_DOCUMENT, new byte[0],
            FinremCaseDetails.builder()
                .data(FinremCaseData.builder()
                    .uploadHearingOrder(List.of(DirectionOrderCollection.builder()
                        .value(DirectionOrder.builder()
                            .uploadDraftDocument(DUPLICATED_CASE_DOCUMENT)
                            .build())
                        .build()))
                    .build())
                .build(),
            FinremCaseDetails.builder().build());

        assertDuplicateFilenameWarning(warnings);
    }

    @Test
    void testGetWarnings_duplicateInHearingOrderOtherDocuments() throws DocumentContentCheckerException {
        List<String> warnings = underTest.getWarnings(DUPLICATED_CASE_DOCUMENT, new byte[0],
            FinremCaseDetails.builder()
                .data(FinremCaseData.builder()
                    .hearingOrderOtherDocuments(List.of(DocumentCollection.builder()
                        .value(DUPLICATED_CASE_DOCUMENT)
                        .build()))
                    .build())
                .build(),
            FinremCaseDetails.builder().build());

        assertDuplicateFilenameWarning(warnings);
    }

    @Test
    void testGetWarnings_duplicateInIntv1HearingNoticesCollection() throws DocumentContentCheckerException {
        List<String> warnings = underTest.getWarnings(DUPLICATED_CASE_DOCUMENT, new byte[0],
            FinremCaseDetails.builder()
                .data(FinremCaseData.builder()
                    .intv1HearingNoticesCollection(List.of(IntervenerHearingNoticeCollection.builder()
                        .value(IntervenerHearingNotice.builder()
                            .caseDocument(DUPLICATED_CASE_DOCUMENT)
                            .build())
                        .build()))
                    .build())
                .build(),
            FinremCaseDetails.builder().build());

        assertDuplicateFilenameWarning(warnings);
    }

    @Test
    void testGetWarnings_duplicateInIntv2HearingNoticesCollection() throws DocumentContentCheckerException {
        List<String> warnings = underTest.getWarnings(DUPLICATED_CASE_DOCUMENT, new byte[0],
            FinremCaseDetails.builder()
                .data(FinremCaseData.builder()
                    .intv2HearingNoticesCollection(List.of(IntervenerHearingNoticeCollection.builder()
                        .value(IntervenerHearingNotice.builder()
                            .caseDocument(DUPLICATED_CASE_DOCUMENT)
                            .build())
                        .build()))
                    .build())
                .build(),
            FinremCaseDetails.builder().build());

        assertDuplicateFilenameWarning(warnings);
    }

    @Test
    void testGetWarnings_duplicateInIntv3HearingNoticesCollection() throws DocumentContentCheckerException {
        List<String> warnings = underTest.getWarnings(DUPLICATED_CASE_DOCUMENT, new byte[0],
            FinremCaseDetails.builder()
                .data(FinremCaseData.builder()
                    .intv3HearingNoticesCollection(List.of(IntervenerHearingNoticeCollection.builder()
                        .value(IntervenerHearingNotice.builder()
                            .caseDocument(DUPLICATED_CASE_DOCUMENT)
                            .build())
                        .build()))
                    .build())
                .build(),
            FinremCaseDetails.builder().build());

        assertDuplicateFilenameWarning(warnings);
    }

    @Test
    void testGetWarnings_duplicateInIntv4HearingNoticesCollection() throws DocumentContentCheckerException {
        List<String> warnings = underTest.getWarnings(DUPLICATED_CASE_DOCUMENT, new byte[0],
            FinremCaseDetails.builder()
                .data(FinremCaseData.builder()
                    .intv4HearingNoticesCollection(List.of(IntervenerHearingNoticeCollection.builder()
                        .value(IntervenerHearingNotice.builder()
                            .caseDocument(DUPLICATED_CASE_DOCUMENT)
                            .build())
                        .build()))
                    .build())
                .build(),
            FinremCaseDetails.builder().build());

        assertDuplicateFilenameWarning(warnings);
    }

    @Test
    void testGetWarnings_duplicateInRefusalOrderCollection() throws DocumentContentCheckerException {
        List<String> warnings = underTest.getWarnings(DUPLICATED_CASE_DOCUMENT, new byte[0],
            FinremCaseDetails.builder()
                .data(FinremCaseData.builder()
                    .refusalOrderCollection(List.of(RefusalOrderCollection.builder()
                        .value(RefusalOrder.builder()
                            .refusalOrderAdditionalDocument(DUPLICATED_CASE_DOCUMENT)
                            .build())
                        .build()))
                    .build())
                .build(),
            FinremCaseDetails.builder().build());

        assertDuplicateFilenameWarning(warnings);
    }

    @Test
    void testGetWarnings_duplicateInUploadAdditionalDocument() throws DocumentContentCheckerException {
        List<String> warnings = underTest.getWarnings(DUPLICATED_CASE_DOCUMENT, new byte[0],
            FinremCaseDetails.builder()
                .data(FinremCaseData.builder()
                    .uploadAdditionalDocument(List.of(UploadAdditionalDocumentCollection.builder()
                        .value(UploadAdditionalDocument.builder()
                            .additionalDocuments(DUPLICATED_CASE_DOCUMENT)
                            .build())
                        .build()))
                    .build())
                .build(),
            FinremCaseDetails.builder().build());

        assertDuplicateFilenameWarning(warnings);
    }

    @Test
    void testGetWarnings_duplicateInCconfidentialDocumentsUploaded() throws DocumentContentCheckerException {
        List<String> warnings = underTest.getWarnings(DUPLICATED_CASE_DOCUMENT, new byte[0],
            FinremCaseDetails.builder()
                .data(FinremCaseData.builder()
                    .confidentialDocumentsUploaded(List.of(ConfidentialUploadedDocumentData.builder()
                        .value(UploadConfidentialDocument.builder()
                            .documentLink(DUPLICATED_CASE_DOCUMENT)
                            .build())
                        .build()))
                    .build())
                .build(),
            FinremCaseDetails.builder().build());

        assertDuplicateFilenameWarning(warnings);
    }

    @Test
    void testGetWarnings_duplicateInManageCaseDocumentCollection() throws DocumentContentCheckerException {
        List<String> warnings = underTest.getWarnings(DUPLICATED_CASE_DOCUMENT, new byte[0],
            FinremCaseDetails.builder()
                .data(FinremCaseData.builder()
                    .manageCaseDocumentCollection(List.of(UploadCaseDocumentCollection.builder()
                        .uploadCaseDocument(UploadCaseDocument.builder()
                            .caseDocuments(DUPLICATED_CASE_DOCUMENT)
                            .build())
                        .build()))
                    .build())
                .build(),
            FinremCaseDetails.builder().build());

        assertDuplicateFilenameWarning(warnings);
    }

    @Test
    void testGetWarnings_duplicateInUploadCaseDocumentWrapper_uploadCaseDocument() throws DocumentContentCheckerException {
        List<String> warnings = underTest.getWarnings(DUPLICATED_CASE_DOCUMENT, new byte[0],
            FinremCaseDetails.builder()
                .data(FinremCaseData.builder()
                    .uploadCaseDocumentWrapper(UploadCaseDocumentWrapper.builder()
                        .uploadCaseDocument(List.of(UploadCaseDocumentCollection.builder()
                            .uploadCaseDocument(UploadCaseDocument.builder()
                                .caseDocuments(DUPLICATED_CASE_DOCUMENT)
                                .build())
                            .build()))
                        .build())
                    .build())
                .build(),
            FinremCaseDetails.builder().build());

        assertDuplicateFilenameWarning(warnings);
    }

    @Test
    void testGetWarnings_duplicateInUploadCaseDocumentWrapper_intv3QaShared() throws DocumentContentCheckerException {
        List<String> warnings = underTest.getWarnings(DUPLICATED_CASE_DOCUMENT, new byte[0],
            FinremCaseDetails.builder()
                .data(FinremCaseData.builder()
                    .uploadCaseDocumentWrapper(UploadCaseDocumentWrapper.builder()
                        .intv3QaShared(List.of(UploadCaseDocumentCollection.builder()
                            .uploadCaseDocument(UploadCaseDocument.builder()
                                .caseDocuments(DUPLICATED_CASE_DOCUMENT)
                                .build())
                            .build()))
                        .build())
                    .build())
                .build(),
            FinremCaseDetails.builder().build());

        assertDuplicateFilenameWarning(warnings);
    }

    @Test
    void testGetWarnings_duplicateInGeneralApplicationWrapper_generalApplicationDirectionsDocument() throws DocumentContentCheckerException {
        List<String> warnings = underTest.getWarnings(DUPLICATED_CASE_DOCUMENT, new byte[0],
            FinremCaseDetails.builder()
                .data(FinremCaseData.builder()
                    .generalApplicationWrapper(GeneralApplicationWrapper.builder()
                        .generalApplicationDirectionsDocument(DUPLICATED_CASE_DOCUMENT)
                        .build())
                    .build())
                .build(),
            FinremCaseDetails.builder().build());

        assertDuplicateFilenameWarning(warnings);
    }

    @Test
    void testGetWarnings_duplicateInGeneralApplicationWrapper_generalApplicationDocument() throws DocumentContentCheckerException {
        List<String> warnings = underTest.getWarnings(DUPLICATED_CASE_DOCUMENT, new byte[0],
            FinremCaseDetails.builder()
                .data(FinremCaseData.builder()
                    .generalApplicationWrapper(GeneralApplicationWrapper.builder()
                        .generalApplicationDocument(DUPLICATED_CASE_DOCUMENT)
                        .build())
                    .build())
                .build(),
            FinremCaseDetails.builder().build());

        assertDuplicateFilenameWarning(warnings);
    }

    @Test
    void testGetWarnings_duplicateInGeneralApplicationWrapper_generalApplicationLatestDocument() throws DocumentContentCheckerException {
        List<String> warnings = underTest.getWarnings(DUPLICATED_CASE_DOCUMENT, new byte[0],
            FinremCaseDetails.builder()
                .data(FinremCaseData.builder()
                    .generalApplicationWrapper(GeneralApplicationWrapper.builder()
                        .generalApplicationLatestDocument(DUPLICATED_CASE_DOCUMENT)
                        .build())
                    .build())
                .build(),
            FinremCaseDetails.builder().build());

        assertDuplicateFilenameWarning(warnings);
    }

    @Test
    void testGetWarnings_duplicateInGeneralApplicationWrapper_generalApplicationDraftOrder() throws DocumentContentCheckerException {
        List<String> warnings = underTest.getWarnings(DUPLICATED_CASE_DOCUMENT, new byte[0],
            FinremCaseDetails.builder()
                .data(FinremCaseData.builder()
                    .generalApplicationWrapper(GeneralApplicationWrapper.builder()
                        .generalApplicationDraftOrder(DUPLICATED_CASE_DOCUMENT)
                        .build())
                    .build())
                .build(),
            FinremCaseDetails.builder().build());

        assertDuplicateFilenameWarning(warnings);
    }

    @Test
    void testGetWarnings_duplicateInGeneralApplicationWrapper_generalApplicationIntvrOrders_generalApplicationDirectionsDocument()
        throws DocumentContentCheckerException {
        List<String> warnings = underTest.getWarnings(DUPLICATED_CASE_DOCUMENT, new byte[0],
            FinremCaseDetails.builder()
                .data(FinremCaseData.builder()
                    .generalApplicationWrapper(GeneralApplicationWrapper.builder()
                        .generalApplicationIntvrOrders(List.of(GeneralApplicationsCollection.builder()
                            .value(GeneralApplicationItems.builder()
                                .generalApplicationDirectionsDocument(DUPLICATED_CASE_DOCUMENT)
                                .build())
                            .build()))
                        .build())
                    .build())
                .build(),
            FinremCaseDetails.builder().build());

        assertDuplicateFilenameWarning(warnings);
    }

    @Test
    void testGetWarnings_duplicateInGeneralApplicationWrapper_generalApplicationIntvrOrders_generalApplicationDraftOrder()
        throws DocumentContentCheckerException {
        List<String> warnings = underTest.getWarnings(DUPLICATED_CASE_DOCUMENT, new byte[0],
            FinremCaseDetails.builder()
                .data(FinremCaseData.builder()
                    .generalApplicationWrapper(GeneralApplicationWrapper.builder()
                        .generalApplicationIntvrOrders(List.of(GeneralApplicationsCollection.builder()
                            .value(GeneralApplicationItems.builder()
                                .generalApplicationDraftOrder(DUPLICATED_CASE_DOCUMENT)
                                .build())
                            .build()))
                        .build())
                    .build())
                .build(),
            FinremCaseDetails.builder().build());

        assertDuplicateFilenameWarning(warnings);
    }

    @Test
    void testGetWarnings_duplicateInGeneralApplicationWrapper_generalApplicationIntvrOrders_generalApplicationDocument()
        throws DocumentContentCheckerException {
        List<String> warnings = underTest.getWarnings(DUPLICATED_CASE_DOCUMENT, new byte[0],
            FinremCaseDetails.builder()
                .data(FinremCaseData.builder()
                    .generalApplicationWrapper(GeneralApplicationWrapper.builder()
                        .generalApplicationIntvrOrders(List.of(GeneralApplicationsCollection.builder()
                            .value(GeneralApplicationItems.builder()
                                .generalApplicationDocument(DUPLICATED_CASE_DOCUMENT)
                                .build())
                            .build()))
                        .build())
                    .build())
                .build(),
            FinremCaseDetails.builder().build());

        assertDuplicateFilenameWarning(warnings);
    }

    @Test
    void testGetWarnings_duplicateInGeneralApplicationWrapper_generalApplications()
        throws DocumentContentCheckerException {
        List<String> warnings = underTest.getWarnings(DUPLICATED_CASE_DOCUMENT, new byte[0],
            FinremCaseDetails.builder()
                .data(FinremCaseData.builder()
                    .generalApplicationWrapper(GeneralApplicationWrapper.builder()
                        .generalApplications(List.of(GeneralApplicationsCollection.builder()
                            .value(GeneralApplicationItems.builder()
                                .generalApplicationDocument(DUPLICATED_CASE_DOCUMENT)
                                .build())
                            .build()))
                        .build())
                    .build())
                .build(),
            FinremCaseDetails.builder().build());

        assertDuplicateFilenameWarning(warnings);
    }

    @Test
    void testGetWarnings_duplicateInInterimWrapper_interimUploadAdditionalDocument()
        throws DocumentContentCheckerException {
        List<String> warnings = underTest.getWarnings(DUPLICATED_CASE_DOCUMENT, new byte[0],
            FinremCaseDetails.builder()
                .data(FinremCaseData.builder()
                    .interimWrapper(InterimWrapper.builder()
                        .interimUploadAdditionalDocument(DUPLICATED_CASE_DOCUMENT)
                        .build())
                    .build())
                .build(),
            FinremCaseDetails.builder().build());

        assertDuplicateFilenameWarning(warnings);
    }

    @Test
    void testGetWarnings_duplicateInInterimWrapper_interimHearingDirectionsDocument()
        throws DocumentContentCheckerException {
        List<String> warnings = underTest.getWarnings(DUPLICATED_CASE_DOCUMENT, new byte[0],
            FinremCaseDetails.builder()
                .data(FinremCaseData.builder()
                    .interimWrapper(InterimWrapper.builder()
                        .interimHearingDirectionsDocument(DUPLICATED_CASE_DOCUMENT)
                        .build())
                    .build())
                .build(),
            FinremCaseDetails.builder().build());

        assertDuplicateFilenameWarning(warnings);
    }

    @Test
    void testGetWarnings_duplicateInInterimWrapper_interimHearingDocuments()
        throws DocumentContentCheckerException {
        List<String> warnings = underTest.getWarnings(DUPLICATED_CASE_DOCUMENT, new byte[0],
            FinremCaseDetails.builder()
                .data(FinremCaseData.builder()
                    .interimWrapper(InterimWrapper.builder()
                        .interimHearingDocuments(List.of(InterimHearingBulkPrintDocumentsData.builder()
                            .value(InterimHearingBulkPrintDocument.builder()
                                .caseDocument(DUPLICATED_CASE_DOCUMENT)
                                .build())
                            .build()))
                        .build())
                    .build())
                .build(),
            FinremCaseDetails.builder().build());

        assertDuplicateFilenameWarning(warnings);
    }

    @Test
    void testGetWarnings_duplicateInDraftDirectionWrapper_draftDirectionOrderCollection()
        throws DocumentContentCheckerException {
        List<String> warnings = underTest.getWarnings(DUPLICATED_CASE_DOCUMENT, new byte[0],
            FinremCaseDetails.builder()
                .data(FinremCaseData.builder()
                    .draftDirectionWrapper(DraftDirectionWrapper.builder()
                        .draftDirectionOrderCollection(List.of(DraftDirectionOrderCollection.builder()
                            .value(DraftDirectionOrder.builder()
                                .uploadDraftDocument(DUPLICATED_CASE_DOCUMENT)
                                .build())
                            .build()))
                        .build())
                    .build())
                .build(),
            FinremCaseDetails.builder().build());

        assertDuplicateFilenameWarning(warnings);
    }

    @Test
    void testGetWarnings_duplicateInDraftDirectionWrapper_latestDraftDirectionOrder()
        throws DocumentContentCheckerException {
        List<String> warnings = underTest.getWarnings(DUPLICATED_CASE_DOCUMENT, new byte[0],
            FinremCaseDetails.builder()
                .data(FinremCaseData.builder()
                    .draftDirectionWrapper(DraftDirectionWrapper.builder()
                        .latestDraftDirectionOrder(DraftDirectionOrder.builder()
                            .uploadDraftDocument(DUPLICATED_CASE_DOCUMENT)
                            .build())
                        .build())
                    .build())
                .build(),
            FinremCaseDetails.builder().build());

        assertDuplicateFilenameWarning(warnings);
    }

    @Test
    void testGetWarnings_duplicateInDraftDirectionWrapper_judgesAmendedOrderCollection()
        throws DocumentContentCheckerException {
        List<String> warnings = underTest.getWarnings(DUPLICATED_CASE_DOCUMENT, new byte[0],
            FinremCaseDetails.builder()
                .data(FinremCaseData.builder()
                    .draftDirectionWrapper(DraftDirectionWrapper.builder()
                        .judgesAmendedOrderCollection(List.of(DraftDirectionOrderCollection.builder()
                            .value(DraftDirectionOrder.builder()
                                .uploadDraftDocument(DUPLICATED_CASE_DOCUMENT)
                                .build())
                            .build()))
                        .build())
                    .build())
                .build(),
            FinremCaseDetails.builder().build());

        assertDuplicateFilenameWarning(warnings);
    }
}
