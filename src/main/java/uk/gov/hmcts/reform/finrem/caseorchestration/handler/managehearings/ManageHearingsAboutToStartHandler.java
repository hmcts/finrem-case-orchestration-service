package uk.gov.hmcts.reform.finrem.caseorchestration.handler.managehearings;

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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.VacateOrAdjournHearingAction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.WorkingHearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PartyService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ValidateHearingService;

import java.util.List;

@Slf4j
@Service
public class ManageHearingsAboutToStartHandler extends FinremCallbackHandler {
    private final PartyService partyService;
    private final ValidateHearingService validateHearingService;
    private final HearingService hearingService;

    public ManageHearingsAboutToStartHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                             PartyService partyService, ValidateHearingService validateHearingService,
                                             HearingService hearingService) {
        super(finremCaseDetailsMapper);
        this.partyService = partyService;
        this.validateHearingService = validateHearingService;
        this.hearingService = hearingService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.MANAGE_HEARINGS.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {

        log.info(CallbackHandlerLogger.aboutToStart(callbackRequest));
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();

        FinremCaseData finremCaseData = caseDetails.getData();

        List<String> errors = validateHearingService.validateManageHearingErrors(finremCaseData);
        if (!errors.isEmpty()) {
            return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
                .data(finremCaseData)
                .errors(errors)
                .build();
        }

        // Reset any previous Manage Hearings action selection
        finremCaseData.getManageHearingsWrapper()
            .setManageHearingsActionSelection(null);

        finremCaseData.getManageHearingsWrapper().setWorkingHearing(
            WorkingHearing.builder()
                .partiesOnCaseMultiSelectList(partyService.getAllActivePartyList(caseDetails))
                .withHearingTypes(HearingType.values())
                .build()
        );
        // Remove this line after /DFR-4138 release
        finremCaseData.setCcdCaseId(caseDetails.getCaseIdAsString());
        finremCaseData.getManageHearingsWrapper().setVacateOrAdjournHearingAction(
            VacateOrAdjournHearingAction.builder()
                .chooseHearings(hearingService.generateSelectableHearingsAsDynamicList(caseDetails, userAuthorisation))
                .build()
        );

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(finremCaseData)
            .build();
    }

}
