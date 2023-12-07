package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerone;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.OtherDocumentsHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.INTERVENER_ONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_ONE_OTHER_COLLECTION;

@Component
public class IntervenerOneOtherDocumentsHandler extends OtherDocumentsHandler {

    public IntervenerOneOtherDocumentsHandler(FeatureToggleService featureToggleService) {
        super(INTERVENER_ONE_OTHER_COLLECTION, INTERVENER_ONE, featureToggleService);
    }

    @Override
    protected DocumentCategory getMiscellaneousOrOtherDocumentCategory() {
        return DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_1_MISCELLANEOUS_OR_OTHER;
    }

    @Override
    protected DocumentCategory getPensionPlanDocumentCategory() {
        return DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_1_PENSION_PLAN;
    }

    @Override
    protected DocumentCategory getCertificatesOfServiceDocumentCategory() {
        return DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_1_CERTIFICATES_OF_SERVICE;
    }

    @Override
    protected DocumentCategory getHearingDocumentsCategory() {
        return DocumentCategory.HEARING_DOCUMENTS_INTERVENER_1;
    }

    @Override
    protected DocumentCategory getFdrDocumentsAndFdrBundleWithoutPrejudiceOffersCategory() {
        return DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_1_WITHOUT_PREJUDICE_OFFERS;
    }

    @Override
    protected DocumentCategory getDefaultPartyCategory() {
        return DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_1;
    }
}
