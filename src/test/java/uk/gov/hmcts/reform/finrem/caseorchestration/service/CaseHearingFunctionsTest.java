package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.api.Test;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.AllocatedRegionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DefaultCourtListWrapper;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.KENT;
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

class CaseHearingFunctionsTest {

    @Test
    void givenMidlandsNottinghamCourtDetailsInCaseData_whenGettingSelectedCourtList_thenNottinghamCourtListIsReturned() {
        Map<String, Object> caseData = Map.of(
            REGION, MIDLANDS,
            MIDLANDS_FRC_LIST, NOTTINGHAM);
        String courtList = CaseHearingFunctions.getSelectedCourt(caseData);
        assertEquals(NOTTINGHAM_COURTLIST, courtList);
    }

    @Test
    void givenMidlandsNottinghamCourtDetailsInCaseData_whenGettingSelectedCourtListGa_thenNottinghamCourtListGaIsReturned() {
        Map<String, Object> caseData = Map.of(
            GENERAL_APPLICATION_DIRECTIONS_HEARING_REGION, MIDLANDS,
            GENERAL_APPLICATION_DIRECTIONS_MIDLANDS_FRC, NOTTINGHAM);
        String courtList = CaseHearingFunctions.getSelectedCourtGA(caseData);
        assertEquals(GENERAL_APPLICATION_DIRECTIONS_NOTTINGHAM_COURT, courtList);
    }

    @Test
    void givenMidlandsNottinghamCourtDetailsInCaseData_whenGettingSelectedCourtListCt_thenNottinghamCourtListCtIsReturned() {
        Map<String, Object> caseData = Map.of(
            REGION_CT, MIDLANDS,
            MIDLANDS_FRC_LIST_CT, NOTTINGHAM);
        String courtList = CaseHearingFunctions.getSelectedCourtComplexType(caseData);
        assertEquals(NOTTINGHAM_COURTLIST, courtList);
    }

    @Test
    void givenHighCourtDetailsInCaseData_whenGettingSelectedCourtListCt_thenHighCourtListCtIsReturned() {
        Map<String, Object> caseData = Map.of(
            REGION_CT, HIGHCOURT,
            HIGHCOURT_FRC_LIST_CT, HIGHCOURT);
        String courtList = CaseHearingFunctions.getSelectedCourtComplexType(caseData);
        assertEquals(HIGHCOURT_COURTLIST, courtList);
    }

    @Test
    void shouldPopulateReedleyCourtDetails() {
        Map<String, Object> caseData = Map.of(
            REGION, NORTHWEST,
            NORTHWEST_FRC_LIST, LANCASHIRE,
            LANCASHIRE_COURTLIST, REEDLEY);
        Map<String, Object> stringObjectMap = CaseHearingFunctions.buildFrcCourtDetails(caseData);
        assertEquals("Reedley Family Hearing Centre", stringObjectMap.get(COURT_DETAILS_NAME_KEY));
        assertEquals("The Court House, Colne Road (Junction with Swaledale Avenue), Reedley, Burnely, BB10 2LJ",
            stringObjectMap.get(COURT_DETAILS_ADDRESS_KEY));
        assertEquals("0300 123 5577", stringObjectMap.get(COURT_DETAILS_PHONE_KEY));
        assertEquals("LancashireandCumbriaFRC@justice.gov.uk", stringObjectMap.get(COURT_DETAILS_EMAIL_KEY));
    }

