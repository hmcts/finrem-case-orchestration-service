package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AdditionalHearingDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingDocumentService;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ListForHearingContestedSubmittedHandler implements CallbackHandler<Map<String, Object>> {

    private final HearingDocumentService hearingDocumentService;
    private final AdditionalHearingDocumentService additionalHearingDocumentService;

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.SUBMITTED.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && (EventType.LIST_FOR_HEARING.equals(eventType) || EventType.UPLOAD_ORDER.equals(eventType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> handle(CallbackRequest callbackRequest,
                                                                                   String userAuthorisation) {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("Handling contested event {} submit callback for case id: {}",
            EventType.valueOf(callbackRequest.getEventId()), caseDetails.getId());

        CaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();
        if (caseDetailsBefore != null && hearingDocumentService.alreadyHadFirstHearing(caseDetailsBefore)) {
            log.info("Sending Additional Hearing Document to bulk print for Contested Case ID: {}", caseDetails.getId());
            additionalHearingDocumentService.sendAdditionalHearingDocuments(userAuthorisation, caseDetails);
            log.info("Sent Additional Hearing Document to bulk print for Contested Case ID: {}", caseDetails.getId());
        } else {
            log.info("Sending Forms A, C, G to bulk print for Contested Case ID: {}", caseDetails.getId());
            hearingDocumentService.sendInitialHearingCorrespondence(caseDetails, userAuthorisation);
            log.info("sent Forms A, C, G to bulk print for Contested Case ID: {}", caseDetails.getId());
        }

        return GenericAboutToStartOrSubmitCallbackResponse
            .<Map<String, Object>>builder()
            .data(caseDetails.getData())
            .build();
    }
}
