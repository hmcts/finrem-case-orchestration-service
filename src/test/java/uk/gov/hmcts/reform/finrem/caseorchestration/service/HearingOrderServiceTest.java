package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NottinghamCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Region;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionMidlandsFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftDirectionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.UploadedDraftOrderCategoriser;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_DIVORCE_CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_JUDGE_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_RESP_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTTINGHAM;

@ExtendWith(MockitoExtension.class)
class HearingOrderServiceTest {

    private static final String DRAFT_DIRECTION_ORDER_BIN_URL = "anyDraftDirectionOrderBinaryUrl";

    private static final String FILENAME_ENDING_WITH_DOCX = "filename_ending_with.docx";

    private HearingOrderService underTest;

    @Mock
    private GenericDocumentService genericDocumentService;

    @Mock
    private OrderDateService orderDateService;

    @Mock
    private DocumentHelper documentHelper;

    @Mock
    private UploadedDraftOrderCategoriser uploadedDraftOrderCategoriser;

    @BeforeEach
    void setUp() {
        underTest = new HearingOrderService(genericDocumentService, documentHelper, orderDateService,
            uploadedDraftOrderCategoriser);
    }

    @Nested
    class StampAndStoreJudgeOrCaseworkerApprovedOrders {

        static final Boolean CASEWORKER_UAO_EVENT = true;
        static final Boolean JUDGE_UAO_EVENT = false;

        static LocalDateTime fixedDateTime = LocalDateTime.of(2025, 10, 1, 0, 0);
        static CaseDocument uao1Docx = caseDocument("UAO1.docx");
        static CaseDocument uao1Pdf = caseDocument("UAO1.pdf");
        static CaseDocument stampedUao1Pdf = caseDocument("StampedUAO1.pdf");
        static CaseDocument uao2Docx = caseDocument("UAO2.docx");
        static CaseDocument uao2Pdf = caseDocument("UAO2.pdf");
        static CaseDocument stampedUao2Pdf = caseDocument("StampedUAO2.pdf");

        static CaseDocument additionalDoc1Docx = caseDocument("additionalDoc1.docx");
        static CaseDocument additionalDoc1Pdf = caseDocument("additionalDoc1.pdf");
        static CaseDocument additionalDoc2Docx = caseDocument("additionalDoc2.docx");
        static CaseDocument additionalDoc2Pdf = caseDocument("additionalDoc2.pdf");

        List<DirectionOrderCollection> existingUploadHearingOrderIsEmpty;

        // Mocks
        List<DirectionOrderCollection> originalFinalOrderCollection = mock(ArrayList.class);
        StampType mockedStampType = mock(StampType.class);

        @BeforeEach
        void nestedSetUp() {
            when(documentHelper.getStampType(any(FinremCaseData.class))).thenReturn(mockedStampType);

            stubDocsConversionToPdf(List.of(
                Pair.of(uao1Docx, uao1Pdf),
                Pair.of(uao2Docx, uao2Pdf),
                Pair.of(additionalDoc1Docx, additionalDoc1Pdf),
                Pair.of(additionalDoc2Docx, additionalDoc2Pdf)
            ));
            stubDocsStamping(List.of(
                Pair.of(uao1Pdf, stampedUao1Pdf),
                Pair.of(uao2Pdf, stampedUao2Pdf)
            ));

            existingUploadHearingOrderIsEmpty = new ArrayList<>();
            lenient().when(orderDateService.syncCreatedDateAndMarkDocumentNotStamped(existingUploadHearingOrderIsEmpty, AUTH_TOKEN))
                .thenReturn(existingUploadHearingOrderIsEmpty);
        }

        static Stream<Arguments> givenSingleApprovedOrder_whenStampAndStore_thenStoredInExpectedProperties() {
            return Stream.of(
                Arguments.of(JUDGE_UAO_EVENT, DraftDirectionWrapper.builder()
                    .judgeApprovedOrderCollection(List.of(
                        createJudgeApprovedOrder(uao1Docx)
                    ))
                    .build()),
                Arguments.of(CASEWORKER_UAO_EVENT, DraftDirectionWrapper.builder()
                    .cwApprovedOrderCollection(List.of(
                        createCwApprovedOrder(uao1Docx)
                    ))
                    .build())
            );
        }