    @Test
    void shouldPopulateReedleyCourtDetailsFinrem() {
        Map<String, Object> caseData = Map.of(
            REGION, NORTHWEST,
            NORTHWEST_FRC_LIST, LANCASHIRE,
            LANCASHIRE_COURTLIST, REEDLEY);
        Map<String, Object> stringObjectMap = CaseHearingFunctions.buildFrcCourtDetails(caseData);
        assertEquals("Reedley Family Hearing Centre", stringObjectMap.get(COURT_DETAILS_NAME_KEY));
        assertEquals("The Court House, Colne Road (Junction with Swaledale Avenue), Reedley, Burnely, BB10 2LJ",
            stringObjectMap.get(COURT_DETAILS_ADDRESS_KEY));
        assertEquals("0300 123 5577", stringObjectMap.get(COURT_DETAILS_PHONE_KEY));
        assertEquals("LancashireandCumbriaFRC@justice.gov.uk", stringObjectMap.get(COURT_DETAILS_EMAIL_KEY));
    }

    @Test
    void shouldPopulateLeylandCourtDetails() {
        Map<String, Object> caseData = Map.of(
            REGION, NORTHWEST,
            NORTHWEST_FRC_LIST, LANCASHIRE,
            LANCASHIRE_COURTLIST, LEYLAND);
        Map<String, Object> stringObjectMap = CaseHearingFunctions.buildFrcCourtDetails(caseData);
        assertEquals("Leyland Family Hearing Centre", stringObjectMap.get(COURT_DETAILS_NAME_KEY));
        assertEquals("The Family Court, Lancastergate, Leyland, PR25 2EX", stringObjectMap.get(COURT_DETAILS_ADDRESS_KEY));
        assertEquals("0300 123 5577", stringObjectMap.get(COURT_DETAILS_PHONE_KEY));
        assertEquals("LancashireandCumbriaFRC@justice.gov.uk", stringObjectMap.get(COURT_DETAILS_EMAIL_KEY));
    }

    @Test
    void shouldPopulateLeylandCourtDetailsFinrem() {
        AllocatedRegionWrapper regionWrapper = AllocatedRegionWrapper.builder().regionList(Region.NORTHWEST)
            .northWestFrcList(RegionNorthWestFrc.LANCASHIRE)
            .courtListWrapper(
                DefaultCourtListWrapper.builder().lancashireCourtList(LancashireCourt.LEYLAND_COURT)
                    .build())
            .build();
        FinremCaseData caseData = getFinremCaseData(regionWrapper);
        Map<String, Object> stringObjectMap = CaseHearingFunctions.buildFrcCourtDetails(caseData);
        assertEquals("Leyland Family Hearing Centre", stringObjectMap.get(COURT_DETAILS_NAME_KEY));
        assertEquals("The Family Court, Lancastergate, Leyland, PR25 2EX", stringObjectMap.get(COURT_DETAILS_ADDRESS_KEY));
        assertEquals("0300 123 5577", stringObjectMap.get(COURT_DETAILS_PHONE_KEY));
        assertEquals("LancashireandCumbriaFRC@justice.gov.uk", stringObjectMap.get(COURT_DETAILS_EMAIL_KEY));
    }

    @Test
    void shouldPopulateCambridgeCourtDetails() {
        Map<String, Object> caseData = Map.of(
            REGION, SOUTHEAST,
            SOUTHEAST_FRC_LIST, BEDFORDSHIRE,
            BEDFORDSHIRE_COURTLIST, CAMBRIDGE);
        Map<String, Object> stringObjectMap = CaseHearingFunctions.buildFrcCourtDetails(caseData);
        assertEquals("Cambridge County and Family Court", stringObjectMap.get(COURT_DETAILS_NAME_KEY));
        assertEquals("197 East Road, Cambridge, CB1 1BA", stringObjectMap.get(COURT_DETAILS_ADDRESS_KEY));
        assertEquals("0300 123 5577", stringObjectMap.get(COURT_DETAILS_PHONE_KEY));
        assertEquals("FRC.NES.BCH@justice.gov.uk", stringObjectMap.get(COURT_DETAILS_EMAIL_KEY));
    }

