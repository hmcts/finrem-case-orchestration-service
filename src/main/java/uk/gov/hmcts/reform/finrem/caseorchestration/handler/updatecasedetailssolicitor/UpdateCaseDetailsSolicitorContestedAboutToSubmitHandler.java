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

import java.util.Objects;
import java.util.Optional;

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
        super.handle(callbackRequest, userAuthorisation);
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

        if (hasChangeRespondentSolicitor(caseDetails, caseDetailsBefore)) {
            generateCoverSheetService.generateAndSetRespondentCoverSheet(caseDetails, userAuthorisation);
        }
    }

    private boolean hasChangeApplicantSolicitor(FinremCaseDetails finremCaseDetails, FinremCaseDetails finremCaseDetailsBefore) {
        String applicantSolicitorName = finremCaseDetails.getAppSolicitorName();
        String applicantSolicitorNameBefore = finremCaseDetailsBefore.getAppSolicitorName();
        boolean applicantSolicitorNameHasChanged = !applicantSolicitorName.equals(applicantSolicitorNameBefore);

        String applicantSolicitorFirm = finremCaseDetails.getAppSolicitorFirm();
        String applicantSolicitorFirmBefore = finremCaseDetailsBefore.getAppSolicitorFirm();
        boolean applicantSolicitorFirmHasChanged = !applicantSolicitorFirm.equals(applicantSolicitorFirmBefore);
        
        Address applicantSolicitorAddress = finremCaseDetails.getData().getContactDetailsWrapper().getApplicantSolicitorAddress();
        Address applicantSolicitorAddressBefore = finremCaseDetailsBefore.getData().getContactDetailsWrapper().getApplicantSolicitorAddress();
        boolean applicantSolicitorAddressChanged = AddressUtils.hasChange(applicantSolicitorAddressBefore, applicantSolicitorAddress);

        return (applicantSolicitorNameHasChanged
            || applicantSolicitorFirmHasChanged
            || applicantSolicitorAddressChanged);
    }

    private boolean hasChangeRespondentSolicitor(FinremCaseDetails finremCaseDetails, FinremCaseDetails finremCaseDetailsBefore) {
        String respondentSolicitorName = finremCaseDetails.getRespSolicitorName();
        String respondentSolicitorNameBefore = finremCaseDetailsBefore.getRespSolicitorName();
        boolean respondentSolicitorNameHasChanged = !Objects.equals(respondentSolicitorName, respondentSolicitorNameBefore);

        String respondentSolicitorFirm = finremCaseDetails.getData().getContactDetailsWrapper().getRespondentSolicitorFirm();
        String respondentSolicitorFirmBefore = finremCaseDetailsBefore.getData().getContactDetailsWrapper().getRespondentSolicitorFirm();
        boolean respondentSolicitorFirmHasChanged = !Objects.equals(respondentSolicitorFirm, respondentSolicitorFirmBefore);

        Address respondentSolicitorAddress = finremCaseDetails.getData().getContactDetailsWrapper().getRespondentSolicitorAddress();
        Address respondentSolicitorAddressBefore = finremCaseDetailsBefore.getData().getContactDetailsWrapper().getRespondentSolicitorAddress();
        boolean respondentSolicitorAddressChanged = AddressUtils.hasChange(respondentSolicitorAddressBefore, respondentSolicitorAddress);

        return (respondentSolicitorNameHasChanged
            || respondentSolicitorFirmHasChanged
            || respondentSolicitorAddressChanged);
    }

    /**
     * Checks if the applicantSolicitorName has changed between the two case details.
     * @param current current case details
     * @param before previous case details
     * @return true if applicantSolicitorName has changed, false otherwise
     */
    public boolean hasChangeApplicantSolicitorName(FinremCaseDetails current, FinremCaseDetails before) {
        return !Objects.equals(
            Optional.ofNullable(current).map(FinremCaseDetails::getAppSolicitorName).orElse(null),
            Optional.ofNullable(before).map(FinremCaseDetails::getAppSolicitorName).orElse(null)
        );
    }

    /**
     * Checks if the applicantSolicitorFirm has changed between the two case details.
     * @param current current case details
     * @param before previous case details
     * @return true if applicantSolicitorFirm has changed, false otherwise
     */
    public boolean hasChangeApplicantSolicitorFirm(FinremCaseDetails current, FinremCaseDetails before) {
        return !Objects.equals(
            Optional.ofNullable(current).map(FinremCaseDetails::getAppSolicitorFirm).orElse(null),
            Optional.ofNullable(before).map(FinremCaseDetails::getAppSolicitorFirm).orElse(null)
        );
    }

    /**
     * Checks if the respondentSolicitorName has changed between the two case details.
     * @param current current case details
     * @param before previous case details
     * @return true if respondentSolicitorName has changed, false otherwise
     */
    public boolean hasChangeRespondentSolicitorName(FinremCaseDetails current, FinremCaseDetails before) {
        return !Objects.equals(
            Optional.ofNullable(current)
                .map(FinremCaseDetails::getData)
                .map(FinremCaseData::getContactDetailsWrapper)
                .map(ContactDetailsWrapper::getRespondentSolicitorName)
                .orElse(null),
            Optional.ofNullable(before)
                .map(FinremCaseDetails::getData)
                .map(FinremCaseData::getContactDetailsWrapper)
                .map(ContactDetailsWrapper::getRespondentSolicitorName)
                .orElse(null)
        );
    }

    /**
     * Checks if the respondentSolicitorFirm has changed between the two case details.
     * @param current current case details
     * @param before previous case details
     * @return true if respondentSolicitorFirm has changed, false otherwise
     */
    public boolean hasChangeRespondentSolicitorFirm(FinremCaseDetails current, FinremCaseDetails before) {
        return !Objects.equals(
            Optional.ofNullable(current)
                .map(FinremCaseDetails::getData)
                .map(FinremCaseData::getContactDetailsWrapper)
                .map(ContactDetailsWrapper::getRespondentSolicitorFirm)
                .orElse(null),
            Optional.ofNullable(before)
                .map(FinremCaseDetails::getData)
                .map(FinremCaseData::getContactDetailsWrapper)
                .map(ContactDetailsWrapper::getRespondentSolicitorFirm)
                .orElse(null)
        );
    }
}
