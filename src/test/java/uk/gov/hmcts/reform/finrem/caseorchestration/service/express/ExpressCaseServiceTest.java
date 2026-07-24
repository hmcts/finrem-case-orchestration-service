package uk.gov.hmcts.reform.finrem.caseorchestration.service.express;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.EstimatedAssetV2;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.EstimatedAssetV3;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ExpressCaseParticipation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.LabelForExpressCaseAmendment;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionMidlandsFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.AllocatedRegionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DefaultCourtListWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ExpressCaseWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.NatureApplicationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.RegionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ScheduleOneWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.EXPRESS_CASE_PARTICIPATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.EstimatedAssetV2.UNABLE_TO_QUANTIFY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.EstimatedAssetV3.OVER_TWENTY_MILLION_POUNDS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ExpressCaseParticipation.DOES_NOT_QUALIFY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ExpressCaseParticipation.ENROLLED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ExpressCaseParticipation.WITHDRAWN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NatureApplication.A_SETTLEMENT_OR_A_TRANSFER_OF_PROPERTY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NatureApplication.CONTESTED_VARIATION_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NatureApplication.LUMP_SUM_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NatureApplication.MAINTENANCE_PENDING_SUIT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NatureApplication.PENSION_ATTACHMENT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NatureApplication.PENSION_COMPENSATION_ATTACHMENT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NatureApplication.PENSION_COMPENSATION_SHARING_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NatureApplication.PENSION_SHARING_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NatureApplication.PERIODICAL_PAYMENT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NatureApplication.PROPERTY_ADJUSTMENT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NottinghamCourt.BOSTON_COUNTY_COURT_AND_FAMILY_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NottinghamCourt.NOTTINGHAM_COUNTY_COURT_AND_FAMILY_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Region.MIDLANDS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Schedule1OrMatrimonialAndCpList.MATRIMONIAL_AND_CIVIL_PARTNERSHIP_PROCEEDINGS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Schedule1OrMatrimonialAndCpList.SCHEDULE_1_CHILDREN_ACT_1989;

@ExtendWith(MockitoExtension.class)
class ExpressCaseServiceTest {

    @InjectMocks
    @Spy
    private ExpressCaseService expressCaseService;

