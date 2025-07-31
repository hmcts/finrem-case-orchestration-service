package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.MissingCourtException;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

import java.util.List;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.SEND_ORDER;

@Slf4j
@Service
public class SendOrderConsentForNotApprovedOrderAboutToStartHandler extends FinremCallbackHandler {

    private CourtDetailsMapper courtDetailsMapper;

    public SendOrderConsentForNotApprovedOrderAboutToStartHandler(FinremCaseDetailsMapper mapper,
                                                                  CourtDetailsMapper courtDetailsMapper) {
        super(mapper);
        this.courtDetailsMapper = courtDetailsMapper;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && SEND_ORDER.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.aboutToStart(callbackRequest));

        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        try {
            courtDetailsMapper.getCourtDetails(
                caseDetails.getData().getRegionWrapper().getDefaultCourtList());
        } catch (MissingCourtException e) {
            return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
                .errors(List.of("No FR court information is present on the case. "
                    + "Please add this information using Update FR Court Info."))
                .build();
        }
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseDetails.getData())
            .build();
    }
}
