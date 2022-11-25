package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BedfordshireCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BirminghamCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BristolCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ClevelandCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CourtList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DevonCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DorsetCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Region;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionLondonFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionMidlandsFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionNorthEastFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionNorthWestFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionSouthEastFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionSouthWestFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionWalesFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.SwanseaCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ThamesValleyCourt;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.fail;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BEDFORDSHIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BEDFORDSHIRE_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BIRMINGHAM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BIRMINGHAM_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BRISTOLFRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BRISTOL_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CLEAVELAND;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CLEAVELAND_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DEVON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DEVON_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DORSET;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DORSET_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HSYORKSHIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HSYORKSHIRE_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.KENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.KENTFRC_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LANCASHIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LANCASHIRE_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LIVERPOOL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LIVERPOOL_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MANCHESTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MANCHESTER_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIDLANDS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIDLANDS_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NEWPORT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NEWPORT_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHEAST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHEAST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHWALES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHWEST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHWEST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTH_WALES_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTTINGHAM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTTINGHAM_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NWYORKSHIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NWYORKSHIRE_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.REGION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHEAST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHEAST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHWEST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHWEST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SWANSEA;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SWANSEA_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.THAMESVALLEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.THAMESVALLEY_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WALES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WALES_FRC_LIST;

public class ConsentedCaseHearingFunctionsTest {

    public static final String BRISTOL_1 = "bristol_1";
    public static final String BIRMINGHAM_1 = "birmingham_1";
    public static final String THAMES_1 = "thames_1";
    public static final String LANCASHIRE_1 = "lancashire_1";
    public static final String HS_YORKSHIRE_1 = "hs_yorkshire_1";
    public static final String NORTHWALES_1 = "northwales_1";
    private Map<String, Object> caseData;
    private FinremCaseData finremCaseData;

    @ParameterizedTest
    @MethodSource("provideSouthWestParameters")
    public void shouldReturnCorrectSouthWestList(String frc, String courtList, String court) {
        caseData = new HashMap<>();
        caseData.put(REGION, SOUTHWEST);
        caseData.put(SOUTHWEST_FRC_LIST, frc);
        caseData.put(courtList, court);

        assertThat(ConsentedCaseHearingFunctions.getSelectedCourt(caseData), is(court));
    }

    private static Stream<Arguments> provideSouthWestParameters() {
        return Stream.of(
            Arguments.of(BRISTOLFRC, BRISTOL_COURTLIST, BRISTOL_1),
            Arguments.of(DEVON, DEVON_COURTLIST, "devon_1"),
            Arguments.of(DORSET, DORSET_COURTLIST, "doreset_1")
        );
    }

    @ParameterizedTest
    @MethodSource("provideMidlandsParameters")
    public void shouldReturnCorrectMidlandsList(String frc, String courtList, String court) {
        caseData = new HashMap<>();
        caseData.put(REGION, MIDLANDS);
        caseData.put(MIDLANDS_FRC_LIST, frc);
        caseData.put(courtList, court);

        assertThat(ConsentedCaseHearingFunctions.getSelectedCourt(caseData), is(court));
    }

    private static Stream<Arguments> provideMidlandsParameters() {
        return Stream.of(
            Arguments.of(BIRMINGHAM, BIRMINGHAM_COURTLIST, BIRMINGHAM_1),
            Arguments.of(NOTTINGHAM, NOTTINGHAM_COURTLIST, "nottingham_1")
        );
    }

    @ParameterizedTest
    @MethodSource("provideSoutheastParameters")
    public void shouldReturnCorrectSoutheastList(String frc, String courtList, String court) {
        caseData = new HashMap<>();
        caseData.put(REGION, SOUTHEAST);
        caseData.put(SOUTHEAST_FRC_LIST, frc);
        caseData.put(courtList, court);

        assertThat(ConsentedCaseHearingFunctions.getSelectedCourt(caseData), is(court));
    }

    private static Stream<Arguments> provideSoutheastParameters() {
        return Stream.of(
            Arguments.of(THAMESVALLEY, THAMESVALLEY_COURTLIST, THAMES_1),
            Arguments.of(BEDFORDSHIRE, BEDFORDSHIRE_COURTLIST, "bedfordshire_1"),
            Arguments.of(KENT, KENTFRC_COURTLIST, "kent_1")
        );
    }

