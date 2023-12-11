package uk.gov.hmcts.reform.finrem.caseorchestration.handler.uploadapprovedorder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.BaseHandlerTestSetup;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadAdditionalDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadAdditionalDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintDocumentService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;

@ExtendWith(MockitoExtension.class)
class UploadApprovedOrderContestedMidHandlerTest extends BaseHandlerTestSetup {

    private UploadApprovedOrderContestedMidHandler handler;
    @Mock
    private BulkPrintDocumentService service;
    private static final String FILE_URL = "http://dm:80/documents/kbjh87y8y9JHVKKKJVJ";
    private static final String FILE_BINARY_URL = "http://dm:80/documents/kbjh87y8y9JHVKKKJVJ/binary";
    private static final String FILE_NAME = "abc.pdf";
    public static final String AUTH_TOKEN = "tokien:)";


    @BeforeEach
    void setup() {
        FinremCaseDetailsMapper finremCaseDetailsMapper = new FinremCaseDetailsMapper(new ObjectMapper().registerModule(new JavaTimeModule()));
        handler = new UploadApprovedOrderContestedMidHandler(finremCaseDetailsMapper, service);
    }

    @Test
    void canHandle() {
        assertTrue(handler.canHandle(CallbackType.MID_EVENT, CaseType.CONTESTED, EventType.UPLOAD_APPROVED_ORDER));
    }

    @Test
    void canNotHandle() {
        assertFalse(handler.canHandle(CallbackType.MID_EVENT, CaseType.CONSENTED, EventType.UPLOAD_APPROVED_ORDER));
    }

    @Test
    void canNotHandleWrongEventType() {
        assertFalse(handler.canHandle(CallbackType.MID_EVENT, CaseType.CONTESTED, EventType.CLOSE));
    }

    @Test
    void canNotHandleWrongCallbackType() {
        assertFalse(handler.canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.UPLOAD_APPROVED_ORDER));
    }

    @Test
    void canNotHandleWhenAllParameterWrong() {
        assertFalse(handler.canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.CLOSE));
    }

    @Test
    void givenContestedCase_whenApprovedUploadOrderButNonEncryptedFileShouldNotGetError() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest(EventType.DIRECTION_UPLOAD_ORDER);
        FinremCaseData caseData = finremCallbackRequest.getCaseDetails().getData();


        CaseDocument caseDocument = caseDocument(FILE_URL, FILE_NAME, FILE_BINARY_URL);

        DirectionOrder order = DirectionOrder.builder().uploadDraftDocument(caseDocument).build();
        DirectionOrderCollection orderCollection = DirectionOrderCollection.builder().value(order).build();
        List<DirectionOrderCollection> uploadHearingOrders = new ArrayList<>();
        uploadHearingOrders.add(orderCollection);
        caseData.setUploadHearingOrder(uploadHearingOrders);

        UploadAdditionalDocumentCollection documentCollection = UploadAdditionalDocumentCollection.builder()
            .value(UploadAdditionalDocument.builder().additionalDocuments(caseDocument).build()).build();
        List<UploadAdditionalDocumentCollection> uploadAdditionalDocument = new ArrayList<>();
        uploadAdditionalDocument.add(documentCollection);
        caseData.setUploadAdditionalDocument(uploadAdditionalDocument);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertTrue(response.getErrors().isEmpty());
        verify(service, times(2)).validateEncryptionOnUploadedDocument(any(), any(), any(), any());
    }

    @Test
    void givenContestedCase_whenApprovedUploadOrderButNonEncryptedFileButIfAlreadyOrderInCollectionShouldNotCheckForExisting() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest(EventType.DIRECTION_UPLOAD_ORDER);
        FinremCaseData caseData = finremCallbackRequest.getCaseDetails().getData();
        CaseDocument caseDocument = caseDocument(FILE_URL, FILE_NAME, FILE_BINARY_URL);
        DirectionOrder order = DirectionOrder.builder().uploadDraftDocument(caseDocument).build();
        DirectionOrderCollection orderCollection = DirectionOrderCollection.builder().value(order).build();
        List<DirectionOrderCollection> uploadHearingOrders = new ArrayList<>();
        uploadHearingOrders.add(orderCollection);
        caseData.setUploadHearingOrder(uploadHearingOrders);
        finremCallbackRequest.getCaseDetailsBefore().getData().setUploadHearingOrder(uploadHearingOrders);

        UploadAdditionalDocumentCollection documentCollection = UploadAdditionalDocumentCollection.builder()
            .value(UploadAdditionalDocument.builder().additionalDocuments(caseDocument).build()).build();
        List<UploadAdditionalDocumentCollection> uploadAdditionalDocument = new ArrayList<>();
        uploadAdditionalDocument.add(documentCollection);
        caseData.setUploadAdditionalDocument(uploadAdditionalDocument);
        finremCallbackRequest.getCaseDetailsBefore().getData().setUploadAdditionalDocument(uploadAdditionalDocument);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertTrue(response.getErrors().isEmpty());
        verify(service, never()).validateEncryptionOnUploadedDocument(any(), any(), any(), any());
    }

    @Test
    void givenContestedCase_whenApprovedUploadOrderButNonEncryptedFileButIfAlreadyOrderInCollectionShouldNotCheckForExistingButCheckNew() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest(EventType.DIRECTION_UPLOAD_ORDER);
        FinremCaseData caseData = finremCallbackRequest.getCaseDetails().getData();
        CaseDocument caseDocument = caseDocument(FILE_URL, FILE_NAME, FILE_BINARY_URL);
        DirectionOrder order = DirectionOrder.builder().uploadDraftDocument(caseDocument).build();
        DirectionOrderCollection orderCollection = DirectionOrderCollection.builder().value(order).build();
        List<DirectionOrderCollection> uploadHearingOrders = new ArrayList<>();
        uploadHearingOrders.add(orderCollection);
        CaseDocument caseDocument1 = caseDocument("fileurl", "abc.pdf", "fileurl/binary");
        DirectionOrder order1 = DirectionOrder.builder().uploadDraftDocument(caseDocument1).build();
        DirectionOrderCollection orderCollection1 = DirectionOrderCollection.builder().value(order1).build();
        uploadHearingOrders.add(orderCollection1);
        caseData.setUploadHearingOrder(uploadHearingOrders);

        DirectionOrder order2 = DirectionOrder.builder().uploadDraftDocument(caseDocument).build();
        DirectionOrderCollection orderCollection2 = DirectionOrderCollection.builder().value(order2).build();
        List<DirectionOrderCollection> uploadHearingOrders2 = new ArrayList<>();
        uploadHearingOrders2.add(orderCollection2);
        finremCallbackRequest.getCaseDetailsBefore().getData().setUploadHearingOrder(uploadHearingOrders2);

        UploadAdditionalDocumentCollection documentCollection = UploadAdditionalDocumentCollection.builder()
            .value(UploadAdditionalDocument.builder().additionalDocuments(caseDocument).build()).build();
        List<UploadAdditionalDocumentCollection> uploadAdditionalDocument = new ArrayList<>();
        uploadAdditionalDocument.add(documentCollection);
        uploadAdditionalDocument.add(documentCollection);
        caseData.setUploadAdditionalDocument(uploadAdditionalDocument);
        finremCallbackRequest.getCaseDetailsBefore().getData().setUploadAdditionalDocument(uploadAdditionalDocument);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertTrue(response.getErrors().isEmpty());
        verify(service, times(1)).validateEncryptionOnUploadedDocument(any(), any(), any(), any());
    }
}