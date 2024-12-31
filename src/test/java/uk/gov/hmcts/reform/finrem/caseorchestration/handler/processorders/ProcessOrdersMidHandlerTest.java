package uk.gov.hmcts.reform.finrem.caseorchestration.handler.processorders;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CfcCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionDetail;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionDetailCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.draftorders.HasApprovableCollectionReader;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.processorder.ProcessOrderService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
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

    @Spy
    private HasApprovableCollectionReader hasApprovableCollectionReader = new HasApprovableCollectionReader();

    @Spy
    private ProcessOrderService processOrderService = new ProcessOrderService(hasApprovableCollectionReader);

    @Test
    void testCanHandle() {
        assertCanHandle(underTest, MID_EVENT, CONTESTED, PROCESS_ORDER);
    }

    @Test
    void shouldCreateEmptyEntryWhenDirectionDetailsCollectionIsEmptyOrNull() {
        List<DirectionDetailCollection> expected = List.of(
            DirectionDetailCollection.builder().value(DirectionDetail.builder().build()).build()
        );

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
    void shouldShowErrorMessageIfLegacyUploadHearingOrderIsEmpty() {
        FinremCallbackRequest finremCallbackRequest = FinremCallbackRequestFactory.from(
            FinremCaseDetails.builder().data(
                FinremCaseData.builder()
                    .uploadHearingOrder(List.of(DirectionOrderCollection.builder().build()))
                    .build()),
            FinremCaseDetails.builder().data(
                FinremCaseData.builder()
                    .build())
        );
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> res = underTest.handle(finremCallbackRequest, AUTH_TOKEN);
        assertThat(res.getErrors()).hasSize(1);
        assertThat(res.getErrors()).contains("Upload Approved Order is required.");
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 0})
    void testDifferentErrorMessages(int scenario) {
        // Mock callbackRequest and its components
        FinremCallbackRequest callbackRequest = mock(FinremCallbackRequest.class);
        FinremCaseDetails caseDetailsBefore = mock(FinremCaseDetails.class);
        FinremCaseDetails caseDetails = mock(FinremCaseDetails.class);
        FinremCaseData caseDataBefore = spy(FinremCaseData.class);
        FinremCaseData caseData = spy(FinremCaseData.class);

        when(callbackRequest.getCaseDetailsBefore()).thenReturn(caseDetailsBefore);
        when(callbackRequest.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetailsBefore.getData()).thenReturn(caseDataBefore);
        when(caseDetails.getData()).thenReturn(caseData);

        // Set up mocks for different scenarios
        switch (scenario) {
            case 1:
                when(processOrderService.areAllLegacyApprovedOrdersRemoved(caseDataBefore, caseData)).thenReturn(true);
                break;
            case 2:
                when(processOrderService.areAllNewOrdersPdfFiles(caseDataBefore, caseData)).thenReturn(false);
                break;
            case 3:
                when(processOrderService.areAllLegacyApprovedOrdersPdf(caseData)).thenReturn(false);
                break;
            case 4:
                when(processOrderService.areAllModifyingUnprocessedOrdersWordDocuments(caseData)).thenReturn(false);
                break;
            case 0: // Success scenario
                when(processOrderService.areAllLegacyApprovedOrdersRemoved(caseDataBefore, caseData)).thenReturn(false);
                when(processOrderService.areAllNewOrdersPdfFiles(caseDataBefore, caseData)).thenReturn(true);
                when(processOrderService.areAllLegacyApprovedOrdersPdf(caseData)).thenReturn(true);
                when(processOrderService.areAllModifyingUnprocessedOrdersWordDocuments(caseData)).thenReturn(true);
                break;
            default:
                fail("unsupported scenario");
        }

        // Call the method under test
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> res = underTest.handle(callbackRequest, AUTH_TOKEN);

        // Verify results based on the scenario
        if (scenario == 1) {
            assertEquals(List.of("Upload Approved Order is required."), res.getErrors());
        } else if (scenario == 2) {
            assertEquals(List.of("You must upload a PDF file for new documents."), res.getErrors());
        } else if (scenario == 3) {
            assertEquals(List.of("You must upload a PDF file for modifying legacy approved documents."), res.getErrors());
        } else if (scenario == 4) {
            assertEquals(List.of("You must upload a Microsoft Word file for modifying an unprocessed approved documents."), res.getErrors());
        } else {
            assertEquals(0, res.getErrors().size(), "Expected no errors in the success scenario.");
        }

        // Verify service interactions
        verify(processOrderService).populateUnprocessedApprovedDocuments(caseDataBefore);
        if (scenario == 0) {
            verify(processOrderService).areAllLegacyApprovedOrdersRemoved(caseDataBefore, caseData);
            verify(processOrderService).areAllNewOrdersPdfFiles(caseDataBefore, caseData);
            verify(processOrderService).areAllLegacyApprovedOrdersPdf(caseData);
            verify(processOrderService).areAllModifyingUnprocessedOrdersWordDocuments(caseData);
        }
    }

}
