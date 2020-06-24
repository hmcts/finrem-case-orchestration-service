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
            "FR_newport_hc_list_2", "Cardiff Civil & Family Justice Centre");

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
            "FR_swansea_hc_list_1", "Swansea Civil & Family Justice Centre");

        verifyCorrectCourtReturned(WALES, WALESLIST, SWANSEA , SWANSEA_COURTLIST,
            "FR_swansea_hc_list_2", "Aberystwyth Justice Centre");

        verifyCorrectCourtReturned(WALES, WALESLIST, SWANSEA , SWANSEA_COURTLIST,
            "FR_swansea_hc_list_3", "Haverfordwest County & Family Court");

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
            "FR_kent_surrey_hc_list_1", "CANTERBURY FAMILY COURT HEARING CENTRE");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEASTLIST, KENTFRC , KENTFRC_COURTLIST,
            "FR_kent_surrey_hc_list_2", "MAIDSTONE COMBINED COURT CENTRE");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEASTLIST, KENTFRC , KENTFRC_COURTLIST,
            "FR_kent_surrey_hc_list_3", "DARTFORD COUNTY COURT AND FAMILY COURT");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEASTLIST, KENTFRC , KENTFRC_COURTLIST,
            "FR_kent_surrey_hc_list_4", "MEDWAY COUNTY COURT AND FAMILY COURT");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEASTLIST, KENTFRC , KENTFRC_COURTLIST,
            "FR_kent_surrey_hc_list_5", "GUILDFORD COUNTY COURT AND FAMILY COURT");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEASTLIST, KENTFRC , KENTFRC_COURTLIST,
            "FR_kent_surrey_hc_list_6", "STAINES COUNTY COURT AND FAMILY COURT");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEASTLIST, KENTFRC , KENTFRC_COURTLIST,
            "FR_kent_surrey_hc_list_7", "BRIGHTON COUNTY AND FAMILY COURT");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEASTLIST, KENTFRC , KENTFRC_COURTLIST,
            "FR_kent_surrey_hc_list_8", "WORTHING COUNTY COURT AND FAMILY COURT");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEASTLIST, KENTFRC , KENTFRC_COURTLIST,
            "FR_kent_surrey_hc_list_9", "HASTINGS COUNTY COURT AND FAMILY COURT HEARING CENTRE");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEASTLIST, KENTFRC , KENTFRC_COURTLIST,
            "FR_kent_surrey_hc_list_10", "HORSHAM COUNTY COURT AND FAMILY COURT");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEASTLIST, "invalid" , KENTFRC_COURTLIST,
            "FR_kent_surrey_hc_list_10", "");


        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEASTLIST, KENTFRC, KENTFRC_COURTLIST,
            "invalid", "");
    }

    @Test
    public void cleavelandCourts() {
        verifyCorrectCourtReturned(NORTHEAST, NORTHEASTLIST, CLEAVELAND, CLEAVELAND_COURTLIST,
            "FR_cleaveland_hc_list_1", "NEWCASTLE UPON TYNE JUSTICE CENTRE");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEASTLIST, CLEAVELAND, CLEAVELAND_COURTLIST,
            "FR_cleaveland_hc_list_2", "DURHAM JUSTICE CENTRE");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEASTLIST, CLEAVELAND, CLEAVELAND_COURTLIST,
            "FR_cleaveland_hc_list_3", "SUNDERLAND COUNTY AND FAMILY COURT");

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
            "FR_nw_yorkshire_hc_list_1", "harrogate justice centre");

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
            "FR_liverpool_hc_list_1", "LIVERPOOL CIVIL AND FAMILY COURT");

        verifyCorrectCourtReturned(NORTHWEST, NORTHWESTLIST, LIVERPOOL, LIVERPOOL_COURTLIST,
            "FR_liverpool_hc_list_2", "CHESTER CIVIL AND FAMILY JUSTICE CENTRE");

        verifyCorrectCourtReturned(NORTHWEST, NORTHWESTLIST, LIVERPOOL, LIVERPOOL_COURTLIST,
            "FR_liverpool_hc_list_3", "CREWE COUNTY COURT AND FAMILY COURT ");

        verifyCorrectCourtReturned(NORTHWEST, NORTHWESTLIST, LIVERPOOL, LIVERPOOL_COURTLIST,
            "FR_liverpool_hc_list_4", "ST. HELENS COUNTY COURT AND FAMILY COURT ");

        verifyCorrectCourtReturned(NORTHWEST, NORTHWESTLIST, LIVERPOOL, LIVERPOOL_COURTLIST,
            "FR_liverpool_hc_list_5", "BIRKENHEAD COUNTY COURT AND FAMILY COURT");

        verifyCorrectCourtReturned(NORTHWEST, NORTHWESTLIST, "invalid", LIVERPOOL_COURTLIST,
            "FR_liverpool_hc_list_1", "");

        verifyCorrectCourtReturned(NORTHWEST, NORTHWESTLIST, LIVERPOOL, LIVERPOOL_COURTLIST,
            "invalid", "");
    }

    @Test
    public void manchesterCourtList() throws Exception {
        verifyCorrectCourtReturned(NORTHWEST, NORTHWESTLIST, MANCHESTER, MANCHESTER_COURTLIST,
            "FR_manchester_hc_list_1", "MANCHESTER COUNTY AND FAMILY COURT ");

        verifyCorrectCourtReturned(NORTHWEST, NORTHWESTLIST, MANCHESTER, MANCHESTER_COURTLIST,
            "FR_manchester_hc_list_2", "STOCKPORT COUNTY COURT AND FAMILY COURT");

        verifyCorrectCourtReturned(NORTHWEST, NORTHWESTLIST, MANCHESTER, MANCHESTER_COURTLIST,
            "FR_manchester_hc_list_3", "WIGAN COUNTY COURT AND FAMILY COURT ");

        verifyCorrectCourtReturned(NORTHWEST, NORTHWESTLIST, MANCHESTER, MANCHESTER_COURTLIST,
            "invalid", "");

        verifyCorrectCourtReturned(NORTHWEST, NORTHWESTLIST, "invalid", MANCHESTER_COURTLIST,
            "FR_manchester_hc_list_3", "");
    }

    @Test
    public void londonCourtListTest() throws Exception {
        verifyCorrectCourtReturned(LONDON, LONDON_LIST, LONDON_CFC, LONDON_COURTLIST,
            "FR_s_CFCList_1", "BROMLEY COUNTY COURT AND FAMILY COURT");

        verifyCorrectCourtReturned(LONDON, LONDON_LIST, LONDON_CFC, LONDON_COURTLIST,
            "FR_s_CFCList_2", "CROYDON COUNTY COURT AND FAMILY COURT");

        verifyCorrectCourtReturned(LONDON, LONDON_LIST, LONDON_CFC, LONDON_COURTLIST,
            "FR_s_CFCList_3", "EDMONTON COUNTY COURT AND FAMILY COURT");

        verifyCorrectCourtReturned(LONDON, LONDON_LIST, LONDON_CFC, LONDON_COURTLIST,
            "FR_s_CFCList_4", "KINGSTON-UPON-THAMES COUNTY COURT AND FAMILY COURT");

        verifyCorrectCourtReturned(LONDON, LONDON_LIST, LONDON_CFC, LONDON_COURTLIST,
            "FR_s_CFCList_5", "ROMFORD COUNTY AND FAMILY COURT");

        verifyCorrectCourtReturned(LONDON, LONDON_LIST, LONDON_CFC, LONDON_COURTLIST,
            "FR_s_CFCList_6", "BARNET CIVIL AND FAMILY COURTS CENTRE");

        verifyCorrectCourtReturned(LONDON, LONDON_LIST, LONDON_CFC, LONDON_COURTLIST,
            "FR_s_CFCList_8", "BRENTFORD COUNTY AND FAMILY COURT");

        verifyCorrectCourtReturned(LONDON, LONDON_LIST, LONDON_CFC, LONDON_COURTLIST,
            "FR_s_CFCList_9", "CENTRAL FAMILY COURT");

        verifyCorrectCourtReturned(LONDON, LONDON_LIST, LONDON_CFC, LONDON_COURTLIST,
            "FR_s_CFCList_11", "EAST LONDON FAMILY COURT");

        verifyCorrectCourtReturned(LONDON, LONDON_LIST, LONDON_CFC, LONDON_COURTLIST,
            "FR_s_CFCList_14", "UXBRIDGE COUNTY COURT AND FAMILY COURT");

        verifyCorrectCourtReturned(LONDON, LONDON_LIST, LONDON_CFC, LONDON_COURTLIST,
            "FR_s_CFCList_16", "WILLESDEN COUNTY COURT AND FAMILY COURT");

        verifyCorrectCourtReturned(LONDON, LONDON_LIST, "invalid", LONDON_COURTLIST,
            "FR_s_CFCList_1", "");

        verifyCorrectCourtReturned(LONDON, LONDON_LIST, LONDON_CFC, LONDON_COURTLIST,
            "invalid", "");
    }

    @Test
    public void birminghamCourtListTest() throws Exception {
        verifyCorrectCourtReturned(MIDLANDS, MIDLANDSLIST, BIRMINGHAM, BIRMINGHAM_COURTLIST,
            "FR_birmingham_hc_list_1", "BIRMINGHAM CIVIL AND FAMILY JUSTICE CENTRE");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDSLIST, BIRMINGHAM, BIRMINGHAM_COURTLIST,
            "FR_birmingham_hc_list_2", "COVENTRY COMBINED COURT CENTRE");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDSLIST, BIRMINGHAM, BIRMINGHAM_COURTLIST,
            "FR_birmingham_hc_list_3", "TELFORD COUNTY COURT AND FAMILY COURT");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDSLIST, BIRMINGHAM, BIRMINGHAM_COURTLIST,
            "FR_birmingham_hc_list_4", "WOLVERHAMPTON COMBINED COURT CENTRE");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDSLIST, BIRMINGHAM, BIRMINGHAM_COURTLIST,
            "FR_birmingham_hc_list_5", "DUDLEY COUNTY COURT AND FAMILY COURT");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDSLIST, BIRMINGHAM, BIRMINGHAM_COURTLIST,
            "FR_birmingham_hc_list_6", "WALSALL COUNTY AND FAMILY COURT");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDSLIST, BIRMINGHAM, BIRMINGHAM_COURTLIST,
            "FR_birmingham_hc_list_7", "STOKE ON TRENT COMBINED COURT");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDSLIST, BIRMINGHAM, BIRMINGHAM_COURTLIST,
            "FR_birmingham_hc_list_8", "WORCESTER COMBINED COURT");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDSLIST, BIRMINGHAM, BIRMINGHAM_COURTLIST,
            "FR_birmingham_hc_list_9", "STAFFORD COMBINED COURT");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDSLIST, BIRMINGHAM, BIRMINGHAM_COURTLIST,
            "FR_birmingham_hc_list_10", "HEREFORD COUNTY COURT AND FAMILY COURT");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDSLIST, BIRMINGHAM, BIRMINGHAM_COURTLIST,
            "invalid", "");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDSLIST, "invalid", BIRMINGHAM_COURTLIST,
            "FR_birmingham_hc_list_10", "");

    }

    @Test
    public void nottinghamCourtListTest() throws Exception {

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDSLIST, NOTTINGHAM, NOTTINGHAM_COURTLIST,
            "FR_s_NottinghamList_1", "NOTTINGHAM COUNTY COURT AND FAMILY COURT");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDSLIST, NOTTINGHAM, NOTTINGHAM_COURTLIST,
            "FR_s_NottinghamList_2", "DERBY COMBINED COURT CENTRE");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDSLIST, NOTTINGHAM, NOTTINGHAM_COURTLIST,
            "FR_s_NottinghamList_3", "LEICESTER COUNTY COURT AND FAMILY COURT");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDSLIST, NOTTINGHAM, NOTTINGHAM_COURTLIST,
            "FR_s_NottinghamList_4", "LINCOLN COUNTY COURT AND FAMILY COURT");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDSLIST, NOTTINGHAM, NOTTINGHAM_COURTLIST,
            "FR_s_NottinghamList_5", "NORTHAMPTON CROWN, COUNTY AND FAMILY COURT");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDSLIST, NOTTINGHAM, NOTTINGHAM_COURTLIST,
            "FR_s_NottinghamList_6", "CHESTERFIELD COUNTY COURT");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDSLIST, NOTTINGHAM, NOTTINGHAM_COURTLIST,
            "FR_s_NottinghamList_7", "MANSFIELD MAGISTRATES AND COUNTY COURT");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDSLIST, NOTTINGHAM, NOTTINGHAM_COURTLIST,
            "FR_s_NottinghamList_8", "BOSTON COUNTY COURT AND FAMILY COURT");

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
