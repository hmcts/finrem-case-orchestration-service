package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulk.scan.transformation;

import org.junit.Test;
import uk.gov.hmcts.reform.bsp.common.model.transformation.in.ExceptionRecord;
import uk.gov.hmcts.reform.bsp.common.model.validation.in.OcrDataField;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.transformation.FormAToCaseTransformer;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.collection.IsMapWithSize.aMapWithSize;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.BULK_SCAN_CASE_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_CASE_ID;

public class FormAToCaseTransformerTest {

    private final FormAToCaseTransformer formAToCaseTransformer = new FormAToCaseTransformer();

    @Test
    public void shouldTransformFieldsAccordingly() {
        ExceptionRecord exceptionRecord = createExceptionRecord(asList(
            new OcrDataField("claimingExemptionMIAM", "Yes"),
            new OcrDataField("familyMediatorMIAM", "No"),
            new OcrDataField("applicantAttendedMIAM", "No")
        ));

        Map<String, Object> transformedCaseData = formAToCaseTransformer.transformIntoCaseData(exceptionRecord);

        assertThat(transformedCaseData, allOf(
            hasEntry(BULK_SCAN_CASE_REFERENCE, TEST_CASE_ID),
            hasEntry("claimingExemptionMIAM", "Yes"),
            hasEntry("familyMediatorMIAM", "No"),
            hasEntry("applicantAttendedMIAM", "No")
        ));
    }

    @Test
    public void shouldNotReturnUnexpectedField() {
        ExceptionRecord incomingExceptionRecord = createExceptionRecord(singletonList(
            new OcrDataField("UnexpectedName", "UnexpectedValue")
        ));

        Map<String, Object> transformedCaseData = formAToCaseTransformer.transformIntoCaseData(incomingExceptionRecord);

        assertThat(transformedCaseData, allOf(
            aMapWithSize(1),
            hasEntry(BULK_SCAN_CASE_REFERENCE, TEST_CASE_ID)
        ));
    }

    private ExceptionRecord createExceptionRecord(List<OcrDataField> ocrDataFields) {
        return ExceptionRecord.builder().id(TEST_CASE_ID).ocrDataFields(ocrDataFields).build();
    }

    @Test
    public void checkCommaSeparatedEntryTransformerReturnsCorrectly() {
        String exemptionsValue = "domesticViolence, urgency, previousMIAMattendance";

        String domesticViolenceValue = "ArrestedRelevantDomesticViolenceOffence, "
            + "RelevantCriminalProceedingsDomesticViolenceOffence, "
            + "FindingOfFactProceedingsUnitedKingdomDomesticViolence, "
            + "LetterOrganisationDomesticViolenceSupportStatementDescriptionReason, "
            + "LetterSecretaryOfStateLeaveToRemain289BImmigrationAct";

        String urgencyValue = "DelayRiskMiscarriageOfJustice, DelayCauseIrretrievableProblemsDealingWithDispute, "
            + "RiskScheduleJurisdiction";

        String previousAttendanceValue = "4MonthsPriorAttendedMIAM, "
            + "4MonthsPriorApplicationConfirmingMIAMExemption, "
            + "ExistingProceedingsAttendedMIAMBeforeInitiating";

        String otherGroundsValue = "ApplicantBankruptPetitionByForBankruptcyOrder, NotContactDetailsForRespondents, "
            + "DisabilityOrInabilityPreventAttendance, NotHabituallyResident, "
            + "ApplicantContactedAuthorisedFamilyMediatorsNotAvailable, "
            + "NoAuthorisedFamilyMediatorWithinFifteenMiles";

        ExceptionRecord exceptionRecord = createExceptionRecord(asList(
            new OcrDataField("MIAMExemptionsChecklist", exemptionsValue),
            new OcrDataField("MIAMDomesticViolenceChecklist", domesticViolenceValue),
            new OcrDataField("MIAMUrgencyChecklist", urgencyValue),
            new OcrDataField("MIAMPreviousAttendanceChecklist", previousAttendanceValue),
            new OcrDataField("MIAMOtherGroundsChecklist", otherGroundsValue)
        ));

        Map<String, Object> transformedCaseData = formAToCaseTransformer.transformIntoCaseData(exceptionRecord);

        List<String> expectedExemptionsResult = asList("domesticViolence", "urgency", "previousMIAMattendance");
        assertThat(transformedCaseData, hasEntry("MIAMExemptionsChecklist", expectedExemptionsResult));

        List<String> expectedDomesticViolenceResult = asList("FR_ms_MIAMDomesticViolenceChecklist_Value_1",
            "FR_ms_MIAMDomesticViolenceChecklist_Value_3",
            "FR_ms_MIAMDomesticViolenceChecklist_Value_9",
            "FR_ms_MIAMDomesticViolenceChecklist_Value_18",
            "FR_ms_MIAMDomesticViolenceChecklist_Value_21");
        assertThat(transformedCaseData, hasEntry("MIAMDomesticViolenceChecklist", expectedDomesticViolenceResult));

        List<String> expectedUrgencyResult = asList("FR_ms_MIAMUrgencyReasonChecklist_Value_2",
            "FR_ms_MIAMUrgencyReasonChecklist_Value_4",
            "FR_ms_MIAMUrgencyReasonChecklist_Value_5");
        assertThat(transformedCaseData, hasEntry("MIAMUrgencyChecklist", expectedUrgencyResult));

        List<String> expectedPreviousAttendanceResult = asList("FR_ms_MIAMPreviousAttendanceChecklist_Value_1",
            "FR_ms_MIAMPreviousAttendanceChecklist_Value_3",
            "FR_ms_MIAMPreviousAttendanceChecklist_Value_4");
        assertThat(transformedCaseData, hasEntry("MIAMPreviousAttendanceChecklist", expectedPreviousAttendanceResult));


        List<String> expectedOtherGroundsResult = asList("FR_ms_MIAMOtherGroundsChecklist_Value_2",
            "FR_ms_MIAMOtherGroundsChecklist_Value_4",
            "FR_ms_MIAMOtherGroundsChecklist_Value_6",
            "FR_ms_MIAMOtherGroundsChecklist_Value_8",
            "FR_ms_MIAMOtherGroundsChecklist_Value_10",
            "FR_ms_MIAMOtherGroundsChecklist_Value_11");
        assertThat(transformedCaseData, hasEntry("MIAMOtherGroundsChecklist", expectedOtherGroundsResult));
    }

