package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftDirectionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UploadedDraftOrderCategoriserTest {
    private UploadedDraftOrderCategoriser uploadedDraftOrderCategoriser;

    @Mock
    FeatureToggleService featureToggleService;

    @BeforeEach
    public void setUp() {
        uploadedDraftOrderCategoriser = new UploadedDraftOrderCategoriser(featureToggleService);
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
    }

    @Test
    void shouldCategoriseJudgesAmendedOrderCollectionWithoutAdditionalDocuments() {
        FinremCaseData caseData = createCaseDataWithJudgeOrders(false);

        uploadedDraftOrderCategoriser.categorise(caseData);

        caseData.getDraftDirectionWrapper().getJudgesAmendedOrderCollection()
            .forEach(order -> {
                assertEquals(DocumentCategory.SYSTEM_DUPLICATES.getDocumentCategoryId(), order.getValue().getUploadDraftDocument().getCategoryId());
            });
    }

    @Test
    void shouldCategoriseDraftDirectionOrderCollectionWithoutAdditionalDocuments() {
        FinremCaseData caseData = createCaseDataForDraftDirectionOrderCollection(false);

        uploadedDraftOrderCategoriser.categorise(caseData);

        caseData.getDraftDirectionWrapper().getDraftDirectionOrderCollection()
            .forEach(order -> {
                assertEquals(DocumentCategory.POST_HEARING_DRAFT_ORDER.getDocumentCategoryId(),
                    order.getValue().getUploadDraftDocument().getCategoryId());
            });
    }

    @Test
    void shouldCategoriseJudgesAmendedOrderCollectionWithAdditionalDocuments() {
        FinremCaseData caseData = createCaseDataWithJudgeOrders(true);

        uploadedDraftOrderCategoriser.categorise(caseData);

        caseData.getDraftDirectionWrapper().getJudgesAmendedOrderCollection()
            .forEach(order -> {
                assertEquals(DocumentCategory.SYSTEM_DUPLICATES.getDocumentCategoryId(),
                    order.getValue().getUploadDraftDocument().getCategoryId());
                order.getValue().getAdditionalDocuments().forEach(additionalDoc ->
                    assertEquals(DocumentCategory.SYSTEM_DUPLICATES.getDocumentCategoryId(), additionalDoc.getValue().getCategoryId())
                );
            });
    }

    @Test
    void shouldCategoriseDraftDirectionOrderCollectionWithAdditionalDocuments() {
        FinremCaseData caseData = createCaseDataForDraftDirectionOrderCollection(true);

        uploadedDraftOrderCategoriser.categorise(caseData);

        caseData.getDraftDirectionWrapper().getDraftDirectionOrderCollection()
            .forEach(order -> {
                assertEquals(DocumentCategory.POST_HEARING_DRAFT_ORDER.getDocumentCategoryId(),
                    order.getValue().getUploadDraftDocument().getCategoryId());
                order.getValue().getAdditionalDocuments().forEach(additionalDoc ->
                    assertEquals(DocumentCategory.POST_HEARING_DRAFT_ORDER.getDocumentCategoryId(),
                        additionalDoc.getValue().getCategoryId())
                );
            });
    }

    private FinremCaseData createCaseDataWithJudgeOrders(boolean withAdditionalDocuments) {
        FinremCaseData caseData = new FinremCaseData();
        DraftDirectionWrapper wrapper = new DraftDirectionWrapper();

        DraftDirectionOrder order1 = createDraftOrder(withAdditionalDocuments);

        wrapper.setJudgesAmendedOrderCollection(List.of(
            createOrderCollection(order1)
        ));

        caseData.setDraftDirectionWrapper(wrapper);
        return caseData;
    }


    private FinremCaseData createCaseDataForDraftDirectionOrderCollection(boolean withAdditionalDocuments) {
        FinremCaseData caseData = new FinremCaseData();
        DraftDirectionWrapper wrapper = new DraftDirectionWrapper();

        DraftDirectionOrder order1 = createDraftOrder(withAdditionalDocuments);
        DraftDirectionOrder order2 = createDraftOrder(withAdditionalDocuments);

        wrapper.setDraftDirectionOrderCollection(List.of(
            createOrderCollection(order1),
            createOrderCollection(order2)
        ));

        caseData.setDraftDirectionWrapper(wrapper);
        return caseData;
    }

    private DraftDirectionOrderCollection createOrderCollection(DraftDirectionOrder order) {
        DraftDirectionOrderCollection collection = new DraftDirectionOrderCollection();
        collection.setValue(order);
        return collection;
    }

    private DraftDirectionOrder createDraftOrder(boolean withAdditionalDocuments) {
        DraftDirectionOrder order = new DraftDirectionOrder();
        CaseDocument draftOrderDocument = CaseDocument.builder().documentFilename("draft order.pdf").build();
        draftOrderDocument.setCategoryId(null); // Simulate missing category
        order.setUploadDraftDocument(draftOrderDocument);

        if (withAdditionalDocuments) {
            CaseDocument document1 = CaseDocument.builder().documentFilename("additional doc 1.pdf").build();
            CaseDocument document2 = CaseDocument.builder().documentFilename("additional doc 2.pdf").build();

            DocumentCollection additionalDocument1 = DocumentCollection.builder()
                .value(document1)
                .build();
            DocumentCollection additionalDocument2 = DocumentCollection.builder()
                .value(document2)
                .build();

            order.setAdditionalDocuments(List.of(additionalDocument1, additionalDocument2));
        } else {
            order.setAdditionalDocuments(null);
        }
        return order;
    }
}
