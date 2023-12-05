package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.BaseHandlerTestSetup;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApproveOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.OrderWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SendOrdersCategoriserTest extends BaseHandlerTestSetup {
    private SendOrdersCategoriser sendOrdersCategoriser;

    @Mock
    FeatureToggleService featureToggleService;

    @BeforeEach
    public void setUp() {
        sendOrdersCategoriser = new SendOrdersCategoriser(featureToggleService);
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
    }

    @Test
    void testCategorizeDocuments() {
        FinremCaseData finremCaseData = buildFinremCaseData();
        sendOrdersCategoriser.categorise(finremCaseData);
        OrderWrapper orderWrapper = finremCaseData.getOrderWrapper();
        assertEquals(DocumentCategory.APPLICANT_DOCUMENTS_SEND_ORDERS.getDocumentCategoryId(),
            orderWrapper.getAppOrderCollection().get(0).getValue().getCaseDocument().getCategoryId());
        assertEquals(DocumentCategory.RESPONDENT_DOCUMENTS_SEND_ORDERS.getDocumentCategoryId(),
            orderWrapper.getRespOrderCollection().get(0).getValue().getCaseDocument().getCategoryId());
        assertEquals(DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_1_SEND_ORDERS.getDocumentCategoryId(),
            orderWrapper.getIntv1OrderCollection().get(0).getValue().getCaseDocument().getCategoryId());
        assertEquals(DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_2_SEND_ORDERS.getDocumentCategoryId(),
            orderWrapper.getIntv2OrderCollection().get(0).getValue().getCaseDocument().getCategoryId());
        assertEquals(DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_3_SEND_ORDERS.getDocumentCategoryId(),
            orderWrapper.getIntv3OrderCollection().get(0).getValue().getCaseDocument().getCategoryId());
        assertEquals(DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_4_SEND_ORDERS.getDocumentCategoryId(),
            orderWrapper.getIntv4OrderCollection().get(0).getValue().getCaseDocument().getCategoryId());
    }

    @Test
    void testCategorizeDocumentsWhenCollectionIsnull() {
        FinremCaseData finremCaseData = FinremCaseData.builder().orderWrapper(OrderWrapper.builder()
            .build()).build();
        sendOrdersCategoriser.categorise(finremCaseData);
        assertNull(finremCaseData.getOrderWrapper().getAppOrderCollection());
        assertNull(finremCaseData.getOrderWrapper().getRespOrderCollection());
        assertNull(finremCaseData.getOrderWrapper().getIntv1OrderCollection());
        assertNull(finremCaseData.getOrderWrapper().getIntv2OrderCollection());
        assertNull(finremCaseData.getOrderWrapper().getIntv3OrderCollection());
        assertNull(finremCaseData.getOrderWrapper().getIntv4OrderCollection());
    }

    protected FinremCaseData buildFinremCaseData() {

        ApprovedOrderCollection appApprovedOrderCollection = ApprovedOrderCollection.builder().value(ApproveOrder.builder()
            .caseDocument(CaseDocument.builder().build()).build()).build();
        ApprovedOrderCollection repApprovedOrderCollection = ApprovedOrderCollection.builder().value(ApproveOrder.builder()
            .caseDocument(CaseDocument.builder().build()).build()).build();
        ApprovedOrderCollection intv1ApprovedOrderCollection = ApprovedOrderCollection.builder().value(ApproveOrder.builder()
            .caseDocument(CaseDocument.builder().build()).build()).build();
        ApprovedOrderCollection intv2ApprovedOrderCollection = ApprovedOrderCollection.builder().value(ApproveOrder.builder()
            .caseDocument(CaseDocument.builder().build()).build()).build();
        ApprovedOrderCollection intv3ApprovedOrderCollection = ApprovedOrderCollection.builder().value(ApproveOrder.builder()
            .caseDocument(CaseDocument.builder().build()).build()).build();
        ApprovedOrderCollection intv4ApprovedOrderCollection = ApprovedOrderCollection.builder().value(ApproveOrder.builder()
            .caseDocument(CaseDocument.builder().build()).build()).build();


        OrderWrapper orderWrapper = OrderWrapper.builder()
            .appOrderCollection(List.of(appApprovedOrderCollection))
            .respOrderCollection(List.of(repApprovedOrderCollection))
            .intv1OrderCollection(List.of(intv1ApprovedOrderCollection))
            .intv2OrderCollection(List.of(intv2ApprovedOrderCollection))
            .intv3OrderCollection(List.of(intv3ApprovedOrderCollection))
            .intv4OrderCollection(List.of(intv4ApprovedOrderCollection))
            .build();

        return FinremCaseData.builder().orderWrapper(orderWrapper).build();

    }
}
