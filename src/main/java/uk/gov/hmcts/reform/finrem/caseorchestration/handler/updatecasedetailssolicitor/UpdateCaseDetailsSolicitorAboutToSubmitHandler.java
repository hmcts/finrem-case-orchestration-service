package uk.gov.hmcts.reform.finrem.caseorchestration.handler.updatecasedetailssolicitor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandlerLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenerateCoverSheetService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.nocworkflows.UpdateRepresentationService;

@Slf4j
@Service
public class UpdateCaseDetailsSolicitorAboutToSubmitHandler extends AbstractUpdateCaseDetailsSolicitorHandler {

    private final GenerateCoverSheetService generateCoverSheetService;

    public UpdateCaseDetailsSolicitorAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                          UpdateRepresentationService updateRepresentationService,
                                                          GenerateCoverSheetService generateCoverSheetService) {
        super(finremCaseDetailsMapper, updateRepresentationService);
        this.generateCoverSheetService = generateCoverSheetService;
    }

    @Override
    /*
     * Method shouldClearTemporaryFields explicitly overridden in this about-to-submit handler.
     * Generally, extending FinremAboutToSubmitCallbackHandler is better.
     * But this extends AbstractUpdateCaseDetailsSolicitorHandler, shared with the mid-handler, which depends on the fields.
     */
    protected final boolean shouldClearTemporaryFields() {
        return true;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && (CaseType.CONTESTED.equals(caseType) || CaseType.CONSENTED.equals(caseType))
            && EventType.UPDATE_CASE_DETAILS_SOLICITOR.equals(eventType);
    }

    @Override
    protected void handleLog(FinremCallbackRequest callbackRequest) {
        log.info(CallbackHandlerLogger.aboutToSubmit(callbackRequest));
    }

    /**
     *  This method is responsible for generating cover sheets for the applicant and
     *  respondent when there is a change in their solicitor details.
     */
    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = super.handle(callbackRequest, userAuthorisation);
        FinremCaseDetails finremCaseDetails = callbackRequest.getCaseDetails();
        FinremCaseDetails finremCaseDetailsBefore = callbackRequest.getCaseDetailsBefore();

        generateCoverSheets(finremCaseDetails, finremCaseDetailsBefore.getData().getContactDetailsWrapper(), userAuthorisation);
        return response;
    }

    private void generateCoverSheets(FinremCaseDetails caseDetails, ContactDetailsWrapper contactDetailsBefore, String userAuthorisation) {
        if (ContactDetailsWrapper.hasApplicantAddressDetailsChanged(caseDetails.getData().getContactDetailsWrapper(), contactDetailsBefore)) {
            generateCoverSheetService.generateAndSetApplicantCoverSheet(caseDetails, userAuthorisation);
        }
        if (ContactDetailsWrapper.hasRespondentAddressDetailsChanged(caseDetails.getData().getContactDetailsWrapper(), contactDetailsBefore)) {
            generateCoverSheetService.generateAndSetRespondentCoverSheet(caseDetails, userAuthorisation);
        }
    }
}