    @Test
    void shouldPopulateCambridgeCourtDetailsFinrem() {
        AllocatedRegionWrapper regionWrapper = AllocatedRegionWrapper.builder().regionList(Region.SOUTHEAST)
            .southEastFrcList(RegionSouthEastFrc.BEDFORDSHIRE)
            .courtListWrapper(
                DefaultCourtListWrapper.builder().bedfordshireCourtList(BedfordshireCourt.CAMBRIDGE)
                    .build())
            .build();
        FinremCaseData caseData = getFinremCaseData(regionWrapper);
        Map<String, Object> stringObjectMap = CaseHearingFunctions.buildFrcCourtDetails(caseData);
        assertEquals("Cambridge County and Family Court", stringObjectMap.get(COURT_DETAILS_NAME_KEY));
        assertEquals("197 East Road, Cambridge, CB1 1BA", stringObjectMap.get(COURT_DETAILS_ADDRESS_KEY));
        assertEquals("0300 123 5577", stringObjectMap.get(COURT_DETAILS_PHONE_KEY));
        assertEquals("FRC.NES.BCH@justice.gov.uk", stringObjectMap.get(COURT_DETAILS_EMAIL_KEY));
    }

    @Test
    void shouldPopulateReadingCourtDetails() {
        Map<String, Object> caseData = Map.of(
            REGION, SOUTHEAST,
            SOUTHEAST_FRC_LIST, THAMESVALLEY,
            THAMESVALLEY_COURTLIST, READING);
        Map<String, Object> stringObjectMap = CaseHearingFunctions.buildFrcCourtDetails(caseData);
        assertEquals("Reading County Court and Family Court", stringObjectMap.get(COURT_DETAILS_NAME_KEY));
        assertEquals("Hearing Centre, 160-163 Friar Street, Reading, RG1 1HE", stringObjectMap.get(COURT_DETAILS_ADDRESS_KEY));
        assertEquals("0300 123 5577", stringObjectMap.get(COURT_DETAILS_PHONE_KEY));
        assertEquals("family.reading.countycourt@justice.gov.uk", stringObjectMap.get(COURT_DETAILS_EMAIL_KEY));
    }

    @Test
    void shouldPopulateReadingCourtDetailsFinrem() {
        AllocatedRegionWrapper regionWrapper = AllocatedRegionWrapper.builder().regionList(Region.SOUTHEAST)
            .southEastFrcList(RegionSouthEastFrc.THAMES_VALLEY)
            .courtListWrapper(
                DefaultCourtListWrapper.builder().thamesValleyCourtList(ThamesValleyCourt.READING)
                    .build())
            .build();
        FinremCaseData caseData = getFinremCaseData(regionWrapper);
        Map<String, Object> stringObjectMap = CaseHearingFunctions.buildFrcCourtDetails(caseData);
        assertEquals("Reading County Court and Family Court", stringObjectMap.get(COURT_DETAILS_NAME_KEY));
        assertEquals("Hearing Centre, 160-163 Friar Street, Reading, RG1 1HE", stringObjectMap.get(COURT_DETAILS_ADDRESS_KEY));
        assertEquals("0300 123 5577", stringObjectMap.get(COURT_DETAILS_PHONE_KEY));
        assertEquals("family.reading.countycourt@justice.gov.uk", stringObjectMap.get(COURT_DETAILS_EMAIL_KEY));
    }

    @Test
    void shouldPopulateKentCourtDetails() {
        Map<String, Object> caseData = Map.of(
            REGION, SOUTHEAST,
            SOUTHEAST_FRC_LIST, KENT,
            KENTFRC_COURTLIST, KENT_DARTFORD_COURTS);
        Map<String, Object> stringObjectMap = CaseHearingFunctions.buildFrcCourtDetails(caseData);
        assertEquals("Dartford County Court And Family Court", stringObjectMap.get(COURT_DETAILS_NAME_KEY));
        assertEquals("Home Gardens, Dartford, DA1 1DX", stringObjectMap.get(COURT_DETAILS_ADDRESS_KEY));
        assertEquals("0300 123 5577", stringObjectMap.get(COURT_DETAILS_PHONE_KEY));
        assertEquals("family.dartford.countycourt@justice.gov.uk", stringObjectMap.get(COURT_DETAILS_EMAIL_KEY));
    }

