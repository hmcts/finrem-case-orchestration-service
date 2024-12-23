package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.CourtDetailsParseException;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AdditionalHearingDocumentService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class DirectionUploadOrderAboutToSubmitHandlerTest {

    @InjectMocks
    private DirectionUploadOrderAboutToSubmitHandler handler;

    @Mock
    private AdditionalHearingDocumentService service;

    @Test
    void testCanHandle() {
        assertCanHandle(handler, CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.DIRECTION_UPLOAD_ORDER);
    }

    @Test
    void createAndStoreAdditionalHearingDocumentsHandleException() throws JsonProcessingException {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        FinremCaseDetails finremCaseDetails = finremCallbackRequest.getCaseDetails();
        doThrow(new CourtDetailsParseException()).when(service)
            .createAndStoreAdditionalHearingDocuments(finremCaseDetails, AUTH_TOKEN);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> res = handler.handle(finremCallbackRequest, AUTH_TOKEN);
        assertEquals(1, res.getErrors().size());
        assertEquals("Failed to parse court details.", res.getErrors().get(0));
        verify(service).createAndStoreAdditionalHearingDocuments(finremCallbackRequest.getCaseDetails(), AUTH_TOKEN);
    }

    @Test
    void createAndStoreAdditionalHearingDocuments() throws JsonProcessingException {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        handler.handle(finremCallbackRequest, AUTH_TOKEN);
        verify(service).createAndStoreAdditionalHearingDocuments(finremCallbackRequest.getCaseDetails(), AUTH_TOKEN);
    }

    @Test
    void shouldClearTemporaryFields() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest(FinremCaseData.builder()
            .draftOrdersWrapper(DraftOrdersWrapper.builder()
                .unprocessedApprovedDocuments(List.of(DirectionOrderCollection.builder().build()))
                .build())
            .build());
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> res = handler.handle(finremCallbackRequest, AUTH_TOKEN);
        assertThat(res.getData().getDraftOrdersWrapper().getUnprocessedApprovedDocuments()).isEmpty();
        assertNull(res.getData().getDraftOrdersWrapper().getIsLegacyApprovedOrderPresent());
        assertNull(res.getData().getDraftOrdersWrapper().getIsUnprocessedApprovedDocumentPresent());
    }

    private FinremCallbackRequest buildCallbackRequest() {
        return buildCallbackRequest(FinremCaseData.builder().build());
    }

    private FinremCallbackRequest buildCallbackRequest(FinremCaseData finremCaseData) {
        return FinremCallbackRequest
            .builder()
            .eventType(EventType.DIRECTION_UPLOAD_ORDER)
            .caseDetails(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(finremCaseData).build())
            .build();
    }
}
