package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
public enum CaseDocumentType {
    STATEMENT_OF_ISSUES("Statement of Issues"),
    CHRONOLOGY("Chronology"),
    FORM_B("Form B"),
    APPLICANT_FORM_E("Applicant - Form E"),
    FORM_F("Form F"),
    FORM_G("Form G"),
    FORM_H("Form H"),
    LETTER_FROM_APPLICANT("Letter from Applicant"),
    CASE_SUMMARY("Case Summary"),
    QUESTIONNAIRE("Questionnaire"),
    REPLY_TO_QUESTIONNAIRE("Reply to Questionnaire"),
    VALUATION_REPORT("Valuation Report"),
    PENSION_PLAN("Pension Plan"),
    POSITION_STATEMENT("Position Statement"),
    SKELETON_ARGUMENT("Skeleton Argument"),
    EXPERT_EVIDENCE("Expert Evidence"),
    STATEMENT_AFFIDAVIT("Statement/Affidavit"),
    WITNESS_STATEMENT_AFFIDAVIT("Witness Statement/Affidavit"),
    CARE_PLAN("Care Plan"),
    OFFERS("Offers"),
    TRIAL_BUNDLE("Trial Bundle"),
    CONDITIONAL_ORDER("Conditional order"),
    FINAL_ORDER("Final order"),
    OTHER("other");

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