        @MethodSource
        @ParameterizedTest
        void givenSingleApprovedOrder_whenStampAndStore_thenStoredInExpectedProperties(final boolean isCaseworkerUao,
                                                                                       DraftDirectionWrapper draftDirectionWrapper) {
            // Arrange
            FinremCaseData finremCaseData = setupFinremCaseData(draftDirectionWrapper);

            List<DirectionOrderCollection> createdDateSyncedFinalOrderCollection = new ArrayList<>(List.of(
                createStampedDirectionOrderCollection(
                    caseDocument("existingFinalOrderOne.pdf"),
                    LocalDateTime.of(2025, 6, 4, 11, 59))
            ));
            when(orderDateService.syncCreatedDateAndMarkDocumentStamped(originalFinalOrderCollection, AUTH_TOKEN))
                .thenReturn(createdDateSyncedFinalOrderCollection);

            try (MockedStatic<LocalDateTime> mockedStatic = Mockito.mockStatic(LocalDateTime.class)) {
                mockedStatic.when(LocalDateTime::now).thenReturn(fixedDateTime);
                // Act
                doTest(finremCaseData, isCaseworkerUao);

                InOrder inOrder = Mockito.inOrder(genericDocumentService, orderDateService, documentHelper);
                inOrder.verify(orderDateService, times(isCaseworkerUao ? 1 : 0))
                    .syncCreatedDateAndMarkDocumentNotStamped(existingUploadHearingOrderIsEmpty, AUTH_TOKEN);
                inOrder.verify(orderDateService).syncCreatedDateAndMarkDocumentStamped(originalFinalOrderCollection, AUTH_TOKEN);
                inOrder.verify(genericDocumentService).convertDocumentIfNotPdfAlready(uao1Docx, AUTH_TOKEN, CASE_ID);
                inOrder.verify(genericDocumentService).stampDocument(uao1Pdf, AUTH_TOKEN, mockedStampType, CASE_ID);

                assertLatestDraftHearingOrder(finremCaseData, stampedUao1Pdf);
                assertFinalOrderCollection(finremCaseData,
                    createdDateSyncedFinalOrderCollection.getFirst(),
                    createStampedDirectionOrderCollection(stampedUao1Pdf, fixedDateTime));
                assertUploadHearingOrder(finremCaseData, createUploadHearingEntry(stampedUao1Pdf));
            }
        }

        static Stream<Arguments> givenSingleApprovedOrderWithAdditionalDocs_whenStampAndStore_thenStoredInExpectedProperties() {
            return Stream.of(
                Arguments.of(JUDGE_UAO_EVENT, DraftDirectionWrapper.builder()
                    .judgeApprovedOrderCollection(List.of(
                        createJudgeApprovedOrder(uao1Docx,
                            List.of(
                                DocumentCollectionItem.fromCaseDocument(additionalDoc1Docx),
                                DocumentCollectionItem.fromCaseDocument(additionalDoc2Docx)
                            ))
                    ))
                    .build()),
                Arguments.of(CASEWORKER_UAO_EVENT, DraftDirectionWrapper.builder()
                    .cwApprovedOrderCollection(List.of(
                        createCwApprovedOrder(uao1Docx,
                            List.of(
                                DocumentCollectionItem.fromCaseDocument(additionalDoc1Docx),
                                DocumentCollectionItem.fromCaseDocument(additionalDoc2Docx)
                            ))
                    ))
                    .build())
            );
        }

