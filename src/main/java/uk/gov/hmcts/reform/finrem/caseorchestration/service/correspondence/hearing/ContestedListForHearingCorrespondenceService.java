package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hearing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AdditionalHearingDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.SelectablePartiesCorrespondenceService;

@Component
@Slf4j
public class ContestedListForHearingCorrespondenceService {

    private final HearingDocumentService hearingDocumentService;

    private final AdditionalHearingDocumentService additionalHearingDocumentService;

    private final SelectablePartiesCorrespondenceService selectablePartiesCorrespondenceService;

    public ContestedListForHearingCorrespondenceService(HearingDocumentService hearingDocumentService,
                                                        AdditionalHearingDocumentService additionalHearingDocumentService,
                                                        SelectablePartiesCorrespondenceService selectablePartiesCorrespondenceService) {
        this.hearingDocumentService = hearingDocumentService;
        this.additionalHearingDocumentService = additionalHearingDocumentService;
        this.selectablePartiesCorrespondenceService = selectablePartiesCorrespondenceService;
    }


    public void sendHearingCorrespondence(FinremCallbackRequest callbackRequest,
                                          String userAuthorisation) {

        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("Handling contested event {} submit callback for Case ID: {}",
            EventType.LIST_FOR_HEARING, caseDetails.getId());
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

    }
}
