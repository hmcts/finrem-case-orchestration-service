package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenDocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CitizenUploadDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.CitizenDocumentWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;

import java.util.List;

@Service
public class CUIDocumentsCategoriser extends DocumentCategoriser {

    public CUIDocumentsCategoriser(FeatureToggleService service) {
        super(service);
    }

    @Override
    protected void categoriseDocuments(FinremCaseData caseData) {
        categorisePartyDocuments(caseData, CitizenPartyType.APPLICANT);
        categorisePartyDocuments(caseData, CitizenPartyType.RESPONDENT);
    }

    private void categorisePartyDocuments(FinremCaseData caseData, CitizenPartyType party) {
        CitizenDocumentWrapper citizenWrapper = caseData.getCitizenDocumentWrapper();
        if (citizenWrapper == null) {
            return;
        }

        List<CitizenDocumentCollectionItem> documents =
            CitizenPartyType.APPLICANT.equals(party)
                ? citizenWrapper.getUploadedCuiApplicantDocuments()
                : citizenWrapper.getUploadedCuiRespondentDocuments();

        if (CollectionUtils.isEmpty(documents)) {
            return;
        }

        for (CitizenDocumentCollectionItem collection : documents) {
            applyCategory(collection.getValue(), party);
        }
    }

    private void applyCategory(CitizenUploadDocument doc, CitizenPartyType partyType) {
        if (!isValid(doc)) {
            return;
        }

        String category = switch (doc.getDocumentType()) {
            case POINTS_OF_CLAIM_DEFENCE -> getPointsOfClaimDefenceCategory(partyType);
            case STATEMENT_OF_POSITION_ON_NON_COURT_DISPUTE_RESOLUTION_NCDR_FORM_FM5 -> getFm5FolderCategory(partyType);
            case FINANCIAL_STATEMENT_FORM_E_E1_OR_E2,
                 ATTACHMENTS_TO_FORM_E -> getFromECategory(partyType);
            case ESTIMATE_OF_COSTS_INCURRED_FORM_H,
                 STATEMENT_OF_COSTS_FORM_H1,
                 STATEMENT_OF_COSTS_SUMMARY_ASSESSMENT_FORM_N260 -> getStatementOfCostsCategory(partyType);
            case CERTIFICATE_OF_SERVICE_FORM_FP6 -> getCertificateOfServiceCategory(partyType);
            case RESPONSE_TO_THE_NOTICE_OF_FIRST_APPOINTMENT_FORM_G -> getFormGCategory(partyType);
            case SCHEDULE_OF_DEFICIENCIES -> getScheduleOfDeficienciesCategory(partyType, doc);
            case CASE_SUMMARY -> getCaseSummaryCategory(partyType, doc);
            case POSITION_STATEMENT -> getPositionStatementCategory(partyType, doc);
            case CHRONOLOGY -> getChronologyCategory(partyType, doc);
            case STATEMENT_OF_ISSUES -> getStatementOfIssuesCategory(partyType, doc);
            case DIVORCE_APPLICATION_PETITION ->
                DocumentCategory.DIVORCE_DOCUMENTS_APPLICATION_OR_PETITION.getDocumentCategoryId();
            case DIVORCE_CONDITIONAL_ORDER_DECREE_NISI ->
                DocumentCategory.DIVORCE_DOCUMENTS_CONDITIONAL_ORDER_OR_DECREE_NISI.getDocumentCategoryId();
            case DIVORCE_FINAL_ORDER_DECREE_ABSOLUTE ->
                DocumentCategory.DIVORCE_DOCUMENTS_FINAL_ORDER_OR_DECREE_ABSOLUTE.getDocumentCategoryId();
            case COMPOSITE_CASE_SUMMARY_FORM_ES1 -> getEs1Category(partyType, doc);
            case COMPOSITE_SCHEDULE_OF_ASSETS_AND_INCOME_FORM_ES2 -> getEs2Category(partyType, doc);
            case MARKET_APPRAISAL_OR_VALUATION_OF_FAMILY_HOME -> getMarketAppraisalCategory(partyType, doc);
            case HOUSING_NEEDS_PROPERTY_PARTICULARS,
                 POTENTIAL_BORROWING_CAPACITY_MORTGAGE_CAPACITIES -> getHousingParticularsCategory(partyType, doc);
            case OPEN_OFFERS -> getOpenOffersCategory(partyType, doc);
            case QUESTIONNAIRE_REQUEST_FOR_FURTHER_DOCUMENTS,
                 SUPPLEMENTAL_QUESTIONNAIRE -> getQuestionnaireCategory(partyType, doc);
            case SECTION_25_STATEMENT -> getS25Category(partyType, doc);
            case WITNESS_STATEMENT -> getWitnessStatementCategory(partyType, doc);
            case WITHOUT_PREJUDICE_OFFERS_FOR_SETTLEMENT -> getWithoutPrejudiceCategory(partyType);
            case PENSION_REPORT_EXPERT_REPORT,
                 MEDICAL_REPORT,
                 BUSINESS_VALUATION,
                 OTHER_PROPERTY_VALUATION -> getExportReportCategory(doc);
            case HEARING_BUNDLE -> getHearingBundleCategory(doc);
            case FDR_BUNDLE -> DocumentCategory.FDR_BUNDLE.getDocumentCategoryId();
            case PRE_HEARING_DRAFT_ORDER -> getPreHearingDraftOrderCategory(partyType, doc);
            case REPLY_TO_QUESTIONNAIRE,
                 REPLY_TO_SCHEDULE_OF_DEFICIENCIES_OR_SUPPLEMENTAL_QUESTIONNAIRES,
                 REPLY_TO_QUESTIONNAIRE_SUPPORTING_DOCUMENTS,
                 REPLY_TO_SCHEDULE_OF_DEFICIENCIES_OR_SUPPLEMENTAL_QUESTIONNAIRES_SUPPORTING_DOCUMENTS ->
                getReplyToQuestionaireCategory(partyType);
            case UPDATING_DISCLOSURE,
                 BANK_STATEMENTS,
                 PAYSLIPS,
                 P60,
                 P45,
                 DEBT_STATEMENT,
                 LIST_OF_ASSETS,
                 LOAN_STATEMENT,
                 CAR_INSURANCE_LOAN_STATEMENT,
                 PERSONAL_SELLING_SIGHT_STATEMENT,
                 SCHOOL_FEES,
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
                 LIFE_INSURANCE_INCLUDING_ENDOWMENT_POLICIES,
                 MANAGEMENT_ACCOUNTS -> getUpdatingDisclosureCategory(partyType);
            default -> null;
        };

        setCategory(doc, category);
    }

