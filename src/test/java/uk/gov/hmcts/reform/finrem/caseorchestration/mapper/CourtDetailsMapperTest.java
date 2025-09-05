package uk.gov.hmcts.reform.finrem.caseorchestration.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CourtDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CourtDetailsConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.MissingCourtException;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BirminghamCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CfcCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Court;
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

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourtDetailsMapperTest {

    @Mock
    private CourtDetailsConfiguration courtDetailsConfiguration;

    private CourtDetailsMapper courtDetailsMapper;

    @BeforeEach
    void setUp() {
        courtDetailsMapper = new CourtDetailsMapper(new ObjectMapper(), courtDetailsConfiguration);
    }

    @Test
    void givenDefaultCourtListAndValidFieldDeclarations_whenGetCourtDetails_thenReturnExpectedCourtDetails() {
        DefaultCourtListWrapper courtList = new DefaultCourtListWrapper();
        courtList.setCfcCourtList(CfcCourt.BROMLEY_COUNTY_COURT_AND_FAMILY_COURT);

        CourtDetailsTemplateFields courtDetails = courtDetailsMapper.getCourtDetails(courtList);

        assertEquals("Bromley County Court And Family Court", courtDetails.getCourtName());
        assertEquals("Bromley County Court, College Road, Bromley, BR1 3PX", courtDetails.getCourtAddress());
        assertEquals("FRCLondon@justice.gov.uk", courtDetails.getEmail());
        assertEquals("0300 123 5577", courtDetails.getPhoneNumber());
    }

    @Test
    void givenGeneralApplicationCourtListAndValidFieldDeclarations_whenGetCourtDetails_thenReturnExpectedCourtDetails() {
        GeneralApplicationCourtListWrapper courtList = new GeneralApplicationCourtListWrapper();
        courtList.setGeneralApplicationDirectionsCfcCourtList(CfcCourt.CROYDON_COUNTY_COURT_AND_FAMILY_COURT);

        CourtDetailsTemplateFields courtDetails = courtDetailsMapper.getCourtDetails(courtList);

        assertEquals("Croydon County Court And Family Court", courtDetails.getCourtName());
        assertEquals("Croydon County Court, Altyre Road, Croydon, CR9 5AB", courtDetails.getCourtAddress());
        assertEquals("FRCLondon@justice.gov.uk", courtDetails.getEmail());
        assertEquals("0300 123 5577", courtDetails.getPhoneNumber());
    }

    @Test
    void givenInterimCourtListAndValidFieldDeclarations_whenGetCourtDetails_thenReturnExpectedCourtDetails() {
        InterimCourtListWrapper courtList = new InterimCourtListWrapper();
        courtList.setInterimCfcCourtList(CfcCourt.BARNET_CIVIL_AND_FAMILY_COURTS_CENTRE);

        CourtDetailsTemplateFields courtDetails = courtDetailsMapper.getCourtDetails(courtList);

        assertEquals("Barnet Civil And Family Courts Centre", courtDetails.getCourtName());
        assertEquals("Barnet County Court, St Marys Court, Regents Park Road, Finchley Central, London, N3 1BQ", courtDetails.getCourtAddress());
        assertEquals("FRCLondon@justice.gov.uk", courtDetails.getEmail());
        assertEquals("0300 123 5577", courtDetails.getPhoneNumber());
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

        assertEquals(Region.MIDLANDS, allocatedRegionWrapperReturn.getRegionList());
        assertEquals(RegionMidlandsFrc.BIRMINGHAM, allocatedRegionWrapperReturn.getMidlandsFrcList());
        assertEquals(BirminghamCourt.WORCESTER_COMBINED_COURT, allocatedRegionWrapperReturn.getDefaultCourtListWrapper().getBirminghamCourtList());
        assertNull(allocatedRegionWrapperReturn.getLondonFrcList());
        assertNull(allocatedRegionWrapperReturn.getDefaultCourtListWrapper().getCfcCourtList());
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

        assertEquals(Region.LONDON, allocatedRegionWrapperReturn.getRegionList());
        assertEquals(RegionLondonFrc.LONDON, allocatedRegionWrapperReturn.getLondonFrcList());
        assertEquals(CfcCourt.BRENTFORD_COUNTY_AND_FAMILY_COURT, allocatedRegionWrapperReturn.getDefaultCourtListWrapper().getCfcCourtList());
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

        assertEquals(Region.MIDLANDS, allocatedRegionWrapperReturn.getRegionList());
        assertEquals(RegionMidlandsFrc.BIRMINGHAM, allocatedRegionWrapperReturn.getMidlandsFrcList());
        assertEquals(BirminghamCourt.WORCESTER_COMBINED_COURT, allocatedRegionWrapperReturn.getDefaultCourtListWrapper().getBirminghamCourtList());
        assertNull(allocatedRegionWrapperReturn.getLondonFrcList());
        assertNull(allocatedRegionWrapperReturn.getWalesFrcList());
        assertNull(allocatedRegionWrapperReturn.getDefaultCourtListWrapper().getCfcCourtList());
        assertNull(allocatedRegionWrapperReturn.getDefaultCourtListWrapper().getSwanseaCourtList());
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

        assertEquals(Region.LONDON, allocatedRegionWrapperReturn.getRegionList());
        assertEquals(RegionLondonFrc.LONDON, allocatedRegionWrapperReturn.getLondonFrcList());
        assertEquals(CfcCourt.CROYDON_COUNTY_COURT_AND_FAMILY_COURT, allocatedRegionWrapperReturn.getDefaultCourtListWrapper().getCfcCourtList());
        assertNull(allocatedRegionWrapperReturn.getWalesFrcList());
        assertNull(allocatedRegionWrapperReturn.getDefaultCourtListWrapper().getSwanseaCourtList());
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

        assertEquals(Region.LONDON, allocatedRegionWrapperReturn.getRegionList());
        assertEquals(RegionLondonFrc.LONDON, allocatedRegionWrapperReturn.getLondonFrcList());
        assertEquals(LondonCourt.CENTRAL_FAMILY_COURT, allocatedRegionWrapperReturn.getDefaultCourtListWrapper().getLondonCourtList());
        assertNull(allocatedRegionWrapperReturn.getWalesFrcList());
        assertNull(allocatedRegionWrapperReturn.getDefaultCourtListWrapper().getSwanseaCourtList());
    }

    @Test
    void givenValidCourtField_whenConvertToFrcCourtDetails_thenReturnExpectedCourtDetailsTemplateFields() {
        // Mocking the court details map
        when(courtDetailsConfiguration.getCourts()).thenReturn(Map.of(
            "FR_s_CFCList_2", new CourtDetails("Croydon County Court And Family Court",
                "Croydon County Court, Altyre Road, Croydon, CR9 5AB", "0300 123 5577",
                "FRCLondon@justice.gov.uk", "42b18e70-18e8-4290-bb85-9c5254548345")
        ));

        // Setting up the test data
        DefaultCourtListWrapper courtList = new DefaultCourtListWrapper();
        courtList.setCfcCourtList(CfcCourt.CROYDON_COUNTY_COURT_AND_FAMILY_COURT);

        Court court = Court
            .builder()
            .londonList(RegionLondonFrc.LONDON)
            .region(Region.LONDON)
            .courtListWrapper(DefaultCourtListWrapper
                .builder()
                .cfcCourtList(CfcCourt.CROYDON_COUNTY_COURT_AND_FAMILY_COURT)
                .build())
            .build();

        // Invoking the method
        CourtDetails courtDetails = courtDetailsMapper.convertToFrcCourtDetails(court);

        // Assertions
        assertEquals("Croydon County Court And Family Court", courtDetails.getCourtName());
        assertEquals("Croydon County Court, Altyre Road, Croydon, CR9 5AB", courtDetails.getCourtAddress());
        assertEquals("FRCLondon@justice.gov.uk", courtDetails.getEmail());
        assertEquals("0300 123 5577", courtDetails.getPhoneNumber());
        assertEquals("42b18e70-18e8-4290-bb85-9c5254548345", courtDetails.getEmailReplyToId());
    }

    @Test
    void givenNoValidFieldInCourtListWrapper_whenConvertToFrcCourtDetails_thenThrowIllegalStateException() {
        // Mocking a Court object with an empty DefaultCourtListWrapper
        Court court = Court.builder()
            .courtListWrapper(DefaultCourtListWrapper.builder().build())
            .build();

        // Expecting an IllegalStateException
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> courtDetailsMapper.convertToFrcCourtDetails(court));

        // Verifying the exception message
        assertEquals("No valid field found in the court list wrapper", exception.getMessage());
    }
}
