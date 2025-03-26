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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SelectedCourtService;

@Slf4j
@Service
public class UpdateFrcInformationAboutToSubmitHandler extends FinremCallbackHandler {

    private final CourtDetailsMapper courtDetailsMapper;
    private final SelectedCourtService selectedCourtService;

    @Autowired
    public UpdateFrcInformationAboutToSubmitHandler(FinremCaseDetailsMapper mapper,
                                                    CourtDetailsMapper courtDetailsMapper,
                                                    SelectedCourtService selectedCourtService) {
        super(mapper);
        this.courtDetailsMapper = courtDetailsMapper;
        this.selectedCourtService = selectedCourtService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && (EventType.UPDATE_FRC_INFORMATION.equals(eventType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        CallbackHandlerLogger.aboutToSubmit(callbackRequest);

        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();
        caseData.getRegionWrapper()
            .setAllocatedRegionWrapper(
                courtDetailsMapper.getLatestAllocatedCourt(
                    callbackRequest.getCaseDetailsBefore().getData().getRegionWrapper().getAllocatedRegionWrapper(),
                    caseData.getRegionWrapper().getAllocatedRegionWrapper(),
                    false));
        selectedCourtService.setSelectedCourtDetailsIfPresent(caseData);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData).build();
    }
}
