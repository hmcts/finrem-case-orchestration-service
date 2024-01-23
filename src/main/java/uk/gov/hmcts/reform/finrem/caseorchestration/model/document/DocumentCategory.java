package uk.gov.hmcts.reform.finrem.caseorchestration.model.document;

import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public enum DocumentCategory {
    APPLICATIONS("applications"),
    APPLICATIONS_FORM_A_OR_A1_OR_B("applicationsFormAOrA1OrB"),
    APPLICATIONS_GENERAL_APPLICATIONS("applicationsGeneralApplications"),
    APPLICATIONS_CONSENT_APPLICATION("applicationsConsentApplication"),
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
    APPLICANT_DOCUMENTS_OPEN_OFFERS("applicantDocumentsOpenOffers"),
    APPLICANT_DOCUMENTS_S25_STATEMENT("applicantDocumentsS25Statement"),
    APPLICANT_DOCUMENTS_WITNESS_STATEMENTS("applicantDocumentsWitnessStatements"),
    APPLICANT_DOCUMENTS_HOUSING_PARTICULARS("applicantDocumentsHousingParticulars"),
    APPLICANT_DOCUMENTS_CERTIFICATES_OF_SERVICE("applicantDocumentsCertificatesOfService"),
    APPLICANT_DOCUMENTS_MISCELLANEOUS_OR_OTHER("applicantDocumentsMiscellaneousOrOther"),
    APPLICANT_DOCUMENTS_PENSION_PLAN("applicantDocumentsPensionPlan"),
    RESPONDENT_DOCUMENTS("respondentDocuments"),
    RESPONDENT_DOCUMENTS_FORM_E("respondentDocumentsFormE"),
    RESPONDENT_DOCUMENTS_FORM_G("respondentDocumentsFormG"),
    RESPONDENT_DOCUMENTS_REPLIES_TO_QUESTIONNAIRE("respondentDocumentsRepliesToQuestionnaire"),
    RESPONDENT_DOCUMENTS_OPEN_OFFERS("respondentDocumentsOpenOffers"),
    RESPONDENT_DOCUMENTS_S25_STATEMENT("respondentDocumentsS25Statement"),
    RESPONDENT_DOCUMENTS_WITNESS_STATEMENTS("respondentDocumentsWitnessStatements"),
    RESPONDENT_DOCUMENTS_HOUSING_PARTICULARS("respondentDocumentsHousingParticulars"),
    RESPONDENT_DOCUMENTS_CERTIFICATES_OF_SERVICE("respondentDocumentsCertificatesOfService"),
    RESPONDENT_DOCUMENTS_MISCELLANEOUS_OR_OTHER("respondentDocumentsMiscellaneousOrOther"),
    RESPONDENT_DOCUMENTS_PENSION_PLAN("respondentDocumentsPensionPlan"),
    LIP_OR_SCANNED_DOCUMENTS("lipOrScannedDocuments"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE("fdrDocumentsAndFdrBundle"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT("fdrDocumentsAndFdrBundleApplicant"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_FDR_BUNDLE("fdrDocumentsAndFdrBundleApplicantFdrBundle"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_POSITION_STATEMENTS("fdrDocumentsAndFdrBundleApplicantPositionStatements"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_ES1("fdrDocumentsAndFdrBundleApplicantEs1"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_ES2("fdrDocumentsAndFdrBundleApplicantEs2"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_WITHOUT_PREJUDICE_OFFERS("fdrDocumentsAndFdrBundleApplicantWithoutPrejudiceOffers"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_CHRONOLOGY("fdrDocumentsAndFdrBundleApplicantChronology"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_QUESTIONNAIRES("fdrDocumentsAndFdrBundleApplicantQuestionnaires"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_FAMILY_HOME_VALUATION("fdrDocumentsAndFdrBundleApplicantFamilyHomeValuation"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_PRE_HEARING_DRAFT_ORDER("fdrDocumentsAndFdrBundleApplicantPreHearingDraftOrder"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_APPLICANT_SKELETON_ARGUMENT("fdrDocumentsAndFdrBundleApplicantSkeletonArgument"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT("fdrDocumentsAndFdrBundleRespondent"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_FDR_BUNDLE("fdrDocumentsAndFdrBundleRespondentFdrBundle"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_POSITION_STATEMENTS("fdrDocumentsAndFdrBundleRespondentPositionStatements"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_ES1("fdrDocumentsAndFdrBundleRespondentEs1"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_ES2("fdrDocumentsAndFdrBundleRespondentEs2"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_WITHOUT_PREJUDICE_OFFERS("fdrDocumentsAndFdrBundleRespondentWithoutPrejudiceOffers"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_CHRONOLOGY("fdrDocumentsAndFdrBundleRespondentChronology"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_QUESTIONNAIRES("fdrDocumentsAndFdrBundleRespondentQuestionnaires"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_FAMILY_HOME_VALUATION("fdrDocumentsAndFdrBundleRespondentFamilyHomeValuation"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_PRE_HEARING_DRAFT_ORDER("fdrDocumentsAndFdrBundleRespondentPreHearingDraftOrder"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_SKELETON_ARGUMENT("fdrDocumentsAndFdrBundleRespondentSkeletonArgument"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_1("fdrDocumentsAndFdrBundleIntervener1"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_1_FDR_BUNDLE("fdrDocumentsAndFdrBundleIntervener1FdrBundle"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_1_POSITION_STATEMENTS("fdrDocumentsAndFdrBundleIntervener1PositionStatements"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_1_ES1("fdrDocumentsAndFdrBundleIntervener1Es1"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_1_ES2("fdrDocumentsAndFdrBundleIntervener1Es2"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_1_WITHOUT_PREJUDICE_OFFERS("fdrDocumentsAndFdrBundleIntervener1WithoutPrejudiceOffers"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_1_CHRONOLOGY("fdrDocumentsAndFdrBundleIntervener1Chronology"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_1_QUESTIONNAIRES("fdrDocumentsAndFdrBundleIntervener1Questionnaires"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_1_FAMILY_HOME_VALUATION("fdrDocumentsAndFdrBundleIntervener1FamilyHomeValuation"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_1_PRE_HEARING_DRAFT_ORDER("fdrDocumentsAndFdrBundleIntervener1PreHearingDraftOrder"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_1_SKELETON_ARGUMENT("fdrDocumentsAndFdrBundleIntervener1SkeletonArgument"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_2("fdrDocumentsAndFdrBundleIntervener2"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_2_FDR_BUNDLE("fdrDocumentsAndFdrBundleIntervener2FdrBundle"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_2_POSITION_STATEMENTS("fdrDocumentsAndFdrBundleIntervener2PositionStatements"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_2_ES1("fdrDocumentsAndFdrBundleIntervener2Es1"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_2_ES2("fdrDocumentsAndFdrBundleIntervener2Es2"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_2_WITHOUT_PREJUDICE_OFFERS("fdrDocumentsAndFdrBundleIntervener2WithoutPrejudiceOffers"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_2_CHRONOLOGY("fdrDocumentsAndFdrBundleIntervener2Chronology"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_2_QUESTIONNAIRES("fdrDocumentsAndFdrBundleIntervener2Questionnaires"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_2_FAMILY_HOME_VALUATION("fdrDocumentsAndFdrBundleIntervener2FamilyHomeValuation"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_2_PRE_HEARING_DRAFT_ORDER("fdrDocumentsAndFdrBundleIntervener2PreHearingDraftOrder"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_2_SKELETON_ARGUMENT("fdrDocumentsAndFdrBundleIntervener2SkeletonArgument"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_3("fdrDocumentsAndFdrBundleIntervener3"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_3_FDR_BUNDLE("fdrDocumentsAndFdrBundleIntervener3FdrBundle"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_3_POSITION_STATEMENTS("fdrDocumentsAndFdrBundleIntervener3PositionStatements"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_3_ES1("fdrDocumentsAndFdrBundleIntervener3Es1"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_3_ES2("fdrDocumentsAndFdrBundleIntervener3Es2"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_3_WITHOUT_PREJUDICE_OFFERS("fdrDocumentsAndFdrBundleIntervener3WithoutPrejudiceOffers"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_3_CHRONOLOGY("fdrDocumentsAndFdrBundleIntervener3Chronology"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_3_QUESTIONNAIRES("fdrDocumentsAndFdrBundleIntervener3Questionnaires"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_3_FAMILY_HOME_VALUATION("fdrDocumentsAndFdrBundleIntervener3FamilyHomeValuation"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_3_PRE_HEARING_DRAFT_ORDER("fdrDocumentsAndFdrBundleIntervener3PreHearingDraftOrder"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_3_SKELETON_ARGUMENT("fdrDocumentsAndFdrBundleIntervener3SkeletonArgument"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_4("fdrDocumentsAndFdrBundleIntervener4"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_4_FDR_BUNDLE("fdrDocumentsAndFdrBundleIntervener4FdrBundle"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_4_POSITION_STATEMENTS("fdrDocumentsAndFdrBundleIntervener4PositionStatements"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_4_ES1("fdrDocumentsAndFdrBundleIntervener4Es1"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_4_ES2("fdrDocumentsAndFdrBundleIntervener4Es2"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_4_WITHOUT_PREJUDICE_OFFERS("fdrDocumentsAndFdrBundleIntervener4WithoutPrejudiceOffers"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_4_CHRONOLOGY("fdrDocumentsAndFdrBundleIntervener4Chronology"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_4_QUESTIONNAIRES("fdrDocumentsAndFdrBundleIntervener4Questionnaires"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_4_FAMILY_HOME_VALUATION("fdrDocumentsAndFdrBundleIntervener4FamilyHomeValuation"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_4_PRE_HEARING_DRAFT_ORDER("fdrDocumentsAndFdrBundleIntervener4PreHearingDraftOrder"),
    FDR_DOCUMENTS_AND_FDR_BUNDLE_INTERVENER_4_SKELETON_ARGUMENT("fdrDocumentsAndFdrBundleIntervener4SkeletonArgument"),
    HEARING_DOCUMENTS("hearingDocuments"),
    HEARING_DOCUMENTS_APPLICANT("hearingDocumentsApplicant"),
    HEARING_DOCUMENTS_APPLICANT_CASE_SUMMARY("hearingDocumentsApplicantCaseSummary"),
    HEARING_DOCUMENTS_APPLICANT_POSITION_STATEMENT("hearingDocumentsApplicantPositionStatement"),
    HEARING_DOCUMENTS_APPLICANT_CONCISE_STATEMENT_OF_ISSUES("hearingDocumentsApplicantConciseStatementOfIssues"),
    HEARING_DOCUMENTS_APPLICANT_ES1("hearingDocumentsApplicantEs1"),
    HEARING_DOCUMENTS_APPLICANT_ES2("hearingDocumentsApplicantEs2"),
    HEARING_DOCUMENTS_APPLICANT_CHRONOLOGY("hearingDocumentsApplicantChronology"),
    HEARING_DOCUMENTS_APPLICANT_QUESTIONNAIRES("hearingDocumentsApplicantQuestionnaires"),
    HEARING_DOCUMENTS_APPLICANT_FAMILY_HOME_VALUATION("hearingDocumentsApplicantFamilyHomeValuation"),
    HEARING_DOCUMENTS_APPLICANT_MORTGAGE_CAPACITIES("hearingDocumentsApplicantMortgageCapacities"),
    HEARING_DOCUMENTS_APPLICANT_COSTS_ESTIMATES_OR_FORM_H_OR_FORM_H1("hearingDocumentsApplicantCostsEstimatesOrFormHOrFormH1"),
    HEARING_DOCUMENTS_APPLICANT_PRE_HEARING_DRAFT_ORDER("hearingDocumentsApplicantPreHearingDraftOrder"),
    HEARING_DOCUMENTS_APPLICANT_SKELETON_ARGUMENT("hearingDocumentsApplicantSkeletonArgument"),
    HEARING_DOCUMENTS_RESPONDENT("hearingDocumentsRespondent"),
    HEARING_DOCUMENTS_RESPONDENT_CASE_SUMMARY("hearingDocumentsRespondentCaseSummary"),
    HEARING_DOCUMENTS_RESPONDENT_POSITION_STATEMENT("hearingDocumentsRespondentPositionStatement"),
    HEARING_DOCUMENTS_RESPONDENT_CONCISE_STATEMENT_OF_ISSUES("hearingDocumentsRespondentConciseStatementOfIssues"),
    HEARING_DOCUMENTS_RESPONDENT_ES1("hearingDocumentsRespondentEs1"),
    HEARING_DOCUMENTS_RESPONDENT_ES2("hearingDocumentsRespondentEs2"),
    HEARING_DOCUMENTS_RESPONDENT_CHRONOLOGY("hearingDocumentsRespondentChronology"),
    HEARING_DOCUMENTS_RESPONDENT_QUESTIONNAIRES("hearingDocumentsRespondentQuestionnaires"),
    HEARING_DOCUMENTS_RESPONDENT_FAMILY_HOME_VALUATION("hearingDocumentsRespondentFamilyHomeValuation"),
    HEARING_DOCUMENTS_RESPONDENT_MORTGAGE_CAPACITIES("hearingDocumentsRespondentMortgageCapacities"),
    HEARING_DOCUMENTS_RESPONDENT_COSTS_ESTIMATES_OR_FORM_H_OR_FORM_H1("hearingDocumentsRespondentCostsEstimatesOrFormHOrFormH1"),
    HEARING_DOCUMENTS_RESPONDENT_PRE_HEARING_DRAFT_ORDER("hearingDocumentsRespondentPreHearingDraftOrder"),
    HEARING_DOCUMENTS_RESPONDENT_SKELETON_ARGUMENT("hearingDocumentsRespondentSkeletonArgument"),
    HEARING_DOCUMENTS_INTERVENER_1("hearingDocumentsIntervener1"),
    HEARING_DOCUMENTS_INTERVENER_1_CASE_SUMMARY("hearingDocumentsIntervener1CaseSummary"),
    HEARING_DOCUMENTS_INTERVENER_1_POSITION_STATEMENT("hearingDocumentsIntervener1PositionStatement"),
    HEARING_DOCUMENTS_INTERVENER_1_CONCISE_STATEMENT_OF_ISSUES("hearingDocumentsIntervener1ConciseStatementOfIssues"),
    HEARING_DOCUMENTS_INTERVENER_1_ES1("hearingDocumentsIntervener1Es1"),
    HEARING_DOCUMENTS_INTERVENER_1_ES2("hearingDocumentsIntervener1Es2"),
    HEARING_DOCUMENTS_INTERVENER_1_CHRONOLOGY("hearingDocumentsIntervener1Chronology"),
    HEARING_DOCUMENTS_INTERVENER_1_QUESTIONNAIRES("hearingDocumentsIntervener1Questionnaires"),
    HEARING_DOCUMENTS_INTERVENER_1_FAMILY_HOME_VALUATION("hearingDocumentsIntervener1FamilyHomeValuation"),
    HEARING_DOCUMENTS_INTERVENER_1_MORTGAGE_CAPACITIES("hearingDocumentsIntervener1MortgageCapacities"),
    HEARING_DOCUMENTS_INTERVENER_1_COSTS_ESTIMATES_OR_FORM_H_OR_FORM_H1("hearingDocumentsIntervener1CostsEstimatesOrFormHOrFormH1"),
    HEARING_DOCUMENTS_INTERVENER_1_PRE_HEARING_DRAFT_ORDER("hearingDocumentsIntervener1PreHearingDraftOrder"),
    HEARING_DOCUMENTS_INTERVENER_1_SKELETON_ARGUMENT("hearingDocumentsIntervener1SkeletonArgument"),
    HEARING_DOCUMENTS_INTERVENER_2("hearingDocumentsIntervener2"),
    HEARING_DOCUMENTS_INTERVENER_2_CASE_SUMMARY("hearingDocumentsIntervener2CaseSummary"),
    HEARING_DOCUMENTS_INTERVENER_2_POSITION_STATEMENT("hearingDocumentsIntervener2PositionStatement"),
    HEARING_DOCUMENTS_INTERVENER_2_CONCISE_STATEMENT_OF_ISSUES("hearingDocumentsIntervener2ConciseStatementOfIssues"),
    HEARING_DOCUMENTS_INTERVENER_2_ES1("hearingDocumentsIntervener2Es1"),
    HEARING_DOCUMENTS_INTERVENER_2_ES2("hearingDocumentsIntervener2Es2"),
    HEARING_DOCUMENTS_INTERVENER_2_CHRONOLOGY("hearingDocumentsIntervener2Chronology"),
    HEARING_DOCUMENTS_INTERVENER_2_QUESTIONNAIRES("hearingDocumentsIntervener2Questionnaires"),
    HEARING_DOCUMENTS_INTERVENER_2_FAMILY_HOME_VALUATION("hearingDocumentsIntervener2FamilyHomeValuation"),
    HEARING_DOCUMENTS_INTERVENER_2_MORTGAGE_CAPACITIES("hearingDocumentsIntervener2MortgageCapacities"),
    HEARING_DOCUMENTS_INTERVENER_2_COSTS_ESTIMATES_OR_FORM_H_OR_FORM_H1("hearingDocumentsIntervener2CostsEstimatesOrFormHOrFormH1"),
    HEARING_DOCUMENTS_INTERVENER_2_PRE_HEARING_DRAFT_ORDER("hearingDocumentsIntervener2PreHearingDraftOrder"),
    HEARING_DOCUMENTS_INTERVENER_2_SKELETON_ARGUMENT("hearingDocumentsIntervener2SkeletonArgument"),
    HEARING_DOCUMENTS_INTERVENER_3("hearingDocumentsIntervener3"),
    HEARING_DOCUMENTS_INTERVENER_3_CASE_SUMMARY("hearingDocumentsIntervener3CaseSummary"),
    HEARING_DOCUMENTS_INTERVENER_3_POSITION_STATEMENT("hearingDocumentsIntervener3PositionStatement"),
    HEARING_DOCUMENTS_INTERVENER_3_CONCISE_STATEMENT_OF_ISSUES("hearingDocumentsIntervener3ConciseStatementOfIssues"),
    HEARING_DOCUMENTS_INTERVENER_3_ES1("hearingDocumentsIntervener3Es1"),
    HEARING_DOCUMENTS_INTERVENER_3_ES2("hearingDocumentsIntervener3Es2"),
    HEARING_DOCUMENTS_INTERVENER_3_CHRONOLOGY("hearingDocumentsIntervener3Chronology"),
    HEARING_DOCUMENTS_INTERVENER_3_QUESTIONNAIRES("hearingDocumentsIntervener3Questionnaires"),
    HEARING_DOCUMENTS_INTERVENER_3_FAMILY_HOME_VALUATION("hearingDocumentsIntervener3FamilyHomeValuation"),
    HEARING_DOCUMENTS_INTERVENER_3_MORTGAGE_CAPACITIES("hearingDocumentsIntervener3MortgageCapacities"),
    HEARING_DOCUMENTS_INTERVENER_3_COSTS_ESTIMATES_OR_FORM_H_OR_FORM_H1("hearingDocumentsIntervener3CostsEstimatesOrFormHOrFormH1"),
    HEARING_DOCUMENTS_INTERVENER_3_PRE_HEARING_DRAFT_ORDER("hearingDocumentsIntervener3PreHearingDraftOrder"),
    HEARING_DOCUMENTS_INTERVENER_3_SKELETON_ARGUMENT("hearingDocumentsIntervener3SkeletonArgument"),
    HEARING_DOCUMENTS_INTERVENER_4("hearingDocumentsIntervener4"),
    HEARING_DOCUMENTS_INTERVENER_4_CASE_SUMMARY("hearingDocumentsIntervener4CaseSummary"),
    HEARING_DOCUMENTS_INTERVENER_4_POSITION_STATEMENT("hearingDocumentsIntervener4PositionStatement"),
    HEARING_DOCUMENTS_INTERVENER_4_CONCISE_STATEMENT_OF_ISSUES("hearingDocumentsIntervener4ConciseStatementOfIssues"),
    HEARING_DOCUMENTS_INTERVENER_4_ES1("hearingDocumentsIntervener4Es1"),
    HEARING_DOCUMENTS_INTERVENER_4_ES2("hearingDocumentsIntervener4Es2"),
    HEARING_DOCUMENTS_INTERVENER_4_CHRONOLOGY("hearingDocumentsIntervener4Chronology"),
    HEARING_DOCUMENTS_INTERVENER_4_QUESTIONNAIRES("hearingDocumentsIntervener4Questionnaires"),
    HEARING_DOCUMENTS_INTERVENER_4_FAMILY_HOME_VALUATION("hearingDocumentsIntervener4FamilyHomeValuation"),
    HEARING_DOCUMENTS_INTERVENER_4_MORTGAGE_CAPACITIES("hearingDocumentsIntervener4MortgageCapacities"),
    HEARING_DOCUMENTS_INTERVENER_4_COSTS_ESTIMATES_OR_FORM_H_OR_FORM_H1("hearingDocumentsIntervener4CostsEstimatesOrFormHOrFormH1"),
    HEARING_DOCUMENTS_INTERVENER_4_PRE_HEARING_DRAFT_ORDER("hearingDocumentsIntervener4PreHearingDraftOrder"),
    HEARING_DOCUMENTS_INTERVENER_4_SKELETON_ARGUMENT("hearingDocumentsIntervener4SkeletonArgument"),
    HEARING_DOCUMENTS_WITNESS_SUMMONS("hearingDocumentsWitnessSummons"),
    HEARING_BUNDLE("hearingBundle"),
    REPORTS("reports"),
    REPORTS_EXPERT_REPORTS("reportsExpertReports"),
    REPORTS_PENSION_REPORTS("reportsPensionReports"),
    CORRESPONDENCE("correspondence"),
    CORRESPONDENCE_APPLICANT("correspondenceApplicant"),
    CORRESPONDENCE_RESPONDENT("correspondenceRespondent"),
    CORRESPONDENCE_INTERVENER_1("correspondenceIntervener1"),
    CORRESPONDENCE_INTERVENER_2("correspondenceIntervener2"),
    CORRESPONDENCE_INTERVENER_3("correspondenceIntervener3"),
    CORRESPONDENCE_INTERVENER_4("correspondenceIntervener4"),
    CORRESPONDENCE_OTHER("correspondenceOther"),
    CONFIDENTIAL_DOCUMENTS("confidentialDocuments"),
    CONFIDENTIAL_DOCUMENTS_APPLICANT("confidentialDocumentsApplicant"),
    CONFIDENTIAL_DOCUMENTS_RESPONDENT("confidentialDocumentsRespondent"),
    CONFIDENTIAL_DOCUMENTS_INTERVENER_1("confidentialDocumentsIntervener1"),
    CONFIDENTIAL_DOCUMENTS_INTERVENER_2("confidentialDocumentsIntervener2"),
    CONFIDENTIAL_DOCUMENTS_INTERVENER_3("confidentialDocumentsIntervener3"),
    CONFIDENTIAL_DOCUMENTS_INTERVENER_4("confidentialDocumentsIntervener4"),
    INTERVENER_DOCUMENTS("intervenerDocuments"),
    INTERVENER_DOCUMENTS_INTERVENER_1("intervenerDocumentsIntervener1"),
    INTERVENER_DOCUMENTS_INTERVENER_1_FORM_E("intervenerDocumentsIntervener1FormE"),
    INTERVENER_DOCUMENTS_INTERVENER_1_FORM_G("intervenerDocumentsIntervener1FormG"),
    INTERVENER_DOCUMENTS_INTERVENER_1_REPLIES_TO_QUESTIONNAIRE("intervenerDocumentsIntervener1RepliesToQuestionnaire"),
    INTERVENER_DOCUMENTS_INTERVENER_1_OPEN_OFFERS("intervenerDocumentsIntervener1OpenOffers"),
    INTERVENER_DOCUMENTS_INTERVENER_1_S25_STATEMENT("intervenerDocumentsIntervener1S25Statement"),
    INTERVENER_DOCUMENTS_INTERVENER_1_WITNESS_STATEMENTS("intervenerDocumentsIntervener1WitnessStatements"),
    INTERVENER_DOCUMENTS_INTERVENER_1_HOUSING_PARTICULARS("intervenerDocumentsIntervener1HousingParticulars"),
    INTERVENER_DOCUMENTS_INTERVENER_1_CERTIFICATES_OF_SERVICE("intervenerDocumentsIntervener1CertificatesOfService"),
    INTERVENER_DOCUMENTS_INTERVENER_1_MISCELLANEOUS_OR_OTHER("intervenerDocumentsIntervener1MiscellaneousOrOther"),
    INTERVENER_DOCUMENTS_INTERVENER_1_PENSION_PLAN("intervenerDocumentsIntervener1PensionPlan"),
    INTERVENER_DOCUMENTS_INTERVENER_2("intervenerDocumentsIntervener2"),
    INTERVENER_DOCUMENTS_INTERVENER_2_FORM_E("intervenerDocumentsIntervener2FormE"),
    INTERVENER_DOCUMENTS_INTERVENER_2_FORM_G("intervenerDocumentsIntervener2FormG"),
    INTERVENER_DOCUMENTS_INTERVENER_2_REPLIES_TO_QUESTIONNAIRE("intervenerDocumentsIntervener2RepliesToQuestionnaire"),
    INTERVENER_DOCUMENTS_INTERVENER_2_OPEN_OFFERS("intervenerDocumentsIntervener2OpenOffers"),
    INTERVENER_DOCUMENTS_INTERVENER_2_S25_STATEMENT("intervenerDocumentsIntervener2S25Statement"),
    INTERVENER_DOCUMENTS_INTERVENER_2_WITNESS_STATEMENTS("intervenerDocumentsIntervener2WitnessStatements"),
    INTERVENER_DOCUMENTS_INTERVENER_2_HOUSING_PARTICULARS("intervenerDocumentsIntervener2HousingParticulars"),
    INTERVENER_DOCUMENTS_INTERVENER_2_CERTIFICATES_OF_SERVICE("intervenerDocumentsIntervener2CertificatesOfService"),
    INTERVENER_DOCUMENTS_INTERVENER_2_MISCELLANEOUS_OR_OTHER("intervenerDocumentsIntervener2MiscellaneousOrOther"),
    INTERVENER_DOCUMENTS_INTERVENER_2_PENSION_PLAN("intervenerDocumentsIntervener2PensionPlan"),
    INTERVENER_DOCUMENTS_INTERVENER_3("intervenerDocumentsIntervener3"),
    INTERVENER_DOCUMENTS_INTERVENER_3_FORM_E("intervenerDocumentsIntervener3FormE"),
    INTERVENER_DOCUMENTS_INTERVENER_3_FORM_G("intervenerDocumentsIntervener3FormG"),
    INTERVENER_DOCUMENTS_INTERVENER_3_REPLIES_TO_QUESTIONNAIRE("intervenerDocumentsIntervener3RepliesToQuestionnaire"),
    INTERVENER_DOCUMENTS_INTERVENER_3_OPEN_OFFERS("intervenerDocumentsIntervener3OpenOffers"),
    INTERVENER_DOCUMENTS_INTERVENER_3_S25_STATEMENT("intervenerDocumentsIntervener3S25Statement"),
    INTERVENER_DOCUMENTS_INTERVENER_3_WITNESS_STATEMENTS("intervenerDocumentsIntervener3WitnessStatements"),
    INTERVENER_DOCUMENTS_INTERVENER_3_HOUSING_PARTICULARS("intervenerDocumentsIntervener3HousingParticulars"),
    INTERVENER_DOCUMENTS_INTERVENER_3_CERTIFICATES_OF_SERVICE("intervenerDocumentsIntervener3CertificatesOfService"),
    INTERVENER_DOCUMENTS_INTERVENER_3_MISCELLANEOUS_OR_OTHER("intervenerDocumentsIntervener3MiscellaneousOrOther"),
    INTERVENER_DOCUMENTS_INTERVENER_3_PENSION_PLAN("intervenerDocumentsIntervener3PensionPlan"),
    INTERVENER_DOCUMENTS_INTERVENER_4("intervenerDocumentsIntervener4"),
    INTERVENER_DOCUMENTS_INTERVENER_4_FORM_E("intervenerDocumentsIntervener4FormE"),
    INTERVENER_DOCUMENTS_INTERVENER_4_FORM_G("intervenerDocumentsIntervener4FormG"),
    INTERVENER_DOCUMENTS_INTERVENER_4_REPLIES_TO_QUESTIONNAIRE("intervenerDocumentsIntervener4RepliesToQuestionnaire"),
    INTERVENER_DOCUMENTS_INTERVENER_4_OPEN_OFFERS("intervenerDocumentsIntervener4OpenOffers"),
    INTERVENER_DOCUMENTS_INTERVENER_4_S25_STATEMENT("intervenerDocumentsIntervener4S25Statement"),
    INTERVENER_DOCUMENTS_INTERVENER_4_WITNESS_STATEMENTS("intervenerDocumentsIntervener4WitnessStatements"),
    INTERVENER_DOCUMENTS_INTERVENER_4_HOUSING_PARTICULARS("intervenerDocumentsIntervener4HousingParticulars"),
    INTERVENER_DOCUMENTS_INTERVENER_4_CERTIFICATES_OF_SERVICE("intervenerDocumentsIntervener4CertificatesOfService"),
    INTERVENER_DOCUMENTS_INTERVENER_4_MISCELLANEOUS_OR_OTHER("intervenerDocumentsIntervener4MiscellaneousOrOther"),
    INTERVENER_DOCUMENTS_INTERVENER_4_PENSION_PLAN("intervenerDocumentsIntervener4PensionPlan"),
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
    APPLICATIONS_GENERAL_APPLICATIONS_APPLICATION_1("applicationsGeneralApplicationsapp1"),
    APPLICATIONS_GENERAL_APPLICATIONS_APPLICATION_2("applicationsGeneralApplicationsapp2"),
    APPLICATIONS_GENERAL_APPLICATIONS_APPLICATION_3("applicationsGeneralApplicationsapp3"),
    APPLICATIONS_GENERAL_APPLICATIONS_APPLICATION_4("applicationsGeneralApplicationsapp4"),
    APPLICATIONS_GENERAL_APPLICATIONS_APPLICATION_5("applicationsGeneralApplicationsapp5"),
    APPLICATIONS_GENERAL_APPLICATIONS_APPLICATION_6("applicationsGeneralApplicationsapp6"),
    APPLICATIONS_GENERAL_APPLICATIONS_APPLICATION_7("applicationsGeneralApplicationsapp7"),
    APPLICATIONS_GENERAL_APPLICATIONS_APPLICATION_8("applicationsGeneralApplicationsapp8"),
    APPLICATIONS_GENERAL_APPLICATIONS_APPLICATION_9("applicationsGeneralApplicationsapp9"),
    APPLICATIONS_GENERAL_APPLICATIONS_APPLICATION_10("applicationsGeneralApplicationsapp10"),
    DUPLICATED_GENERAL_ORDERS("duplicatedGeneralOrders"),
    SYSTEM_DUPLICATES("systemDuplicates"),
    APPLICATIONS_GENERAL_APPLICATIONS_OVERFLOW("applicationsGeneralApplicationsOverflow"),

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
