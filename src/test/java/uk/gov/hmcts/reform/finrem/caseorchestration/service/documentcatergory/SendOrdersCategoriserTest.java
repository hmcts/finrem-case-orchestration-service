package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.BaseHandlerTestSetup;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApproveOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApproveOrdersHolder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrderConsolidateCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentInContestedApprovedOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentInContestedApprovedOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrderSentToPartiesCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.SendOrderDocuments;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ConsentOrderWrapper;
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
    @Mock
    DocumentHelper documentHelper;

    @BeforeEach
    public void setUp() {
        sendOrdersCategoriser = new SendOrdersCategoriser(featureToggleService, documentHelper);
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
    }

    @Test
    void testCategorizeDocuments() {
        FinremCaseData finremCaseData = buildFinremCaseData();
        sendOrdersCategoriser.categorise(finremCaseData);

        assertEquals(DocumentCategory.ADMINISTRATIVE_DOCUMENTS_TRANSITIONAL.getDocumentCategoryId(),
            finremCaseData.getOrdersSentToPartiesCollection().get(0).getValue().getCaseDocument().getCategoryId());

        OrderWrapper orderWrapper = finremCaseData.getOrderWrapper();
        assertEquals(DocumentCategory.SYSTEM_DUPLICATES.getDocumentCategoryId(),
            orderWrapper.getAppOrderCollections().get(0).getValue().getApproveOrders()
                .get(0).getValue().getCaseDocument().getCategoryId());
        assertEquals(DocumentCategory.SYSTEM_DUPLICATES.getDocumentCategoryId(),
            orderWrapper.getRespOrderCollections().get(0).getValue().getApproveOrders()
                .get(0).getValue().getCaseDocument().getCategoryId());
        assertEquals(DocumentCategory.SYSTEM_DUPLICATES.getDocumentCategoryId(),
            orderWrapper.getIntv1OrderCollections().get(0).getValue().getApproveOrders()
                .get(0).getValue().getCaseDocument().getCategoryId());
        assertEquals(DocumentCategory.SYSTEM_DUPLICATES.getDocumentCategoryId(),
            orderWrapper.getIntv2OrderCollections().get(0).getValue().getApproveOrders()
                .get(0).getValue().getCaseDocument().getCategoryId());
        assertEquals(DocumentCategory.SYSTEM_DUPLICATES.getDocumentCategoryId(),
            orderWrapper.getIntv3OrderCollections().get(0).getValue().getApproveOrders()
                .get(0).getValue().getCaseDocument().getCategoryId());
        assertEquals(DocumentCategory.SYSTEM_DUPLICATES.getDocumentCategoryId(),
            orderWrapper.getIntv4OrderCollections().get(0).getValue().getApproveOrders()
                .get(0).getValue().getCaseDocument().getCategoryId());

        ConsentOrderWrapper consentOrderWrapper = finremCaseData.getConsentOrderWrapper();
        assertEquals(DocumentCategory.SYSTEM_DUPLICATES.getDocumentCategoryId(),
            consentOrderWrapper.getAppConsentApprovedOrders().get(0).getApprovedOrder().getConsentOrder()
                .getCategoryId());
        assertEquals(DocumentCategory.SYSTEM_DUPLICATES.getDocumentCategoryId(),
            consentOrderWrapper.getRespConsentApprovedOrders().get(0).getApprovedOrder().getConsentOrder()
                .getCategoryId());
        assertEquals(DocumentCategory.SYSTEM_DUPLICATES.getDocumentCategoryId(),
            consentOrderWrapper.getIntv1ConsentApprovedOrders().get(0).getApprovedOrder().getConsentOrder()
                .getCategoryId());
        assertEquals(DocumentCategory.SYSTEM_DUPLICATES.getDocumentCategoryId(),
            consentOrderWrapper.getIntv2ConsentApprovedOrders().get(0).getApprovedOrder().getConsentOrder()
                .getCategoryId());
        assertEquals(DocumentCategory.SYSTEM_DUPLICATES.getDocumentCategoryId(),
            consentOrderWrapper.getIntv3ConsentApprovedOrders().get(0).getApprovedOrder().getConsentOrder()
                .getCategoryId());
        assertEquals(DocumentCategory.SYSTEM_DUPLICATES.getDocumentCategoryId(),
            consentOrderWrapper.getIntv4ConsentApprovedOrders().get(0).getApprovedOrder().getConsentOrder()
                .getCategoryId());
    }

    @Test
    void testCategorizeDocumentsWhenCollectionIsnull() {
        FinremCaseData finremCaseData = FinremCaseData.builder().orderWrapper(OrderWrapper.builder()
            .build()).build();
        sendOrdersCategoriser.categorise(finremCaseData);
        assertNull(finremCaseData.getOrdersSentToPartiesCollection());

        assertNull(finremCaseData.getOrderWrapper().getAppOrderCollections());
        assertNull(finremCaseData.getOrderWrapper().getRespOrderCollections());
        assertNull(finremCaseData.getOrderWrapper().getIntv1OrderCollections());
        assertNull(finremCaseData.getOrderWrapper().getIntv2OrderCollections());
        assertNull(finremCaseData.getOrderWrapper().getIntv3OrderCollections());
        assertNull(finremCaseData.getOrderWrapper().getIntv4OrderCollections());

        assertNull(finremCaseData.getConsentOrderWrapper().getAppConsentApprovedOrders());
        assertNull(finremCaseData.getConsentOrderWrapper().getRespConsentApprovedOrders());
        assertNull(finremCaseData.getConsentOrderWrapper().getIntv1ConsentApprovedOrders());
        assertNull(finremCaseData.getConsentOrderWrapper().getIntv2ConsentApprovedOrders());
        assertNull(finremCaseData.getConsentOrderWrapper().getIntv3ConsentApprovedOrders());
        assertNull(finremCaseData.getConsentOrderWrapper().getIntv4ConsentApprovedOrders());


    }

    protected FinremCaseData buildFinremCaseData() {

        OrderSentToPartiesCollection orderSentToPartiesCollection = OrderSentToPartiesCollection.builder()
            .value(SendOrderDocuments.builder().caseDocument(CaseDocument.builder().build()).build())
            .build();


        ApprovedOrderConsolidateCollection appApprovedOrderConsolidateCollection = ApprovedOrderConsolidateCollection.builder()
            .value(ApproveOrdersHolder.builder().approveOrders(List.of(ApprovedOrderCollection.builder()
                    .value(ApproveOrder.builder().caseDocument(CaseDocument.builder().build()).build()).build()))
                .build()).build();
        ApprovedOrderConsolidateCollection repApprovedOrderConsolidateCollection = ApprovedOrderConsolidateCollection.builder()
            .value(ApproveOrdersHolder.builder().approveOrders(List.of(ApprovedOrderCollection.builder()
                    .value(ApproveOrder.builder().caseDocument(CaseDocument.builder().build()).build()).build()))
                .build()).build();
        ApprovedOrderConsolidateCollection intv1ApprovedOrderConsolidateCollection = ApprovedOrderConsolidateCollection.builder()
            .value(ApproveOrdersHolder.builder().approveOrders(List.of(ApprovedOrderCollection.builder()
                    .value(ApproveOrder.builder().caseDocument(CaseDocument.builder().build()).build()).build()))
                .build()).build();
        ApprovedOrderConsolidateCollection intv2ApprovedOrderConsolidateCollection = ApprovedOrderConsolidateCollection.builder()
            .value(ApproveOrdersHolder.builder().approveOrders(List.of(ApprovedOrderCollection.builder()
                    .value(ApproveOrder.builder().caseDocument(CaseDocument.builder().build()).build()).build()))
                .build()).build();
        ApprovedOrderConsolidateCollection intv3ApprovedOrderConsolidateCollection = ApprovedOrderConsolidateCollection.builder()
            .value(ApproveOrdersHolder.builder().approveOrders(List.of(ApprovedOrderCollection.builder()
                    .value(ApproveOrder.builder().caseDocument(CaseDocument.builder().build()).build()).build()))
                .build()).build();
        ApprovedOrderConsolidateCollection intv4ApprovedOrderConsolidateCollection = ApprovedOrderConsolidateCollection.builder()
            .value(ApproveOrdersHolder.builder().approveOrders(List.of(ApprovedOrderCollection.builder()
                    .value(ApproveOrder.builder().caseDocument(CaseDocument.builder().build()).build()).build()))
                .build()).build();


        OrderWrapper orderWrapper = OrderWrapper.builder()
            .appOrderCollections(List.of(appApprovedOrderConsolidateCollection))
            .respOrderCollections(List.of(repApprovedOrderConsolidateCollection))
            .intv1OrderCollections(List.of(intv1ApprovedOrderConsolidateCollection))
            .intv2OrderCollections(List.of(intv2ApprovedOrderConsolidateCollection))
            .intv3OrderCollections(List.of(intv3ApprovedOrderConsolidateCollection))
            .intv4OrderCollections(List.of(intv4ApprovedOrderConsolidateCollection))
            .build();

        ConsentInContestedApprovedOrderCollection appConsentOrderCollection = ConsentInContestedApprovedOrderCollection
            .builder().approvedOrder(ConsentInContestedApprovedOrder.builder()
                .consentOrder(CaseDocument.builder().build())
                .orderLetter(CaseDocument.builder().build())
                .additionalConsentDocuments(null)
                .pensionDocuments(null).build())
            .build();

        ConsentInContestedApprovedOrderCollection repsConsentOrderCollection = ConsentInContestedApprovedOrderCollection
            .builder().approvedOrder(ConsentInContestedApprovedOrder.builder()
                .consentOrder(CaseDocument.builder().build())
                .orderLetter(CaseDocument.builder().build())
                .additionalConsentDocuments(null)
                .pensionDocuments(null).build())
            .build();
        ConsentInContestedApprovedOrderCollection intv1ConsentOrderCollection = ConsentInContestedApprovedOrderCollection
            .builder().approvedOrder(ConsentInContestedApprovedOrder.builder()
                .consentOrder(CaseDocument.builder().build())
                .orderLetter(CaseDocument.builder().build())
                .additionalConsentDocuments(null)
                .pensionDocuments(null).build())
            .build();
        ConsentInContestedApprovedOrderCollection intv2ConsentOrderCollection = ConsentInContestedApprovedOrderCollection
            .builder().approvedOrder(ConsentInContestedApprovedOrder.builder()
                .consentOrder(CaseDocument.builder().build())
                .orderLetter(CaseDocument.builder().build())
                .additionalConsentDocuments(null)
                .pensionDocuments(null).build())
            .build();
        ConsentInContestedApprovedOrderCollection intv3ConsentOrderCollection = ConsentInContestedApprovedOrderCollection
            .builder().approvedOrder(ConsentInContestedApprovedOrder.builder()
                .consentOrder(CaseDocument.builder().build())
                .orderLetter(CaseDocument.builder().build())
                .additionalConsentDocuments(null)
                .pensionDocuments(null).build())
            .build();
        ConsentInContestedApprovedOrderCollection intv4ConsentOrderCollection = ConsentInContestedApprovedOrderCollection
            .builder().approvedOrder(ConsentInContestedApprovedOrder.builder()
                .consentOrder(CaseDocument.builder().build())
                .orderLetter(CaseDocument.builder().build())
                .additionalConsentDocuments(null)
                .pensionDocuments(null).build())
            .build();

        ConsentOrderWrapper consentOrderWrapper = ConsentOrderWrapper.builder()
            .appConsentApprovedOrders(List.of(appConsentOrderCollection))
            .respConsentApprovedOrders(List.of(repsConsentOrderCollection))
            .intv1ConsentApprovedOrders(List.of(intv1ConsentOrderCollection))
            .intv2ConsentApprovedOrders(List.of(intv2ConsentOrderCollection))
            .intv3ConsentApprovedOrders(List.of(intv3ConsentOrderCollection))
            .intv4ConsentApprovedOrders(List.of(intv4ConsentOrderCollection))
            .build();

        return FinremCaseData.builder().ordersSentToPartiesCollection(List.of(orderSentToPartiesCollection))
            .orderWrapper(orderWrapper).consentOrderWrapper(consentOrderWrapper).build();

    }
}
