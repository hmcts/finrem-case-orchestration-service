package uk.gov.hmcts.reform.finrem.caseorchestration.model.document;

import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public enum DocumentCategory {
    APPLICATIONS("applications"),
    APPLICATIONS_MAIN_APPLICATION("applicationsMainApplication"),
    APPLICATIONS_OTHER_APPLICATION("applicationsOtherApplication"),
    APPLICATIONS_CONSENT_ORDER_TO_FINALISE_PROCEEDINGS("applicationsConsentOrderToFinaliseProceedings"),
    HEARING_NOTICES("hearingNotices"),
    POST_HEARING_DRAFT_ORDER("postHearingDraftOrder"),
    APPROVED_ORDERS("approvedOrders"),
    APPROVED_ORDERS_CASE("approvedOrdersCase"),
    APPROVED_ORDERS_GENERAL_APPLICATIONS("approvedOrdersGeneralApplications"),
    APPROVED_ORDERS_CONSENT_APPLICATION("approvedOrdersConsentApplication"),
    APPLICANT_DOCUMENTS("applicantDocuments"),
    APPLICANT_DOCUMENTS_FORM_E("applicantDocumentsFormE"),
    APPLICANT_DOCUMENTS_FORM_G("applicantDocumentsFormG"),
    APPLICANT_DOCUMENTS_REPLIES_TO_QUESTIONNAIRE("applicantDocumentsRepliesToQuestionnaire"),
    APPLICANT_MORTGAGE_CAPACITIES_OR_HOUSING_PARTICULARS("applicantMortgageCapacitiesOrHousingParticulars"),
    APPLICANT_DOCUMENTS_OPEN_OFFERS("applicantDocumentsOpenOffers"),
    APPLICANT_DOCUMENTS_S25_STATEMENT("applicantDocumentsS25Statement"),
    APPLICANT_DOCUMENTS_WITNESS_STATEMENTS("applicantDocumentsWitnessStatements"),
    APPLICANT_DOCUMENTS_CERTIFICATES_OF_SERVICE("applicantDocumentsCertificatesOfService"),
    APPLICANT_DOCUMENTS_MISCELLANEOUS_OR_OTHER("applicantDocumentsMiscellaneousOrOther"),
    APPLICANT_DOCUMENTS_PENSION_PLAN("applicantDocumentsPensionPlan"),
    RESPONDENT_DOCUMENTS("respondentDocuments"),
    RESPONDENT_DOCUMENTS_FORM_E("respondentDocumentsFormE"),
    RESPONDENT_DOCUMENTS_FORM_G("respondentDocumentsFormG"),
    RESPONDENT_DOCUMENTS_REPLIES_TO_QUESTIONNAIRE("respondentDocumentsRepliesToQuestionnaire"),
    RESPONDENT_MORTGAGE_CAPACITIES_OR_HOUSING_PARTICULARS("respondentMortgageCapacitiesOrHousingParticulars"),
    RESPONDENT_DOCUMENTS_OPEN_OFFERS("respondentDocumentsOpenOffers"),
    RESPONDENT_DOCUMENTS_S25_STATEMENT("respondentDocumentsS25Statement"),
    RESPONDENT_DOCUMENTS_WITNESS_STATEMENTS("respondentDocumentsWitnessStatements"),
    RESPONDENT_DOCUMENTS_CERTIFICATES_OF_SERVICE("respondentDocumentsCertificatesOfService"),
    RESPONDENT_DOCUMENTS_MISCELLANEOUS_OR_OTHER("respondentDocumentsMiscellaneousOrOther"),
    RESPONDENT_DOCUMENTS_PENSION_PLAN("respondentDocumentsPensionPlan"),
    LIP_OR_SCANNED_DOCUMENTS("lipOrScannedDocuments"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE("fdrDocumentsAndFdrBundle"),
    FDR_BUNDLE("fdrBundle"),
    FDR_JOINT_DOCUMENTS("fdrDocumentsJointDocuments"),
    FDR_JOINT_DOCUMENTS_ES1("fdrDocumentsJointDocumentsEs1"),
    FDR_JOINT_DOCUMENTS_ES2("fdrDocumentsJointDocumentsEs2"),
    FDR_JOINT_DOCUMENTS_CHRONOLOGY("fdrDocumentsJointDocumentsChronology"),
    FDR_REPORTS("fdrDocumentsReports"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT("fdrDocumentsAndFdrBundleApplicant"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_POSITION_STATEMENTS("fdrDocumentsAndFdrBundleApplicantPositionStatements"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_WITHOUT_PREJUDICE_OFFERS("fdrDocumentsAndFdrBundleApplicantWithoutPrejudiceOffers"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_OTHER("fdrDocumentsAndFdrBundleApplicantOther"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_DRAFT_ORDER("fdrDocumentsAndFdrBundleApplicantDraftOrder"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT("fdrDocumentsAndFdrBundleRespondent"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_POSITION_STATEMENTS("fdrDocumentsAndFdrBundleRespondentPositionStatements"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_WITHOUT_PREJUDICE_OFFERS("fdrDocumentsAndFdrBundleRespondentWithoutPrejudiceOffers"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_OTHER("fdrDocumentsAndFdrBundleRespondentOther"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_DRAFT_ORDER("fdrDocumentsAndFdrBundleRespondentDraftOrder"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_1("fdrDocumentsAndFdrBundleIntervener1"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_1_POSITION_STATEMENTS("fdrDocumentsAndFdrBundleIntervener1PositionStatements"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_1_COSTS_STATEMENT("fdrDocumentsAndFdrBundleIntervener1CostsStatement"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_1_WITHOUT_PREJUDICE_OFFERS("fdrDocumentsAndFdrBundleIntervener1WithoutPrejudiceOffers"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_1_OTHER("fdrDocumentsAndFdrBundleIntervener1Other"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_1_DRAFT_ORDER("fdrDocumentsAndFdrBundleIntervener1DraftOrder"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_2("fdrDocumentsAndFdrBundleIntervener2"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_2_POSITION_STATEMENTS("fdrDocumentsAndFdrBundleIntervener2PositionStatements"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_2_COSTS_STATEMENT("fdrDocumentsAndFdrBundleIntervener2CostsStatement"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_2_WITHOUT_PREJUDICE_OFFERS("fdrDocumentsAndFdrBundleIntervener2WithoutPrejudiceOffers"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_2_OTHER("fdrDocumentsAndFdrBundleIntervener2Other"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_2_DRAFT_ORDER("fdrDocumentsAndFdrBundleIntervener2DraftOrder"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_3("fdrDocumentsAndFdrBundleIntervener3"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_3_POSITION_STATEMENTS("fdrDocumentsAndFdrBundleIntervener3PositionStatements"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_3_COSTS_STATEMENT("fdrDocumentsAndFdrBundleIntervener3CostsStatement"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_3_WITHOUT_PREJUDICE_OFFERS("fdrDocumentsAndFdrBundleIntervener3WithoutPrejudiceOffers"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_3_OTHER("fdrDocumentsAndFdrBundleIntervener3Other"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_3_DRAFT_ORDER("fdrDocumentsAndFdrBundleIntervener3DraftOrder"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_4("fdrDocumentsAndFdrBundleIntervener4"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_4_POSITION_STATEMENTS("fdrDocumentsAndFdrBundleIntervener4PositionStatements"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_4_COSTS_STATEMENT("fdrDocumentsAndFdrBundleIntervener4CostsStatement"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_4_WITHOUT_PREJUDICE_OFFERS("fdrDocumentsAndFdrBundleIntervener4WithoutPrejudiceOffers"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_4_OTHER("fdrDocumentsAndFdrBundleIntervener4Other"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_4_DRAFT_ORDER("fdrDocumentsAndFdrBundleIntervener4DraftOrder"),
    HEARING_DOCUMENTS("hearingDocuments"),
    HEARING_DOCUMENTS_APPLICANT_JOINT("hearingDocumentsApplicantJoint"),
    HEARING_DOCUMENTS_APPLICANT("hearingDocumentsApplicant"),
    HEARING_DOCUMENTS_APPLICANT_CASE_SUMMARY("hearingDocumentsApplicantCaseSummary"),
    HEARING_DOCUMENTS_APPLICANT_POSITION_STATEMENT("hearingDocumentsApplicantPositionStatement"),
    HEARING_DOCUMENTS_APPLICANT_CONCISE_STATEMENT_OF_ISSUES("hearingDocumentsApplicantConciseStatementOfIssues"),
    HEARING_DOCUMENTS_APPLICANT_ES1("hearingDocumentsApplicantES1"),
    HEARING_DOCUMENTS_APPLICANT_ES2("hearingDocumentsApplicantES2"),
    HEARING_DOCUMENTS_APPLICANT_CHRONOLOGY("hearingDocumentsApplicantChronology"),
    HEARING_DOCUMENTS_APPLICANT_QUESTIONNAIRES("hearingDocumentsApplicantQuestionnaires"),
    HEARING_DOCUMENTS_APPLICANT_COSTS_FORM_H_OR_FORM_H1_OR_FORM_N260("hearingDocumentsApplicantCostsFormH/H1/N260"),
    HEARING_DOCUMENTS_APPLICANT_PRE_HEARING_DRAFT_ORDER("hearingDocumentsApplicantPreHearingDraftOrder"),
    HEARING_DOCUMENTS_RESPONDENT("hearingDocumentsRespondent"),
    HEARING_DOCUMENTS_RESPONDENT_JOINT("hearingDocumentsRespondentJoint"),
    HEARING_DOCUMENTS_RESPONDENT_CASE_SUMMARY("hearingDocumentsRespondentCaseSummary"),
    HEARING_DOCUMENTS_RESPONDENT_POSITION_STATEMENT("hearingDocumentsRespondentPositionStatement"),
    HEARING_DOCUMENTS_RESPONDENT_CONCISE_STATEMENT_OF_ISSUES("hearingDocumentsRespondentConciseStatementOfIssues"),
    HEARING_DOCUMENTS_RESPONDENT_ES1("hearingDocumentsRespondentES1"),
    HEARING_DOCUMENTS_RESPONDENT_ES2("hearingDocumentsRespondentES2"),
    HEARING_DOCUMENTS_RESPONDENT_CHRONOLOGY("hearingDocumentsRespondentChronology"),
    HEARING_DOCUMENTS_RESPONDENT_QUESTIONNAIRES("hearingDocumentsRespondentQuestionnaires"),
    HEARING_DOCUMENTS_RESPONDENT_COSTS_FORM_H_OR_FORM_H1_OR_FORM_N260("hearingDocumentsRespondentCostsFormH/H1/N2601"),
    HEARING_DOCUMENTS_RESPONDENT_PRE_HEARING_DRAFT_ORDER("hearingDocumentsRespondentPreHearingDraftOrder"),
    HEARING_DOCUMENTS_INTERVENER_1("hearingDocumentsIntervener1"),
    HEARING_DOCUMENTS_INTERVENER_1_JOINT("hearingDocumentsIntervener1Joint"),
    HEARING_DOCUMENTS_INTERVENER_1_CASE_SUMMARY("hearingDocumentsIntervener1CaseSummary"),
    HEARING_DOCUMENTS_INTERVENER_1_POSITION_STATEMENT("hearingDocumentsIntervener1PositionStatement"),
    HEARING_DOCUMENTS_INTERVENER_1_CONCISE_STATEMENT_OF_ISSUES("hearingDocumentsIntervener1ConciseStatementOfIssues"),
    HEARING_DOCUMENTS_INTERVENER_1_ES1("hearingDocumentsIntervener1ES1"),
    HEARING_DOCUMENTS_INTERVENER_1_ES2("hearingDocumentsIntervener1ES2"),
    HEARING_DOCUMENTS_INTERVENER_1_CHRONOLOGY("hearingDocumentsIntervener1Chronology"),
    HEARING_DOCUMENTS_INTERVENER_1_QUESTIONNAIRES("hearingDocumentsIntervener1Questionnaires"),
    HEARING_DOCUMENTS_INTERVENER_1_COSTS_FORM_H_OR_FORM_H1_OR_FORM_N260("hearingDocumentsIntervener1CostsFormH/H1/N260"),
    HEARING_DOCUMENTS_INTERVENER_1_PRE_HEARING_DRAFT_ORDER("hearingDocumentsIntervener1PreHearingDraftOrder"),
    HEARING_DOCUMENTS_INTERVENER_1_OPEN_OFFERS("hearingDocumentsIntervener1OpenOffers"),
    HEARING_DOCUMENTS_INTERVENER_1_WITNESS_STATEMENTS("hearingDocumentsIntervener1WitnessStatements"),
    HEARING_DOCUMENTS_INTERVENER_1_COST_STATEMENT("hearingDocumentsIntervener1CostStatement"),
    HEARING_DOCUMENTS_INTERVENER_1_OTHER("hearingDocumentsIntervener1Other"),
    HEARING_DOCUMENTS_INTERVENER_2("hearingDocumentsIntervener2"),
    HEARING_DOCUMENTS_INTERVENER_2_JOINT("hearingDocumentsIntervener2Joint"),
    HEARING_DOCUMENTS_INTERVENER_2_CASE_SUMMARY("hearingDocumentsIntervener2CaseSummary"),
    HEARING_DOCUMENTS_INTERVENER_2_POSITION_STATEMENT("hearingDocumentsIntervener2PositionStatement"),
    HEARING_DOCUMENTS_INTERVENER_2_CONCISE_STATEMENT_OF_ISSUES("hearingDocumentsIntervener2ConciseStatementOfIssues"),
    HEARING_DOCUMENTS_INTERVENER_2_ES1("hearingDocumentsIntervener2ES1"),
    HEARING_DOCUMENTS_INTERVENER_2_ES2("hearingDocumentsIntervener2ES2"),
    HEARING_DOCUMENTS_INTERVENER_2_CHRONOLOGY("hearingDocumentsIntervener2Chronology"),
    HEARING_DOCUMENTS_INTERVENER_2_QUESTIONNAIRES("hearingDocumentsIntervener2Questionnaires"),
    HEARING_DOCUMENTS_INTERVENER_2_COSTS_FORM_H_OR_FORM_H1_OR_FORM_N260("hearingDocumentsIntervener2CostsFormH/H1/N2601"),
    HEARING_DOCUMENTS_INTERVENER_2_PRE_HEARING_DRAFT_ORDER("hearingDocumentsIntervener2PreHearingDraftOrder"),
    HEARING_DOCUMENTS_INTERVENER_2_OPEN_OFFERS("hearingDocumentsIntervener2OpenOffers"),
    HEARING_DOCUMENTS_INTERVENER_2_WITNESS_STATEMENTS("hearingDocumentsIntervener2WitnessStatements"),
    HEARING_DOCUMENTS_INTERVENER_2_COST_STATEMENT("hearingDocumentsIntervener2CostStatement"),
    HEARING_DOCUMENTS_INTERVENER_2_OTHER("hearingDocumentsIntervener2Other"),
    HEARING_DOCUMENTS_INTERVENER_3("hearingDocumentsIntervener3"),
    HEARING_DOCUMENTS_INTERVENER_3_JOINT("hearingDocumentsIntervener3Joint"),
    HEARING_DOCUMENTS_INTERVENER_3_CASE_SUMMARY("hearingDocumentsIntervener3CaseSummary"),
    HEARING_DOCUMENTS_INTERVENER_3_POSITION_STATEMENT("hearingDocumentsIntervener3PositionStatement"),
    HEARING_DOCUMENTS_INTERVENER_3_CONCISE_STATEMENT_OF_ISSUES("hearingDocumentsIntervener3ConciseStatementOfIssues"),
    HEARING_DOCUMENTS_INTERVENER_3_ES1("hearingDocumentsIntervener3ES1"),
    HEARING_DOCUMENTS_INTERVENER_3_ES2("hearingDocumentsIntervener3ES2"),
    HEARING_DOCUMENTS_INTERVENER_3_CHRONOLOGY("hearingDocumentsIntervener3Chronology"),
    HEARING_DOCUMENTS_INTERVENER_3_QUESTIONNAIRES("hearingDocumentsIntervener3Questionnaires"),
    HEARING_DOCUMENTS_INTERVENER_3_COSTS_FORM_H_OR_FORM_H1_OR_FORM_N260("hearingDocumentsIntervener3CostsFormH/H1/N2601"),
    HEARING_DOCUMENTS_INTERVENER_3_PRE_HEARING_DRAFT_ORDER("hearingDocumentsIntervener3PreHearingDraftOrder"),
    HEARING_DOCUMENTS_INTERVENER_3_OPEN_OFFERS("hearingDocumentsIntervener3OpenOffers"),
    HEARING_DOCUMENTS_INTERVENER_3_WITNESS_STATEMENTS("hearingDocumentsIntervener3WitnessStatements"),
    HEARING_DOCUMENTS_INTERVENER_3_COST_STATEMENT("hearingDocumentsIntervener3CostStatement"),
    HEARING_DOCUMENTS_INTERVENER_3_OTHER("hearingDocumentsIntervener3Other"),
    HEARING_DOCUMENTS_INTERVENER_4("hearingDocumentsIntervener4"),
    HEARING_DOCUMENTS_INTERVENER_4_JOINT("hearingDocumentsIntervener4Joint"),
    HEARING_DOCUMENTS_INTERVENER_4_CASE_SUMMARY("hearingDocumentsIntervener4CaseSummary"),
    HEARING_DOCUMENTS_INTERVENER_4_POSITION_STATEMENT("hearingDocumentsIntervener4PositionStatement"),
    HEARING_DOCUMENTS_INTERVENER_4_CONCISE_STATEMENT_OF_ISSUES("hearingDocumentsIntervener4ConciseStatementOfIssues"),
    HEARING_DOCUMENTS_INTERVENER_4_ES1("hearingDocumentsIntervener4ES1"),
    HEARING_DOCUMENTS_INTERVENER_4_ES2("hearingDocumentsIntervener4ES2"),
    HEARING_DOCUMENTS_INTERVENER_4_CHRONOLOGY("hearingDocumentsIntervener4Chronology"),
    HEARING_DOCUMENTS_INTERVENER_4_QUESTIONNAIRES("hearingDocumentsIntervener4Questionnaires"),
    HEARING_DOCUMENTS_INTERVENER_4_COSTS_FORM_H_OR_FORM_H1_OR_FORM_N260("hearingDocumentsIntervener4CostsFormH/H1/N2601"),
    HEARING_DOCUMENTS_INTERVENER_4_PRE_HEARING_DRAFT_ORDER("hearingDocumentsIntervener4PreHearingDraftOrder"),
    HEARING_DOCUMENTS_INTERVENER_4_OPEN_OFFERS("hearingDocumentsIntervener4OpenOffers"),
    HEARING_DOCUMENTS_INTERVENER_4_WITNESS_STATEMENTS("hearingDocumentsIntervener4WitnessStatements"),
    HEARING_DOCUMENTS_INTERVENER_4_COST_STATEMENT("hearingDocumentsIntervener4CostStatement"),
    HEARING_DOCUMENTS_INTERVENER_4_OTHER("hearingDocumentsIntervener4Other"),
    HEARING_DOCUMENTS_WITNESS_SUMMONS("hearingDocumentsWitnessSummons"),
    HEARING_BUNDLE("hearingBundle"),
    REPORTS("reports"),
    COURT_CORRESPONDENCE("courtCorrespondenc"),
    COURT_CORRESPONDENCE_APPLICANT("courtCorrespondenceApplicant"),
    COURT_CORRESPONDENCE_RESPONDENT("courtCorrespondenceRespondent"),
    COURT_CORRESPONDENCE_INTERVENER_1("courtCorrespondenceIntervener1"),
    COURT_CORRESPONDENCE_INTERVENER_1_OPEN_OFFERS("courtCorrespondenceIntervener1OpenOffers"),
    COURT_CORRESPONDENCE_INTERVENER_1_WITNESS_STATEMENTS("courtCorrespondenceIntervener1WitnessStatements"),
    COURT_CORRESPONDENCE_INTERVENER_1_COSTS_STATEMENT("courtCorrespondenceIntervener1CostsStatement"),
    COURT_CORRESPONDENCE_INTERVENER_1_OTHER("courtCorrespondenceIntervener1Other"),
    COURT_CORRESPONDENCE_INTERVENER_2("courtCorrespondenceIntervener2"),
    COURT_CORRESPONDENCE_INTERVENER_2_OPEN_OFFERS("courtCorrespondenceIntervener2OpenOffers"),
    COURT_CORRESPONDENCE_INTERVENER_2_WITNESS_STATEMENTS("courtCorrespondenceIntervener2WitnessStatements"),
    COURT_CORRESPONDENCE_INTERVENER_2_COSTS_STATEMENT("courtCorrespondenceIntervener2CostsStatement"),
    COURT_CORRESPONDENCE_INTERVENER_2_OTHER("courtCorrespondenceIntervener2Other"),
    COURT_CORRESPONDENCE_INTERVENER_3("courtCorrespondenceIntervener3"),
    COURT_CORRESPONDENCE_INTERVENER_3_OPEN_OFFERS("courtCorrespondenceIntervener3OpenOffers"),
    COURT_CORRESPONDENCE_INTERVENER_3_WITNESS_STATEMENTS("courtCorrespondenceIntervener3WitnessStatements"),
    COURT_CORRESPONDENCE_INTERVENER_3_COSTS_STATEMENT("courtCorrespondenceIntervener3CostsStatement"),
    COURT_CORRESPONDENCE_INTERVENER_3_OTHER("courtCorrespondenceIntervener3Other"),
    COURT_CORRESPONDENCE_INTERVENER_4("courtCorrespondenceIntervener4"),
    COURT_CORRESPONDENCE_INTERVENER_4_OPEN_OFFERS("courtCorrespondenceIntervener4OpenOffers"),
    COURT_CORRESPONDENCE_INTERVENER_4_WITNESS_STATEMENTS("courtCorrespondenceIntervener4WitnessStatements"),
    COURT_CORRESPONDENCE_INTERVENER_4_COSTS_STATEMENT("courtCorrespondenceIntervener4 CostsStatement"),
    COURT_CORRESPONDENCE_INTERVENER_4_OTHER("courtCorrespondenceIntervener3Other"),
    COURT_CORRESPONDENCE_OTHER("courtCorrespondenceOther"),
    CONFIDENTIAL_DOCUMENTS("confidentialDocuments"),
    CONFIDENTIAL_DOCUMENTS_APPLICANT("confidentialDocumentsApplicant"),
    CONFIDENTIAL_DOCUMENTS_RESPONDENT("confidentialDocumentsRespondent"),
    CONFIDENTIAL_DOCUMENTS_INTERVENER_1("confidentialDocumentsIntervener1"),
    CONFIDENTIAL_DOCUMENTS_INTERVENER_1_OPEN_OFFERS("confidentialDocumentsIntervener1OpenOffers"),
    CONFIDENTIAL_DOCUMENTS_INTERVENER_1_WITNESS_STATEMENTS("confidentialDocumentsIntervener1WitnessStatements"),
    CONFIDENTIAL_DOCUMENTS_INTERVENER_1_COSTS_STATEMENT("confidentialDocumentsIntervener1CostsStatement"),
    CONFIDENTIAL_DOCUMENTS_INTERVENER_1_OTHER("confidentialDocumentsIntervener1Other"),
    CONFIDENTIAL_DOCUMENTS_INTERVENER_2("confidentialDocumentsIntervener2"),
    CONFIDENTIAL_DOCUMENTS_INTERVENER_2_OPEN_OFFERS("confidentialDocumentsIntervener2OpenOffers"),
    CONFIDENTIAL_DOCUMENTS_INTERVENER_2_WITNESS_STATEMENTS("confidentialDocumentsIntervener2WitnessStatements"),
    CONFIDENTIAL_DOCUMENTS_INTERVENER_2_COSTS_STATEMENT("confidentialDocumentsIntervener2CostsStatement"),
    CONFIDENTIAL_DOCUMENTS_INTERVENER_2_OTHER("confidentialDocumentsIntervener2Other"),
    CONFIDENTIAL_DOCUMENTS_INTERVENER_3("confidentialDocumentsIntervener3"),
    CONFIDENTIAL_DOCUMENTS_INTERVENER_3_OPEN_OFFERS("confidentialDocumentsIntervener3OpenOffers"),
    CONFIDENTIAL_DOCUMENTS_INTERVENER_3_WITNESS_STATEMENTS("confidentialDocumentsIntervener3WitnessStatements"),
    CONFIDENTIAL_DOCUMENTS_INTERVENER_3_COSTS_STATEMENT("confidentialDocumentsIntervener3CostsStatement"),
    CONFIDENTIAL_DOCUMENTS_INTERVENER_3_OTHER("confidentialDocumentsIntervener3Other"),
    CONFIDENTIAL_DOCUMENTS_INTERVENER_4("confidentialDocumentsIntervener4"),
    CONFIDENTIAL_DOCUMENTS_INTERVENER_4_OPEN_OFFERS("confidentialDocumentsIntervener4OpenOffers"),
    CONFIDENTIAL_DOCUMENTS_INTERVENER_4_WITNESS_STATEMENTS("confidentialDocumentsIntervener4WitnessStatements"),
    CONFIDENTIAL_DOCUMENTS_INTERVENER_4_COSTS_STATEMENT("confidentialDocumentsIntervener4CostsStatement"),
    CONFIDENTIAL_DOCUMENTS_INTERVENER_4_OTHER("confidentialDocumentsIntervener4Other"),
    INTERVENER_DOCUMENTS("intervenerDocuments"),
    INTERVENER_DOCUMENTS_INTERVENER_1("intervenerDocumentsIntervener1"),
    INTERVENER_DOCUMENTS_INTERVENER_1_FORM_E("intervenerDocumentsIntervener1FormE"),
    INTERVENER_DOCUMENTS_INTERVENER_1_FORM_G("intervenerDocumentsIntervener1FormG"),
    INTERVENER_DOCUMENTS_INTERVENER_1_REPLIES_TO_QUESTIONNAIRE("intervenerDocumentsIntervener1RepliesToQuestionnaire"),
    INTERVENER_DOCUMENTS_INTERVENER_1_MORTGAGE_CAPACITIES_OR_HOUSING_PARTICULARS(
        "intervenerDocumentsIntervener1MortgageCapacitiesOrHousingParticulars"),
    INTERVENER_DOCUMENTS_INTERVENER_1_OPEN_OFFERS("intervenerDocumentsIntervener1OpenOffers"),
    INTERVENER_DOCUMENTS_INTERVENER_1_S25_STATEMENT("intervenerDocumentsIntervener1S25Statement"),
    INTERVENER_DOCUMENTS_INTERVENER_1_WITNESS_STATEMENTS("intervenerDocumentsIntervener1WitnessStatements"),
    INTERVENER_DOCUMENTS_INTERVENER_1_CERTIFICATES_OF_SERVICE("intervenerDocumentsIntervener1CertificatesOfService"),
    INTERVENER_DOCUMENTS_INTERVENER_1_OTHER("intervenerDocumentsIntervener1Other"),
    INTERVENER_DOCUMENTS_INTERVENER_1_PENSION_PLAN("intervenerDocumentsIntervener1PensionPlan"),
    INTERVENER_DOCUMENTS_INTERVENER_1_COSTS_STATEMENT("intervenerDocumentsIntervener1CostsStatement"),
    INTERVENER_DOCUMENTS_INTERVENER_2("intervenerDocumentsIntervener2"),
    INTERVENER_DOCUMENTS_INTERVENER_2_FORM_E("intervenerDocumentsIntervener2FormE"),
    INTERVENER_DOCUMENTS_INTERVENER_2_FORM_G("intervenerDocumentsIntervener2FormG"),
    INTERVENER_DOCUMENTS_INTERVENER_2_REPLIES_TO_QUESTIONNAIRE("intervenerDocumentsIntervener2RepliesToQuestionnaire"),
    INTERVENER_DOCUMENTS_INTERVENER_2_MORTGAGE_CAPACITIES_OR_HOUSING_PARTICULARS(
        "intervenerDocumentsIntervener2MortgageCapacitiesOrHousingParticulars"),
    INTERVENER_DOCUMENTS_INTERVENER_2_OPEN_OFFERS("intervenerDocumentsIntervener2OpenOffers"),
    INTERVENER_DOCUMENTS_INTERVENER_2_S25_STATEMENT("intervenerDocumentsIntervener2S25Statement"),
    INTERVENER_DOCUMENTS_INTERVENER_2_WITNESS_STATEMENTS("intervenerDocumentsIntervener2WitnessStatements"),
    INTERVENER_DOCUMENTS_INTERVENER_2_CERTIFICATES_OF_SERVICE("intervenerDocumentsIntervener2CertificatesOfService"),
    INTERVENER_DOCUMENTS_INTERVENER_2_OTHER("intervenerDocumentsIntervener2Other"),
    INTERVENER_DOCUMENTS_INTERVENER_2_PENSION_PLAN("intervenerDocumentsIntervener2PensionPlan"),
    INTERVENER_DOCUMENTS_INTERVENER_2_COSTS_STATEMENT("intervenerDocumentsIntervener2CostsStatement"),
    INTERVENER_DOCUMENTS_INTERVENER_3("intervenerDocumentsIntervener3"),
    INTERVENER_DOCUMENTS_INTERVENER_3_FORM_E("intervenerDocumentsIntervener3FormE"),
    INTERVENER_DOCUMENTS_INTERVENER_3_FORM_G("intervenerDocumentsIntervener3FormG"),
    INTERVENER_DOCUMENTS_INTERVENER_3_REPLIES_TO_QUESTIONNAIRE("intervenerDocumentsIntervener3RepliesToQuestionnaire"),
    INTERVENER_DOCUMENTS_INTERVENER_3_MORTGAGE_CAPACITIES_OR_HOUSING_PARTICULARS(
        "intervenerDocumentsIntervener3MortgageCapacitiesOrHousingParticulars"),
    INTERVENER_DOCUMENTS_INTERVENER_3_OPEN_OFFERS("intervenerDocumentsIntervener3OpenOffers"),
    INTERVENER_DOCUMENTS_INTERVENER_3_S25_STATEMENT("intervenerDocumentsIntervener3S25Statement"),
    INTERVENER_DOCUMENTS_INTERVENER_3_WITNESS_STATEMENTS("intervenerDocumentsIntervener3WitnessStatements"),
    INTERVENER_DOCUMENTS_INTERVENER_3_CERTIFICATES_OF_SERVICE("intervenerDocumentsIntervener3CertificatesOfService"),
    INTERVENER_DOCUMENTS_INTERVENER_3_OTHER("intervenerDocumentsIntervener3Other"),
    INTERVENER_DOCUMENTS_INTERVENER_3_PENSION_PLAN("intervenerDocumentsIntervener3PensionPlan"),
    INTERVENER_DOCUMENTS_INTERVENER_3_COSTS_STATEMENT("intervenerDocumentsIntervener3CostsStatement"),
    INTERVENER_DOCUMENTS_INTERVENER_4("intervenerDocumentsIntervener4"),
    INTERVENER_DOCUMENTS_INTERVENER_4_FORM_E("intervenerDocumentsIntervener4FormE"),
    INTERVENER_DOCUMENTS_INTERVENER_4_FORM_G("intervenerDocumentsIntervener4FormG"),
    INTERVENER_DOCUMENTS_INTERVENER_4_REPLIES_TO_QUESTIONNAIRE("intervenerDocumentsIntervener4RepliesToQuestionnaire"),
    INTERVENER_DOCUMENTS_INTERVENER_4_MORTGAGE_CAPACITIES_OR_HOUSING_PARTICULARS(
        "intervenerDocumentsIntervener4MortgageCapacitiesOrHousingParticulars"),
    INTERVENER_DOCUMENTS_INTERVENER_4_OPEN_OFFERS("intervenerDocumentsIntervener4OpenOffers"),
    INTERVENER_DOCUMENTS_INTERVENER_4_S25_STATEMENT("intervenerDocumentsIntervener4S25Statement"),
    INTERVENER_DOCUMENTS_INTERVENER_4_WITNESS_STATEMENTS("intervenerDocumentsIntervener4WitnessStatements"),
    INTERVENER_DOCUMENTS_INTERVENER_4_CERTIFICATES_OF_SERVICE("intervenerDocumentsIntervener4CertificatesOfService"),
    INTERVENER_DOCUMENTS_INTERVENER_4_OTHER("intervenerDocumentsIntervener4MiscellaneousOrOther"),
    INTERVENER_DOCUMENTS_INTERVENER_4_PENSION_PLAN("intervenerDocumentsIntervener4PensionPlan"),
    INTERVENER_DOCUMENTS_INTERVENER_4_COSTS_STATEMENT("intervenerDocumentsIntervener4CostsStatement"),
    DIVORCE_DOCUMENTS("divorceDocuments"),
    DIVORCE_DOCUMENTS_APPLICATION_OR_PETITION("divorceDocumentsApplicationOrPetition"),
    DIVORCE_DOCUMENTS_CONDITIONAL_ORDER_OR_DECREE_NISI("divorceDocumentsConditionalOrderOrDecreeNisi"),
    DIVORCE_DOCUMENTS_FINAL_ORDER_OR_DECREE_ABSOLUTE("divorceDocumentsFinalOrderOrDecreeAbsolute"),
    ADMINISTRATIVE_DOCUMENTS("administrativeDocuments"),
    ADMINISTRATIVE_DOCUMENTS_COVERSHEETS("administrativeDocumentsCoversheets"),
    ADMINISTRATIVE_DOCUMENTS_JUDICIAL_NOTES("administrativeDocumentsJudicialNotes"),
    ADMINISTRATIVE_DOCUMENTS_ATTENDANCE_SHEETS("administrativeDocumentsAttendanceSheets"),
    ADMINISTRATIVE_DOCUMENTS_OTHER("administrativeDocumentsOther"),
    ADMINISTRATIVE_DOCUMENTS_TRANSITIONAL("administrativeDocumentsTransitional"),
    JUDGMENT_OR_TRANSCRIPT("judgmentOrTranscript"),
    CASE_DOCUMENTS("caseDocuments"),
    SHARED("shared"),
    APPLICATIONS_OTHER_APPLICATION_APPLICATION_1("applicationsGeneralApplicationsapp1"),
    APPLICATIONS_OTHER_APPLICATION_APPLICATION_2("applicationsGeneralApplicationsapp2"),
    APPLICATIONS_OTHER_APPLICATION_APPLICATION_3("applicationsGeneralApplicationsapp3"),
    APPLICATIONS_OTHER_APPLICATION_APPLICATION_4("applicationsGeneralApplicationsapp4"),
    APPLICATIONS_OTHER_APPLICATION_APPLICATION_5("applicationsGeneralApplicationsapp5"),
    APPLICATIONS_OTHER_APPLICATION_APPLICATION_6("applicationsGeneralApplicationsapp6"),
    APPLICATIONS_OTHER_APPLICATION_APPLICATION_7("applicationsGeneralApplicationsapp7"),
    APPLICATIONS_OTHER_APPLICATION_APPLICATION_8("applicationsGeneralApplicationsapp8"),
    APPLICATIONS_OTHER_APPLICATION_APPLICATION_9("applicationsGeneralApplicationsapp9"),
    APPLICATIONS_OTHER_APPLICATION_APPLICATION_10("applicationsGeneralApplicationsapp10"),
    DUPLICATED_GENERAL_ORDERS("duplicatedGeneralOrders"),
    SYSTEM_DUPLICATES("systemDuplicates"),
    APPLICATIONS_OTHER_APPLICATION_OVERFLOW("applicationsGeneralApplicationsOverflow"),

    APPLICANT_DOCUMENTS_SEND_ORDERS("applicantDocumentsSendOrders"),
    APPLICANT_DOCUMENTS_SEND_ORDERS_OVERFLOW("applicantDocumentsSendOrdersOverflow"),
    RESPONDENT_DOCUMENTS_SEND_ORDERS("respondentDocumentsSendOrders"),
    RESPONDENT_DOCUMENTS_SEND_ORDERS_OVERFLOW("respondentDocumentsSendOrdersOverflow"),
    INTERVENER_DOCUMENTS_INTERVENER_1_SEND_ORDERS("intervenerDocumentsIntervener1SendOrders"),
    INTERVENER_DOCUMENTS_INTERVENER_1_SEND_ORDERS_OVERFLOW("intervenerDocumentsIntervener1SendOrdersOverflow"),
    INTERVENER_DOCUMENTS_INTERVENER_2_SEND_ORDERS("intervenerDocumentsIntervener2SendOrders"),
    INTERVENER_DOCUMENTS_INTERVENER_2_SEND_ORDERS_OVERFLOW("intervenerDocumentsIntervener2SendOrdersOverflow"),
    INTERVENER_DOCUMENTS_INTERVENER_3_SEND_ORDERS("intervenerDocumentsIntervener3SendOrders"),
    INTERVENER_DOCUMENTS_INTERVENER_3_SEND_ORDERS_OVERFLOW("intervenerDocumentsIntervener3SendOrdersOverflow"),
    INTERVENER_DOCUMENTS_INTERVENER_4_SEND_ORDERS("intervenerDocumentsIntervener4SendOrders"),
    INTERVENER_DOCUMENTS_INTERVENER_4_SEND_ORDERS_OVERFLOW("intervenerDocumentsIntervener4SendOrdersOverflow"),

    APPLICANT_DOCUMENTS_CONSENT_ORDERS("applicantDocumentsConsentOrders"),
    APPLICANT_DOCUMENTS_CONSENT_ORDERS_OVERFLOW("applicantDocumentsConsentOrdersOverflow"),
    RESPONDENT_DOCUMENTS_CONSENT_ORDERS("respondentDocumentsConsentOrders"),
    RESPONDENT_DOCUMENTS_CONSENT_ORDERS_OVERFLOW("respondentDocumentsConsentOrdersOverflow"),
    INTERVENER_DOCUMENTS_INTERVENER_1_CONSENT_ORDERS("intervenerDocumentsIntervener1ConsentOrders"),
    INTERVENER_DOCUMENTS_INTERVENER_1_CONSENT_ORDERS_OVERFLOW("intervenerDocumentsIntervener1ConsentOrdersOverflow"),
    INTERVENER_DOCUMENTS_INTERVENER_2_CONSENT_ORDERS("intervenerDocumentsIntervener2ConsentOrders"),
    INTERVENER_DOCUMENTS_INTERVENER_2_CONSENT_ORDERS_OVERFLOW("intervenerDocumentsIntervener2ConsentOrdersOverflow"),
    INTERVENER_DOCUMENTS_INTERVENER_3_CONSENT_ORDERS("intervenerDocumentsIntervener3ConsentOrders"),
    INTERVENER_DOCUMENTS_INTERVENER_3_CONSENT_ORDERS_OVERFLOW("intervenerDocumentsIntervener3ConsentOrdersOverflow"),
    INTERVENER_DOCUMENTS_INTERVENER_4_CONSENT_ORDERS("intervenerDocumentsIntervener4ConsentOrders"),
    INTERVENER_DOCUMENTS_INTERVENER_4_CONSENT_ORDERS_OVERFLOW("intervenerDocumentsIntervener4ConsentOrdersOverflow"),

    UNCATEGORISED(null);


    private final String documentCategoryId;

    public String getDocumentCategoryId() {
        return documentCategoryId;
    }
}
