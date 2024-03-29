package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentedHearingDataElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentedHearingDataWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintDocumentService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class HearingConsentMidHandler extends FinremCallbackHandler {

    private final BulkPrintDocumentService service;

    public HearingConsentMidHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                             BulkPrintDocumentService service) {
        super(finremCaseDetailsMapper);
        this.service = service;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.MID_EVENT.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && (EventType.LIST_FOR_HEARING_CONSENTED.equals(eventType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        String caseId = String.valueOf(caseDetails.getId());
        log.info("Invoking contested event {} mid callback for Case ID: {}", EventType.LIST_FOR_HEARING_CONSENTED, caseId);

        FinremCaseData caseData = caseDetails.getData();
        List<String> errors = new ArrayList<>();

        List<ConsentedHearingDataWrapper> listForHearings = caseData.getListForHearings();

        if (listForHearings != null && !listForHearings.isEmpty()) {
            FinremCaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();
            FinremCaseData caseDataBefore = caseDetailsBefore.getData();
            List<ConsentedHearingDataWrapper> listForHearingsBefore = caseDataBefore.getListForHearings();
            if (listForHearingsBefore != null && !listForHearingsBefore.isEmpty()) {
                listForHearings.removeAll(listForHearingsBefore);
            }
            listForHearings.forEach(hearing -> {
                ConsentedHearingDataElement hearingValue = hearing.getValue();
                if (hearingValue.getPromptForAnyDocument().equals("Yes")) {
                    service.validateEncryptionOnUploadedDocument(hearingValue.getUploadAdditionalDocument(),
                        caseId, errors, userAuthorisation);
                }
            });
        }
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseData).errors(errors).build();
    }
}
