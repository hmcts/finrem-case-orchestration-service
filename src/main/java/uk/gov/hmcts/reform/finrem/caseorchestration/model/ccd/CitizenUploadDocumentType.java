package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
public enum CitizenUploadDocumentType {

    ATTACHMENTS_TO_FORM_E("Attachments to Form E"),
    BANK_STATEMENTS("Bank statements"),
    BUSINESS_ACCOUNTS("Business accounts"),
    BUSINESS_VALUATION("Business valuation"),
    CAR_INSURANCE_LOAN_STATEMENT("Car insurance loan statement"),
    CASE_SUMMARY("Case summary"),
    CERTIFICATE_OF_SERVICE_FORM_FP6("Certificate of service: Form FP6"),
    CHRONOLOGY("Chronology"),
    COMPOSITE_CASE_SUMMARY_FORM_ES1("Composite case summary: Form ES1"),
    COMPOSITE_SCHEDULE_OF_ASSETS_AND_INCOME_FORM_ES2("Composite schedule of assets and income: Form ES2"),
    DEBT_STATEMENT("Debt statement"),
    DIVORCE_APPLICATION_PETITION("Divorce application / petition"),
    DIVORCE_CONDITIONAL_ORDER_DECREE_NISI("Divorce Conditional Order / Decree Nisi"),
    DIVORCE_FINAL_ORDER_DECREE_ABSOLUTE("Divorce Final Order / Decree Absolute"),
    ESTIMATE_OF_COSTS_INCURRED_FORM_H("Estimate of costs incurred: Form H"),
    FAMILY_MEDIATION_INFORMATION_AND_ASSESSMENT_MEETING_MIAM_FORM_FM1(
        "Family Mediation Information and Assessment Meeting (MIAM) Form: Form FM1"
    ),
    FDR_BUNDLE("FDR bundle"),
    FINANCIAL_STATEMENT_FORM_E_E1_OR_E2("Financial statement: Form E, E1 or E2"),
    HEARING_BUNDLE("Hearing bundle"),
    HOUSING_NEEDS_PROPERTY_PARTICULARS("Housing needs / property particulars"),
    INCOME_EVIDENCE("Income evidence"),
    INVESTMENT_STATEMENTS("Investment statements"),
    LIFE_INSURANCE_INCLUDING_ENDOWMENT_POLICIES("Life insurance (including endowment) policies"),
    LIST_OF_ASSETS("List of assets"),
    LOAN_STATEMENT("Loan statement"),
    MANAGEMENT_ACCOUNTS("Management accounts"),
    MARKET_APPRAISAL_OR_VALUATION_OF_FAMILY_HOME("Market appraisal or valuation of family home"),
    MEDICAL_REPORT("Medical report"),
    MORTGAGE_STATEMENTS_FOR_FAMILY_HOME("Mortgage statements for family home"),
    MORTGAGE_STATEMENTS_FOR_OTHER_PROPERTIES("Mortgage statements for other properties"),
    OPEN_OFFERS("Open offers"),
    OTHER_PROPERTY_VALUATION("Other property valuation"),
    P11D("P11D"),
    P45("P45"),
    P60("P60"),
    PAYSLIPS("Payslips"),
    PENSION_REPORT_EXPERT_REPORT("Pension report/ expert report"),
    PENSION_STATEMENT("Pension statement"),
    PERSONAL_SELLING_SIGHT_STATEMENT("Personal selling sight statement"),
    POINTS_OF_CLAIM_DEFENCE("Points of claim/defence"),
    POSITION_STATEMENT("Position statement"),
    POTENTIAL_BORROWING_CAPACITY_MORTGAGE_CAPACITIES(
        "Potential borrowing capacity / mortgage capacities"
    ),
    PRE_HEARING_DRAFT_ORDER("Pre hearing draft order"),
    QUESTIONNAIRE_REQUEST_FOR_FURTHER_DOCUMENTS("Questionnaire / request for further documents"),
    REPLY_TO_QUESTIONNAIRE("Reply to questionnaire"),
    REPLY_TO_QUESTIONNAIRE_SUPPORTING_DOCUMENTS("Reply to questionnaire - supporting documents"),
    REPLY_TO_SCHEDULE_OF_DEFICIENCIES_OR_SUPPLEMENTAL_QUESTIONNAIRES(
        "Reply to schedule of deficiencies or supplemental questionnaires"
    ),
    REPLY_TO_SCHEDULE_OF_DEFICIENCIES_OR_SUPPLEMENTAL_QUESTIONNAIRES_SUPPORTING_DOCUMENTS(
        "Reply to schedule of deficiencies or supplemental questionnaires - supporting documents"
    ),
    RESPONSE_TO_THE_NOTICE_OF_FIRST_APPOINTMENT_FORM_G(
        "Response to the notice of first appointment: Form G"
    ),
    SCHEDULE_OF_DEFICIENCIES("Schedule of deficiencies"),
    SCHOOL_FEES("School fees"),
    SECTION_25_STATEMENT("Section 25 statement"),
    SELF_ASSESSMENT_TAX_FORMS("Self assessment tax forms"),
    STATEMENT_OF_COSTS_SUMMARY_ASSESSMENT_FORM_N260(
        "Statement of costs (summary assessment): Form N260"
    ),
    STATEMENT_OF_COSTS_FORM_H1("Statement of costs: Form H1"),
    STATEMENT_OF_ISSUES("Statement of issues"),
    STATEMENT_OF_POSITION_ON_NON_COURT_DISPUTE_RESOLUTION_NCDR_FORM_FM5(
        "Statement of position on non-court dispute resolution (NCDR): Form FM5"
    ),
    SUPPLEMENTAL_QUESTIONNAIRE("Supplemental questionnaire"),
    TAX_ASSESSMENTS("Tax assessments"),
    UNIVERSAL_CREDIT_STATEMENT("Universal credit statement"),
    UPDATING_DISCLOSURE("Updating disclosure"),
    WITNESS_STATEMENT("Witness statement"),
    WITHOUT_PREJUDICE_OFFERS_FOR_SETTLEMENT("Without prejudice offers for settlement");

    private final String id;

    @JsonValue
    public String getId() {
        return id;
    }

    @JsonCreator
    public static CitizenUploadDocumentType getCaseDocumentType(String ccdType) {
        return Arrays.stream(CitizenUploadDocumentType.values())
            .filter(option -> option.id.equals(ccdType))
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
    }
}
