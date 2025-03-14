package uk.gov.hmcts.reform.finrem.caseorchestration.service.express;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ExpressCaseParticipation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionMidlandsFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.AllocatedRegionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DefaultCourtListWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.NatureApplicationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.RegionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ScheduleOneWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.EXPRESS_CASE_PARTICIPATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.EstimatedAssetV2.UNABLE_TO_QUANTIFY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.EstimatedAssetV2.UNDER_TWO_HUNDRED_AND_FIFTY_THOUSAND_POUNDS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ExpressCaseParticipation.DOES_NOT_QUALIFY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ExpressCaseParticipation.ENROLLED;
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
    private ExpressCaseService expressCaseService;

    @Mock
    private FeatureToggleService featureToggleService;

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(expressCaseService, "expressCaseFrcs", List.of("FR_s_NottinghamList_1", "FR_s_NottinghamList_2"));
    }

    @Test
    void setExpressCaseEnrollmentStatus_shouldEnrollInExpressPilot_WhenCaseDataMeetsRequirements() {
        FinremCaseData caseData = createCaseData();
        expressCaseService.setExpressCaseEnrollmentStatus(caseData);
        assertEquals(ENROLLED, caseData.getExpressCaseParticipation());
    }

    @ParameterizedTest
    @MethodSource("provideInvalidCaseData")
    void setExpressCaseEnrollmentStatus_shouldNotEnroll_WhenCaseDataDoesNotMeetCriteria(FinremCaseData caseData) {
        expressCaseService.setExpressCaseEnrollmentStatus(caseData);
        assertEquals(DOES_NOT_QUALIFY, caseData.getExpressCaseParticipation());
    }

    /*
     * Only one scenario sets amendedExpressCaseCriteriaNotMet to Yes; when a Case was enrolled,
     * but the answers have changed in amendment so express criteria is no longer met.
     */
    @Test
    void setWhetherDisqualifiedFromExpress_shouldSetYes_WhenCaseAmendedToDisqualifyFromExpress() {
        FinremCaseData caseDataOnceAmended = new FinremCaseData();
        caseDataOnceAmended.setExpressCaseParticipation(DOES_NOT_QUALIFY);

        FinremCaseData caseDataBeforeAmending = new FinremCaseData();
        caseDataBeforeAmending.setExpressCaseParticipation(ENROLLED);

        expressCaseService.setWhetherDisqualifiedFromExpress(caseDataOnceAmended, caseDataBeforeAmending);
        assertEquals(YesOrNo.YES, caseDataOnceAmended.getAmendedExpressCaseCriteriaNotMet());
    }

    @ParameterizedTest
    @MethodSource("provideAmendedExpressCaseParticipation")
    void setWhetherDisqualifiedFromExpress_shouldSetNo_WhenCaseAmendmentDoesNotDisqualify(
            Pair<FinremCaseData, FinremCaseData> caseDataBeforeAndAfterAmending) {

        FinremCaseData dataBeforeAmending = caseDataBeforeAndAfterAmending.getLeft();
        FinremCaseData amendedData = caseDataBeforeAndAfterAmending.getRight();

        expressCaseService.setWhetherDisqualifiedFromExpress(amendedData, dataBeforeAmending);

        assertEquals(YesOrNo.NO, amendedData.getAmendedExpressCaseCriteriaNotMet());
    }

    @ParameterizedTest
    @MethodSource("provideIsExpressCase")
    void shouldReturnIfCaseIsExpressEnrolledAndReturnFalseIfExpressIsDisabledCaseDetails(boolean isExpressPilotEnabled,
                                                                         CaseDetails caseDetails,
                                                                         boolean expected) {
        when(featureToggleService.isExpressPilotEnabled()).thenReturn(isExpressPilotEnabled);
        assertEquals(expected, expressCaseService.isExpressCase(caseDetails));
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

    private static CaseDetails createCaseDetailsWithParticipation(ExpressCaseParticipation participation) {
        return CaseDetails.builder().data(
            Map.of(EXPRESS_CASE_PARTICIPATION, participation.getValue())).build();
    }

    private static Stream<FinremCaseData> provideInvalidCaseData() {
        FinremCaseData hasVariationOrder = createCaseData();
        hasVariationOrder.getNatureApplicationWrapper()
            .setNatureOfApplicationChecklist(List.of(CONTESTED_VARIATION_ORDER));

        FinremCaseData isNotInParticipatingFRC = createCaseData();
        isNotInParticipatingFRC.getRegionWrapper().getAllocatedRegionWrapper()
            .getDefaultCourtListWrapper().setNottinghamCourtList(BOSTON_COUNTY_COURT_AND_FAMILY_COURT);

        FinremCaseData isNotUnder250k = createCaseData();
        isNotUnder250k.setEstimatedAssetsChecklistV2(UNABLE_TO_QUANTIFY);

        FinremCaseData isFastTrackApplication = createCaseData();
        isFastTrackApplication.setFastTrackDecision(YesOrNo.YES);

        FinremCaseData isNotMatrimonialApplication = createCaseData();
        isNotMatrimonialApplication.getScheduleOneWrapper().setTypeOfApplication(SCHEDULE_1_CHILDREN_ACT_1989);

        return Stream.of(
            hasVariationOrder,
            isNotInParticipatingFRC,
            isNotUnder250k,
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
            .estimatedAssetsChecklistV2(UNDER_TWO_HUNDRED_AND_FIFTY_THOUSAND_POUNDS)
            .build();
    }

    /*
     * Creates pairs of FinremCaseData.  Case data now and case data before.
     * Each of these has expressCaseParticipation states set
     * These sets, when checked, should not cause getAmendedExpressCaseCriteriaNotMet
     * to be set to "Yes".
     */
    private static Stream<Pair<FinremCaseData, FinremCaseData>> provideAmendedExpressCaseParticipation() {

        // Both enrolled
        FinremCaseData dataBeforeAmending = FinremCaseData.builder().expressCaseParticipation(ENROLLED).build();
        FinremCaseData dataAfterAmending = FinremCaseData.builder().expressCaseParticipation(ENROLLED).build();
        Pair<FinremCaseData, FinremCaseData> bothEnrolled = Pair.of(dataBeforeAmending, dataAfterAmending);



        return Stream.of(
                bothEnrolled
        );
    }
}
