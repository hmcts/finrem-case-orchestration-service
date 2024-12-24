package uk.gov.hmcts.reform.finrem.caseorchestration.handler.processorders;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.DirectionUploadOrderAboutToSubmitHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.agreed.AgreedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrderDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.PsaDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AdditionalHearingDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.draftorders.HasApprovableCollectionReader;

import java.util.ArrayList;
import java.util.List;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.PROCESS_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus.APPROVED_BY_JUDGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus.PROCESSED;

@Slf4j
@Service
public class ProcessOrdersAboutToSubmitHandler extends DirectionUploadOrderAboutToSubmitHandler {

    private final HasApprovableCollectionReader hasApprovableCollectionReader;

    public ProcessOrdersAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                             AdditionalHearingDocumentService service,
                                             HasApprovableCollectionReader hasApprovableCollectionReader) {
        super(finremCaseDetailsMapper, service);
        this.hasApprovableCollectionReader = hasApprovableCollectionReader;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return ABOUT_TO_SUBMIT.equals(callbackType) && CONTESTED.equals(caseType) && PROCESS_ORDER.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> resp = super.handle(callbackRequest, userAuthorisation);
        FinremCaseData caseData = resp.getData();

        // to clear the temp fields
        markOrderStatusToProcessed(caseData);
        clearTemporaryFields(caseData);

        return resp;
    }

    private boolean doesDocumentMatch(CaseDocument doc1, CaseDocument doc2) {
        return ofNullable(doc1).orElse(CaseDocument.builder().documentUrl("").build()).getDocumentUrl()
            .equals(doc2.getDocumentUrl());
    }

    private void markOrderStatusToProcessed(FinremCaseData caseData) {
        List<DraftOrderDocReviewCollection> collector = new ArrayList<>();
        List<PsaDocReviewCollection> psaCollector = new ArrayList<>();
        hasApprovableCollectionReader.filterAndCollectDraftOrderDocs(caseData.getDraftOrdersWrapper().getDraftOrdersReviewCollection(),
            collector, APPROVED_BY_JUDGE::equals);
        hasApprovableCollectionReader.filterAndCollectPsaDocs(caseData.getDraftOrdersWrapper().getDraftOrdersReviewCollection(),
            psaCollector, APPROVED_BY_JUDGE::equals);
        List<AgreedDraftOrderCollection> agreedOrderCollector = new ArrayList<>();
        hasApprovableCollectionReader.collectAgreedDraftOrders(caseData.getDraftOrdersWrapper().getAgreedDraftOrderCollection(),
            agreedOrderCollector, APPROVED_BY_JUDGE::equals);

        caseData.getDraftOrdersWrapper().getUnprocessedApprovedDocuments().forEach(d -> {
            // mark draft order
            collector.stream().filter(draftOrder -> doesDocumentMatch(draftOrder.getValue().getTargetDocument(),
                d.getValue().getUploadDraftDocument())).forEach(draftOrder -> draftOrder.getValue().setOrderStatus(PROCESSED));
            // mark PSA
            psaCollector.stream().filter(draftOrder -> doesDocumentMatch(draftOrder.getValue().getTargetDocument(),
                d.getValue().getUploadDraftDocument())).forEach(draftOrder -> draftOrder.getValue().setOrderStatus(PROCESSED));
            // mark AgreedDraftOrder
            agreedOrderCollector.stream().filter(draftOrder -> doesDocumentMatch(draftOrder.getValue().getTargetDocument(),
                d.getValue().getUploadDraftDocument())).forEach(draftOrder -> draftOrder.getValue().setOrderStatus(PROCESSED));
            }
        );
    }

    private void clearTemporaryFields(FinremCaseData caseData) {
        clearUnprocessedApprovedDocuments(caseData.getDraftOrdersWrapper());
        clearMetaDataFields(caseData);
    }

    private void clearUnprocessedApprovedDocuments(DraftOrdersWrapper draftOrdersWrapper) {
        draftOrdersWrapper.setUnprocessedApprovedDocuments(List.of());
    }

    private void clearMetaDataFields(FinremCaseData caseData) {
        caseData.getDraftOrdersWrapper().setIsLegacyApprovedOrderPresent(null);
        caseData.getDraftOrdersWrapper().setIsUnprocessedApprovedDocumentPresent(null);
    }
}
