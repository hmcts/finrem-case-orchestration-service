package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BedfordshireCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BristolCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ClevelandCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DevonCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DorsetCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HighCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.KentSurreyCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.LancashireCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NorthWalesCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Region;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionHighCourtFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionNorthEastFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionNorthWestFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionSouthEastFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionSouthWestFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionWalesFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ThamesValleyCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DefaultCourtListWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DefaultRegionWrapper;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_DIVORCE_CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_JUDGE_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_RESP_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BEDFORDSHIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BEDFORDSHIRE_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BOURNEMOUTH;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BRISTOLFRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BRISTOL_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CAMBRIDGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CFC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CLEAVELAND_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CLEVELAND;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HIGHCOURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HIGHCOURT_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HIGHCOURT_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HIGHCOURT_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HIGHCOURT_FRC_LIST_CT;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHEAST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHEAST_FRC_LIST;
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
    public void givenHighCourtDetailsInCaseData_whenGettingSelectedCourtListCt_thenHighCourtListCtIsReturned() {
        Map<String, Object> caseData = ImmutableMap.of(
            REGION_CT, HIGHCOURT,
            HIGHCOURT_FRC_LIST_CT, HIGHCOURT);

        String courtList = CaseHearingFunctions.getSelectedCourtComplexType(caseData);
        assertThat(courtList, is(HIGHCOURT_COURTLIST));
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
    public void shouldPopulateReedleyCourtDetailsFinrem() {
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
        assertThat(stringObjectMap.get(COURT_DETAILS_ADDRESS_KEY), is("Lancastergate, Leyland, PR25 2EX"));
        assertThat(stringObjectMap.get(COURT_DETAILS_PHONE_KEY), is("0300 303 0642"));
        assertThat(stringObjectMap.get(COURT_DETAILS_EMAIL_KEY), is("LancashireandCumbriaFRC@justice.gov.uk"));
    }

    @Test
    public void shouldPopulateLeylandCourtDetailsFinrem() {

        DefaultRegionWrapper regionWrapper = DefaultRegionWrapper.builder().regionList(Region.NORTHWEST)
            .northWestFrcList(RegionNorthWestFrc.LANCASHIRE)
            .courtListWrapper(
                DefaultCourtListWrapper.builder().lancashireCourtList(LancashireCourt.LEYLAND_COURT)
                    .build())
            .build();
        FinremCaseData caseData = getFinremCaseData(regionWrapper);

        Map<String, Object> stringObjectMap = CaseHearingFunctions.buildFrcCourtDetails(caseData);
        assertThat(stringObjectMap.get(COURT_DETAILS_NAME_KEY), is("Leyland Family Hearing Centre"));
        assertThat(stringObjectMap.get(COURT_DETAILS_ADDRESS_KEY), is("Lancastergate, Leyland, PR25 2EX"));
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
    public void shouldPopulateCambridgeCourtDetailsFinrem() {

        DefaultRegionWrapper regionWrapper = DefaultRegionWrapper.builder().regionList(Region.SOUTHEAST)
            .southEastFrcList(RegionSouthEastFrc.BEDFORDSHIRE)
            .courtListWrapper(
                DefaultCourtListWrapper.builder().bedfordshireCourtList(BedfordshireCourt.CAMBRIDGE)
                    .build())
            .build();

        FinremCaseData caseData = getFinremCaseData(regionWrapper);
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
        assertThat(stringObjectMap.get(COURT_DETAILS_PHONE_KEY), is("0118 987 0500"));
        assertThat(stringObjectMap.get(COURT_DETAILS_EMAIL_KEY), is("family.reading.countycourt@justice.gov.uk"));
    }


    @Test
    public void shouldPopulateReadingCourtDetailsFinrem() {
        DefaultRegionWrapper regionWrapper = DefaultRegionWrapper.builder().regionList(Region.SOUTHEAST)
            .southEastFrcList(RegionSouthEastFrc.THAMES_VALLEY)
            .courtListWrapper(
                DefaultCourtListWrapper.builder().thamesValleyCourtList(ThamesValleyCourt.READING)
                    .build())
            .build();

        FinremCaseData caseData = getFinremCaseData(regionWrapper);

        Map<String, Object> stringObjectMap = CaseHearingFunctions.buildFrcCourtDetails(caseData);
        assertThat(stringObjectMap.get(COURT_DETAILS_NAME_KEY), is("Reading County Court and Family Court"));
        assertThat(stringObjectMap.get(COURT_DETAILS_ADDRESS_KEY), is("Hearing Centre, 160-163 Friar Street, Reading, RG1 1HE"));
        assertThat(stringObjectMap.get(COURT_DETAILS_PHONE_KEY), is("0118 987 0500"));
        assertThat(stringObjectMap.get(COURT_DETAILS_EMAIL_KEY), is("family.reading.countycourt@justice.gov.uk"));
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
        assertThat(stringObjectMap.get(COURT_DETAILS_PHONE_KEY), is("0300 123 5577"));
        assertThat(stringObjectMap.get(COURT_DETAILS_EMAIL_KEY), is("family.dartford.countycourt@justice.gov.uk"));
    }

    @Test
    public void shouldPopulateKentCourtDetailsFinrem() {

        DefaultRegionWrapper regionWrapper = DefaultRegionWrapper.builder().regionList(Region.SOUTHEAST)
            .southEastFrcList(RegionSouthEastFrc.KENT)
            .courtListWrapper(
                DefaultCourtListWrapper.builder().kentSurreyCourtList(KentSurreyCourt.KENT_DARTFORD_COURTS)
                    .build())
            .build();

        FinremCaseData caseData = getFinremCaseData(regionWrapper);

        Map<String, Object> stringObjectMap = CaseHearingFunctions.buildFrcCourtDetails(caseData);
        assertThat(stringObjectMap.get(COURT_DETAILS_NAME_KEY), is("Dartford County Court And Family Court"));
        assertThat(stringObjectMap.get(COURT_DETAILS_ADDRESS_KEY), is("Home Gardens, Dartford, DA1 1DX"));
        assertThat(stringObjectMap.get(COURT_DETAILS_PHONE_KEY), is("0300 123 5577"));
        assertThat(stringObjectMap.get(COURT_DETAILS_EMAIL_KEY), is("family.dartford.countycourt@justice.gov.uk"));
    }

    @Test
    public void shouldPopulateDurhamCourtDetails() {
        Map<String, Object> caseData = ImmutableMap.of(
            REGION, NORTHEAST,
            NORTHEAST_FRC_LIST, CLEVELAND,
            CLEAVELAND_COURTLIST, "FR_cleaveland_hc_list_2");

        Map<String, Object> stringObjectMap = CaseHearingFunctions.buildFrcCourtDetails(caseData);
        assertThat(stringObjectMap.get(COURT_DETAILS_NAME_KEY), is("Durham Justice Centre"));
        assertThat(stringObjectMap.get(COURT_DETAILS_ADDRESS_KEY), is("Green Lane, Old Elvet, Durham, DH1 3RG"));
        assertThat(stringObjectMap.get(COURT_DETAILS_PHONE_KEY), is("0191 2058750"));
        assertThat(stringObjectMap.get(COURT_DETAILS_EMAIL_KEY), is("Family.newcastle.countycourt@justice.gov.uk"));
    }

    @Test
    public void shouldPopulateDurhamCourtDetailsFinrem() {
        DefaultRegionWrapper regionWrapper = DefaultRegionWrapper.builder().regionList(Region.NORTHEAST)
            .northEastFrcList(RegionNorthEastFrc.CLEVELAND)
            .courtListWrapper(
                DefaultCourtListWrapper.builder().cleavelandCourtList(ClevelandCourt.FR_CLEVELAND_HC_LIST_2)
                    .build())
            .build();

        FinremCaseData caseData = getFinremCaseData(regionWrapper);

        Map<String, Object> stringObjectMap = CaseHearingFunctions.buildFrcCourtDetails(caseData);
        assertThat(stringObjectMap.get(COURT_DETAILS_NAME_KEY), is("Durham Justice Centre"));
        assertThat(stringObjectMap.get(COURT_DETAILS_ADDRESS_KEY), is("Green Lane, Old Elvet, Durham, DH1 3RG"));
        assertThat(stringObjectMap.get(COURT_DETAILS_PHONE_KEY), is("0191 2058750"));
        assertThat(stringObjectMap.get(COURT_DETAILS_EMAIL_KEY), is("Family.newcastle.countycourt@justice.gov.uk"));
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
    public void shouldPopulateBournemouthCourtDetailsFinrem() {

        DefaultRegionWrapper regionWrapper = DefaultRegionWrapper.builder().regionList(Region.SOUTHWEST)
            .southWestFrcList(RegionSouthWestFrc.DORSET)
            .courtListWrapper(
                DefaultCourtListWrapper.builder().dorsetCourtList(DorsetCourt.BOURNEMOUTH)
                    .build())
            .build();

        FinremCaseData caseData = getFinremCaseData(regionWrapper);

        Map<String, Object> stringObjectMap = CaseHearingFunctions.buildFrcCourtDetails(caseData);
        assertThat(stringObjectMap.get(COURT_DETAILS_NAME_KEY), is("Bournemouth and Poole County Court and Family Court"));
        assertThat(stringObjectMap.get(COURT_DETAILS_ADDRESS_KEY), is("Courts of Justice, Deansleigh Road, Bournemouth, BH7 7DS"));
        assertThat(stringObjectMap.get(COURT_DETAILS_PHONE_KEY), is("01202 502 800"));
        assertThat(stringObjectMap.get(COURT_DETAILS_EMAIL_KEY), is("BournemouthFRC.bournemouth.countycourt@justice.gov.uk"));
    }

    @Test
    public void shouldPopulateTorquayCourtDetailsFinrem() {
        DefaultRegionWrapper regionWrapper = DefaultRegionWrapper.builder().regionList(Region.SOUTHWEST)
            .southWestFrcList(RegionSouthWestFrc.DEVON)
            .courtListWrapper(
                DefaultCourtListWrapper.builder().devonCourtList(DevonCourt.TORQUAY)
                    .build())
            .build();

        FinremCaseData caseData = getFinremCaseData(regionWrapper);

        Map<String, Object> stringObjectMap = CaseHearingFunctions.buildFrcCourtDetails(caseData);
        assertThat(stringObjectMap.get(COURT_DETAILS_NAME_KEY), is("Torquay and Newton Abbot County and Family Court"));
        assertThat(stringObjectMap.get(COURT_DETAILS_ADDRESS_KEY), is("The Willows, Nicholson Road, Torquay, TQ2 7AZ"));
        assertThat(stringObjectMap.get(COURT_DETAILS_PHONE_KEY), is("01752 677 400"));
        assertThat(stringObjectMap.get(COURT_DETAILS_EMAIL_KEY), is("FR.PlymouthHub@justice.gov.uk"));
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
    public void shouldPopulateSalisburyCourtDetailsFinrem() {
        DefaultRegionWrapper regionWrapper = DefaultRegionWrapper.builder().regionList(Region.SOUTHWEST)
            .southWestFrcList(RegionSouthWestFrc.BRISTOL)
            .courtListWrapper(
                DefaultCourtListWrapper.builder().bristolCourtList(BristolCourt.SALISBURY_LAW_COURTS)
                    .build())
            .build();

        FinremCaseData caseData = getFinremCaseData(regionWrapper);

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
    public void shouldPopulatePrestatynCourtDetailsFinrem() {
        DefaultRegionWrapper regionWrapper = DefaultRegionWrapper.builder().regionList(Region.WALES)
            .walesFrcList(RegionWalesFrc.NORTH_WALES)
            .courtListWrapper(
                DefaultCourtListWrapper.builder().northWalesCourtList(NorthWalesCourt.PRESTATYN)
                    .build())
            .build();

        FinremCaseData caseData = getFinremCaseData(regionWrapper);

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
    public void shouldPopulateHighCourtDetails() {
        Map<String, Object> caseData = ImmutableMap.of(
            REGION, HIGHCOURT,
            HIGHCOURT_FRC_LIST, HIGHCOURT,
            HIGHCOURT_COURTLIST, HIGHCOURT_COURT);

        Map<String, Object> stringObjectMap = CaseHearingFunctions.buildFrcCourtDetails(caseData);
        assertThat(stringObjectMap.get(COURT_DETAILS_NAME_KEY), is("High Court Family Division"));
        assertThat(stringObjectMap.get(COURT_DETAILS_ADDRESS_KEY),
            is("High Court Family Division, Queens Building, Royal Courts of Justice, Strand, London, WC2A 2LL"));
        assertThat(stringObjectMap.get(COURT_DETAILS_PHONE_KEY), is("020 7947 7551"));
        assertThat(stringObjectMap.get(COURT_DETAILS_EMAIL_KEY), is("rcj.familyhighcourt@justice.gov.uk"));
    }


    @Test
    public void shouldPopulateHighCourtDetailsFinrem() {

        DefaultRegionWrapper regionWrapper = DefaultRegionWrapper.builder().regionList(Region.HIGHCOURT)
            .highCourtFrcList(RegionHighCourtFrc.HIGHCOURT)
            .courtListWrapper(
                DefaultCourtListWrapper.builder().highCourtList(HighCourt.HIGHCOURT_COURT)
                    .build())
            .build();

        FinremCaseData caseData = getFinremCaseData(regionWrapper);

        Map<String, Object> stringObjectMap = CaseHearingFunctions.buildFrcCourtDetails(caseData);
        assertThat(stringObjectMap.get(COURT_DETAILS_NAME_KEY), is("High Court Family Division"));
        assertThat(stringObjectMap.get(COURT_DETAILS_ADDRESS_KEY),
            is("High Court Family Division, Queens Building, Royal Courts of Justice, Strand, London, WC2A 2LL"));
        assertThat(stringObjectMap.get(COURT_DETAILS_PHONE_KEY), is("020 7947 7551"));
        assertThat(stringObjectMap.get(COURT_DETAILS_EMAIL_KEY), is("rcj.familyhighcourt@justice.gov.uk"));
    }

    @Test
    public void shouldReturnNullIfHighCourtFRCIsWrongDetails() {
        Map<String, Object> caseData = ImmutableMap.of(
            REGION, HIGHCOURT,
            HIGHCOURT_FRC_LIST, LONDON,
            HIGHCOURT_COURTLIST, HIGHCOURT_COURT);

        Map<String, Object> stringObjectMap = CaseHearingFunctions.buildFrcCourtDetails(caseData);
        assertTrue(stringObjectMap.isEmpty());
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

    private FinremCaseData getFinremCaseData(DefaultRegionWrapper regionWrapper) {
        FinremCaseData caseData = new FinremCaseData();
        caseData.getContactDetailsWrapper().setApplicantSolicitorEmail(TEST_SOLICITOR_EMAIL);
        caseData.getContactDetailsWrapper().setApplicantSolicitorName(TEST_SOLICITOR_NAME);
        caseData.getContactDetailsWrapper().setRespondentSolicitorEmail(TEST_RESP_SOLICITOR_EMAIL);
        caseData.getContactDetailsWrapper().setRespondentSolicitorName(TEST_RESP_SOLICITOR_NAME);
        caseData.getContactDetailsWrapper().setRespondentSolicitorReference(TEST_RESP_SOLICITOR_REFERENCE);
        caseData.getContactDetailsWrapper().setSolicitorReference(TEST_SOLICITOR_REFERENCE);
        caseData.setDivorceCaseNumber(TEST_DIVORCE_CASE_NUMBER);
        caseData.getGeneralApplicationWrapper().setGeneralApplicationReferToJudgeEmail(TEST_JUDGE_EMAIL);
        caseData.getRegionWrapper().setDefaultRegionWrapper(regionWrapper);
        caseData.setBulkPrintLetterIdRes(NOTTINGHAM);
        return caseData;
    }
}
