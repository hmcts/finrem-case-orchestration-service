package uk.gov.hmcts.reform.finrem.caseorchestration.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.MissingCourtException;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BirminghamCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CfcCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.LondonCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Region;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionLondonFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionMidlandsFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionWalesFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.SwanseaCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.AllocatedRegionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DefaultCourtListWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationCourtListWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.InterimCourtListWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CourtDetailsTemplateFields;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CourtDetailsMapperTest {

    private CourtDetailsMapper courtDetailsMapper;

    @BeforeEach
    void setUp() {
        courtDetailsMapper = new CourtDetailsMapper(new ObjectMapper());
    }

    @Test
    void givenDefaultCourtListAndValidFieldDeclarations_whenGetCourtDetails_thenReturnExpectedCourtDetails() {
        DefaultCourtListWrapper courtList = new DefaultCourtListWrapper();
        courtList.setCfcCourtList(CfcCourt.BROMLEY_COUNTY_COURT_AND_FAMILY_COURT);

        CourtDetailsTemplateFields courtDetails = courtDetailsMapper.getCourtDetails(courtList);

        assertThat(courtDetails.getCourtName(), is("Bromley County Court And Family Court"));
        assertThat(courtDetails.getCourtAddress(), is("Bromley County Court, College Road, Bromley, BR1 3PX"));
        assertThat(courtDetails.getEmail(), is("FRCLondon@justice.gov.uk"));
        assertThat(courtDetails.getPhoneNumber(), is("0300 123 5577"));
    }

    @Test
    void givenGeneralApplicationCourtListAndValidFieldDeclarations_whenGetCourtDetails_thenReturnExpectedCourtDetails() {
        GeneralApplicationCourtListWrapper courtList = new GeneralApplicationCourtListWrapper();
        courtList.setGeneralApplicationDirectionsCfcCourtList(CfcCourt.CROYDON_COUNTY_COURT_AND_FAMILY_COURT);

        CourtDetailsTemplateFields courtDetails = courtDetailsMapper.getCourtDetails(courtList);

        assertThat(courtDetails.getCourtName(), is("Croydon County Court And Family Court"));
        assertThat(courtDetails.getCourtAddress(), is("Croydon County Court, Altyre Road, Croydon, CR9 5AB"));
        assertThat(courtDetails.getEmail(), is("FRCLondon@justice.gov.uk"));
        assertThat(courtDetails.getPhoneNumber(), is("0300 123 5577"));
    }

    @Test
    void givenInterimCourtListAndValidFieldDeclarations_whenGetCourtDetails_thenReturnExpectedCourtDetails() {
        InterimCourtListWrapper courtList = new InterimCourtListWrapper();
        courtList.setInterimCfcCourtList(CfcCourt.BARNET_CIVIL_AND_FAMILY_COURTS_CENTRE);

        CourtDetailsTemplateFields courtDetails = courtDetailsMapper.getCourtDetails(courtList);

        assertThat(courtDetails.getCourtName(), is("Barnet Civil And Family Courts Centre"));
        assertThat(courtDetails.getCourtAddress(), is("Barnet County Court, St Marys Court, "
            + "Regents Park Road, Finchley Central, London, N3 1BQ"));
        assertThat(courtDetails.getEmail(), is("FRCLondon@justice.gov.uk"));
        assertThat(courtDetails.getPhoneNumber(), is("0300 123 5577"));
    }

    @Test
    void givenCourtListAndInvalidFieldDeclarations_whenGetCourtDetails_thenThrowIllegalStateException() {
        try {
            courtDetailsMapper.getCourtDetails(new DefaultCourtListWrapper());
        } catch (MissingCourtException ex) {
            String expectedMessage = "There must be exactly one court selected in case data";
            assertTrue(ex.getMessage().contains(expectedMessage));
        }
    }

    @Test
    void givenCaseDataWithDifferentRegions_whenGetCaseDetailsWithOnlyLatestAllocatedCourt_thenCaseDataWithOnlyOneCourt() {
        AllocatedRegionWrapper allocatedRegionWrapper = AllocatedRegionWrapper.builder().regionList(Region.MIDLANDS)
            .londonFrcList(RegionLondonFrc.LONDON)
            .midlandsFrcList(RegionMidlandsFrc.BIRMINGHAM)
            .courtListWrapper(
                DefaultCourtListWrapper.builder()
                    .cfcCourtList(CfcCourt.BRENTFORD_COUNTY_AND_FAMILY_COURT)
                    .birminghamCourtList(BirminghamCourt.WORCESTER_COMBINED_COURT)
                    .build())
            .build();
        AllocatedRegionWrapper allocatedRegionWrapperBefore = AllocatedRegionWrapper.builder().regionList(Region.LONDON)
            .londonFrcList(RegionLondonFrc.LONDON)
            .courtListWrapper(
                DefaultCourtListWrapper.builder().cfcCourtList(CfcCourt.BRENTFORD_COUNTY_AND_FAMILY_COURT).build())
            .build();

        AllocatedRegionWrapper allocatedRegionWrapperReturn =
            courtDetailsMapper.getLatestAllocatedCourt(allocatedRegionWrapperBefore, allocatedRegionWrapper, null);

        assertThat(allocatedRegionWrapperReturn.getRegionList(),
            is(equalTo(Region.MIDLANDS)));
        assertThat(allocatedRegionWrapperReturn.getMidlandsFrcList(),
            is(equalTo(RegionMidlandsFrc.BIRMINGHAM)));
        assertThat(allocatedRegionWrapperReturn.getDefaultCourtListWrapper().getBirminghamCourtList(),
            is(equalTo(BirminghamCourt.WORCESTER_COMBINED_COURT)));
        assertThat(allocatedRegionWrapperReturn.getLondonFrcList(),
            is(nullValue()));
        assertThat(allocatedRegionWrapperReturn.getDefaultCourtListWrapper().getCfcCourtList(),
            is(nullValue()));
    }

    @Test
    void givenCaseDataWithSameCourts_whenGetCaseDetailsWithOnlyLatestAllocatedCourt_thenCaseDataUnchanged() {
        AllocatedRegionWrapper allocatedRegionWrapper = AllocatedRegionWrapper.builder().regionList(Region.LONDON)
            .londonFrcList(RegionLondonFrc.LONDON)
            .courtListWrapper(
                DefaultCourtListWrapper.builder().cfcCourtList(CfcCourt.BRENTFORD_COUNTY_AND_FAMILY_COURT).build())
            .build();
        AllocatedRegionWrapper allocatedRegionWrapperBefore = AllocatedRegionWrapper.builder().regionList(Region.LONDON)
            .londonFrcList(RegionLondonFrc.LONDON)
            .courtListWrapper(
                DefaultCourtListWrapper.builder().cfcCourtList(CfcCourt.BRENTFORD_COUNTY_AND_FAMILY_COURT).build())
            .build();

        AllocatedRegionWrapper allocatedRegionWrapperReturn =
            courtDetailsMapper.getLatestAllocatedCourt(
                allocatedRegionWrapperBefore, allocatedRegionWrapper, true);

        assertThat(allocatedRegionWrapperReturn.getRegionList(),
            is(equalTo(Region.LONDON)));
        assertThat(allocatedRegionWrapperReturn.getLondonFrcList(),
            is(equalTo(RegionLondonFrc.LONDON)));
        assertThat(allocatedRegionWrapperReturn.getDefaultCourtListWrapper().getCfcCourtList(),
            is(equalTo(CfcCourt.BRENTFORD_COUNTY_AND_FAMILY_COURT)));

    }

    @Test
    void givenCaseDataWithThreeCourtLists_whenGetCaseDetailsWithOnlyLatestAllocatedCourt_thenCaseDataWithOnlyOneCourt() {
        AllocatedRegionWrapper allocatedRegionWrapper = AllocatedRegionWrapper.builder().regionList(Region.MIDLANDS)
            .midlandsFrcList(RegionMidlandsFrc.BIRMINGHAM)
            .londonFrcList(RegionLondonFrc.LONDON)
            .walesFrcList(RegionWalesFrc.SWANSEA)
            .courtListWrapper(
                DefaultCourtListWrapper.builder()
                    .birminghamCourtList(BirminghamCourt.WORCESTER_COMBINED_COURT)
                    .cfcCourtList(CfcCourt.BRENTFORD_COUNTY_AND_FAMILY_COURT)
                    .swanseaCourtList(SwanseaCourt.FR_swanseaList_1)
                    .build())
            .build();
        AllocatedRegionWrapper allocatedRegionWrapperBefore = AllocatedRegionWrapper.builder().regionList(Region.LONDON)
            .londonFrcList(RegionLondonFrc.LONDON)
            .walesFrcList(RegionWalesFrc.SWANSEA)
            .courtListWrapper(
                DefaultCourtListWrapper.builder()
                    .cfcCourtList(CfcCourt.BRENTFORD_COUNTY_AND_FAMILY_COURT)
                    .swanseaCourtList(SwanseaCourt.FR_swanseaList_1)
                    .build())
            .build();

        AllocatedRegionWrapper allocatedRegionWrapperReturn =
            courtDetailsMapper.getLatestAllocatedCourt(allocatedRegionWrapperBefore, allocatedRegionWrapper,
                null);

        assertThat(allocatedRegionWrapperReturn.getRegionList(),
            is(equalTo(Region.MIDLANDS)));
        assertThat(allocatedRegionWrapperReturn.getMidlandsFrcList(),
            is(equalTo(RegionMidlandsFrc.BIRMINGHAM)));
        assertThat(allocatedRegionWrapperReturn.getDefaultCourtListWrapper().getBirminghamCourtList(),
            is(equalTo(BirminghamCourt.WORCESTER_COMBINED_COURT)));
        assertThat(allocatedRegionWrapperReturn.getLondonFrcList(),
            is(nullValue()));
        assertThat(allocatedRegionWrapperReturn.getWalesFrcList(),
            is(nullValue()));
        assertThat(allocatedRegionWrapperReturn.getDefaultCourtListWrapper().getCfcCourtList(),
            is(nullValue()));
        assertThat(allocatedRegionWrapperReturn.getDefaultCourtListWrapper().getSwanseaCourtList(),
            is(nullValue()));
    }

    @Test
    void givenCaseDataWithDifferentCourtsOnSameFrc_whenGetCaseDetailsWithOnlyLatestAllocatedCourt_thenCaseDataWithOnlyOneLatestCourt() {
        AllocatedRegionWrapper allocatedRegionWrapper = AllocatedRegionWrapper.builder().regionList(Region.LONDON)
            .londonFrcList(RegionLondonFrc.LONDON)
            .walesFrcList(RegionWalesFrc.SWANSEA)
            .courtListWrapper(
                DefaultCourtListWrapper.builder()
                    .cfcCourtList(CfcCourt.CROYDON_COUNTY_COURT_AND_FAMILY_COURT)
                    .swanseaCourtList(SwanseaCourt.FR_swanseaList_1)
                    .build())
            .build();
        AllocatedRegionWrapper allocatedRegionWrapperBefore = AllocatedRegionWrapper.builder().regionList(Region.LONDON)
            .londonFrcList(RegionLondonFrc.LONDON)
            .walesFrcList(RegionWalesFrc.SWANSEA)
            .courtListWrapper(
                DefaultCourtListWrapper.builder()
                    .cfcCourtList(CfcCourt.BRENTFORD_COUNTY_AND_FAMILY_COURT)
                    .swanseaCourtList(SwanseaCourt.FR_swanseaList_1)
                    .build())
            .build();

        AllocatedRegionWrapper allocatedRegionWrapperReturn =
            courtDetailsMapper.getLatestAllocatedCourt(allocatedRegionWrapperBefore, allocatedRegionWrapper,
                null);

        assertThat(allocatedRegionWrapperReturn.getRegionList(),
            is(equalTo(Region.LONDON)));
        assertThat(allocatedRegionWrapperReturn.getLondonFrcList(),
            is(equalTo(RegionLondonFrc.LONDON)));
        assertThat(allocatedRegionWrapperReturn.getDefaultCourtListWrapper().getCfcCourtList(),
            is(equalTo(CfcCourt.CROYDON_COUNTY_COURT_AND_FAMILY_COURT)));
        assertThat(allocatedRegionWrapperReturn.getWalesFrcList(),
            is(nullValue()));
        assertThat(allocatedRegionWrapperReturn.getDefaultCourtListWrapper().getSwanseaCourtList(),
            is(nullValue()));
    }

    @Test
    void givenCaseDataWithNoPreviousCourtList_whenLondonCourt_thenCaseDataWithOnlyOneLatestCourt() {
        AllocatedRegionWrapper allocatedRegionWrapper = AllocatedRegionWrapper.builder().regionList(Region.LONDON)
            .londonFrcList(RegionLondonFrc.LONDON)
            .courtListWrapper(
                DefaultCourtListWrapper.builder()
                    .londonCourtList(LondonCourt.CENTRAL_FAMILY_COURT)
                    .build())
            .build();
        AllocatedRegionWrapper allocatedRegionWrapperBefore = new AllocatedRegionWrapper();

        AllocatedRegionWrapper allocatedRegionWrapperReturn =
            courtDetailsMapper.getLatestAllocatedCourt(allocatedRegionWrapperBefore, allocatedRegionWrapper,
                true);

        assertThat(allocatedRegionWrapperReturn.getRegionList(),
            is(equalTo(Region.LONDON)));
        assertThat(allocatedRegionWrapperReturn.getLondonFrcList(),
            is(equalTo(RegionLondonFrc.LONDON)));
        assertThat(allocatedRegionWrapperReturn.getDefaultCourtListWrapper().getLondonCourtList(),
            is(equalTo(LondonCourt.CENTRAL_FAMILY_COURT)));
        assertThat(allocatedRegionWrapperReturn.getWalesFrcList(),
            is(nullValue()));
        assertThat(allocatedRegionWrapperReturn.getDefaultCourtListWrapper().getSwanseaCourtList(),
            is(nullValue()));
    }
}
