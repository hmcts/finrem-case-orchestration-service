package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignPartiesAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;

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
            && EventType.AMEND_APP_DETAILS.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.submitted(callbackRequest));

        String grantAppSolicitorError = grantApplicantSolicitor(callbackRequest.getFinremCaseData());
        String revokeAppSolicitorError = revokeApplicantSolicitor(callbackRequest.getFinremCaseDataBefore());

        boolean isHavingErrors = !StringUtils.isAllBlank(grantAppSolicitorError, revokeAppSolicitorError);

        if (isHavingErrors) {
            return submittedResponse(
                toConfirmationHeader(CONFIRMATION_HEADER_WITH_ERROR),
                toConfirmationBody(grantAppSolicitorError));
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

    private String revokeApplicantSolicitor(FinremCaseData caseDataBefore) {
        String appSolicitorEmail = caseDataBefore.getAppSolicitorEmailIfRepresented();
        if (StringUtils.isBlank(appSolicitorEmail)) {
            return null;
        }
        try {
            retryExecutor.runWithRetry(() -> assignPartiesAccessService.revokeApplicantSolicitor(caseDataBefore),
                "revoking applicant solicitor", caseDataBefore.getCcdCaseId());
            return null;
        } catch (Exception ex) {
            log.error("Error revoking applicant solicitor", ex);
            return "There was a problem revoking access to applicant solicitor (%s). Please grant access manually."
                .formatted(appSolicitorEmail);
        }
    }
}
