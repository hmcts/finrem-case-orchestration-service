package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DefaultCourtListWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationCourtListWrapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class GeneralApplicationCourtListWrapperTest {

    private GeneralApplicationCourtListWrapper generalApplicationCourtListWrapper;

    // Sample test data objects for each court list
    private final NottinghamCourt nottinghamCourtList = mock(NottinghamCourt.class);
    private final CfcCourt cfcCourtList = mock(CfcCourt.class);
    private final BirminghamCourt birminghamCourtList = mock(BirminghamCourt.class);
    private final LiverpoolCourt liverpoolCourtList = mock(LiverpoolCourt.class);
    private final ManchesterCourt manchesterCourtList = mock(ManchesterCourt.class);
    private final LancashireCourt lancashireCourtList = mock(LancashireCourt.class);
    private final ClevelandCourt clevelandCourtList = mock(ClevelandCourt.class);
    private final NwYorkshireCourt nwYorkshireCourtList = mock(NwYorkshireCourt.class);
    private final HumberCourt humberCourtList = mock(HumberCourt.class);
    private final KentSurreyCourt kentSurreyCourtList = mock(KentSurreyCourt.class);
    private final BedfordshireCourt bedfordshireCourtList = mock(BedfordshireCourt.class);
    private final ThamesValleyCourt thamesValleyCourtList = mock(ThamesValleyCourt.class);
    private final DevonCourt devonCourtList = mock(DevonCourt.class);
    private final DorsetCourt dorsetCourtList = mock(DorsetCourt.class);
    private final BristolCourt bristolCourtList = mock(BristolCourt.class);
    private final NewportCourt newportCourtList = mock(NewportCourt.class);
    private final SwanseaCourt swanseaCourtList = mock(SwanseaCourt.class);
    private final NorthWalesCourt northWalesCourtList = mock(NorthWalesCourt.class);
    private final HighCourt highCourtList = mock(HighCourt.class);

    @BeforeEach
    void setUp() {
        // Assuming your class has a constructor or setters to accept the court lists
        generalApplicationCourtListWrapper = GeneralApplicationCourtListWrapper.builder()
            .generalApplicationDirectionsNottinghamCourtList(nottinghamCourtList)
            .generalApplicationDirectionsCfcCourtList(cfcCourtList)
            .generalApplicationDirectionsBirminghamCourtList(birminghamCourtList)
            .generalApplicationDirectionsLiverpoolCourtList(liverpoolCourtList)
            .generalApplicationDirectionsManchesterCourtList(manchesterCourtList)
            .generalApplicationDirectionsLancashireCourtList(lancashireCourtList)
            .generalApplicationDirectionsClevelandCourtList(clevelandCourtList)
            .generalApplicationDirectionsNwYorkshireCourtList(nwYorkshireCourtList)
            .generalApplicationDirectionsHumberCourtList(humberCourtList)
            .generalApplicationDirectionsKentSurreyCourtList(kentSurreyCourtList)
            .generalApplicationDirectionsBedfordshireCourtList(bedfordshireCourtList)
            .generalApplicationDirectionsThamesValleyCourtList(thamesValleyCourtList)
            .generalApplicationDirectionsDevonCourtList(devonCourtList)
            .generalApplicationDirectionsDorsetCourtList(dorsetCourtList)
            .generalApplicationDirectionsBristolCourtList(bristolCourtList)
            .generalApplicationDirectionsNewportCourtList(newportCourtList)
            .generalApplicationDirectionsSwanseaCourtList(swanseaCourtList)
            .generalApplicationDirectionsNorthWalesCourtList(northWalesCourtList)
            .generalApplicationDirectionsHighCourtList(highCourtList)
            .build();
    }

    @Test
    void testToDefaultCourtListWrapper() {
        DefaultCourtListWrapper result = generalApplicationCourtListWrapper.toDefaultCourtListWrapper();

        assertThat(result.getNottinghamCourtList()).isEqualTo(nottinghamCourtList);
        assertThat(result.getCfcCourtList()).isEqualTo(cfcCourtList);
        assertThat(result.getBirminghamCourtList()).isEqualTo(birminghamCourtList);
        assertThat(result.getLiverpoolCourtList()).isEqualTo(liverpoolCourtList);
        assertThat(result.getManchesterCourtList()).isEqualTo(manchesterCourtList);
        assertThat(result.getLancashireCourtList()).isEqualTo(lancashireCourtList);
        assertThat(result.getClevelandCourtList()).isEqualTo(clevelandCourtList);
        assertThat(result.getNwYorkshireCourtList()).isEqualTo(nwYorkshireCourtList);
        assertThat(result.getHumberCourtList()).isEqualTo(humberCourtList);
        assertThat(result.getKentSurreyCourtList()).isEqualTo(kentSurreyCourtList);
        assertThat(result.getBedfordshireCourtList()).isEqualTo(bedfordshireCourtList);
        assertThat(result.getThamesValleyCourtList()).isEqualTo(thamesValleyCourtList);
        assertThat(result.getDevonCourtList()).isEqualTo(devonCourtList);
        assertThat(result.getDorsetCourtList()).isEqualTo(dorsetCourtList);
        assertThat(result.getBristolCourtList()).isEqualTo(bristolCourtList);
        assertThat(result.getNewportCourtList()).isEqualTo(newportCourtList);
        assertThat(result.getSwanseaCourtList()).isEqualTo(swanseaCourtList);
        assertThat(result.getNorthWalesCourtList()).isEqualTo(northWalesCourtList);
        assertThat(result.getHighCourtList()).isEqualTo(highCourtList);
    }
}
