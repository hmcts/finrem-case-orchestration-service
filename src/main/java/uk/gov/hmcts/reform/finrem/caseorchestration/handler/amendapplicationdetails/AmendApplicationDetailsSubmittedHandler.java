package uk.gov.hmcts.reform.finrem.caseorchestration.handler.amendapplicationdetails;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignPartiesAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;

import java.util.List;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.AMEND_APP_DETAILS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.AMEND_CONTESTED_APP_DETAILS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.AMEND_CONTESTED_PAPER_APP_DETAILS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@Slf4j
@Service
public class AmendApplicationDetailsSubmittedHandler extends FinremCallbackHandler {

    private static final String CONFIRMATION_HEADER_WITH_ERROR = "Application amended with errors";

    private final AssignPartiesAccessService assignPartiesAccessService;

    private final RetryExecutor retryExecutor;

    @Autowired
    public AmendApplicationDetailsSubmittedHandler(FinremCaseDetailsMapper mapper,
                                                   RetryExecutor retryExecutor,
                                                   AssignPartiesAccessService assignPartiesAccessService) {
        super(mapper);
        this.retryExecutor = retryExecutor;
        this.assignPartiesAccessService = assignPartiesAccessService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.SUBMITTED.equals(callbackType)
            && (matchesContestedEvents(caseType, eventType) || matchesConsentedEvents(caseType, eventType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.submitted(callbackRequest));

        FinremCaseData finremCaseData = callbackRequest.getFinremCaseData();
        FinremCaseData finremCaseDataBefore = callbackRequest.getFinremCaseDataBefore();

        String grantAppSolicitorError = grantApplicantSolicitor(finremCaseData);
        String revokeAppSolicitorError = shouldRevokeOldApplicantSolicitor(callbackRequest)
            ? null : revokeApplicantSolicitor(finremCaseDataBefore);

        boolean isHavingErrors = !StringUtils.isAllBlank(grantAppSolicitorError, revokeAppSolicitorError);

        if (isHavingErrors) {
            return submittedResponse(
                toConfirmationHeader(CONFIRMATION_HEADER_WITH_ERROR),
                toConfirmationBody(grantAppSolicitorError, revokeAppSolicitorError));
        } else {
            return submittedResponse();
        }
    }

    private String grantApplicantSolicitor(FinremCaseData caseData) {
        String appSolicitorEmail = caseData.getAppSolicitorEmailIfRepresented();
        if (StringUtils.isBlank(appSolicitorEmail)) {
            return null;
        }
        try {
            retryExecutor.runWithRetry(() -> assignPartiesAccessService.grantApplicantSolicitor(caseData),
                "granting applicant solicitor", caseData.getCcdCaseId());
            return null;
        } catch (Exception ex) {
            log.error("Error granting applicant solicitor", ex);
            return "There was a problem granting access to applicant solicitor (%s). Please grant access manually."
                .formatted(appSolicitorEmail);
        }
    }

    private boolean shouldRevokeOldApplicantSolicitor(FinremCallbackRequest callbackRequest) {
        return callbackRequest.isApplicantSolicitorChanged()
            && StringUtils.isNotBlank(callbackRequest.getFinremCaseData().getAppSolicitorEmailIfRepresented());
    }

    private String revokeApplicantSolicitor(FinremCaseData caseDataBefore) {
        String appSolicitorEmail = caseDataBefore.getAppSolicitorEmailIfRepresented();
        try {
            retryExecutor.runWithRetry(() -> assignPartiesAccessService.revokeApplicantSolicitor(caseDataBefore),
                "revoking old applicant solicitor", caseDataBefore.getCcdCaseId());
            return null;
        } catch (Exception ex) {
            log.error("Error revoking old applicant solicitor", ex);
            return "There was a problem revoking access to applicant solicitor (%s). Please revoke access manually."
                .formatted(appSolicitorEmail);
        }
    }

    private boolean matchesConsentedEvents(CaseType caseType, EventType eventType) {
        return CONSENTED.equals(caseType) && AMEND_APP_DETAILS.equals(eventType);
    }

    private boolean matchesContestedEvents(CaseType caseType, EventType eventType) {
        return CONTESTED.equals(caseType) && List.of(AMEND_CONTESTED_PAPER_APP_DETAILS, AMEND_CONTESTED_APP_DETAILS)
            .contains(eventType);
    }
}
