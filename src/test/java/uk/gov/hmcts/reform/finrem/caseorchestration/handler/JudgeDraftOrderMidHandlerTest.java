package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftDirectionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintDocumentService;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;

@ExtendWith(MockitoExtension.class)
class JudgeDraftOrderMidHandlerTest extends BaseHandlerTestSetup {

    private JudgeDraftOrderMidHandler handler;
    @Mock
    private BulkPrintDocumentService service;
    private static final String FILE_URL = "http://dm:80/documents/kbjh87y8y9JHVKKKJVJ";
    private static final String FILE_BINARY_URL = "http://dm:80/documents/kbjh87y8y9JHVKKKJVJ/binary";
    private static final String FILE_NAME = "abc.pdf";
    public static final String AUTH_TOKEN = "tokien:)";


    @BeforeEach
    void setup() {
        FinremCaseDetailsMapper finremCaseDetailsMapper = new FinremCaseDetailsMapper(new ObjectMapper().registerModule(new JavaTimeModule()));
        handler = new JudgeDraftOrderMidHandler(finremCaseDetailsMapper, service);
    }

    @Test
    void canHandle() {
        assertTrue(handler.canHandle(CallbackType.MID_EVENT, CaseType.CONTESTED, EventType.JUDGE_DRAFT_ORDER));
    }

    @Test
    void canNotHandle() {
        assertFalse(handler.canHandle(CallbackType.MID_EVENT, CaseType.CONSENTED, EventType.JUDGE_DRAFT_ORDER));
    }

    @Test
    void canNotHandleWrongEventType() {
        assertFalse(handler.canHandle(CallbackType.MID_EVENT, CaseType.CONTESTED, EventType.CLOSE));
    }

    @Test
    void canNotHandleWrongCallbackType() {
        assertFalse(handler.canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.JUDGE_DRAFT_ORDER));
    }

    @Test
    void givenDraftDirectionOrderCollectionIsEmpty_whenHandle_shouldGetError() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest(EventType.JUDGE_DRAFT_ORDER);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        String errorMessage = "No orders have been uploaded. Please upload an order.";
        assertThat(response.getErrors()).contains(errorMessage);
    }

    @Test
    void givenContestedCase_whenDraftOrderUploadedButNonEncryptedFileShouldNotGetError() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest(EventType.JUDGE_DRAFT_ORDER);
        FinremCaseData caseData = finremCallbackRequest.getCaseDetails().getData();

        DraftDirectionWrapper draftDirectionWrapper = caseData.getDraftDirectionWrapper();
        draftDirectionWrapper.setDraftDirectionOrderCollection(getDraftDirectionOrderObj());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertTrue(response.getErrors().isEmpty());
        verify(service).validateEncryptionOnUploadedDocument(any(), any(), any(), any());
    }

    @Test
    void givenContestedCase_whenThereIsexistingDraftOrderNonEncryptedFileShouldNotGetError() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest(EventType.JUDGE_DRAFT_ORDER);
        FinremCaseData caseData = finremCallbackRequest.getCaseDetails().getData();

        DraftDirectionWrapper draftDirectionWrapper = caseData.getDraftDirectionWrapper();
        draftDirectionWrapper.setDraftDirectionOrderCollection(getDraftDirectionOrderObj());

        FinremCaseData caseDataBefore = finremCallbackRequest.getCaseDetailsBefore().getData();
        DraftDirectionWrapper draftDirectionWrapperBefore = caseDataBefore.getDraftDirectionWrapper();
        draftDirectionWrapperBefore.setDraftDirectionOrderCollection(getDraftDirectionOrderObj());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertTrue(response.getErrors().isEmpty());
        verify(service, never()).validateEncryptionOnUploadedDocument(any(), any(), any(), any());
    }

    private List<DraftDirectionOrderCollection> getDraftDirectionOrderObj() {
        CaseDocument caseDocument = caseDocument(FILE_URL, FILE_NAME, FILE_BINARY_URL);
        DraftDirectionOrder directionOrder =
            DraftDirectionOrder
                .builder()
                .purposeOfDocument("test")
                .uploadDraftDocument(caseDocument)
                .build();
        DraftDirectionOrderCollection directionOrderCollection = DraftDirectionOrderCollection.builder().value(directionOrder).build();

        List<DraftDirectionOrderCollection> draftDirectionOrderCollection = new ArrayList<>();
        draftDirectionOrderCollection.add(directionOrderCollection);
        return draftDirectionOrderCollection;
    }
}