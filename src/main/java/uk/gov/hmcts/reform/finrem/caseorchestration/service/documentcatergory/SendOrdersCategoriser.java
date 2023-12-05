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
                ao -> setCategoryToAllOrdersDocs(ao, DocumentCategory.APPLICANT_DOCUMENTS_SEND_ORDERS));
        }
        if (finremCaseData.getOrderWrapper().getRespOrderCollection() != null) {
            finremCaseData.getOrderWrapper().getRespOrderCollection().forEach(
                ao -> setCategoryToAllOrdersDocs(ao, DocumentCategory.RESPONDENT_DOCUMENTS_SEND_ORDERS));
        }
        if (finremCaseData.getOrderWrapper().getIntv1OrderCollection() != null) {
            finremCaseData.getOrderWrapper().getIntv1OrderCollection().forEach(
                ao -> setCategoryToAllOrdersDocs(ao, DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_1_SEND_ORDERS));
        }
        if (finremCaseData.getOrderWrapper().getIntv2OrderCollection() != null) {
            finremCaseData.getOrderWrapper().getIntv2OrderCollection().forEach(
                ao -> setCategoryToAllOrdersDocs(ao, DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_2_SEND_ORDERS));
        }
        if (finremCaseData.getOrderWrapper().getIntv3OrderCollection() != null) {
            finremCaseData.getOrderWrapper().getIntv3OrderCollection().forEach(
                ao -> setCategoryToAllOrdersDocs(ao, DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_3_SEND_ORDERS));
        }
        if (finremCaseData.getOrderWrapper().getIntv4OrderCollection() != null) {
            finremCaseData.getOrderWrapper().getIntv4OrderCollection().forEach(
                ao -> setCategoryToAllOrdersDocs(ao, DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_4_SEND_ORDERS));
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
