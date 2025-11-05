package uk.gov.hmcts.reform.finrem.caseorchestration.handler.processorder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.BaseHandlerTestSetup;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CfcCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionDetail;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionDetailCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsAction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.WorkingHearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ValidateHearingService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.processorder.ProcessOrderService;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.DIRECTION_UPLOAD_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.PROCESS_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class ProcessOrderMidHandlerTest extends BaseHandlerTestSetup {

    @InjectMocks
    private ProcessOrderMidHandler handler;
    @Mock
    private BulkPrintDocumentService service;
    @Mock
    private ProcessOrderService processOrderService;
    @Mock
    private ValidateHearingService validateHearingService;

    private static final String FILE_URL = "http://dm:80/documents/kbjh87y8y9JHVKKKJVJ";
    private static final String FILE_BINARY_URL = "http://dm:80/documents/kbjh87y8y9JHVKKKJVJ/binary";
    private static final String FILE_NAME = "abc.pdf";
    private static final String ERROR_NO_ORDERS = "There are no draft orders to be processed.";
    private static final String ERROR_NEW_DOCS = "You must upload a Microsoft Word file or PDF for new documents.";
    private static final String ERROR_LEGACY_PDF = "You must upload a PDF file for modifying legacy approved documents.";
    private static final String ERROR_MODIFY_UNPROCESSED =
        "You must upload a Microsoft Word file or PDF for modifying an unprocessed approved documents.";

    @Test
    void testCanHandle() {
        assertCanHandle(handler, CallbackType.MID_EVENT, CaseType.CONTESTED, PROCESS_ORDER);
    }

    @Test
    void givenContestedCase_whenDirectionUploadOrderButNonEncryptedFileShouldNotGetError() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest(PROCESS_ORDER);
        FinremCaseData caseData = finremCallbackRequest.getCaseDetails().getData();

        mockPassAllValidations();

        CaseDocument caseDocument = TestSetUpUtils.caseDocument(FILE_URL, FILE_NAME, FILE_BINARY_URL);

        DirectionOrder order = DirectionOrder.builder().uploadDraftDocument(caseDocument).build();
        DirectionOrderCollection orderCollection = DirectionOrderCollection.builder().value(order).build();
        List<DirectionOrderCollection> uploadHearingOrders = new ArrayList<>();
        uploadHearingOrders.add(orderCollection);
        caseData.setUploadHearingOrder(uploadHearingOrders);

        DocumentCollectionItem documentCollectionItem = DocumentCollectionItem.builder().value(caseDocument).build();
        List<DocumentCollectionItem> hearingOrderOtherDocuments = new ArrayList<>();
        hearingOrderOtherDocuments.add(documentCollectionItem);
        caseData.setHearingOrderOtherDocuments(hearingOrderOtherDocuments);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertThat(response.getErrors()).isEmpty();
        verify(service, times(2)).validateEncryptionOnUploadedDocument(any(), any(), any(), any());
    }

    @Test
    void givenContestedCase_whenExistingDirectionUploadOrderAndUploadedSame_thenShouldNotCheck() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest(PROCESS_ORDER);
        FinremCaseData caseData = finremCallbackRequest.getCaseDetails().getData();

        mockPassAllValidations();

        CaseDocument caseDocument = TestSetUpUtils.caseDocument(FILE_URL, FILE_NAME, FILE_BINARY_URL);

        DirectionOrder order = DirectionOrder.builder().uploadDraftDocument(caseDocument).build();
        DirectionOrderCollection orderCollection = DirectionOrderCollection.builder().value(order).build();
        List<DirectionOrderCollection> uploadHearingOrders = new ArrayList<>();
        uploadHearingOrders.add(orderCollection);
        caseData.setUploadHearingOrder(uploadHearingOrders);
        finremCallbackRequest.getCaseDetailsBefore().getData().setUploadHearingOrder(uploadHearingOrders);

        DocumentCollectionItem documentCollectionItem = DocumentCollectionItem.builder().value(caseDocument).build();
        List<DocumentCollectionItem> hearingOrderOtherDocuments = new ArrayList<>();
        hearingOrderOtherDocuments.add(documentCollectionItem);
        caseData.setHearingOrderOtherDocuments(hearingOrderOtherDocuments);

        finremCallbackRequest.getCaseDetailsBefore().getData().setHearingOrderOtherDocuments(hearingOrderOtherDocuments);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertThat(response.getErrors()).isEmpty();
        verify(service, never()).validateEncryptionOnUploadedDocument(any(), any(), any(), any());
    }

    @Test
    void shouldCreateEmptyEntryWhenDirectionDetailsCollectionIsEmptyOrNull() {
        mockPassAllValidations();

        // Test with null directionDetailsCollection
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(PROCESS_ORDER, FinremCaseDetails.builder()
            .caseType(CaseType.CONTESTED)
            .data(FinremCaseData.builder().build()));

        callbackRequest.setCaseDetailsBefore(FinremCaseDetails.builder().data(FinremCaseData.builder().build()).build());

        FinremCaseData result = handler.handle(callbackRequest, AUTH_TOKEN).getData();
        assertThat(result.getDirectionDetailsCollection()).isNull();

        // Test with empty directionDetailsCollection
        callbackRequest = FinremCallbackRequestFactory.from(PROCESS_ORDER, FinremCaseDetails.builder()
            .caseType(CaseType.CONTESTED)
            .data(FinremCaseData.builder().directionDetailsCollection(List.of()).build()));

        callbackRequest.setCaseDetailsBefore(FinremCaseDetails.builder().data(FinremCaseData.builder().build()).build());

        result = handler.handle(callbackRequest, AUTH_TOKEN).getData();
        assertThat(result.getDirectionDetailsCollection()).isEmpty();
    }

    @Test
    void shouldNotInitializeDirectionDetailsCollectionWhenNotEventDirectionUploadOrder() {
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(PROCESS_ORDER, FinremCaseDetails.builder()
            .caseType(CaseType.CONTESTED)
            .data(FinremCaseData.builder().build()));

        callbackRequest.setCaseDetailsBefore(FinremCaseDetails.builder().data(FinremCaseData.builder().build()).build());

        FinremCaseData result = handler.handle(callbackRequest, AUTH_TOKEN).getData();
        assertNull(result.getDirectionDetailsCollection());
    }

    @Test
    void shouldNotCreateEmptyEntryWhenDirectionDetailsCollectionIsNotEmpty() {
        List<DirectionDetailCollection> notExpected = List.of(
            DirectionDetailCollection.builder().value(DirectionDetail.builder().build()).build()
        );

        mockPassAllValidations();

        FinremCallbackRequest finremCallbackRequest = FinremCallbackRequestFactory.from(FinremCaseData.builder()
            .directionDetailsCollection(List.of(
                DirectionDetailCollection.builder().value(DirectionDetail.builder().build()).build(),
                DirectionDetailCollection.builder().value(DirectionDetail.builder().build()).build()
            ))
            .build());
        FinremCaseData result = handler.handle(finremCallbackRequest, AUTH_TOKEN).getData();
        assertNotEquals(notExpected, result.getDirectionDetailsCollection());

        finremCallbackRequest = FinremCallbackRequestFactory.from(FinremCaseData.builder()
            .directionDetailsCollection(List.of(
                DirectionDetailCollection.builder().value(DirectionDetail.builder().cfcList(CfcCourt.BARNET_CIVIL_AND_FAMILY_COURTS_CENTRE)
                    .build()).build()
            ))
            .build());
        result = handler.handle(finremCallbackRequest, AUTH_TOKEN).getData();
        assertNotEquals(notExpected, result.getDirectionDetailsCollection());
    }

    @Test
    void shouldShowErrorMessageWhenAllLegacyApprovedOrdersRemoved() {
        when(processOrderService.hasNoApprovedOrdersToProcess(any(FinremCaseData.class))).thenReturn(true);
        FinremCallbackRequest finremCallbackRequest = FinremCallbackRequestFactory.from(FinremCaseData.builder().build());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> res = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertThat(res.getErrors()).containsExactly(ERROR_NO_ORDERS);
        verify(processOrderService).hasNoApprovedOrdersToProcess(any(FinremCaseData.class));
    }

    @Test
    void shouldShowErrorMessageWhenNotAllNewOrdersWordOrPdfFiles() {
        when(processOrderService.hasNoApprovedOrdersToProcess(any(FinremCaseData.class))).thenReturn(false);
        when(processOrderService.areAllNewOrdersWordOrPdfFiles(any(FinremCaseData.class))).thenReturn(false);
        FinremCallbackRequest finremCallbackRequest = FinremCallbackRequestFactory.from(FinremCaseData.builder().build());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> res = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertThat(res.getErrors()).containsExactly(ERROR_NEW_DOCS);
        verify(processOrderService).hasNoApprovedOrdersToProcess(any(FinremCaseData.class));
        verify(processOrderService).areAllNewOrdersWordOrPdfFiles(any(FinremCaseData.class));
    }

    @Test
    void shouldShowErrorMessageWhenNotAllLegacyApprovedOrdersPdf() {
        when(processOrderService.hasNoApprovedOrdersToProcess(any(FinremCaseData.class))).thenReturn(false);
        when(processOrderService.areAllNewOrdersWordOrPdfFiles(any(FinremCaseData.class))).thenReturn(true);
        when(processOrderService.areAllLegacyApprovedOrdersPdf(any(FinremCaseData.class))).thenReturn(false);
        FinremCallbackRequest finremCallbackRequest = FinremCallbackRequestFactory.from(FinremCaseData.builder().build());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> res = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertThat(res.getErrors()).containsExactly(ERROR_LEGACY_PDF);
        verify(processOrderService).hasNoApprovedOrdersToProcess(any(FinremCaseData.class));
        verify(processOrderService).areAllNewOrdersWordOrPdfFiles(any(FinremCaseData.class));
        verify(processOrderService).areAllLegacyApprovedOrdersPdf(any(FinremCaseData.class));
    }

    @Test
    void shouldShowErrorMessageWhenNotAllModifyingUnprocessedOrdersWordDocumentsOrPdf() {
        when(processOrderService.hasNoApprovedOrdersToProcess(any(FinremCaseData.class))).thenReturn(false);
        when(processOrderService.areAllNewOrdersWordOrPdfFiles(any(FinremCaseData.class))).thenReturn(true);
        when(processOrderService.areAllLegacyApprovedOrdersPdf(any(FinremCaseData.class))).thenReturn(true);
        when(processOrderService.areAllModifyingUnprocessedOrdersWordOrPdfDocuments(any(FinremCaseData.class))).thenReturn(false);
        FinremCallbackRequest finremCallbackRequest = FinremCallbackRequestFactory.from(FinremCaseData.builder().build());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> res = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertThat(res.getErrors()).containsExactly(ERROR_MODIFY_UNPROCESSED);
        verify(processOrderService).hasNoApprovedOrdersToProcess(any(FinremCaseData.class));
        verify(processOrderService).areAllNewOrdersWordOrPdfFiles(any(FinremCaseData.class));
        verify(processOrderService).areAllLegacyApprovedOrdersPdf(any(FinremCaseData.class));
        verify(processOrderService).areAllModifyingUnprocessedOrdersWordOrPdfDocuments(any(FinremCaseData.class));
    }

    private void mockPassAllValidations() {
        when(processOrderService.hasNoApprovedOrdersToProcess(any(FinremCaseData.class))).thenReturn(false);
        when(processOrderService.areAllNewOrdersWordOrPdfFiles(any(FinremCaseData.class))).thenReturn(true);
        when(processOrderService.areAllLegacyApprovedOrdersPdf(any(FinremCaseData.class))).thenReturn(true);
        when(processOrderService.areAllModifyingUnprocessedOrdersWordOrPdfDocuments(any(FinremCaseData.class))).thenReturn(true);
    }

    @Test
    void givenContestedCase_whenDirectionUploadOrderWithPreviousFiles_shouldNotModifyUploadHearingOrder() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest(DIRECTION_UPLOAD_ORDER);
        FinremCaseData caseData = finremCallbackRequest.getCaseDetails().getData();
        FinremCaseData caseDataBefore = finremCallbackRequest.getCaseDetailsBefore().getData();

        mockPassAllValidations();

        //Create old and new documents
        CaseDocument oldDocument = TestSetUpUtils.caseDocument(FILE_URL, FILE_NAME, FILE_BINARY_URL);
        CaseDocument newDocument = TestSetUpUtils.caseDocument("new-file-url", "new-file-name", "new-binary-url");

        //Old document in 'before' case data
        DirectionOrder oldOrder = DirectionOrder.builder().uploadDraftDocument(oldDocument).build();
        DirectionOrderCollection oldOrderCollection = DirectionOrderCollection.builder().value(oldOrder).build();
        caseDataBefore.setUploadHearingOrder(List.of(oldOrderCollection));

        //New document in current case data
        DirectionOrder newOrder = DirectionOrder.builder().uploadDraftDocument(newDocument).build();
        DirectionOrderCollection newOrderCollection = DirectionOrderCollection.builder().value(newOrder).build();
        caseData.setUploadHearingOrder(List.of(oldOrderCollection, newOrderCollection));

        //Create similar setup for hearingOrderOtherDocuments
        DocumentCollectionItem oldDocCollection = DocumentCollectionItem.builder().value(oldDocument).build();
        DocumentCollectionItem newDocCollection = DocumentCollectionItem.builder().value(newDocument).build();
        caseDataBefore.setHearingOrderOtherDocuments(List.of(oldDocCollection));
        caseData.setHearingOrderOtherDocuments(List.of(oldDocCollection, newDocCollection));

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertThat(response.getData().getUploadHearingOrder())
            .extracting(DirectionOrderCollection::getValue)
            .extracting(DirectionOrder::getUploadDraftDocument)
            .containsExactlyInAnyOrder(oldDocument, newDocument);
        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void givenInvalidAdditionalDocument_whenHandle_thenReturnsError() {
        //Arrange
        WorkingHearing workingHearing = WorkingHearing.builder()
            .additionalHearingDocPrompt(YesOrNo.YES)
            .build();

        FinremCaseDetails.FinremCaseDetailsBuilder caseDataBuilder = FinremCaseDetails.builder()
            .data(FinremCaseData.builder()
            .manageHearingsWrapper(ManageHearingsWrapper.builder()
                .isAddHearingChosen(YesOrNo.YES)
                .manageHearingsActionSelection(ManageHearingsAction.ADD_HEARING)
                .workingHearing(workingHearing)
                .build())
            .build());

        FinremCaseDetails.FinremCaseDetailsBuilder caseDataBefore =  FinremCaseDetails.builder().data(mock(FinremCaseData.class));

        FinremCallbackRequest finremCallbackRequest = FinremCallbackRequestFactory
            .from(PROCESS_ORDER, caseDataBefore, caseDataBuilder);

        FinremCaseData finremCaseData = finremCallbackRequest.getCaseDetails().getData();
        when(validateHearingService.hasInvalidAdditionalHearingDocsForAddHearingChosen(finremCaseData))
            .thenReturn(true);

        // Act
        var response = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        // Assert
        assertThat(response.getErrors()).containsExactly("All additional hearing documents must be Word or PDF files.");
        verify(validateHearingService).hasInvalidAdditionalHearingDocsForAddHearingChosen(finremCaseData);
    }
}
