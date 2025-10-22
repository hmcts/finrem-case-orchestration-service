package uk.gov.hmcts.reform.finrem.caseorchestration.handler.processorder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandlerLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.HasApprovable;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.agreed.AgreedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrderDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.PsaDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsAction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AdditionalHearingDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.StampType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.draftorders.HasApprovableCollectionReader;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings.ManageHearingActionService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static org.apache.commons.collections4.ListUtils.emptyIfNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus.APPROVED_BY_JUDGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus.PROCESSED;

@Slf4j
@Service
public class ProcessOrderAboutToSubmitHandler extends FinremCallbackHandler {

    private final AdditionalHearingDocumentService additionalHearingDocumentService;
    private final HasApprovableCollectionReader hasApprovableCollectionReader;
    private final DocumentHelper documentHelper;
    private final GenericDocumentService genericDocumentService;
    private final ManageHearingActionService manageHearingActionService;

    public ProcessOrderAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                            AdditionalHearingDocumentService additionalHearingDocumentService,
                                            HasApprovableCollectionReader hasApprovableCollectionReader,
                                            DocumentHelper documentHelper, GenericDocumentService genericDocumentService,
                                            ManageHearingActionService manageHearingActionService) {
        super(finremCaseDetailsMapper);
        this.additionalHearingDocumentService = additionalHearingDocumentService;
        this.hasApprovableCollectionReader = hasApprovableCollectionReader;
        this.genericDocumentService = genericDocumentService;
        this.documentHelper = documentHelper;
        this.manageHearingActionService = manageHearingActionService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return ABOUT_TO_SUBMIT.equals(callbackType)
            && CONTESTED.equals(caseType)
            && EventType.PROCESS_ORDER.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.aboutToSubmit(callbackRequest));
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        String caseId = String.valueOf(caseDetails.getId());
        FinremCaseData caseData = caseDetails.getData();

        // handleNewDocument must be handled before storeAdditionalHearingDocuments in order to stamp the newly uploaded document.
        handleNewDocumentInUnprocessedApprovedDocuments(caseData);

        additionalHearingDocumentService.stampAndCollectOrderCollection(caseDetails, userAuthorisation);

        List<String> errors = new ArrayList<>();
        log.info("Storing Additional Hearing Document for Case ID: {}", caseId);

        Map<String, CaseDocument> stampedDocuments = getStampedDocuments(caseData, userAuthorisation, caseId);
        Map<String, CaseDocument> additionalDocsConverted = new HashMap<>();

        handleDraftOrderDocuments(caseData, stampedDocuments, userAuthorisation, additionalDocsConverted, caseId);
        handlePsaDocuments(caseData, stampedDocuments);
        handleAgreedDraftOrdersCollection(caseData, stampedDocuments, additionalDocsConverted);
        clearTemporaryFields(caseData);

        ManageHearingsWrapper hearingsWrapper = caseData.getManageHearingsWrapper();

        if (EventType.PROCESS_ORDER.equals(callbackRequest.getEventType())
            && YesOrNo.YES.equals(hearingsWrapper.getIsAddHearingChosen())) {
            hearingsWrapper.setManageHearingsActionSelection(ManageHearingsAction.ADD_HEARING);
            manageHearingActionService.performAddHearing(caseDetails, userAuthorisation);
            manageHearingActionService.updateTabData(caseData);
        }

        hearingsWrapper.setWorkingHearing(null);
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData).errors(errors).build();
    }

    private void handleNewDocumentInUnprocessedApprovedDocuments(FinremCaseData caseData) {
        nullSafeUnprocessedApprovedDocuments(caseData).forEach(unprocessedApprovedDocuments -> {
            if (isNewDocument(unprocessedApprovedDocuments)) {
                insertNewDocumentToUploadHearingOrder(caseData, unprocessedApprovedDocuments);
            }
        });
    }

    private Map<String, CaseDocument> getStampedDocuments(FinremCaseData caseData, String userAuthorisation, String caseId) {
        Map<String, CaseDocument> stampedDocuments = new HashMap<>();
        StampType stampType = documentHelper.getStampType(caseData);
        String documentCategoryId = DocumentCategory.APPROVED_ORDERS.getDocumentCategoryId();
        List<DirectionOrderCollection> unprocessedApprovedDocuments = nullSafeUnprocessedApprovedDocuments(caseData);

        unprocessedApprovedDocuments.forEach(doc -> {
            if (!isNewDocument(doc)) {
                CaseDocument originalDocument = doc.getValue().getOriginalDocument();
                CaseDocument uploadedDocument = doc.getValue().getUploadDraftDocument();
                CaseDocument stampedDocument = genericDocumentService.stampDocument(uploadedDocument, userAuthorisation,
                    stampType, caseId);
                stampedDocument.setCategoryId(documentCategoryId);

                stampedDocuments.put(originalDocument.getDocumentUrl(), stampedDocument);
            }
        });

        return stampedDocuments;
    }

    private void handleDraftOrderDocuments(FinremCaseData caseData, Map<String, CaseDocument> stampedDocuments,
                                           String authorisation, Map<String, CaseDocument> additionalDocsConverted, String caseId) {
        List<DraftOrderDocReviewCollection> collector = new ArrayList<>();
        hasApprovableCollectionReader.filterAndCollectDraftOrderDocs(caseData.getDraftOrdersWrapper().getDraftOrdersReviewCollection(),
            collector, APPROVED_BY_JUDGE::equals);

        getApprovedDocumentsToProcess(caseData)
            .forEach(unprocessedApprovedOrder ->
                collector.stream()
                    .filter(psa -> doesDocumentMatch(psa, unprocessedApprovedOrder))
                    .map(DraftOrderDocReviewCollection::getValue)
                    .forEach(draftOrderDocumentReview -> {
                        CaseDocument originalDocument = draftOrderDocumentReview.getDraftOrderDocument();
                        CaseDocument stampedDocument = stampedDocuments.get(originalDocument.getDocumentUrl());
                        draftOrderDocumentReview.setOrderStatus(PROCESSED);
                        draftOrderDocumentReview.setDraftOrderDocument(stampedDocument);

                        //Process attachments
                        emptyIfNull(draftOrderDocumentReview.getAttachments()).forEach(attachment -> {
                            CaseDocument convertedAttachment = genericDocumentService.convertDocumentIfNotPdfAlready(
                                attachment.getValue(), authorisation, caseId);

                            //Store additional document and replace attachment in review collection
                            additionalDocsConverted.put(attachment.getValue().getDocumentUrl(), convertedAttachment);
                            attachment.setValue(convertedAttachment);
                        });

                    })
            );
    }

    private void handlePsaDocuments(FinremCaseData caseData, Map<String, CaseDocument> stampedDocuments) {
        List<PsaDocReviewCollection> psaCollector = new ArrayList<>();

        hasApprovableCollectionReader.filterAndCollectPsaDocs(caseData.getDraftOrdersWrapper().getDraftOrdersReviewCollection(),
            psaCollector, APPROVED_BY_JUDGE::equals);

        getApprovedDocumentsToProcess(caseData)
            .forEach(unprocessedApprovedOrder ->
                psaCollector.stream()
                    .filter(psa -> doesDocumentMatch(psa, unprocessedApprovedOrder))
                    .map(PsaDocReviewCollection::getValue)
                    .forEach(psaDocumentReview -> {
                        CaseDocument originalDocument = psaDocumentReview.getPsaDocument();
                        CaseDocument stampedDocument = stampedDocuments.get(originalDocument.getDocumentUrl());
                        psaDocumentReview.setOrderStatus(PROCESSED);
                        psaDocumentReview.setPsaDocument(stampedDocument);
                    })
            );
    }

    private void handleAgreedDraftOrdersCollection(FinremCaseData caseData, Map<String, CaseDocument> stampedDocuments,
                                                   Map<String, CaseDocument> additionalDocsConverted) {
        List<AgreedDraftOrderCollection> agreedOrderCollector = new ArrayList<>();

        hasApprovableCollectionReader.collectAgreedDraftOrders(caseData.getDraftOrdersWrapper().getAgreedDraftOrderCollection(),
            agreedOrderCollector, APPROVED_BY_JUDGE::equals);
        hasApprovableCollectionReader.collectAgreedDraftOrders(caseData.getDraftOrdersWrapper().getIntvAgreedDraftOrderCollection(),
            agreedOrderCollector, APPROVED_BY_JUDGE::equals);

        getApprovedDocumentsToProcess(caseData)
            .forEach(unprocessedApprovedOrder ->
                agreedOrderCollector.stream()
                    .filter(agreedDraftOrder -> doesDocumentMatch(agreedDraftOrder, unprocessedApprovedOrder))
                    .map(AgreedDraftOrderCollection::getValue)
                    .forEach(agreedDraftOrder -> {
                        if (agreedDraftOrder.getDraftOrder() != null) {
                            CaseDocument originalDocument = agreedDraftOrder.getDraftOrder();
                            CaseDocument stampedDocument = stampedDocuments.get(originalDocument.getDocumentUrl());

                            //Replace additional documents with converted PDF
                            emptyIfNull(agreedDraftOrder.getAttachments()).forEach(attachment -> {
                                CaseDocument convertedAdditionalDocument = additionalDocsConverted.get(attachment.getValue().getDocumentUrl());
                                attachment.setValue(convertedAdditionalDocument);
                            });

                            agreedDraftOrder.setOrderStatus(PROCESSED);
                            agreedDraftOrder.setDraftOrder(stampedDocument);
                        } else if (agreedDraftOrder.getPensionSharingAnnex() != null) {
                            CaseDocument originalDocument = agreedDraftOrder.getPensionSharingAnnex();
                            CaseDocument stampedDocument = stampedDocuments.get(originalDocument.getDocumentUrl());
                            agreedDraftOrder.setOrderStatus(PROCESSED);
                            agreedDraftOrder.setPensionSharingAnnex(stampedDocument);
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
        draftOrdersWrapper.setUnprocessedApprovedDocuments(null);
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

    private List<DirectionOrderCollection> getApprovedDocumentsToProcess(FinremCaseData caseData) {
        return nullSafeUnprocessedApprovedDocuments(caseData).stream().filter(not(this::isNewDocument)).toList();
    }
}
