package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory;

import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;

import java.util.List;

@Configuration
public class HearingNoticesCategoriser extends DocumentCategoriser {

    public HearingNoticesCategoriser(FeatureToggleService featureToggleService) {
        super(featureToggleService);
    }

    @Override
    protected void categoriseDocuments(FinremCaseData finremCaseData) {
        List<DocumentCollectionItem> hearingNoticeDocumentPack = finremCaseData.getHearingNoticeDocumentPack();
        if (hearingNoticeDocumentPack != null && !hearingNoticeDocumentPack.isEmpty()) {
            hearingNoticeDocumentPack.forEach(documentCollection -> {
                CaseDocument value = documentCollection.getValue();
                if (value.getDocumentFilename().equals("AdditionalHearingDocument.pdf")) {
                    value.setCategoryId(DocumentCategory.SYSTEM_DUPLICATES.getDocumentCategoryId());
                } else {
                    value.setCategoryId(DocumentCategory.HEARING_NOTICES.getDocumentCategoryId());
                }
            });
        }
    }
}
