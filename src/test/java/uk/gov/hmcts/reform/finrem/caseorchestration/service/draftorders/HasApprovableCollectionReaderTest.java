package uk.gov.hmcts.reform.finrem.caseorchestration.service.draftorders;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.agreed.AgreedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.agreed.AgreedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrderDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrderDocumentReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrdersReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrdersReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.PsaDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.PsaDocumentReview;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus.APPROVED_BY_JUDGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus.PROCESSED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus.REFUSED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.OrderStatus.TO_BE_REVIEWED;

@ExtendWith(MockitoExtension.class)
class HasApprovableCollectionReaderTest {

    @InjectMocks
    private HasApprovableCollectionReader underTest;

    @Test
    void shouldPartitionByOrderStatus() {
        List<AgreedDraftOrderCollection> sample =
            List.of(
                AgreedDraftOrderCollection.builder().value(AgreedDraftOrder.builder().orderStatus(REFUSED).build()).build(),
                AgreedDraftOrderCollection.builder().value(AgreedDraftOrder.builder().orderStatus(TO_BE_REVIEWED).build()).build(),
                AgreedDraftOrderCollection.builder().value(AgreedDraftOrder.builder().orderStatus(APPROVED_BY_JUDGE).build()).build(),
                AgreedDraftOrderCollection.builder().value(AgreedDraftOrder.builder().orderStatus(APPROVED_BY_JUDGE).build()).build()
            );

        Map<Boolean, List<AgreedDraftOrderCollection>> result = underTest.partitionByOrderStatus(sample, REFUSED::equals);
        assertThat(result.get(true)).hasSize(1);
        assertThat(result.get(false)).hasSize(3);

        Map<Boolean, List<AgreedDraftOrderCollection>> result2 = underTest.partitionByOrderStatus(sample, TO_BE_REVIEWED::equals);
        assertThat(result2.get(true)).hasSize(1);
        assertThat(result2.get(false)).hasSize(3);

        Map<Boolean, List<AgreedDraftOrderCollection>> result3 = underTest.partitionByOrderStatus(sample, APPROVED_BY_JUDGE::equals);
        assertThat(result3.get(true)).hasSize(2);
        assertThat(result3.get(false)).hasSize(2);
    }

    @Test
    void shouldFilterAndCollectDraftOrderDocs() {
        List<DraftOrderDocReviewCollection> collector = new ArrayList<>();

        CaseDocument draftOrderDocument1 = CaseDocument.builder().documentUrl("do1.doc").build();
        CaseDocument draftOrderDocument2 = CaseDocument.builder().documentUrl("do2.doc").build();

        List<DraftOrdersReviewCollection> sample = List.of(
            DraftOrdersReviewCollection.builder()
                .value(DraftOrdersReview.builder().draftOrderDocReviewCollection(
                    List.of(
                        DraftOrderDocReviewCollection.builder()
                            .value(DraftOrderDocumentReview.builder().draftOrderDocument(draftOrderDocument1).orderStatus(REFUSED).build())
                            .build(),
                        DraftOrderDocReviewCollection.builder()
                            .value(DraftOrderDocumentReview.builder().draftOrderDocument(draftOrderDocument2).orderStatus(TO_BE_REVIEWED).build())
                            .build()
                    )
                ).build()).build()
        );
        List<DraftOrdersReviewCollection> result = underTest.filterAndCollectDraftOrderDocs(sample, collector, REFUSED::equals);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getValue().getDraftOrderDocReviewCollection()).hasSize(1);
        assertEquals(draftOrderDocument2, result.get(0).getValue().getDraftOrderDocReviewCollection().get(0).getValue().getDraftOrderDocument());

