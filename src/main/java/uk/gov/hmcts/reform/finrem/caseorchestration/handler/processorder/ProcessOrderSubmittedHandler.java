package uk.gov.hmcts.reform.finrem.caseorchestration.handler.processorder;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
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

import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.DIRECTION_UPLOAD_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@Slf4j
@Service
public class ProcessOrderSubmittedHandler extends FinremCallbackHandler {

    private final HearingDocumentService hearingDocumentService;
    private final AdditionalHearingDocumentService additionalHearingDocumentService;

    public ProcessOrderSubmittedHandler(FinremCaseDetailsMapper finremCaseDetailsMapper, HearingDocumentService hearingDocumentService,
                                        AdditionalHearingDocumentService additionalHearingDocumentService) {
        super(finremCaseDetailsMapper);
        this.hearingDocumentService = hearingDocumentService;
        this.additionalHearingDocumentService = additionalHearingDocumentService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return SUBMITTED.equals(callbackType) && CONTESTED.equals(caseType)
            && (DIRECTION_UPLOAD_ORDER.equals(eventType) || EventType.PROCESS_ORDER.equals(eventType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {

        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("Handling contested event {} submit callback for case id: {}", callbackRequest.getEventType(), caseDetails.getId());
        FinremCaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();

        if (EventType.DIRECTION_UPLOAD_ORDER.equals(callbackRequest.getEventType())) {
            if (CollectionUtils.isNotEmpty(caseDetails.getData().getDirectionDetailsCollection())) {
                if (caseDetails.getData().getDirectionDetailsCollection().stream()
                    .anyMatch(dd -> dd.getValue().getIsAnotherHearingYN().equals(YesOrNo.YES))) {

                    if (caseDetailsBefore != null && caseDetailsBefore.getData() != null
                        && caseDetailsBefore.getData().getListForHearingWrapper().getFormC() != null) {
                        log.info("Sending Additional Hearing Document to bulk print for Contested Case ID: {}", caseDetails.getId());
                        additionalHearingDocumentService.sendAdditionalHearingDocuments(userAuthorisation, caseDetails);
                        log.info("Sent Additional Hearing Document to bulk print for Contested Case ID: {}", caseDetails.getId());
                    } else {
                        log.info("Sending Forms A, C, G to bulk print for Contested Case ID: {}", caseDetails.getId());
                        hearingDocumentService.sendInitialHearingCorrespondence(caseDetails, userAuthorisation);
                        log.info("sent Forms A, C, G to bulk print for Contested Case ID: {}", caseDetails.getId());
                    }
                }
            }
        } else if (EventType.PROCESS_ORDER.equals(callbackRequest.getEventType())) {
            // MH Process Order notifications
            log.info("Handling process order notifications for contested case id: {}", caseDetails.getId());
        }


        return GenericAboutToStartOrSubmitCallbackResponse
            .<FinremCaseData>builder()
            .data(caseDetails.getData())
            .build();
    }
}
