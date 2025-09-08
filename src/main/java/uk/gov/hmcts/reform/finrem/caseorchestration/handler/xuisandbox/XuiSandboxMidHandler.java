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

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.MID_EVENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.XUI_SANDBOX;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.XUI_SANDBOX_ONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.XUI_SANDBOX_TWO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;

@Slf4j
@Service
public class XuiSandboxMidHandler extends FinremCallbackHandler {

    public XuiSandboxMidHandler(FinremCaseDetailsMapper finremCaseDetailsMapper) {
        super(finremCaseDetailsMapper);
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return MID_EVENT.equals(callbackType) && CONSENTED.equals(caseType)
                && (XUI_SANDBOX.equals(eventType) || XUI_SANDBOX_ONE.equals(eventType) || XUI_SANDBOX_TWO.equals(eventType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.midEvent(callbackRequest));

        FinremCaseDetails finremCaseDetails = callbackRequest.getCaseDetails();
        FinremCaseData finremCaseData = finremCaseDetails.getData();

        boolean isApplicantOrganisationPolicyNull = finremCaseData.getApplicantOrganisationPolicy() == null;
        log.debug(format("DEBUG (%s): Is ApplicantOrganisationPolicy null? %s", callbackRequest.getEventType(),
            isApplicantOrganisationPolicyNull));
        boolean isRespondentOrganisationPolicyNull = finremCaseData.getRespondentOrganisationPolicy() == null;
        log.debug(format("DEBUG (%s): Is RespondentOrganisationPolicy null? %s", callbackRequest.getEventType(),
            isRespondentOrganisationPolicyNull));
        if (isApplicantOrganisationPolicyNull) {
            throw new IllegalStateException("ApplicantOrganisationPolicy is null");
        }
        if (isRespondentOrganisationPolicyNull) {
            throw new IllegalStateException("RespondentOrganisationPolicy is null");
        }

        List<String> errors = new ArrayList<>();
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(finremCaseData).errors(errors).build();
    }
}