    @Test
    void shouldPopulateKentCourtDetailsFinrem() {
        AllocatedRegionWrapper regionWrapper = AllocatedRegionWrapper.builder().regionList(Region.SOUTHEAST)
            .southEastFrcList(RegionSouthEastFrc.KENT_FRC)
            .courtListWrapper(
                DefaultCourtListWrapper.builder().kentSurreyCourtList(KentSurreyCourt.FR_kent_surreyList_3)
                    .build())
            .build();
        FinremCaseData caseData = getFinremCaseData(regionWrapper);
        Map<String, Object> stringObjectMap = CaseHearingFunctions.buildFrcCourtDetails(caseData);
        assertEquals("Dartford County Court And Family Court", stringObjectMap.get(COURT_DETAILS_NAME_KEY));
        assertEquals("Home Gardens, Dartford, DA1 1DX", stringObjectMap.get(COURT_DETAILS_ADDRESS_KEY));
        assertEquals("0300 123 5577", stringObjectMap.get(COURT_DETAILS_PHONE_KEY));
        assertEquals("family.dartford.countycourt@justice.gov.uk", stringObjectMap.get(COURT_DETAILS_EMAIL_KEY));
    }

    @Test
    void shouldPopulateKentThanetCourtDetailsFinrem() {
        AllocatedRegionWrapper regionWrapper = AllocatedRegionWrapper.builder().regionList(Region.SOUTHEAST)
            .southEastFrcList(RegionSouthEastFrc.KENT_FRC)
            .courtListWrapper(
                DefaultCourtListWrapper.builder().kentSurreyCourtList(KentSurreyCourt.FR_kent_surreyList_11)
                    .build())
            .build();
        FinremCaseData caseData = getFinremCaseData(regionWrapper);
        Map<String, Object> stringObjectMap = CaseHearingFunctions.buildFrcCourtDetails(caseData);
        assertEquals("Thanet Family Court Hearing Centre", stringObjectMap.get(COURT_DETAILS_NAME_KEY));
        assertEquals("2nd Floor, The Courthouse, Cecil Square, Margate, Kent CT9 1RL", stringObjectMap.get(COURT_DETAILS_ADDRESS_KEY));
        assertEquals("01227 819200", stringObjectMap.get(COURT_DETAILS_PHONE_KEY));
        assertEquals("Family.canterbury.countycourt@justice.gov.uk", stringObjectMap.get(COURT_DETAILS_EMAIL_KEY));
    }

    @Test
    void shouldPopulateDurhamCourtDetails() {
        Map<String, Object> caseData = Map.of(
            REGION, NORTHEAST,
            NORTHEAST_FRC_LIST, CLEVELAND,
            CLEAVELAND_COURTLIST, "FR_cleaveland_hc_list_2");
        Map<String, Object> stringObjectMap = CaseHearingFunctions.buildFrcCourtDetails(caseData);
        assertEquals("Durham Justice Centre", stringObjectMap.get(COURT_DETAILS_NAME_KEY));
        assertEquals("Green Lane, Old Elvet, Durham, DH1 3RG", stringObjectMap.get(COURT_DETAILS_ADDRESS_KEY));
        assertEquals("0300 123 5577", stringObjectMap.get(COURT_DETAILS_PHONE_KEY));
        assertEquals("Family.newcastle.countycourt@justice.gov.uk", stringObjectMap.get(COURT_DETAILS_EMAIL_KEY));
    }

