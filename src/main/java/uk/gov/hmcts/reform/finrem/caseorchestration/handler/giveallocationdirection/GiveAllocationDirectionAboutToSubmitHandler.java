package uk.gov.hmcts.reform.finrem.caseorchestration.handler.giveallocationdirection;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandlerLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremAboutToSubmitCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ExpressCaseWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.RegionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SelectedCourtService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.express.ExpressCaseService;

import java.util.List;

@Slf4j
@Service
public class GiveAllocationDirectionAboutToSubmitHandler extends FinremAboutToSubmitCallbackHandler {

    private final CourtDetailsMapper courtDetailsMapper;
    private final SelectedCourtService selectedCourtService;
    private final ExpressCaseService expressCaseService;

    @Autowired
    public GiveAllocationDirectionAboutToSubmitHandler(FinremCaseDetailsMapper mapper,
                                                       CourtDetailsMapper courtDetailsMapper,
                                                       SelectedCourtService selectedCourtService,
                                                       ExpressCaseService expressCaseService) {
        super(mapper);
        this.courtDetailsMapper = courtDetailsMapper;
        this.selectedCourtService = selectedCourtService;
        this.expressCaseService = expressCaseService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && List.of(EventType.GIVE_ALLOCATION_DIRECTIONS, EventType.GIVE_ALLOCATION_DIRECTIONS_V2).contains(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.aboutToSubmit(callbackRequest));
        FinremCaseData finremCaseData = callbackRequest.getFinremCaseData();
        FinremCaseData finremCaseDataBefore = callbackRequest.getFinremCaseDataBefore();

        RegionWrapper regionWrapper = finremCaseData.getRegionWrapper();

        regionWrapper.setAllocatedRegionWrapper(
            courtDetailsMapper.getLatestAllocatedCourt(
                finremCaseDataBefore.getRegionWrapper().getAllocatedRegionWrapper(),
                regionWrapper.getAllocatedRegionWrapper(),
                false));

        selectedCourtService.setSelectedCourtDetailsIfPresent(finremCaseData);

        setExpressPilotStatus(finremCaseData);

        return response(finremCaseData);
    }

    private void setExpressPilotStatus(FinremCaseData finremCaseData) {
        ExpressCaseWrapper expressCaseWrapper = finremCaseData.getExpressCaseWrapper();
        if (YesOrNo.isYes(expressCaseWrapper.getShouldAllocateToExpressPilot())) {
            expressCaseService.setExpressCaseEnrollmentStatus(finremCaseData);
            log.info("{} - Setting express case enrollment status to {}", finremCaseData.getCcdCaseId(),
                expressCaseWrapper.getExpressCaseParticipation());
        }
    }
}
