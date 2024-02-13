package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
public enum CaseDocumentType {
    STATEMENT_OF_ISSUES("Statement of Issues"),
    CHRONOLOGY("Chronology"),
    APPLICANT_FORM_E("Applicant - Form E"),
    FORM_G("Form G"),
    FORM_H("Form H"),
    LETTER_FROM_APPLICANT("Letter from Applicant"),
    CASE_SUMMARY("Case Summary"),
    QUESTIONNAIRE("Questionnaire"),
    REPLY_TO_QUESTIONNAIRE("Reply to Questionnaire"),
    VALUATION_REPORT("Valuation Report"),
    PENSION_PLAN("Pension Plan"),
    POSITION_STATEMENT_SKELETON_ARGUMENT("Position Statement"),
    SKELETON_ARGUMENT("Skeleton Argument"),
    EXPERT_EVIDENCE("Expert Evidence"),
    STATEMENT_AFFIDAVIT("Statement/Affidavit"),
    WITNESS_STATEMENT_AFFIDAVIT("Witness Statement/Affidavit"),
    OFFERS("Offers"),
    TRIAL_BUNDLE("Trial Bundle"),
    CONDITIONAL_ORDER("Conditional order"),
    FINAL_ORDER("Final order"),
    OTHER("other"),

    // New CFV document types
    ATTENDANCE_SHEETS("Attendance Sheets"),
    BILL_OF_COSTS("Bill of Costs"),
    CERTIFICATES_OF_SERVICE("Certificates of service"),
    ES1("ES1"),
    ES2("ES2"),
    FAMILY_HOME_VALUATION("Family home valuation"),
    HOUSING_PARTICULARS("Housing particulars"),
    JUDICIAL_NOTES("Judicial notes"),
    JUDGMENT("Judgment"),
    MORTGAGE_CAPACITIES("Mortgage capacities"),
    PENSION_REPORT("Pension report"),
    PRE_HEARING_DRAFT_ORDER("Pre hearing draft order"),
    TRANSCRIPT("Transcript"),
    WITHOUT_PREJUDICE_OFFERS("Without Prejudice offers"),
    WITNESS_SUMMONS("Witness Summons");


    private final String id;

    @JsonValue
    public String getId() {
        return id;
    }

    public static CaseDocumentType getCaseDocumentType(String ccdType) {
        return Arrays.stream(CaseDocumentType.values())
            .filter(option -> option.id.equals(ccdType))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
