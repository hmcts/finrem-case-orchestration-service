package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerthree;

import org.springframework.stereotype.Component;
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
    protected DocumentCategory getHearingDocumentsCategoryES1() {
        return DocumentCategory.HEARING_DOCUMENTS_INTERVENER_3_ES1;
    }

    @Override
    protected DocumentCategory getHearingDocumentsCategoryES2() {
        return DocumentCategory.HEARING_DOCUMENTS_INTERVENER_3_ES2;
    }

    @Override
    protected DocumentCategory getPartyDocumentsCategoryMortgageCapacities() {
        return DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_3_MORTGAGE_CAPACITIES_OR_HOUSING_PARTICULARS;
    }

    @Override
    protected DocumentCategory getPreHearingDraftOrderDocumentCategory() {
        return DocumentCategory.HEARING_DOCUMENTS_INTERVENER_3_PRE_HEARING_DRAFT_ORDER;
    }

    @Override
    protected DocumentCategory getPointsOfClaimOrDefenceDocumentCategory() {
        return DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_3_POINTS_OF_CLAIM_OR_DEFENCE;
    }

    @Override
    protected DocumentCategory getHearingDocumentsCategoryFM5() {
        return DocumentCategory.HEARING_DOCUMENTS_INTERVENER_3_FM5;
    }

    @Override
    protected DocumentCategory getDefaultPartyCategory() {
        return DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_3;
    }


    @Override
    protected DocumentCategory getFdrDocumentsAndFdrBundleWithoutPrejudiceOffersCategory() {
        return DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_3_WITHOUT_PREJUDICE_OFFERS;
    }
}
