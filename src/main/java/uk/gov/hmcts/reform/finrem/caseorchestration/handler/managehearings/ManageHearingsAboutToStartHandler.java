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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.WorkingHearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PartyService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ValidateHearingService;

import java.util.List;

import static java.lang.String.format;

@Slf4j
@Service
public class ManageHearingsAboutToStartHandler extends FinremCallbackHandler {
    private final PartyService partyService;
    private final ValidateHearingService validateHearingService;

    public ManageHearingsAboutToStartHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                             PartyService partyService, ValidateHearingService validateHearingService) {
        super(finremCaseDetailsMapper);
        this.partyService = partyService;
        this.validateHearingService = validateHearingService;
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
                .hearingTypeDynamicList(generateHearingTypeList())
                .build()
        );

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(finremCaseData)
            .build();
    }

    private DynamicList generateHearingTypeList() {
        List<HearingType> hearingTypes = List.of(HearingType.values());

        List<DynamicListElement> listElements = hearingTypes.stream()
            .map(hearingType -> DynamicListElement.builder()
                .code(hearingType.name())
                .label(hearingType.getId())
                .build())
            .toList();

        return DynamicList.builder()
            .listItems(listElements)
            .build();
    }
}
