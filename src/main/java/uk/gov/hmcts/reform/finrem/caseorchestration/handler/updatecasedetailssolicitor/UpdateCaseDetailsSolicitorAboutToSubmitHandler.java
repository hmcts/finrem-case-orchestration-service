package uk.gov.hmcts.reform.finrem.caseorchestration.handler.updatecasedetailssolicitor;

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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignPartiesAccessService;

import java.util.Optional;

@Slf4j
@Service
public class UpdateCaseDetailsSolicitorAboutToSubmitHandler extends FinremCallbackHandler {

    private final AssignPartiesAccessService assignPartiesAccessService;

    public UpdateCaseDetailsSolicitorAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                          AssignPartiesAccessService assignPartiesAccessService) {
        super(finremCaseDetailsMapper);
        this.assignPartiesAccessService = assignPartiesAccessService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.UPDATE_CASE_DETAILS_SOLICITOR.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.aboutToSubmit(callbackRequest));
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData caseData = caseDetails.getData();
        FinremCaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();

        if (solicitorEmailChanged(caseDetails, caseDetailsBefore)) {
            handleSolicitorEmailChange(caseDetails, caseDetailsBefore);
        }

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData).build();
    }

    /**
     * Determines if the representation change if applicant solicitor email has changed or applicant was not represented before.
     *
     * @param caseDetails       the current case data
     * @param caseDetailsBefore the case details before the update
     * @return true if the representation has changed, false otherwise
     */
    private static boolean solicitorEmailChanged(FinremCaseDetails caseDetails, FinremCaseDetails caseDetailsBefore) {
        Optional<ContactDetailsWrapper> contactDetailsWrapper = Optional.ofNullable(caseDetails.getData().getContactDetailsWrapper());
        String currentEmail = contactDetailsWrapper.map(ContactDetailsWrapper::getApplicantSolicitorEmail).orElse(null);
        Optional<ContactDetailsWrapper> beforeContactDetailsWrapper = Optional.ofNullable(caseDetailsBefore.getData().getContactDetailsWrapper());
        String beforeEmail = beforeContactDetailsWrapper.map(ContactDetailsWrapper::getApplicantSolicitorEmail).orElse(null);

        return (!caseDetailsBefore.getData().isApplicantRepresentedByASolicitor() && currentEmail != null
            || currentEmail != null && !currentEmail.equals(beforeEmail)
            || (caseDetailsBefore.getData().isApplicantRepresentedByASolicitor()
            && !caseDetails.getData().isApplicantRepresentedByASolicitor()));
    }

    private void handleSolicitorEmailChange(FinremCaseDetails finremCaseDetails, FinremCaseDetails caseDetailsBefore) {
        String caseId = finremCaseDetails.getData().getCcdCaseId();
        if (finremCaseDetails.getData().isApplicantRepresentedByASolicitor()) {
            log.info("Granting access to the new solicitor for case ID: {}", caseId);
            assignPartiesAccessService.grantApplicantSolicitor(finremCaseDetails.getData());
        }

        if (caseDetailsBefore.getData().isApplicantRepresentedByASolicitor()) {
            log.info("Revoking access for the previous solicitor for case ID: {}", caseDetailsBefore.getData().getCcdCaseId());
            assignPartiesAccessService.revokeApplicantSolicitor(finremCaseDetails.getData().getCcdCaseId(), caseDetailsBefore.getData());
        }
    }
}
