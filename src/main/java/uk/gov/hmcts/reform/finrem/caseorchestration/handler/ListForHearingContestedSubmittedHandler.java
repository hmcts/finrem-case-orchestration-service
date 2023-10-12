package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AdditionalHearingDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.SelectablePartiesCorrespondenceService;

@Slf4j
@Service
public class ListForHearingContestedSubmittedHandler extends FinremCallbackHandler {

    private final HearingDocumentService hearingDocumentService;
    private final AdditionalHearingDocumentService additionalHearingDocumentService;

    private final SelectablePartiesCorrespondenceService selectablePartiesCorrespondenceService;

    public ListForHearingContestedSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper, HearingDocumentService hearingDocumentService,
                                                   AdditionalHearingDocumentService additionalHearingDocumentService,
                                                   SelectablePartiesCorrespondenceService selectablePartiesCorrespondenceService) {
        super(finremCaseDetailsMapper);
        this.hearingDocumentService = hearingDocumentService;
        this.additionalHearingDocumentService = additionalHearingDocumentService;
        this.selectablePartiesCorrespondenceService = selectablePartiesCorrespondenceService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.SUBMITTED.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && (EventType.LIST_FOR_HEARING.equals(eventType) || EventType.UPLOAD_ORDER.equals(eventType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {

        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("Handling contested event {} submit callback for case id: {}",
            callbackRequest.getEventType(), caseDetails.getId());
        FinremCaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();

        selectablePartiesCorrespondenceService.setPartiesToReceiveCorrespondence(caseDetails.getData());

        if (caseDetailsBefore != null && caseDetailsBefore.getData().getFormC() != null) {
            log.info("Sending Additional Hearing Document to bulk print for Contested Case ID: {}", caseDetails.getId());
            additionalHearingDocumentService.sendAdditionalHearingDocuments(userAuthorisation, caseDetails);
            log.info("Sent Additional Hearing Document to bulk print for Contested Case ID: {}", caseDetails.getId());
        } else {
            log.info("Sending Forms A, C, G to bulk print for Contested Case ID: {}", caseDetails.getId());
            hearingDocumentService.sendInitialHearingCorrespondence(caseDetails, userAuthorisation);
            log.info("sent Forms A, C, G to bulk print for Contested Case ID: {}", caseDetails.getId());
        }

        return GenericAboutToStartOrSubmitCallbackResponse
            .<FinremCaseData>builder()
            .data(caseDetails.getData())
            .build();
    }
}