    @Test
    void shouldPopulateDurhamCourtDetailsFinrem() {
        AllocatedRegionWrapper regionWrapper = AllocatedRegionWrapper.builder().regionList(Region.NORTHEAST)
            .northEastFrcList(RegionNorthEastFrc.CLEVELAND)
            .courtListWrapper(
                DefaultCourtListWrapper.builder().cleavelandCourtList(ClevelandCourt.FR_CLEVELAND_HC_LIST_2)
                    .build())
            .build();
        FinremCaseData caseData = getFinremCaseData(regionWrapper);
        Map<String, Object> stringObjectMap = CaseHearingFunctions.buildFrcCourtDetails(caseData);
        assertEquals("Durham Justice Centre", stringObjectMap.get(COURT_DETAILS_NAME_KEY));
        assertEquals("Green Lane, Old Elvet, Durham, DH1 3RG", stringObjectMap.get(COURT_DETAILS_ADDRESS_KEY));
        assertEquals("0300 123 5577", stringObjectMap.get(COURT_DETAILS_PHONE_KEY));
        assertEquals("Family.newcastle.countycourt@justice.gov.uk", stringObjectMap.get(COURT_DETAILS_EMAIL_KEY));
    }

    @Test
    void shouldPopulateBournemouthCourtDetails() {
        Map<String, Object> caseData = Map.of(
            REGION, SOUTHWEST,
            SOUTHWEST_FRC_LIST, DORSET,
            DORSET_COURTLIST, BOURNEMOUTH);
        Map<String, Object> stringObjectMap = CaseHearingFunctions.buildFrcCourtDetails(caseData);
        assertEquals("Bournemouth and Poole County Court and Family Court", stringObjectMap.get(COURT_DETAILS_NAME_KEY));
        assertEquals("Courts of Justice, Deansleigh Road, Bournemouth, BH7 7DS", stringObjectMap.get(COURT_DETAILS_ADDRESS_KEY));
        assertEquals("0300 123 5577", stringObjectMap.get(COURT_DETAILS_PHONE_KEY));
        assertEquals("BournemouthFRC.bournemouth.countycourt@justice.gov.uk", stringObjectMap.get(COURT_DETAILS_EMAIL_KEY));
    }

    @Test
    void shouldPopulateBournemouthCourtDetailsFinrem() {
        AllocatedRegionWrapper regionWrapper = AllocatedRegionWrapper.builder().regionList(Region.SOUTHWEST)
            .southWestFrcList(RegionSouthWestFrc.DORSET)
            .courtListWrapper(
                DefaultCourtListWrapper.builder().dorsetCourtList(DorsetCourt.BOURNEMOUTH)
                    .build())
            .build();
        FinremCaseData caseData = getFinremCaseData(regionWrapper);
        Map<String, Object> stringObjectMap = CaseHearingFunctions.buildFrcCourtDetails(caseData);
        assertEquals("Bournemouth and Poole County Court and Family Court", stringObjectMap.get(COURT_DETAILS_NAME_KEY));
        assertEquals("Courts of Justice, Deansleigh Road, Bournemouth, BH7 7DS", stringObjectMap.get(COURT_DETAILS_ADDRESS_KEY));
        assertEquals("0300 123 5577", stringObjectMap.get(COURT_DETAILS_PHONE_KEY));
        assertEquals("BournemouthFRC.bournemouth.countycourt@justice.gov.uk", stringObjectMap.get(COURT_DETAILS_EMAIL_KEY));
    }

    @Test
    void shouldPopulateTorquayCourtDetailsFinrem() {
        AllocatedRegionWrapper regionWrapper = AllocatedRegionWrapper.builder().regionList(Region.SOUTHWEST)
            .southWestFrcList(RegionSouthWestFrc.DEVON)
            .courtListWrapper(
                DefaultCourtListWrapper.builder().devonCourtList(DevonCourt.TORQUAY)
                    .build())
            .build();
        FinremCaseData caseData = getFinremCaseData(regionWrapper);
        Map<String, Object> stringObjectMap = CaseHearingFunctions.buildFrcCourtDetails(caseData);
        assertEquals("Torquay and Newton Abbot County and Family Court", stringObjectMap.get(COURT_DETAILS_NAME_KEY));
        assertEquals("The Willows, Nicholson Road, Torquay, TQ2 7AZ", stringObjectMap.get(COURT_DETAILS_ADDRESS_KEY));
        assertEquals("0300 123 5577", stringObjectMap.get(COURT_DETAILS_PHONE_KEY));
        assertEquals("FR.PlymouthHub@justice.gov.uk", stringObjectMap.get(COURT_DETAILS_EMAIL_KEY));
    }