        @MethodSource
        @ParameterizedTest
        void givenSingleApprovedOrderWithAdditionalDocs_whenStampAndStore_thenStoredInExpectedProperties(
            final boolean isCaseworkerUao,
            DraftDirectionWrapper draftDirectionWrapper
        ) {
            // Arrange
            FinremCaseData finremCaseData = setupFinremCaseData(draftDirectionWrapper);

            List<DirectionOrderCollection> createdDateSyncedFinalOrderCollection = new ArrayList<>(List.of(
                createStampedDirectionOrderCollection(
                    caseDocument("existingFinalOrderOne.pdf"),
                    LocalDateTime.of(2025, 6, 4, 11, 59))
            ));
            when(orderDateService.syncCreatedDateAndMarkDocumentStamped(originalFinalOrderCollection, AUTH_TOKEN))
                .thenReturn(createdDateSyncedFinalOrderCollection);

            try (MockedStatic<LocalDateTime> mockedStatic = Mockito.mockStatic(LocalDateTime.class)) {
                mockedStatic.when(LocalDateTime::now).thenReturn(fixedDateTime);
                // Act
                doTest(finremCaseData, isCaseworkerUao);

                InOrder inOrder = Mockito.inOrder(genericDocumentService, orderDateService, documentHelper);
                inOrder.verify(orderDateService, times(isCaseworkerUao ? 1 : 0))
                    .syncCreatedDateAndMarkDocumentNotStamped(existingUploadHearingOrderIsEmpty, AUTH_TOKEN);
                verifyAdditionalDocsConversionToPdf(inOrder);
                inOrder.verify(orderDateService).syncCreatedDateAndMarkDocumentStamped(originalFinalOrderCollection, AUTH_TOKEN);
                inOrder.verify(genericDocumentService).convertDocumentIfNotPdfAlready(uao1Docx, AUTH_TOKEN, CASE_ID);
                inOrder.verify(genericDocumentService).stampDocument(uao1Pdf, AUTH_TOKEN, mockedStampType, CASE_ID);

                assertLatestDraftHearingOrder(finremCaseData, stampedUao1Pdf);
                assertFinalOrderCollection(finremCaseData,
                    createdDateSyncedFinalOrderCollection.getFirst(),
                    createStampedDirectionOrderCollection(stampedUao1Pdf, fixedDateTime,
                        List.of(
                            DocumentCollectionItem.fromCaseDocument(additionalDoc1Pdf),
                            DocumentCollectionItem.fromCaseDocument(additionalDoc2Pdf)
                        )));
                assertUploadHearingOrder(finremCaseData, createUploadHearingEntry(stampedUao1Pdf,
                    List.of(
                        DocumentCollectionItem.fromCaseDocument(additionalDoc1Pdf),
                        DocumentCollectionItem.fromCaseDocument(additionalDoc2Pdf)
                    )));
            }
        }

        static Stream<Arguments> givenMultipleApprovedOrders_whenStampAndStore_thenStoredInExpectedProperties() {
            return Stream.of(
                Arguments.of(JUDGE_UAO_EVENT, DraftDirectionWrapper.builder()
                        .judgeApprovedOrderCollection(List.of(
                            createJudgeApprovedOrder(uao1Docx, List.of(
                                DocumentCollectionItem.fromCaseDocument(additionalDoc1Docx),
                                DocumentCollectionItem.fromCaseDocument(additionalDoc2Docx)
                            )),
                            createJudgeApprovedOrder(uao2Docx)
                        ))
                        .build()
                )
                ,Arguments.of(CASEWORKER_UAO_EVENT, DraftDirectionWrapper.builder()
                    .cwApprovedOrderCollection(List.of(
                        createCwApprovedOrder(uao1Docx,
                            List.of(
                                DocumentCollectionItem.fromCaseDocument(additionalDoc1Docx),
                                DocumentCollectionItem.fromCaseDocument(additionalDoc2Docx)
                            )),
                        createCwApprovedOrder(uao2Docx)
                    ))
                    .build()
                )
            );
        }

