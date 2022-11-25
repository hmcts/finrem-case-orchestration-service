package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BedfordshireCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BirminghamCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BristolCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CfcCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ClevelandCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CourtList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DevonCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DorsetCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HumberCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.KentSurreyCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.LancashireCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.LiverpoolCourt;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BOURNEMOUTH;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BRISTOLFRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BRISTOL_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CAMBRIDGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CFC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_ADDRESS_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_EMAIL_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_NAME_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_PHONE_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DEVON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DEVON_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DORSET;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DORSET_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_HEARING_REGION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_MIDLANDS_FRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_NOTTINGHAM_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_CFC_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_LONDON_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_REGION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.KENTFRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.KENTFRC_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.KENT_DARTFORD_COURTS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LANCASHIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LANCASHIRE_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LEYLAND;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIDLANDS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIDLANDS_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIDLANDS_FRC_LIST_CT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHWALES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHWEST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHWEST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTH_WALES_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTTINGHAM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTTINGHAM_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.PRESTATYN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.READING;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.REEDLEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.REGION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.REGION_CT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SALISBURY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHEAST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHEAST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHWEST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHWEST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.THAMESVALLEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.THAMESVALLEY_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.TORQUAY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WALES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WALES_FRC_LIST;

public class CaseHearingFunctionsTest {

    private FinremCaseData finremCaseData;

    @Test
    public void givenMidlandsNottinghamCourtDetailsInCaseData_whenGettingSelectedCourtList_thenNottinghamCourtListIsReturned() {
        Map<String, Object> caseData = ImmutableMap.of(
            REGION, MIDLANDS,
            MIDLANDS_FRC_LIST, NOTTINGHAM);

        String courtList = CaseHearingFunctions.getSelectedCourt(caseData);
        assertThat(courtList, is(NOTTINGHAM_COURTLIST));
    }

    @Test
    public void givenMidlandsNottinghamCourtDetailsInCaseData_whenGettingSelectedCourtListGa_thenNottinghamCourtListGaIsReturned() {
        Map<String, Object> caseData = ImmutableMap.of(
            GENERAL_APPLICATION_DIRECTIONS_HEARING_REGION, MIDLANDS,
            GENERAL_APPLICATION_DIRECTIONS_MIDLANDS_FRC, NOTTINGHAM);

        String courtList = CaseHearingFunctions.getSelectedCourtGA(caseData);
        assertThat(courtList, is(GENERAL_APPLICATION_DIRECTIONS_NOTTINGHAM_COURT));
    }

    @Test
    public void givenMidlandsNottinghamCourtDetailsInCaseData_whenGettingSelectedCourtListCt_thenNottinghamCourtListCtIsReturned() {
        Map<String, Object> caseData = ImmutableMap.of(
            REGION_CT, MIDLANDS,
            MIDLANDS_FRC_LIST_CT, NOTTINGHAM);

        String courtList = CaseHearingFunctions.getSelectedCourtComplexType(caseData);
        assertThat(courtList, is(NOTTINGHAM_COURTLIST));
    }

    @Test
    public void shouldPopulateReedleyCourtDetails() {
        Map<String, Object> caseData = ImmutableMap.of(
            REGION, NORTHWEST,
            NORTHWEST_FRC_LIST, LANCASHIRE,
            LANCASHIRE_COURTLIST, REEDLEY);

        Map<String, Object> stringObjectMap = CaseHearingFunctions.buildFrcCourtDetails(caseData);
        assertThat(stringObjectMap.get(COURT_DETAILS_NAME_KEY), is("Reedley Family Hearing Centre"));
        assertThat(stringObjectMap.get(COURT_DETAILS_ADDRESS_KEY), is("Blackburn Family Court, 64 Victoria Street, Blackburn, BB1 6DJ"));
        assertThat(stringObjectMap.get(COURT_DETAILS_PHONE_KEY), is("0300 303 0642"));
        assertThat(stringObjectMap.get(COURT_DETAILS_EMAIL_KEY), is("LancashireandCumbriaFRC@justice.gov.uk"));
    }

