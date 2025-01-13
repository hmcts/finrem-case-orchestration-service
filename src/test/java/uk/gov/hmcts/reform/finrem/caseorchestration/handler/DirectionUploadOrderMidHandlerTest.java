package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintDocumentService;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DirectionUploadOrderMidHandlerTest extends BaseHandlerTestSetup {

    private DirectionUploadOrderMidHandler handler;
    @Mock
    private BulkPrintDocumentService service;
    private static final String FILE_URL = "http://dm:80/documents/kbjh87y8y9JHVKKKJVJ";
    private static final String FILE_BINARY_URL = "http://dm:80/documents/kbjh87y8y9JHVKKKJVJ/binary";
    private static final String FILE_NAME = "abc.pdf";
    public static final String AUTH_TOKEN = "tokien:)";


    @BeforeEach
    void setup() {
        FinremCaseDetailsMapper finremCaseDetailsMapper = new FinremCaseDetailsMapper(new ObjectMapper().registerModule(new JavaTimeModule()));
        handler = new DirectionUploadOrderMidHandler(finremCaseDetailsMapper, service);
    }

    @Test
    void canHandle() {
        assertTrue(handler.canHandle(CallbackType.MID_EVENT, CaseType.CONTESTED, EventType.DIRECTION_UPLOAD_ORDER));
    }

    @Test
    void canNotHandle() {
        assertFalse(handler.canHandle(CallbackType.MID_EVENT, CaseType.CONSENTED, EventType.DIRECTION_UPLOAD_ORDER));
    }

    @Test
    void canNotHandleWrongEventType() {
        assertFalse(handler.canHandle(CallbackType.MID_EVENT, CaseType.CONTESTED, EventType.CLOSE));
    }

    @Test
    void canNotHandleWrongCallbackType() {
        assertFalse(handler.canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.DIRECTION_UPLOAD_ORDER));
    }


    @Test
    void givenContestedCase_whenDirectionUploadOrderButNonEncryptedFileShouldNotGetError() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest(EventType.DIRECTION_UPLOAD_ORDER);
        FinremCaseData caseData = finremCallbackRequest.getCaseDetails().getData();


        CaseDocument caseDocument = TestSetUpUtils.caseDocument(FILE_URL, FILE_NAME, FILE_BINARY_URL);

        DirectionOrder order = DirectionOrder.builder().uploadDraftDocument(caseDocument).build();
        DirectionOrderCollection orderCollection = DirectionOrderCollection.builder().value(order).build();
        List<DirectionOrderCollection> uploadHearingOrders = new ArrayList<>();
        uploadHearingOrders.add(orderCollection);
        caseData.setUploadHearingOrder(uploadHearingOrders);

        DocumentCollection documentCollection = DocumentCollection.builder().value(caseDocument).build();
        List<DocumentCollection> hearingOrderOtherDocuments = new ArrayList<>();
        hearingOrderOtherDocuments.add(documentCollection);
        caseData.setHearingOrderOtherDocuments(hearingOrderOtherDocuments);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertTrue(response.getErrors().isEmpty());
        verify(service, times(2)).validateEncryptionOnUploadedDocument(any(), any(), any(), any());
    }

    @Test
    void givenContestedCase_whenExistingDirectionUploadOrderAndUploadedSame_thenShouldNotCheck() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest(EventType.DIRECTION_UPLOAD_ORDER);
        FinremCaseData caseData = finremCallbackRequest.getCaseDetails().getData();


        CaseDocument caseDocument = TestSetUpUtils.caseDocument(FILE_URL, FILE_NAME, FILE_BINARY_URL);

        DirectionOrder order = DirectionOrder.builder().uploadDraftDocument(caseDocument).build();
        DirectionOrderCollection orderCollection = DirectionOrderCollection.builder().value(order).build();
        List<DirectionOrderCollection> uploadHearingOrders = new ArrayList<>();
        uploadHearingOrders.add(orderCollection);
        caseData.setUploadHearingOrder(uploadHearingOrders);
        finremCallbackRequest.getCaseDetailsBefore().getData().setUploadHearingOrder(uploadHearingOrders);

        DocumentCollection documentCollection = DocumentCollection.builder().value(caseDocument).build();
        List<DocumentCollection> hearingOrderOtherDocuments = new ArrayList<>();
        hearingOrderOtherDocuments.add(documentCollection);
        caseData.setHearingOrderOtherDocuments(hearingOrderOtherDocuments);

        finremCallbackRequest.getCaseDetailsBefore().getData().setHearingOrderOtherDocuments(hearingOrderOtherDocuments);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertTrue(response.getErrors().isEmpty());
        verify(service, never()).validateEncryptionOnUploadedDocument(any(), any(), any(), any());
    }

    @Test
    void givenContestedCase_whenDirectionUploadOrderWithPreviousFiles_shouldNotModifyUploadHearingOrder() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest(EventType.DIRECTION_UPLOAD_ORDER);
        FinremCaseData caseData = finremCallbackRequest.getCaseDetails().getData();
        FinremCaseData caseDataBefore = finremCallbackRequest.getCaseDetailsBefore().getData();

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
        DocumentCollection oldDocCollection = DocumentCollection.builder().value(oldDocument).build();
        DocumentCollection newDocCollection = DocumentCollection.builder().value(newDocument).build();
        caseDataBefore.setHearingOrderOtherDocuments(List.of(oldDocCollection));
        caseData.setHearingOrderOtherDocuments(List.of(oldDocCollection, newDocCollection));

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertThat(response.getData().getUploadHearingOrder())
            .hasSize(2);
        assertTrue(
            response.getData().getUploadHearingOrder().stream()
                .map(DirectionOrderCollection::getValue)
                .map(DirectionOrder::getUploadDraftDocument)
                .anyMatch(doc -> doc.equals(oldDocument)),
            "Expected the old document to be present in the response"
        );
        assertTrue(
            response.getData().getUploadHearingOrder().stream()
                .map(DirectionOrderCollection::getValue)
                .map(DirectionOrder::getUploadDraftDocument)
                .anyMatch(doc -> doc.equals(newDocument)),
            "Expected the new document to be present in the response"
        );
        assertTrue(response.getErrors().isEmpty());
    }
}
