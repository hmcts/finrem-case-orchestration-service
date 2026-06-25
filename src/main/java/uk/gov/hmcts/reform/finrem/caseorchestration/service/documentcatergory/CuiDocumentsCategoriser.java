package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory;

import org.apache.commons.collections.CollectionUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;

import java.util.List;

public class CuiDocumentsCategoriser extends DocumentCategoriser {

    public enum Party {
        APPLICANT,
        RESPONDENT
    }

    private final Party party;

    public CuiDocumentsCategoriser(FeatureToggleService service, Party party) {
        super(service);
        this.party = party;
    }

    @Override
    protected void categoriseDocuments(FinremCaseData caseData) {

        List<CitizenDocumentCollection> documents =
            party == Party.APPLICANT
                ? caseData.getCitizenDocumentWrapper().getCitizenApplicantDocument()
                : caseData.getCitizenDocumentWrapper().getCitizenRespondentDocument();

        if (CollectionUtils.isEmpty(documents)) {
            return;
        }

        for (CitizenDocumentCollection collection : documents) {
            applyCategory(collection.getValue());
        }
    }

    private void applyCategory(CitizenUploadDocument doc) {
        if (!isValid(doc)) {
            return;
        }

        String category = switch (doc.getDocumentType()) {

            case POINTS_OF_CLAIM_DEFENCE -> pointsOfClaimDefence(doc);
            case STATEMENT_OF_POSITION_ON_NON_COURT_DISPUTE_RESOLUTION_NCDR_FORM_FM5 -> party == Party.APPLICANT
                ? DocumentCategory.HEARING_DOCUMENTS_APPLICANT_FM5.getDocumentCategoryId()
                : DocumentCategory.HEARING_DOCUMENTS_RESPONDENT_FM5.getDocumentCategoryId();
            case FINANCIAL_STATEMENT_FORM_E_E1_OR_E2 -> party == Party.APPLICANT
                ? DocumentCategory.APPLICANT_DOCUMENTS_FORM_E.getDocumentCategoryId()
                : DocumentCategory.RESPONDENT_DOCUMENTS_FORM_E.getDocumentCategoryId();
            case ESTIMATE_OF_COSTS_INCURRED_FORM_H, STATEMENT_OF_COSTS_FORM_H1, STATEMENT_OF_COSTS_SUMMARY_ASSESSMENT_FORM_N260 ->
                party == Party.APPLICANT
                    ? DocumentCategory.HEARING_DOCUMENTS_APPLICANT_COSTS_FORM_H_OR_FORM_H1_OR_FORM_N260.getDocumentCategoryId()
                    : DocumentCategory.HEARING_DOCUMENTS_RESPONDENT_COSTS_FORM_H_OR_FORM_H1_OR_FORM_N260.getDocumentCategoryId();
            case CERTIFICATE_OF_SERVICE_FORM_FP6 -> party == Party.APPLICANT
                ? DocumentCategory.APPLICANT_DOCUMENTS_CERTIFICATES_OF_SERVICE.getDocumentCategoryId()
                : DocumentCategory.RESPONDENT_DOCUMENTS_CERTIFICATES_OF_SERVICE.getDocumentCategoryId();
            case RESPONSE_TO_THE_NOTICE_OF_FIRST_APPOINTMENT_FORM_G -> party == Party.APPLICANT
                ? DocumentCategory.APPLICANT_DOCUMENTS_FORM_G.getDocumentCategoryId()
                : DocumentCategory.RESPONDENT_DOCUMENTS_FORM_G.getDocumentCategoryId();
            case SCHEDULE_OF_DEFICIENCIES -> resolveScheduleOfDeficiencies(doc);
            case CASE_SUMMARY -> resolveCaseSummary(doc);
            case POSITION_STATEMENT -> resolvePositionStatement(doc);
            case CHRONOLOGY -> resolveChronology(doc);
            case STATEMENT_OF_ISSUES -> resolveStatementOfIssues(doc);
            case DIVORCE_APPLICATION_PETITION -> DocumentCategory.DIVORCE_DOCUMENTS_APPLICATION_OR_PETITION.getDocumentCategoryId();
            case DIVORCE_CONDITIONAL_ORDER_DECREE_NISI -> DocumentCategory.DIVORCE_DOCUMENTS_CONDITIONAL_ORDER_OR_DECREE_NISI.getDocumentCategoryId();
            case DIVORCE_FINAL_ORDER_DECREE_ABSOLUTE -> DocumentCategory.DIVORCE_DOCUMENTS_FINAL_ORDER_OR_DECREE_ABSOLUTE.getDocumentCategoryId();
            case COMPOSITE_CASE_SUMMARY_FORM_ES1 -> resolveES1(doc);
            case COMPOSITE_SCHEDULE_OF_ASSETS_AND_INCOME_FORM_ES2 -> resolveES2(doc);
            case MARKET_APPRAISAL_OR_VALUATION_OF_FAMILY_HOME -> resolveMarketAppraisal(doc);
            case HOUSING_NEEDS_PROPERTY_PARTICULARS, POTENTIAL_BORROWING_CAPACITY_MORTGAGE_CAPACITIES -> resolveHousingParticulars(doc);
            case OPEN_OFFERS -> resolveOpenOffers(doc);
            case QUESTIONNAIRE_REQUEST_FOR_FURTHER_DOCUMENTS, SUPPLEMENTAL_QUESTIONNAIRE -> resolveQuestionnaire(doc);
            case SECTION_25_STATEMENT -> resolveS25(doc);
            case WITNESS_STATEMENT -> resolveWitnessStatement(doc);
            case WITHOUT_PREJUDICE_OFFERS_FOR_SETTLEMENT ->  resolveWithoutPrejudice(doc);
            case PENSION_REPORT_EXPERT_REPORT, MEDICAL_REPORT -> doc.getIsFdr().isYes()
                ? DocumentCategory.FDR_REPORTS.getDocumentCategoryId()
                : DocumentCategory.REPORTS.getDocumentCategoryId();
            case HEARING_BUNDLE -> doc.getIsFdr().isYes()
                ? DocumentCategory.FDR_BUNDLE.getDocumentCategoryId()
                : DocumentCategory.HEARING_BUNDLE.getDocumentCategoryId();
            case FDR_BUNDLE -> doc.getIsFdr().isYes()
                ? DocumentCategory.FDR_BUNDLE.getDocumentCategoryId()
                : null;
            case PRE_HEARING_DRAFT_ORDER ->  resolvePreHearingDraftOrder(doc);
            case REPLY_TO_QUESTIONNAIRE, REPLY_TO_SCHEDULE_OF_DEFICIENCIES_OR_SUPPLEMENTAL_QUESTIONNAIRES ->  party == Party.APPLICANT
                ? DocumentCategory.APPLICANT_DOCUMENTS_REPLIES_TO_QUESTIONNAIRE.getDocumentCategoryId()
                : DocumentCategory.RESPONDENT_DOCUMENTS_REPLIES_TO_QUESTIONNAIRE.getDocumentCategoryId();
            default -> null;
        };

        setCategory(doc, category);
    }

