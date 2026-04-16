package uk.gov.hmcts.reform.finrem.caseorchestration.handler.updatecasedetailssolicitor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandlerLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenerateCoverSheetService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.nocworkflows.UpdateRepresentationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.AddressUtils;

import java.util.Map;

@Slf4j
@Service
public class UpdateCaseDetailsSolicitorContestedAboutToSubmitHandler extends AbstractUpdateCaseDetailsSolicitorHandler {

    private final GenerateCoverSheetService generateCoverSheetService;

    public UpdateCaseDetailsSolicitorContestedAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
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

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        handleLog(callbackRequest);
        FinremCaseDetails finremCaseDetails = callbackRequest.getCaseDetails();
        FinremCaseData finremCaseData = finremCaseDetails.getData();

        FinremCaseDetails finremCaseDetailsBefore = callbackRequest.getCaseDetailsBefore();
        generateCoverSheets(finremCaseDetails, finremCaseDetailsBefore, userAuthorisation);

        return response(finremCaseData);
    }

    private void generateCoverSheets(FinremCaseDetails caseDetails, FinremCaseDetails caseDetailsBefore, String userAuthorisation) {
        if (hasChangeApplicantSolicitor(caseDetails, caseDetailsBefore)) {
            generateCoverSheetService.generateAndSetApplicantCoverSheet(caseDetails, userAuthorisation);
        }

        if (hasChangeRespondantSolicitor(caseDetails, caseDetailsBefore)) {
            generateCoverSheetService.generateAndSetRespondentCoverSheet(caseDetails, userAuthorisation);
        }
    }

    private boolean hasChangeApplicantSolicitor(FinremCaseDetails finremCaseDetails, FinremCaseDetails finremCaseDetailsBefore) {
        Map<String, Object[]> fieldsChanged = ContactDetailsWrapper.diff(finremCaseDetails.getData().getContactDetailsWrapper(),
            finremCaseDetailsBefore.getData().getContactDetailsWrapper());

        Address applicantSolicitorAddress = finremCaseDetails.getData().getContactDetailsWrapper().getApplicantSolicitorAddress();
        Address applicantSolicitorAddressBefore = finremCaseDetailsBefore.getData().getContactDetailsWrapper().getApplicantSolicitorAddress();
        boolean applicantSolicitorAddressChanged = AddressUtils.hasChange(applicantSolicitorAddressBefore, applicantSolicitorAddress);

        return (fieldsChanged.containsKey("applicantSolicitorName")
            || fieldsChanged.containsKey("applicantSolicitorFirm")
            || applicantSolicitorAddressChanged);
    }

    private boolean hasChangeRespondantSolicitor(FinremCaseDetails finremCaseDetails, FinremCaseDetails finremCaseDetailsBefore) {
        Map<String, Object[]> fieldsChanged = ContactDetailsWrapper.diff(finremCaseDetails.getData().getContactDetailsWrapper(),
            finremCaseDetailsBefore.getData().getContactDetailsWrapper());

        Address respondentSolicitorAddress = finremCaseDetails.getData().getContactDetailsWrapper().getRespondentSolicitorAddress();
        Address respondentSolicitorAddressBefore = finremCaseDetailsBefore.getData().getContactDetailsWrapper().getRespondentSolicitorAddress();
        boolean respondentSolicitorAddressChanged = AddressUtils.hasChange(respondentSolicitorAddressBefore, respondentSolicitorAddress);

        return (fieldsChanged.containsKey("respondentSolicitorName")
            || fieldsChanged.containsKey("respondentSolicitorFirm")
            || respondentSolicitorAddressChanged);
    }
}
