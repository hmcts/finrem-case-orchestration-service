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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SolicitorAccessService;

@Slf4j
@Service
public class UpdateCaseDetailsSolicitorSubmittedHandler extends FinremCallbackHandler {

    private final SolicitorAccessService solicitorAccessService;

    public UpdateCaseDetailsSolicitorSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                      SolicitorAccessService solicitorAccessService) {
        super(finremCaseDetailsMapper);
        this.solicitorAccessService = solicitorAccessService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.SUBMITTED.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.UPDATE_CONTACT_DETAILS.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.aboutToSubmit(callbackRequest));
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData caseData = caseDetails.getData();
        FinremCaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();
        FinremCaseData caseDataBefore = caseDetailsBefore.getData();

        // Fix for missing CCD Case ID in case data before, which is required for updating solicitor access
        caseDataBefore.setCcdCaseId(String.valueOf(caseDetailsBefore.getId()));

        if(SolicitorAccessService.hasApplicantSolicitorChanged(caseData, caseDataBefore)) {
            solicitorAccessService.updateApplicantSolicitor(caseData, caseDataBefore);
        }

        if(SolicitorAccessService.hasRespondentSolicitorChanged(caseData, caseDataBefore)) {
            solicitorAccessService.updateRespondentSolicitor(caseData, caseDataBefore);
        }

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData).build();
    }
}
