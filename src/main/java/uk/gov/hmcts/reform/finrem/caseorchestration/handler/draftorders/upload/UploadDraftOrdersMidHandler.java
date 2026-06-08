package uk.gov.hmcts.reform.finrem.caseorchestration.handler.draftorders.upload;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandlerLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed.UploadAgreedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingService;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.DraftOrdersConstants.ACCELERATED_ORDER_OPTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.DraftOrdersConstants.FDA_HEARING_LESS_THAN_14_DAYS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingType.ADJOURNED_FDA;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingType.FDA;

@Slf4j
@Service
public class UploadDraftOrdersMidHandler extends FinremCallbackHandler {

    private final HearingService hearingService;

    public UploadDraftOrdersMidHandler(FinremCaseDetailsMapper finremCaseDetailsMapper, HearingService hearingService) {
        super(finremCaseDetailsMapper);
        this.hearingService = hearingService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.MID_EVENT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.DRAFT_ORDERS.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.midEvent(callbackRequest));

        FinremCaseData finremCaseData = callbackRequest.getFinremCaseData();
        DraftOrdersWrapper draftOrdersWrapper = finremCaseData.getDraftOrdersWrapper();
        UploadAgreedDraftOrder uploadAgreedDraftOrder = draftOrdersWrapper.getUploadAgreedDraftOrder();

        String hearingType = hearingService.getHearingType(
            finremCaseData, uploadAgreedDraftOrder.getHearingDetails().getValue()
        );
        LocalDate hearingDate = hearingService.getHearingDate(
            finremCaseData, uploadAgreedDraftOrder.getHearingDetails().getValue()
        );

        List<String> errors = new ArrayList<>();
        if (isAcceleratedOrderWithin14Days(draftOrdersWrapper, hearingType, hearingDate)) {
            errors.add(FDA_HEARING_LESS_THAN_14_DAYS);
        }

        return response(finremCaseData, null, errors);
    }

    private boolean isAcceleratedOrderWithin14Days(DraftOrdersWrapper draftOrdersWrapper,
                                                   String hearingType,
                                                   LocalDate hearingDate) {
        return Objects.equals(ACCELERATED_ORDER_OPTION, draftOrdersWrapper.getTypeOfDraftOrder())
            && (FDA.getId().equalsIgnoreCase(hearingType)
            || ADJOURNED_FDA.getId().equalsIgnoreCase(hearingType))
            && ChronoUnit.DAYS.between(LocalDate.now(), hearingDate) < 14;
    }
}
