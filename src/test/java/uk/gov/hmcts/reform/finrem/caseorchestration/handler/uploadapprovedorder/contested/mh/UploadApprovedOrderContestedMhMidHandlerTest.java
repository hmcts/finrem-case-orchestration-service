package uk.gov.hmcts.reform.finrem.caseorchestration.handler.uploadapprovedorder.contested.mh;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCaseDetailsBuilderFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.BaseHandlerTestSetup;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsAction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.WorkingHearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ValidateHearingService;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class UploadApprovedOrderContestedMhMidHandlerTest extends BaseHandlerTestSetup {

    @Mock
    private BulkPrintDocumentService bulkPrintDocumentService;

    @Mock
    private ValidateHearingService validateHearingService;

    @InjectMocks
    private UploadApprovedOrderContestedMhMidHandler handler;

    @Test
    void canHandle() {
        assertCanHandle(handler, CallbackType.MID_EVENT, CaseType.CONTESTED, EventType.UPLOAD_APPROVED_ORDER_MH);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void givenMissingUploadHearingOrder_whenHandle_thenReturnAnError(List<DirectionOrderCollection> uploadHearingOrder) {
        FinremCaseData finremCaseData = spy(FinremCaseData.class);
        finremCaseData.setUploadHearingOrder(uploadHearingOrder);

        FinremCallbackRequest finremCallbackRequest = mock(FinremCallbackRequest.class);

        FinremCaseDetails finremCaseDetails = mock(FinremCaseDetails.class);
        when(finremCaseDetails.getData()).thenReturn(finremCaseData);

        FinremCaseDetails finremCaseDetailsBefore = mock(FinremCaseDetails.class);
        when(finremCallbackRequest.getCaseDetails()).thenReturn(finremCaseDetails);

        when(finremCallbackRequest.getCaseDetailsBefore()).thenReturn(finremCaseDetailsBefore);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(finremCallbackRequest, AUTH_TOKEN);
        assertThat(response.getErrors()).containsExactly("No upload approved order found");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void givenCaseWithNewUploadHearingOrders_whenHandle_thenUploadedDocumentValidated(boolean valid) {
        FinremCaseData finremCaseData = spy(FinremCaseData.class);
        CaseDocument newUploadDocument = mock(CaseDocument.class);
        finremCaseData.setUploadHearingOrder(List.of(
            DirectionOrderCollection.builder().value(DirectionOrder.builder()
                .uploadDraftDocument(newUploadDocument).build()).build()
        ));

        FinremCallbackRequest finremCallbackRequest = mock(FinremCallbackRequest.class);

        FinremCaseDetails finremCaseDetails = mock(FinremCaseDetails.class);
        when(finremCaseDetails.getId()).thenReturn(Long.valueOf(CASE_ID));
        when(finremCaseDetails.getData()).thenReturn(finremCaseData);

        FinremCaseData finremCaseDataBefore = spy(FinremCaseData.class);

        FinremCaseDetails finremCaseDetailsBefore = mock(FinremCaseDetails.class);
        when(finremCaseDetailsBefore.getData()).thenReturn(finremCaseDataBefore);

        when(finremCallbackRequest.getCaseDetails()).thenReturn(finremCaseDetails);
        when(finremCallbackRequest.getCaseDetailsBefore()).thenReturn(finremCaseDetailsBefore);

        doAnswer(invocation -> {
            if (!valid) {
                List<String> errors = invocation.getArgument(2);
                errors.add("Mocked encryption error for testing");
            }
            return null;
        }).when(bulkPrintDocumentService)
            .validateEncryptionOnUploadedDocument(eq(newUploadDocument), eq(CASE_ID), anyList(), eq(AUTH_TOKEN));

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(finremCallbackRequest, AUTH_TOKEN);
        if (!valid) {
            assertThat(response.getErrors()).containsExactly("Mocked encryption error for testing");
        } else {
            assertThat(response.getErrors()).isEmpty();
        }

        verify(bulkPrintDocumentService)
            .validateEncryptionOnUploadedDocument(eq(newUploadDocument), eq(CASE_ID), anyList(), eq(AUTH_TOKEN));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void givenExistingUploadHearingOrderWithNewUploadHearingOrders_whenHandle_thenUploadedDocumentValidated(boolean valid) {
        FinremCaseData finremCaseData = spy(FinremCaseData.class);
        CaseDocument newUploadDocument = mock(CaseDocument.class);
        finremCaseData.setUploadHearingOrder(new ArrayList(List.of(
            DirectionOrderCollection.builder().value(DirectionOrder.builder()
                .uploadDraftDocument(newUploadDocument).build()).build()
        )));

        FinremCaseDetails finremCaseDetails = mock(FinremCaseDetails.class);
        when(finremCaseDetails.getId()).thenReturn(Long.valueOf(CASE_ID));
        when(finremCaseDetails.getData()).thenReturn(finremCaseData);

        FinremCaseData finremCaseDataBefore = spy(FinremCaseData.class);
        CaseDocument existingUploadedDocument = mock(CaseDocument.class);
        finremCaseDataBefore.setUploadHearingOrder(List.of(
            DirectionOrderCollection.builder().value(DirectionOrder.builder()
                .uploadDraftDocument(existingUploadedDocument).build()).build()
        ));

        FinremCaseDetails finremCaseDetailsBefore = mock(FinremCaseDetails.class);
        when(finremCaseDetailsBefore.getData()).thenReturn(finremCaseDataBefore);

        FinremCallbackRequest finremCallbackRequest = mock(FinremCallbackRequest.class);
        when(finremCallbackRequest.getCaseDetails()).thenReturn(finremCaseDetails);
        when(finremCallbackRequest.getCaseDetailsBefore()).thenReturn(finremCaseDetailsBefore);

        doAnswer(invocation -> {
            if (!valid) {
                List<String> errors = invocation.getArgument(2);
                errors.add("Mocked encryption error for testing");
            }
            return null;
        }).when(bulkPrintDocumentService)
            .validateEncryptionOnUploadedDocument(eq(newUploadDocument), eq(CASE_ID), anyList(), eq(AUTH_TOKEN));
        lenient(). // should not be invoked
            doAnswer(invocation -> {
                List<String> errors = invocation.getArgument(2);
                errors.add("Mocked encryption error for existing document");
                return null;
            }).when(bulkPrintDocumentService)
            .validateEncryptionOnUploadedDocument(eq(existingUploadedDocument), eq(CASE_ID), anyList(), eq(AUTH_TOKEN));

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(finremCallbackRequest, AUTH_TOKEN);
        if (!valid) {
            assertThat(response.getErrors()).containsExactly("Mocked encryption error for testing");
        } else {
            assertThat(response.getErrors()).isEmpty();
        }

        verify(bulkPrintDocumentService)
            .validateEncryptionOnUploadedDocument(eq(newUploadDocument), eq(CASE_ID), anyList(), eq(AUTH_TOKEN));
        verify(bulkPrintDocumentService, never())
            .validateEncryptionOnUploadedDocument(eq(existingUploadedDocument), eq(CASE_ID), anyList(), eq(AUTH_TOKEN));
    }

    @Test
    void givenInvalidAdditionalDocument_whenHandle_thenReturnsError() {
        //Arrange
        WorkingHearing workingHearing = WorkingHearing.builder()
            .additionalHearingDocPrompt(YesOrNo.YES)
            .build();

        FinremCaseData.FinremCaseDataBuilder builder = FinremCaseData.builder()
            .manageHearingsWrapper(ManageHearingsWrapper.builder()
                .isAddHearingChosen(YesOrNo.YES)
                .manageHearingsActionSelection(ManageHearingsAction.ADD_HEARING)
                .workingHearing(workingHearing)
                .build());

        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory
            .from(FinremCaseDetailsBuilderFactory.from(CONTESTED, builder));

        when(validateHearingService.areAllAdditionalHearingDocsWordOrPdf(any()))
            .thenReturn(false);

        // Act
        var response = handler.handle(callbackRequest, AUTH_TOKEN);

        // Assert
        assertThat(response.getErrors()).containsExactly("All additional hearing documents must be Word or PDF files.");
    }
}
