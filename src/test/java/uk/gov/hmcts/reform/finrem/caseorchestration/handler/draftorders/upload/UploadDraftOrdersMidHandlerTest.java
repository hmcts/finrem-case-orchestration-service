package uk.gov.hmcts.reform.finrem.caseorchestration.handler.draftorders.upload;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed.UploadAgreedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingService;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.DraftOrdersConstants.ACCELERATED_ORDER_OPTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.DraftOrdersConstants.AGREED_DRAFT_ORDER_OPTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.DraftOrdersConstants.FDA_HEARING_LESS_THAN_14_DAYS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingType.ADJOURNED_FDA;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingType.DIR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingType.FDA;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class UploadDraftOrdersMidHandlerTest {

    @InjectMocks
    private UploadDraftOrdersMidHandler handler;

    @Mock
    private HearingService hearingService;

    @Test
    void canHandle() {
        assertCanHandle(handler, CallbackType.MID_EVENT, CaseType.CONTESTED, EventType.DRAFT_ORDERS);
    }

    @Test
    void givenAcceleratedOrderAndAdjournedFdaWithin14Days_thenReturnsValidationError() {
        FinremCaseData caseData = buildCaseDataWithTypeOfDraftOrder(ACCELERATED_ORDER_OPTION);

        when(hearingService.getHearingType(any(), any())).thenReturn(ADJOURNED_FDA.getId());
        when(hearingService.getHearingDate(any(), any())).thenReturn(LocalDate.now().plusDays(13));

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            handler.handle(FinremCallbackRequestFactory.from(caseData), AUTH_TOKEN);

        assertThat(response.getErrors())
            .containsExactly(FDA_HEARING_LESS_THAN_14_DAYS);
    }

    @Test
    void givenAcceleratedOrderAndFdaWithin14Days_thenReturnsValidationError() {
        FinremCaseData caseData = buildCaseDataWithTypeOfDraftOrder(ACCELERATED_ORDER_OPTION);

        when(hearingService.getHearingType(any(), any())).thenReturn(FDA.getId());
        when(hearingService.getHearingDate(any(), any())).thenReturn(LocalDate.now().plusDays(13));

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            handler.handle(FinremCallbackRequestFactory.from(caseData), AUTH_TOKEN);

        assertThat(response.getErrors())
            .containsExactly(FDA_HEARING_LESS_THAN_14_DAYS);
    }

    @Test
    void givenAcceleratedOrderAndNonFdaWithin14Days_thenNoValidationError() {
        FinremCaseData caseData = buildCaseDataWithTypeOfDraftOrder(ACCELERATED_ORDER_OPTION);

        when(hearingService.getHearingType(any(), any())).thenReturn(DIR.getId());
        when(hearingService.getHearingDate(any(), any())).thenReturn(LocalDate.now().plusDays(5));

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            handler.handle(FinremCallbackRequestFactory.from(caseData), AUTH_TOKEN);

        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void givenAcceleratedOrderAndFdaAt14Days_thenNoValidationError() {
        FinremCaseData caseData = buildCaseDataWithTypeOfDraftOrder(ACCELERATED_ORDER_OPTION);

        when(hearingService.getHearingType(any(), any())).thenReturn(FDA.getId());
        when(hearingService.getHearingDate(any(), any())).thenReturn(LocalDate.now().plusDays(14));

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            handler.handle(FinremCallbackRequestFactory.from(caseData), AUTH_TOKEN);

        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void givenAgreedDraftOrderAndFdaWithin14Days_thenNoValidationError() {
        FinremCaseData caseData = buildCaseDataWithTypeOfDraftOrder(AGREED_DRAFT_ORDER_OPTION);

        when(hearingService.getHearingType(any(), any())).thenReturn(FDA.getId());
        when(hearingService.getHearingDate(any(), any())).thenReturn(LocalDate.now().plusDays(5));

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            handler.handle(FinremCallbackRequestFactory.from(caseData), AUTH_TOKEN);

        assertThat(response.getErrors()).isEmpty();
    }

    private FinremCaseData buildCaseDataWithTypeOfDraftOrder(String typeOfDraftOrder) {
        DynamicList hearingDetails = mock(DynamicList.class);
        when(hearingDetails.getValue()).thenReturn(DynamicListElement.builder()
            .code(UUID.randomUUID().toString())
            .build());

        UploadAgreedDraftOrder uploadAgreedDraftOrder = UploadAgreedDraftOrder.builder()
            .hearingDetails(hearingDetails)
            .build();

        DraftOrdersWrapper draftOrdersWrapper = DraftOrdersWrapper.builder()
            .typeOfDraftOrder(typeOfDraftOrder)
            .uploadAgreedDraftOrder(uploadAgreedDraftOrder)
            .build();

        return FinremCaseData.builder()
            .draftOrdersWrapper(draftOrdersWrapper)
            .build();
    }
}
