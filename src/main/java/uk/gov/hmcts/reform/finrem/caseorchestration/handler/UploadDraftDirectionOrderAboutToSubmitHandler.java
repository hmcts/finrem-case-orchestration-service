package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnStartDefaultValueService;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UploadDraftDirectionOrderAboutToSubmitHandler extends FinremCallbackHandler {

    private final GenericDocumentService service;

    public UploadDraftDirectionOrderAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                         GenericDocumentService service) {
        super(finremCaseDetailsMapper);
        this.service = service;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && (EventType.SOLICITOR_DRAFT_DIRECTION_ORDER.equals(eventType));
    }


    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info("Invoking contested event {} about to start callback for case id: {}",
            EventType.SOLICITOR_DRAFT_DIRECTION_ORDER, callbackRequest.getCaseDetails().getId());
        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();
        List<DraftDirectionOrderCollection> draftDirectionOrderCollection = caseData
            .getDraftDirectionWrapper().getDraftDirectionOrderCollection();

        List<DraftDirectionOrderCollection> orderList = draftDirectionOrderCollection.stream().map(order -> covertToPdf(order.getValue(), userAuthorisation)).toList();
        caseData.getDraftDirectionWrapper().setDraftDirectionOrderCollection(orderList);
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseData).build();
    }

    private DraftDirectionOrderCollection covertToPdf(DraftDirectionOrder value, String userAuthorisation) {

        return DraftDirectionOrderCollection.builder()
            .value(DraftDirectionOrder.builder()
                .purposeOfDocument(value.getPurposeOfDocument())
                .uploadDraftDocument(service.convertDocumentIfNotPdfAlready(value.getUploadDraftDocument(), userAuthorisation))
                .build())
            .build();
    }
}