        @ParameterizedTest
        @MethodSource
        void givenMultipleApprovedOrders_whenStampAndStore_thenStoredInExpectedProperties(
            final boolean isCaseworkerUao,
            DraftDirectionWrapper draftDirectionWrapper) {
            // Arrange
            FinremCaseData finremCaseData = setupFinremCaseData(draftDirectionWrapper);

            List<DirectionOrderCollection> createdDateSyncedFinalOrderCollection = new ArrayList<>(List.of(
                createStampedDirectionOrderCollection(
                    caseDocument("existingFinalOrderOne.pdf"),
                    LocalDateTime.of(2025, 6, 4, 11, 59))
            ));
            when(orderDateService.syncCreatedDateAndMarkDocumentStamped(originalFinalOrderCollection, AUTH_TOKEN))
                .thenReturn(createdDateSyncedFinalOrderCollection);

            try (MockedStatic<LocalDateTime> mockedStatic = Mockito.mockStatic(LocalDateTime.class)) {
                mockedStatic.when(LocalDateTime::now).thenReturn(fixedDateTime);
                // Act
                doTest(finremCaseData, isCaseworkerUao);

                InOrder inOrder = Mockito.inOrder(genericDocumentService, orderDateService, documentHelper);

                inOrder.verify(orderDateService, times(isCaseworkerUao ? 1 : 0))
                    .syncCreatedDateAndMarkDocumentNotStamped(existingUploadHearingOrderIsEmpty, AUTH_TOKEN);
                verifyAdditionalDocsConversionToPdf(inOrder);
                inOrder.verify(genericDocumentService).convertDocumentIfNotPdfAlready(uao1Docx, AUTH_TOKEN, CASE_ID);
                inOrder.verify(genericDocumentService).convertDocumentIfNotPdfAlready(uao2Docx, AUTH_TOKEN, CASE_ID);
                inOrder.verify(genericDocumentService).stampDocument(uao1Pdf, AUTH_TOKEN, mockedStampType, CASE_ID);
                inOrder.verify(genericDocumentService).stampDocument(uao2Pdf, AUTH_TOKEN, mockedStampType, CASE_ID);

                assertLatestDraftHearingOrder(finremCaseData, stampedUao2Pdf);
                assertFinalOrderCollection(finremCaseData,
                    createdDateSyncedFinalOrderCollection.getFirst(),
                    createStampedDirectionOrderCollection(stampedUao1Pdf, fixedDateTime, List.of(
                        DocumentCollectionItem.fromCaseDocument(additionalDoc1Pdf),
                        DocumentCollectionItem.fromCaseDocument(additionalDoc2Pdf)
                    )),
                    createStampedDirectionOrderCollection(stampedUao2Pdf, fixedDateTime));
                assertUploadHearingOrder(finremCaseData, createUploadHearingEntry(stampedUao1Pdf, List.of(
                        DocumentCollectionItem.fromCaseDocument(additionalDoc1Pdf),
                        DocumentCollectionItem.fromCaseDocument(additionalDoc2Pdf)
                    )),
                    createUploadHearingEntry(stampedUao2Pdf));
            }
        }

