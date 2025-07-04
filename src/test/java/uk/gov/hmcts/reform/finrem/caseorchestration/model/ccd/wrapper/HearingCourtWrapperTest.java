package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import org.junit.jupiter.api.Test;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ManchesterCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NewportCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NorthWalesCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NottinghamCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NwYorkshireCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.SwanseaCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ThamesValleyCourt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class HearingCourtWrapperTest {

    @Test
    void shouldMapFieldsToDefaultCourtListWrapperCorrectly() {
        // Arrange - create mock court objects
        NottinghamCourt nottingham = mock(NottinghamCourt.class);
        CfcCourt cfc = mock(CfcCourt.class);
        BirminghamCourt birmingham = mock(BirminghamCourt.class);
        LiverpoolCourt liverpool = mock(LiverpoolCourt.class);
        ManchesterCourt manchester = mock(ManchesterCourt.class);
        LancashireCourt lancashire = mock(LancashireCourt.class);
        ClevelandCourt cleveland = mock(ClevelandCourt.class);
        NwYorkshireCourt nwYorkshire = mock(NwYorkshireCourt.class);
        HumberCourt humber = mock(HumberCourt.class);
        KentSurreyCourt kentSurrey = mock(KentSurreyCourt.class);
        BedfordshireCourt bedfordshire = mock(BedfordshireCourt.class);
        ThamesValleyCourt thamesValley = mock(ThamesValleyCourt.class);
        DevonCourt devon = mock(DevonCourt.class);
        DorsetCourt dorset = mock(DorsetCourt.class);
        BristolCourt bristol = mock(BristolCourt.class);
        NewportCourt newport = mock(NewportCourt.class);
        SwanseaCourt swansea = mock(SwanseaCourt.class);
        NorthWalesCourt northWales = mock(NorthWalesCourt.class);
        HighCourt highCourt = mock(HighCourt.class);

        HearingCourtWrapper wrapper = HearingCourtWrapper.builder()
            .hearingNottinghamCourtList(nottingham)
            .hearingCfcCourtList(cfc)
            .hearingBirminghamCourtList(birmingham)
            .hearingLiverpoolCourtList(liverpool)
            .hearingManchesterCourtList(manchester)
            .hearingLancashireCourtList(lancashire)
            .hearingClevelandCourtList(cleveland)
            .hearingNwYorkshireCourtList(nwYorkshire)
            .hearingHumberCourtList(humber)
            .hearingKentSurreyCourtList(kentSurrey)
            .hearingBedfordshireCourtList(bedfordshire)
            .hearingThamesValleyCourtList(thamesValley)
            .hearingDevonCourtList(devon)
            .hearingDorsetCourtList(dorset)
            .hearingBristolCourtList(bristol)
            .hearingNewportCourtList(newport)
            .hearingSwanseaCourtList(swansea)
            .hearingNorthWalesCourtList(northWales)
            .hearingHighCourtList(highCourt)
            .build();

        // Act
        DefaultCourtListWrapper result = wrapper.toDefaultCourtListWrapper();

        // Assert
        assertEquals(nottingham, result.getNottinghamCourtList());
        assertEquals(cfc, result.getCfcCourtList());
        assertEquals(birmingham, result.getBirminghamCourtList());
        assertEquals(liverpool, result.getLiverpoolCourtList());
        assertEquals(manchester, result.getManchesterCourtList());
        assertEquals(lancashire, result.getLancashireCourtList());
        assertEquals(cleveland, result.getClevelandCourtList());
        assertEquals(nwYorkshire, result.getNwYorkshireCourtList());
        assertEquals(humber, result.getHumberCourtList());
        assertEquals(kentSurrey, result.getKentSurreyCourtList());
        assertEquals(bedfordshire, result.getBedfordshireCourtList());
        assertEquals(thamesValley, result.getThamesValleyCourtList());
        assertEquals(devon, result.getDevonCourtList());
        assertEquals(dorset, result.getDorsetCourtList());
        assertEquals(bristol, result.getBristolCourtList());
        assertEquals(newport, result.getNewportCourtList());
        assertEquals(swansea, result.getSwanseaCourtList());
        assertEquals(northWales, result.getNorthWalesCourtList());
        assertEquals(highCourt, result.getHighCourtList());
    }

}
