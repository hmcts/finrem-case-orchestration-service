package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Court;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Region;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionHighCourtFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionLondonFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionMidlandsFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionNorthEastFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionNorthWestFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionSouthEastFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionSouthWestFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionWalesFrc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HearingRegionWrapperTest {

    @Test
    void shouldBuildCourtFromCourtDataFields() {
        // Arrange
        HearingRegionWrapper underTest = new HearingRegionWrapper();

        Region region = mock(Region.class);
        RegionMidlandsFrc midlands = mock(RegionMidlandsFrc.class);
        RegionLondonFrc london = mock(RegionLondonFrc.class);
        RegionNorthWestFrc northWest = mock(RegionNorthWestFrc.class);
        RegionNorthEastFrc northEast = mock(RegionNorthEastFrc.class);
        RegionSouthEastFrc southEast = mock(RegionSouthEastFrc.class);
        RegionSouthWestFrc southWest = mock(RegionSouthWestFrc.class);
        RegionWalesFrc wales = mock(RegionWalesFrc.class);
        RegionHighCourtFrc highCourt = mock(RegionHighCourtFrc.class);
        DefaultCourtListWrapper defaultWrapper = mock(DefaultCourtListWrapper.class);

        HearingCourtWrapper courtWrapper = mock(HearingCourtWrapper.class);
        when(courtWrapper.toDefaultCourtListWrapper()).thenReturn(defaultWrapper);

        underTest.setHearingRegionList(region);
        underTest.setHearingMidlandsFrcList(midlands);
        underTest.setHearingLondonFrcList(london);
        underTest.setHearingNorthWestFrcList(northWest);
        underTest.setHearingNorthEastFrcList(northEast);
        underTest.setHearingSouthEastFrcList(southEast);
        underTest.setHearingSouthWestFrcList(southWest);
        underTest.setHearingWalesFrcList(wales);
        underTest.setHearingHighCourtFrcList(highCourt);
        underTest.setCourtListWrapper(courtWrapper);

        // Act
        Court result = underTest.toCourt();

        // Assert
        assertEquals(region, result.getRegion());
        assertEquals(midlands, result.getMidlandsList());
        assertEquals(london, result.getLondonList());
        assertEquals(northWest, result.getNorthWestList());
        assertEquals(northEast, result.getNorthEastList());
        assertEquals(southEast, result.getSouthEastList());
        assertEquals(southWest, result.getSouthWestList());
        assertEquals(wales, result.getWalesList());
        assertEquals(highCourt, result.getHcCourtList());
        assertEquals(defaultWrapper, result.getDefaultCourtListWrapper());
    }

}
