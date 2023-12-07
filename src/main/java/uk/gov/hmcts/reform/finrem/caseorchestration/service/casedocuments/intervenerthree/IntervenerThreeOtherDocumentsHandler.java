package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerthree;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.OtherDocumentsHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.INTERVENER_THREE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_THREE_OTHER_COLLECTION;

@Component
public class IntervenerThreeOtherDocumentsHandler extends OtherDocumentsHandler {

    public IntervenerThreeOtherDocumentsHandler(FeatureToggleService featureToggleService) {
        super(INTERVENER_THREE_OTHER_COLLECTION, INTERVENER_THREE, featureToggleService);
    }

    @Override
    public DocumentCategory getDocumentCategoryFromDocumentType(CaseDocumentType caseDocumentType) {
        switch (caseDocumentType) {
            case OTHER -> {
                return DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_3_MISCELLANEOUS_OR_OTHER;
            }
            case PENSION_PLAN -> {
                return DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_3_PENSION_PLAN;
            }
            case FORM_B, FORM_F, CARE_PLAN -> {
                return DocumentCategory.ADMINISTRATIVE_DOCUMENTS_OTHER;
            }
            default -> {
                return DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_3;
            }
        }
    }

    @Override
    protected DocumentCategory getMiscellaneousOrOtherDocumentCategory() {
        return DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_3_MISCELLANEOUS_OR_OTHER;
    }

    @Override
    protected DocumentCategory getPensionPlanDocumentCategory() {
        return DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_3_PENSION_PLAN;
    }

    @Override
    protected DocumentCategory getCertificatesOfServiceDocumentCategory() {
        return DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_3_CERTIFICATES_OF_SERVICE;
    }

    @Override
    protected DocumentCategory getHearingDocumentsCategory() {
        return DocumentCategory.HEARING_DOCUMENTS_INTERVENER_3;
    }

    @Override
    protected DocumentCategory getFdrDocumentsAndFdrBundleWithoutPrejudiceOffersCategory() {
        return DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_3_WITHOUT_PREJUDICE_OFFERS;
    }

    @Override
    protected DocumentCategory getDefaultPartyCategory() {
        return DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_3;
    }
}
