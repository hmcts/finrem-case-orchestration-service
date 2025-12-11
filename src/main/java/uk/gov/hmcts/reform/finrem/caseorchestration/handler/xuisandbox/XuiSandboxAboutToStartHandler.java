package uk.gov.hmcts.reform.finrem.caseorchestration.handler.xuisandbox;

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

import static java.lang.String.format;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.XUI_SANDBOX;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.XUI_SANDBOX_ONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.XUI_SANDBOX_TWO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;

@Slf4j
@Service
public class XuiSandboxAboutToStartHandler extends FinremCallbackHandler {

    public XuiSandboxAboutToStartHandler(FinremCaseDetailsMapper finremCaseDetailsMapper) {
        super(finremCaseDetailsMapper);
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return ABOUT_TO_START.equals(callbackType) && CONSENTED.equals(caseType)
                && (XUI_SANDBOX.equals(eventType) || XUI_SANDBOX_ONE.equals(eventType) || XUI_SANDBOX_TWO.equals(eventType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.aboutToStart(callbackRequest));

        FinremCaseDetails finremCaseDetails = callbackRequest.getCaseDetails();
        FinremCaseData finremCaseData = finremCaseDetails.getData();

        boolean isApplicantOrganisationPolicyNull = finremCaseData.getApplicantOrganisationPolicy() == null;
        log.debug(format("DEBUG (%s): Is ApplicantOrganisationPolicy null? %s", callbackRequest.getEventType(),
            isApplicantOrganisationPolicyNull));
        boolean isRespondentOrganisationPolicyNull = finremCaseData.getApplicantOrganisationPolicy() == null;
        log.debug(format("DEBUG (%s): Is RespondentOrganisationPolicy null? %s", callbackRequest.getEventType(),
            isRespondentOrganisationPolicyNull));

        // reset nocParty
        finremCaseData.getContactDetailsWrapper().setNocParty(null);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(finremCaseData).build();
    }
}
