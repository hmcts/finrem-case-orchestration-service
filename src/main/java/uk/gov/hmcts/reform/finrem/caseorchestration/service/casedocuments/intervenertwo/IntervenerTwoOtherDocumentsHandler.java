package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenertwo;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.OtherDocumentsHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.INTERVENER_TWO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_TWO_OTHER_COLLECTION;

@Component
public class IntervenerTwoOtherDocumentsHandler extends OtherDocumentsHandler {

    public IntervenerTwoOtherDocumentsHandler(FeatureToggleService featureToggleService) {
        super(INTERVENER_TWO_OTHER_COLLECTION, INTERVENER_TWO, featureToggleService);
    }


    @Override
    protected DocumentCategory getMiscellaneousOrOtherDocumentCategory() {
        return DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_2_MISCELLANEOUS_OR_OTHER;
    }

    @Override
    protected DocumentCategory getPensionPlanDocumentCategory() {
        return DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_2_PENSION_PLAN;
    }

    @Override
    protected DocumentCategory getCertificatesOfServiceDocumentCategory() {
        return DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_2_CERTIFICATES_OF_SERVICE;
    }

    @Override
    protected DocumentCategory getHearingDocumentsCategory() {
        return DocumentCategory.HEARING_DOCUMENTS_INTERVENER_2;
    }

    @Override
    protected DocumentCategory getFdrDocumentsAndFdrBundleWithoutPrejudiceOffersCategory() {
        return DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_2_WITHOUT_PREJUDICE_OFFERS;
    }

    @Override
    protected DocumentCategory getDefaultPartyCategory() {
        return DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_2;
    }

}
