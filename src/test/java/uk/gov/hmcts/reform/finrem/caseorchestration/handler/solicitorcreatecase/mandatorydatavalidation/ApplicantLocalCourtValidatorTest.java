package uk.gov.hmcts.reform.finrem.caseorchestration.handler.solicitorcreatecase.mandatorydatavalidation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BedfordshireCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BirminghamCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BristolCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CfcCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ClevelandCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DevonCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DorsetCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HighCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HumberCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.KentSurreyCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.LancashireCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.LiverpoolCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.LondonCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ManchesterCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NewportCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NorthWalesCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NottinghamCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NwYorkshireCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.SwanseaCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.AllocatedRegionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DefaultCourtListWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.RegionWrapper;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicantLocalCourtValidatorTest {

    @ParameterizedTest
    @MethodSource
    void testValidate(String courtId, boolean expectedValid) {
        DefaultCourtListWrapper courtList = new DefaultCourtListWrapper();
        courtList.setCourt(courtId, false);
        FinremCaseData caseData = FinremCaseData.builder()
            .regionWrapper(RegionWrapper.builder()
                .allocatedRegionWrapper(AllocatedRegionWrapper.builder()
                    .courtListWrapper(courtList)
                    .build())
                .build())
            .build();

        ApplicantLocalCourtValidator validator = new ApplicantLocalCourtValidator();
        List<String> validationErrors = validator.validate(caseData);

        if (expectedValid) {
            assertThat(validationErrors).isEmpty();
        } else {
            assertThat(validationErrors).hasSize(1);
            assertThat(validationErrors.get(0)).isEqualTo(
                "Applicant's Local Court is required. Update Please choose the Region in which the Applicant resides");
        }
    }

    private static Stream<Arguments> testValidate() {
        return Stream.of(
            Arguments.of("Not a valid court id", false),
            Arguments.of(NottinghamCourt.CHESTERFIELD_COUNTY_COURT.getId(), true),
            Arguments.of(CfcCourt.CENTRAL_FAMILY_COURT.getId(), true),
            Arguments.of(BirminghamCourt.BIRMINGHAM_CIVIL_AND_FAMILY_JUSTICE_CENTRE.getId(), true),
            Arguments.of(LondonCourt.CENTRAL_FAMILY_COURT.getId(), true),
            Arguments.of(LiverpoolCourt.BIRKENHEAD_COUNTY_FAMILY_COURT.getId(), true),
            Arguments.of(ManchesterCourt.STOCKPORT_COURT.getId(), true),
            Arguments.of(LancashireCourt.CARLISLE_COURT.getId(), true),
            Arguments.of(ClevelandCourt.FR_CLEVELAND_HC_LIST_5.getId(), true),
            Arguments.of(NwYorkshireCourt.BRADFORD_COURT.getId(), true),
            Arguments.of(HumberCourt.FR_humberList_4.getId(), true),
            Arguments.of(KentSurreyCourt.FR_kent_surreyList_3.getId(), true),
            Arguments.of(BedfordshireCourt.IPSWICH.getId(), true),
            Arguments.of(DevonCourt.YEOVIL.getId(), true),
            Arguments.of(DorsetCourt.ISLE_OF_WIGHT.getId(), true),
            Arguments.of(BristolCourt.BATH_LAW_COURTS.getId(), true),
            Arguments.of(NewportCourt.FR_newportList_3.getId(), true),
            Arguments.of(SwanseaCourt.FR_swanseaList_1.getId(), true),
            Arguments.of(NorthWalesCourt.WELSHPOOL.getId(), true),
            Arguments.of(HighCourt.HIGHCOURT_COURT.getId(), true),
            Arguments.of(KentSurreyCourt.FR_kent_surreyList_11.getId(), true)
            );
    }
}
