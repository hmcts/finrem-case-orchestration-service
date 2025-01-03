package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.CourtDetailsParseException;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.HasApprovable;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.agreed.AgreedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrderDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.PsaDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AdditionalHearingDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.draftorders.HasApprovableCollectionReader;

import java.util.ArrayList;
import java.util.List;

import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.DIRECTION_UPLOAD_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus.APPROVED_BY_JUDGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus.PROCESSED;

@Slf4j
@Service
public class DirectionUploadOrderAboutToSubmitHandler extends FinremCallbackHandler {

    private final AdditionalHearingDocumentService additionalHearingDocumentService;

    private final HasApprovableCollectionReader hasApprovableCollectionReader;

    public DirectionUploadOrderAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                    AdditionalHearingDocumentService additionalHearingDocumentService,
                                                    HasApprovableCollectionReader hasApprovableCollectionReader) {
        super(finremCaseDetailsMapper);
        this.additionalHearingDocumentService = additionalHearingDocumentService;
        this.hasApprovableCollectionReader = hasApprovableCollectionReader;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return ABOUT_TO_SUBMIT.equals(callbackType) && CONTESTED.equals(caseType) && DIRECTION_UPLOAD_ORDER.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        String caseId = String.valueOf(caseDetails.getId());
        log.info("Invoking contested event {} mid callback for Case ID: {}", callbackRequest.getEventType(), caseId);
        FinremCaseData caseData = caseDetails.getData();

        // handleNewDocument must be handled before storeAdditionalHearingDocuments in order to stamp the newly uploaded document.
        handleNewDocumentInUnprocessedApprovedDocuments(caseData);

        List<String> errors = new ArrayList<>();
        log.info("Storing Additional Hearing Document for Case ID: {}", caseId);
        try {
            storeAdditionalHearingDocuments(callbackRequest.getCaseDetails(), userAuthorisation);
        } catch (CourtDetailsParseException | JsonProcessingException e) {
            log.error("Case ID: {} {}", callbackRequest.getCaseDetails().getId(), e.getMessage());
            return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
                .data(caseData).errors(List.of("There was an unexpected error")).build();
        }

        handleDraftOrderDocuments(caseData);
        handlePsaDocuments(caseData);
        handleAgreedDraftOrdersCollection(caseData);
        clearTemporaryFields(caseData);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData).errors(errors).build();
    }

    private void storeAdditionalHearingDocuments(FinremCaseDetails caseDetails, String userAuthorisation) throws JsonProcessingException {
        additionalHearingDocumentService.createAndStoreAdditionalHearingDocuments(caseDetails, userAuthorisation);
    }

    private void handleNewDocumentInUnprocessedApprovedDocuments(FinremCaseData caseData) {
        nullSafeUnprocessedApprovedDocuments(caseData).forEach(unprocessedApprovedDocuments -> {
            if (isNewDocument(unprocessedApprovedDocuments)) {
                insertNewDocumentToUploadHearingOrder(caseData, unprocessedApprovedDocuments);
            }
        });
    }

    private void handleDraftOrderDocuments(FinremCaseData caseData) {
        List<DraftOrderDocReviewCollection> collector = new ArrayList<>();
        hasApprovableCollectionReader.filterAndCollectDraftOrderDocs(caseData.getDraftOrdersWrapper().getDraftOrdersReviewCollection(),
            collector, APPROVED_BY_JUDGE::equals);

        nullSafeUnprocessedApprovedDocuments(caseData).stream().filter(not(this::isNewDocument))
            .forEach(unprocessedApprovedOrder ->
                collector.stream().filter(psa -> doesDocumentMatch(psa, unprocessedApprovedOrder)).forEach(toBeUpdated -> {
                    toBeUpdated.getValue().setOrderStatus(PROCESSED);
                    toBeUpdated.getValue().setDraftOrderDocument(unprocessedApprovedOrder.getValue().getUploadDraftDocument());
                }));
    }

    private void handlePsaDocuments(FinremCaseData caseData) {
        List<PsaDocReviewCollection> psaCollector = new ArrayList<>();
        hasApprovableCollectionReader.filterAndCollectPsaDocs(caseData.getDraftOrdersWrapper().getDraftOrdersReviewCollection(),
            psaCollector, APPROVED_BY_JUDGE::equals);

        nullSafeUnprocessedApprovedDocuments(caseData).stream().filter(not(this::isNewDocument))
            .forEach(unprocessedApprovedOrder ->
                psaCollector.stream().filter(psa -> doesDocumentMatch(psa, unprocessedApprovedOrder)).forEach(toBeUpdated -> {
                    toBeUpdated.getValue().setOrderStatus(PROCESSED);
                    toBeUpdated.getValue().setPsaDocument(unprocessedApprovedOrder.getValue().getUploadDraftDocument());
                }));
    }

    private void handleAgreedDraftOrdersCollection(FinremCaseData caseData) {
        List<AgreedDraftOrderCollection> agreedOrderCollector = new ArrayList<>();
        hasApprovableCollectionReader.collectAgreedDraftOrders(caseData.getDraftOrdersWrapper().getAgreedDraftOrderCollection(),
            agreedOrderCollector, APPROVED_BY_JUDGE::equals);

        nullSafeUnprocessedApprovedDocuments(caseData).stream().filter(not(this::isNewDocument))
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
                    }));
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

    private List<DirectionOrderCollection> nullSafeUnprocessedApprovedDocuments(FinremCaseData caseData) {
        return ofNullable(caseData.getDraftOrdersWrapper().getUnprocessedApprovedDocuments()).orElse(List.of());
    }
}
