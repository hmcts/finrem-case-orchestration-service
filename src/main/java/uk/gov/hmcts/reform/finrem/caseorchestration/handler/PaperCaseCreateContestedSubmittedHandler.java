package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignPartiesAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CreateCaseService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;

@Slf4j
@Service
public class PaperCaseCreateContestedSubmittedHandler extends FinremCallbackHandler {

    private final CreateCaseService createCaseService;

    private final AssignPartiesAccessService assignPartiesAccessService;

    private final RetryExecutor retryExecutor;

    public PaperCaseCreateContestedSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                    CreateCaseService createCaseService,
                                                    AssignPartiesAccessService assignPartiesAccessService,
                                                    RetryExecutor retryExecutor) {
        super(finremCaseDetailsMapper);
        this.createCaseService = createCaseService;
        this.assignPartiesAccessService = assignPartiesAccessService;
        this.retryExecutor = retryExecutor;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.SUBMITTED.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.NEW_PAPER_CASE.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.submitted(callbackRequest));

        String setSupplementaryDataError = setSupplementaryData(callbackRequest, userAuthorisation);
        String assignApplicantSolicitorError = grantApplicantSolicitor(callbackRequest);

        boolean isHavingErrors = !StringUtils.isAllBlank(setSupplementaryDataError, assignApplicantSolicitorError);

        if (isHavingErrors) {
            return submittedResponse(
                toConfirmationHeader("Paper Case Created with Errors"),
                toConfirmationBody(setSupplementaryDataError, assignApplicantSolicitorError));
        } else {
            return submittedResponse();
        }
    }

    private String setSupplementaryData(FinremCallbackRequest request, String userAuthorisation) {
        try {
            retryExecutor.runWithRetry(() -> createCaseService.setSupplementaryData(request, userAuthorisation),
                "setting supplementary data", request.getCaseDetails().getCaseIdAsString());
            return null;
        } catch (Exception ex) {
            return "There was a problem setting supplementary data.";
        }
    }

    private String grantApplicantSolicitor(FinremCallbackRequest request) {
        String appSolEmail = request.getCaseDetails().getData().getAppSolicitorEmailIfRepresented();
        if (StringUtils.isBlank(appSolEmail)) {
            return null;
        }
        try {
            retryExecutor.runWithRetry(() -> assignPartiesAccessService.grantApplicantSolicitor(request.getCaseDetails().getData()),
                "granting applicant solicitor", request.getCaseDetails().getCaseIdAsString()
            );
            return null;
        } catch (Exception ex) {
            return "There was a problem granting access to applicant solicitor: %s".formatted(appSolEmail);
        }
    }
}
