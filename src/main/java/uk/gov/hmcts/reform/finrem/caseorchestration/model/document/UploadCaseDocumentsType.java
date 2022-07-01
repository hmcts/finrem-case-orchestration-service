package uk.gov.hmcts.reform.finrem.caseorchestration.model.document;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum UploadCaseDocumentsType {

    TRIAL_BUNDLE("Trial Bundle"),
    APPLICANT_FORM_E("Applicant - Form E"),
    CHRONOLOGIES_STATEMENT_OF_ISSUES("Statement of Issues"),
    CHRONOLOGY("Chronology"),
    FORM_G("Form G"),
    QUESTIONNAIRE("Questionnaire"),
    REPLY_OF_QUESTIONNAIRE("Reply to Questionnaire"),
    STATEMENT_OR_AFFIDAVIT("Statement/Affidavit"),
    WITNESS_STATEMENT_OR_AFFIDAVIT("Witness Statement/Affidavit"),
    POSITION_STATEMENT("Position Statement"),
    SKELETON_ARGUMENT("Skeleton Argument"),
    CASE_SUMMARY("Case Summary"),
    FORM_H("Form H"),
    VALUATION_REPORT("Valuation Report"),
    EXPERT_EVIDENCE("Expert Evidence"),
    OFFERS("Offers"),
    LETTER_FROM_APPLICANT("Letter from Applicant"),
    OTHER("other"),
    FORM_B("Form B"),
    FORM_F("Form F"),
    CARE_PLAN("Care Plan"),
    PENSION_PLAN("Pension Plan");

    private final String caseDocumentType;

    public String getCaseDocumentType() {
        return caseDocumentType;
    }
}
