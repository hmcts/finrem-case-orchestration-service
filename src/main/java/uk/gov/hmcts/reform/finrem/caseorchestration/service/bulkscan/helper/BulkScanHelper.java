package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.helper;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.bsp.common.error.FormFieldValidationException;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.OcrDataField;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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

    /**
     * The following assumptions are in place.
     * - the delimiter is a comma followed by a space ", "
     * - leading and trailing white spaces for each entry are removed - consequentially an empty entry will be discarded
     * so a warning will not be raised on further processing
     *
     * @param commaSeparatedString the comma separated string containing entries to be parsed
     * @return a list of strings without empty values
     */
    public static List<String> getCommaSeparatedValuesFromOcrDataField(String commaSeparatedString) {
        if (commaSeparatedString.isEmpty()) {
            return Collections.emptyList();
        }
        return Splitter.on(", ").splitToList(commaSeparatedString)
            .stream()
            .filter(Objects::nonNull)
            .map(String::trim)
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.toList());
    }

    public static final Map<String, String> natureOfApplicationChecklistToCcdFieldNames =
        new ImmutableMap.Builder<String, String>()
            .put("Periodical Payment Order", "Periodical Payment Order")
            .put("Lump Sum Order", "Lump Sum Order")
            .put("Pension Sharing Order", "Pension Sharing Order")
            .put("Pension Attachment Order", "Pension Attachment Order")
            .put("Pension Compensation Sharing Order", "Pension Compensation Sharing Order")
            .put("Pension Compensation Attachment Order", "Pension Compensation Attachment Order")
            .put("A settlement or a transfer of property", "A settlement or a transfer of property")
            .put("Property Adjustment Order", "Property Adjustment Order")
            .build();
    
    public static final Map<String, String> dischargePeriodicalPaymentSubstituteChecklistToCcdFieldNames =
        new ImmutableMap.Builder<String, String>()
            .put("a lump sum order", "Lump Sum Order")
            .put("a property adjustment order", "Property Adjustment Order")
            .put("a pension sharing order", "Pension Sharing Order")
            .put("a pension compensation sharing order", "Pension Compensation Sharing Order")
            .build();
        
    public static final Map<String, String> miamExemptionsChecklistToCcdFieldNames =
        new ImmutableMap.Builder<String, String>()
            .put("domesticViolence", "domesticViolence")
            .put("urgency", "urgency")
            .put("previousMIAMattendance", "previousMIAMattendance")
            .put("other", "other")
            .build();


    public static final Map<String, String> miamDomesticViolenceChecklistToCcdFieldNames =
        new ImmutableMap.Builder<String, String>()
            .put("ArrestedRelevantDomesticViolenceOffence", "FR_ms_MIAMDomesticViolenceChecklist_Value_1")
            .put("RelevantPoliceCautionDomesticViolenceOffence", "FR_ms_MIAMDomesticViolenceChecklist_Value_2")
            .put("RelevantCriminalProceedingsDomesticViolenceOffence", "FR_ms_MIAMDomesticViolenceChecklist_Value_3")
            .put("RelevantConvictionDomesticViolenceOffence", "FR_ms_MIAMDomesticViolenceChecklist_Value_4")
            .put("CourtOrderBindingInConnectionDomesticViolenceOffence", "FR_ms_MIAMDomesticViolenceChecklist_Value_5")
            .put("DomesticViolenceProtectionNotice", "FR_ms_MIAMDomesticViolenceChecklist_Value_6")
            .put("RelevantProtectiveInjunction", "FR_ms_MIAMDomesticViolenceChecklist_Value_7")
            .put("UndertakingSection46Or63EFamilyLawActOrScotlandNorthernIrelandProtectiveInjunction", "FR_ms_MIAMDomesticViolenceChecklist_Value_8")
            .put("FindingOfFactProceedingsUnitedKingdomDomesticViolence", "FR_ms_MIAMDomesticViolenceChecklist_Value_9")
            .put("ExpertReportProceedingsUnitedKingdomAssessedBeingAtRisk", "FR_ms_MIAMDomesticViolenceChecklist_Value_10")
            .put("ReportHealthProfessionalInjuriesConsistentDomesticViolence", "FR_ms_MIAMDomesticViolenceChecklist_Value_11")
            .put("ReportHealthProfessionalConfirmingReferralSpecialistVictimsDomesticViolence", "FR_ms_MIAMDomesticViolenceChecklist_Value_12")
            .put("LetterMemberRiskAssessmentConferenceOtherLocalSafeguardingForumRiskDomesticViolence",
                "FR_ms_MIAMDomesticViolenceChecklist_Value_13")
            .put("LetterIndependentDomesticViolenceAdvisorProvidingSupport", "FR_ms_MIAMDomesticViolenceChecklist_Value_15")
            .put("LetterIndependentSexualViolenceAdvisorProvidingSupport", "FR_ms_MIAMDomesticViolenceChecklist_Value_16")
            .put("LetterLocalAuthorityOrHousingAssociationRiskOrDescriptionSpecificMattersDescriptionSupportProvided",
                "FR_ms_MIAMDomesticViolenceChecklist_Value_17")
            .put("LetterOrganisationDomesticViolenceSupportStatementDescriptionReason", "FR_ms_MIAMDomesticViolenceChecklist_Value_18")
            .put("ReportOrganisationDomesticViolenceSupportAdmissionToRefuge", "FR_ms_MIAMDomesticViolenceChecklist_Value_19")
            .put("LetterPublicAuthorityRiskDomesticViolence", "FR_ms_MIAMDomesticViolenceChecklist_Value_20")
            .put("LetterSecretaryOfStateLeaveToRemain289BImmigrationAct", "FR_ms_MIAMDomesticViolenceChecklist_Value_21")
            .put("EvidenceAbuseRelatesFinancialMatters", "FR_ms_MIAMDomesticViolenceChecklist_Value_22")
            .build();

    public static final Map<String, String> maimUrgencyChecklistToCcdFieldNames =
        new ImmutableMap.Builder<String, String>()
            .put("RiskLifeLibertyPhysicalSafety", "FR_ms_MIAMUrgencyReasonChecklist_Value_1")
            .put("DelayRiskMiscarriageOfJustice", "FR_ms_MIAMUrgencyReasonChecklist_Value_2")
            .put("DelayCauseUnreasonableHardship", "FR_ms_MIAMUrgencyReasonChecklist_Value_3")
            .put("DelayCauseIrretrievableProblemsDealingWithDispute", "FR_ms_MIAMUrgencyReasonChecklist_Value_4")
            .put("RiskScheduleJurisdiction", "FR_ms_MIAMUrgencyReasonChecklist_Value_5")
            .build();

    public static final Map<String, String> miamPreviousAttendanceChecklistToCcdFieldNames =
        new ImmutableMap.Builder<String, String>()
            .put("4MonthsPriorAttendedMIAM", "FR_ms_MIAMPreviousAttendanceChecklist_Value_1")
            .put("AnotherDisputeResolution", "FR_ms_MIAMPreviousAttendanceChecklist_Value_2")
            .put("4MonthsPriorApplicationConfirmingMIAMExemption", "FR_ms_MIAMPreviousAttendanceChecklist_Value_3")
            .put("ExistingProceedingsAttendedMIAMBeforeInitiating", "FR_ms_MIAMPreviousAttendanceChecklist_Value_4")
            .put("ExistingProceedingsMIAMExemptionApplied", "FR_ms_MIAMPreviousAttendanceChecklist_Value_5")
            .build();


    public static final Map<String, String> miamOtherGroundsChecklistToCcdFieldNames =
        new ImmutableMap.Builder<String, String>()
            .put("ApplicantBankruptApplicationForBankruptcyOrder", "FR_ms_MIAMOtherGroundsChecklist_Value_1")
            .put("ApplicantBankruptPetitionByForBankruptcyOrder", "FR_ms_MIAMOtherGroundsChecklist_Value_2")
            .put("ApplicantBankruptBankruptcyOrderInRespectOfProspectiveApplicant.", "FR_ms_MIAMOtherGroundsChecklist_Value_3")
            .put("NotContactDetailsForRespondents", "FR_ms_MIAMOtherGroundsChecklist_Value_4")
            .put("ApplicationMadeWithoutNotice", "FR_ms_MIAMOtherGroundsChecklist_Value_5")
            .put("DisabilityOrInabilityPreventAttendance", "FR_ms_MIAMOtherGroundsChecklist_Value_6")
            .put("CannotAttendInPrisonOrOtherInstitution", "FR_ms_MIAMOtherGroundsChecklist_Value_7")
            .put("NotHabituallyResident", "FR_ms_MIAMOtherGroundsChecklist_Value_8")
            .put("ChildProspectivePartiesRule12", "FR_ms_MIAMOtherGroundsChecklist_Value_9")
            .put("ApplicantContactedAuthorisedFamilyMediatorsNotAvailable", "FR_ms_MIAMOtherGroundsChecklist_Value_10")
            .put("NoAuthorisedFamilyMediatorWithinFifteenMiles", "FR_ms_MIAMOtherGroundsChecklist_Value_11")
            .build();
}