    @Test
    public void commaSeparatedEntryTransformerDoesNotAddNonExistentElements() {
        String domesticViolenceValue = "something ,   , I don't exist, ArrestedRelevantDomesticViolenceOffence, "
            + "who dis?, RelevantCriminalProceedingsDomesticViolenceOffence, "
            + "noWay, blah random string here8u4120912";

        ExceptionRecord exceptionRecord = createExceptionRecord(singletonList(
            new OcrDataField("MIAMDomesticViolenceChecklist", domesticViolenceValue)));

        Map<String, Object> transformedCaseData = formAToCaseTransformer.transformIntoCaseData(exceptionRecord);

        List<String> expectedCommaSeparatedList = asList(
            "FR_ms_MIAMDomesticViolenceChecklist_Value_1",
            "FR_ms_MIAMDomesticViolenceChecklist_Value_3"
        );

        assertThat(transformedCaseData, hasEntry("MIAMDomesticViolenceChecklist", expectedCommaSeparatedList));
    }

    @Test
    public void commaSeparatedEntryTransformerDoesNotAddEmptyStrings() {
        String domesticViolenceValue = "         ,   , I don't exist, ArrestedRelevantDomesticViolenceOffence, "
            + "  , RelevantCriminalProceedingsDomesticViolenceOffence, "
            + "       ,";

        ExceptionRecord exceptionRecord = createExceptionRecord(singletonList(
            new OcrDataField("MIAMDomesticViolenceChecklist", domesticViolenceValue)));

        Map<String, Object> transformedCaseData = formAToCaseTransformer.transformIntoCaseData(exceptionRecord);

        List<String> expectedCommaSeparatedList = asList(
            "FR_ms_MIAMDomesticViolenceChecklist_Value_1",
            "FR_ms_MIAMDomesticViolenceChecklist_Value_3"
        );

        assertThat(transformedCaseData, hasEntry("MIAMDomesticViolenceChecklist", expectedCommaSeparatedList));
    }

    @Test
    public void commaSeparatedEntryTransformerDoesNotTransformFieldsWithoutValues() {
        ExceptionRecord exceptionRecord = createExceptionRecord(singletonList(new OcrDataField("MIAMDomesticViolenceChecklist", "")));

        Map<String, Object> transformedCaseData = formAToCaseTransformer.transformIntoCaseData(exceptionRecord);

        assertThat(transformedCaseData, not(hasEntry("MIAMDomesticViolenceChecklist", emptyList())));
        assertThat(transformedCaseData, not(hasKey("MIAMDomesticViolenceChecklist")));
        assertThat(transformedCaseData, not(hasKey("MIAMExemptionsChecklist")));
        assertThat(transformedCaseData, not(hasKey("MIAMPreviousAttendanceChecklist")));
    }
}
