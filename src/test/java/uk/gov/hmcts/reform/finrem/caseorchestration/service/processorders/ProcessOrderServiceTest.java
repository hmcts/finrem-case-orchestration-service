package uk.gov.hmcts.reform.finrem.caseorchestration.service.processorders;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.agreed.AgreedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.agreed.AgreedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrderDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrderDocumentReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrdersReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrdersReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.PsaDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.PsaDocumentReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.draftorders.HasApprovableCollectionReader;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.processorder.ProcessOrderService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo.NO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus.APPROVED_BY_JUDGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus.TO_BE_REVIEWED;

@ExtendWith(MockitoExtension.class)
class ProcessOrderServiceTest {

    @Spy
    private HasApprovableCollectionReader hasApprovableCollectionReader = new HasApprovableCollectionReader();

    @InjectMocks
    private ProcessOrderService underTest;

    @Test
    void givenPopulateUnprocessedUploadHearingDocuments_shouldPopulateOnlyUnstampedOrders() {
        CaseDocument unstampedDocument = caseDocument("unstamped.pdf");
        DirectionOrderCollection stampedOrder = DirectionOrderCollection.builder()
            .value(DirectionOrder.builder()
                .isOrderStamped(YesOrNo.YES)
                .uploadDraftDocument(CaseDocument.builder().documentFilename("stamped.pdf").build())
                .build())
            .build();

        DirectionOrderCollection unstampedOrder = DirectionOrderCollection.builder()
            .value(DirectionOrder.builder()
                .isOrderStamped(YesOrNo.NO)
                .uploadDraftDocument(unstampedDocument)
                .build())
            .build();

        DirectionOrderCollection unstampedOrder2 = DirectionOrderCollection.builder()
            .value(DirectionOrder.builder()
                .isOrderStamped(null)
                .uploadDraftDocument(unstampedDocument)
                .build())
            .build();

        FinremCaseData caseData = FinremCaseData.builder()
            .uploadHearingOrder(List.of(stampedOrder, unstampedOrder, unstampedOrder2))
            .build();

        underTest.populateUnprocessedUploadHearingDocuments(caseData);

        List<DirectionOrderCollection> result = caseData.getUnprocessedUploadHearingDocuments();
        assertThat(result)
            .hasSize(2)
            .extracting(DirectionOrderCollection::getValue)
            .extracting(DirectionOrder::getUploadDraftDocument)
            .containsExactly(unstampedDocument, unstampedDocument);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testPopulateUnprocessedApprovedDocuments() {
        // Prepare mock objects
        FinremCaseData caseData = mock(FinremCaseData.class);
        DraftOrdersWrapper draftOrdersWrapper = mock(DraftOrdersWrapper.class);
        CaseDocument doc1 = createCaseDocument("http://doc1.pdf");
        CaseDocument doc2 = createCaseDocument("http://doc2.pdf");
        CaseDocument doc3 = createCaseDocument("http://doc3.pdf");
        CaseDocument doc4 = createCaseDocument("http://doc4.pdf");

        List<DraftOrderDocReviewCollection> draftOrderDocs = List.of(
            createDraftOrderDocReview(APPROVED_BY_JUDGE, doc1, LocalDateTime.of(2024, 1, 1, 12, 0, 0)),
            createDraftOrderDocReview(TO_BE_REVIEWED, doc2, LocalDateTime.of(2024, 1, 2, 12, 0, 0))
        );

        List<PsaDocReviewCollection> psaDocs = List.of(
            createPsaDocReview(APPROVED_BY_JUDGE, doc3, LocalDateTime.of(2024, 1, 3, 12, 0, 0)),
            createPsaDocReview(TO_BE_REVIEWED, doc4, LocalDateTime.of(2024, 1, 4, 12, 0, 0))
        );

        // Mock behaviour of the reader
        List<DraftOrdersReviewCollection> draftOrdersReviewCollections = List.of(
            DraftOrdersReviewCollection.builder().value(DraftOrdersReview.builder()
                .draftOrderDocReviewCollection(draftOrderDocs)
                .psaDocReviewCollection(psaDocs).build()).build()
        );
        when(draftOrdersWrapper.getDraftOrdersReviewCollection()).thenReturn(draftOrdersReviewCollections);
        when(caseData.getDraftOrdersWrapper()).thenReturn(draftOrdersWrapper);
        doAnswer(invocation -> {
            ((List<DraftOrderDocReviewCollection>) invocation.getArgument(1)).add(draftOrderDocs.get(0)); // Collect only the approved document
            return null;
        }).when(hasApprovableCollectionReader)
            .filterAndCollectDraftOrderDocs(eq(draftOrdersReviewCollections), anyList(), any());
        doAnswer(invocation -> {
            ((List<PsaDocReviewCollection>) invocation.getArgument(1)).add(psaDocs.get(0)); // Collect only the approved document
            return null;
        }).when(hasApprovableCollectionReader)
            .filterAndCollectPsaDocs(eq(draftOrdersReviewCollections), anyList(), any());

        // Execute the method
        underTest.populateUnprocessedApprovedDocuments(caseData);

        // Verify the result
        verify(draftOrdersWrapper).setUnprocessedApprovedDocuments(argThat(result -> {
            assertEquals(2, result.size());

            DirectionOrder firstDirectionOrder = result.get(0).getValue();
            assertEquals("http://doc1.pdf", firstDirectionOrder.getOriginalDocument().getDocumentUrl());
            assertEquals(LocalDateTime.of(2024, 1, 1, 12, 0, 0), firstDirectionOrder.getOrderDateTime());
            assertEquals(NO, firstDirectionOrder.getIsOrderStamped());
            assertEquals(doc1, firstDirectionOrder.getUploadDraftDocument());
            assertEquals(firstDirectionOrder.getOriginalDocument(), firstDirectionOrder.getUploadDraftDocument());

            DirectionOrder secondDirectionOrder = result.get(1).getValue();
            assertEquals("http://doc3.pdf", secondDirectionOrder.getOriginalDocument().getDocumentUrl());
            assertEquals(LocalDateTime.of(2024, 1, 3, 12, 0, 0), secondDirectionOrder.getOrderDateTime());
            assertEquals(NO, secondDirectionOrder.getIsOrderStamped());
            assertEquals(doc3, secondDirectionOrder.getUploadDraftDocument());
            assertEquals(secondDirectionOrder.getOriginalDocument(), secondDirectionOrder.getUploadDraftDocument());

            return true;
        }));
    }

    @ParameterizedTest
    @MethodSource("provideAreAllNewUploadedOrdersPdfDocumentsPresentTestCases")
    void testAreAllNewUploadedOrdersPdfDocumentsPresent(FinremCaseData caseData, boolean expectedResult) {
        boolean result = underTest.areAllNewOrdersWordOrPdfFiles(caseData);
        assertEquals(expectedResult, result);
    }

    private static Stream<Arguments> provideAreAllNewUploadedOrdersPdfDocumentsPresentTestCases() {
        DirectionOrderCollection existingWordOrder = createDirectionOrder("http://example.com/document1.DOCX", true);
        DirectionOrderCollection legacyExistingPdfOrder = createDirectionOrder("http://example.com/document1.pdf");

        DirectionOrderCollection newPdfOrder1 = createDirectionOrder("http://example.com/document2.PDF");
        DirectionOrderCollection newPdfOrder2 = createDirectionOrder("http://example.com/document3.pdf");
        DirectionOrderCollection newWordOrder2 = createDirectionOrder("http://example.com/document4.doc");
        DirectionOrderCollection newWordOrder3 = createDirectionOrder("http://example.com/document5.docx");
        DirectionOrderCollection excelFile = createDirectionOrder("http://example.com/document4.xlxs");

        return Stream.of(
            // All new documents are valid (.pdf, .doc, .docx)
            Arguments.of(createCaseData(List.of(existingWordOrder, newPdfOrder1, newPdfOrder2), false), true),
            Arguments.of(createCaseData(List.of(legacyExistingPdfOrder, newPdfOrder1, newPdfOrder2), true), true),
            Arguments.of(createCaseData(List.of(legacyExistingPdfOrder), List.of(existingWordOrder, newPdfOrder1, newPdfOrder2)), true),
            Arguments.of(createCaseData(List.of(legacyExistingPdfOrder, newPdfOrder2), List.of(existingWordOrder, newPdfOrder1)), true),
            // No new documents
            Arguments.of(createCaseData(List.of(existingWordOrder), false), true),
            Arguments.of(createCaseData(List.of(legacyExistingPdfOrder), true), true),

            // Invalid case: new document is not .pdf, .doc, or .docx
            Arguments.of(createCaseData(List.of(existingWordOrder, excelFile), false), false),

            // Valid cases: new documents are .doc or .docx
            Arguments.of(createCaseData(List.of(existingWordOrder, newWordOrder2), false), true),
            Arguments.of(createCaseData(List.of(legacyExistingPdfOrder, newWordOrder2), true), true),
            Arguments.of(createCaseData(List.of(existingWordOrder, newWordOrder3), false), true),
            Arguments.of(createCaseData(List.of(legacyExistingPdfOrder, newWordOrder3), true), true),
            Arguments.of(createCaseData(List.of(existingWordOrder, newWordOrder2, newWordOrder3), false), true),
            Arguments.of(createCaseData(List.of(legacyExistingPdfOrder, newWordOrder2, newWordOrder3), true), true)
        );
    }

    private static Stream<Arguments> provideAreAllLegacyApprovedOrdersWordOrPdfData() {
        return Stream.of(
            Arguments.of(
                List.of(
                    createDirectionOrder("http://example.xyz/document.pdf"),
                    createDirectionOrder("http://example.xyz/document.pdf", "http://example.xyz/document_new.pdf")
                ), true
            ),
            Arguments.of(
                List.of(
                    createDirectionOrder("http://example.xyz/document.docx")
                ), true // .docx is valid
            ),
            Arguments.of(
                List.of(
                    createDirectionOrder("http://example.xyz/documentX.pdf", "http://example.xyz/document.doc")
                ), true // .doc is valid
            ),
            Arguments.of(
                List.of(
                    createDirectionOrder("http://example.xyz/documentX.pdf", "http://example.xyz/document.xlsx")
                ), false // .xlsx is NOT valid
            )
        );
    }

    @ParameterizedTest
    @MethodSource("provideAreAllLegacyApprovedOrdersWordOrPdfData")
    void testAreAllLegacyApprovedOrdersWordOrPdf(List<DirectionOrderCollection> uploadHearingOrder, boolean expectedTrue) {
        FinremCaseData caseData = FinremCaseData.builder()
            .unprocessedUploadHearingDocuments(uploadHearingOrder)
            .build();

        boolean result = underTest.areAllLegacyApprovedOrdersWordOrPdf(caseData);

        if (expectedTrue) {
            assertTrue(result, "Expected all documents to have .doc, .docx, or .pdf extensions");
        } else {
            assertFalse(result, "Expected not all documents to have valid extensions, but the method returned true.");
        }
    }

    private static Stream<Arguments> provideAreAllModifyingUnprocessedOrdersWordDocumentsTestCases() {
        return Stream.of(
            Arguments.of(
                List.of(
                    createDirectionOrder("http://example.xyz/document.docx"),
                    createDirectionOrder("http://example.xyz/document.docx", true),
                    createDirectionOrder("http://example.xyz/document.docx", "http://example.xyz/document.doc")
                ), true
            ),
            Arguments.of(
                List.of(
                    createDirectionOrder("http://example.xyz/document.pdf")
                ), true
            ),
            Arguments.of(
                List.of(
                    createDirectionOrder("http://example.xyz/documentX.docx", "http://example.xyz/document.txt")
                ), false
            )
        );
    }

    @ParameterizedTest
    @MethodSource("provideAreAllModifyingUnprocessedOrdersWordDocumentsTestCases")
    void testAreAllModifyingUnprocessedOrdersWordDocuments(List<DirectionOrderCollection> unprocessedApprovedDocuments, boolean expectedTrue) {
        // Mocking the unprocessed approved documents
        DraftOrdersWrapper draftOrdersWrapper = mock(DraftOrdersWrapper.class);
        FinremCaseData caseData = mock(FinremCaseData.class);
        when(caseData.getDraftOrdersWrapper()).thenReturn(draftOrdersWrapper);

        // Set up the mock data based on expectedResult
        when(draftOrdersWrapper.getUnprocessedApprovedDocuments()).thenReturn(unprocessedApprovedDocuments);

        // Call the method to test
        boolean result = underTest.areAllModifyingUnprocessedOrdersWordOrPdfDocuments(caseData);

        // Assert the expected result
        if (expectedTrue) {
            assertTrue(result, "Expected all documents to have .doc or .docx extensions");
        } else {
            assertFalse(result, "Expected not all documents to have .doc or .docx extensions, but the method returned true.");
        }
    }

    @Test
    void testAreAllModifyingUnprocessedOrdersWordDocumentsShouldExcludePsas() {
        String draftOrderDocumentUrl = "http://example.xyz/draftOrder.docx";
        String psaCaseDocumentUrl = "http://example.xyz/PSA-1.pdf";

        DraftOrdersWrapper draftOrdersWrapper = DraftOrdersWrapper.builder()
            .agreedDraftOrderCollection(List.of(
                AgreedDraftOrderCollection.builder()
                    .value(AgreedDraftOrder.builder().draftOrder(createCaseDocument(draftOrderDocumentUrl)).build())
                    .build(),
                AgreedDraftOrderCollection.builder()
                    .value(AgreedDraftOrder.builder().pensionSharingAnnex(createCaseDocument(psaCaseDocumentUrl)).build())
                    .build()
            ))
            .unprocessedApprovedDocuments(List.of(
                createDirectionOrder(draftOrderDocumentUrl, true),
                createDirectionOrder(psaCaseDocumentUrl, true)
            ))
            .build();
        FinremCaseData caseData = mock(FinremCaseData.class);
        when(caseData.getDraftOrdersWrapper()).thenReturn(draftOrdersWrapper);

        // Call the method to test
        boolean result = underTest.areAllModifyingUnprocessedOrdersWordOrPdfDocuments(caseData);
        assertTrue(result, "Expected all draft orders (excluding PSA) to have .doc or .docx extensions");
    }

    @ParameterizedTest
    @MethodSource("approvedOrderCollectionsToProcess")
    void testHasNoApprovedOrdersToProcess(FinremCaseData input, boolean expected) {
        boolean actual = underTest.hasNoApprovedOrdersToProcess(input);
        assertThat(actual).isEqualTo(expected);
    }

    private static Stream<Arguments> approvedOrderCollectionsToProcess() {
        DirectionOrderCollection sampleOrder = DirectionOrderCollection.builder().build();

        List<DirectionOrderCollection> noOrders = null;
        List<DirectionOrderCollection> emptyOrders = List.of();
        List<DirectionOrderCollection> oneOrder = List.of(sampleOrder);

        return Stream.of(
            // Both collections are null: expect true (no orders to process)
            Arguments.of(buildCaseData(noOrders, noOrders), true),
            // Only unprocessedApprovedDocuments has one order: expect false
            Arguments.of(buildCaseData(oneOrder, emptyOrders), false),
            // Only uploadHearingOrder has one order: expect false
            Arguments.of(buildCaseData(emptyOrders, oneOrder), false),
            // Both collections have one order: expect false
            Arguments.of(buildCaseData(oneOrder, oneOrder), false)
        );
    }

    private static FinremCaseData buildCaseData(List<DirectionOrderCollection> unprocessedApprovedDocuments,
                                                List<DirectionOrderCollection> uploadHearingOrder) {
        return FinremCaseData.builder()
            .draftOrdersWrapper(DraftOrdersWrapper.builder()
                .unprocessedApprovedDocuments(unprocessedApprovedDocuments)
                .build())
            .uploadHearingOrder(uploadHearingOrder)
            .unprocessedUploadHearingDocuments(uploadHearingOrder)
            .build();
    }

    private static String extractFileName(String url) {
        if (url == null || url.isEmpty()) {
            return null; // or throw an exception if you prefer
        }

        // Find the last '/' and extract the substring after it
        int lastSlashIndex = url.lastIndexOf('/');
        if (lastSlashIndex != -1 && lastSlashIndex < url.length() - 1) {
            return url.substring(lastSlashIndex + 1);
        }

        return null; // No file name found
    }

    private static DirectionOrderCollection createDirectionOrder(String documentUrl) {
        return createDirectionOrder(documentUrl, false);
    }

    private static DirectionOrderCollection createDirectionOrder(String documentUrl, boolean markOriginalDocument) {
        return DirectionOrderCollection.builder()
            .value(DirectionOrder.builder()
                .uploadDraftDocument(CaseDocument.builder().documentUrl(documentUrl).documentFilename(extractFileName(documentUrl)).build())
                .originalDocument(markOriginalDocument
                    ? CaseDocument.builder().documentUrl(documentUrl).documentFilename(extractFileName(documentUrl)).build() : null)
                .build())
            .build();
    }

    private static DirectionOrderCollection createDirectionOrder(String originalDocumentUrl, String newDocumentUrl) {
        return DirectionOrderCollection.builder()
            .value(DirectionOrder.builder()
                .uploadDraftDocument(CaseDocument.builder()
                    .documentUrl(originalDocumentUrl)
                    .documentFilename(extractFileName(newDocumentUrl))
                    .build())
                .originalDocument(CaseDocument.builder()
                    .documentUrl(originalDocumentUrl)
                    .documentFilename(extractFileName(originalDocumentUrl))
                    .build())
                .build())
            .build();
    }

    private static FinremCaseData createCaseData(List<DirectionOrderCollection> legacyTarget, List<DirectionOrderCollection> target) {
        return FinremCaseData.builder()
            .uploadHearingOrder(legacyTarget)
            .draftOrdersWrapper(DraftOrdersWrapper.builder().unprocessedApprovedDocuments(target).build())
            .build();
    }

    private static FinremCaseData createCaseData(List<DirectionOrderCollection> target, boolean legacy) {
        return legacy ? FinremCaseData.builder().uploadHearingOrder(target).build()
            : FinremCaseData.builder().draftOrdersWrapper(DraftOrdersWrapper.builder().unprocessedApprovedDocuments(target).build()).build();
    }

    // Helper methods to create mock objects
    private DraftOrderDocReviewCollection createDraftOrderDocReview(OrderStatus approvalStatus, CaseDocument document, LocalDateTime approvalDate) {
        DraftOrderDocReviewCollection collection = mock(DraftOrderDocReviewCollection.class);
        DraftOrderDocumentReview value = mock(DraftOrderDocumentReview.class);
        lenient().when(collection.getValue()).thenReturn(value);
        lenient().when(value.getApprovalDate()).thenReturn(approvalDate);
        lenient().when(value.getTargetDocument()).thenReturn(document);
        lenient().when(value.getOrderStatus()).thenReturn(approvalStatus);
        return collection;
    }

    private PsaDocReviewCollection createPsaDocReview(OrderStatus approvalStatus, CaseDocument document, LocalDateTime approvalDate) {
        PsaDocReviewCollection collection = mock(PsaDocReviewCollection.class);
        PsaDocumentReview value = mock(PsaDocumentReview.class);
        lenient().when(collection.getValue()).thenReturn(value);
        lenient().when(value.getApprovalDate()).thenReturn(approvalDate);
        lenient().when(value.getTargetDocument()).thenReturn(document);
        lenient().when(value.getOrderStatus()).thenReturn(approvalStatus);
        return collection;
    }

    private CaseDocument createCaseDocument(String documentUrl) {
        return CaseDocument.builder().documentFilename(extractFileName(documentUrl)).documentUrl(documentUrl).build();
    }
}