    @ParameterizedTest
    @MethodSource("provideNorthwestParameters")
    public void shouldReturnCorrectNorthwestList(String frc, String courtList, String court) {
        caseData = new HashMap<>();
        caseData.put(REGION, NORTHWEST);
        caseData.put(NORTHWEST_FRC_LIST, frc);
        caseData.put(courtList, court);

        assertThat(ConsentedCaseHearingFunctions.getSelectedCourt(caseData), is(court));
    }

    private static Stream<Arguments> provideNorthwestParameters() {
        return Stream.of(
            Arguments.of(LANCASHIRE, LANCASHIRE_COURTLIST, LANCASHIRE_1),
            Arguments.of(MANCHESTER, MANCHESTER_COURTLIST, "manchester_1"),
            Arguments.of(LIVERPOOL, LIVERPOOL_COURTLIST, "liverpool_1")
        );
    }

    @ParameterizedTest
    @MethodSource("provideNortheastParameters")
    public void shouldReturnCorrectNortheastList(String frc, String courtList, String court) {
        caseData = new HashMap<>();
        caseData.put(REGION, NORTHEAST);
        caseData.put(NORTHEAST_FRC_LIST, frc);
        caseData.put(courtList, court);

        assertThat(ConsentedCaseHearingFunctions.getSelectedCourt(caseData), is(court));
    }

    private static Stream<Arguments> provideNortheastParameters() {
        return Stream.of(
            Arguments.of(HSYORKSHIRE, HSYORKSHIRE_COURTLIST, HS_YORKSHIRE_1),
            Arguments.of(NWYORKSHIRE, NWYORKSHIRE_COURTLIST, "nwYorkshire_1"),
            Arguments.of(CLEAVELAND, CLEAVELAND_COURTLIST, "cleveland_1")
        );
    }

    @ParameterizedTest
    @MethodSource("provideWalesParameters")
    public void shouldReturnCorrectWalesList(String frc, String courtList, String court) {
        caseData = new HashMap<>();
        caseData.put(REGION, WALES);
        caseData.put(WALES_FRC_LIST, frc);
        caseData.put(courtList, court);

        assertThat(ConsentedCaseHearingFunctions.getSelectedCourt(caseData), is(court));
    }

    private static Stream<Arguments> provideWalesParameters() {
        return Stream.of(
            Arguments.of(NORTHWALES, NORTH_WALES_COURTLIST, NORTHWALES_1),
            Arguments.of(SWANSEA, SWANSEA_COURTLIST, "swansea_1"),
            Arguments.of(NEWPORT, NEWPORT_COURTLIST, "newport_1")
        );
    }

    @ParameterizedTest
    @MethodSource("provideSouthWestParametersFinrem")
    public void shouldReturnCorrectSouthWestListFinrem(RegionSouthWestFrc frc,
                                                       CourtList court) {
        finremCaseData = new FinremCaseData();
        finremCaseData.getRegionWrapper().getDefaultRegionWrapper().setRegionList(Region.SOUTHWEST);
        finremCaseData.getRegionWrapper().getDefaultRegionWrapper().setSouthWestFrcList(frc);

        switch (frc) {
            case DEVON -> finremCaseData.getRegionWrapper().getDefaultRegionWrapper()
                .getDefaultCourtListWrapper().setDevonCourtList((DevonCourt) court);
            case DORSET -> finremCaseData.getRegionWrapper().getDefaultRegionWrapper()
                .getDefaultCourtListWrapper().setDorsetCourtList((DorsetCourt) court);
            case BRISTOL -> finremCaseData.getRegionWrapper().getDefaultRegionWrapper()
                .getDefaultCourtListWrapper().setBristolCourtList((BristolCourt) court);
            default -> fail();
        }


        assertThat(ConsentedCaseHearingFunctions.getSelectedCourt(finremCaseData), is(court));
    }

    private static Stream<Arguments> provideSouthWestParametersFinrem() {
        return Stream.of(
            Arguments.of(RegionSouthWestFrc.DEVON, DevonCourt.FR_devonList_1),
            Arguments.of(RegionSouthWestFrc.DORSET, DorsetCourt.FR_DORSET_LIST_1),
            Arguments.of(RegionSouthWestFrc.BRISTOL, BristolCourt.FR_bristolList_2)
        );
    }