        @Test
        void givenMultipleApprovedOrdersWithSameFilename_whenJudgeUploads_thenStoredInExpectedProperties() {
            // Arrange
            FinremCaseData finremCaseData = setupFinremCaseData(DraftDirectionWrapper.builder()
                .judgeApprovedOrderCollection(List.of(
                    createJudgeApprovedOrder(uao1Docx, List.of(
                        DocumentCollectionItem.fromCaseDocument(additionalDoc1Docx),
                        DocumentCollectionItem.fromCaseDocument(additionalDoc2Docx)
                    )),
                    createJudgeApprovedOrder(uao1Docx)
                ))
                .build());

            when(genericDocumentService.stampDocument(uao1Pdf, AUTH_TOKEN, mockedStampType, CASE_ID)).thenReturn(stampedUao1Pdf);
            List<DirectionOrderCollection> createdDateSyncedFinalOrderCollection = new ArrayList<>();
            when(orderDateService.syncCreatedDateAndMarkDocumentStamped(originalFinalOrderCollection, AUTH_TOKEN))
                .thenReturn(createdDateSyncedFinalOrderCollection);

            try (MockedStatic<LocalDateTime> mockedStatic = Mockito.mockStatic(LocalDateTime.class)) {
                mockedStatic.when(LocalDateTime::now).thenReturn(fixedDateTime);
                // Act
                underTest.stampAndStoreJudgeApprovedOrders(finremCaseData, AUTH_TOKEN);

                verifyAdditionalDocsConversionToPdf(null);

                InOrder inOrder = Mockito.inOrder(genericDocumentService, orderDateService, documentHelper);
                verifyAdditionalDocsConversionToPdf(inOrder);
                inOrder.verify(orderDateService).syncCreatedDateAndMarkDocumentStamped(originalFinalOrderCollection, AUTH_TOKEN);
                inOrder.verify(genericDocumentService, times(2)).convertDocumentIfNotPdfAlready(uao1Docx, AUTH_TOKEN, CASE_ID);
                inOrder.verify(genericDocumentService, times(2)).stampDocument(uao1Pdf, AUTH_TOKEN, mockedStampType, CASE_ID);

                assertLatestDraftHearingOrder(finremCaseData, stampedUao1Pdf);
                assertFinalOrderCollection(finremCaseData,
                    createStampedDirectionOrderCollection(stampedUao1Pdf, fixedDateTime, List.of(
                        DocumentCollectionItem.fromCaseDocument(additionalDoc1Pdf),
                        DocumentCollectionItem.fromCaseDocument(additionalDoc2Pdf)
                    )),
                    createStampedDirectionOrderCollection(stampedUao1Pdf, fixedDateTime));
                assertUploadHearingOrder(finremCaseData, createUploadHearingEntry(stampedUao1Pdf, List.of(
                        DocumentCollectionItem.fromCaseDocument(additionalDoc1Pdf),
                        DocumentCollectionItem.fromCaseDocument(additionalDoc2Pdf)
                    )),
                    createUploadHearingEntry(stampedUao1Pdf));
            }
        }

        private void doTest(FinremCaseData finremCaseData, boolean isCaseworkerUao) {
            if (isCaseworkerUao) {
                underTest.stampAndStoreCwApprovedOrders(finremCaseData, AUTH_TOKEN);
            } else {
                underTest.stampAndStoreJudgeApprovedOrders(finremCaseData, AUTH_TOKEN);
            }
        }

        private void stubDocsConversionToPdf(List<Pair<CaseDocument, CaseDocument>> pairs) {
            for (Pair<CaseDocument, CaseDocument> pair : pairs) {
                lenient().when(genericDocumentService.convertDocumentIfNotPdfAlready(pair.getLeft(), AUTH_TOKEN, CASE_ID))
                    .thenReturn(pair.getRight());
            }
        }

        private void stubDocsStamping(List<Pair<CaseDocument, CaseDocument>> pairs) {
            for (Pair<CaseDocument, CaseDocument> pair : pairs) {
                lenient().when(genericDocumentService.stampDocument(pair.getLeft(), AUTH_TOKEN, mockedStampType, CASE_ID))
                    .thenReturn(pair.getRight());
            }
        }

        private void verifyAdditionalDocsConversionToPdf(InOrder inOrder) {
            inOrder.verify(genericDocumentService).convertDocumentIfNotPdfAlready(additionalDoc1Docx, AUTH_TOKEN, CASE_ID);
            inOrder.verify(genericDocumentService).convertDocumentIfNotPdfAlready(additionalDoc2Docx, AUTH_TOKEN, CASE_ID);
        }

        private void assertLatestDraftHearingOrder(FinremCaseData finremCaseData, CaseDocument expectedOrder) {
            assertThat(finremCaseData.getLatestDraftHearingOrder()).isEqualTo(expectedOrder);
        }

        private void assertUploadHearingOrder(FinremCaseData finremCaseData, DirectionOrderCollection... expectedOrder) {
            assertThat(finremCaseData.getUploadHearingOrder()).containsExactly(expectedOrder);
        }

        private void assertFinalOrderCollection(FinremCaseData finremCaseData, DirectionOrderCollection... expectedOrder) {
            assertThat(finremCaseData.getFinalOrderCollection()).containsExactly(expectedOrder);
        }

        private FinremCaseData setupFinremCaseData(DraftDirectionWrapper draftDirectionWrapper) {
            return FinremCaseData.builder()
                .ccdCaseId(CASE_ID)
                .finalOrderCollection(originalFinalOrderCollection)
                .draftDirectionWrapper(draftDirectionWrapper)
                .uploadHearingOrder(existingUploadHearingOrderIsEmpty)
                .build();
        }

