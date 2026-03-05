package uk.gov.hmcts.reform.finrem.caseorchestration.handler.updatecasedetailssolicitor;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ContactDetailsValidator;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
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

        validateSolicitorFields(caseData, errors, userAuthorisation);
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData).errors(errors).build();
    }

    /*
     * Used for testing only.  Better option than reflection.
     * About-to-submit should return true, mid-event should return false.
     * @return boolean.
     */
    public boolean clearsTemporaryFields() {
        return shouldClearTemporaryFields();
    }

    protected void handleLog(FinremCallbackRequest callbackRequest) {

    }

    protected void validateSolicitorFields(FinremCaseData caseData,
                                           List<String> errors, String userAuthorisation) {
        ContactDetailsWrapper wrapper = caseData.getContactDetailsWrapper();

        if (YesOrNo.YES.equals(wrapper.getCurrentUserIsApplicantSolicitor())) {
            validateApplicantSolicitorFields(caseData, errors, userAuthorisation);
            return;
        }

        if (YesOrNo.YES.equals(wrapper.getCurrentUserIsRespondentSolicitor())) {
            validateRespondentSolicitorFields(caseData, errors, userAuthorisation);
            return;
        }

        throw new IllegalArgumentException(
                "Update Contact Details: Current user is not applicant or respondent solicitor. "
                + "Case reference:" + caseData.getCcdCaseId());
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

    // PT todo - would benefit from javadoc with some pseudo
    private void validateRespondentSolicitorFields(FinremCaseData caseData, List<String> errors, String userAuthorisation) {

        ContactDetailsWrapper wrapper = caseData.getContactDetailsWrapper();
        ContactDetailsValidator.checkForEmptyRespondentSolicitorPostcode(caseData, wrapper, errors);

        if (ContactDetailsValidator.checkForRespondentSolicitorEmail(caseData, wrapper, errors)) {
            errors.addAll(
                updateRepresentationService.validateEmailActiveForOrganisation(
                    caseData.getRespondentSolicitorEmail(),
                    caseData.getCcdCaseId(),
                    userAuthorisation)
            );
        }
    }
}
