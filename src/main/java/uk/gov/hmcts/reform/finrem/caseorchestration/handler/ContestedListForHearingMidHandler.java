package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDataContested;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintDocumentService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ContestedListForHearingMidHandler extends FinremCallbackHandler<FinremCaseDataContested> {

    private final BulkPrintDocumentService service;

    public ContestedListForHearingMidHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                             BulkPrintDocumentService service) {
        super(finremCaseDetailsMapper);
        this.service = service;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.MID_EVENT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && (EventType.LIST_FOR_HEARING.equals(eventType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseDataContested> handle(FinremCallbackRequest<FinremCaseDataContested> callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails<FinremCaseDataContested> caseDetails = callbackRequest.getCaseDetails();
        String caseId = String.valueOf(caseDetails.getId());
        log.info("Invoking contested event {} mid callback for case id: {}",
            EventType.LIST_FOR_HEARING, caseId);
        FinremCaseDataContested caseData = caseDetails.getData();
        List<String> errors = new ArrayList<>();
        if (caseData.getAdditionalHearingDocumentsOption().isYes()) {
            CaseDocument additionalListOfHearingDocument = caseData.getAdditionalListOfHearingDocuments();
            service.validateEncryptionOnUploadedDocument(additionalListOfHearingDocument,
                caseId, errors, userAuthorisation);
        }
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseDataContested>builder()
            .data(caseData).errors(errors).build();
    }
}