    @Test
    void shouldPopulateTorquayCourtDetails() {
        Map<String, Object> caseData = Map.of(
            REGION, SOUTHWEST,
            SOUTHWEST_FRC_LIST, DEVON,
            DEVON_COURTLIST, TORQUAY);
        Map<String, Object> stringObjectMap = CaseHearingFunctions.buildFrcCourtDetails(caseData);
        assertEquals("Torquay and Newton Abbot County and Family Court", stringObjectMap.get(COURT_DETAILS_NAME_KEY));
        assertEquals("The Willows, Nicholson Road, Torquay, TQ2 7AZ", stringObjectMap.get(COURT_DETAILS_ADDRESS_KEY));
        assertEquals("0300 123 5577", stringObjectMap.get(COURT_DETAILS_PHONE_KEY));
        assertEquals("FR.PlymouthHub@justice.gov.uk", stringObjectMap.get(COURT_DETAILS_EMAIL_KEY));
    }

    @Test
    void shouldPopulateSalisburyCourtDetails() {
        Map<String, Object> caseData = Map.of(
            REGION, SOUTHWEST,
            SOUTHWEST_FRC_LIST, BRISTOLFRC,
            BRISTOL_COURTLIST, SALISBURY);
        Map<String, Object> stringObjectMap = CaseHearingFunctions.buildFrcCourtDetails(caseData);
        assertEquals("Salisbury Law Courts", stringObjectMap.get(COURT_DETAILS_NAME_KEY));
        assertEquals("Wilton Road, Salisbury, SP2 7EP", stringObjectMap.get(COURT_DETAILS_ADDRESS_KEY));
        assertEquals("0300 123 5577", stringObjectMap.get(COURT_DETAILS_PHONE_KEY));
        assertEquals("BristolFRC.bristol.countycourt@justice.gov.uk", stringObjectMap.get(COURT_DETAILS_EMAIL_KEY));
    }

    @Test
    void shouldPopulateSalisburyCourtDetailsFinrem() {
        AllocatedRegionWrapper regionWrapper = AllocatedRegionWrapper.builder().regionList(Region.SOUTHWEST)
            .southWestFrcList(RegionSouthWestFrc.BRISTOL)
            .courtListWrapper(
                DefaultCourtListWrapper.builder().bristolCourtList(BristolCourt.SALISBURY_LAW_COURTS)
                    .build())
            .build();
        FinremCaseData caseData = getFinremCaseData(regionWrapper);
        Map<String, Object> stringObjectMap = CaseHearingFunctions.buildFrcCourtDetails(caseData);
        assertEquals("Salisbury Law Courts", stringObjectMap.get(COURT_DETAILS_NAME_KEY));
        assertEquals("Wilton Road, Salisbury, SP2 7EP", stringObjectMap.get(COURT_DETAILS_ADDRESS_KEY));
        assertEquals("0300 123 5577", stringObjectMap.get(COURT_DETAILS_PHONE_KEY));
        assertEquals("BristolFRC.bristol.countycourt@justice.gov.uk", stringObjectMap.get(COURT_DETAILS_EMAIL_KEY));
    }

