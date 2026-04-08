package uk.gov.hmcts.reform.finrem.caseorchestration.handler.updatecontactdetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandlerLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenerateCoverSheetService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.AddressUtils;

import java.util.Map;

@Slf4j
@Service
public class UpdateContactDetailsSubmittedHandler extends FinremCallbackHandler {
    private final GenerateCoverSheetService generateCoverSheetService;

    public UpdateContactDetailsSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                GenerateCoverSheetService generateCoverSheetService) {
        super(finremCaseDetailsMapper);
        this.generateCoverSheetService = generateCoverSheetService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.SUBMITTED.equals(callbackType)
            && (CaseType.CONTESTED.equals(caseType) || CaseType.CONSENTED.equals(caseType))
            && EventType.UPDATE_CONTACT_DETAILS.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.submitted(callbackRequest));
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();
        generateCoverSheets(caseDetails, caseDetailsBefore, userAuthorisation);
        return submittedResponse();
    }

    private void generateCoverSheets(FinremCaseDetails caseDetails, FinremCaseDetails caseDetailsBefore, String userAuthorisation) {
        if (hasChangeApplicant(caseDetails, caseDetailsBefore)) {
            generateCoverSheetService.generateAndSetApplicantCoverSheet(caseDetails, userAuthorisation);
        }
        if (hasChangeRespondant(caseDetails, caseDetailsBefore)) {
            generateCoverSheetService.generateAndSetRespondentCoverSheet(caseDetails, userAuthorisation);
        }
    }

    private boolean hasChangeApplicant(FinremCaseDetails finremCaseDetails, FinremCaseDetails finremCaseDetailsBefore) {
        Map<String, Object[]> fieldsChanged = ContactDetailsWrapper.diff(finremCaseDetails.getData().getContactDetailsWrapper(),
            finremCaseDetailsBefore.getData().getContactDetailsWrapper());

        Address applicantAddress = finremCaseDetails.getData().getContactDetailsWrapper().getApplicantAddress();
        Address applicantAddressBefore = finremCaseDetailsBefore.getData().getContactDetailsWrapper().getApplicantAddress();
        boolean applicantAddressChanged = AddressUtils.hasChange(applicantAddressBefore, applicantAddress);

        return (fieldsChanged.keySet().contains("applicantFmName")
            || fieldsChanged.keySet().contains("applicantLname")
            || applicantAddressChanged);
    }

    private boolean hasChangeRespondant(FinremCaseDetails finremCaseDetails, FinremCaseDetails finremCaseDetailsBefore) {
        Map<String, Object[]> fieldsChanged = ContactDetailsWrapper.diff(finremCaseDetails.getData().getContactDetailsWrapper(),
            finremCaseDetailsBefore.getData().getContactDetailsWrapper());

        Address respondentAddress = finremCaseDetails.getData().getContactDetailsWrapper().getRespondentAddress();
        Address respondentAddressBefore = finremCaseDetailsBefore.getData().getContactDetailsWrapper().getRespondentAddress();
        boolean respondentAddressChanged = AddressUtils.hasChange(respondentAddressBefore, respondentAddress);

        return (fieldsChanged.keySet().contains("respondentFmName")
            || fieldsChanged.keySet().contains("respondentLname")
            || respondentAddressChanged);
    }
}