    private String getReplyToQuestionaireCategory(CitizenPartyType partyType) {
        return isApplicant(partyType)
            ? DocumentCategory.APPLICANT_DOCUMENTS_REPLIES_TO_QUESTIONNAIRE.getDocumentCategoryId()
            : DocumentCategory.RESPONDENT_DOCUMENTS_REPLIES_TO_QUESTIONNAIRE.getDocumentCategoryId();
    }

    private String getHearingBundleCategory(CitizenUploadDocument doc) {
        return isFdr(doc)
            ? DocumentCategory.FDR_BUNDLE.getDocumentCategoryId()
            : DocumentCategory.HEARING_BUNDLE.getDocumentCategoryId();
    }

    private String getExportReportCategory(CitizenUploadDocument doc) {
        return isFdr(doc)
            ? DocumentCategory.FDR_REPORTS.getDocumentCategoryId()
            : DocumentCategory.REPORTS.getDocumentCategoryId();
    }

    private String getFormGCategory(CitizenPartyType partyType) {
        return isApplicant(partyType)
            ? DocumentCategory.APPLICANT_DOCUMENTS_FORM_G.getDocumentCategoryId()
            : DocumentCategory.RESPONDENT_DOCUMENTS_FORM_G.getDocumentCategoryId();
    }