    @Test
    public void shouldPopulateLeylandCourtDetails() {
        Map<String, Object> caseData = ImmutableMap.of(
            REGION, NORTHWEST,
            NORTHWEST_FRC_LIST, LANCASHIRE,
            LANCASHIRE_COURTLIST, LEYLAND);

        Map<String, Object> stringObjectMap = CaseHearingFunctions.buildFrcCourtDetails(caseData);
        assertThat(stringObjectMap.get(COURT_DETAILS_NAME_KEY), is("Leyland Family Hearing Centre"));
        assertThat(stringObjectMap.get(COURT_DETAILS_ADDRESS_KEY), is("The Family Court, Sessions House, Lancaster Road, Preston, PR1 2PD"));
        assertThat(stringObjectMap.get(COURT_DETAILS_PHONE_KEY), is("0300 303 0642"));
        assertThat(stringObjectMap.get(COURT_DETAILS_EMAIL_KEY), is("LancashireandCumbriaFRC@justice.gov.uk"));
    }

    @Test
    public void shouldPopulateCambridgeCourtDetails() {
        Map<String, Object> caseData = ImmutableMap.of(
            REGION, SOUTHEAST,
            SOUTHEAST_FRC_LIST, BEDFORDSHIRE,
            BEDFORDSHIRE_COURTLIST, CAMBRIDGE);

        Map<String, Object> stringObjectMap = CaseHearingFunctions.buildFrcCourtDetails(caseData);
        assertThat(stringObjectMap.get(COURT_DETAILS_NAME_KEY), is("Cambridge County and Family Court"));
        assertThat(stringObjectMap.get(COURT_DETAILS_ADDRESS_KEY), is("197 East Road, Cambridge, CB1 1BA"));
        assertThat(stringObjectMap.get(COURT_DETAILS_PHONE_KEY), is("0344 892 4000"));
        assertThat(stringObjectMap.get(COURT_DETAILS_EMAIL_KEY), is("FRC.NES.BCH@justice.gov.uk"));
    }

    @Test
    public void shouldPopulateReadingCourtDetails() {
        Map<String, Object> caseData = ImmutableMap.of(
            REGION, SOUTHEAST,
            SOUTHEAST_FRC_LIST, THAMESVALLEY,
            THAMESVALLEY_COURTLIST, READING);

        Map<String, Object> stringObjectMap = CaseHearingFunctions.buildFrcCourtDetails(caseData);
        assertThat(stringObjectMap.get(COURT_DETAILS_NAME_KEY), is("Reading County Court and Family Court"));
        assertThat(stringObjectMap.get(COURT_DETAILS_ADDRESS_KEY), is("Hearing Centre, 160-163 Friar Street, Reading, RG1 1HE"));
        assertThat(stringObjectMap.get(COURT_DETAILS_PHONE_KEY), is("01865 264 225"));
        assertThat(stringObjectMap.get(COURT_DETAILS_EMAIL_KEY), is("FRCThamesValley@justice.gov.uk"));
    }

    @Test
    public void shouldPopulateKentCourtDetails() {
        Map<String, Object> caseData = ImmutableMap.of(
            REGION, SOUTHEAST,
            SOUTHEAST_FRC_LIST, KENTFRC,
            KENTFRC_COURTLIST, KENT_DARTFORD_COURTS);

        Map<String, Object> stringObjectMap = CaseHearingFunctions.buildFrcCourtDetails(caseData);
        assertThat(stringObjectMap.get(COURT_DETAILS_NAME_KEY), is("Dartford County Court And Family Court"));
        assertThat(stringObjectMap.get(COURT_DETAILS_ADDRESS_KEY), is("Home Gardens, Dartford, DA1 1DX"));
        assertThat(stringObjectMap.get(COURT_DETAILS_PHONE_KEY), is("01634 887900"));
        assertThat(stringObjectMap.get(COURT_DETAILS_EMAIL_KEY), is("FRCKSS@justice.gov.uk"));
    }

