package uk.gov.hmcts.reform.finrem.caseorchestration.handler.uploadapprovedorder.contested;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.helper.DocumentWarningsHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.WorkingHearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftDirectionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.UploadApprovedOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.ManageHearingActionService;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsAction.ADD_HEARING;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class UploadApprovedOrderContestedAboutToSubmitHandlerTest {

    @Mock
    private DocumentWarningsHelper documentWarningsHelper;
    @Mock
    private UploadApprovedOrderService uploadApprovedOrderService;
    @Mock
    private ManageHearingActionService manageHearingActionService;

    @InjectMocks
    private UploadApprovedOrderContestedAboutToSubmitHandler handler;

    @Test
    void canHandle() {
        assertCanHandle(handler, CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.UPLOAD_APPROVED_ORDER_MH);
    }

    @Test
    void handle_shouldProcessApprovedOrdersAndReturnResponse() {
        var caseData = mock(FinremCaseData.class);
        var caseDetails = mock(FinremCaseDetails.class);
        var callbackRequest = mock(FinremCallbackRequest.class);

        when(callbackRequest.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getData()).thenReturn(caseData);

        var manageHearingsWrapper = mock(ManageHearingsWrapper.class);
        when(caseData.getManageHearingsWrapper()).thenReturn(manageHearingsWrapper);
        when(manageHearingsWrapper.getIsAddHearingChosen()).thenReturn(YesOrNo.NO);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            handler.handle(callbackRequest, AUTH_TOKEN);

        verify(uploadApprovedOrderService).processApprovedOrdersMh(caseDetails, AUTH_TOKEN);
        verifyNoInteractions(manageHearingActionService);
        assertNotNull(response);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 10})
    void handle_shouldInvokeManageHearingActionServiceWhenAddHearingChosen(int numberOfUploadedApprovedOrders) {

        WorkingHearing workingHearing = mock(WorkingHearing.class);

        ManageHearingsWrapper manageHearingsWrapper = ManageHearingsWrapper.builder()
            .workingHearing(workingHearing)
            .isAddHearingChosen(YesOrNo.YES)
            .build();

        List<CaseDocument> caseDocuments = new ArrayList<>();
        for (int i = 0; i < numberOfUploadedApprovedOrders; i++) {
            caseDocuments.add(mock(CaseDocument.class));
        }
        List<DirectionOrderCollection> cwApprovedOrderCollection = caseDocuments.stream()
            .map(this::toDirectionOrderCollection).toList();

        FinremCaseData caseData = FinremCaseData.builder()
            .draftDirectionWrapper(DraftDirectionWrapper.builder()
                .cwApprovedOrderCollection(cwApprovedOrderCollection)
                .build())
            .manageHearingsWrapper(manageHearingsWrapper)
            .build();

        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(caseData);
        var caseDetails = callbackRequest.getCaseDetails();

        var response = handler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(response.getData().getManageHearingsWrapper().getManageHearingsActionSelection())
            .isEqualTo(ADD_HEARING);

        if (caseDocuments.isEmpty()) {
            verify(workingHearing, never()).addDocumentToAdditionalHearingDocs(any(CaseDocument.class));
        } else {
            caseDocuments.forEach(document ->
                verify(workingHearing).addDocumentToAdditionalHearingDocs(document));
        }
        verify(manageHearingActionService).performAddHearing(caseDetails, AUTH_TOKEN);
        verify(manageHearingActionService).updateTabData(caseData);
        assertNotNull(response);
    }

    @Test
    void givenContestedCase_whenUploadDocumentWithWarnings_thenReturnWarnings() {
        var caseData = mock(FinremCaseData.class);
        var caseDetails = mock(FinremCaseDetails.class);
        var callbackRequest = mock(FinremCallbackRequest.class);

        when(callbackRequest.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getData()).thenReturn(caseData);

        var manageHearingsWrapper = mock(ManageHearingsWrapper.class);
        when(caseData.getManageHearingsWrapper()).thenReturn(manageHearingsWrapper);
        when(manageHearingsWrapper.getIsAddHearingChosen()).thenReturn(YesOrNo.NO);

        when(documentWarningsHelper.getDocumentWarnings(eq(callbackRequest), any(Function.class), eq(AUTH_TOKEN)))
            .thenReturn(List.of("warning 1"));

        var response = handler.handle(callbackRequest, AUTH_TOKEN);
        assertThat(response.getWarnings()).containsExactly("warning 1");

        verify(uploadApprovedOrderService).processApprovedOrdersMh(caseDetails, AUTH_TOKEN);
        verify(documentWarningsHelper).getDocumentWarnings(eq(callbackRequest), any(Function.class), eq(AUTH_TOKEN));
    }

    private DirectionOrderCollection toDirectionOrderCollection(CaseDocument caseDocument) {
        return DirectionOrderCollection.builder()
            .value(DirectionOrder.builder().uploadDraftDocument(caseDocument).build())
            .build();
    }
}
