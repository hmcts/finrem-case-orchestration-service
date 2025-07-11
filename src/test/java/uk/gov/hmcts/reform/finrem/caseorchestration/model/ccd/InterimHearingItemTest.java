package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DefaultCourtListWrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class InterimHearingItemTest {

    @Test
    void testToCourt() {
        // Prepare all dependent objects (can be mocks or real instances)
        Region region = Region.LONDON;
        RegionMidlandsFrc midlands = RegionMidlandsFrc.BIRMINGHAM;
        RegionLondonFrc london = RegionLondonFrc.LONDON;
        RegionNorthWestFrc northWest = RegionNorthWestFrc.LANCASHIRE;
        RegionNorthEastFrc northEast = RegionNorthEastFrc.CLEAVELAND;
        RegionSouthEastFrc southEast = RegionSouthEastFrc.KENT_FRC;
        RegionSouthWestFrc southWest = RegionSouthWestFrc.BRISTOL;
        RegionWalesFrc wales = RegionWalesFrc.NORTH_WALES;
        RegionHighCourtFrc highCourtFrc = RegionHighCourtFrc.HIGHCOURT;

        NottinghamCourt nottingham = NottinghamCourt.NOTTINGHAM_COUNTY_COURT_AND_FAMILY_COURT;
        CfcCourt cfc = CfcCourt.BROMLEY_COUNTY_COURT_AND_FAMILY_COURT;
        BirminghamCourt birmingham = BirminghamCourt.BIRMINGHAM_CIVIL_AND_FAMILY_JUSTICE_CENTRE;
        LiverpoolCourt liverpool = LiverpoolCourt.LIVERPOOL_CIVIL_FAMILY_COURT;
        ManchesterCourt manchester = ManchesterCourt.MANCHESTER_COURT;
        LancashireCourt lancashire = LancashireCourt.BARROW_COURT;
        ClevelandCourt cleveland = ClevelandCourt.FR_CLEVELAND_HC_LIST_1;
        NwYorkshireCourt nwYorkshire = NwYorkshireCourt.BRADFORD_COURT;
        HumberCourt humber = HumberCourt.CONSENTED_FR_humberList_1;
        KentSurreyCourt kentSurrey = KentSurreyCourt.CONSENTED_FR_kent_surreyList_1;
        BedfordshireCourt bedfordshire = BedfordshireCourt.BEDFORD;
        ThamesValleyCourt thamesValley = ThamesValleyCourt.MILTON_KEYNES;
        DevonCourt devon = DevonCourt.BARNSTAPLE;
        DorsetCourt dorset = DorsetCourt.ALDERSHOT;
        BristolCourt bristol = BristolCourt.BRISTOL_MAGISTRATES_COURT;
        NewportCourt newport = NewportCourt.FR_newportList_1;
        SwanseaCourt swansea = SwanseaCourt.FR_swanseaList_1;
        NorthWalesCourt northWales = NorthWalesCourt.CAERNARFON;
        HighCourt highCourt = HighCourt.HIGHCOURT_COURT;

        // Build InterimHearingItem with all fields set
        InterimHearingItem item = InterimHearingItem.builder()
            .interimRegionList(region)
            .interimMidlandsFrcList(midlands)
            .interimLondonFrcList(london)
            .interimNorthWestFrcList(northWest)
            .interimNorthEastFrcList(northEast)
            .interimSouthEastFrcList(southEast)
            .interimSouthWestFrcList(southWest)
            .interimWalesFrcList(wales)
            .interimHighCourtFrcList(highCourtFrc)
            .interimNottinghamCourtList(nottingham)
            .interimCfcCourtList(cfc)
            .interimBirminghamCourtList(birmingham)
            .interimLiverpoolCourtList(liverpool)
            .interimManchesterCourtList(manchester)
            .interimLancashireCourtList(lancashire)
            .interimClevelandCourtList(cleveland)
            .interimNwYorkshireCourtList(nwYorkshire)
            .interimHumberCourtList(humber)
            .interimKentSurreyCourtList(kentSurrey)
            .interimBedfordshireCourtList(bedfordshire)
            .interimThamesValleyCourtList(thamesValley)
            .interimDevonCourtList(devon)
            .interimDorsetCourtList(dorset)
            .interimBristolCourtList(bristol)
            .interimNewportCourtList(newport)
            .interimSwanseaCourtList(swansea)
            .interimNorthWalesCourtList(northWales)
            .interimHighCourtList(highCourt)
            .build();

        // Call toCourt
        Court court = item.toCourt();

        // Verify top-level region mappings
        assertEquals(region, court.getRegion());
        assertEquals(midlands, court.getMidlandsList());
        assertEquals(london, court.getLondonList());
        assertEquals(northWest, court.getNorthWestList());
        assertEquals(northEast, court.getNorthEastList());
        assertEquals(southEast, court.getSouthEastList());
        assertEquals(southWest, court.getSouthWestList());
        assertEquals(wales, court.getWalesList());
        assertEquals(highCourtFrc, court.getHcCourtList());

        // Verify court list wrapper mappings
        DefaultCourtListWrapper wrapper = court.getDefaultCourtListWrapper();
        assertNotNull(wrapper);

        assertEquals(nottingham, wrapper.getNottinghamCourtList());
        assertEquals(cfc, wrapper.getCfcCourtList());
        assertEquals(birmingham, wrapper.getBirminghamCourtList());
        assertEquals(liverpool, wrapper.getLiverpoolCourtList());
        assertEquals(manchester, wrapper.getManchesterCourtList());
        assertEquals(lancashire, wrapper.getLancashireCourtList());
        assertEquals(cleveland, wrapper.getClevelandCourtList());
        assertEquals(nwYorkshire, wrapper.getNwYorkshireCourtList());
        assertEquals(humber, wrapper.getHumberCourtList());
        assertEquals(kentSurrey, wrapper.getKentSurreyCourtList());
        assertEquals(bedfordshire, wrapper.getBedfordshireCourtList());
        assertEquals(thamesValley, wrapper.getThamesValleyCourtList());
        assertEquals(devon, wrapper.getDevonCourtList());
        assertEquals(dorset, wrapper.getDorsetCourtList());
        assertEquals(bristol, wrapper.getBristolCourtList());
        assertEquals(newport, wrapper.getNewportCourtList());
        assertEquals(swansea, wrapper.getSwanseaCourtList());
        assertEquals(northWales, wrapper.getNorthWalesCourtList());
        assertEquals(highCourt, wrapper.getHighCourtList());
    }
}
