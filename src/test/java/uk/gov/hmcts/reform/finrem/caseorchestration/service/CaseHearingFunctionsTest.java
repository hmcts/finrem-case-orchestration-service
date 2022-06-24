package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
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
        assertThat(stringObjectMap.get(COURT_DETAILS_EMAIL_KEY), is("hmctsfinancialremedy@justice.gov.uk"));
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
        assertThat(stringObjectMap.get(COURT_DETAILS_EMAIL_KEY), is("hmctsfinancialremedy@justice.gov.uk"));
    }

    @Test
    public void shouldPopulateCambridgeCourtDetails() {
        Map<String, Object> caseData = ImmutableMap.of(
            REGION, SOUTHEAST,
            SOUTHEAST_FRC_LIST, BEDFORDSHIRE,
            BEDFORDSHIRE_COURTLIST, CAMBRIDGE);

        Map<String, Object> stringObjectMap = CaseHearingFunctions.buildFrcCourtDetails(caseData);
        assertThat(stringObjectMap.get(COURT_DETAILS_NAME_KEY), is("Cambridge County Court and Family Court"));
        assertThat(stringObjectMap.get(COURT_DETAILS_ADDRESS_KEY), is("Crown Buildings, Rivergate, Peterborough, PE1 1EJ"));
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
        assertThat(stringObjectMap.get(COURT_DETAILS_PHONE_KEY), is("01865 264 200"));
        assertThat(stringObjectMap.get(COURT_DETAILS_EMAIL_KEY), is("FRCThamesValley@justice.gov.uk"));
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
        assertThat(stringObjectMap.get(COURT_DETAILS_PHONE_KEY), is("0300 303 0642"));
        assertThat(stringObjectMap.get(COURT_DETAILS_EMAIL_KEY), is("hmctsfinancialremedy@justice.gov.uk"));
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
}
