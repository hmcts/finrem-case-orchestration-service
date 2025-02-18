package uk.gov.hmcts.reform.finrem.caseorchestration.service.expresspilot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionMidlandsFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.AllocatedRegionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DefaultCourtListWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.NatureApplicationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.RegionWrapper;


import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.EstimatedAssetV2.UNABLE_TO_QUANTIFY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.EstimatedAssetV2.UNDER_TWO_HUNDRED_AND_FIFTY_THOUSAND_POUNDS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ExpressPilotParticipation.DOES_NOT_QUALIFY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ExpressPilotParticipation.ENROLLED;
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

class ExpressPilotServiceTest {

    @Mock
    private FinremCaseData caseData;

    @BeforeEach
    public void setUp() {
        expressPilotService = new ExpressPilotService();
        expressPilotService.expressPilotFrcs = List.of("FR_s_NottinghamList_1", "FR_s_NottinghamList_2");
    }

    @InjectMocks
    private ExpressPilotService expressPilotService;

    @Test
    void shouldEnrollInExpressPilot_WhenCaseDataMeetsRequirements() {

        FinremCaseData caseData = FinremCaseData.builder()
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

        expressPilotService.setPilotEnrollmentStatus(caseData);
        assertEquals(ENROLLED, caseData.getExpressPilotParticipation());
    }

    @ParameterizedTest
    @MethodSource("provideInvalidCaseData")
    void shouldNotQualify_WhenCaseDataDoesNotMeetCriteria(FinremCaseData caseData) {
        expressPilotService.setPilotEnrollmentStatus(caseData);
        assertEquals(DOES_NOT_QUALIFY, caseData.getExpressPilotParticipation());
    }

    private static Stream<FinremCaseData> provideInvalidCaseData() {
        return Stream.of(
            // Has contested variation order
            FinremCaseData.builder()
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
                   CONTESTED_VARIATION_ORDER
                ))
                .build())
            .fastTrackDecision(YesOrNo.NO)
            .estimatedAssetsChecklistV2(UNDER_TWO_HUNDRED_AND_FIFTY_THOUSAND_POUNDS)
            .build(),
            // Is not in participating FRC
            FinremCaseData.builder()
                .regionWrapper(RegionWrapper.builder()
                    .allocatedRegionWrapper(AllocatedRegionWrapper.builder()
                        .courtListWrapper(DefaultCourtListWrapper.builder()
                            .nottinghamCourtList(BOSTON_COUNTY_COURT_AND_FAMILY_COURT)
                            .build())
                        .regionList(MIDLANDS)
                        .midlandsFrcList(RegionMidlandsFrc.NOTTINGHAM)
                        .build())
                    .build())
                .natureApplicationWrapper(NatureApplicationWrapper.builder()
                    .natureOfApplicationChecklist(List.of(
                        MAINTENANCE_PENDING_SUIT
                    ))
                    .build())
                .fastTrackDecision(YesOrNo.NO)
                .estimatedAssetsChecklistV2(UNDER_TWO_HUNDRED_AND_FIFTY_THOUSAND_POUNDS)
                .build(),
            // Is not under £250,000
            FinremCaseData.builder()
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
                        MAINTENANCE_PENDING_SUIT
                    ))
                    .build())
                .fastTrackDecision(YesOrNo.NO)
                .estimatedAssetsChecklistV2(UNABLE_TO_QUANTIFY)
                .build(),
            // Is a Fast Track Application
            FinremCaseData.builder()
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
                        MAINTENANCE_PENDING_SUIT
                    ))
                    .build())
                .fastTrackDecision(YesOrNo.YES)
                .estimatedAssetsChecklistV2(UNDER_TWO_HUNDRED_AND_FIFTY_THOUSAND_POUNDS)
                .build()
        );
    }
}