    private String getCertificateOfServiceCategory(CitizenPartyType partyType) {
        return isApplicant(partyType)
            ? DocumentCategory.APPLICANT_DOCUMENTS_CERTIFICATES_OF_SERVICE.getDocumentCategoryId()
            : DocumentCategory.RESPONDENT_DOCUMENTS_CERTIFICATES_OF_SERVICE.getDocumentCategoryId();
    }

    private String getStatementOfCostsCategory(CitizenPartyType partyType) {
        return isApplicant(partyType)
            ? DocumentCategory.HEARING_DOCUMENTS_APPLICANT_COSTS_FORM_H_OR_FORM_H1_OR_FORM_N260.getDocumentCategoryId()
            : DocumentCategory.HEARING_DOCUMENTS_RESPONDENT_COSTS_FORM_H_OR_FORM_H1_OR_FORM_N260.getDocumentCategoryId();
    }

    private String getFromECategory(CitizenPartyType partyType) {
        return isApplicant(partyType)
            ? DocumentCategory.APPLICANT_DOCUMENTS_FORM_E.getDocumentCategoryId()
            : DocumentCategory.RESPONDENT_DOCUMENTS_FORM_E.getDocumentCategoryId();
    }

    private String getFm5FolderCategory(CitizenPartyType partyType) {
        return isApplicant(partyType)
            ? DocumentCategory.HEARING_DOCUMENTS_APPLICANT_FM5.getDocumentCategoryId()
            : DocumentCategory.HEARING_DOCUMENTS_RESPONDENT_FM5.getDocumentCategoryId();
    }

    private String getPointsOfClaimDefenceCategory(CitizenPartyType partyType) {
        return isApplicant(partyType)
            ? DocumentCategory.APPLICANT_DOCUMENTS_POINTS_OF_CLAIM_OR_DEFENCE.getDocumentCategoryId()
            : DocumentCategory.RESPONDENT_DOCUMENTS_POINTS_OF_CLAIM_OR_DEFENCE.getDocumentCategoryId();
    }

    private String getScheduleOfDeficienciesCategory(CitizenPartyType partyType, CitizenUploadDocument doc) {
        if (isFdr(doc)) {
            return isApplicant(partyType)
                ? DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_OTHER.getDocumentCategoryId()
                : DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_OTHER.getDocumentCategoryId();
        }
        return isApplicant(partyType)
            ? DocumentCategory.HEARING_DOCUMENTS_APPLICANT_REPLIES_TO_QUESTIONNAIRE.getDocumentCategoryId()
            : DocumentCategory.HEARING_DOCUMENTS_RESPONDENT_REPLIES_TO_QUESTIONNAIRE.getDocumentCategoryId();
    }

    private String getCaseSummaryCategory(CitizenPartyType partyType, CitizenUploadDocument doc) {
        if (isFdr(doc)) {
            return isApplicant(partyType)
                ? DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_OTHER.getDocumentCategoryId()
                : DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_OTHER.getDocumentCategoryId();
        }
        return isApplicant(partyType)
            ? DocumentCategory.HEARING_DOCUMENTS_APPLICANT_CASE_SUMMARY.getDocumentCategoryId()
            : DocumentCategory.HEARING_DOCUMENTS_RESPONDENT_CASE_SUMMARY.getDocumentCategoryId();
    }

