package uk.gov.hmcts.reform.finrem.caseorchestration.handler.processorder;

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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AdditionalHearingDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.managehearing.ManageHearingsCorresponder;

import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@Slf4j
@Service
public class ProcessOrderSubmittedHandler extends FinremCallbackHandler {

    private final ManageHearingsCorresponder manageHearingsCorresponder;

    public ProcessOrderSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                        HearingDocumentService hearingDocumentService,
                                        AdditionalHearingDocumentService additionalHearingDocumentService,
                                        ManageHearingsCorresponder manageHearingsCorresponder) {
        super(finremCaseDetailsMapper);
        this.manageHearingsCorresponder = manageHearingsCorresponder;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return SUBMITTED.equals(callbackType) && CONTESTED.equals(caseType)
            && EventType.PROCESS_ORDER.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.submitted(callbackRequest));
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData caseData = caseDetails.getData();

        if (YesOrNo.YES.equals(caseData.getManageHearingsWrapper().getIsAddHearingChosen())) {
            log.info("Handling process order notifications for contested case id: {}", caseDetails.getId());
            manageHearingsCorresponder.sendHearingCorrespondence(callbackRequest, userAuthorisation);
        }

        return GenericAboutToStartOrSubmitCallbackResponse
            .<FinremCaseData>builder()
            .data(caseDetails.getData())
            .build();
    }
}
