package uk.gov.hmcts.reform.finrem.caseorchestration.handler.processorders;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

}
