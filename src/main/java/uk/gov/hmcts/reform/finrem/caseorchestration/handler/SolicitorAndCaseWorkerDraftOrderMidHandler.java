package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftDirectionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintDocumentService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class SolicitorAndCaseWorkerDraftOrderMidHandler extends FinremCallbackHandler {

    private final BulkPrintDocumentService service;

    public SolicitorAndCaseWorkerDraftOrderMidHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                      BulkPrintDocumentService service) {
        super(finremCaseDetailsMapper);
        this.service = service;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.MID_EVENT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.SOLICITOR_CW_DRAFT_ORDER.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        String caseId = String.valueOf(caseDetails.getId());
        log.info("Invoking contested event {} mid callback for Case ID: {}",
            EventType.SOLICITOR_CW_DRAFT_ORDER, caseId);
        FinremCaseData caseData = caseDetails.getData();

        DraftDirectionWrapper draftDirectionWrapper = caseData.getDraftDirectionWrapper();
        List<DraftDirectionOrderCollection> draftDirectionOrderCollection = draftDirectionWrapper.getDraftDirectionOrderCollection();
        FinremCaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();
        FinremCaseData caseDataBefore = caseDetailsBefore.getData();
        DraftDirectionWrapper draftDirectionWrapperBefore = caseDataBefore.getDraftDirectionWrapper();
        if (draftDirectionWrapperBefore != null) {
            List<DraftDirectionOrderCollection> draftDirectionsBefore = draftDirectionWrapperBefore.getDraftDirectionOrderCollection();
            if (draftDirectionsBefore != null && !draftDirectionsBefore.isEmpty()) {
                draftDirectionOrderCollection.removeAll(draftDirectionsBefore);
            }
        }

        List<String> errors = new ArrayList<>();

        draftDirectionOrderCollection.forEach(doc ->
            service.validateEncryptionOnUploadedDocument(doc.getValue().getUploadDraftDocument(),
                caseId, errors, userAuthorisation)
        );

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseData).errors(errors).build();
    }
}
