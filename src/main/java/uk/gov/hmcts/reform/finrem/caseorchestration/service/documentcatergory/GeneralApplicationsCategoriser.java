package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationItems;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationSupportingDocumentData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.APPROVED_ORDERS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory.DUPLICATED_GENERAL_ORDERS;

@Configuration
public class GeneralApplicationsCategoriser extends DocumentCategoriser {

    private static final Map<Integer, DocumentCategory> gaNumberToCategory = Map.of(
        1, DocumentCategory.APPLICATIONS_OTHER_APPLICATION_APPLICATION_1,
        2, DocumentCategory.APPLICATIONS_OTHER_APPLICATION_APPLICATION_2,
        3, DocumentCategory.APPLICATIONS_OTHER_APPLICATION_APPLICATION_3,
        4, DocumentCategory.APPLICATIONS_OTHER_APPLICATION_APPLICATION_4,
        5, DocumentCategory.APPLICATIONS_OTHER_APPLICATION_APPLICATION_5,
        6, DocumentCategory.APPLICATIONS_OTHER_APPLICATION_APPLICATION_6,
        7, DocumentCategory.APPLICATIONS_OTHER_APPLICATION_APPLICATION_7,
        8, DocumentCategory.APPLICATIONS_OTHER_APPLICATION_APPLICATION_8,
        9, DocumentCategory.APPLICATIONS_OTHER_APPLICATION_APPLICATION_9,
        10, DocumentCategory.APPLICATIONS_OTHER_APPLICATION_APPLICATION_10);

    public GeneralApplicationsCategoriser(FeatureToggleService featureToggleService) {
        super(featureToggleService);
    }

    @Override
    protected void categoriseDocuments(FinremCaseData finremCaseData) {
        List<GeneralApplicationsCollection> generalApplications = finremCaseData.getGeneralApplicationWrapper()
            .getAppRespGeneralApplications();
        if (CollectionUtils.isEmpty(generalApplications)) {
            return;
        }

        AtomicInteger generalApplicationCounter = new AtomicInteger();
        generalApplications.forEach(
            ga -> {
                generalApplicationCounter.getAndIncrement();
                DocumentCategory categoryToApply = getNextApplicationsCategory(generalApplicationCounter.get());
                setCategoryToAllGaDocs(ga, categoryToApply, APPROVED_ORDERS);
            });
    }

    public void uncategoriseDuplicatedCollections(FinremCaseData finremCaseData) {
        GeneralApplicationWrapper generalApplication = finremCaseData.getGeneralApplicationWrapper();
        if (generalApplication.getGeneralApplicationIntvrOrders() != null) {
            removeDocumentCategory(generalApplication.getGeneralApplicationIntvrOrders());
        }

        if (generalApplication.getAppRespGeneralApplications() != null) {
            removeDocumentCategory(generalApplication.getAppRespGeneralApplications());
        }

        if (generalApplication.getIntervener1GeneralApplications() != null) {
            removeDocumentCategory(generalApplication.getIntervener1GeneralApplications());
        }

        if (generalApplication.getIntervener2GeneralApplications() != null) {
            removeDocumentCategory(generalApplication.getIntervener2GeneralApplications());
        }

        if (generalApplication.getIntervener3GeneralApplications() != null) {
            removeDocumentCategory(generalApplication.getIntervener3GeneralApplications());
        }

        if (generalApplication.getIntervener4GeneralApplications() != null) {
            removeDocumentCategory(generalApplication.getIntervener4GeneralApplications());
        }
    }

    private DocumentCategory getNextApplicationsCategory(int counter) {
        if (counter > 10) {
            return DocumentCategory.APPLICATIONS_OTHER_APPLICATION_OVERFLOW;
        } else {
            return gaNumberToCategory.get(counter);
        }
    }

    private void removeDocumentCategory(List<GeneralApplicationsCollection> collectionToRemoveCategoryFrom) {
        collectionToRemoveCategoryFrom.forEach(
            ga -> setCategoryToAllGaDocs(ga, DUPLICATED_GENERAL_ORDERS, DUPLICATED_GENERAL_ORDERS)
        );
    }

    private void setCategoryToAllGaDocs(GeneralApplicationsCollection ga, DocumentCategory categoryToApply,
                                        DocumentCategory directionsDocumentCategory) {
        GeneralApplicationItems generalApplicationItems = ga.getValue();
        CaseDocument generalApplicationDocument = generalApplicationItems.getGeneralApplicationDocument();
        CaseDocument generalApplicationDraftOrder = generalApplicationItems.getGeneralApplicationDraftOrder();
        CaseDocument generalApplicationDirectionsDocument = generalApplicationItems.getGeneralApplicationDirectionsDocument();

        if (generalApplicationDocument != null) {
            generalApplicationDocument.setCategoryId(categoryToApply.getDocumentCategoryId());
        }

        if (generalApplicationDraftOrder != null) {
            generalApplicationDraftOrder.setCategoryId(categoryToApply.getDocumentCategoryId());
        }

        if (generalApplicationDirectionsDocument != null) {
            generalApplicationDirectionsDocument.setCategoryId(directionsDocumentCategory.getDocumentCategoryId());
        }

        List<GeneralApplicationSupportingDocumentData> generalApplicationSupportDocument = ga.getValue().getGaSupportDocuments();
        if (generalApplicationSupportDocument != null) {
            generalApplicationSupportDocument.forEach(
                gaSupportDocument -> {
                    CaseDocument supportDocument = gaSupportDocument.getValue().getSupportDocument();
                    if (supportDocument != null) {
                        supportDocument.setCategoryId(
                            categoryToApply.getDocumentCategoryId()
                        );
                    }
                }
            );
        }
    }
}