    private String getPositionStatementCategory(CitizenPartyType partyType, CitizenUploadDocument doc) {
        if (isFdr(doc)) {
            return isApplicant(partyType)
                ? DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_POSITION_STATEMENTS.getDocumentCategoryId()
                : DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_POSITION_STATEMENTS.getDocumentCategoryId();
        }
        return isApplicant(partyType)
            ? DocumentCategory.HEARING_DOCUMENTS_APPLICANT_POSITION_STATEMENT.getDocumentCategoryId()
            : DocumentCategory.HEARING_DOCUMENTS_RESPONDENT_POSITION_STATEMENT.getDocumentCategoryId();
    }

    private String getChronologyCategory(CitizenPartyType partyType, CitizenUploadDocument doc) {
        if (isFdr(doc)) {
            return DocumentCategory.FDR_JOINT_DOCUMENTS_CHRONOLOGY.getDocumentCategoryId();
        }
        return isApplicant(partyType)
            ? DocumentCategory.HEARING_DOCUMENTS_APPLICANT_CHRONOLOGY.getDocumentCategoryId()
            : DocumentCategory.HEARING_DOCUMENTS_RESPONDENT_CHRONOLOGY.getDocumentCategoryId();
    }

    private String getStatementOfIssuesCategory(CitizenPartyType partyType, CitizenUploadDocument doc) {
        if (isFdr(doc)) {
            return isApplicant(partyType)
                ? DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_POSITION_STATEMENTS.getDocumentCategoryId()
                : DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_POSITION_STATEMENTS.getDocumentCategoryId();
        }
        return isApplicant(partyType)
            ? DocumentCategory.HEARING_DOCUMENTS_APPLICANT_CONCISE_STATEMENT_OF_ISSUES.getDocumentCategoryId()
            : DocumentCategory.HEARING_DOCUMENTS_RESPONDENT_CONCISE_STATEMENT_OF_ISSUES.getDocumentCategoryId();
    }

    private String getEs1Category(CitizenPartyType partyType, CitizenUploadDocument doc) {
        if (isFdr(doc)) {
            return DocumentCategory.FDR_JOINT_DOCUMENTS_ES1.getDocumentCategoryId();
        }
        return isApplicant(partyType)
            ? DocumentCategory.HEARING_DOCUMENTS_APPLICANT_ES1.getDocumentCategoryId()
            : DocumentCategory.HEARING_DOCUMENTS_RESPONDENT_ES1.getDocumentCategoryId();
    }

    private String getEs2Category(CitizenPartyType partyType, CitizenUploadDocument doc) {
        if (isFdr(doc)) {
            return DocumentCategory.FDR_JOINT_DOCUMENTS_ES2.getDocumentCategoryId();
        }
        return isApplicant(partyType)
            ? DocumentCategory.HEARING_DOCUMENTS_APPLICANT_ES2.getDocumentCategoryId()
            : DocumentCategory.HEARING_DOCUMENTS_RESPONDENT_ES2.getDocumentCategoryId();
    }

    private String getMarketAppraisalCategory(CitizenPartyType partyType, CitizenUploadDocument doc) {
        if (isFdr(doc)) {
            return isApplicant(partyType)
                ? DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_OTHER.getDocumentCategoryId()
                : DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_OTHER.getDocumentCategoryId();
        }
        return isApplicant(partyType)
            ? DocumentCategory.APPLICANT_MORTGAGE_CAPACITIES_OR_MARKET_APPRAISAL.getDocumentCategoryId()
            : DocumentCategory.RESPONDENT_MORTGAGE_CAPACITIES_OR_MARKET_APPRAISAL.getDocumentCategoryId();
    }

    private String getHousingParticularsCategory(CitizenPartyType partyType, CitizenUploadDocument doc) {
        if (isFdr(doc)) {
            return isApplicant(partyType)
                ? DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_OTHER.getDocumentCategoryId()
                : DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_OTHER.getDocumentCategoryId();
        }
        return isApplicant(partyType)
            ? DocumentCategory.APPLICANT_MORTGAGE_CAPACITIES_OR_HOUSING_PARTICULARS.getDocumentCategoryId()
            : DocumentCategory.RESPONDENT_MORTGAGE_CAPACITIES_OR_HOUSING_PARTICULARS.getDocumentCategoryId();
    }

