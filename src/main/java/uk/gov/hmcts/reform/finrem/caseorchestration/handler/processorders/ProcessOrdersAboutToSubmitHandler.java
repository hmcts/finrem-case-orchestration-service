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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.HasApprovable;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.agreed.AgreedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrderDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.PsaDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AdditionalHearingDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.draftorders.HasApprovableCollectionReader;

import java.util.ArrayList;
import java.util.List;

import static java.util.function.Predicate.not;
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
        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();
        // handleNewDocument is required to be done before calling super method which stamps the uploading document.
        handleNewDocument(caseData);
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> resp = super.handle(callbackRequest, userAuthorisation);
        caseData = resp.getData();

        handleDraftOrderDocuments(caseData);
        handlePsaDocuments(caseData);
        handleAgreedDraftOrdersCollection(caseData);
        clearTemporaryFields(caseData);

        return resp;
    }

    void handleNewDocument(FinremCaseData caseData) {
        caseData.getDraftOrdersWrapper().getUnprocessedApprovedDocuments().forEach(unprocessedApprovedOrder -> {
            if (isNewDocument(unprocessedApprovedOrder)) {
                insertNewDocumentToUploadHearingOrder(caseData, unprocessedApprovedOrder);
            }
        });
    }

    private void handleDraftOrderDocuments(FinremCaseData caseData) {
        List<DraftOrderDocReviewCollection> collector = new ArrayList<>();
        hasApprovableCollectionReader.filterAndCollectDraftOrderDocs(caseData.getDraftOrdersWrapper().getDraftOrdersReviewCollection(),
            collector, APPROVED_BY_JUDGE::equals);

        caseData.getDraftOrdersWrapper().getUnprocessedApprovedDocuments().stream().filter(not(this::isNewDocument))
            .forEach(unprocessedApprovedOrder ->
                collector.stream().filter(psa -> doesDocumentMatch(psa, unprocessedApprovedOrder)).forEach(toBeUpdated -> {
                    toBeUpdated.getValue().setOrderStatus(PROCESSED);
                    toBeUpdated.getValue().setDraftOrderDocument(unprocessedApprovedOrder.getValue().getUploadDraftDocument());
                })
        );
    }

    private void handlePsaDocuments(FinremCaseData caseData) {
        List<PsaDocReviewCollection> psaCollector = new ArrayList<>();
        hasApprovableCollectionReader.filterAndCollectPsaDocs(caseData.getDraftOrdersWrapper().getDraftOrdersReviewCollection(),
            psaCollector, APPROVED_BY_JUDGE::equals);

        caseData.getDraftOrdersWrapper().getUnprocessedApprovedDocuments().stream().filter(not(this::isNewDocument))
            .forEach(unprocessedApprovedOrder ->
                psaCollector.stream().filter(psa -> doesDocumentMatch(psa, unprocessedApprovedOrder)).forEach(toBeUpdated -> {
                    toBeUpdated.getValue().setOrderStatus(PROCESSED);
                    toBeUpdated.getValue().setPsaDocument(unprocessedApprovedOrder.getValue().getUploadDraftDocument());
                })
        );
    }

    private void handleAgreedDraftOrdersCollection(FinremCaseData caseData) {
        List<AgreedDraftOrderCollection> agreedOrderCollector = new ArrayList<>();
        hasApprovableCollectionReader.collectAgreedDraftOrders(caseData.getDraftOrdersWrapper().getAgreedDraftOrderCollection(),
            agreedOrderCollector, APPROVED_BY_JUDGE::equals);

        caseData.getDraftOrdersWrapper().getUnprocessedApprovedDocuments().stream().filter(not(this::isNewDocument))
            .forEach(unprocessedApprovedOrder ->
                agreedOrderCollector.stream().filter(agreedDraftOrder -> doesDocumentMatch(agreedDraftOrder, unprocessedApprovedOrder))
                    .forEach(toBeUpdated -> {
                        toBeUpdated.getValue().setOrderStatus(PROCESSED);
                        // replace the document by the new uploaded approved document
                        if (toBeUpdated.getValue().getPensionSharingAnnex() != null) {
                            toBeUpdated.getValue().setPensionSharingAnnex(unprocessedApprovedOrder.getValue().getUploadDraftDocument());
                        } else if (toBeUpdated.getValue().getDraftOrder() != null) {
                            toBeUpdated.getValue().setDraftOrder(unprocessedApprovedOrder.getValue().getUploadDraftDocument());
                        }
                    })
        );
    }

    private void insertNewDocumentToUploadHearingOrder(FinremCaseData caseData, DirectionOrderCollection unprocessedApprovedOrder) {
        if (caseData.getUploadHearingOrder() == null) {
            caseData.setUploadHearingOrder(new ArrayList<>());
        }
        caseData.getUploadHearingOrder().add(DirectionOrderCollection.builder()
            .value(DirectionOrder.builder()
                .uploadDraftDocument(unprocessedApprovedOrder.getValue().getUploadDraftDocument())
                .build())
            .build());
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

    private boolean doesDocumentMatch(HasApprovable hasApprovable, DirectionOrderCollection unprocessedApprovedOrder) {
        return doesDocumentMatch(hasApprovable.getValue().getTargetDocument(), unprocessedApprovedOrder.getValue().getOriginalDocument());
    }

    private boolean doesDocumentMatch(CaseDocument doc1, CaseDocument doc2) {
        return doc1.getDocumentUrl().equals(doc2.getDocumentUrl());
    }

    private boolean isNewDocument(DirectionOrderCollection unprocessedApprovedOrder) {
        return unprocessedApprovedOrder.getValue().getOriginalDocument() == null;
    }
}