    @ParameterizedTest
    @MethodSource("provideSouthEastParametersFinrem")
    public void shouldReturnCorrectSouthEastListFinrem(RegionSouthEastFrc frc,
                                                 CourtList court) {
        finremCaseData = new FinremCaseData();
        finremCaseData.getRegionWrapper().getDefaultRegionWrapper().setRegionList(Region.SOUTHEAST);
        finremCaseData.getRegionWrapper().getDefaultRegionWrapper().setSouthEastFrcList(frc);
        switch (frc) {
            case BEDFORDSHIRE -> finremCaseData.getRegionWrapper().getDefaultRegionWrapper()
                .getDefaultCourtListWrapper().setBedfordshireCourtList((BedfordshireCourt) court);
            case KENT -> finremCaseData.getRegionWrapper().getDefaultRegionWrapper()
                .getDefaultCourtListWrapper().setKentSurreyCourtList((KentSurreyCourt) court);
            case THAMES_VALLEY -> finremCaseData.getRegionWrapper().getDefaultRegionWrapper()
                .getDefaultCourtListWrapper().setThamesValleyCourtList((ThamesValleyCourt) court);
            default -> fail();
        }


        assertThat(ConsentedCaseHearingFunctions.getSelectedCourt(finremCaseData), is(court));
    }

    private static Stream<Arguments> provideSouthEastParametersFinrem() {
        return Stream.of(
            Arguments.of(RegionSouthEastFrc.BEDFORDSHIRE, BedfordshireCourt.FR_bedfordshireList_1),
            Arguments.of(RegionSouthEastFrc.KENT, KentSurreyCourt.CONSENTED_FR_kent_surreyList_1),
            Arguments.of(RegionSouthEastFrc.THAMES_VALLEY, ThamesValleyCourt.FR_thamesvalleyList_1)
        );
    }


    @ParameterizedTest
    @MethodSource("provideLondonParametersFinrem")
    public void shouldReturnCorrectLondonList(LondonCourt court) {
        finremCaseData = new FinremCaseData();
        finremCaseData.getRegionWrapper().getDefaultRegionWrapper().setRegionList(Region.LONDON);
        finremCaseData.getRegionWrapper().getDefaultRegionWrapper().setLondonFrcList(RegionLondonFrc.LONDON);
        finremCaseData.getRegionWrapper().getDefaultRegionWrapper().getDefaultCourtListWrapper().setLondonCourtList(court);

        assertThat(ConsentedCaseHearingFunctions.getSelectedCourt(finremCaseData), is(court));
    }

