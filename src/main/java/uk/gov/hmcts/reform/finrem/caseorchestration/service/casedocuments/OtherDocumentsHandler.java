package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;

import java.util.List;

public abstract class OtherDocumentsHandler extends PartyDocumentsHandler {

    private static final List<CaseDocumentType> otherDocuments = List.of(
        CaseDocumentType.OTHER,
        CaseDocumentType.FORM_B,
        CaseDocumentType.FORM_F,
        CaseDocumentType.CARE_PLAN,
        CaseDocumentType.PENSION_PLAN,
        CaseDocumentType.CERTIFICATES_OF_SERVICE,
        CaseDocumentType.ES1,
        CaseDocumentType.ES2,
        CaseDocumentType.HOUSING_PARTICULARS,
        CaseDocumentType.MORTGAGE_CAPACITIES,
        CaseDocumentType.PRE_HEARING_DRAFT_ORDER,
        CaseDocumentType.WITHOUT_PREJUDICE_OFFERS,
        CaseDocumentType.PENSION_REPORT,
        CaseDocumentType.BILL_OF_COSTS,
        CaseDocumentType.POINTS_OF_CLAIM_OR_DEFENCE,
        CaseDocumentType.FM5
    );

    protected OtherDocumentsHandler(CaseDocumentCollectionType caseDocumentCollectionType,
                                    CaseDocumentParty party, FeatureToggleService featureToggleService) {
        super(caseDocumentCollectionType, party, featureToggleService);
    }

    @Override
    protected boolean canHandleDocument(UploadCaseDocument uploadCaseDocument) {
        CaseDocumentType caseDocumentType = uploadCaseDocument.getCaseDocumentType();
        return uploadCaseDocument.getCaseDocumentFdr().equals(YesOrNo.NO)
            && (otherDocuments.contains(caseDocumentType));
    }

    @Override
    public DocumentCategory getDocumentCategoryFromDocumentType(CaseDocumentType caseDocumentType, CaseDocumentParty caseDocumentParty) {
        switch (caseDocumentType) {
            case OTHER -> {
                return getOtherDocumentCategory();
            }
            case PENSION_PLAN -> {
                return DocumentCategory.REPORTS;
            }
            case FORM_B, FORM_F, CARE_PLAN -> {
                return DocumentCategory.ADMINISTRATIVE_DOCUMENTS_OTHER;
            }
            case CERTIFICATES_OF_SERVICE -> {
                return getCertificatesOfServiceDocumentCategory();
            }
            case ES1 -> {
                return getHearingDocumentsCategoryES1();
            }
            case ES2 -> {
                return getHearingDocumentsCategoryES2();
            }
            case MORTGAGE_CAPACITIES, HOUSING_PARTICULARS -> {
                return getPartyDocumentsCategoryMortgageCapacities();
            }
            case WITHOUT_PREJUDICE_OFFERS -> {
                return getFdrDocumentsAndFdrBundleWithoutPrejudiceOffersCategory();
            }
            case PENSION_REPORT -> {
                return DocumentCategory.REPORTS;
            }
            case PRE_HEARING_DRAFT_ORDER -> {
                return getPreHearingDraftOrderDocumentCategory();
            }
            case POINTS_OF_CLAIM_OR_DEFENCE -> {
                return getPointsOfClaimOrDefenceDocumentCategory();
            }
            case FM5 -> {
                return getHearingDocumentsCategoryFM5();
            }
            default -> {
                return getDefaultPartyCategory();
            }
        }

    }

    protected abstract DocumentCategory getOtherDocumentCategory();

    protected abstract DocumentCategory getCertificatesOfServiceDocumentCategory();

    protected abstract DocumentCategory getHearingDocumentsCategoryES1();

    protected abstract DocumentCategory getHearingDocumentsCategoryES2();

    protected abstract DocumentCategory getPartyDocumentsCategoryMortgageCapacities();

    protected abstract DocumentCategory getFdrDocumentsAndFdrBundleWithoutPrejudiceOffersCategory();

    protected abstract DocumentCategory getDefaultPartyCategory();

    protected abstract DocumentCategory getPreHearingDraftOrderDocumentCategory();

    protected abstract DocumentCategory getPointsOfClaimOrDefenceDocumentCategory();

    protected abstract DocumentCategory getHearingDocumentsCategoryFM5();

}