        assertThat(collector).hasSize(1);
        assertEquals(draftOrderDocument1, collector.get(0).getValue().getDraftOrderDocument());
    }

    @Test
    void shouldFilterAndCollectPsaDocs() {
        List<PsaDocReviewCollection> collector = new ArrayList<>();

        CaseDocument psaDocument1 = CaseDocument.builder().documentUrl("psa1.doc").build();
        CaseDocument psaDocument2 = CaseDocument.builder().documentUrl("psa2.doc").build();
        CaseDocument psaDocument3 = CaseDocument.builder().documentUrl("psa3.doc").build();
        CaseDocument psaDocument4 = CaseDocument.builder().documentUrl("psa4.doc").build();

        List<DraftOrdersReviewCollection> sample = List.of(
            DraftOrdersReviewCollection.builder()
                .value(DraftOrdersReview.builder().psaDocReviewCollection(
                    List.of(
                        PsaDocReviewCollection.builder()
                            .value(PsaDocumentReview.builder().psaDocument(psaDocument1).orderStatus(REFUSED).build())
                            .build(),
                        PsaDocReviewCollection.builder()
                            .value(PsaDocumentReview.builder().psaDocument(psaDocument2).orderStatus(TO_BE_REVIEWED).build())
                            .build()
                    )
                ).build()).build(),

            DraftOrdersReviewCollection.builder()
                .value(DraftOrdersReview.builder().psaDocReviewCollection(
                    List.of(
                        PsaDocReviewCollection.builder()
                            .value(PsaDocumentReview.builder().psaDocument(psaDocument3).orderStatus(REFUSED).build())
                            .build(),
                        PsaDocReviewCollection.builder()
                            .value(PsaDocumentReview.builder().psaDocument(psaDocument4).orderStatus(APPROVED_BY_JUDGE).build())
                            .build()
                    )
                ).build()).build()
        );
        List<DraftOrdersReviewCollection> result = underTest.filterAndCollectPsaDocs(sample, collector, REFUSED::equals);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getValue().getPsaDocReviewCollection()).hasSize(1);
        assertThat(result.get(1).getValue().getPsaDocReviewCollection()).hasSize(1);
        assertEquals(psaDocument2, result.get(0).getValue().getPsaDocReviewCollection().get(0).getValue().getPsaDocument());
        assertEquals(psaDocument4, result.get(1).getValue().getPsaDocReviewCollection().get(0).getValue().getPsaDocument());

        assertThat(collector).hasSize(2)
            .extracting(PsaDocReviewCollection::getValue)
            .extracting(PsaDocumentReview::getPsaDocument)
            .containsExactly(psaDocument1, psaDocument3);
    }

    @Test
    void shouldCollectAgreedDraftOrder() {
        List<AgreedDraftOrderCollection> collector = new ArrayList<>();

        CaseDocument psaDocument1 = CaseDocument.builder().documentUrl("psa1.doc").build();
        CaseDocument draftOrderDocument1 = CaseDocument.builder().documentUrl("draftOrder1.doc").build();

        List<AgreedDraftOrderCollection> sample = List.of(
            AgreedDraftOrderCollection.builder().value(AgreedDraftOrder.builder().orderStatus(PROCESSED).build())
                .build(),
            AgreedDraftOrderCollection.builder().value(AgreedDraftOrder.builder().orderStatus(APPROVED_BY_JUDGE).build())
                .build(),
            AgreedDraftOrderCollection.builder().value(AgreedDraftOrder.builder().pensionSharingAnnex(psaDocument1).orderStatus(REFUSED).build())
                .build(),
            AgreedDraftOrderCollection.builder().value(AgreedDraftOrder.builder().draftOrder(draftOrderDocument1).orderStatus(REFUSED).build())
                .build()
        );
        underTest.collectAgreedDraftOrders(sample, collector, REFUSED::equals);

        assertThat(collector).hasSize(2)
            .extracting(AgreedDraftOrderCollection::getValue)
            .extracting(AgreedDraftOrder::getTargetDocument)
            .containsExactly(psaDocument1, draftOrderDocument1);
    }
}
