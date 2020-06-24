package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import org.junit.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.HashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.defaultCaseDetails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ALLOCATED_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BIRMINGHAM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BIRMINGHAM_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CLEAVELAND;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CLEAVELAND_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HSYORKSHIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HSYORKSHIRE_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.KENTFRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.KENTFRC_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LIVERPOOL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LIVERPOOL_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON_CFC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MANCHESTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MANCHESTER_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIDLANDS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIDLANDSLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NEWPORT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NEWPORT_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHEAST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHEASTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHWEST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHWESTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTTINGHAM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTTINGHAM_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NWYORKSHIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NWYORKSHIRE_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.REGION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHEAST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHEASTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SWANSEA;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SWANSEA_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WALES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WALESLIST;

public class ContestedCourtHelperTest {

    private CaseDetails details;

    @Test
    public void newportCourts() {
        verifyCorrectCourtReturned(WALES, WALESLIST, NEWPORT , NEWPORT_COURTLIST,
            "FR_newport_hc_list_1", "Newport Civil and Family Court");

        verifyCorrectCourtReturned(WALES, WALESLIST, NEWPORT , NEWPORT_COURTLIST,
            "FR_newport_hc_list_2", "Cardiff Civil and Family Justice Centre");

        verifyCorrectCourtReturned(WALES, WALESLIST, NEWPORT , NEWPORT_COURTLIST,
            "FR_newport_hc_list_3", "Merthyr Tydfil Combined Court Centre");

        verifyCorrectCourtReturned(WALES, WALESLIST, NEWPORT , NEWPORT_COURTLIST,
            "FR_newport_hc_list_4", "Pontypridd County and Family Court");

        verifyCorrectCourtReturned(WALES, WALESLIST, NEWPORT , NEWPORT_COURTLIST,
            "FR_newport_hc_list_5", "Blackwood Civil and Family Court");

        verifyCorrectCourtReturned(WALES, WALESLIST, "invalid" , NEWPORT_COURTLIST,
            "FR_newport_hc_list_5", "");

        verifyCorrectCourtReturned(WALES, WALESLIST, NEWPORT , NEWPORT_COURTLIST,
            "invalid", "");
    }

    @Test
    public void swanseaCourts() {
        verifyCorrectCourtReturned(WALES, WALESLIST, SWANSEA , SWANSEA_COURTLIST,
            "FR_swansea_hc_list_1", "Swansea Civil and Family Justice Centre");

        verifyCorrectCourtReturned(WALES, WALESLIST, SWANSEA , SWANSEA_COURTLIST,
            "FR_swansea_hc_list_2", "Aberystwyth Justice Centre");

        verifyCorrectCourtReturned(WALES, WALESLIST, SWANSEA , SWANSEA_COURTLIST,
            "FR_swansea_hc_list_3", "Haverfordwest County and Family Court");

        verifyCorrectCourtReturned(WALES, WALESLIST, SWANSEA , SWANSEA_COURTLIST,
            "FR_swansea_hc_list_4", "Carmarthen County and Family Court");

        verifyCorrectCourtReturned(WALES, WALESLIST, SWANSEA , SWANSEA_COURTLIST,
            "FR_swansea_hc_list_5", "Llanelli Law Courts");

        verifyCorrectCourtReturned(WALES, WALESLIST, SWANSEA , SWANSEA_COURTLIST,
            "FR_swansea_hc_list_6", "Port Talbot Justice Centre");

        verifyCorrectCourtReturned(WALES, WALESLIST, "invalid" , SWANSEA_COURTLIST,
            "FR_swansea_hc_list_6", "");

        verifyCorrectCourtReturned(WALES, WALESLIST, SWANSEA , SWANSEA_COURTLIST,
            "invalid", "");

    }