        private DirectionOrderCollection createUploadHearingEntry(CaseDocument uploadDraftDocument) {
            return createUploadHearingEntry(uploadDraftDocument, null);
        }

        private DirectionOrderCollection createUploadHearingEntry(CaseDocument uploadDraftDocument,
                                                                  List<DocumentCollectionItem> additionalDocs) {
            return DirectionOrderCollection.builder()
                .value(DirectionOrder.builder()
                    .uploadDraftDocument(uploadDraftDocument)
                    .additionalDocuments(additionalDocs)
                    .build())
                .build();
        }

        private DirectionOrderCollection createStampedDirectionOrderCollection(CaseDocument uploadDraftDocument,
                                                                               LocalDateTime orderDateTime) {
            return createStampedDirectionOrderCollection(uploadDraftDocument, orderDateTime, null);
        }

        private DirectionOrderCollection createStampedDirectionOrderCollection(CaseDocument uploadDraftDocument,
                                                                               LocalDateTime orderDateTime,
                                                                               List<DocumentCollectionItem> additionalDocs) {
            return DirectionOrderCollection.builder()
                .value(DirectionOrder.builder()
                    .isOrderStamped(YesOrNo.YES)
                    .orderDateTime(orderDateTime)
                    .uploadDraftDocument(uploadDraftDocument)
                    .additionalDocuments(additionalDocs)
                    .build())
                .build();
        }

        private static DirectionOrderCollection createCwApprovedOrder(CaseDocument uploadDraftDocument) {
            return createCwApprovedOrder(uploadDraftDocument, null);
        }

        private static DirectionOrderCollection createCwApprovedOrder(CaseDocument uploadDraftDocument,
                                                                      List<DocumentCollectionItem> additionalDocs) {
            return DirectionOrderCollection.builder()
                .value(DirectionOrder.builder()
                    .uploadDraftDocument(uploadDraftDocument)
                    .additionalDocuments(additionalDocs)
                    .build())
                .build();
        }

        private static DraftDirectionOrderCollection createJudgeApprovedOrder(CaseDocument uploadDraftDocument) {
            return createJudgeApprovedOrder(uploadDraftDocument, null);
        }

        private static DraftDirectionOrderCollection createJudgeApprovedOrder(CaseDocument uploadDraftDocument,
                                                                              List<DocumentCollectionItem> additionalDocs) {
            return DraftDirectionOrderCollection.builder()
                .value(DraftDirectionOrder.builder()
                    .uploadDraftDocument(uploadDraftDocument)
                    .additionalDocuments(additionalDocs)
                    .build())
                .build();
        }
    }

    @Test
    void appendLatestDraftDirectionOrderToJudgesAmendedDirectionOrdersV2() {
        FinremCallbackRequest callbackRequest = getContestedNewCallbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData finremCaseData = caseDetails.getData();
        DraftDirectionOrder other
            = DraftDirectionOrder.builder().uploadDraftDocument(caseDocument()).purposeOfDocument("Other").build();
        finremCaseData.getDraftDirectionWrapper().setLatestDraftDirectionOrder(other);

        underTest.appendLatestDraftDirectionOrderToJudgesAmendedDirectionOrders(caseDetails);

        List<DraftDirectionOrderCollection> judgesAmendedOrderCollection
            = finremCaseData.getDraftDirectionWrapper().getJudgesAmendedOrderCollection();
        DraftDirectionOrder directionOrder = judgesAmendedOrderCollection.getFirst().getValue();
        assertEquals(caseDocument(), directionOrder.getUploadDraftDocument());
        assertEquals("Other", directionOrder.getPurposeOfDocument());
    }

    @Test
    void appendLatestDraftDirectionOrderToJudgesAmendedDirectionOrdersV2WhenNoDraft() {
        FinremCallbackRequest callbackRequest = getContestedNewCallbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData finremCaseData = caseDetails.getData();

        underTest.appendLatestDraftDirectionOrderToJudgesAmendedDirectionOrders(caseDetails);

        List<DraftDirectionOrderCollection> judgesAmendedOrderCollection
            = finremCaseData.getDraftDirectionWrapper().getJudgesAmendedOrderCollection();
        assertNull(judgesAmendedOrderCollection);
    }