    @Mock
    private FeatureToggleService featureToggleService;

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(expressCaseService, "expressCaseFrcs", List.of("FR_s_NottinghamList_1", "FR_s_NottinghamList_2"));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void setExpressCaseEnrollmentStatus_shouldEnrollInExpressPilot_WhenCaseDataMeetsRequirements(Boolean isAssetsChecklistV3) {
        FinremCaseData caseData = createExpressCaseWithAssertV3(isAssetsChecklistV3);

        if (isAssetsChecklistV3) {
            caseData.setEstimatedAssetsChecklistV3(EstimatedAssetV3.UNDER_TWO_HUNDRED_AND_FIFTY_THOUSAND_POUNDS);
        } else {
            caseData.setEstimatedAssetsChecklistV2(EstimatedAssetV2.UNDER_TWO_HUNDRED_AND_FIFTY_THOUSAND_POUNDS);
        }

        when(featureToggleService.isExpressPilotEnabled()).thenReturn(true);
        expressCaseService.setExpressCaseEnrollmentStatus(caseData);
        assertEquals(ENROLLED, caseData.getExpressCaseWrapper().getExpressCaseParticipation());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldSetExpressEnrollmentStatusToWithdrawn(Boolean isAssetsChecklistV3) {
        FinremCaseData caseData = createExpressCaseWithAssertV3(isAssetsChecklistV3);
        expressCaseService.setExpressCaseEnrollmentStatusToWithdrawn(caseData);
        assertEquals(WITHDRAWN, caseData.getExpressCaseWrapper().getExpressCaseParticipation());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldNotEnrollInExpressPilot_WhenFeatureGateOff(Boolean isAssetsChecklistV3) {
        FinremCaseData caseData = createExpressCaseWithAssertV3(isAssetsChecklistV3);
        when(featureToggleService.isExpressPilotEnabled()).thenReturn(false);
        expressCaseService.setExpressCaseEnrollmentStatus(caseData);
        assertNull(caseData.getExpressCaseWrapper().getExpressCaseParticipation());
    }

    @ParameterizedTest
    @MethodSource("provideInvalidCaseData")
    void setExpressCaseEnrollmentStatus_shouldNotEnroll_WhenCaseDataDoesNotMeetCriteria(FinremCaseData caseData) {
        when(featureToggleService.isExpressPilotEnabled()).thenReturn(true);
        expressCaseService.setExpressCaseEnrollmentStatus(caseData);
        assertEquals(DOES_NOT_QUALIFY, caseData.getExpressCaseWrapper().getExpressCaseParticipation());
    }

    /*
     * This is the only scenario where a User amending Form A contested case sees the UNSUITABLE_FOR_EXPRESS_LABEL,
     * when a Case was enrolled, but the answers have changed in amendment so express criteria is no longer met.
     */
    @Test
    void setWhichExpressCaseAmendmentLabelToShow_shouldSetUnsuitable_WhenCaseAmendedToDisqualifyFromExpress() {
        FinremCaseData caseDataOnceAmended = new FinremCaseData();
        caseDataOnceAmended.getExpressCaseWrapper().setExpressCaseParticipation(DOES_NOT_QUALIFY);

        FinremCaseData caseDataBeforeAmending = new FinremCaseData();
        caseDataBeforeAmending.getExpressCaseWrapper().setExpressCaseParticipation(ENROLLED);

        expressCaseService.setWhichExpressCaseAmendmentLabelToShow(caseDataOnceAmended, caseDataBeforeAmending);
        assertEquals(LabelForExpressCaseAmendment.UNSUITABLE_FOR_EXPRESS_LABEL,
            caseDataOnceAmended.getExpressCaseWrapper().getLabelForExpressCaseAmendment());
    }

    /*
     * These are the scenarios when a User amending Form A contested case sees the SUITABLE_FOR_EXPRESS_LABEL,
     * essentially whenever setExpressCaseParticipation has been set to ENROLLED
     */
    @ParameterizedTest
    @MethodSource("provideAmendedExpressCaseSuitableScenarios")
    void setWhichExpressCaseAmendmentLabelToShow_shouldSetSuitable_WhenCaseAmendmentDoesNotDisqualify(
        Pair<FinremCaseData, FinremCaseData> caseDataBeforeAndAfterAmending) {

        FinremCaseData dataBeforeAmending = caseDataBeforeAndAfterAmending.getLeft();
        FinremCaseData amendedData = caseDataBeforeAndAfterAmending.getRight();

        expressCaseService.setWhichExpressCaseAmendmentLabelToShow(amendedData, dataBeforeAmending);

        assertEquals(LabelForExpressCaseAmendment.SUITABLE_FOR_EXPRESS_LABEL, amendedData.getExpressCaseWrapper().getLabelForExpressCaseAmendment());
    }

    /*
     * These are the scenarios when the User sees no dynamic page related to Express Case processing,
     * whenever setExpressCaseParticipation.  Essentially, when the case was not set to ENROLLED and still
     * isn't eligible for enrollment.
     */
    @ParameterizedTest
    @MethodSource("provideAmendedExpressCaseScenariosNeedingNoLabel")
    void setWhetherDisqualifiedFromExpress_shouldSetNoLabel_WhenCaseRemainsUnsuitableForExpress(
        Pair<FinremCaseData, FinremCaseData> caseDataBeforeAndAfterAmending) {

        FinremCaseData dataBeforeAmending = caseDataBeforeAndAfterAmending.getLeft();
        FinremCaseData amendedData = caseDataBeforeAndAfterAmending.getRight();

        expressCaseService.setWhichExpressCaseAmendmentLabelToShow(amendedData, dataBeforeAmending);

        assertEquals(LabelForExpressCaseAmendment.SHOW_NEITHER_PAGE_NOR_LABEL, amendedData.getExpressCaseWrapper().getLabelForExpressCaseAmendment());
    }

    @ParameterizedTest
    @MethodSource("provideIsExpressCase")
    void shouldReturnIfCaseIsExpressEnrolledAndReturnFalseIfExpressIsDisabledCaseDetails(boolean isExpressPilotEnabled,
                                                                                         CaseDetails caseDetails,
                                                                                         boolean expected) {
        when(featureToggleService.isExpressPilotEnabled()).thenReturn(isExpressPilotEnabled);
        assertEquals(expected, expressCaseService.isExpressCase(caseDetails));
    }

    @ParameterizedTest
    @MethodSource("provideIsExpressCaseFinRemCaseData")
    void shouldReturnIfCaseIsExpressEnrolledAndReturnFalseIfExpressIsDisabledFinRemCaseData(boolean isExpressPilotEnabled,
                                                                                            FinremCaseData caseData,
                                                                                            boolean expected) {
        when(featureToggleService.isExpressPilotEnabled()).thenReturn(isExpressPilotEnabled);
        assertEquals(expected, expressCaseService.isExpressCase(caseData));
    }

    @Test
    void setExpressCaseEnrollmentStatus_shouldNotEnrollInExpressPilot_WhenNoAssetValueExists() {

        FinremCaseData caseData = createCaseData();

        // Explicitly setting both of these values to null for test clarity.
        caseData.setEstimatedAssetsChecklistV2(null);
        caseData.setEstimatedAssetsChecklistV3(null);

        when(featureToggleService.isExpressPilotEnabled()).thenReturn(true);
        expressCaseService.setExpressCaseEnrollmentStatus(caseData);
        assertEquals(DOES_NOT_QUALIFY, caseData.getExpressCaseWrapper().getExpressCaseParticipation());
    }

    @Test
    void givenV3ValueExists_clearUnusedEstimatedAssetsChecklist() {
        EstimatedAssetV3 v3Value = EstimatedAssetV3.BETWEEN_FIVE_HUNDRED_THOUSAND_TO_ONE_MILLION_POUNDS;
        FinremCaseData caseData = FinremCaseData.builder()
            .estimatedAssetsChecklistV3(v3Value)
            .estimatedAssetsChecklistV2(mock(EstimatedAssetV2.class))
            .build();
        expressCaseService.clearUnusedEstimatedAssetsChecklist(caseData);

        assertNull(caseData.getEstimatedAssetsChecklistV2());
        assertEquals(v3Value, caseData.getEstimatedAssetsChecklistV3());
    }

    @Test
    void givenV3ValueDoesNotExist_clearUnusedEstimatedAssetsChecklist() {
        EstimatedAssetV2 v2Value = EstimatedAssetV2.UNDER_TWO_HUNDRED_AND_FIFTY_THOUSAND_POUNDS;
        FinremCaseData caseData = FinremCaseData.builder()
            .estimatedAssetsChecklistV2(v2Value)
            .build();
        expressCaseService.clearUnusedEstimatedAssetsChecklist(caseData);

        assertEquals(v2Value, caseData.getEstimatedAssetsChecklistV2());
    }

    @Test
    void givenExpressPilotDisabled_canSetExpressPilotStatus_returnsFalse() {
        when(featureToggleService.isExpressPilotEnabled()).thenReturn(false);
        FinremCaseData caseData = FinremCaseData.builder().build();
        assertThat(expressCaseService.canSetExpressPilotStatus(caseData)).isFalse();
    }

    @Test
    void givenCaseEnrolledExpressPilot_canSetExpressPilotStatus_returnsFalse() {
        when(featureToggleService.isExpressPilotEnabled()).thenReturn(true);
        ExpressCaseWrapper expressCaseWrapper = ExpressCaseWrapper.builder()
            .expressCaseParticipation(ENROLLED)
            .build();
        FinremCaseData caseData = FinremCaseData.builder()
            .expressCaseWrapper(expressCaseWrapper)
            .build();
        assertThat(expressCaseService.canSetExpressPilotStatus(caseData)).isFalse();
    }

    @Test
    void givenCaseEnrolledExpressPilot_canSetExpressPilotStatusAndIgnoreExpressCaseParticipation_returnsTrue() {
        when(featureToggleService.isExpressPilotEnabled()).thenReturn(true);
        ExpressCaseWrapper expressCaseWrapper = ExpressCaseWrapper.builder()
            .expressCaseParticipation(ENROLLED)
            .build();
        FinremCaseData caseData = FinremCaseData.builder()
            .expressCaseWrapper(expressCaseWrapper)
            .build();
        when(expressCaseService.qualifiesForExpress(caseData)).thenReturn(true);

        assertThat(expressCaseService.canSetExpressPilotStatus(caseData, true)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void givenCaseQualifiesForExpress_canSetExpressPilotStatus_returnsTrueOnlyWhenNoHearingsExist(
        boolean hasNoHearings
    ) {
        when(featureToggleService.isExpressPilotEnabled()).thenReturn(true);
        ManageHearingsWrapper manageHearingsWrapper = mock(ManageHearingsWrapper.class);
        when(manageHearingsWrapper.hasNoHearings()).thenReturn(hasNoHearings);

        FinremCaseData caseData = FinremCaseData.builder()
            .manageHearingsWrapper(manageHearingsWrapper)
            .build();

        lenient().when(expressCaseService.qualifiesForExpress(caseData)).thenReturn(true);

        assertThat(expressCaseService.canSetExpressPilotStatus(caseData)).isEqualTo(hasNoHearings);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void givenCaseWithoutHearings_canSetExpressPilotStatus_returnsTrueOnlyWhenCaseQualifiesForExpress(
        boolean qualifiesForExpress
    ) {
        when(featureToggleService.isExpressPilotEnabled()).thenReturn(true);
        ManageHearingsWrapper manageHearingsWrapper = mock(ManageHearingsWrapper.class);
        when(manageHearingsWrapper.hasNoHearings()).thenReturn(true);

        FinremCaseData caseData = FinremCaseData.builder()
            .manageHearingsWrapper(manageHearingsWrapper)
            .build();

        when(expressCaseService.qualifiesForExpress(caseData)).thenReturn(qualifiesForExpress);

        assertThat(expressCaseService.canSetExpressPilotStatus(caseData)).isEqualTo(qualifiesForExpress);
    }

    private static Stream<Arguments> provideIsExpressCase() {
        return Stream.of(
            Arguments.of(false, createCaseDetailsWithParticipation(ENROLLED), false),
            Arguments.of(true, createCaseDetailsWithParticipation(ENROLLED), true),
            Arguments.of(true, createCaseDetailsWithParticipation(DOES_NOT_QUALIFY), false),
            // Test EP flag not set
            Arguments.of(true, CaseDetails.builder().data(new HashMap<>()).build(), false)
        );
    }

    private static Stream<Arguments> provideIsExpressCaseFinRemCaseData() {
        return Stream.of(
            Arguments.of(false, createFinRemEpCaseData(ENROLLED), false),
            Arguments.of(true, createFinRemEpCaseData(ENROLLED), true),
            Arguments.of(true, createFinRemEpCaseData(DOES_NOT_QUALIFY), false)
        );
    }

    private static Stream<FinremCaseData> provideInvalidCaseData() {

        Boolean useV3 = true;
        Boolean useV2 = false;

        FinremCaseData hasVariationOrder = createExpressCaseWithAssertV3(useV3);
        hasVariationOrder.getNatureApplicationWrapper()
            .setNatureOfApplicationChecklist(List.of(CONTESTED_VARIATION_ORDER));

        FinremCaseData isNotInParticipatingFRC = createExpressCaseWithAssertV3(useV3);
        isNotInParticipatingFRC.getRegionWrapper().getAllocatedRegionWrapper()
            .getDefaultCourtListWrapper().setNottinghamCourtList(BOSTON_COUNTY_COURT_AND_FAMILY_COURT);

        FinremCaseData isNotUnder250kForV2 = createExpressCaseWithAssertV3(useV2);
        isNotUnder250kForV2.setEstimatedAssetsChecklistV2(UNABLE_TO_QUANTIFY);

        FinremCaseData isNotUnder250kForV3 = createExpressCaseWithAssertV3(useV3);
        isNotUnder250kForV3.setEstimatedAssetsChecklistV3(OVER_TWENTY_MILLION_POUNDS);

        FinremCaseData isFastTrackApplication = createExpressCaseWithAssertV3(useV3);
        isFastTrackApplication.setFastTrackDecision(YesOrNo.YES);

        FinremCaseData isNotMatrimonialApplication = createExpressCaseWithAssertV3(useV3);
        isNotMatrimonialApplication.getScheduleOneWrapper().setTypeOfApplication(SCHEDULE_1_CHILDREN_ACT_1989);

        return Stream.of(
            hasVariationOrder,
            isNotInParticipatingFRC,
            isNotUnder250kForV2,
            isNotUnder250kForV3,
            isFastTrackApplication,
            isNotMatrimonialApplication
        );
    }

    private static FinremCaseData createCaseData() {
        return FinremCaseData.builder()
            .scheduleOneWrapper(ScheduleOneWrapper.builder()
                .typeOfApplication(MATRIMONIAL_AND_CIVIL_PARTNERSHIP_PROCEEDINGS)
                .build())
            .regionWrapper(RegionWrapper.builder()
                .allocatedRegionWrapper(AllocatedRegionWrapper.builder()
                    .courtListWrapper(DefaultCourtListWrapper.builder()
                        .nottinghamCourtList(NOTTINGHAM_COUNTY_COURT_AND_FAMILY_COURT)
                        .build())
                    .regionList(MIDLANDS)
                    .midlandsFrcList(RegionMidlandsFrc.NOTTINGHAM)
                    .build())
                .build())
            .natureApplicationWrapper(NatureApplicationWrapper.builder()
                .natureOfApplicationChecklist(List.of(
                    MAINTENANCE_PENDING_SUIT,
                    LUMP_SUM_ORDER,
                    PROPERTY_ADJUSTMENT_ORDER,
                    A_SETTLEMENT_OR_A_TRANSFER_OF_PROPERTY,
                    PERIODICAL_PAYMENT_ORDER,
                    PENSION_SHARING_ORDER,
                    PENSION_COMPENSATION_SHARING_ORDER,
                    PENSION_ATTACHMENT_ORDER,
                    PENSION_COMPENSATION_ATTACHMENT_ORDER
                ))
                .build())
            .fastTrackDecision(YesOrNo.NO)
            .build();
    }

    private static FinremCaseData createExpressCaseWithAssertV3(Boolean isAssetsChecklistV3) {
        FinremCaseData data = createCaseData();
        if (isAssetsChecklistV3) {
            data.setEstimatedAssetsChecklistV3(EstimatedAssetV3.UNDER_TWO_HUNDRED_AND_FIFTY_THOUSAND_POUNDS);
        } else {
            data.setEstimatedAssetsChecklistV2(EstimatedAssetV2.UNDER_TWO_HUNDRED_AND_FIFTY_THOUSAND_POUNDS);
        }
        return data;
    }

    /*
     * Creates pairs of FinremCaseData.
     * Left is case data before. Right is case data now, having been amended.
     * Each Pair represents a scenario where a case becomes suitable for Express processing upon amendment.
     */
    private static Stream<Pair<FinremCaseData, FinremCaseData>> provideAmendedExpressCaseSuitableScenarios() {

        FinremCaseData dataQualifies = FinremCaseData.builder().build();
        dataQualifies.getExpressCaseWrapper().setExpressCaseParticipation(ENROLLED);

        FinremCaseData dataDoesNotQualify = FinremCaseData.builder().build();
        dataDoesNotQualify.getExpressCaseWrapper().setExpressCaseParticipation(DOES_NOT_QUALIFY);

        FinremCaseData nullEnrollmentData = FinremCaseData.builder().build();

        // Case qualified for enrollment on creation, and still does following amendment,
        Pair<FinremCaseData, FinremCaseData> amendedStillEnrolled = Pair.of(dataQualifies, dataQualifies);

        // Case did not qualify for enrollment during creation, but it does upon amendment
        Pair<FinremCaseData, FinremCaseData> amendedNowQualifies = Pair.of(dataDoesNotQualify, dataQualifies);

        // Case created before Express Cases went live, but qualify as an express case upon amendment
        Pair<FinremCaseData, FinremCaseData> amendedWasNullNowQualifies = Pair.of(nullEnrollmentData, dataQualifies);

        return Stream.of(
            amendedStillEnrolled,
            amendedNowQualifies,
            amendedWasNullNowQualifies
        );
    }

    /*
     * Creates pairs of FinremCaseData.
     * Left is case data before. Right is case data now, having been amended.
     * Each Pair represents a scenario where a User needs to see no label regarding Express processing.
     */
    private static Stream<Pair<FinremCaseData, FinremCaseData>> provideAmendedExpressCaseScenariosNeedingNoLabel() {

        // Both enrolled
        FinremCaseData dataDoesNotQualify = FinremCaseData.builder().build();
        dataDoesNotQualify.getExpressCaseWrapper().setExpressCaseParticipation(DOES_NOT_QUALIFY);

        FinremCaseData nullEnrollmentData = FinremCaseData.builder().build();

        // Case qualified for enrollment on creation, and still does following amendment,
        Pair<FinremCaseData, FinremCaseData> amendedStillDoesNotQualify = Pair.of(dataDoesNotQualify, dataDoesNotQualify);

        // Case did not qualify for enrollment, but it does upon amendment
        Pair<FinremCaseData, FinremCaseData> amendedWasNullStillDoesNotQualify = Pair.of(nullEnrollmentData, dataDoesNotQualify);

        // Case did not qualify for enrollment, and still doesn't upon amendment
        return Stream.of(
            amendedStillDoesNotQualify,
            amendedWasNullStillDoesNotQualify
        );
    }

    private static CaseDetails createCaseDetailsWithParticipation(ExpressCaseParticipation participation) {
        return CaseDetails.builder().data(
            Map.of(EXPRESS_CASE_PARTICIPATION, participation.getValue())).build();
    }

    private static FinremCaseData createFinRemEpCaseData(ExpressCaseParticipation epParticipation) {
        FinremCaseData data = FinremCaseData.builder().build();
        data.getExpressCaseWrapper().setExpressCaseParticipation(epParticipation);
        return data;
    }
}
