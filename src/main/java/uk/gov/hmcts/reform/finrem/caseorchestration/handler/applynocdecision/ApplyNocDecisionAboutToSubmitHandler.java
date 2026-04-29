package uk.gov.hmcts.reform.finrem.caseorchestration.handler.applynocdecision;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandlerLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremAboutToSubmitCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdate;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdateHistoryCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenerateCoverSheetService;

import java.util.List;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.APPLY_NOC_DECISION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.NocUtils.isNocRequestAccepted;

@Slf4j
@Service
public class ApplyNocDecisionAboutToSubmitHandler extends FinremAboutToSubmitCallbackHandler {

    private final GenerateCoverSheetService generateCoverSheetService;

    public ApplyNocDecisionAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                GenerateCoverSheetService generateCoverSheetService) {
        super(finremCaseDetailsMapper);
        this.generateCoverSheetService = generateCoverSheetService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return ABOUT_TO_SUBMIT.equals(callbackType)
            && APPLY_NOC_DECISION.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.aboutToSubmit(callbackRequest));

        final FinremCaseDetails finremCaseDetails = callbackRequest.getCaseDetails();
        final FinremCaseData finremCaseData = finremCaseDetails.getData();

        if (isNocRequestAccepted(finremCaseData)) {
            if (isApplicantSolicitorNocRequested(finremCaseData)) {
                generateCoverSheetService.generateAndSetApplicantCoverSheet(finremCaseDetails, userAuthorisation);
            } else if (isRespondentSolicitorNocRequested(finremCaseData)) {
                generateCoverSheetService.generateAndSetRespondentCoverSheet(finremCaseDetails, userAuthorisation);
            }
        }
        
        return response(finremCaseData);
    }

    private String getLastUpdatedParty(FinremCaseData caseData) {
        return ofNullable(caseData.getRepresentationUpdateHistory())
            .map(List::getLast)
            .map(RepresentationUpdateHistoryCollection::getValue)
            .map(RepresentationUpdate::getParty)
            .orElse("");
    }

    private boolean isApplicantSolicitorNocRequested(FinremCaseData caseData) {
        return CCDConfigConstant.APPLICANT.equals(getLastUpdatedParty(caseData));
    }

    private boolean isRespondentSolicitorNocRequested(FinremCaseData caseData) {
        return CCDConfigConstant.RESPONDENT.equals(getLastUpdatedParty(caseData));
    }
}