    @Test
    public void kentCourts() {
        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEASTLIST, KENTFRC , KENTFRC_COURTLIST,
            "FR_kent_surrey_hc_list_1", "Canterbury Family Court Hearing Centre");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEASTLIST, KENTFRC , KENTFRC_COURTLIST,
            "FR_kent_surrey_hc_list_2", "Maidstone Combined Court Centre");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEASTLIST, KENTFRC , KENTFRC_COURTLIST,
            "FR_kent_surrey_hc_list_3", "Dartford County Court and Family Court");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEASTLIST, KENTFRC , KENTFRC_COURTLIST,
            "FR_kent_surrey_hc_list_4", "Medway County Court and Family Court");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEASTLIST, KENTFRC , KENTFRC_COURTLIST,
            "FR_kent_surrey_hc_list_5", "Guildford County Court and Family Court");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEASTLIST, KENTFRC , KENTFRC_COURTLIST,
            "FR_kent_surrey_hc_list_6", "Staines County Court and Family Court");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEASTLIST, KENTFRC , KENTFRC_COURTLIST,
            "FR_kent_surrey_hc_list_7", "Brighton County and Family Court");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEASTLIST, KENTFRC , KENTFRC_COURTLIST,
            "FR_kent_surrey_hc_list_8", "Worthing County Court and Family Court");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEASTLIST, KENTFRC , KENTFRC_COURTLIST,
            "FR_kent_surrey_hc_list_9", "Hastings County Court and Family Court Hearing Centre");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEASTLIST, KENTFRC , KENTFRC_COURTLIST,
            "FR_kent_surrey_hc_list_10", "Horsham County Court and Family Court");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEASTLIST, "invalid" , KENTFRC_COURTLIST,
            "FR_kent_surrey_hc_list_10", "");


        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEASTLIST, KENTFRC, KENTFRC_COURTLIST,
            "invalid", "");
    }

    @Test
    public void cleavelandCourts() {
        verifyCorrectCourtReturned(NORTHEAST, NORTHEASTLIST, CLEAVELAND, CLEAVELAND_COURTLIST,
            "FR_cleaveland_hc_list_1", "Newcastle upon Tyne Justice Centre");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEASTLIST, CLEAVELAND, CLEAVELAND_COURTLIST,
            "FR_cleaveland_hc_list_2", "Durham Justice Centre");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEASTLIST, CLEAVELAND, CLEAVELAND_COURTLIST,
            "FR_cleaveland_hc_list_3", "Sunderland County and Family Court");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEASTLIST, CLEAVELAND, CLEAVELAND_COURTLIST,
            "FR_cleaveland_hc_list_4", "Middlesbrough County Court at Teesside Combined Court");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEASTLIST, CLEAVELAND, CLEAVELAND_COURTLIST,
            "FR_cleaveland_hc_list_5", "Gateshead County Court and Family Court");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEASTLIST, CLEAVELAND, CLEAVELAND_COURTLIST,
            "FR_cleaveland_hc_list_6", "South Shields County Court and Family Court");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEASTLIST, CLEAVELAND, CLEAVELAND_COURTLIST,
            "FR_cleaveland_hc_list_7", "North Shields County Court and Family Court");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEASTLIST, CLEAVELAND, CLEAVELAND_COURTLIST,
            "FR_cleaveland_hc_list_8", "Darlington County Court and Family Court");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEASTLIST, "invalid", CLEAVELAND_COURTLIST,
            "FR_cleaveland_hc_list_8", "");


        verifyCorrectCourtReturned(NORTHEAST, NORTHEASTLIST, CLEAVELAND, CLEAVELAND_COURTLIST,
            "invalid", "");
    }

    @Test
    public void nwYorkshireCourts() {
        verifyCorrectCourtReturned(NORTHEAST, NORTHEASTLIST, NWYORKSHIRE, NWYORKSHIRE_COURTLIST,
            "FR_nw_yorkshire_hc_list_1", "Harrogate Justice Centre");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEASTLIST, NWYORKSHIRE, NWYORKSHIRE_COURTLIST,
            "FR_nw_yorkshire_hc_list_2", "Bradford Combined Court Centre");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEASTLIST, NWYORKSHIRE, NWYORKSHIRE_COURTLIST,
            "FR_nw_yorkshire_hc_list_3", "Huddersfield County Court and Family Court");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEASTLIST, NWYORKSHIRE, NWYORKSHIRE_COURTLIST,
            "FR_nw_yorkshire_hc_list_4", "Wakefield Civil and Family Justice Centre");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEASTLIST, NWYORKSHIRE, NWYORKSHIRE_COURTLIST,
            "FR_nw_yorkshire_hc_list_5", "York County Court and Family Court");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEASTLIST, NWYORKSHIRE, NWYORKSHIRE_COURTLIST,
            "FR_nw_yorkshire_hc_list_6", "Scarborough Justice Centre");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEASTLIST, NWYORKSHIRE, NWYORKSHIRE_COURTLIST,
            "FR_nw_yorkshire_hc_list_7", "Skipton County Court and Family Court");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEASTLIST, NWYORKSHIRE, NWYORKSHIRE_COURTLIST,
            "FR_nw_yorkshire_hc_list_8", "Leeds Combined Court Centre");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEASTLIST, "invalid", "",
            "", "");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEASTLIST, NWYORKSHIRE, "",
            "invalid", "");
    }

    @Test
    public void HSYorkshireCourts() {
        verifyCorrectCourtReturned(NORTHEAST, NORTHEASTLIST, HSYORKSHIRE, HSYORKSHIRE_COURTLIST,
            "FR_humber_hc_list_1", "Sheffield Family Hearing Centre");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEASTLIST, HSYORKSHIRE, HSYORKSHIRE_COURTLIST,
            "FR_humber_hc_list_2", "Kingston-upon-Hull Combined Court Centre");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEASTLIST, HSYORKSHIRE, HSYORKSHIRE_COURTLIST,
            "FR_humber_hc_list_3", "Doncaster Justice Centre North");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEASTLIST, HSYORKSHIRE, HSYORKSHIRE_COURTLIST,
            "FR_humber_hc_list_4", "Great Grimsby Combined Court Centre");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEASTLIST, HSYORKSHIRE, HSYORKSHIRE_COURTLIST,
            "FR_humber_hc_list_5", "Barnsley Law Courts");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEASTLIST, "invalid", HSYORKSHIRE_COURTLIST,
            "FR_humber_hc_list_5", "");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEASTLIST, HSYORKSHIRE, HSYORKSHIRE_COURTLIST,
            "invalid", "");
    }

    @Test
    public void liverpoolCourtList() throws Exception {
        verifyCorrectCourtReturned(NORTHWEST, NORTHWESTLIST, LIVERPOOL, LIVERPOOL_COURTLIST,
            "FR_liverpool_hc_list_1", "Liverpool Civil and Family Court");

        verifyCorrectCourtReturned(NORTHWEST, NORTHWESTLIST, LIVERPOOL, LIVERPOOL_COURTLIST,
            "FR_liverpool_hc_list_2", "Chester Civil and Family Justice Centre");

        verifyCorrectCourtReturned(NORTHWEST, NORTHWESTLIST, LIVERPOOL, LIVERPOOL_COURTLIST,
            "FR_liverpool_hc_list_3", "Crewe County Court and Family Court");

        verifyCorrectCourtReturned(NORTHWEST, NORTHWESTLIST, LIVERPOOL, LIVERPOOL_COURTLIST,
            "FR_liverpool_hc_list_4", "St. Helens County Court and Family Court");

        verifyCorrectCourtReturned(NORTHWEST, NORTHWESTLIST, LIVERPOOL, LIVERPOOL_COURTLIST,
            "FR_liverpool_hc_list_5", "Birkenhead County Court and Family Court");

        verifyCorrectCourtReturned(NORTHWEST, NORTHWESTLIST, "invalid", LIVERPOOL_COURTLIST,
            "FR_liverpool_hc_list_1", "");

        verifyCorrectCourtReturned(NORTHWEST, NORTHWESTLIST, LIVERPOOL, LIVERPOOL_COURTLIST,
            "invalid", "");
    }

    @Test
    public void manchesterCourtList() throws Exception {
        verifyCorrectCourtReturned(NORTHWEST, NORTHWESTLIST, MANCHESTER, MANCHESTER_COURTLIST,
            "FR_manchester_hc_list_1", "Manchester County and Family Court");

        verifyCorrectCourtReturned(NORTHWEST, NORTHWESTLIST, MANCHESTER, MANCHESTER_COURTLIST,
            "FR_manchester_hc_list_2", "Stockport County Court and Family Court");

        verifyCorrectCourtReturned(NORTHWEST, NORTHWESTLIST, MANCHESTER, MANCHESTER_COURTLIST,
            "FR_manchester_hc_list_3", "Wigan County Court and Family Court");

        verifyCorrectCourtReturned(NORTHWEST, NORTHWESTLIST, MANCHESTER, MANCHESTER_COURTLIST,
            "invalid", "");

        verifyCorrectCourtReturned(NORTHWEST, NORTHWESTLIST, "invalid", MANCHESTER_COURTLIST,
            "FR_manchester_hc_list_3", "");
    }

    @Test
    public void londonCourtListTest() throws Exception {
        verifyCorrectCourtReturned(LONDON, LONDON_LIST, LONDON_CFC, LONDON_COURTLIST,
            "FR_s_CFCList_1", "Bromley County Court and Family Court");

        verifyCorrectCourtReturned(LONDON, LONDON_LIST, LONDON_CFC, LONDON_COURTLIST,
            "FR_s_CFCList_2", "Croydon County Court and Family Court");

        verifyCorrectCourtReturned(LONDON, LONDON_LIST, LONDON_CFC, LONDON_COURTLIST,
            "FR_s_CFCList_3", "Edmonton County Court and Family Court");

        verifyCorrectCourtReturned(LONDON, LONDON_LIST, LONDON_CFC, LONDON_COURTLIST,
            "FR_s_CFCList_4", "Kingston-upon-thames County Court and Family Court");

        verifyCorrectCourtReturned(LONDON, LONDON_LIST, LONDON_CFC, LONDON_COURTLIST,
            "FR_s_CFCList_5", "Romford County and Family Court");

        verifyCorrectCourtReturned(LONDON, LONDON_LIST, LONDON_CFC, LONDON_COURTLIST,
            "FR_s_CFCList_6", "Barnet Civil and Family Courts Centre");

        verifyCorrectCourtReturned(LONDON, LONDON_LIST, LONDON_CFC, LONDON_COURTLIST,
            "FR_s_CFCList_8", "Brentford County and Family Court");

        verifyCorrectCourtReturned(LONDON, LONDON_LIST, LONDON_CFC, LONDON_COURTLIST,
            "FR_s_CFCList_9", "Central Family Court");

        verifyCorrectCourtReturned(LONDON, LONDON_LIST, LONDON_CFC, LONDON_COURTLIST,
            "FR_s_CFCList_11", "East London Family Court");

        verifyCorrectCourtReturned(LONDON, LONDON_LIST, LONDON_CFC, LONDON_COURTLIST,
            "FR_s_CFCList_14", "Uxbridge County Court and Family Court");

        verifyCorrectCourtReturned(LONDON, LONDON_LIST, LONDON_CFC, LONDON_COURTLIST,
            "FR_s_CFCList_16", "Willesden County Court and Family Court");

        verifyCorrectCourtReturned(LONDON, LONDON_LIST, "invalid", LONDON_COURTLIST,
            "FR_s_CFCList_1", "");

        verifyCorrectCourtReturned(LONDON, LONDON_LIST, LONDON_CFC, LONDON_COURTLIST,
            "invalid", "");
    }

    @Test
    public void birminghamCourtListTest() throws Exception {
        verifyCorrectCourtReturned(MIDLANDS, MIDLANDSLIST, BIRMINGHAM, BIRMINGHAM_COURTLIST,
            "FR_birmingham_hc_list_1", "Birmingham Civil and Family Justice Centre");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDSLIST, BIRMINGHAM, BIRMINGHAM_COURTLIST,
            "FR_birmingham_hc_list_2", "Coventry Combined Court Centre");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDSLIST, BIRMINGHAM, BIRMINGHAM_COURTLIST,
            "FR_birmingham_hc_list_3", "Telford County Court and Family Court");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDSLIST, BIRMINGHAM, BIRMINGHAM_COURTLIST,
            "FR_birmingham_hc_list_4", "Wolverhampton Combined Court Centre");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDSLIST, BIRMINGHAM, BIRMINGHAM_COURTLIST,
            "FR_birmingham_hc_list_5", "Dudley County Court and Family Court");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDSLIST, BIRMINGHAM, BIRMINGHAM_COURTLIST,
            "FR_birmingham_hc_list_6", "Walsall County and Family Court");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDSLIST, BIRMINGHAM, BIRMINGHAM_COURTLIST,
            "FR_birmingham_hc_list_7", "Stoke on Trent Combined Court");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDSLIST, BIRMINGHAM, BIRMINGHAM_COURTLIST,
            "FR_birmingham_hc_list_8", "Worcester Combined Court");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDSLIST, BIRMINGHAM, BIRMINGHAM_COURTLIST,
            "FR_birmingham_hc_list_9", "Stafford Combined Court");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDSLIST, BIRMINGHAM, BIRMINGHAM_COURTLIST,
            "FR_birmingham_hc_list_10", "Hereford County Court and Family Court");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDSLIST, BIRMINGHAM, BIRMINGHAM_COURTLIST,
            "invalid", "");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDSLIST, "invalid", BIRMINGHAM_COURTLIST,
            "FR_birmingham_hc_list_10", "");

    }

    @Test
    public void nottinghamCourtListTest() throws Exception {

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDSLIST, NOTTINGHAM, NOTTINGHAM_COURTLIST,
            "FR_s_NottinghamList_1", "Nottingham County Court and Family Court");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDSLIST, NOTTINGHAM, NOTTINGHAM_COURTLIST,
            "FR_s_NottinghamList_2", "Derby Combined Court Centre");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDSLIST, NOTTINGHAM, NOTTINGHAM_COURTLIST,
            "FR_s_NottinghamList_3", "Leicester County Court and Family Court");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDSLIST, NOTTINGHAM, NOTTINGHAM_COURTLIST,
            "FR_s_NottinghamList_4", "Lincoln County Court and Family Court");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDSLIST, NOTTINGHAM, NOTTINGHAM_COURTLIST,
            "FR_s_NottinghamList_5", "Northampton Crown, County and Family Court");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDSLIST, NOTTINGHAM, NOTTINGHAM_COURTLIST,
            "FR_s_NottinghamList_6", "Chesterfield County Court");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDSLIST, NOTTINGHAM, NOTTINGHAM_COURTLIST,
            "FR_s_NottinghamList_7", "Mansfield Magistrates and County Court");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDSLIST, NOTTINGHAM, NOTTINGHAM_COURTLIST,
            "FR_s_NottinghamList_8", "Boston County Court and Family Court");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDSLIST, NOTTINGHAM, NOTTINGHAM_COURTLIST,
            "INVALID", "");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDSLIST, "invalid", NOTTINGHAM_COURTLIST,
            "FR_s_NottinghamList_8", "");
    }


    private CaseDetails getCaseDetailsWithAllocatedValues(String region, String subRegionListName, String subRegion,
                                                          String courtListName, String court) {
        CaseDetails details = defaultCaseDetails();
        HashMap allocatedCourtList = new HashMap<String, String>();
        allocatedCourtList.put(REGION, region);
        allocatedCourtList.put(subRegionListName, subRegion);
        allocatedCourtList.put(courtListName, court);
        details.getData().put(ALLOCATED_COURT_LIST, allocatedCourtList);
        return details;
    }

    private void verifyCorrectCourtReturned(String region, String subRegionListName, String subRegion,
                                                          String courtListName, String court, String expectedValue) {
        details = getCaseDetailsWithAllocatedValues(region, subRegionListName, subRegion, courtListName,
            court);
        assertThat(ContestedCourtHelper.getSelectedCourt(details), is(expectedValue));
    }
}
