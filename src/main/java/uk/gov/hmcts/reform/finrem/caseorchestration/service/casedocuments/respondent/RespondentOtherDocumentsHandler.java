package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.OtherDocumentsHandler;

@Component
public class RespondentOtherDocumentsHandler extends OtherDocumentsHandler {

    public RespondentOtherDocumentsHandler(FeatureToggleService featureToggleService) {
        super(CaseDocumentCollectionType.RESP_OTHER_COLLECTION,
            CaseDocumentParty.RESPONDENT, featureToggleService);
    }

    @Override
    protected DocumentCategory getMiscellaneousOrOtherDocumentCategory() {
        return DocumentCategory.RESPONDENT_DOCUMENTS_MISCELLANEOUS_OR_OTHER;
    }

    @Override
    protected DocumentCategory getPensionPlanDocumentCategory() {
        return DocumentCategory.RESPONDENT_DOCUMENTS_PENSION_PLAN;
    }


    @Override
    protected DocumentCategory getCertificatesOfServiceDocumentCategory() {
        return DocumentCategory.RESPONDENT_DOCUMENTS_CERTIFICATES_OF_SERVICE;
    }

    @Override
    protected DocumentCategory getHearingDocumentsCategoryES1() {
        return DocumentCategory.HEARING_DOCUMENTS_RESPONDENT_ES1;
    }

    @Override
    protected DocumentCategory getHearingDocumentsCategoryES2() {
        return DocumentCategory.HEARING_DOCUMENTS_RESPONDENT_ES2;
    }

    @Override
    protected DocumentCategory getHearingDocumentsCategoryMortgageCapacities() {
        return DocumentCategory.HEARING_DOCUMENTS_RESPONDENT_MORTGAGE_CAPACITIES;
    }

    @Override
    protected DocumentCategory getFdrDocumentsAndFdrBundleWithoutPrejudiceOffersCategory() {
        return DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_WITHOUT_PREJUDICE_OFFERS;
    }

    @Override
    protected DocumentCategory getHouseParticularsDocumentCategory() {
        return DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_4_HOUSING_PARTICULARS;
    }

    @Override
    protected DocumentCategory getPreHearingDraftOrderDocumentCategory() {
        return DocumentCategory.HEARING_DOCUMENTS_INTERVENER_4_PRE_HEARING_DRAFT_ORDER;
    }

    @Override
    protected DocumentCategory getDefaultPartyCategory() {
        return DocumentCategory.RESPONDENT_DOCUMENTS;
    }
}