    @Test
    void shouldPopulatePrestatynCourtDetails() {
        Map<String, Object> caseData = Map.of(
            REGION, WALES,
            WALES_FRC_LIST, NORTHWALES,
            NORTH_WALES_COURTLIST, PRESTATYN);
        Map<String, Object> stringObjectMap = CaseHearingFunctions.buildFrcCourtDetails(caseData);
        assertEquals("Prestatyn Justice Centre", stringObjectMap.get(COURT_DETAILS_NAME_KEY));
        assertEquals("Victoria Road, Prestatyn, LL19 7TE", stringObjectMap.get(COURT_DETAILS_ADDRESS_KEY));
        assertEquals("0300 123 5577", stringObjectMap.get(COURT_DETAILS_PHONE_KEY));
        assertEquals("Family.prestatyn.countycourt@justice.gov.uk", stringObjectMap.get(COURT_DETAILS_EMAIL_KEY));
    }

    @Test
    void shouldPopulatePrestatynCourtDetailsFinrem() {
        AllocatedRegionWrapper regionWrapper = AllocatedRegionWrapper.builder().regionList(Region.WALES)
            .walesFrcList(RegionWalesFrc.NORTH_WALES)
            .courtListWrapper(
                DefaultCourtListWrapper.builder().northWalesCourtList(NorthWalesCourt.PRESTATYN)
                    .build())
            .build();
        FinremCaseData caseData = getFinremCaseData(regionWrapper);
        Map<String, Object> stringObjectMap = CaseHearingFunctions.buildFrcCourtDetails(caseData);
        assertEquals("Prestatyn Justice Centre", stringObjectMap.get(COURT_DETAILS_NAME_KEY));
        assertEquals("Victoria Road, Prestatyn, LL19 7TE", stringObjectMap.get(COURT_DETAILS_ADDRESS_KEY));
        assertEquals("0300 123 5577", stringObjectMap.get(COURT_DETAILS_PHONE_KEY));
        assertEquals("Family.prestatyn.countycourt@justice.gov.uk", stringObjectMap.get(COURT_DETAILS_EMAIL_KEY));
    }

    @Test
    void shouldPopulateConsentedCourtDetails() {
        Map<String, Object> stringObjectMap = CaseHearingFunctions.buildConsentedFrcCourtDetails();
        assertEquals("Family Court at the Courts and Tribunal Service Centre", stringObjectMap.get(COURT_DETAILS_NAME_KEY));
        assertEquals("PO Box 12746, Harlow, CM20 9QZ", stringObjectMap.get(COURT_DETAILS_ADDRESS_KEY));
        assertEquals("0300 303 0642", stringObjectMap.get(COURT_DETAILS_PHONE_KEY));
        assertEquals("contactFinancialRemedy@justice.gov.uk", stringObjectMap.get(COURT_DETAILS_EMAIL_KEY));
    }

    @Test
    void shouldPopulateHighCourtDetails() {
        Map<String, Object> caseData = Map.of(
            REGION, HIGHCOURT,
            HIGHCOURT_FRC_LIST, HIGHCOURT,
            HIGHCOURT_COURTLIST, HIGHCOURT_COURT);
        Map<String, Object> stringObjectMap = CaseHearingFunctions.buildFrcCourtDetails(caseData);
        assertEquals("High Court Family Division", stringObjectMap.get(COURT_DETAILS_NAME_KEY));
        assertEquals("High Court Family Division, Queens Building, Royal Courts of Justice, Strand, London, WC2A 2LL",
            stringObjectMap.get(COURT_DETAILS_ADDRESS_KEY));
        assertEquals("020 7947 7551", stringObjectMap.get(COURT_DETAILS_PHONE_KEY));
        assertEquals("rcj.familyhighcourt@justice.gov.uk", stringObjectMap.get(COURT_DETAILS_EMAIL_KEY));
    }