    private String pointsOfClaimDefence(CitizenUploadDocument doc) {
        if (doc.getIsFdr().isYes()) {
            return null;
        }
        return party == Party.APPLICANT
            ? DocumentCategory.APPLICANT_DOCUMENTS_POINTS_OF_CLAIM_OR_DEFENCE.getDocumentCategoryId()
            : DocumentCategory.RESPONDENT_DOCUMENTS_POINTS_OF_CLAIM_OR_DEFENCE.getDocumentCategoryId();
    }

    private String resolveScheduleOfDeficiencies(CitizenUploadDocument doc) {
        if (doc.getIsFdr().isYes()) {
            return party == Party.APPLICANT
                ? DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_OTHER.getDocumentCategoryId()
                : DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_OTHER.getDocumentCategoryId();
        }
        return party == Party.APPLICANT
            ? DocumentCategory.HEARING_DOCUMENTS_APPLICANT_REPLIES_TO_QUESTIONNAIRE.getDocumentCategoryId()
            : DocumentCategory.HEARING_DOCUMENTS_RESPONDENT_REPLIES_TO_QUESTIONNAIRE.getDocumentCategoryId();
    }

    private String resolveCaseSummary(CitizenUploadDocument doc) {
        if (doc.getIsFdr().isYes()) {
            return party == Party.APPLICANT
                ? DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_OTHER.getDocumentCategoryId()
                : DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_OTHER.getDocumentCategoryId();
        }
        return party == Party.APPLICANT
            ? DocumentCategory.HEARING_DOCUMENTS_APPLICANT_CASE_SUMMARY.getDocumentCategoryId()
            : DocumentCategory.HEARING_DOCUMENTS_RESPONDENT_CASE_SUMMARY.getDocumentCategoryId();
    }

