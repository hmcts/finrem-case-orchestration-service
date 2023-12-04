package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory;

import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApproveOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;

@Configuration
public class SendOrdersCategoriser extends DocumentCategoriser {


    public SendOrdersCategoriser(FeatureToggleService featureToggleService) {
        super(featureToggleService);
    }

    @Override
    protected void categoriseDocuments(FinremCaseData finremCaseData) {

        if (finremCaseData.getOrderWrapper().getAppOrderCollection() != null) {
            finremCaseData.getOrderWrapper().getAppOrderCollection().forEach(
                ao -> setCategoryToAllOrdersDocs(ao, DocumentCategory.APPROVED_ORDERS_SEND_ORDERS_APPLICANT));
        }
        if (finremCaseData.getOrderWrapper().getRespOrderCollection() != null) {
            finremCaseData.getOrderWrapper().getRespOrderCollection().forEach(
                ao -> setCategoryToAllOrdersDocs(ao, DocumentCategory.APPROVED_ORDERS_SEND_ORDERS_RESPONDENT));
        }
        if (finremCaseData.getOrderWrapper().getIntv1OrderCollection() != null) {
            finremCaseData.getOrderWrapper().getIntv1OrderCollection().forEach(
                ao -> setCategoryToAllOrdersDocs(ao, DocumentCategory.APPROVED_ORDERS_SEND_ORDERS_INTERVENER_1));
        }
        if (finremCaseData.getOrderWrapper().getIntv2OrderCollection() != null) {
            finremCaseData.getOrderWrapper().getIntv2OrderCollection().forEach(
                ao -> setCategoryToAllOrdersDocs(ao, DocumentCategory.APPROVED_ORDERS_SEND_ORDERS_INTERVENER_2));
        }
        if (finremCaseData.getOrderWrapper().getIntv3OrderCollection() != null) {
            finremCaseData.getOrderWrapper().getIntv3OrderCollection().forEach(
                ao -> setCategoryToAllOrdersDocs(ao, DocumentCategory.APPROVED_ORDERS_SEND_ORDERS_INTERVENER_3));
        }
        if (finremCaseData.getOrderWrapper().getIntv4OrderCollection() != null) {
            finremCaseData.getOrderWrapper().getIntv4OrderCollection().forEach(
                ao -> setCategoryToAllOrdersDocs(ao, DocumentCategory.APPROVED_ORDERS_SEND_ORDERS_INTERVENER_4));
        }
    }

    private void setCategoryToAllOrdersDocs(ApprovedOrderCollection ga, DocumentCategory categoryToApply) {
        ApproveOrder approveOrderItem = ga.getValue();
        CaseDocument document = approveOrderItem.getCaseDocument();

        if (document != null) {
            document.setCategoryId(
                categoryToApply.getDocumentCategoryId()
            );
        }
    }
}
