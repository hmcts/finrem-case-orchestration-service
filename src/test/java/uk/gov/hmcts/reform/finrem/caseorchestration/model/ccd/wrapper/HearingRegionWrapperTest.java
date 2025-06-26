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
        underTest.setHearingRegionList(region);
        RegionMidlandsFrc midlands = mock(RegionMidlandsFrc.class);
        underTest.setHearingMidlandsFrcList(midlands);
        RegionLondonFrc london = mock(RegionLondonFrc.class);
        underTest.setHearingLondonFrcList(london);
        RegionNorthWestFrc northWest = mock(RegionNorthWestFrc.class);
        underTest.setHearingNorthWestFrcList(northWest);
        RegionNorthEastFrc northEast = mock(RegionNorthEastFrc.class);
        underTest.setHearingNorthEastFrcList(northEast);
        RegionSouthEastFrc southEast = mock(RegionSouthEastFrc.class);
        underTest.setHearingSouthEastFrcList(southEast);
        RegionSouthWestFrc southWest = mock(RegionSouthWestFrc.class);
        underTest.setHearingSouthWestFrcList(southWest);
        RegionWalesFrc wales = mock(RegionWalesFrc.class);
        underTest.setHearingWalesFrcList(wales);
        RegionHighCourtFrc highCourt = mock(RegionHighCourtFrc.class);
        underTest.setHearingHighCourtFrcList(highCourt);
        HearingCourtWrapper courtWrapper = mock(HearingCourtWrapper.class);
        DefaultCourtListWrapper defaultWrapper = mock(DefaultCourtListWrapper.class);
        when(courtWrapper.toDefaultCourtListWrapper()).thenReturn(defaultWrapper);
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