    private String resolvePositionStatement(CitizenUploadDocument doc) {
        if (doc.getIsFdr().isYes()) {
            return party == Party.APPLICANT
                ? DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_POSITION_STATEMENTS.getDocumentCategoryId()
                : DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_POSITION_STATEMENTS.getDocumentCategoryId();
        }
        return party == Party.APPLICANT
            ? DocumentCategory.HEARING_DOCUMENTS_APPLICANT_POSITION_STATEMENT.getDocumentCategoryId()
            : DocumentCategory.HEARING_DOCUMENTS_RESPONDENT_POSITION_STATEMENT.getDocumentCategoryId();
    }

    private String resolveChronology(CitizenUploadDocument doc) {
        if (doc.getIsFdr().isYes()) {
            return DocumentCategory.FDR_JOINT_DOCUMENTS_CHRONOLOGY.getDocumentCategoryId();
        }
        return party == Party.APPLICANT
            ? DocumentCategory.HEARING_DOCUMENTS_APPLICANT_CHRONOLOGY.getDocumentCategoryId()
            : DocumentCategory.HEARING_DOCUMENTS_RESPONDENT_CHRONOLOGY.getDocumentCategoryId();
    }

    private String resolveStatementOfIssues(CitizenUploadDocument doc) {
        if (doc.getIsFdr().isYes()) {
            return party == Party.APPLICANT
                ? DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_POSITION_STATEMENTS.getDocumentCategoryId()
                : DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_POSITION_STATEMENTS.getDocumentCategoryId();
        }
        return party == Party.APPLICANT
            ? DocumentCategory.HEARING_DOCUMENTS_APPLICANT_CONCISE_STATEMENT_OF_ISSUES.getDocumentCategoryId()
            : DocumentCategory.HEARING_DOCUMENTS_RESPONDENT_CONCISE_STATEMENT_OF_ISSUES.getDocumentCategoryId();
    }

    private String resolveES1(CitizenUploadDocument doc) {
        if (doc.getIsFdr().isYes()) {
            return DocumentCategory.FDR_JOINT_DOCUMENTS_ES1.getDocumentCategoryId();
        }
        return party == Party.APPLICANT
            ? DocumentCategory.HEARING_DOCUMENTS_APPLICANT_ES1.getDocumentCategoryId()
            : DocumentCategory.HEARING_DOCUMENTS_RESPONDENT_ES1.getDocumentCategoryId();
    }

    private String resolveES2(CitizenUploadDocument doc) {
        if (doc.getIsFdr().isYes()) {
            return DocumentCategory.FDR_JOINT_DOCUMENTS_ES2.getDocumentCategoryId();
        }
        return party == Party.APPLICANT
            ? DocumentCategory.HEARING_DOCUMENTS_APPLICANT_ES2.getDocumentCategoryId()
            : DocumentCategory.HEARING_DOCUMENTS_RESPONDENT_ES2.getDocumentCategoryId();
    }

    private String resolveMarketAppraisal(CitizenUploadDocument doc) {
        if (doc.getIsFdr().isYes()) {
            return party == Party.APPLICANT
                ? DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_OTHER.getDocumentCategoryId()
                : DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_OTHER.getDocumentCategoryId();
        }
        return party == Party.APPLICANT
            ? DocumentCategory.APPLICANT_MORTGAGE_CAPACITIES_OR_MARKET_APPRAISAL.getDocumentCategoryId()
            : DocumentCategory.RESPONDENT_MORTGAGE_CAPACITIES_OR_MARKET_APPRAISAL.getDocumentCategoryId();
    }

