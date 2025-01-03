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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class DirectionUploadOrderMidHandlerTest extends BaseHandlerTestSetup {

    private DirectionUploadOrderMidHandler handler;
    @Mock
    private BulkPrintDocumentService service;
    private static final String FILE_URL = "http://dm:80/documents/kbjh87y8y9JHVKKKJVJ";
    private static final String FILE_BINARY_URL = "http://dm:80/documents/kbjh87y8y9JHVKKKJVJ/binary";
    private static final String FILE_NAME = "abc.pdf";

    @BeforeEach
    void setup() {
        FinremCaseDetailsMapper finremCaseDetailsMapper = new FinremCaseDetailsMapper(new ObjectMapper().registerModule(new JavaTimeModule()));
        handler = new DirectionUploadOrderMidHandler(finremCaseDetailsMapper, service);
    }

    @Test
    void testCanHandle() {
        assertCanHandle(handler, CallbackType.MID_EVENT, CaseType.CONTESTED, EventType.DIRECTION_UPLOAD_ORDER);
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
}
