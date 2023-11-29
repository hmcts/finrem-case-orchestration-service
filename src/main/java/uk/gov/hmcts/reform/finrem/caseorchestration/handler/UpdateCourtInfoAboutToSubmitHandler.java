package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;


@Slf4j
@Service
public class UpdateCourtInfoAboutToSubmitHandler extends FinremCallbackHandler {

    private CourtDetailsMapper courtDetailsMapper;

    @Autowired
    public UpdateCourtInfoAboutToSubmitHandler(FinremCaseDetailsMapper mapper,
                                               CourtDetailsMapper courtDetailsMapper) {
        super(mapper);
        this.courtDetailsMapper = courtDetailsMapper;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && (EventType.UPDATE_COURT_INFO.equals(eventType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("About to Submit handler for FR_updateCourtInfo Case ID: {}", caseDetails.getId());

        FinremCaseData caseData = caseDetails.getData();

        caseData.getRegionWrapper()
            .setAllocatedRegionWrapper(
                courtDetailsMapper.getLatestAllocatedCourt(
                    callbackRequest.getCaseDetailsBefore().getData().getRegionWrapper().getAllocatedRegionWrapper(),
                    caseData.getRegionWrapper().getAllocatedRegionWrapper(),
                    true));

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData).build();
    }
}
