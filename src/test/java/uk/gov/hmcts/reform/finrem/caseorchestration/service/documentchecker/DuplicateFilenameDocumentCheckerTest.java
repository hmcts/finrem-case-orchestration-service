package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentchecker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCaseDetailsBuilderFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.DocumentCheckContext;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AdditionalHearingDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AdditionalHearingDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AmendedConsentOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AmendedConsentOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApproveOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApproveOrdersHolder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrderConsolidateCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConfidentialUploadedDocumentData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentInContestedApprovedOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentInContestedApprovedOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentedHearingDataElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentedHearingDataWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedGeneralOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedGeneralOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Document;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationItems;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralEmailCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralEmailHolder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralLetter;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralLetterCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralOrderCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingBulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingBulkPrintDocumentsData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerHearingNotice;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerHearingNoticeCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ManageScannedDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ManageScannedDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrderRefusalCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrderRefusalHolder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrderSentToPartiesCollection;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.SendOrderDocuments;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UnapproveOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UnapprovedOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadAdditionalDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadAdditionalDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadConfidentialDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadConsentOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadConsentOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadConsentOrderDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadConsentOrderDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadGeneralDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadGeneralDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.VariationDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.VariationDocumentTypeCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.BulkPrintCoversheetWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ConsentOrderWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftDirectionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralEmailWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralLetterWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralOrderWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.InterimWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.OrderWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.UploadCaseDocumentWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DuplicateFilenameDocumentCheckerTest {

    private static final String WARNING = "A document with this filename already exists on the case";

    private static final String DUPLICATED_FILENAME = "newFilename.pdf";

    private static final CaseDocument DUPLICATED_CASE_DOCUMENT = CaseDocument.builder().documentFilename(DUPLICATED_FILENAME).build();

    private static final Document DUPLICATED_DOCUMENT = Document.builder().filename(DUPLICATED_FILENAME).build();

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

    @ParameterizedTest
    @EnumSource(value = CaseType.class, names = {"CONTESTED", "CONSENTED"})
    void testGetWarnings_noDuplication(CaseType caseType) throws DocumentContentCheckerException {
        List<String> warnings = underTest.getWarnings(DocumentCheckContext.builder()
                .caseDocument(DUPLICATED_CASE_DOCUMENT)
                .bytes(new byte[0])
                .beforeCaseDetails(FinremCaseDetailsBuilderFactory.from(caseType, FinremCaseData.builder()
                    .additionalDocument(CaseDocument.builder().documentFilename("additionalDocument").build())
                    .generalOrderWrapper(GeneralOrderWrapper.builder()
                        .generalOrderLatestDocument(CaseDocument.builder().documentFilename("generalOrderLatestDocument").build())
                        .build())).build())
                .caseDetails(FinremCaseDetailsBuilderFactory.from(caseType).build())
            .build());

        assertThat(warnings).isEmpty();
    }

    static Stream<Arguments> testGetWarningsOnExistingCase() {
        return Stream.of(
            Arguments.of(FinremCaseData.builder()
                .generalOrderWrapper(GeneralOrderWrapper.builder()
                    .generalOrderLatestDocument(DUPLICATED_CASE_DOCUMENT)
                    .build())
                .build()),
            Arguments.of(FinremCaseData.builder()
                .generalOrderWrapper(GeneralOrderWrapper.builder()
                    .generalOrderPreviewDocument(DUPLICATED_CASE_DOCUMENT)
                    .build())
                .build()),
            Arguments.of(FinremCaseData.builder()
                .generalOrderWrapper(GeneralOrderWrapper.builder()
                    .generalOrders(List.of(ContestedGeneralOrderCollection.builder()
                        .value(ContestedGeneralOrder.builder()
                            .additionalDocument(DUPLICATED_CASE_DOCUMENT)
                            .build())
                        .build()))
                    .build())
                .build()),
            Arguments.of(FinremCaseData.builder()
                .generalOrderWrapper(GeneralOrderWrapper.builder()
                    .generalOrdersConsent(List.of(ContestedGeneralOrderCollection.builder()
                        .value(ContestedGeneralOrder.builder()
                            .additionalDocument(DUPLICATED_CASE_DOCUMENT)
                            .build())
                        .build()))
                    .build())
                .build()),
            Arguments.of(FinremCaseData.builder()
                .generalOrderWrapper(GeneralOrderWrapper.builder()
                    .generalOrderCollection(List.of(GeneralOrderCollectionItem.builder()
                        .generalOrder(GeneralOrder.builder()
                            .generalOrderDocumentUpload(DUPLICATED_CASE_DOCUMENT)
                            .build())
                        .build()))
                    .build())
                .build()),
            Arguments.of(FinremCaseData.builder()
                .pensionCollection(List.of(PensionTypeCollection.builder()
                    .typedCaseDocument(PensionType.builder()
                        .pensionDocument(DUPLICATED_CASE_DOCUMENT)
                        .build())
                    .build()))
                .build()),
            Arguments.of(FinremCaseData.builder()
                    .otherDocumentsCollection(List.of(OtherDocumentCollection.builder()
                        .value(OtherDocument.builder()
                            .uploadedDocument(DUPLICATED_CASE_DOCUMENT)
                            .build())
                        .build()))
                    .build()),
            Arguments.of(FinremCaseData.builder()
                .additionalCicDocuments(List.of(DocumentCollection.builder()
                    .value(DUPLICATED_CASE_DOCUMENT)
                    .build()))
                .build()),
            Arguments.of(FinremCaseData.builder()
                .orderRefusalCollection(List.of(OrderRefusalCollection.builder()
                    .value(OrderRefusalHolder.builder()
                        .orderRefusalDocs(DUPLICATED_CASE_DOCUMENT)
                        .build())
                    .build()))
                .build()),
            Arguments.of(FinremCaseData.builder()
                .orderRefusalCollectionNew(List.of(OrderRefusalCollection.builder()
                    .value(OrderRefusalHolder.builder()
                        .orderRefusalDocs(DUPLICATED_CASE_DOCUMENT)
                        .build())
                    .build()))
                .build()),
            Arguments.of(FinremCaseData.builder()
                .orderRefusalOnScreen(OrderRefusalHolder.builder()
                    .orderRefusalDocs(DUPLICATED_CASE_DOCUMENT)
                    .build())
                .build()),
            Arguments.of(FinremCaseData.builder()
                .uploadConsentOrderDocuments(List.of(UploadConsentOrderDocumentCollection.builder()
                    .value(UploadConsentOrderDocument.builder()
                        .documentLink(DUPLICATED_CASE_DOCUMENT)
                        .build())
                    .build()))
                .build()),
            Arguments.of(FinremCaseData.builder()
                .uploadOrder(List.of(UploadOrderCollection.builder()
                    .value(UploadOrder.builder()
                        .documentLink(DUPLICATED_CASE_DOCUMENT)
                        .build())
                    .build()))
                .build()),
            Arguments.of(FinremCaseData.builder()
                .uploadDocuments(List.of(UploadDocumentCollection.builder()
                    .value(UploadDocument.builder()
                        .documentLink(DUPLICATED_CASE_DOCUMENT)
                        .build())
                    .build()))
                .build()),
            Arguments.of(FinremCaseData.builder()
                .approvedOrderCollection(List.of(ConsentOrderCollection.builder()
                    .approvedOrder(ApprovedOrder.builder()
                        .orderLetter(DUPLICATED_CASE_DOCUMENT)
                        .build())
                    .build()))
                .build()),
            Arguments.of(FinremCaseData.builder()
                .respondToOrderDocuments(List.of(RespondToOrderDocumentCollection.builder()
                    .value(RespondToOrderDocument.builder()
                        .documentLink(DUPLICATED_CASE_DOCUMENT)
                        .build())
                    .build()))
                .build()),
            Arguments.of(FinremCaseData.builder()
                .amendedConsentOrderCollection(List.of(AmendedConsentOrderCollection.builder()
                    .value(AmendedConsentOrder.builder()
                        .amendedConsentOrder(DUPLICATED_CASE_DOCUMENT)
                        .build())
                    .build()))
                .build()),
            Arguments.of(FinremCaseData.builder()
                .scannedDocuments(List.of(ScannedDocumentCollection.builder()
                    .value(ScannedDocument.builder()
                        .url(DUPLICATED_CASE_DOCUMENT)
                        .build())
                    .build()))
                .build()),
            Arguments.of(FinremCaseData.builder()
                .approvedOrderCollection(List.of(ConsentOrderCollection.builder()
                    .approvedOrder(ApprovedOrder.builder()
                        .consentOrder(DUPLICATED_CASE_DOCUMENT)
                        .build())
                    .build()))
                .build()),
            Arguments.of(FinremCaseData.builder()
                .approvedOrderCollection(List.of(ConsentOrderCollection.builder()
                    .approvedOrder(ApprovedOrder.builder()
                        .pensionDocuments(List.of(PensionTypeCollection.builder()
                            .typedCaseDocument(PensionType.builder()
                                .pensionDocument(DUPLICATED_CASE_DOCUMENT)
                                .build())
                            .build()))
                        .build())
                    .build()))
                .build()),
            Arguments.of(FinremCaseData.builder()
                .scannedD81s(List.of(DocumentCollection.builder()
                    .value(DUPLICATED_CASE_DOCUMENT)
                    .build()))
                .build()),
            Arguments.of(FinremCaseData.builder()
                .additionalHearingDocuments(List.of(AdditionalHearingDocumentCollection.builder()
                    .value(AdditionalHearingDocument.builder()
                        .document(DUPLICATED_CASE_DOCUMENT)
                        .build())
                    .build()))
                .build()),
            Arguments.of(FinremCaseData.builder()
                .hearingNoticeDocumentPack(List.of(DocumentCollection.builder()
                    .value(DUPLICATED_CASE_DOCUMENT)
                    .build()))
                .build()),
            Arguments.of(FinremCaseData.builder()
                .hearingNoticesDocumentCollection(List.of(DocumentCollection.builder()
                    .value(DUPLICATED_CASE_DOCUMENT)
                    .build()))
                .build()),
            Arguments.of(FinremCaseData.builder()
                .uploadGeneralDocuments(List.of(UploadGeneralDocumentCollection.builder()
                    .value(UploadGeneralDocument.builder()
                        .documentLink(DUPLICATED_CASE_DOCUMENT)
                        .build())
                    .build()))
                .build()),
            Arguments.of(FinremCaseData.builder()
                .uploadHearingOrder(List.of(DirectionOrderCollection.builder()
                    .value(DirectionOrder.builder()
                        .uploadDraftDocument(DUPLICATED_CASE_DOCUMENT)
                        .build())
                    .build()))
                .build()),
            Arguments.of(FinremCaseData.builder()
                .hearingOrderOtherDocuments(List.of(DocumentCollection.builder()
                    .value(DUPLICATED_CASE_DOCUMENT)
                    .build()))
                .build()),
            Arguments.of(FinremCaseData.builder()
                .intv1HearingNoticesCollection(List.of(IntervenerHearingNoticeCollection.builder()
                    .value(IntervenerHearingNotice.builder()
                        .caseDocument(DUPLICATED_CASE_DOCUMENT)
                        .build())
                    .build()))
                .build()),
            Arguments.of(FinremCaseData.builder()
                .intv2HearingNoticesCollection(List.of(IntervenerHearingNoticeCollection.builder()
                    .value(IntervenerHearingNotice.builder()
                        .caseDocument(DUPLICATED_CASE_DOCUMENT)
                        .build())
                    .build()))
                .build()),
            Arguments.of(FinremCaseData.builder()
                .intv3HearingNoticesCollection(List.of(IntervenerHearingNoticeCollection.builder()
                    .value(IntervenerHearingNotice.builder()
                        .caseDocument(DUPLICATED_CASE_DOCUMENT)
                        .build())
                    .build()))
                .build()),
            Arguments.of(FinremCaseData.builder()
                .intv4HearingNoticesCollection(List.of(IntervenerHearingNoticeCollection.builder()
                    .value(IntervenerHearingNotice.builder()
                        .caseDocument(DUPLICATED_CASE_DOCUMENT)
                        .build())
                    .build()))
                .build()),
            Arguments.of(FinremCaseData.builder()
                .refusalOrderCollection(List.of(RefusalOrderCollection.builder()
                    .value(RefusalOrder.builder()
                        .refusalOrderAdditionalDocument(DUPLICATED_CASE_DOCUMENT)
                        .build())
                    .build()))
                .build()),
            Arguments.of(FinremCaseData.builder()
                .uploadAdditionalDocument(List.of(UploadAdditionalDocumentCollection.builder()
                    .value(UploadAdditionalDocument.builder()
                        .additionalDocuments(DUPLICATED_CASE_DOCUMENT)
                        .build())
                    .build()))
                .build()),
            Arguments.of(FinremCaseData.builder()
                .confidentialDocumentsUploaded(List.of(ConfidentialUploadedDocumentData.builder()
                    .value(UploadConfidentialDocument.builder()
                        .documentLink(DUPLICATED_CASE_DOCUMENT)
                        .build())
                    .build()))
                .build()),
            Arguments.of(FinremCaseData.builder()
                .manageCaseDocumentCollection(List.of(UploadCaseDocumentCollection.builder()
                    .uploadCaseDocument(UploadCaseDocument.builder()
                        .caseDocuments(DUPLICATED_CASE_DOCUMENT)
                        .build())
                    .build()))
                .build()),
            Arguments.of(FinremCaseData.builder()
                .uploadCaseDocumentWrapper(UploadCaseDocumentWrapper.builder()
                    .uploadCaseDocument(List.of(UploadCaseDocumentCollection.builder()
                        .uploadCaseDocument(UploadCaseDocument.builder()
                            .caseDocuments(DUPLICATED_CASE_DOCUMENT)
                            .build())
                        .build()))
                    .build())
                .build()),
            Arguments.of(FinremCaseData.builder()
                .uploadCaseDocumentWrapper(UploadCaseDocumentWrapper.builder()
                    .intv3QaShared(List.of(UploadCaseDocumentCollection.builder()
                        .uploadCaseDocument(UploadCaseDocument.builder()
                            .caseDocuments(DUPLICATED_CASE_DOCUMENT)
                            .build())
                        .build()))
                    .build())
                .build()),
            Arguments.of(FinremCaseData.builder()
                .generalApplicationWrapper(GeneralApplicationWrapper.builder()
                    .generalApplicationDirectionsDocument(DUPLICATED_CASE_DOCUMENT)
                    .build())
                .build()),
            Arguments.of(FinremCaseData.builder()
                .generalApplicationWrapper(GeneralApplicationWrapper.builder()
                    .generalApplicationDocument(DUPLICATED_CASE_DOCUMENT)
                    .build())
                .build()),
            Arguments.of(FinremCaseData.builder()
                .generalApplicationWrapper(GeneralApplicationWrapper.builder()
                    .generalApplicationLatestDocument(DUPLICATED_CASE_DOCUMENT)
                    .build())
                .build()),
            Arguments.of(FinremCaseData.builder()
                .generalApplicationWrapper(GeneralApplicationWrapper.builder()
                    .generalApplicationDraftOrder(DUPLICATED_CASE_DOCUMENT)
                    .build())
                .build()),
            Arguments.of(FinremCaseData.builder()
                .generalApplicationWrapper(GeneralApplicationWrapper.builder()
                    .generalApplicationIntvrOrders(List.of(GeneralApplicationsCollection.builder()
                        .value(GeneralApplicationItems.builder()
                            .generalApplicationDirectionsDocument(DUPLICATED_CASE_DOCUMENT)
                            .build())
                        .build()))
                    .build())
                .build()),
            Arguments.of(FinremCaseData.builder()
                .generalApplicationWrapper(GeneralApplicationWrapper.builder()
                    .generalApplicationIntvrOrders(List.of(GeneralApplicationsCollection.builder()
                        .value(GeneralApplicationItems.builder()
                            .generalApplicationDraftOrder(DUPLICATED_CASE_DOCUMENT)
                            .build())
                        .build()))
                    .build())
                .build()),
            Arguments.of(FinremCaseData.builder()
                .generalApplicationWrapper(GeneralApplicationWrapper.builder()
                    .generalApplicationIntvrOrders(List.of(GeneralApplicationsCollection.builder()
                        .value(GeneralApplicationItems.builder()
                            .generalApplicationDocument(DUPLICATED_CASE_DOCUMENT)
                            .build())
                        .build()))
                    .build())
                .build()),
            Arguments.of(FinremCaseData.builder()
                .generalApplicationWrapper(GeneralApplicationWrapper.builder()
                    .generalApplications(List.of(GeneralApplicationsCollection.builder()
                        .value(GeneralApplicationItems.builder()
                            .generalApplicationDocument(DUPLICATED_CASE_DOCUMENT)
                            .build())
                        .build()))
                    .build())
                .build()),
            Arguments.of(FinremCaseData.builder()
                .interimWrapper(InterimWrapper.builder()
                    .interimUploadAdditionalDocument(DUPLICATED_CASE_DOCUMENT)
                    .build())
                .build()),
            Arguments.of(FinremCaseData.builder()
                .interimWrapper(InterimWrapper.builder()
                    .interimHearingDirectionsDocument(DUPLICATED_CASE_DOCUMENT)
                    .build())
                .build()),
            Arguments.of(FinremCaseData.builder()
                .interimWrapper(InterimWrapper.builder()
                    .interimHearingDocuments(List.of(InterimHearingBulkPrintDocumentsData.builder()
                        .value(InterimHearingBulkPrintDocument.builder()
                            .caseDocument(DUPLICATED_CASE_DOCUMENT)
                            .build())
                        .build()))
                    .build())
                .build()),
            Arguments.of(FinremCaseData.builder()
                .draftDirectionWrapper(DraftDirectionWrapper.builder()
                    .draftDirectionOrderCollection(List.of(DraftDirectionOrderCollection.builder()
                        .value(DraftDirectionOrder.builder()
                            .uploadDraftDocument(DUPLICATED_CASE_DOCUMENT)
                            .build())
                        .build()))
                    .build())
                .build()),
            Arguments.of(FinremCaseData.builder()
                .draftDirectionWrapper(DraftDirectionWrapper.builder()
                    .latestDraftDirectionOrder(DraftDirectionOrder.builder()
                        .uploadDraftDocument(DUPLICATED_CASE_DOCUMENT)
                        .build())
                    .build())
                .build()),
            Arguments.of(FinremCaseData.builder()
                .draftDirectionWrapper(DraftDirectionWrapper.builder()
                    .judgesAmendedOrderCollection(List.of(DraftDirectionOrderCollection.builder()
                        .value(DraftDirectionOrder.builder()
                            .uploadDraftDocument(DUPLICATED_CASE_DOCUMENT)
                            .build())
                        .build()))
                    .build())
                .build()),
            Arguments.of(FinremCaseData.builder()
                .generalLetterWrapper(GeneralLetterWrapper.builder()
                    .generalLetterPreview(DUPLICATED_CASE_DOCUMENT)
                    .build())
                .build()),
            Arguments.of(FinremCaseData.builder()
                .generalLetterWrapper(GeneralLetterWrapper.builder()
                    .generalLetterUploadedDocument(DUPLICATED_CASE_DOCUMENT)
                    .build())
                .build()),
            Arguments.of(FinremCaseData.builder()
                .generalLetterWrapper(GeneralLetterWrapper.builder()
                    .generalLetterUploadedDocuments(List.of(DocumentCollection.builder()
                        .value(DUPLICATED_CASE_DOCUMENT)
                        .build()))
                    .build())
                .build()),
            Arguments.of(FinremCaseData.builder()
                .generalLetterWrapper(GeneralLetterWrapper.builder()
                    .generalLetterCollection(List.of(GeneralLetterCollection.builder()
                        .value(GeneralLetter.builder()
                            .generatedLetter(DUPLICATED_CASE_DOCUMENT)
                            .build())
                        .build()))
                    .build())
                .build()),
            Arguments.of(FinremCaseData.builder()
                .generalLetterWrapper(GeneralLetterWrapper.builder()
                    .generalLetterCollection(List.of(GeneralLetterCollection.builder()
                        .value(GeneralLetter.builder()
                            .generalLetterUploadedDocument(DUPLICATED_CASE_DOCUMENT)
                            .build())
                        .build()))
                    .build())
                .build()),
            Arguments.of(FinremCaseData.builder()
                .generalLetterWrapper(GeneralLetterWrapper.builder()
                    .generalLetterCollection(List.of(GeneralLetterCollection.builder()
                        .value(GeneralLetter.builder()
                            .generalLetterUploadedDocuments(List.of(DocumentCollection.builder()
                                .value(DUPLICATED_CASE_DOCUMENT)
                                .build()))
                            .build())
                        .build()))
                    .build())
                .build()),
            Arguments.of(FinremCaseData.builder()
                .generalEmailWrapper(GeneralEmailWrapper.builder()
                    .generalEmailUploadedDocument(DUPLICATED_CASE_DOCUMENT)
                    .build())
                .build()),
            Arguments.of(FinremCaseData.builder()
                .generalEmailWrapper(GeneralEmailWrapper.builder()
                    .generalEmailCollection(List.of(GeneralEmailCollection.builder()
                        .value(GeneralEmailHolder.builder()
                            .generalEmailUploadedDocument(DUPLICATED_CASE_DOCUMENT)
                            .build())
                        .build()))
                    .build())
                .build()),
            Arguments.of(FinremCaseData.builder()
                .consentOrderWrapper(ConsentOrderWrapper.builder()
                    .consentD81Joint(DUPLICATED_CASE_DOCUMENT)
                    .build())
                .build()),
            Arguments.of(FinremCaseData.builder()
                .consentOrderWrapper(ConsentOrderWrapper.builder()
                    .consentD81Applicant(DUPLICATED_CASE_DOCUMENT)
                    .build())
                .build()),
            Arguments.of(FinremCaseData.builder()
                .consentOrderWrapper(ConsentOrderWrapper.builder()
                    .consentD81Respondent(DUPLICATED_CASE_DOCUMENT)
                    .build())
                .build()),
            Arguments.of(FinremCaseData.builder()
                .consentOrderWrapper(ConsentOrderWrapper.builder()
                    .consentMiniFormA(DUPLICATED_CASE_DOCUMENT)
                    .build())
                .build()),
            Arguments.of(FinremCaseData.builder()
                .consentOrderWrapper(ConsentOrderWrapper.builder()
                    .uploadApprovedConsentOrder(DUPLICATED_CASE_DOCUMENT)
                    .build())
                .build()),
            Arguments.of(FinremCaseData.builder()
                .consentOrderWrapper(ConsentOrderWrapper.builder()
                    .latestDraftDirectionOrder(DraftDirectionOrder.builder()
                        .uploadDraftDocument(DUPLICATED_CASE_DOCUMENT)
                        .build())
                    .build())
                .build()),
            Arguments.of(FinremCaseData.builder()
                .consentOrderWrapper(ConsentOrderWrapper.builder()
                    .consentOtherCollection(List.of(OtherDocumentCollection.builder()
                        .value(OtherDocument.builder()
                            .uploadedDocument(DUPLICATED_CASE_DOCUMENT)
                            .build())
                        .build()))
                    .build())
                .build()),
            Arguments.of(FinremCaseData.builder()
                .consentOrderWrapper(ConsentOrderWrapper.builder()
                    .consentedNotApprovedOrders(List.of(ConsentOrderCollection.builder()
                        .approvedOrder(ApprovedOrder.builder()
                            .consentOrder(DUPLICATED_CASE_DOCUMENT)
                            .build())
                        .build()))
                    .build())
                .build()),
            Arguments.of(FinremCaseData.builder()
                .consentOrderWrapper(ConsentOrderWrapper.builder()
                    .consentedNotApprovedOrders(List.of(ConsentOrderCollection.builder()
                        .approvedOrder(ApprovedOrder.builder()
                            .orderLetter(DUPLICATED_CASE_DOCUMENT)
                            .build())
                        .build()))
                    .build())
                .build()),
            Arguments.of(FinremCaseData.builder()
                .consentOrderWrapper(ConsentOrderWrapper.builder()
                    .consentedNotApprovedOrders(List.of(ConsentOrderCollection.builder()
                        .approvedOrder(ApprovedOrder.builder()
                            .pensionDocuments(List.of(PensionTypeCollection.builder()
                                .typedCaseDocument(PensionType.builder()
                                    .pensionDocument(DUPLICATED_CASE_DOCUMENT)
                                    .build())
                                .build()))
                            .build())
                        .build()))
                    .build())
                .build()),
            Arguments.of(FinremCaseData.builder()
                .consentOrderWrapper(ConsentOrderWrapper.builder()
                    .uploadConsentOrder(List.of(UploadConsentOrderCollection.builder()
                        .value(UploadConsentOrder.builder()
                            .documentLink(DUPLICATED_CASE_DOCUMENT)
                            .build())
                        .build()))
                    .build())
                .build()),
            Arguments.of(FinremCaseData.builder()
                .consentOrderWrapper(ConsentOrderWrapper.builder()
                    .otherVariationCollection(List.of(VariationDocumentTypeCollection.builder()
                        .value(VariationDocumentType.builder()
                            .uploadedDocument(DUPLICATED_DOCUMENT)
                            .build())
                        .build()))
                    .build())
                .build()),
            Arguments.of(FinremCaseData.builder()
                .consentOrderWrapper(ConsentOrderWrapper.builder()
                    .appConsentApprovedOrders(List.of(ConsentInContestedApprovedOrderCollection.builder()
                        .approvedOrder(ConsentInContestedApprovedOrder.builder()
                            .orderLetter(DUPLICATED_CASE_DOCUMENT)
                            .build())
                        .build()))
                    .build())
                .build()),
            Arguments.of(FinremCaseData.builder()
                .consentOrderWrapper(ConsentOrderWrapper.builder()
                    .appConsentApprovedOrders(List.of(ConsentInContestedApprovedOrderCollection.builder()
                        .approvedOrder(ConsentInContestedApprovedOrder.builder()
                            .consentOrder(DUPLICATED_CASE_DOCUMENT)
                            .build())
                        .build()))
                    .build())
                .build()),
            Arguments.of(FinremCaseData.builder()
                .consentOrderWrapper(ConsentOrderWrapper.builder()
                    .appConsentApprovedOrders(List.of(ConsentInContestedApprovedOrderCollection.builder()
                        .approvedOrder(ConsentInContestedApprovedOrder.builder()
                            .pensionDocuments(List.of(PensionTypeCollection.builder()
                                .typedCaseDocument(PensionType.builder()
                                    .pensionDocument(DUPLICATED_CASE_DOCUMENT)
                                    .build())
                                .build()))
                            .build())
                        .build()))
                    .build())
                .build()),
            Arguments.of(FinremCaseData.builder()
                .consentOrderWrapper(ConsentOrderWrapper.builder()
                    .appConsentApprovedOrders(List.of(ConsentInContestedApprovedOrderCollection.builder()
                        .approvedOrder(ConsentInContestedApprovedOrder.builder()
                            .additionalConsentDocuments(List.of(DocumentCollection.builder()
                                .value(DUPLICATED_CASE_DOCUMENT)
                                .build()))
                            .build())
                        .build()))
                    .build())
                .build()),
            Arguments.of(FinremCaseData.builder()
                .consentOrderWrapper(ConsentOrderWrapper.builder()
                    .appRefusedOrderCollection(List.of(UnapprovedOrderCollection.builder()
                        .value(UnapproveOrder.builder()
                            .caseDocument(DUPLICATED_CASE_DOCUMENT)
                            .build())
                        .build()))
                    .build())
                .build()),
            Arguments.of(FinremCaseData.builder()
                .orderWrapper(OrderWrapper.builder()
                    .appOrderCollections(List.of(ApprovedOrderConsolidateCollection.builder()
                        .value(ApproveOrdersHolder.builder()
                            .approveOrders(List.of(ApprovedOrderCollection.builder()
                                .value(ApproveOrder.builder()
                                    .caseDocument(DUPLICATED_CASE_DOCUMENT)
                                    .build())
                                .build()))
                            .build())
                        .build()))
                    .build())
                .build()),
            Arguments.of(FinremCaseData.builder()
                .orderWrapper(OrderWrapper.builder()
                    .appOrderCollection(List.of(ApprovedOrderCollection.builder()
                        .value(ApproveOrder.builder()
                            .caseDocument(DUPLICATED_CASE_DOCUMENT)
                            .build())
                        .build()))
                    .build())
                .build()),
            Arguments.of(FinremCaseData.builder()
                .applicantScanDocuments(List.of(ScannedDocumentCollection.builder()
                    .value(ScannedDocument.builder()
                        .url(DUPLICATED_CASE_DOCUMENT)
                        .build())
                    .build()))
                .build()),
            Arguments.of(FinremCaseData.builder()
                .respondentScanDocuments(List.of(ScannedDocumentCollection.builder()
                    .value(ScannedDocument.builder()
                        .url(DUPLICATED_CASE_DOCUMENT)
                        .build())
                    .build()))
                .build()),
            Arguments.of(FinremCaseData.builder()
                .manageScannedDocumentCollection(List.of(ManageScannedDocumentCollection.builder()
                    .manageScannedDocument(ManageScannedDocument.builder()
                        .uploadCaseDocument(UploadCaseDocument.builder()
                            .caseDocuments(DUPLICATED_CASE_DOCUMENT)
                            .build())
                        .build())
                    .build()))
                .build()),
            Arguments.of(FinremCaseData.builder()
                .ordersSentToPartiesCollection(List.of(OrderSentToPartiesCollection.builder()
                    .value(SendOrderDocuments.builder()
                        .caseDocument(DUPLICATED_CASE_DOCUMENT)
                        .build())
                    .build()))
                .build()),
            Arguments.of(FinremCaseData.builder()
                .listForHearings(List.of(ConsentedHearingDataWrapper.builder()
                    .value(ConsentedHearingDataElement.builder()
                        .hearingNotice(DUPLICATED_CASE_DOCUMENT)
                        .build())
                    .build()))
                .build()),
            Arguments.of(FinremCaseData.builder()
                .listForHearings(List.of(ConsentedHearingDataWrapper.builder()
                    .value(ConsentedHearingDataElement.builder()
                        .uploadAdditionalDocument(DUPLICATED_CASE_DOCUMENT)
                        .build())
                    .build()))
                .build()),
            Arguments.of(FinremCaseData.builder()
                .divorceUploadEvidence1(DUPLICATED_CASE_DOCUMENT)
                .build()),
            Arguments.of(FinremCaseData.builder()
                .d11(DUPLICATED_CASE_DOCUMENT)
                .build()),
            Arguments.of(FinremCaseData.builder()
                .divorceUploadEvidence2(DUPLICATED_CASE_DOCUMENT)
                .build()),
            Arguments.of(FinremCaseData.builder()
                .miniFormA(DUPLICATED_CASE_DOCUMENT)
                .build()),
            Arguments.of(FinremCaseData.builder()
                .consentOrder(DUPLICATED_CASE_DOCUMENT)
                .build()),
            Arguments.of(FinremCaseData.builder()
                .consentOrderText(DUPLICATED_CASE_DOCUMENT)
                .build()),
            Arguments.of(FinremCaseData.builder()
                .latestConsentOrder(DUPLICATED_CASE_DOCUMENT)
                .build()),
            Arguments.of(FinremCaseData.builder()
                .d81Joint(DUPLICATED_CASE_DOCUMENT)
                .build()),
            Arguments.of(FinremCaseData.builder()
                .d81Applicant(DUPLICATED_CASE_DOCUMENT)
                .build()),
            Arguments.of(FinremCaseData.builder()
                .d81Respondent(DUPLICATED_CASE_DOCUMENT)
                .build()),
            Arguments.of(FinremCaseData.builder()
                .orderDirectionOpt1(DUPLICATED_CASE_DOCUMENT)
                .build()),
            Arguments.of(FinremCaseData.builder()
                .additionalDocument(DUPLICATED_CASE_DOCUMENT)
                .build()),
            Arguments.of(FinremCaseData.builder()
                .orderRefusalPreviewDocument(DUPLICATED_CASE_DOCUMENT)
                .build()),
            Arguments.of(FinremCaseData.builder()
                .approvedConsentOrderLetter(DUPLICATED_CASE_DOCUMENT)
                .build()),
            Arguments.of(FinremCaseData.builder()
                .bulkPrintCoversheetWrapper(BulkPrintCoversheetWrapper.builder()
                    .bulkPrintCoverSheetApp(DUPLICATED_CASE_DOCUMENT)
                    .build())
                .build()),
            Arguments.of(FinremCaseData.builder()
                .bulkPrintCoversheetWrapper(BulkPrintCoversheetWrapper.builder()
                    .bulkPrintCoverSheetRes(DUPLICATED_CASE_DOCUMENT)
                    .build())
                .build()),
            Arguments.of(FinremCaseData.builder()
                .bulkPrintCoversheetWrapper(BulkPrintCoversheetWrapper.builder()
                    .bulkPrintCoverSheetIntv1(DUPLICATED_CASE_DOCUMENT)
                    .build())
                .build()),
            Arguments.of(FinremCaseData.builder()
                .bulkPrintCoversheetWrapper(BulkPrintCoversheetWrapper.builder()
                    .bulkPrintCoverSheetIntv2(DUPLICATED_CASE_DOCUMENT)
                    .build())
                .build()),
            Arguments.of(FinremCaseData.builder()
                .bulkPrintCoversheetWrapper(BulkPrintCoversheetWrapper.builder()
                    .bulkPrintCoverSheetIntv3(DUPLICATED_CASE_DOCUMENT)
                    .build())
                .build()),
            Arguments.of(FinremCaseData.builder()
                .bulkPrintCoversheetWrapper(BulkPrintCoversheetWrapper.builder()
                    .bulkPrintCoverSheetIntv4(DUPLICATED_CASE_DOCUMENT)
                    .build())
                .build()),
            Arguments.of(FinremCaseData.builder()
                .formA(DUPLICATED_CASE_DOCUMENT)
                .build()),
            Arguments.of(FinremCaseData.builder()
                .bulkPrintCoversheetWrapper(BulkPrintCoversheetWrapper.builder()
                    .bulkPrintCoverSheetAppConfidential(DUPLICATED_CASE_DOCUMENT)
                    .build())
                .build()),
            Arguments.of(FinremCaseData.builder()
                .bulkPrintCoversheetWrapper(BulkPrintCoversheetWrapper.builder()
                    .bulkPrintCoverSheetResConfidential(DUPLICATED_CASE_DOCUMENT)
                    .build())
                .build()),
            Arguments.of(FinremCaseData.builder()
                .divorceUploadPetition(DUPLICATED_CASE_DOCUMENT)
                .build()),
            Arguments.of(FinremCaseData.builder()
                .uploadMediatorDocument(DUPLICATED_CASE_DOCUMENT)
                .build()),
            Arguments.of(FinremCaseData.builder()
                .uploadMediatorDocumentPaperCase(DUPLICATED_CASE_DOCUMENT)
                .build()),
            Arguments.of(FinremCaseData.builder()
                .formC(DUPLICATED_CASE_DOCUMENT)
                .build()),
            Arguments.of(FinremCaseData.builder()
                .formG(DUPLICATED_CASE_DOCUMENT)
                .build()),
            Arguments.of(FinremCaseData.builder()
                .refusalOrderPreviewDocument(DUPLICATED_CASE_DOCUMENT)
                .build()),
            Arguments.of(FinremCaseData.builder()
                .latestRefusalOrder(DUPLICATED_CASE_DOCUMENT)
                .build()),
            Arguments.of(FinremCaseData.builder()
                .refusalOrderAdditionalDocument(DUPLICATED_CASE_DOCUMENT)
                .build()),
            Arguments.of(FinremCaseData.builder()
                .latestDraftHearingOrder(DUPLICATED_CASE_DOCUMENT)
                .build()),
            Arguments.of(FinremCaseData.builder()
                .orderApprovedCoverLetter(DUPLICATED_CASE_DOCUMENT)
                .build()),
            Arguments.of(FinremCaseData.builder()
                .outOfFamilyCourtResolution(DUPLICATED_CASE_DOCUMENT)
                .build()),
            Arguments.of(FinremCaseData.builder()
                .additionalListOfHearingDocuments(DUPLICATED_CASE_DOCUMENT)
                .build()),
            Arguments.of(FinremCaseData.builder()
                .variationOrderDocument(DUPLICATED_CASE_DOCUMENT)
                .build()),
            Arguments.of(FinremCaseData.builder()
                .consentVariationOrderDocument(DUPLICATED_CASE_DOCUMENT)
                .build())
        );
    }

    @ParameterizedTest
    @MethodSource
    void testGetWarningsOnExistingCase(FinremCaseData caseData)
        throws DocumentContentCheckerException {
        List<String> warnings = underTest.getWarnings(DocumentCheckContext.builder()
            .caseDocument(DUPLICATED_CASE_DOCUMENT)
            .bytes(new byte[0])
            .beforeCaseDetails(FinremCaseDetails.builder().data(caseData).build())
            .caseDetails(FinremCaseDetails.builder().data(
                caseData.toBuilder().uploadDocuments(
                    Stream.concat(
                        Optional.ofNullable(caseData.getUploadDocuments()).orElse(new ArrayList<>()).stream(),
                        Stream.of(UploadDocumentCollection.builder().value(UploadDocument.builder()
                            .documentLink(DUPLICATED_CASE_DOCUMENT)
                            .build()).build())
                    ).toList()
                ).build()
            ).build())
            .build());
        assertDuplicateFilenameWarning(warnings);
    }

    @Test
    void testGetWarningsOnUploadingDuplicatedFilesAtTheSameTimeConsented() throws DocumentContentCheckerException {
        List<String> warnings = underTest.getWarnings(DocumentCheckContext.builder()
            .caseDocument(DUPLICATED_CASE_DOCUMENT)
            .bytes(new byte[0])
            .beforeCaseDetails(FinremCaseDetailsBuilderFactory.from(CaseType.CONSENTED).build())
            .caseDetails(FinremCaseDetailsBuilderFactory.from(CaseType.CONSENTED, FinremCaseData.builder()
                .uploadDocuments(List.of(
                    UploadDocumentCollection.builder().value(UploadDocument.builder()
                            .documentComment("1")
                            .documentLink(DUPLICATED_CASE_DOCUMENT)
                            .build())
                        .build(),
                    UploadDocumentCollection.builder().value(UploadDocument.builder()
                            .documentComment("2")
                            .documentLink(DUPLICATED_CASE_DOCUMENT)
                            .build())
                        .build()
                ))).build())
            .build());
        assertDuplicateFilenameWarning(warnings);
    }

    @Test
    void shouldNotGetWarningsOnUploadingDuplicatedFilesAtTheSameTimeConsented() throws DocumentContentCheckerException {
        List<String> warnings = underTest.getWarnings(DocumentCheckContext.builder()
            .caseDocument(DUPLICATED_CASE_DOCUMENT)
            .bytes(new byte[0])
            .beforeCaseDetails(FinremCaseDetailsBuilderFactory.from(CaseType.CONSENTED).build())
            .caseDetails(FinremCaseDetailsBuilderFactory.from(CaseType.CONSENTED, FinremCaseData.builder()
                .uploadDocuments(List.of(
                    UploadDocumentCollection.builder().value(UploadDocument.builder()
                            .documentComment("1")
                            .documentLink(DUPLICATED_CASE_DOCUMENT)
                            .build())
                        .build()
                ))).build())
            .build());

        assertThat(warnings).isEmpty();
    }

    @Test
    void testGetWarningsOnUploadingDuplicatedFilesAtTheSameTimeContested() throws DocumentContentCheckerException {
        List<String> warnings = underTest.getWarnings(DocumentCheckContext.builder()
            .caseDocument(DUPLICATED_CASE_DOCUMENT)
            .bytes(new byte[0])
            .beforeCaseDetails(FinremCaseDetailsBuilderFactory.from(CaseType.CONTESTED).build())
            .caseDetails(FinremCaseDetailsBuilderFactory.from(CaseType.CONTESTED, FinremCaseData.builder()
                .uploadGeneralDocuments(List.of(
                    UploadGeneralDocumentCollection.builder().value(UploadGeneralDocument.builder()
                            .documentComment("1")
                            .documentLink(DUPLICATED_CASE_DOCUMENT)
                            .build())
                        .build(),
                    UploadGeneralDocumentCollection.builder().value(UploadGeneralDocument.builder()
                            .documentComment("2")
                            .documentLink(DUPLICATED_CASE_DOCUMENT)
                            .build())
                        .build()
                ))).build())
            .build());
        assertDuplicateFilenameWarning(warnings);
    }

    @Test
    void shouldNotGetWarningsOnUploadingDuplicatedFilesAtTheSameTimeContested() throws DocumentContentCheckerException {
        List<String> warnings = underTest.getWarnings(DocumentCheckContext.builder()
            .caseDocument(DUPLICATED_CASE_DOCUMENT)
            .bytes(new byte[0])
            .beforeCaseDetails(FinremCaseDetailsBuilderFactory.from(CaseType.CONTESTED).build())
            .caseDetails(FinremCaseDetailsBuilderFactory.from(CaseType.CONTESTED, FinremCaseData.builder()
                .uploadGeneralDocuments(List.of(
                    UploadGeneralDocumentCollection.builder().value(UploadGeneralDocument.builder()
                            .documentComment("1")
                            .documentLink(DUPLICATED_CASE_DOCUMENT)
                            .build())
                        .build()
                ))).build())
            .build());

        assertThat(warnings).isEmpty();
    }
}
