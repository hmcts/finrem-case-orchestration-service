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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.ManageHearingsMigrationService;

/**
 * Handler for the "Manage Hearings Migration" event during the ABOUT_TO_SUBMIT callback phase.
 *
 * <p>
 * This handler is temporarily implemented to support Playwright test automation or QA testing via the User Interface.
 * It populates the {@code ListForHearingWrapper} field in the {@link FinremCaseData} to simulate migrated hearing data.
 * </p>
 */
@Slf4j
@Service
public class ManageHearingsMigrationAboutToSubmitHandler extends FinremCallbackHandler {

    private final ManageHearingsMigrationService manageHearingsMigrationService;

    public ManageHearingsMigrationAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                       ManageHearingsMigrationService manageHearingsMigrationService) {
        super(finremCaseDetailsMapper);
        this.manageHearingsMigrationService = manageHearingsMigrationService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.MANAGE_HEARINGS_MIGRATION.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.aboutToSubmit(callbackRequest));
        FinremCaseDetails finremCaseDetails = callbackRequest.getCaseDetails();

        FinremCaseData finremCaseData = finremCaseDetails.getData();

        if (manageHearingsMigrationService.wasMigrated(finremCaseData)) {
            manageHearingsMigrationService.revertManageHearingMigration(finremCaseData);
        } else {
            manageHearingsMigrationService.runManageHearingMigration(finremCaseData, "ui");
        }

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(finremCaseData).build();
    }
}
