package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory;

import org.apache.commons.collections.CollectionUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;

import java.util.List;

public class CUIDocumentsCategoriser extends DocumentCategoriser {

    public enum Party {
        APPLICANT,
        RESPONDENT
    }

    private final Party party;

    public CUIDocumentsCategoriser(FeatureToggleService service, Party party) {
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

            case FAMILY_MEDIATION_INFORMATION_AND_ASSESSMENT_MEETING_MIAM_FORM_FM1 ->
                DocumentCategory.APPLICATIONS_MAIN_APPLICATION.getDocumentCategoryId();
            case POINTS_OF_CLAIM_DEFENCE -> getPointsOfClaimDefenceCategory();
            case STATEMENT_OF_POSITION_ON_NON_COURT_DISPUTE_RESOLUTION_NCDR_FORM_FM5 -> getFm5FolderCategory(doc);
            case FINANCIAL_STATEMENT_FORM_E_E1_OR_E2,
                 ATTACHMENTS_TO_FORM_E -> getFromECategory();
            case ESTIMATE_OF_COSTS_INCURRED_FORM_H,
                 STATEMENT_OF_COSTS_FORM_H1,
                 STATEMENT_OF_COSTS_SUMMARY_ASSESSMENT_FORM_N260 -> getStatementOfCostsCategory();
            case CERTIFICATE_OF_SERVICE_FORM_FP6 -> getCertificateOfServiceCategory();
            case RESPONSE_TO_THE_NOTICE_OF_FIRST_APPOINTMENT_FORM_G -> getFormGCategory();
            case SCHEDULE_OF_DEFICIENCIES -> getScheduleOfDeficienciesCategory(doc);
            case CASE_SUMMARY -> getCaseSummaryCategory(doc);
            case POSITION_STATEMENT -> getPositionStatementCategory(doc);
            case CHRONOLOGY -> getChronologyCategory(doc);
            case STATEMENT_OF_ISSUES -> getStatementOfIssuesCategory(doc);
            case DIVORCE_APPLICATION_PETITION ->
                DocumentCategory.DIVORCE_DOCUMENTS_APPLICATION_OR_PETITION.getDocumentCategoryId();
            case DIVORCE_CONDITIONAL_ORDER_DECREE_NISI ->
                DocumentCategory.DIVORCE_DOCUMENTS_CONDITIONAL_ORDER_OR_DECREE_NISI.getDocumentCategoryId();
            case DIVORCE_FINAL_ORDER_DECREE_ABSOLUTE ->
                DocumentCategory.DIVORCE_DOCUMENTS_FINAL_ORDER_OR_DECREE_ABSOLUTE.getDocumentCategoryId();
            case COMPOSITE_CASE_SUMMARY_FORM_ES1 -> getEs1Category(doc);
            case COMPOSITE_SCHEDULE_OF_ASSETS_AND_INCOME_FORM_ES2 -> getEs2Category(doc);
            case MARKET_APPRAISAL_OR_VALUATION_OF_FAMILY_HOME -> getMarketAppraisalCategory(doc);
            case HOUSING_NEEDS_PROPERTY_PARTICULARS,
                 POTENTIAL_BORROWING_CAPACITY_MORTGAGE_CAPACITIES -> getHousingParticularsCategory(doc);
            case OPEN_OFFERS -> getOpenOffersCategory(doc);
            case QUESTIONNAIRE_REQUEST_FOR_FURTHER_DOCUMENTS,
                 SUPPLEMENTAL_QUESTIONNAIRE -> getQuestionnaireCategory(doc);
            case SECTION_25_STATEMENT -> getS25Category(doc);
            case WITNESS_STATEMENT -> getWitnessStatementCategory(doc);
            case WITHOUT_PREJUDICE_OFFERS_FOR_SETTLEMENT -> getWithoutPrejudiceCategory();
            case PENSION_REPORT_EXPERT_REPORT,
                 MEDICAL_REPORT -> getExportReportCategory(doc);
            case HEARING_BUNDLE -> getHeringBundleCategory(doc);
            case FDR_BUNDLE -> DocumentCategory.FDR_BUNDLE.getDocumentCategoryId();
            case PRE_HEARING_DRAFT_ORDER -> getPreHearingDraftOrderCategory(doc);
            case REPLY_TO_QUESTIONNAIRE,
                 REPLY_TO_SCHEDULE_OF_DEFICIENCIES_OR_SUPPLEMENTAL_QUESTIONNAIRES,
                 REPLY_TO_QUESTIONNAIRE_SUPPORTING_DOCUMENTS,
                 REPLY_TO_SCHEDULE_OF_DEFICIENCIES_OR_SUPPLEMENTAL_QUESTIONNAIRES_SUPPORTING_DOCUMENTS -> getReplyToQuestionaireCategory();
            case UPDATING_DISCLOSURE,
                 BANK_STATEMENTS,
                 PAYSLIPS,
                 P60,
                 P45,
                 DEBT_STATEMENT,
                 LIST_OF_ASSETS,
                 LOAN_STATEMENT,
                 CAR_INSURANCE_LOAN_STATEMENT ,
                 PERSONAL_SELLING_SIGHT_STATEMENT,
                 SELF_ASSESSMENT_TAX_FORMS,
                 UNIVERSAL_CREDIT_STATEMENT,
                 MORTGAGE_STATEMENTS_FOR_FAMILY_HOME,
                 MORTGAGE_STATEMENTS_FOR_OTHER_PROPERTIES,
                 INVESTMENT_STATEMENTS,
                 BUSINESS_ACCOUNTS,
                 P11D,
                 TAX_ASSESSMENTS,
                 INCOME_EVIDENCE,
                 PENSION_STATEMENT,
                 OTHER_PROPERTY_VALUATION,
                 LIFE_INSURANCE_INCLUDING_ENDOWMENT_POLICIES,
                 BUSINESS_VALUATION,
                 MANAGEMENT_ACCOUNTS-> getUpdatingDisclosureCategory();
            default -> null;
        };