    @Test
    public void shouldPopulateBournemouthCourtDetails() {
        Map<String, Object> caseData = ImmutableMap.of(
            REGION, SOUTHWEST,
            SOUTHWEST_FRC_LIST, DORSET,
            DORSET_COURTLIST, BOURNEMOUTH);

        Map<String, Object> stringObjectMap = CaseHearingFunctions.buildFrcCourtDetails(caseData);
        assertThat(stringObjectMap.get(COURT_DETAILS_NAME_KEY), is("Bournemouth and Poole County Court and Family Court"));
        assertThat(stringObjectMap.get(COURT_DETAILS_ADDRESS_KEY), is("Courts of Justice, Deansleigh Road, Bournemouth, BH7 7DS"));
        assertThat(stringObjectMap.get(COURT_DETAILS_PHONE_KEY), is("01202 502 800"));
        assertThat(stringObjectMap.get(COURT_DETAILS_EMAIL_KEY), is("BournemouthFRC.bournemouth.countycourt@justice.gov.uk"));
    }

    @Test
    public void shouldPopulateTorquayCourtDetails() {
        Map<String, Object> caseData = ImmutableMap.of(
            REGION, SOUTHWEST,
            SOUTHWEST_FRC_LIST, DEVON,
            DEVON_COURTLIST, TORQUAY);

        Map<String, Object> stringObjectMap = CaseHearingFunctions.buildFrcCourtDetails(caseData);
        assertThat(stringObjectMap.get(COURT_DETAILS_NAME_KEY), is("Torquay and Newton Abbot County and Family Court"));
        assertThat(stringObjectMap.get(COURT_DETAILS_ADDRESS_KEY), is("The Willows, Nicholson Road, Torquay, TQ2 7AZ"));
        assertThat(stringObjectMap.get(COURT_DETAILS_PHONE_KEY), is("01752 677 400"));
        assertThat(stringObjectMap.get(COURT_DETAILS_EMAIL_KEY), is("FR.PlymouthHub@justice.gov.uk"));
    }

    @Test
    public void shouldPopulateSalisburyCourtDetails() {
        Map<String, Object> caseData = ImmutableMap.of(
            REGION, SOUTHWEST,
            SOUTHWEST_FRC_LIST, BRISTOLFRC,
            BRISTOL_COURTLIST, SALISBURY);

        Map<String, Object> stringObjectMap = CaseHearingFunctions.buildFrcCourtDetails(caseData);
        assertThat(stringObjectMap.get(COURT_DETAILS_NAME_KEY), is("Salisbury Law Courts"));
        assertThat(stringObjectMap.get(COURT_DETAILS_ADDRESS_KEY), is("Wilton Road, Salisbury, SP2 7EP"));
        assertThat(stringObjectMap.get(COURT_DETAILS_PHONE_KEY), is("0117 366 4880"));
        assertThat(stringObjectMap.get(COURT_DETAILS_EMAIL_KEY), is("BristolFRC.bristol.countycourt@justice.gov.uk"));
    }

    @Test
    public void shouldPopulatePrestatynCourtDetails() {
        Map<String, Object> caseData = ImmutableMap.of(
            REGION, WALES,
            WALES_FRC_LIST, NORTHWALES,
            NORTH_WALES_COURTLIST, PRESTATYN);

        Map<String, Object> stringObjectMap = CaseHearingFunctions.buildFrcCourtDetails(caseData);
        assertThat(stringObjectMap.get(COURT_DETAILS_NAME_KEY), is("Prestatyn Justice Centre"));
        assertThat(stringObjectMap.get(COURT_DETAILS_ADDRESS_KEY), is("Victoria Road, Prestatyn, LL19 7TE"));
        assertThat(stringObjectMap.get(COURT_DETAILS_PHONE_KEY), is("01745 851916"));
        assertThat(stringObjectMap.get(COURT_DETAILS_EMAIL_KEY), is("Family.prestatyn.countycourt@justice.gov.uk"));
    }