    private String getOpenOffersCategory(CitizenPartyType partyType, CitizenUploadDocument doc) {
        if (isFdr(doc)) {
            return isApplicant(partyType)
                ? DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_OTHER.getDocumentCategoryId()
                : DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_OTHER.getDocumentCategoryId();
        }
        return isApplicant(partyType)
            ? DocumentCategory.APPLICANT_DOCUMENTS_OPEN_OFFERS.getDocumentCategoryId()
            : DocumentCategory.RESPONDENT_DOCUMENTS_OPEN_OFFERS.getDocumentCategoryId();
    }

    private String getQuestionnaireCategory(CitizenPartyType partyType, CitizenUploadDocument doc) {
        if (isFdr(doc)) {
            return isApplicant(partyType)
                ? DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_OTHER.getDocumentCategoryId()
                : DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_OTHER.getDocumentCategoryId();
        }
        return isApplicant(partyType)
            ? DocumentCategory.HEARING_DOCUMENTS_APPLICANT_QUESTIONNAIRES.getDocumentCategoryId()
            : DocumentCategory.HEARING_DOCUMENTS_RESPONDENT_QUESTIONNAIRES.getDocumentCategoryId();
    }

    private String getS25Category(CitizenPartyType partyType, CitizenUploadDocument doc) {
        if (isFdr(doc)) {
            return isApplicant(partyType)
                ? DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_OTHER.getDocumentCategoryId()
                : DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_OTHER.getDocumentCategoryId();
        }
        return isApplicant(partyType)
            ? DocumentCategory.APPLICANT_DOCUMENTS_S25_STATEMENT.getDocumentCategoryId()
            : DocumentCategory.RESPONDENT_DOCUMENTS_S25_STATEMENT.getDocumentCategoryId();
    }

    private String getWitnessStatementCategory(CitizenPartyType partyType, CitizenUploadDocument doc) {
        if (isFdr(doc)) {
            return isApplicant(partyType)
                ? DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_OTHER.getDocumentCategoryId()
                : DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_OTHER.getDocumentCategoryId();
        }
        return isApplicant(partyType)
            ? DocumentCategory.APPLICANT_DOCUMENTS_WITNESS_STATEMENTS.getDocumentCategoryId()
            : DocumentCategory.RESPONDENT_DOCUMENTS_WITNESS_STATEMENTS.getDocumentCategoryId();
    }

    private String getWithoutPrejudiceCategory(CitizenPartyType partyType) {
        return isApplicant(partyType)
            ? DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_WITHOUT_PREJUDICE_OFFERS.getDocumentCategoryId()
            : DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_WITHOUT_PREJUDICE_OFFERS.getDocumentCategoryId();
    }

    private String getPreHearingDraftOrderCategory(CitizenPartyType partyType, CitizenUploadDocument doc) {
        if (isFdr(doc)) {
            return isApplicant(partyType)
                ? DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_DRAFT_ORDER.getDocumentCategoryId()
                : DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_DRAFT_ORDER.getDocumentCategoryId();
        }
        return isApplicant(partyType)
            ? DocumentCategory.HEARING_DOCUMENTS_APPLICANT_PRE_HEARING_DRAFT_ORDER.getDocumentCategoryId()
            : DocumentCategory.HEARING_DOCUMENTS_RESPONDENT_PRE_HEARING_DRAFT_ORDER.getDocumentCategoryId();
    }

    private String getUpdatingDisclosureCategory(CitizenPartyType partyType) {
        return isApplicant(partyType)
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

    private boolean isFdr(CitizenUploadDocument doc) {
        return YesOrNo.isYes(doc.getIsFdr());
    }

    private boolean isApplicant(CitizenPartyType party) {
        return CitizenPartyType.APPLICANT.equals(party);
    }
}
