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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ValidateHearingService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class ManageHearingsAboutToSubmitHandler  extends FinremCallbackHandler {

    private final ValidateHearingService validateHearingService;

    public ManageHearingsAboutToSubmitHandler(FinremCaseDetailsMapper finreCaseDetailsMapper, ValidateHearingService validateHearingService) {
        super(finreCaseDetailsMapper);
        this.validateHearingService = validateHearingService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.MANAGE_HEARINGS.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.aboutToSubmit(callbackRequest));
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();

        FinremCaseData finremCaseData = caseDetails.getData();
        ManageHearingsWrapper manageHearingsWrapper = finremCaseData.getManageHearingsWrapper();

        List<String> errors = new ArrayList<>(validateHearingService.validateManageHearingErrors(finremCaseData));

        List<String> warnings = new ArrayList<>(validateHearingService.validateManageHearingWarnings(finremCaseData,
            manageHearingsWrapper.getWorkingHearing().getHearingType()));

        List<ManageHearingsCollectionItem> manageHearingsCollectionItemList = Optional.ofNullable(
                manageHearingsWrapper.getHearings())
            .orElseGet(ArrayList::new);

        UUID manageHearingID = UUID.randomUUID();
        manageHearingsCollectionItemList.add(
            ManageHearingsCollectionItem.builder().id(manageHearingID).value(manageHearingsWrapper.getWorkingHearing()).build()
        );
        manageHearingsWrapper.setWorkingHearingId(manageHearingID);
        manageHearingsWrapper.setHearings(manageHearingsCollectionItemList);

        manageHearingsWrapper.setWorkingHearing(null);
        manageHearingsWrapper.setManageHearingsActionSelection(null);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(finremCaseData).errors(errors).warnings(warnings).build();
    }
}