    @Test
    void shouldPopulateHighCourtDetailsFinrem() {
        AllocatedRegionWrapper regionWrapper = AllocatedRegionWrapper.builder().regionList(Region.HIGHCOURT)
            .highCourtFrcList(RegionHighCourtFrc.HIGHCOURT)
            .courtListWrapper(
                DefaultCourtListWrapper.builder().highCourtList(HighCourt.HIGHCOURT_COURT)
                    .build())
            .build();
        FinremCaseData caseData = getFinremCaseData(regionWrapper);
        Map<String, Object> stringObjectMap = CaseHearingFunctions.buildFrcCourtDetails(caseData);
        assertEquals("High Court Family Division", stringObjectMap.get(COURT_DETAILS_NAME_KEY));
        assertEquals("High Court Family Division, Queens Building, Royal Courts of Justice, Strand, London, WC2A 2LL",
            stringObjectMap.get(COURT_DETAILS_ADDRESS_KEY));
        assertEquals("020 7947 7551", stringObjectMap.get(COURT_DETAILS_PHONE_KEY));
        assertEquals("rcj.familyhighcourt@justice.gov.uk", stringObjectMap.get(COURT_DETAILS_EMAIL_KEY));
    }

    @Test
    void shouldReturnNullIfHighCourtFRCIsWrongDetails() {
        Map<String, Object> caseData = Map.of(
            REGION, HIGHCOURT,
            HIGHCOURT_FRC_LIST, LONDON,
            HIGHCOURT_COURTLIST, HIGHCOURT_COURT);
        Map<String, Object> stringObjectMap = CaseHearingFunctions.buildFrcCourtDetails(caseData);
        assertTrue(stringObjectMap.isEmpty());
    }

    @Test
    void shouldPopulateInterimHearingLondonFrcCourtDetails() {
        Map<String, Object> caseData = Map.of(
            INTERIM_REGION, LONDON,
            INTERIM_LONDON_FRC_LIST, CFC,
            INTERIM_HEARING_CFC_COURT_LIST, "FR_s_CFCList_2");
        Map<String, Object> stringObjectMap = CaseHearingFunctions.buildInterimHearingFrcCourtDetails(caseData);
        assertEquals("Croydon County Court And Family Court", stringObjectMap.get(COURT_DETAILS_NAME_KEY));
        assertEquals("Croydon County Court, Altyre Road, Croydon, CR9 5AB", stringObjectMap.get(COURT_DETAILS_ADDRESS_KEY));
        assertEquals("0300 123 5577", stringObjectMap.get(COURT_DETAILS_PHONE_KEY));
        assertEquals("FRCLondon@justice.gov.uk", stringObjectMap.get(COURT_DETAILS_EMAIL_KEY));
    }

    @Test
    void shouldReturnEmptyMap() {
        Map<String, Object> caseData = new HashMap<>();
        Map<String, Object> stringObjectMap = CaseHearingFunctions.buildInterimHearingFrcCourtDetails(caseData);
        assertEquals(true, stringObjectMap.isEmpty());
    }

    private FinremCaseData getFinremCaseData(AllocatedRegionWrapper regionWrapper) {
        FinremCaseData caseData = new FinremCaseData();
        caseData.getContactDetailsWrapper().setApplicantSolicitorEmail(TEST_SOLICITOR_EMAIL);
        caseData.getContactDetailsWrapper().setApplicantSolicitorName(TEST_SOLICITOR_NAME);
        caseData.getContactDetailsWrapper().setRespondentSolicitorEmail(TEST_RESP_SOLICITOR_EMAIL);
        caseData.getContactDetailsWrapper().setRespondentSolicitorName(TEST_RESP_SOLICITOR_NAME);
        caseData.getContactDetailsWrapper().setRespondentSolicitorReference(TEST_RESP_SOLICITOR_REFERENCE);
        caseData.getContactDetailsWrapper().setSolicitorReference(TEST_SOLICITOR_REFERENCE);
        caseData.setDivorceCaseNumber(TEST_DIVORCE_CASE_NUMBER);
        caseData.getGeneralApplicationWrapper().setGeneralApplicationReferToJudgeEmail(TEST_JUDGE_EMAIL);
        caseData.getRegionWrapper().setAllocatedRegionWrapper(regionWrapper);
        caseData.setBulkPrintLetterIdRes(NOTTINGHAM);
        return caseData;
    }
}
