package uk.gov.hmcts.reform.finrem.caseorchestration.handler.processorders;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CfcCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionDetail;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionDetailCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.processorder.ProcessOrderService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.MID_EVENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.PROCESS_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class ProcessOrdersMidHandlerTest {

    @InjectMocks
    private ProcessOrdersMidHandler underTest;

    @Mock
    private ProcessOrderService processOrderService;

    @Test
    void testCanHandle() {
        assertCanHandle(underTest, MID_EVENT, CONTESTED, PROCESS_ORDER);
    }

    private void mockPassAllValidations() {
        when(processOrderService.areAllLegacyApprovedOrdersRemoved(any(FinremCaseData.class), any(FinremCaseData.class))).thenReturn(false);
        when(processOrderService.areAllNewOrdersPdfFiles(any(FinremCaseData.class), any(FinremCaseData.class))).thenReturn(true);
        when(processOrderService.areAllLegacyApprovedOrdersPdf(any(FinremCaseData.class))).thenReturn(true);
        when(processOrderService.areAllModifyingUnprocessedOrdersWordDocuments(any(FinremCaseData.class))).thenReturn(true);
    }

    @Test
    void shouldCreateEmptyEntryWhenDirectionDetailsCollectionIsEmptyOrNull() {
        List<DirectionDetailCollection> expected = List.of(
            DirectionDetailCollection.builder().value(DirectionDetail.builder().build()).build()
        );

        mockPassAllValidations();

        FinremCallbackRequest finremCallbackRequest = FinremCallbackRequestFactory.from(FinremCaseData.builder().build());
        FinremCaseData result = underTest.handle(finremCallbackRequest, AUTH_TOKEN).getData();
        assertEquals(expected, result.getDirectionDetailsCollection());

        finremCallbackRequest = FinremCallbackRequestFactory.from(FinremCaseData.builder().directionDetailsCollection(List.of()).build());
        result = underTest.handle(finremCallbackRequest, AUTH_TOKEN).getData();
        assertEquals(expected, result.getDirectionDetailsCollection());
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
        FinremCaseData result = underTest.handle(finremCallbackRequest, AUTH_TOKEN).getData();
        assertNotEquals(notExpected, result.getDirectionDetailsCollection());

        finremCallbackRequest = FinremCallbackRequestFactory.from(FinremCaseData.builder()
            .directionDetailsCollection(List.of(
                DirectionDetailCollection.builder().value(DirectionDetail.builder().cfcList(CfcCourt.BARNET_CIVIL_AND_FAMILY_COURTS_CENTRE)
                    .build()).build()
            ))
            .build());
        result = underTest.handle(finremCallbackRequest, AUTH_TOKEN).getData();
        assertNotEquals(notExpected, result.getDirectionDetailsCollection());
    }

    @Test
    void shouldShowErrorMessageWhenAllLegacyApprovedOrdersRemoved() {
        when(processOrderService.areAllLegacyApprovedOrdersRemoved(any(FinremCaseData.class), any(FinremCaseData.class))).thenReturn(true);
        FinremCallbackRequest finremCallbackRequest = FinremCallbackRequestFactory.from(FinremCaseData.builder().build());
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData>  res = underTest.handle(finremCallbackRequest, AUTH_TOKEN);
        assertEquals(List.of("Upload Approved Order is required."), res.getErrors());
    }

    @Test
    void shouldShowErrorMessageWhenNotAllNewOrdersPdfFiles() {
        when(processOrderService.areAllLegacyApprovedOrdersRemoved(any(FinremCaseData.class), any(FinremCaseData.class))).thenReturn(false);
        when(processOrderService.areAllNewOrdersPdfFiles(any(FinremCaseData.class), any(FinremCaseData.class))).thenReturn(false);
        FinremCallbackRequest finremCallbackRequest = FinremCallbackRequestFactory.from(FinremCaseData.builder().build());
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData>  res = underTest.handle(finremCallbackRequest, AUTH_TOKEN);
        assertEquals(List.of("You must upload a PDF file for new documents."), res.getErrors());
    }

    @Test
    void shouldShowErrorMessageWhenNotAllLegacyApprovedOrdersPdf() {
        when(processOrderService.areAllLegacyApprovedOrdersRemoved(any(FinremCaseData.class), any(FinremCaseData.class))).thenReturn(false);
        when(processOrderService.areAllNewOrdersPdfFiles(any(FinremCaseData.class), any(FinremCaseData.class))).thenReturn(true);
        when(processOrderService.areAllLegacyApprovedOrdersPdf(any(FinremCaseData.class))).thenReturn(false);
        FinremCallbackRequest finremCallbackRequest = FinremCallbackRequestFactory.from(FinremCaseData.builder().build());
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData>  res = underTest.handle(finremCallbackRequest, AUTH_TOKEN);
        assertEquals(List.of("You must upload a PDF file for modifying legacy approved documents."), res.getErrors());
    }

    @Test
    void shouldShowErrorMessageWhenNotAllModifyingUnprocessedOrdersWordDocuments() {
        when(processOrderService.areAllLegacyApprovedOrdersRemoved(any(FinremCaseData.class), any(FinremCaseData.class))).thenReturn(false);
        when(processOrderService.areAllNewOrdersPdfFiles(any(FinremCaseData.class), any(FinremCaseData.class))).thenReturn(true);
        when(processOrderService.areAllLegacyApprovedOrdersPdf(any(FinremCaseData.class))).thenReturn(true);
        when(processOrderService.areAllModifyingUnprocessedOrdersWordDocuments(any(FinremCaseData.class))).thenReturn(false);

        FinremCallbackRequest finremCallbackRequest = FinremCallbackRequestFactory.from(FinremCaseData.builder().build());
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData>  res = underTest.handle(finremCallbackRequest, AUTH_TOKEN);
        assertEquals(List.of("You must upload a Microsoft Word file for modifying an unprocessed approved documents."), res.getErrors());
    }
}
