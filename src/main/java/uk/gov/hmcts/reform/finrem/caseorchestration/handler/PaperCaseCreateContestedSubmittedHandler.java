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

@Slf4j
@Service
public class PaperCaseCreateContestedSubmittedHandler extends FinremCallbackHandler {

    private final CreateCaseService createCaseService;

    private final AssignPartiesAccessService assignPartiesAccessService;

    public PaperCaseCreateContestedSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                    CreateCaseService createCaseService,
                                                    AssignPartiesAccessService assignPartiesAccessService) {
        super(finremCaseDetailsMapper);
        this.createCaseService = createCaseService;
        this.assignPartiesAccessService = assignPartiesAccessService;
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

        return submittedResponse(toConfirmationHeader(!StringUtils.isAllBlank(setSupplementaryDataError,
                assignApplicantSolicitorError)),
            toConfirmationBody(setSupplementaryDataError, assignApplicantSolicitorError));
    }

    private String toConfirmationHeader(boolean withError) {
        return "# Paper Case Created%s".formatted(withError ? " with error" : "");
    }

    private String toConfirmationBody(String setSupplementaryDataError,
                                      String assignApplicantSolicitorError) {

        StringBuilder body = new StringBuilder("<ul>");

        if (setSupplementaryDataError != null && !setSupplementaryDataError.isBlank()) {
            body.append("<li>").append(setSupplementaryDataError).append("</li>");
        }

        if (assignApplicantSolicitorError != null && !assignApplicantSolicitorError.isBlank()) {
            body.append("<li>").append(assignApplicantSolicitorError).append("</li>");
        }

        body.append("</ul>");

        return body.toString();
    }

    private String setSupplementaryData(FinremCallbackRequest request, String userAuthorisation) {
        try {
            executeWithRetry(log,
                () -> createCaseService.setSupplementaryData(request, userAuthorisation),
                request.getCaseDetails().getCaseIdAsString(),
                "setting supplementary data",
                3
            );
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
            executeWithRetry(log,
                () -> assignPartiesAccessService.grantApplicantSolicitor(request.getCaseDetails().getData()),
                request.getCaseDetails().getCaseIdAsString(),
                "granting respondent solicitor",
                3
            );
            return null;
        } catch (Exception ex) {
            return "There was a problem granting access to application solicitor %s"
                .formatted(appSolEmail);
        }
    }
}
