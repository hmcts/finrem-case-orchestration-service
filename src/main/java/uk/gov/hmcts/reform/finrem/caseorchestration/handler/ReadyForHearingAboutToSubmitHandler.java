package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
public class ReadyForHearingAboutToSubmitHandler extends FinremCallbackHandler {

    @Autowired
    public ReadyForHearingAboutToSubmitHandler(FinremCaseDetailsMapper mapper) {
        super(mapper);
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && EventType.READY_FOR_HEARING.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData caseData = caseDetails.getData();
        log.info("Received request to update consented case with Case ID: {}", caseDetails.getId());

        if (!isHearingDatePresent(caseData)) {
            return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData)
                .errors(List.of("There is no hearing on the case.")).build();
        }
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData).build();
    }

    private static boolean isHearingDatePresent(FinremCaseData caseData) {
        return caseData.getHearingDate() != null && caseData.getHearingDate().isAfter(LocalDate.now().minusDays(1));
    }
}