    @Test
    public void shouldPopulateConsentedCourtDetails() {

        Map<String, Object> stringObjectMap = CaseHearingFunctions.buildConsentedFrcCourtDetails();
        assertThat(stringObjectMap.get(COURT_DETAILS_NAME_KEY), is("Family Court at the Courts and Tribunal Service Centre"));
        assertThat(stringObjectMap.get(COURT_DETAILS_ADDRESS_KEY), is("PO Box 12746, Harlow, CM20 9QZ"));
        assertThat(stringObjectMap.get(COURT_DETAILS_PHONE_KEY), is("0300 303 0642"));
        assertThat(stringObjectMap.get(COURT_DETAILS_EMAIL_KEY), is("contactFinancialRemedy@justice.gov.uk"));
    }

    @Test
    public void shouldPopulateInterimHearingLondonFrcCourtDetails() {
        Map<String, Object> caseData = ImmutableMap.of(
            INTERIM_REGION, LONDON,
            INTERIM_LONDON_FRC_LIST, CFC,
            INTERIM_HEARING_CFC_COURT_LIST, "FR_s_CFCList_2");

        Map<String, Object> stringObjectMap = CaseHearingFunctions.buildInterimHearingFrcCourtDetails(caseData);
        assertThat(stringObjectMap.get(COURT_DETAILS_NAME_KEY), is("Croydon County Court And Family Court"));
        assertThat(stringObjectMap.get(COURT_DETAILS_ADDRESS_KEY), is("Croydon County Court, Altyre Road, Croydon, CR9 5AB"));
        assertThat(stringObjectMap.get(COURT_DETAILS_PHONE_KEY), is("0300 123 5577"));
        assertThat(stringObjectMap.get(COURT_DETAILS_EMAIL_KEY), is("family.croydon.countycourt@justice.gov.uk"));
    }

