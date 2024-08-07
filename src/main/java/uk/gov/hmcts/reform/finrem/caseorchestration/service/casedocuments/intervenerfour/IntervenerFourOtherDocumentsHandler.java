package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerfour;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.OtherDocumentsHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.INTERVENER_FOUR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_FOUR_OTHER_COLLECTION;

@Component
public class IntervenerFourOtherDocumentsHandler extends OtherDocumentsHandler {

    public IntervenerFourOtherDocumentsHandler(FeatureToggleService featureToggleService) {
        super(INTERVENER_FOUR_OTHER_COLLECTION, INTERVENER_FOUR, featureToggleService);
    }


    @Override
    protected DocumentCategory getOtherDocumentCategory() {
        return DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_4_OTHER;
    }

    @Override
    protected DocumentCategory getCertificatesOfServiceDocumentCategory() {
        return DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_4_CERTIFICATES_OF_SERVICE;
    }

    @Override
    protected DocumentCategory getHearingDocumentsCategoryES1() {
        return DocumentCategory.HEARING_DOCUMENTS_INTERVENER_4_ES1;
    }

    @Override
    protected DocumentCategory getHearingDocumentsCategoryES2() {
        return DocumentCategory.HEARING_DOCUMENTS_INTERVENER_4_ES2;
    }

    @Override
    protected DocumentCategory getDefaultPartyCategory() {
        return DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_4;
    }

    @Override
    protected DocumentCategory getPreHearingDraftOrderDocumentCategory() {
        return DocumentCategory.HEARING_DOCUMENTS_INTERVENER_4_PRE_HEARING_DRAFT_ORDER;
    }

    @Override
    protected DocumentCategory getPointsOfClaimOrDefenceDocumentCategory() {
        return DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_4_POINTS_OF_CLAIM_OR_DEFENCE;
    }

    @Override
    protected DocumentCategory getHearingDocumentsCategoryFM5() {
        return DocumentCategory.HEARING_DOCUMENTS_INTERVENER_4_FM5;
    }

    @Override
    protected DocumentCategory getPartyDocumentsCategoryMortgageCapacities() {
        return DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_4;
    }

    @Override
    protected DocumentCategory getFdrDocumentsAndFdrBundleWithoutPrejudiceOffersCategory() {
        return DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_4_WITHOUT_PREJUDICE_OFFERS;
    }

}
