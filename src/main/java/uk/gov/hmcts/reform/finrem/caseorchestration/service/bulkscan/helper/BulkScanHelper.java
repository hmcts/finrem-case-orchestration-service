package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.helper;

import com.google.common.base.Splitter;
import uk.gov.hmcts.reform.bsp.common.error.FormFieldValidationException;
import uk.gov.hmcts.reform.bsp.common.model.validation.in.OcrDataField;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.time.format.ResolverStyle.STRICT;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class BulkScanHelper {

    private static final DateTimeFormatter EXPECTED_DATE_FORMAT_FROM_FORM = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(STRICT);

    /**
     * Returns map with only the fields that were not blank from the OCR data.
     */
    public static Map<String, String> produceMapWithoutEmptyEntries(List<OcrDataField> ocrDataFields) {
        return ocrDataFields.stream()
            .filter(field -> isNotBlank(field.getValue()))
            .collect(toMap(OcrDataField::getName, OcrDataField::getValue));
    }

    public static LocalDate transformFormDateIntoLocalDate(String formFieldName, String formDate) throws FormFieldValidationException {
        try {
            return LocalDate.parse(formDate, EXPECTED_DATE_FORMAT_FROM_FORM);
        } catch (DateTimeParseException exception) {
            throw new FormFieldValidationException(String.format("%s must be a valid date", formFieldName));
        }
    }

    public static List<String> getCommaSeparatedValueFromOcrDataField(String commaSeparatedString) {
        //TODO clarify what the delimiter is - comma followed by space?
        //TODO modify tests if delimiter changes
        //TODO trim empty spaces or count as warning - perhaps incorrectly scanned so needs flagged?
        if (commaSeparatedString.isEmpty()) {
            return Collections.emptyList();
        }
        return Splitter.on(", ").splitToList(commaSeparatedString);
    }

    public static final Map<String, String> miamExemptionsChecklistToCcdFieldNames = new HashMap<String, String>() {{
            put("domesticViolence", "domesticViolence");
            put("urgency", "urgency");
            put("previousMIAMattendance", "previousMIAMattendance");
            put("other", "other");
        }};

    public static final Map<String, String> miamDomesticViolenceChecklistToCcdFieldNames = new HashMap<String, String>() {{
            put("ArrestedRelevantDomesticViolenceOffence", "FR_ms_MIAMDomesticViolenceChecklist_Value_1");
            put("RelevantPoliceCautionDomesticViolenceOffence", "FR_ms_MIAMDomesticViolenceChecklist_Value_2");
            put("RelevantCriminalProceedingsDomesticViolenceOffence", "FR_ms_MIAMDomesticViolenceChecklist_Value_3");
            put("RelevantConvictionDomesticViolenceOffence", "FR_ms_MIAMDomesticViolenceChecklist_Value_4");
            put("CourtOrderBindingInConnectionDomesticViolenceOffence", "FR_ms_MIAMDomesticViolenceChecklist_Value_5");
            put("DomesticViolenceProtectionNotice", "FR_ms_MIAMDomesticViolenceChecklist_Value_6");
            put("RelevantProtectiveInjunction", "FR_ms_MIAMDomesticViolenceChecklist_Value_7");
            put("UndertakingSection46Or63EFamilyLawActOrScotlandNorthernIrelandProtectiveInjunction", "FR_ms_MIAMDomesticViolenceChecklist_Value_8");
            put("FindingOfFactProceedingsUnitedKingdomDomesticViolence", "FR_ms_MIAMDomesticViolenceChecklist_Value_9");
            put("ExpertReportProceedingsUnitedKingdomAssessedBeingAtRisk", "FR_ms_MIAMDomesticViolenceChecklist_Value_10");
            put("ReportHealthProfessionalInjuriesConsistentDomesticViolence", "FR_ms_MIAMDomesticViolenceChecklist_Value_11");
            put("ReportHealthProfessionalConfirmingReferralSpecialistVictimsDomesticViolence", "FR_ms_MIAMDomesticViolenceChecklist_Value_12");
            put("LetterMemberRiskAssessmentConferenceOtherLocalSafeguardingForumRiskDomesticViolence",
                "FR_ms_MIAMDomesticViolenceChecklist_Value_13");
            put("LetterIndependentDomesticViolenceAdvisorProvidingSupport", "FR_ms_MIAMDomesticViolenceChecklist_Value_15");
            put("LetterIndependentSexualViolenceAdvisorProvidingSupport", "FR_ms_MIAMDomesticViolenceChecklist_Value_16");
            put("LetterLocalAuthorityOrHousingAssociationRiskOrDescriptionSpecificMattersDescriptionSupportProvided",
                "FR_ms_MIAMDomesticViolenceChecklist_Value_17");
            put("LetterOrganisationDomesticViolenceSupportStatementDescriptionReason", "FR_ms_MIAMDomesticViolenceChecklist_Value_18");
            put("ReportOrganisationDomesticViolenceSupportAdmissionToRefuge", "FR_ms_MIAMDomesticViolenceChecklist_Value_19");
            put("LetterPublicAuthorityRiskDomesticViolence", "FR_ms_MIAMDomesticViolenceChecklist_Value_20");
            put("LetterSecretaryOfStateLeaveToRemain289BImmigrationAct", "FR_ms_MIAMDomesticViolenceChecklist_Value_21");
            put("EvidenceAbuseRelatesFinancialMatters", "FR_ms_MIAMDomesticViolenceChecklist_Value_22");
        }};

    public static final Map<String, String> maimUrgencyChecklistToCcdFieldNames = new HashMap<String, String>() {{
            put("RiskLifeLibertyPhysicalSafety", "FR_ms_MIAMUrgencyReasonChecklist_Value_1");
            put("DelayRiskMiscarriageOfJustice", "FR_ms_MIAMUrgencyReasonChecklist_Value_2");
            put("DelayCauseUnreasonableHardship", "FR_ms_MIAMUrgencyReasonChecklist_Value_3");
            put("DelayCauseIrretrievableProblemsDealingWithDispute", "FR_ms_MIAMUrgencyReasonChecklist_Value_4");
            put("RiskScheduleJurisdiction", "FR_ms_MIAMUrgencyReasonChecklist_Value_5");
        }};

    public static final Map<String, String> miamPreviousAttendanceChecklistToCcdFieldNames = new HashMap<String, String>() {{
            put("4MonthsPriorAttendedMIAM", "FR_ms_MIAMPreviousAttendanceChecklist_Value_1");
            put("AnotherDisputeResolution", "FR_ms_MIAMPreviousAttendanceChecklist_Value_2");
            put("4MonthsPriorApplicationConfirmingMIAMExemption", "FR_ms_MIAMPreviousAttendanceChecklist_Value_3");
            put("ExistingProceedingsAttendedMIAMBeforeInitiating", "FR_ms_MIAMPreviousAttendanceChecklist_Value_4");
            put("ExistingProceedingsMIAMExemptionApplied", "FR_ms_MIAMPreviousAttendanceChecklist_Value_5");
        }};

    public static final Map<String, String> miamOtherGroundsChecklistToCcdFieldNames = new HashMap<String, String>() {{
            put("ApplicantBankruptApplicationForBankruptcyOrder", "FR_ms_MIAMOtherGroundsChecklist_Value_1");
            put("ApplicantBankruptPetitionByForBankruptcyOrder", "FR_ms_MIAMOtherGroundsChecklist_Value_2");
            put("ApplicantBankruptBankruptcyOrderInRespectOfProspectiveApplicant.", "FR_ms_MIAMOtherGroundsChecklist_Value_3");
            put("NotContactDetailsForRespondents", "FR_ms_MIAMOtherGroundsChecklist_Value_4");
            put("ApplicationMadeWithoutNotice", "FR_ms_MIAMOtherGroundsChecklist_Value_5");
            put("DisabilityOrInabilityPreventAttendance", "FR_ms_MIAMOtherGroundsChecklist_Value_6");
            put("CannotAttendInPrisonOrOtherInstitution", "FR_ms_MIAMOtherGroundsChecklist_Value_7");
            put("NotHabituallyResident", "FR_ms_MIAMOtherGroundsChecklist_Value_8");
            put("ChildProspectivePartiesRule12", "FR_ms_MIAMOtherGroundsChecklist_Value_9");
            put("ApplicantContactedAuthorisedFamilyMediatorsNotAvailable", "FR_ms_MIAMOtherGroundsChecklist_Value_10");
            put("NoAuthorisedFamilyMediatorWithinFifteenMiles", "FR_ms_MIAMOtherGroundsChecklist_Value_11");
        }};

}