    private static Stream<Arguments> provideLondonParametersFinrem() {
        return Arrays.stream(LondonCourt.values()).map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("provideNorthWestParametersFinrem")
    public void shouldReturnCorrectNorthWestListFinrem(RegionNorthWestFrc frc,
                                                 CourtList court) {
        finremCaseData = new FinremCaseData();
        finremCaseData.getRegionWrapper().getDefaultRegionWrapper().setRegionList(Region.NORTHWEST);
        finremCaseData.getRegionWrapper().getDefaultRegionWrapper().setNorthWestFrcList(frc);
        switch (frc) {
            case LANCASHIRE -> finremCaseData.getRegionWrapper().getDefaultRegionWrapper()
                .getDefaultCourtListWrapper().setLancashireCourtList((LancashireCourt) court);
            case LIVERPOOL -> finremCaseData.getRegionWrapper().getDefaultRegionWrapper()
                .getDefaultCourtListWrapper().setLiverpoolCourtList((LiverpoolCourt) court);
            case MANCHESTER -> finremCaseData.getRegionWrapper().getDefaultRegionWrapper()
                .getDefaultCourtListWrapper().setManchesterCourtList((ManchesterCourt) court);
            default -> fail();
        }


        assertThat(ConsentedCaseHearingFunctions.getSelectedCourt(finremCaseData), is(court));
    }

    private static Stream<Arguments> provideNorthWestParametersFinrem() {
        return Stream.of(
            Arguments.of(RegionNorthWestFrc.LANCASHIRE, LancashireCourt.LANCASTER_COURT),
            Arguments.of(RegionNorthWestFrc.LIVERPOOL, LiverpoolCourt.CONSENTED_LIVERPOOL_CIVIL_FAMILY_COURT),
            Arguments.of(RegionNorthWestFrc.MANCHESTER, ManchesterCourt.CONSENTED_MANCHESTER_COURT)
        );
    }

    @ParameterizedTest
    @MethodSource("provideNorthEastParametersFinrem")
    public void shouldReturnCorrectNorthEastListFinrem(RegionNorthEastFrc frc,
                                                 CourtList court) {
        finremCaseData = new FinremCaseData();
        finremCaseData.getRegionWrapper().getDefaultRegionWrapper().setRegionList(Region.NORTHEAST);
        finremCaseData.getRegionWrapper().getDefaultRegionWrapper().setNorthEastFrcList(frc);

        switch (frc) {
            case CLEVELAND -> finremCaseData.getRegionWrapper().getDefaultRegionWrapper()
                .getDefaultCourtListWrapper().setClevelandCourtList((ClevelandCourt) court);
            case HS_YORKSHIRE -> finremCaseData.getRegionWrapper().getDefaultRegionWrapper()
                .getDefaultCourtListWrapper().setHumberCourtList((HumberCourt) court);
            case NW_YORKSHIRE -> finremCaseData.getRegionWrapper().getDefaultRegionWrapper()
                .getDefaultCourtListWrapper().setNwYorkshireCourtList((NwYorkshireCourt) court);
            default -> fail();
        }


        assertThat(ConsentedCaseHearingFunctions.getSelectedCourt(finremCaseData), is(court));
    }

    private static Stream<Arguments> provideNorthEastParametersFinrem() {
        return Stream.of(
            Arguments.of(RegionNorthEastFrc.CLEVELAND, ClevelandCourt.FR_CLEVELAND_LIST_1),
            Arguments.of(RegionNorthEastFrc.HS_YORKSHIRE, HumberCourt.CONSENTED_FR_humberList_1),
            Arguments.of(RegionNorthEastFrc.NW_YORKSHIRE, NwYorkshireCourt.CONSENTED_BRADFORD_COURT)
        );
    }

    @ParameterizedTest
    @MethodSource("provideWalesParametersFinrem")
    public void shouldReturnCorrectWalesListFinrem(RegionWalesFrc frc,
                                             CourtList court) {
        finremCaseData = new FinremCaseData();
        finremCaseData.getRegionWrapper().getDefaultRegionWrapper().setRegionList(Region.WALES);
        finremCaseData.getRegionWrapper().getDefaultRegionWrapper().setWalesFrcList(frc);

        switch (frc) {
            case NORTH_WALES -> finremCaseData.getRegionWrapper().getDefaultRegionWrapper()
                .getDefaultCourtListWrapper().setNorthWalesCourtList((NorthWalesCourt) court);
            case SWANSEA -> finremCaseData.getRegionWrapper().getDefaultRegionWrapper()
                .getDefaultCourtListWrapper().setSwanseaCourtList((SwanseaCourt) court);
            case NEWPORT -> finremCaseData.getRegionWrapper().getDefaultRegionWrapper()
                .getDefaultCourtListWrapper().setNewportCourtList((NewportCourt) court);
            default -> fail();
        }


        assertThat(CaseHearingFunctions.getSelectedCourt(finremCaseData), is(court));
    }

    private static Stream<Arguments> provideWalesParametersFinrem() {
        return Stream.of(
            Arguments.of(RegionWalesFrc.SWANSEA, SwanseaCourt.FR_swanseaList_1),
            Arguments.of(RegionWalesFrc.NORTH_WALES, NorthWalesCourt.FR_northwalesList_1),
            Arguments.of(RegionWalesFrc.NEWPORT, NewportCourt.FR_newportList_1)
        );
    }

    @ParameterizedTest
    @MethodSource("provideMidlandsParametersFinrem")
    public void shouldReturnCorrectMidlandsListFinrem(RegionMidlandsFrc frc,
                                                CourtList court) {
        finremCaseData = new FinremCaseData();
        finremCaseData.getRegionWrapper().getDefaultRegionWrapper().setRegionList(Region.MIDLANDS);
        finremCaseData.getRegionWrapper().getDefaultRegionWrapper().setMidlandsFrcList(frc);

        switch (frc) {
            case NOTTINGHAM -> finremCaseData.getRegionWrapper().getDefaultRegionWrapper()
                .getDefaultCourtListWrapper().setNottinghamCourtList((NottinghamCourt) court);
            case BIRMINGHAM -> finremCaseData.getRegionWrapper().getDefaultRegionWrapper()
                .getDefaultCourtListWrapper().setBirminghamCourtList((BirminghamCourt) court);
            default -> fail();
        }


        assertThat(CaseHearingFunctions.getSelectedCourt(finremCaseData), is(court));
    }

    private static Stream<Arguments> provideMidlandsParametersFinrem() {
        return Stream.of(
            Arguments.of(RegionMidlandsFrc.NOTTINGHAM, NottinghamCourt.CONSENTED_BOSTON_COUNTY_COURT_AND_FAMILY_COURT),
            Arguments.of(RegionMidlandsFrc.BIRMINGHAM, BirminghamCourt.CONSENTED_BIRMINGHAM_CIVIL_AND_FAMILY_JUSTICE_CENTRE)
        );
    }
}