    private String resolveHousingParticulars(CitizenUploadDocument doc) {
        if (doc.getIsFdr().isYes()) {
            return party == Party.APPLICANT
                ? DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_OTHER.getDocumentCategoryId()
                : DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_OTHER.getDocumentCategoryId();
        }
        return party == Party.APPLICANT
            ? DocumentCategory.APPLICANT_MORTGAGE_CAPACITIES_OR_HOUSING_PARTICULARS.getDocumentCategoryId()
            : DocumentCategory.RESPONDENT_MORTGAGE_CAPACITIES_OR_HOUSING_PARTICULARS.getDocumentCategoryId();
    }

    private String resolveQuestionnaire(CitizenUploadDocument doc) {
        if (doc.getIsFdr().isYes()) {
            return party == Party.APPLICANT
                ? DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_OTHER.getDocumentCategoryId()
                : DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_OTHER.getDocumentCategoryId();
        }
        return party == Party.APPLICANT
            ? DocumentCategory.HEARING_DOCUMENTS_APPLICANT_QUESTIONNAIRES.getDocumentCategoryId()
            : DocumentCategory.HEARING_DOCUMENTS_RESPONDENT_QUESTIONNAIRES.getDocumentCategoryId();
    }

    private String resolveS25(CitizenUploadDocument doc) {
        if (doc.getIsFdr().isYes()) {
            return party == Party.APPLICANT
                ? DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_OTHER.getDocumentCategoryId()
                : DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_OTHER.getDocumentCategoryId();
        }
        return party == Party.APPLICANT
            ? DocumentCategory.APPLICANT_DOCUMENTS_S25_STATEMENT.getDocumentCategoryId()
            : DocumentCategory.RESPONDENT_DOCUMENTS_S25_STATEMENT.getDocumentCategoryId();
    }

    private String resolveWitnessStatement(CitizenUploadDocument doc) {
        if (doc.getIsFdr().isYes()) {
            return party == Party.APPLICANT
                ? DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_OTHER.getDocumentCategoryId()
                : DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_OTHER.getDocumentCategoryId();
        }
        return party == Party.APPLICANT
            ? DocumentCategory.APPLICANT_DOCUMENTS_WITNESS_STATEMENTS.getDocumentCategoryId()
            : DocumentCategory.RESPONDENT_DOCUMENTS_WITNESS_STATEMENTS.getDocumentCategoryId();
    }

    private String resolveWithoutPrejudice(CitizenUploadDocument doc) {
        if (doc.getIsFdr().isYes()) {
            return party == Party.APPLICANT
                ? DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_WITHOUT_PREJUDICE_OFFERS.getDocumentCategoryId()
                : DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_WITHOUT_PREJUDICE_OFFERS.getDocumentCategoryId();
        }
        return null;
    }

    private String resolvePreHearingDraftOrder(CitizenUploadDocument doc) {
        if (doc.getIsFdr().isYes()) {
            return party == Party.APPLICANT
                ? DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_DRAFT_ORDER.getDocumentCategoryId()
                : DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_DRAFT_ORDER.getDocumentCategoryId();
        }
        return party == Party.APPLICANT
            ? DocumentCategory.HEARING_DOCUMENTS_APPLICANT_PRE_HEARING_DRAFT_ORDER.getDocumentCategoryId()
            : DocumentCategory.HEARING_DOCUMENTS_RESPONDENT_PRE_HEARING_DRAFT_ORDER.getDocumentCategoryId();
    }

    private String resolveOpenOffers(CitizenUploadDocument doc) {
        if (doc.getIsFdr().isYes()) {
            return party == Party.APPLICANT
                ? DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_OTHER.getDocumentCategoryId()
                : DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_OTHER.getDocumentCategoryId();
        }
        return party == Party.APPLICANT
            ? DocumentCategory.APPLICANT_DOCUMENTS_OPEN_OFFERS.getDocumentCategoryId()
            : DocumentCategory.RESPONDENT_DOCUMENTS_OPEN_OFFERS.getDocumentCategoryId();
    }

    private void setCategory(CitizenUploadDocument doc, String category) {
        CaseDocument copy = new CaseDocument(doc.getDocumentLink());
        copy.setCategoryId(category);
        doc.setDocumentLink(copy);
    }

    private boolean isValid(CitizenUploadDocument doc) {
        return doc != null && doc.getDocumentLink() != null && doc.getDocumentType() != null;
    }
}
