package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory;

import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationItems;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class GeneralApplicationsCategoriser extends DocumentCategoriser {

    private static final Map<Integer, DocumentCategory> gaNumberToCategory = Map.of(1, DocumentCategory.APPLICATIONS_GENERAL_APPLICATIONS_APPLICATION_1,
        2, DocumentCategory.APPLICATIONS_GENERAL_APPLICATIONS_APPLICATION_2,
        3, DocumentCategory.APPLICATIONS_GENERAL_APPLICATIONS_APPLICATION_3,
        4, DocumentCategory.APPLICATIONS_GENERAL_APPLICATIONS_APPLICATION_4,
        5, DocumentCategory.APPLICATIONS_GENERAL_APPLICATIONS_APPLICATION_5,
        6, DocumentCategory.APPLICATIONS_GENERAL_APPLICATIONS_APPLICATION_6,
        7, DocumentCategory.APPLICATIONS_GENERAL_APPLICATIONS_APPLICATION_7,
        8, DocumentCategory.APPLICATIONS_GENERAL_APPLICATIONS_APPLICATION_8,
        9, DocumentCategory.APPLICATIONS_GENERAL_APPLICATIONS_APPLICATION_9,
        10, DocumentCategory.APPLICATIONS_GENERAL_APPLICATIONS_APPLICATION_10);

    public GeneralApplicationsCategoriser(FeatureToggleService featureToggleService) {
        super(featureToggleService);
    }

    @Override
    protected void categorizeDocuments(FinremCaseData finremCaseData) {
        AtomicInteger generalApplicationCounter = new AtomicInteger();
        if (finremCaseData.getGeneralApplicationWrapper().getGeneralApplications() != null) {
            finremCaseData.getGeneralApplicationWrapper().getGeneralApplications().forEach(
                ga -> {
                    // TODO 10 or more will break this
                    generalApplicationCounter.getAndIncrement();
                    DocumentCategory categoryToApply;

                    if (generalApplicationCounter.get() > 10) {
                        categoryToApply = (DocumentCategory.APPLICATIONS_GENERAL_APPLICATIONS_OVERFLOW);
                    } else {
                        categoryToApply = (gaNumberToCategory.get(generalApplicationCounter.get()));
                    }

                    GeneralApplicationItems generalApplicationItems = ga.getValue();
                    CaseDocument generalApplicationDocument = generalApplicationItems.getGeneralApplicationDocument();
                    CaseDocument generalApplicationDraftOrder = generalApplicationItems.getGeneralApplicationDraftOrder();
                    CaseDocument generalApplicationDirectionsDocument = generalApplicationItems.getGeneralApplicationDirectionsDocument();

                    if (generalApplicationDocument != null) {
                        generalApplicationDocument.setCategoryId(
                            categoryToApply.getDocumentCategoryId()
                        );
                    }

                    if (generalApplicationDraftOrder != null) {
                        generalApplicationDraftOrder.setCategoryId(
                            categoryToApply.getDocumentCategoryId()
                        );
                    }

                    if (generalApplicationDirectionsDocument != null) {
                        generalApplicationDirectionsDocument.setCategoryId(
                            categoryToApply.getDocumentCategoryId()
                        );
                    }
                });
        }
    }

    public void uncategoriseDuplicatedCollections(FinremCaseData finremCaseData) {
        if (finremCaseData.getGeneralApplicationWrapper().getGeneralApplicationIntvrOrders() != null) {
            removeDocumentCategory(finremCaseData.getGeneralApplicationWrapper().getGeneralApplicationIntvrOrders());
        }

        if (finremCaseData.getGeneralApplicationWrapper().getAppRespGeneralApplications() != null) {
            removeDocumentCategory(finremCaseData.getGeneralApplicationWrapper().getAppRespGeneralApplications());
        }

        if (finremCaseData.getGeneralApplicationWrapper().getIntervener1GeneralApplications() != null) {
            removeDocumentCategory(finremCaseData.getGeneralApplicationWrapper().getIntervener1GeneralApplications());
        }

        if (finremCaseData.getGeneralApplicationWrapper().getIntervener2GeneralApplications() != null) {
            removeDocumentCategory(finremCaseData.getGeneralApplicationWrapper().getIntervener2GeneralApplications());
        }

        if (finremCaseData.getGeneralApplicationWrapper().getIntervener3GeneralApplications() != null) {
            removeDocumentCategory(finremCaseData.getGeneralApplicationWrapper().getIntervener3GeneralApplications());
        }

        if (finremCaseData.getGeneralApplicationWrapper().getIntervener4GeneralApplications() != null) {
            removeDocumentCategory(finremCaseData.getGeneralApplicationWrapper().getIntervener4GeneralApplications());
        }
    }

    private void removeDocumentCategory(List<GeneralApplicationsCollection> collectionToRemoveCategoryFrom) {
        collectionToRemoveCategoryFrom.forEach(
            ga -> {
                GeneralApplicationItems generalApplicationItems = ga.getValue();
                CaseDocument generalApplicationDocument = generalApplicationItems.getGeneralApplicationDocument();
                CaseDocument generalApplicationDraftOrder = generalApplicationItems.getGeneralApplicationDraftOrder();
                CaseDocument generalApplicationDirectionsDocument = generalApplicationItems.getGeneralApplicationDirectionsDocument();

                if (generalApplicationDocument != null) {
                    generalApplicationDocument.setCategoryId(
                        DocumentCategory.DUPLICATED_GENERAL_ORDERS.getDocumentCategoryId()
                    );
                }

                if (generalApplicationDraftOrder != null) {
                    generalApplicationDraftOrder.setCategoryId(
                        DocumentCategory.DUPLICATED_GENERAL_ORDERS.getDocumentCategoryId()                        );
                }

                if (generalApplicationDirectionsDocument != null) {
                    generalApplicationDirectionsDocument.setCategoryId(
                        DocumentCategory.DUPLICATED_GENERAL_ORDERS.getDocumentCategoryId()                        );
                }
            }
        );
    }
}
