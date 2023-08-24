package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ConsentedHearingHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentedHearingDataWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDataConsented;

import java.util.List;

@Slf4j
@Service
public class ReadyForHearingConsentedAboutToStartHandler extends FinremCallbackHandler<FinremCaseDataConsented> {


    @Autowired
    public ReadyForHearingConsentedAboutToStartHandler(FinremCaseDetailsMapper mapper) {
        super(mapper);
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && EventType.READY_FOR_HEARING.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseDataConsented> handle(FinremCallbackRequest<FinremCaseDataConsented> callbackRequest,
                                                                                       String userAuthorisation) {
        FinremCaseDataConsented caseData = callbackRequest.getCaseDetails().getData();
        log.info("Received request to invoke event {} for Case ID: {}",
            EventType.READY_FOR_HEARING, caseData.getCcdCaseId());

        if (!isHearingDatePresent(caseData)) {
            return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseDataConsented>builder().data(caseData)
                .errors(List.of("There is no hearing on the case.")).build();
        }
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseDataConsented>builder().data(caseData).build();
    }

    private static boolean isHearingDatePresent(FinremCaseDataConsented caseData) {

        ConsentedHearingHelper helper = new ConsentedHearingHelper(new ObjectMapper());
        ConsentedHearingDataWrapper listForHearings = helper.getHearings(caseData).stream()
            .filter(hearing -> !hearing.getValue().getHearingDate().isEmpty())
            .findAny().orElse(null);

        return listForHearings != null;
    }
}