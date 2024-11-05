package uk.gov.hmcts.reform.finrem.caseorchestration.handler.approvedraftorders;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.CaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.JudgeApproval;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.ReviewableDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.ReviewableDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.ReviewablePsa;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval.ReviewablePsaCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;

import java.util.List;

@Slf4j
@Service
public class ApproveDraftOrdersMidEventHandler extends FinremCallbackHandler {

    public ApproveDraftOrdersMidEventHandler(FinremCaseDetailsMapper finremCaseDetailsMapper) {
        super(finremCaseDetailsMapper);
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.MID_EVENT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.APPROVE_ORDERS.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        String caseId = String.valueOf(caseDetails.getId());
        log.info("Invoking contested {} mid event callback for Case ID: {}", callbackRequest.getEventType(), caseId);

        FinremCaseData finremCaseData = caseDetails.getData();
        DraftOrdersWrapper draftOrdersWrapper = finremCaseData.getDraftOrdersWrapper();

        // XXX
        draftOrdersWrapper.setJudgeApproval(JudgeApproval.builder()
            .reviewablePsaCollection(List.of(
                ReviewablePsaCollection.builder()
                    .value(ReviewablePsa.builder()
                        .document(
                            CaseDocument.builder().documentFilename("PSA.pdf")
                                .documentUrl("http://dm-store-aat.service.core-compute-aat.internal/documents/afd2faae-de15-4ab8-8d0c-d5d0fef41e62")
                                .build()
                        )
                        .build())
                    .build()
            ))
            .reviewableDraftOrderCollection(List.of(
                ReviewableDraftOrderCollection.builder()
                    .value(ReviewableDraftOrder.builder()
                        .document(
                            CaseDocument.builder().documentFilename("DO_A.pdf")
                                .documentUrl("http://dm-store-aat.service.core-compute-aat.internal/documents/afd2faae-de15-4ab8-8d0c-d5d0fef41e62")
                                .build()
                        )
                        .attachments(List.of(
                            CaseDocumentCollection.builder().value(
                                    CaseDocument.builder().documentFilename("Attachment-A.pdf")
                                        .documentUrl("http://dm-store-aat.service.core-compute-aat.internal/documents/afd2faae-de15-4ab8-8d0c-d5d0fef41e62")
                                        .build())
                                .build(),
                            CaseDocumentCollection.builder().value(
                                    CaseDocument.builder().documentFilename("Attachment-B.pdf")
                                        .documentUrl("http://dm-store-aat.service.core-compute-aat.internal/documents/afd2faae-de15-4ab8-8d0c-d5d0fef41e62")
                                        .build())
                                .build()
                        ))
                        .build())
                    .build(),
                ReviewableDraftOrderCollection.builder()
                    .value(ReviewableDraftOrder.builder()
                        .document(
                            CaseDocument.builder().documentFilename("PSA_A.pdf")
                                .documentUrl("http://dm-store-aat.service.core-compute-aat.internal/documents/afd2faae-de15-4ab8-8d0c-d5d0fef41e62")
                                .build()
                        )
                        .build())
                    .build()
            ))
            .build());

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(finremCaseData).build();
    }
}
