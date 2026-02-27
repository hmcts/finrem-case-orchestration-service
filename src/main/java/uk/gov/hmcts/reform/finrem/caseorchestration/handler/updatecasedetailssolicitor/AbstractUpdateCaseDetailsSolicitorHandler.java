package uk.gov.hmcts.reform.finrem.caseorchestration.handler.updatecasedetailssolicitor;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ContactDetailsValidator;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.nocworkflows.UpdateRepresentationService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public abstract class AbstractUpdateCaseDetailsSolicitorHandler extends FinremCallbackHandler {

    private final UpdateRepresentationService updateRepresentationService;

    public AbstractUpdateCaseDetailsSolicitorHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                     UpdateRepresentationService updateRepresentationService) {
        super(finremCaseDetailsMapper);
        this.updateRepresentationService = updateRepresentationService;
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        handleLog(callbackRequest);
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData caseData = caseDetails.getData();
        List<String> errors = new ArrayList<>();

        validateSolicitorFieldsByCaseRole(caseData.getCurrentUserCaseRole(), caseData, errors, userAuthorisation);
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData).errors(errors).build();
    }

    protected void handleLog(FinremCallbackRequest callbackRequest) {

    }

    protected void validateSolicitorFieldsByCaseRole(CaseRole caseRole, FinremCaseData caseData,
                                                     List<String> errors, String userAuthorisation) {
        switch (caseRole) {
            case CaseRole.APP_SOLICITOR -> validateApplicantSolicitorFields(caseData, errors, userAuthorisation);
            case CaseRole.RESP_SOLICITOR -> validateRespondentSolicitorFields();
            case null -> throw new IllegalArgumentException(
                "Update Contact Details: CaseRole is null. Case reference:" + caseData.getCcdCaseId());
            default -> throw new IllegalArgumentException(
                "Update Contact Details provided invalid CaseRole.  Case reference:" + caseData.getCcdCaseId());
        }
    }

    // PT todo - would benefit from javadoc with some pseudo
    private void validateApplicantSolicitorFields(FinremCaseData caseData, List<String> errors, String userAuthorisation) {

        ContactDetailsWrapper wrapper = caseData.getContactDetailsWrapper();
        ContactDetailsValidator.checkForEmptyApplicantSolicitorPostcode(caseData, wrapper, errors);

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

        // PT todo, check which fields need to be temporary fields.  Will be at least currentUserCaseRole.
    }
}
