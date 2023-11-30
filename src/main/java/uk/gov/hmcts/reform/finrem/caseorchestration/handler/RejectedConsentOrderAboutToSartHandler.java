package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrderRefusalHolder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.RefusalOrderDocumentService;

import java.time.LocalDate;
import java.util.Optional;

@Slf4j
@Service
public class RejectedConsentOrderAboutToSartHandler extends FinremCallbackHandler {

    private final RefusalOrderDocumentService service;
    private final IdamService idamService;

    @Autowired
    public RejectedConsentOrderAboutToSartHandler(FinremCaseDetailsMapper mapper,
                                                  RefusalOrderDocumentService service,
                                                  IdamService idamService) {
        super(mapper);
        this.service = service;
        this.idamService = idamService;
    }


    @Override
    public boolean canHandle(final CallbackType callbackType, final CaseType caseType,
                             final EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && EventType.REJECT_ORDER.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("Received request for '{}' event '{}' for Case ID: {}",CallbackType.ABOUT_TO_START,
            EventType.REJECT_ORDER, caseDetails.getId());
        FinremCaseData caseData = caseDetails.getData();
        OrderRefusalHolder orderRefusal = Optional.ofNullable(caseData.getOrderRefusalCollectionNew())
            .orElse(new OrderRefusalHolder());
        orderRefusal.setOrderRefusalDate(LocalDate.now());
        orderRefusal.setOrderRefusalJudgeName(idamService.getIdamFullName(userAuthorisation));
        caseData.setOrderRefusalCollectionNew(orderRefusal);
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseData)
            .build();
    }
}
