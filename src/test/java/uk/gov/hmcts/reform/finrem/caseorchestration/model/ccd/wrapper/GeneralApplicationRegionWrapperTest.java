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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

class GeneralApplicationRegionWrapperTest {

    @Test
    void testToCourt() {
        // Arrange
        GeneralApplicationRegionWrapper underTest = spy(GeneralApplicationRegionWrapper.builder()
            .generalApplicationDirectionsRegionList(Region.MIDLANDS)
            .generalApplicationDirectionsMidlandsFrcList(RegionMidlandsFrc.BIRMINGHAM)
            .generalApplicationDirectionsLondonFrcList(RegionLondonFrc.LONDON)
            .generalApplicationDirectionsNorthWestFrcList(RegionNorthWestFrc.LIVERPOOL)
            .generalApplicationDirectionsNorthEastFrcList(RegionNorthEastFrc.CLEAVELAND)
            .generalApplicationDirectionsSouthEastFrcList(RegionSouthEastFrc.KENT_FRC)
            .generalApplicationDirectionsSouthWestFrcList(RegionSouthWestFrc.BRISTOL)
            .generalApplicationDirectionsWalesFrcList(RegionWalesFrc.NORTH_WALES)
            .generalApplicationDirectionsHighCourtFrcList(RegionHighCourtFrc.HIGHCOURT)
            .build());
        GeneralApplicationCourtListWrapper mockGeneralApplicationCourtListWrapper = mock(GeneralApplicationCourtListWrapper.class);
        DefaultCourtListWrapper mockDefaultCourtListWrapper = mock(DefaultCourtListWrapper.class);
        when(mockGeneralApplicationCourtListWrapper.toDefaultCourtListWrapper()).thenReturn(mockDefaultCourtListWrapper);
        when(underTest.getCourtListWrapper()).thenReturn(mockGeneralApplicationCourtListWrapper);

        // Act
        Court court = underTest.toCourt();

        // Assert
        assertThat(court.getRegion()).isEqualTo(Region.MIDLANDS);
        assertThat(court.getMidlandsList()).isEqualTo(RegionMidlandsFrc.BIRMINGHAM);
        assertThat(court.getLondonList()).isEqualTo(RegionLondonFrc.LONDON);
        assertThat(court.getNorthWestList()).isEqualTo(RegionNorthWestFrc.LIVERPOOL);
        assertThat(court.getNorthEastList()).isEqualTo(RegionNorthEastFrc.CLEAVELAND);
        assertThat(court.getSouthEastList()).isEqualTo(RegionSouthEastFrc.KENT_FRC);
        assertThat(court.getSouthWestList()).isEqualTo(RegionSouthWestFrc.BRISTOL);
        assertThat(court.getWalesList()).isEqualTo(RegionWalesFrc.NORTH_WALES);
        assertThat(court.getHcCourtList()).isEqualTo(RegionHighCourtFrc.HIGHCOURT);
        assertThat(court.getDefaultCourtListWrapper()).isEqualTo(mockDefaultCourtListWrapper);
    }
}
