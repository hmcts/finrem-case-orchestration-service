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
    protected DocumentCategory getMiscellaneousOrOtherDocumentCategory() {
        return DocumentCategory.APPLICANT_DOCUMENTS_MISCELLANEOUS_OR_OTHER;
    }

    @Override
    protected DocumentCategory getPensionPlanDocumentCategory() {
        return DocumentCategory.APPLICANT_DOCUMENTS_PENSION_PLAN;
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
}
