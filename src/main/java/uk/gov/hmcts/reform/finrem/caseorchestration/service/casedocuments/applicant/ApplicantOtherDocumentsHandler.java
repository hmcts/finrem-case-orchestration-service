package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.OtherDocumentsHandler;

@Component
public class ApplicantOtherDocumentsHandler extends OtherDocumentsHandler {

    public ApplicantOtherDocumentsHandler(FeatureToggleService featureToggleService) {
        super(CaseDocumentCollectionType.APP_OTHER_COLLECTION,
            CaseDocumentParty.APPLICANT, featureToggleService);
    }

    @Override
    protected DocumentCategory getOtherDocumentCategory() {
        return DocumentCategory.APPLICANT_DOCUMENTS_MISCELLANEOUS_OR_OTHER;
    }

    @Override
    protected DocumentCategory getCertificatesOfServiceDocumentCategory() {
        return DocumentCategory.APPLICANT_DOCUMENTS_CERTIFICATES_OF_SERVICE;
    }

    @Override
    protected DocumentCategory getHearingDocumentsCategoryES1() {
        return DocumentCategory.HEARING_DOCUMENTS_APPLICANT_ES1;
    }

    @Override
    protected DocumentCategory getHearingDocumentsCategoryES2() {
        return DocumentCategory.HEARING_DOCUMENTS_APPLICANT_ES2;
    }

    @Override
    protected DocumentCategory getPartyDocumentsCategoryMortgageCapacities() {
        return DocumentCategory.APPLICANT_MORTGAGE_CAPACITIES_OR_HOUSING_PARTICULARS;
    }

    @Override
    protected DocumentCategory getDefaultPartyCategory() {
        return DocumentCategory.APPLICANT_DOCUMENTS;
    }

    @Override
    protected DocumentCategory getPreHearingDraftOrderDocumentCategory() {
        return DocumentCategory.HEARING_DOCUMENTS_APPLICANT_PRE_HEARING_DRAFT_ORDER;
    }

    @Override
    protected DocumentCategory getPointsOfClaimOrDefenceDocumentCategory() {
        return DocumentCategory.APPLICANT_DOCUMENTS_POINTS_OF_CLAIM_OR_DEFENCE;
    }

    @Override
    protected DocumentCategory getHearingDocumentsCategoryFM5() {
        return DocumentCategory.HEARING_DOCUMENTS_APPLICANT_FM5;
    }

    @Override
    protected DocumentCategory getFdrDocumentsAndFdrBundleWithoutPrejudiceOffersCategory() {
        return DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_WITHOUT_PREJUDICE_OFFERS;
    }
}