    @Test
    void appendLatestDraftDirectionOrderToJudgesAmendedDirectionOrders() {
        DraftDirectionOrder latestDraftDirectionOrder = makeDraftDirectionOrder();

        FinremCaseDetails finremCaseDetails = FinremCaseDetails.builder().data(
                FinremCaseData.builder().draftDirectionWrapper(DraftDirectionWrapper.builder()
                    .latestDraftDirectionOrder(latestDraftDirectionOrder)
                    .build()).build()
            ).build();

        underTest.appendLatestDraftDirectionOrderToJudgesAmendedDirectionOrders(finremCaseDetails);

        assertThat(finremCaseDetails.getData().getDraftDirectionWrapper())
            .extracting(DraftDirectionWrapper::getJudgesAmendedOrderCollection)
            .extracting(List::getFirst)
            .extracting(DraftDirectionOrderCollection::getValue).isEqualTo(latestDraftDirectionOrder);
    }

    private DraftDirectionOrder makeDraftDirectionOrder() {
        return DraftDirectionOrder.builder().uploadDraftDocument(CaseDocument.builder()
            .documentBinaryUrl(DRAFT_DIRECTION_ORDER_BIN_URL)
            .documentFilename(FILENAME_ENDING_WITH_DOCX)
            .build()).build();
    }

    protected FinremCallbackRequest getContestedNewCallbackRequest() {
        FinremCaseData caseData = getFinremCaseData();
        caseData.getContactDetailsWrapper().setRespondentFmName("David");
        caseData.getContactDetailsWrapper().setRespondentLname("Goodman");
        caseData.setCcdCaseType(CaseType.CONTESTED);
        return FinremCallbackRequest.builder()
            .caseDetails(FinremCaseDetails.builder()
                .caseType(CaseType.CONTESTED)
                .id(Long.valueOf(CASE_ID))
                .data(caseData)
                .build())
            .build();
    }

    private FinremCaseData getFinremCaseData() {
        FinremCaseData caseData = new FinremCaseData();
        caseData.getContactDetailsWrapper().setApplicantFmName("Victoria");
        caseData.getContactDetailsWrapper().setApplicantLname("Goodman");
        caseData.getContactDetailsWrapper().setApplicantSolicitorEmail(TEST_SOLICITOR_EMAIL);
        caseData.getContactDetailsWrapper().setApplicantSolicitorName(TEST_SOLICITOR_NAME);
        caseData.getContactDetailsWrapper().setRespondentSolicitorEmail(TEST_RESP_SOLICITOR_EMAIL);
        caseData.getContactDetailsWrapper().setRespondentSolicitorName(TEST_RESP_SOLICITOR_NAME);
        caseData.getContactDetailsWrapper().setRespondentSolicitorReference(TEST_RESP_SOLICITOR_REFERENCE);
        caseData.getContactDetailsWrapper().setSolicitorReference(TEST_SOLICITOR_REFERENCE);
        caseData.setDivorceCaseNumber(TEST_DIVORCE_CASE_NUMBER);
        caseData.getGeneralApplicationWrapper().setGeneralApplicationReferToJudgeEmail(TEST_JUDGE_EMAIL);
        caseData.getRegionWrapper().getAllocatedRegionWrapper().setRegionList(Region.MIDLANDS);
        caseData.getRegionWrapper().getAllocatedRegionWrapper().setMidlandsFrcList(RegionMidlandsFrc.NOTTINGHAM);
        caseData.getRegionWrapper().getAllocatedRegionWrapper().getDefaultCourtListWrapper()
            .setNottinghamCourtList(NottinghamCourt.NOTTINGHAM_COUNTY_COURT_AND_FAMILY_COURT);
        caseData.setBulkPrintLetterIdRes(NOTTINGHAM);
        return caseData;
    }
}
