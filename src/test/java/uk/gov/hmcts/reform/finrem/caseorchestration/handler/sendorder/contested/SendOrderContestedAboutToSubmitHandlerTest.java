package uk.gov.hmcts.reform.finrem.caseorchestration.handler.sendorder.contested;

import org.apache.commons.lang3.tuple.Triple;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrderSentToPartiesCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.SendOrderDocuments;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.FinalisedOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.FinalisedOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.agreed.AgreedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.agreed.AgreedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrderDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrderDocumentReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrdersReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrdersReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.OrderToShare;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.OrderToShareCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.OrdersToSend;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.SendOrderWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderApprovedDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.DraftOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OrderDateService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.StampType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.SendOrdersCategoriser;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.sendorder.SendOrderApplicantDocumentHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.sendorder.SendOrderIntervenerFourDocumentHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.sendorder.SendOrderIntervenerOneDocumentHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.sendorder.SendOrderIntervenerThreeDocumentHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.sendorder.SendOrderIntervenerTwoDocumentHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.sendorder.SendOrderPartyDocumentHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.sendorder.SendOrderRespondentDocumentHandler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID_IN_LONG;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class SendOrderContestedAboutToSubmitHandlerTest {

    private SendOrderContestedAboutToSubmitHandler underTest;
    @Mock
    private GeneralOrderService generalOrderService;
    @Mock
    private GenericDocumentService genericDocumentService;
    @Mock
    private DocumentHelper documentHelper;
    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;
    @Mock
    private CaseDataService caseDataService;
    @Mock
    private ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private OrderDateService orderDateService;
    @Mock
    private SendOrdersCategoriser sendOrdersCategoriser;
    @Mock
    private DraftOrderService draftOrderService;
    @Mock
    private List<String> parties;
    @Mock
    private CaseType caseType;
    @Mock
    private StampType stampType;
    @Mock
    private CaseDocument orderApprovedCoverLetter;
    @Mock
    private CaseDocument stampedDoc;

    private List<SendOrderPartyDocumentHandler> handlers;

    @BeforeEach
    void setUpTest() {
        SendOrderApplicantDocumentHandler sendOrderApplicantDocumentHandler
            = spy(new SendOrderApplicantDocumentHandler(consentOrderApprovedDocumentService,
            notificationService, caseDataService));
        SendOrderRespondentDocumentHandler sendOrderRespondentDocumentHandler
            = spy(new SendOrderRespondentDocumentHandler(consentOrderApprovedDocumentService,
            notificationService, caseDataService));
        SendOrderIntervenerOneDocumentHandler sendOrderIntervenerOneDocumentHandler
            = spy(new SendOrderIntervenerOneDocumentHandler(consentOrderApprovedDocumentService,
            notificationService));
        SendOrderIntervenerTwoDocumentHandler sendOrderIntervenerTwoDocumentHandler
            = spy(new SendOrderIntervenerTwoDocumentHandler(consentOrderApprovedDocumentService,
            notificationService));
        SendOrderIntervenerThreeDocumentHandler sendOrderIntervenerThreeDocumentHandler
            = spy(new SendOrderIntervenerThreeDocumentHandler(consentOrderApprovedDocumentService,
            notificationService));
        SendOrderIntervenerFourDocumentHandler sendOrderIntervenerFourDocumentHandler
            = spy(new SendOrderIntervenerFourDocumentHandler(consentOrderApprovedDocumentService,
            notificationService));

        underTest = new SendOrderContestedAboutToSubmitHandler(finremCaseDetailsMapper,
            generalOrderService,
            draftOrderService,
            genericDocumentService,
            documentHelper,
            handlers = List.of(
                sendOrderApplicantDocumentHandler,
                sendOrderRespondentDocumentHandler,
                sendOrderIntervenerOneDocumentHandler,
                sendOrderIntervenerTwoDocumentHandler,
                sendOrderIntervenerThreeDocumentHandler,
                sendOrderIntervenerFourDocumentHandler
            ),
            orderDateService, sendOrdersCategoriser);

        lenient().when(generalOrderService.getParties(any(FinremCaseDetails.class))).thenReturn(parties);
        lenient().when(generalOrderService.hearingOrdersToShare(any(FinremCaseDetails.class), anyList()))
            .thenReturn(mock(Triple.class));
        lenient().when(documentHelper.getStampType(any(FinremCaseData.class))).thenReturn(stampType);
    }

    @Test
    void testCanHandle() {
        assertCanHandle(underTest, CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.SEND_ORDER);
    }

    @Test
    void givenEmptyApprovedOrdersToBeSent_whenHandled_shouldNotStampAnyDocumentAndSetUpHearingDocumentPack() {
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(CASE_ID_IN_LONG, caseType,
            spy(FinremCaseData.class));

        var response = underTest.handle(callbackRequest, AUTH_TOKEN);

        assertAll(
            () -> assertEquals(callbackRequest.getFinremCaseData(), response.getData()),
            () -> verifyNeverSetupDocumentOnPartiesTabs(callbackRequest.getCaseDetails()),
            () -> verify(documentHelper, never()).hasAnotherHearing(callbackRequest.getFinremCaseData()),
            () -> verify(documentHelper, never()).getHearingDocumentsAsPdfDocuments(callbackRequest.getCaseDetails(),
                AUTH_TOKEN),
            () -> verifyNoInteractions(orderDateService),
            () -> verify(genericDocumentService, never()).stampDocument(any(CaseDocument.class), eq(AUTH_TOKEN),
                any(StampType.class), eq(caseType))
        );
    }

    @Test
    void givenAnyCase_whenHandled_shouldSetUpOrderDocumentsOnPartiesTab() {
        FinremCaseDetails finremCaseDetails = FinremCaseDetails.builder()
            .data(FinremCaseData.builder().build())
            .build();

        underTest.handle(FinremCallbackRequestFactory.from(finremCaseDetails), AUTH_TOKEN);

        verifySetupDocumentOnPartiesTabs(finremCaseDetails);
    }

    @Nested
    class ResetFieldsTests {

        @Test
        void givenFinalisedOrdersCollectionEmpty_whenHandled_shouldClearTheCollection() {
            FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(
                FinremCaseData.builder()
                    .draftOrdersWrapper(DraftOrdersWrapper.builder()
                        .finalisedOrdersCollection(List.of())
                        .build())
                    .build()
            );

            var response = underTest.handle(callbackRequest, AUTH_TOKEN);

            assertThat(response.getData()).extracting(FinremCaseData::getDraftOrdersWrapper)
                .extracting(DraftOrdersWrapper::getFinalisedOrdersCollection).isNull();
        }

        @Test
        void givenFinalisedOrdersCollectionNotEmpty_whenHandled_shouldClearTheCollection() {
            FinalisedOrderCollection finalisedOrderCollection = mock(FinalisedOrderCollection.class);
            FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(
                FinremCaseData.builder()
                    .draftOrdersWrapper(DraftOrdersWrapper.builder()
                        .finalisedOrdersCollection(List.of(finalisedOrderCollection))
                        .build())
                    .build()
            );

            var response = underTest.handle(callbackRequest, AUTH_TOKEN);

            assertThat(response.getData())
                .extracting(FinremCaseData::getDraftOrdersWrapper)
                .extracting(DraftOrdersWrapper::getFinalisedOrdersCollection)
                .asInstanceOf(InstanceOfAssertFactories.list(FinalisedOrderCollection.class))
                .containsExactly(finalisedOrderCollection);
        }

        @Test
        void givenAgreedDraftOrderCollectionEmpty_whenHandled_shouldClearTheCollection() {
            FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(
                FinremCaseData.builder()
                    .draftOrdersWrapper(DraftOrdersWrapper.builder()
                        .agreedDraftOrderCollection(List.of())
                        .build())
                    .build()
            );

            var response = underTest.handle(callbackRequest, AUTH_TOKEN);

            assertThat(response.getData()).extracting(FinremCaseData::getDraftOrdersWrapper)
                .extracting(DraftOrdersWrapper::getAgreedDraftOrderCollection).isNull();
        }

        @Test
        void givenAgreedDraftOrderCollectionNotEmpty_whenHandled_shouldClearTheCollection() {
            AgreedDraftOrderCollection agreedDraftOrderCollection = mock(AgreedDraftOrderCollection.class);
            FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(
                FinremCaseData.builder()
                    .draftOrdersWrapper(DraftOrdersWrapper.builder()
                        .agreedDraftOrderCollection(List.of(agreedDraftOrderCollection))
                        .build())
                    .build()
            );

            var response = underTest.handle(callbackRequest, AUTH_TOKEN);

            assertThat(response.getData())
                .extracting(FinremCaseData::getDraftOrdersWrapper)
                .extracting(DraftOrdersWrapper::getAgreedDraftOrderCollection)
                .asInstanceOf(InstanceOfAssertFactories.list(AgreedDraftOrderCollection.class))
                .containsExactly(agreedDraftOrderCollection);
        }
    }

    @Test
    void givenAnyCase_whenHandled_shouldCallCategoriser() {
        FinremCaseData finremCaseData = spy(FinremCaseData.class);

        underTest.handle(FinremCallbackRequestFactory.from(finremCaseData), AUTH_TOKEN);

        verify(sendOrdersCategoriser).categorise(finremCaseData);
    }

    @Test
    void givenAnyCase_whenHandled_shouldClearEmptyOrdersInDraftOrdersReviewCollection() {
        FinremCaseData finremCaseData = spy(FinremCaseData.class);

        underTest.handle(FinremCallbackRequestFactory.from(finremCaseData), AUTH_TOKEN);

        verify(draftOrderService).clearEmptyOrdersInDraftOrdersReviewCollection(finremCaseData);
    }

    @Test
    void givenApprovedOrdersExist_whenHandledWithCoversheetMissing_shouldThrowException() {
        List<OrderToShareCollection> selectedOrders = List.of(
            toSelectedOrderToShare("DOC_1.pdf")
        );
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(CASE_ID_IN_LONG,
            caseType,
            FinremCaseData.builder()
                .sendOrderWrapper(SendOrderWrapper.builder()
                    .ordersToSend(OrdersToSend.builder()
                        .value(selectedOrders)
                        .build())
                    .build())
                .build()
        );

        List<CaseDocument> legacyHearingOrders = mock(List.class);
        List<CaseDocument> newProcessedOrders = mock(List.class);
        Map<CaseDocument, List<CaseDocument>> order2AttachmentMap = mock(Map.class);

        when(generalOrderService.hearingOrdersToShare(callbackRequest.getCaseDetails(),
            selectedOrders.stream().map(OrderToShareCollection::getValue).toList()))
            .thenReturn(Triple.of(legacyHearingOrders, newProcessedOrders, order2AttachmentMap));

        IllegalStateException e = assertThrows(IllegalStateException.class, () -> underTest.handle(callbackRequest, AUTH_TOKEN));
        assertThat(e).extracting(Throwable::getMessage).isEqualTo("orderApprovedCoverLetter is missing unexpectedly");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("orderDocumentsOnCaseScenarios")
    void givenApprovedOrdersExist_whenHandled_shouldSetUpOrderDocumentsOnCaseAndPrint(
        String scenarioName,
        List<CaseDocument> legacyHearingOrders,
        List<CaseDocument> newProcessedOrders,
        Map<CaseDocument, List<CaseDocument>> order2AttachmentMap,
        boolean hasAnotherHearing,
        Optional<CaseDocument> latestAdditionalHearingDocument,
        List<CaseDocument> otherHearingDocuments) {

        List<OrderToShareCollection> selectedOrders = List.of(
            toSelectedOrderToShare("DOC_1.pdf")
        );
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(CASE_ID_IN_LONG,
            caseType,
            FinremCaseData.builder()
                .orderApprovedCoverLetter(orderApprovedCoverLetter)
                .sendOrderWrapper(SendOrderWrapper.builder()
                    .ordersToSend(OrdersToSend.builder()
                        .value(selectedOrders)
                        .build())
                    .build())
                .build()
        );
        final FinremCaseData finremCaseData = callbackRequest.getFinremCaseData();

        when(generalOrderService.hearingOrdersToShare(callbackRequest.getCaseDetails(),
            selectedOrders.stream().map(OrderToShareCollection::getValue).toList()))
            .thenReturn(Triple.of(legacyHearingOrders, newProcessedOrders, order2AttachmentMap));

        when(documentHelper.hasAnotherHearing(finremCaseData)).thenReturn(hasAnotherHearing);
        lenient().when(documentHelper.getLatestAdditionalHearingDocument(finremCaseData))
            .thenReturn(latestAdditionalHearingDocument);

        when(documentHelper.getHearingDocumentsAsPdfDocuments(callbackRequest.getCaseDetails(), AUTH_TOKEN))
            .thenReturn(otherHearingDocuments);

        var response = underTest.handle(callbackRequest, AUTH_TOKEN);

        List<CaseDocument> expectedHearingDocumentPack = new ArrayList<>();
        expectedHearingDocumentPack.addAll(legacyHearingOrders);
        expectedHearingDocumentPack.addAll(newProcessedOrders);
        order2AttachmentMap.values().forEach(expectedHearingDocumentPack::addAll);
        latestAdditionalHearingDocument.ifPresent(expectedHearingDocumentPack::add);
        expectedHearingDocumentPack.addAll(otherHearingDocuments);
        verifySetUpOrderDocumentsOnCase(callbackRequest.getCaseDetails(), expectedHearingDocumentPack);

        assertThat(response.getData().getOrdersSentToPartiesCollection())
            .extracting(OrderSentToPartiesCollection::getValue)
            .extracting(SendOrderDocuments::getCaseDocument)
            .containsAll(expectedHearingDocumentPack);
    }

    private static Stream<Arguments> orderDocumentsOnCaseScenarios() {
        CaseDocument legacyHearingOrder = caseDocument("legacyHearingOrder");
        CaseDocument newProcessedOrder = caseDocument("newProcessedOrder");
        CaseDocument orderAttachmentA = caseDocument("orderAttachmentA");
        CaseDocument orderAttachmentB = caseDocument("orderAttachmentB");
        CaseDocument latestAdditionalHearingDocument = caseDocument("latestAdditionalHearingDocument");
        CaseDocument otherHearingDocument = caseDocument("otherHearingDocument");

        return Stream.of(
            Arguments.of(
                "all documents present, single attachment",
                List.of(legacyHearingOrder),
                List.of(newProcessedOrder),
                Map.of(mock(CaseDocument.class), List.of(orderAttachmentA)),
                true,
                Optional.of(latestAdditionalHearingDocument),
                List.of(otherHearingDocument)
            ),
            Arguments.of(
                "no additional hearing",
                List.of(legacyHearingOrder),
                List.of(newProcessedOrder),
                Map.of(mock(CaseDocument.class), List.of(orderAttachmentA)),
                false,
                Optional.empty(),
                List.of(otherHearingDocument)
            ),
            Arguments.of(
                "hasAnotherHearing true but no document returned",
                List.of(legacyHearingOrder),
                List.of(newProcessedOrder),
                Map.of(),
                true,
                Optional.empty(),
                List.of(otherHearingDocument)
            ),
            Arguments.of(
                "no other hearing documents",
                List.of(legacyHearingOrder),
                List.of(newProcessedOrder),
                Map.of(mock(CaseDocument.class), List.of(orderAttachmentA)),
                true,
                Optional.of(latestAdditionalHearingDocument),
                List.of()
            ),
            Arguments.of(
                "multiple orders each with multiple attachments",
                List.of(legacyHearingOrder, caseDocument("legacyHearingOrder2")),
                List.of(newProcessedOrder, caseDocument("newProcessedOrder2")),
                Map.of(
                    mock(CaseDocument.class), List.of(orderAttachmentA, orderAttachmentB),
                    mock(CaseDocument.class), List.of(caseDocument("orderAttachmentC"))
                ),
                true,
                Optional.of(latestAdditionalHearingDocument),
                List.of(otherHearingDocument, caseDocument("otherHearingDocument2"))
            ),
            Arguments.of(
                "no legacy orders, no attachments, minimal pack",
                List.of(),
                List.of(newProcessedOrder),
                Map.of(),
                false,
                Optional.empty(),
                List.of()
            ),
            Arguments.of(
                "legacy orders only, no attachments, minimal pack",
                List.of(legacyHearingOrder),
                List.of(),
                Map.of(),
                false,
                Optional.empty(),
                List.of()
            )
        );
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void givenLegacyHearingOrdersExist_whenHandled_shouldStampLegacyDocumentIfNecessaryAndPopulateToFinalOrderCollection(
        boolean isOrderAlreadyStamped) {

        List<OrderToShareCollection> selectedOrders = List.of(
            toSelectedOrderToShare("DOC_1.pdf")
        );
        List<DirectionOrderCollection> finalOrderCollection = mock(List.class);
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(CASE_ID_IN_LONG,
            caseType,
            FinremCaseData.builder()
                .uploadHearingOrder(List.of(
                    toDirectionOrderCollection("legacyHearingOrder", isOrderAlreadyStamped)
                ))
                .finalOrderCollection(finalOrderCollection)
                .orderApprovedCoverLetter(orderApprovedCoverLetter)
                .sendOrderWrapper(SendOrderWrapper.builder()
                    .ordersToSend(OrdersToSend.builder()
                        .value(selectedOrders)
                        .build())
                    .build())
                .build()
        );

        CaseDocument legacyHearingOrder = caseDocument("legacyHearingOrder");
        CaseDocument attachmentA = caseDocument("attachmentA");
        Map<CaseDocument, List<CaseDocument>> order2AttachmentMap = Map.of(legacyHearingOrder, List.of(attachmentA));

        when(generalOrderService.hearingOrdersToShare(callbackRequest.getCaseDetails(),
            selectedOrders.stream().map(OrderToShareCollection::getValue).toList()))
            .thenReturn(Triple.of(List.of(legacyHearingOrder), List.of(), order2AttachmentMap));

        when(orderDateService.syncCreatedDateAndMarkDocumentStamped(finalOrderCollection, AUTH_TOKEN))
            .thenReturn(finalOrderCollection);
        when(documentHelper.checkIfOrderAlreadyInFinalOrderCollection(finalOrderCollection, legacyHearingOrder))
            .thenReturn(Boolean.FALSE);
        lenient().when(genericDocumentService.stampDocument(legacyHearingOrder, AUTH_TOKEN, stampType, caseType)).thenReturn(stampedDoc);

        var response = underTest.handle(callbackRequest, AUTH_TOKEN);

        verify(genericDocumentService, times(isOrderAlreadyStamped ? 0 : 1)).stampDocument(legacyHearingOrder, AUTH_TOKEN, stampType, caseType);
        verify(orderDateService).syncCreatedDateAndMarkDocumentStamped(finalOrderCollection, AUTH_TOKEN);
        if (isOrderAlreadyStamped) {
            assertThat(response.getData().getFinalOrderCollection()).isEqualTo(finalOrderCollection);
        } else {
            // To verify a stamped document is added to finalOrderCollection
            verify(finalOrderCollection).add(argThat(f -> f.getValue().getUploadDraftDocument().equals(stampedDoc)));
        }
    }

    @Test
    void givenAgreedDraftOrdersSelected_whenHandled_shouldPopulateToFinalisedOrderCollection() {
        List<OrderToShareCollection> selectedOrders = List.of(
            toSelectedOrderToShare("agreedDraftOrder")
        );

        final CaseDocument newProcessedOrder = caseDocument("agreedDraftOrder");
        final List<DocumentCollectionItem> attachments = mock(List.class);
        final LocalDateTime submittedDate = mock(LocalDateTime.class);
        final String submittedBy = "Claire Mumford";
        final YesOrNo finalOrder = mock(YesOrNo.class);
        final LocalDateTime approvalDate = mock(LocalDateTime.class);
        final String approvalJudge = "Peter Chapman";
        final CaseDocument coversheet = caseDocument("coversheet");

        AgreedDraftOrder agreedDraftOrder = mock(AgreedDraftOrder.class);
        when(agreedDraftOrder.getTargetDocument()).thenReturn(newProcessedOrder);

        AgreedDraftOrderCollection remainedAgreedDraftOrderCollection = mock(AgreedDraftOrderCollection.class);
        when(remainedAgreedDraftOrderCollection.getValue()).thenReturn(new AgreedDraftOrder());

        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(CASE_ID_IN_LONG,
            caseType,
            FinremCaseData.builder()
                .draftOrdersWrapper(DraftOrdersWrapper.builder()
                    .draftOrdersReviewCollection(List.of(
                        DraftOrdersReviewCollection.builder()
                            .value(DraftOrdersReview.builder()
                                .draftOrderDocReviewCollection(new ArrayList<>(List.of(
                                    DraftOrderDocReviewCollection.builder()
                                        .value(
                                            DraftOrderDocumentReview.builder()
                                                .attachments(attachments)
                                                .draftOrderDocument(newProcessedOrder)
                                                .submittedDate(submittedDate)
                                                .submittedBy(submittedBy)
                                                .finalOrder(finalOrder)
                                                .approvalDate(approvalDate)
                                                .approvalJudge(approvalJudge)
                                                .coverLetter(coversheet)
                                            .build()
                                        )
                                        .build()
                                )))
                                .build())
                            .build()
                    ))
                    .agreedDraftOrderCollection(List.of(
                        remainedAgreedDraftOrderCollection,
                        AgreedDraftOrderCollection.builder()
                            .value(agreedDraftOrder)
                            .build()
                    ))
                    .build())
                .orderApprovedCoverLetter(orderApprovedCoverLetter)
                .sendOrderWrapper(SendOrderWrapper.builder()
                    .ordersToSend(OrdersToSend.builder()
                        .value(selectedOrders)
                        .build())
                    .build())
                .build()
        );

        when(generalOrderService.hearingOrdersToShare(callbackRequest.getCaseDetails(),
            selectedOrders.stream().map(OrderToShareCollection::getValue).toList()))
            .thenReturn(Triple.of(List.of(), List.of(newProcessedOrder), Map.of()));

        var response = underTest.handle(callbackRequest, AUTH_TOKEN);

        assertThat(response.getData().getDraftOrdersWrapper().getAgreedDraftOrderCollection()).containsOnly(remainedAgreedDraftOrderCollection);
        assertThat(response.getData().getDraftOrdersWrapper().getFinalisedOrdersCollection())
            .extracting(FinalisedOrderCollection::getValue)
            .extracting(
                FinalisedOrder::getAttachments,
                FinalisedOrder::getFinalisedDocument,
                FinalisedOrder::getSubmittedDate,
                FinalisedOrder::getSubmittedBy,
                FinalisedOrder::getFinalOrder,
                FinalisedOrder::getApprovalDate,
                FinalisedOrder::getApprovalJudge,
                FinalisedOrder::getCoverLetter
            )
            .contains(Tuple.tuple(attachments,
                newProcessedOrder, submittedDate, submittedBy, finalOrder, approvalDate, approvalJudge,
                coversheet));
    }

    private OrderToShareCollection toSelectedOrderToShare(String documentName) {
        return OrderToShareCollection.builder().value(
            OrderToShare.builder().documentName(documentName).documentToShare(YesOrNo.YES).build()
        ).build();
    }

    private void verifySetupDocumentOnPartiesTabs(FinremCaseDetails finremCaseDetails) {
        handlers.forEach(handler ->
            verify(handler).setUpOrderDocumentsOnPartiesTab(finremCaseDetails,
                parties));
    }

    private void verifySetUpOrderDocumentsOnCase(FinremCaseDetails finremCaseDetails, List<CaseDocument> hearingDocumentPack) {
        handlers.forEach(handler ->
            verify(handler).setUpOrderDocumentsOnCase(eq(finremCaseDetails),
                eq(parties), argThat(a -> a.containsAll(hearingDocumentPack))));
    }

    private void verifyNeverSetupDocumentOnPartiesTabs(FinremCaseDetails finremCaseDetails) {
        handlers.forEach(handler ->
            verify(handler, never()).setUpOrderDocumentsOnCase(eq(finremCaseDetails),
                eq(parties), anyList()));
    }

    private DirectionOrderCollection toDirectionOrderCollection(String filename, boolean stamped) {
        return DirectionOrderCollection.builder()
            .value(DirectionOrder.builder()
                .uploadDraftDocument(caseDocument(filename))
                .isOrderStamped(YesOrNo.forValue(stamped))
                .build())
            .build();
    }
}
