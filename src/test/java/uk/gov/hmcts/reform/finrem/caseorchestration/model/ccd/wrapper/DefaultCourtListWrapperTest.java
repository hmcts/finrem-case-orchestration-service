package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

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

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultCourtListWrapperTest {

    @ParameterizedTest
    @MethodSource
    void testIsAnyCourtSelected(String courtId, boolean expected) {
        DefaultCourtListWrapper wrapper = new DefaultCourtListWrapper();
        wrapper.setCourt(courtId, false);

        assertThat(wrapper.isAnyCourtSelected()).isEqualTo(expected);
    }

    private static Stream<Arguments> testIsAnyCourtSelected() {
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
            Arguments.of(KentSurreyCourt.KENT_DARTFORD_COURTS.getId(), true),
            Arguments.of(BedfordshireCourt.IPSWICH.getId(), true),
            Arguments.of(DevonCourt.YEOVIL.getId(), true),
            Arguments.of(DorsetCourt.ISLE_OF_WIGHT.getId(), true),
            Arguments.of(BristolCourt.BATH_LAW_COURTS.getId(), true),
            Arguments.of(NewportCourt.FR_newportList_3.getId(), true),
            Arguments.of(SwanseaCourt.FR_swanseaList_1.getId(), true),
            Arguments.of(NorthWalesCourt.WELSHPOOL.getId(), true),
            Arguments.of(HighCourt.HIGHCOURT_COURT.getId(), true)
        );
    }

}