    @Test
    public void shouldReturnEmptyMap() {
        Map<String, Object> caseData = new HashMap<>();
        Map<String, Object> stringObjectMap = CaseHearingFunctions.buildInterimHearingFrcCourtDetails(caseData);
        assertThat("Returns empty map", stringObjectMap.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("provideLondonParameters")
    public void shouldReturnCorrectLondonList(CfcCourt court) {
        finremCaseData = new FinremCaseData();
        finremCaseData.getRegionWrapper().getDefaultRegionWrapper().setRegionList(Region.LONDON);
        finremCaseData.getRegionWrapper().getDefaultRegionWrapper().setLondonFrcList(RegionLondonFrc.LONDON);
        finremCaseData.getRegionWrapper().getDefaultRegionWrapper().getDefaultCourtListWrapper().setCfcCourtList(court);

        assertThat(CaseHearingFunctions.getSelectedCourt(finremCaseData), is(court));
    }

    private static Stream<Arguments> provideLondonParameters() {
        return Arrays.stream(CfcCourt.values()).map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("provideSouthEastParameters")
    public void shouldReturnCorrectSouthEastList(RegionSouthEastFrc frc,
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


        assertThat(CaseHearingFunctions.getSelectedCourt(finremCaseData), is(court));
    }

    private static Stream<Arguments> provideSouthEastParameters() {
        return Stream.of(
            Arguments.of(RegionSouthEastFrc.BEDFORDSHIRE, BedfordshireCourt.FR_bedfordshireList_1),
            Arguments.of(RegionSouthEastFrc.KENT, KentSurreyCourt.FR_kent_surreyList_1),
            Arguments.of(RegionSouthEastFrc.THAMES_VALLEY, ThamesValleyCourt.FR_thamesvalleyList_1)
        );
    }

    @ParameterizedTest
    @MethodSource("provideSouthWestParameters")
    public void shouldReturnCorrectSouthWestList(RegionSouthWestFrc frc,
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


        assertThat(CaseHearingFunctions.getSelectedCourt(finremCaseData), is(court));
    }

    private static Stream<Arguments> provideSouthWestParameters() {
        return Stream.of(
            Arguments.of(RegionSouthWestFrc.DEVON, DevonCourt.FR_devonList_1),
            Arguments.of(RegionSouthWestFrc.DORSET, DorsetCourt.FR_DORSET_LIST_1),
            Arguments.of(RegionSouthWestFrc.BRISTOL, BristolCourt.FR_bristolList_2)
        );
    }

    @ParameterizedTest
    @MethodSource("provideNorthWestParameters")
    public void shouldReturnCorrectNorthWestList(RegionNorthWestFrc frc,
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


        assertThat(CaseHearingFunctions.getSelectedCourt(finremCaseData), is(court));
    }

    private static Stream<Arguments> provideNorthWestParameters() {
        return Stream.of(
            Arguments.of(RegionNorthWestFrc.LANCASHIRE, LancashireCourt.LANCASTER_COURT),
            Arguments.of(RegionNorthWestFrc.LIVERPOOL, LiverpoolCourt.LIVERPOOL_CIVIL_FAMILY_COURT),
            Arguments.of(RegionNorthWestFrc.MANCHESTER, ManchesterCourt.MANCHESTER_COURT)
        );
    }

    @ParameterizedTest
    @MethodSource("provideNorthEastParameters")
    public void shouldReturnCorrectNorthEastList(RegionNorthEastFrc frc,
                                                 CourtList court) {
        finremCaseData = new FinremCaseData();
        finremCaseData.getRegionWrapper().getDefaultRegionWrapper().setRegionList(Region.NORTHEAST);
        finremCaseData.getRegionWrapper().getDefaultRegionWrapper().setNorthEastFrcList(frc);

        switch (frc) {
            case CLEVELAND -> finremCaseData.getRegionWrapper().getDefaultRegionWrapper()
                .getDefaultCourtListWrapper().setCleavelandCourtList((ClevelandCourt) court);
            case HS_YORKSHIRE -> finremCaseData.getRegionWrapper().getDefaultRegionWrapper()
                .getDefaultCourtListWrapper().setHumberCourtList((HumberCourt) court);
            case NW_YORKSHIRE -> finremCaseData.getRegionWrapper().getDefaultRegionWrapper()
                .getDefaultCourtListWrapper().setNwYorkshireCourtList((NwYorkshireCourt) court);
            default -> fail();
        }


        assertThat(CaseHearingFunctions.getSelectedCourt(finremCaseData), is(court));
    }

    private static Stream<Arguments> provideNorthEastParameters() {
        return Stream.of(
            Arguments.of(RegionNorthEastFrc.CLEVELAND, ClevelandCourt.FR_CLEVELAND_LIST_1),
            Arguments.of(RegionNorthEastFrc.HS_YORKSHIRE, HumberCourt.FR_humberList_1),
            Arguments.of(RegionNorthEastFrc.NW_YORKSHIRE, NwYorkshireCourt.BRADFORD_COURT)
        );
    }

    @ParameterizedTest
    @MethodSource("provideWalesParameters")
    public void shouldReturnCorrectWalesList(RegionWalesFrc frc,
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

    private static Stream<Arguments> provideWalesParameters() {
        return Stream.of(
            Arguments.of(RegionWalesFrc.SWANSEA, SwanseaCourt.FR_swanseaList_1),
            Arguments.of(RegionWalesFrc.NORTH_WALES, NorthWalesCourt.FR_northwalesList_1),
            Arguments.of(RegionWalesFrc.NEWPORT, NewportCourt.FR_newportList_1)
        );
    }

    @ParameterizedTest
    @MethodSource("provideMidlandsParameters")
    public void shouldReturnCorrectMidlandsList(RegionMidlandsFrc frc,
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

    private static Stream<Arguments> provideMidlandsParameters() {
        return Stream.of(
            Arguments.of(RegionMidlandsFrc.NOTTINGHAM, NottinghamCourt.BOSTON_COUNTY_COURT_AND_FAMILY_COURT),
            Arguments.of(RegionMidlandsFrc.BIRMINGHAM, BirminghamCourt.BIRMINGHAM_CIVIL_AND_FAMILY_JUSTICE_CENTRE)
        );
    }
}
