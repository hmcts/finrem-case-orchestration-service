package uk.gov.hmcts.reform.finrem.caseorchestration.handler.updatecasedetailssolicitor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandlerLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ContactDetailsValidator;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.nocworkflows.UpdateRepresentationService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class UpdateCaseDetailsSolicitorContestedMidHandler extends FinremCallbackHandler {

    private final UpdateRepresentationService updateRepresentationService;

    public UpdateCaseDetailsSolicitorContestedMidHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                         UpdateRepresentationService updateRepresentationService) {
        super(finremCaseDetailsMapper);
        this.updateRepresentationService = updateRepresentationService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.MID_EVENT.equals(callbackType)
            && (CaseType.CONTESTED.equals(caseType) || CaseType.CONSENTED.equals(caseType))
            && EventType.UPDATE_CASE_DETAILS_SOLICITOR.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.midEvent(callbackRequest));
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData caseData = caseDetails.getData();
        List<String> errors = new ArrayList<>();

        switch (caseData.getCurrentUserCaseRole()) {
            case CaseRole.APP_SOLICITOR -> validateApplicantSolicitorFields(caseData, errors, userAuthorisation);
            case CaseRole.RESP_SOLICITOR -> validateRespondentSolicitorFields();
            case null -> throw new IllegalArgumentException(
                "Update Contact Details: CaseRole is null. Case reference:" + caseDetails.getCaseIdAsString());
            default -> throw new IllegalArgumentException(
                "Update Contact Details provided invalid CaseRole.  Case reference:" + caseDetails.getCaseIdAsString());
        }

        // PT todo - if this can be circumvented, with <>, then inherit this and absbmt handler from common abstract class.

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData).errors(errors).build();
    }

    // PT todo - would benefit from javadoc with some pseudo
    private void validateApplicantSolicitorFields(FinremCaseData caseData, List<String> errors, String userAuthorisation) {

        ContactDetailsWrapper wrapper = caseData.getContactDetailsWrapper();
        ContactDetailsValidator.checkForEmptyApplicantSolicitorPostcode(caseData, wrapper, errors);

        // Only check that the email is active for an org, once the email itself has undergone some validity checking.
        if (ContactDetailsValidator.checkForApplicantSolicitorEmailAddress(caseData, wrapper, errors)) {
            errors.addAll(
                updateRepresentationService.validateEmailActiveForOrganisation(
                    caseData.getAppSolicitorEmail(),
                    caseData.getCcdCaseId(),
                    userAuthorisation)
            );
        }
    }

    private void validateRespondentSolicitorFields() {
        log.info("to follow");
        // sol email org ok
        // email address validation
        // case addresses
    }
}