        setCategory(doc, category);
    }

    private String getReplyToQuestionaireCategory() {
        return party == Party.APPLICANT
            ? DocumentCategory.APPLICANT_DOCUMENTS_REPLIES_TO_QUESTIONNAIRE.getDocumentCategoryId()
            : DocumentCategory.RESPONDENT_DOCUMENTS_REPLIES_TO_QUESTIONNAIRE.getDocumentCategoryId();
    }

    private static String getHeringBundleCategory(CitizenUploadDocument doc) {
        return doc.getIsFdr().isYes()
            ? DocumentCategory.FDR_BUNDLE.getDocumentCategoryId()
            : DocumentCategory.HEARING_BUNDLE.getDocumentCategoryId();
    }

    private static String getExportReportCategory(CitizenUploadDocument doc) {
        return doc.getIsFdr().isYes()
            ? DocumentCategory.FDR_REPORTS.getDocumentCategoryId()
            : DocumentCategory.REPORTS.getDocumentCategoryId();
    }

    private String getFormGCategory() {
        return party == Party.APPLICANT
            ? DocumentCategory.APPLICANT_DOCUMENTS_FORM_G.getDocumentCategoryId()
            : DocumentCategory.RESPONDENT_DOCUMENTS_FORM_G.getDocumentCategoryId();
    }

    private String getCertificateOfServiceCategory() {
        return party == Party.APPLICANT
            ? DocumentCategory.APPLICANT_DOCUMENTS_CERTIFICATES_OF_SERVICE.getDocumentCategoryId()
            : DocumentCategory.RESPONDENT_DOCUMENTS_CERTIFICATES_OF_SERVICE.getDocumentCategoryId();
    }

    private String getStatementOfCostsCategory() {
        return party == Party.APPLICANT
            ? DocumentCategory.HEARING_DOCUMENTS_APPLICANT_COSTS_FORM_H_OR_FORM_H1_OR_FORM_N260.getDocumentCategoryId()
            : DocumentCategory.HEARING_DOCUMENTS_RESPONDENT_COSTS_FORM_H_OR_FORM_H1_OR_FORM_N260.getDocumentCategoryId();
    }

    private String getFromECategory() {
        return party == Party.APPLICANT
            ? DocumentCategory.APPLICANT_DOCUMENTS_FORM_E.getDocumentCategoryId()
            : DocumentCategory.RESPONDENT_DOCUMENTS_FORM_E.getDocumentCategoryId();
    }

    private String getFm5FolderCategory(CitizenUploadDocument document) {
        if (document.getIsFdr().isYes()) {
            return party == Party.APPLICANT
                ? DocumentCategory.HEARING_DOCUMENTS_APPLICANT_FM5.getDocumentCategoryId()
                : DocumentCategory.HEARING_DOCUMENTS_RESPONDENT_FM5.getDocumentCategoryId();
        }
        return party == Party.APPLICANT
            ? DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_OTHER.getDocumentCategoryId()
            : DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_OTHER.getDocumentCategoryId();
    }

    private String getPointsOfClaimDefenceCategory() {
        return party == Party.APPLICANT
            ? DocumentCategory.APPLICANT_DOCUMENTS_POINTS_OF_CLAIM_OR_DEFENCE.getDocumentCategoryId()
            : DocumentCategory.RESPONDENT_DOCUMENTS_POINTS_OF_CLAIM_OR_DEFENCE.getDocumentCategoryId();
    }

    private String getScheduleOfDeficienciesCategory(CitizenUploadDocument doc) {
        if (doc.getIsFdr().isYes()) {
            return party == Party.APPLICANT
                ? DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_OTHER.getDocumentCategoryId()
                : DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_OTHER.getDocumentCategoryId();
        }
        return party == Party.APPLICANT
            ? DocumentCategory.HEARING_DOCUMENTS_APPLICANT_REPLIES_TO_QUESTIONNAIRE.getDocumentCategoryId()
            : DocumentCategory.HEARING_DOCUMENTS_RESPONDENT_REPLIES_TO_QUESTIONNAIRE.getDocumentCategoryId();
    }

    private String getCaseSummaryCategory(CitizenUploadDocument doc) {
        if (doc.getIsFdr().isYes()) {
            return party == Party.APPLICANT
                ? DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_OTHER.getDocumentCategoryId()
                : DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_OTHER.getDocumentCategoryId();
        }
        return party == Party.APPLICANT
            ? DocumentCategory.HEARING_DOCUMENTS_APPLICANT_CASE_SUMMARY.getDocumentCategoryId()
            : DocumentCategory.HEARING_DOCUMENTS_RESPONDENT_CASE_SUMMARY.getDocumentCategoryId();
    }

    private String getPositionStatementCategory(CitizenUploadDocument doc) {
        if (doc.getIsFdr().isYes()) {
            return party == Party.APPLICANT
                ? DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_POSITION_STATEMENTS.getDocumentCategoryId()
                : DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_POSITION_STATEMENTS.getDocumentCategoryId();
        }
        return party == Party.APPLICANT
            ? DocumentCategory.HEARING_DOCUMENTS_APPLICANT_POSITION_STATEMENT.getDocumentCategoryId()
            : DocumentCategory.HEARING_DOCUMENTS_RESPONDENT_POSITION_STATEMENT.getDocumentCategoryId();
    }

    private String getChronologyCategory(CitizenUploadDocument doc) {
        if (doc.getIsFdr().isYes()) {
            return DocumentCategory.FDR_JOINT_DOCUMENTS_CHRONOLOGY.getDocumentCategoryId();
        }
        return party == Party.APPLICANT
            ? DocumentCategory.HEARING_DOCUMENTS_APPLICANT_CHRONOLOGY.getDocumentCategoryId()
            : DocumentCategory.HEARING_DOCUMENTS_RESPONDENT_CHRONOLOGY.getDocumentCategoryId();
    }

    private String getStatementOfIssuesCategory(CitizenUploadDocument doc) {
        if (doc.getIsFdr().isYes()) {
            return party == Party.APPLICANT
                ? DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_POSITION_STATEMENTS.getDocumentCategoryId()
                : DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_POSITION_STATEMENTS.getDocumentCategoryId();
        }
        return party == Party.APPLICANT
            ? DocumentCategory.HEARING_DOCUMENTS_APPLICANT_CONCISE_STATEMENT_OF_ISSUES.getDocumentCategoryId()
            : DocumentCategory.HEARING_DOCUMENTS_RESPONDENT_CONCISE_STATEMENT_OF_ISSUES.getDocumentCategoryId();
    }

    private String getEs1Category(CitizenUploadDocument doc) {
        if (doc.getIsFdr().isYes()) {
            return DocumentCategory.FDR_JOINT_DOCUMENTS_ES1.getDocumentCategoryId();
        }
        return party == Party.APPLICANT
            ? DocumentCategory.HEARING_DOCUMENTS_APPLICANT_ES1.getDocumentCategoryId()
            : DocumentCategory.HEARING_DOCUMENTS_RESPONDENT_ES1.getDocumentCategoryId();
    }

    private String getEs2Category(CitizenUploadDocument doc) {
        if (doc.getIsFdr().isYes()) {
            return DocumentCategory.FDR_JOINT_DOCUMENTS_ES2.getDocumentCategoryId();
        }
        return party == Party.APPLICANT
            ? DocumentCategory.HEARING_DOCUMENTS_APPLICANT_ES2.getDocumentCategoryId()
            : DocumentCategory.HEARING_DOCUMENTS_RESPONDENT_ES2.getDocumentCategoryId();
    }

    private String getMarketAppraisalCategory(CitizenUploadDocument doc) {
        if (doc.getIsFdr().isYes()) {
            return party == Party.APPLICANT
                ? DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_OTHER.getDocumentCategoryId()
                : DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_OTHER.getDocumentCategoryId();
        }
        return party == Party.APPLICANT
            ? DocumentCategory.APPLICANT_MORTGAGE_CAPACITIES_OR_MARKET_APPRAISAL.getDocumentCategoryId()
            : DocumentCategory.RESPONDENT_MORTGAGE_CAPACITIES_OR_MARKET_APPRAISAL.getDocumentCategoryId();
    }

    private String getHousingParticularsCategory(CitizenUploadDocument doc) {
        if (doc.getIsFdr().isYes()) {
            return party == Party.APPLICANT
                ? DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_OTHER.getDocumentCategoryId()
                : DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_OTHER.getDocumentCategoryId();
        }
        return party == Party.APPLICANT
            ? DocumentCategory.APPLICANT_MORTGAGE_CAPACITIES_OR_HOUSING_PARTICULARS.getDocumentCategoryId()
            : DocumentCategory.RESPONDENT_MORTGAGE_CAPACITIES_OR_HOUSING_PARTICULARS.getDocumentCategoryId();
    }

    private String getOpenOffersCategory(CitizenUploadDocument doc) {
        if (doc.getIsFdr().isYes()) {
            return party == Party.APPLICANT
                ? DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_OTHER.getDocumentCategoryId()
                : DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_OTHER.getDocumentCategoryId();
        }
        return party == Party.APPLICANT
            ? DocumentCategory.APPLICANT_DOCUMENTS_OPEN_OFFERS.getDocumentCategoryId()
            : DocumentCategory.RESPONDENT_DOCUMENTS_OPEN_OFFERS.getDocumentCategoryId();
    }

    private String getQuestionnaireCategory(CitizenUploadDocument doc) {
        if (doc.getIsFdr().isYes()) {
            return party == Party.APPLICANT
                ? DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_OTHER.getDocumentCategoryId()
                : DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_OTHER.getDocumentCategoryId();
        }
        return party == Party.APPLICANT
            ? DocumentCategory.HEARING_DOCUMENTS_APPLICANT_QUESTIONNAIRES.getDocumentCategoryId()
            : DocumentCategory.HEARING_DOCUMENTS_RESPONDENT_QUESTIONNAIRES.getDocumentCategoryId();
    }

    private String getS25Category(CitizenUploadDocument doc) {
        if (doc.getIsFdr().isYes()) {
            return party == Party.APPLICANT
                ? DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_OTHER.getDocumentCategoryId()
                : DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_OTHER.getDocumentCategoryId();
        }
        return party == Party.APPLICANT
            ? DocumentCategory.APPLICANT_DOCUMENTS_S25_STATEMENT.getDocumentCategoryId()
            : DocumentCategory.RESPONDENT_DOCUMENTS_S25_STATEMENT.getDocumentCategoryId();
    }

    private String getWitnessStatementCategory(CitizenUploadDocument doc) {
        if (doc.getIsFdr().isYes()) {
            return party == Party.APPLICANT
                ? DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_OTHER.getDocumentCategoryId()
                : DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_OTHER.getDocumentCategoryId();
        }
        return party == Party.APPLICANT
            ? DocumentCategory.APPLICANT_DOCUMENTS_WITNESS_STATEMENTS.getDocumentCategoryId()
            : DocumentCategory.RESPONDENT_DOCUMENTS_WITNESS_STATEMENTS.getDocumentCategoryId();
    }

    private String getWithoutPrejudiceCategory() {
        return party == Party.APPLICANT
            ? DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_WITHOUT_PREJUDICE_OFFERS.getDocumentCategoryId()
            : DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_WITHOUT_PREJUDICE_OFFERS.getDocumentCategoryId();
    }

    private String getPreHearingDraftOrderCategory(CitizenUploadDocument doc) {
        if (doc.getIsFdr().isYes()) {
            return party == Party.APPLICANT
                ? DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_DRAFT_ORDER.getDocumentCategoryId()
                : DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_DRAFT_ORDER.getDocumentCategoryId();
        }
        return party == Party.APPLICANT
            ? DocumentCategory.HEARING_DOCUMENTS_APPLICANT_PRE_HEARING_DRAFT_ORDER.getDocumentCategoryId()
            : DocumentCategory.HEARING_DOCUMENTS_RESPONDENT_PRE_HEARING_DRAFT_ORDER.getDocumentCategoryId();
    }

    private String getUpdatingDisclosureCategory() {
        return party == Party.APPLICANT
            ? DocumentCategory.APPLICANT_DOCUMENTS_UPDATING_DISCLOSURE.getDocumentCategoryId()
            : DocumentCategory.RESPONDENT_DOCUMENTS_UPDATING_DISCLOSURE.getDocumentCategoryId();
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
