package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import org.junit.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.defaultCaseDetails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BIRMINGHAM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BIRMINGHAM_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CFC_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CLEAVELAND;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CLEAVELAND_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HSYORKSHIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HSYORKSHIRE_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.KENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.KENTFRC_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LIVERPOOL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LIVERPOOL_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON_CFC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MANCHESTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MANCHESTER_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIDLANDS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIDLANDS_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NEWPORT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NEWPORT_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHEAST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHEAST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHWEST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHWEST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTTINGHAM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTTINGHAM_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NWYORKSHIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NWYORKSHIRE_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.REGION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHEAST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHEAST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SWANSEA;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SWANSEA_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WALES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WALES_FRC_LIST;

public class ContestedCourtHelperTest {

    private CaseDetails details;

    @Test
    public void newportCourts() {
        verifyCorrectCourtReturned(WALES, WALES_FRC_LIST, NEWPORT, NEWPORT_COURTLIST,
            "FR_newport_hc_list_1", "Newport Civil and Family Court");

        verifyCorrectCourtReturned(WALES, WALES_FRC_LIST, NEWPORT, NEWPORT_COURTLIST,
            "FR_newport_hc_list_2", "Cardiff Civil and Family Justice Centre");

        verifyCorrectCourtReturned(WALES, WALES_FRC_LIST, NEWPORT, NEWPORT_COURTLIST,
            "FR_newport_hc_list_3", "Merthyr Tydfil Combined Court Centre");

        verifyCorrectCourtReturned(WALES, WALES_FRC_LIST, NEWPORT, NEWPORT_COURTLIST,
            "FR_newport_hc_list_4", "Pontypridd County and Family Court");

        verifyCorrectCourtReturned(WALES, WALES_FRC_LIST, NEWPORT, NEWPORT_COURTLIST,
            "FR_newport_hc_list_5", "Blackwood Civil and Family Court");

        verifyCorrectCourtReturned(WALES, WALES_FRC_LIST, "invalid", NEWPORT_COURTLIST,
            "FR_newport_hc_list_5", "");

        verifyCorrectCourtReturned(WALES, WALES_FRC_LIST, NEWPORT, NEWPORT_COURTLIST,
            "invalid", "");
    }

    @Test
    public void swanseaCourts() {
        verifyCorrectCourtReturned(WALES, WALES_FRC_LIST, SWANSEA, SWANSEA_COURTLIST,
            "FR_swansea_hc_list_1", "Swansea Civil and Family Justice Centre");

        verifyCorrectCourtReturned(WALES, WALES_FRC_LIST, SWANSEA, SWANSEA_COURTLIST,
            "FR_swansea_hc_list_2", "Aberystwyth Justice Centre");

        verifyCorrectCourtReturned(WALES, WALES_FRC_LIST, SWANSEA, SWANSEA_COURTLIST,
            "FR_swansea_hc_list_3", "Haverfordwest County and Family Court");

        verifyCorrectCourtReturned(WALES, WALES_FRC_LIST, SWANSEA, SWANSEA_COURTLIST,
            "FR_swansea_hc_list_4", "Carmarthen County and Family Court");

        verifyCorrectCourtReturned(WALES, WALES_FRC_LIST, SWANSEA, SWANSEA_COURTLIST,
            "FR_swansea_hc_list_5", "Llanelli Law Courts");

        verifyCorrectCourtReturned(WALES, WALES_FRC_LIST, SWANSEA, SWANSEA_COURTLIST,
            "FR_swansea_hc_list_6", "Port Talbot Justice Centre");

        verifyCorrectCourtReturned(WALES, WALES_FRC_LIST, "invalid", SWANSEA_COURTLIST,
            "FR_swansea_hc_list_6", "");

        verifyCorrectCourtReturned(WALES, WALES_FRC_LIST, SWANSEA, SWANSEA_COURTLIST,
            "invalid", "");

    }

    @Test
    public void kentCourts() {
        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, KENT, KENTFRC_COURTLIST,
            "FR_kent_surrey_hc_list_1", "Canterbury Family Court Hearing Centre");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, KENT, KENTFRC_COURTLIST,
            "FR_kent_surrey_hc_list_2", "Maidstone Combined Court Centre");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, KENT, KENTFRC_COURTLIST,
            "FR_kent_surrey_hc_list_3", "Dartford County Court and Family Court");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, KENT, KENTFRC_COURTLIST,
            "FR_kent_surrey_hc_list_4", "Medway County Court and Family Court");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, KENT, KENTFRC_COURTLIST,
            "FR_kent_surrey_hc_list_5", "Guildford County Court and Family Court");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, KENT, KENTFRC_COURTLIST,
            "FR_kent_surrey_hc_list_6", "Staines County Court and Family Court");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, KENT, KENTFRC_COURTLIST,
            "FR_kent_surrey_hc_list_7", "Brighton County and Family Court");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, KENT, KENTFRC_COURTLIST,
            "FR_kent_surrey_hc_list_8", "Worthing County Court and Family Court");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, KENT, KENTFRC_COURTLIST,
            "FR_kent_surrey_hc_list_9", "Hastings County Court and Family Court Hearing Centre");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, KENT, KENTFRC_COURTLIST,
            "FR_kent_surrey_hc_list_10", "Horsham County Court and Family Court");

        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, "invalid", KENTFRC_COURTLIST,
            "FR_kent_surrey_hc_list_10", "");


        verifyCorrectCourtReturned(SOUTHEAST, SOUTHEAST_FRC_LIST, KENT, KENTFRC_COURTLIST,
            "invalid", "");
    }

    @Test
    public void cleavelandCourts() {
        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, CLEAVELAND, CLEAVELAND_COURTLIST,
            "FR_cleaveland_hc_list_1", "Newcastle upon Tyne Justice Centre");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, CLEAVELAND, CLEAVELAND_COURTLIST,
            "FR_cleaveland_hc_list_2", "Durham Justice Centre");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, CLEAVELAND, CLEAVELAND_COURTLIST,
            "FR_cleaveland_hc_list_3", "Sunderland County and Family Court");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, CLEAVELAND, CLEAVELAND_COURTLIST,
            "FR_cleaveland_hc_list_4", "Middlesbrough County Court at Teesside Combined Court");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, CLEAVELAND, CLEAVELAND_COURTLIST,
            "FR_cleaveland_hc_list_5", "Gateshead County Court and Family Court");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, CLEAVELAND, CLEAVELAND_COURTLIST,
            "FR_cleaveland_hc_list_6", "South Shields County Court and Family Court");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, CLEAVELAND, CLEAVELAND_COURTLIST,
            "FR_cleaveland_hc_list_7", "North Shields County Court and Family Court");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, CLEAVELAND, CLEAVELAND_COURTLIST,
            "FR_cleaveland_hc_list_8", "Darlington County Court and Family Court");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, "invalid", CLEAVELAND_COURTLIST,
            "FR_cleaveland_hc_list_8", "");


        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, CLEAVELAND, CLEAVELAND_COURTLIST,
            "invalid", "");
    }

    @Test
    public void nwYorkshireCourts() {
        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, NWYORKSHIRE, NWYORKSHIRE_COURTLIST,
            "FR_nw_yorkshire_hc_list_1", "Harrogate Justice Centre");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, NWYORKSHIRE, NWYORKSHIRE_COURTLIST,
            "FR_nw_yorkshire_hc_list_2", "Bradford Combined Court Centre");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, NWYORKSHIRE, NWYORKSHIRE_COURTLIST,
            "FR_nw_yorkshire_hc_list_3", "Huddersfield County Court and Family Court");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, NWYORKSHIRE, NWYORKSHIRE_COURTLIST,
            "FR_nw_yorkshire_hc_list_4", "Wakefield Civil and Family Justice Centre");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, NWYORKSHIRE, NWYORKSHIRE_COURTLIST,
            "FR_nw_yorkshire_hc_list_5", "York County Court and Family Court");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, NWYORKSHIRE, NWYORKSHIRE_COURTLIST,
            "FR_nw_yorkshire_hc_list_6", "Scarborough Justice Centre");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, NWYORKSHIRE, NWYORKSHIRE_COURTLIST,
            "FR_nw_yorkshire_hc_list_7", "Skipton County Court and Family Court");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, NWYORKSHIRE, NWYORKSHIRE_COURTLIST,
            "FR_nw_yorkshire_hc_list_8", "Leeds Combined Court Centre");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, "invalid", "",
            "", "");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, NWYORKSHIRE, "",
            "invalid", "");
    }

    @Test
    public void hsYorkshireCourts() {
        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, HSYORKSHIRE, HSYORKSHIRE_COURTLIST,
            "FR_humber_hc_list_1", "Sheffield Family Hearing Centre");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, HSYORKSHIRE, HSYORKSHIRE_COURTLIST,
            "FR_humber_hc_list_2", "Kingston-upon-Hull Combined Court Centre");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, HSYORKSHIRE, HSYORKSHIRE_COURTLIST,
            "FR_humber_hc_list_3", "Doncaster Justice Centre North");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, HSYORKSHIRE, HSYORKSHIRE_COURTLIST,
            "FR_humber_hc_list_4", "Great Grimsby Combined Court Centre");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, HSYORKSHIRE, HSYORKSHIRE_COURTLIST,
            "FR_humber_hc_list_5", "Barnsley Law Courts");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, "invalid", HSYORKSHIRE_COURTLIST,
            "FR_humber_hc_list_5", "");

        verifyCorrectCourtReturned(NORTHEAST, NORTHEAST_FRC_LIST, HSYORKSHIRE, HSYORKSHIRE_COURTLIST,
            "invalid", "");
    }

    @Test
    public void liverpoolCourtList() {
        verifyCorrectCourtReturned(NORTHWEST, NORTHWEST_FRC_LIST, LIVERPOOL, LIVERPOOL_COURTLIST,
            "FR_liverpool_hc_list_1", "Liverpool Civil and Family Court");

        verifyCorrectCourtReturned(NORTHWEST, NORTHWEST_FRC_LIST, LIVERPOOL, LIVERPOOL_COURTLIST,
            "FR_liverpool_hc_list_2", "Chester Civil and Family Justice Centre");

        verifyCorrectCourtReturned(NORTHWEST, NORTHWEST_FRC_LIST, LIVERPOOL, LIVERPOOL_COURTLIST,
            "FR_liverpool_hc_list_3", "Crewe County Court and Family Court");

        verifyCorrectCourtReturned(NORTHWEST, NORTHWEST_FRC_LIST, LIVERPOOL, LIVERPOOL_COURTLIST,
            "FR_liverpool_hc_list_4", "St. Helens County Court and Family Court");

        verifyCorrectCourtReturned(NORTHWEST, NORTHWEST_FRC_LIST, LIVERPOOL, LIVERPOOL_COURTLIST,
            "FR_liverpool_hc_list_5", "Birkenhead County Court and Family Court");

        verifyCorrectCourtReturned(NORTHWEST, NORTHWEST_FRC_LIST, "invalid", LIVERPOOL_COURTLIST,
            "FR_liverpool_hc_list_1", "");

        verifyCorrectCourtReturned(NORTHWEST, NORTHWEST_FRC_LIST, LIVERPOOL, LIVERPOOL_COURTLIST,
            "invalid", "");
    }

    @Test
    public void manchesterCourtList() {
        verifyCorrectCourtReturned(NORTHWEST, NORTHWEST_FRC_LIST, MANCHESTER, MANCHESTER_COURTLIST,
            "FR_manchester_hc_list_1", "Manchester County and Family Court");

        verifyCorrectCourtReturned(NORTHWEST, NORTHWEST_FRC_LIST, MANCHESTER, MANCHESTER_COURTLIST,
            "FR_manchester_hc_list_2", "Stockport County Court and Family Court");

        verifyCorrectCourtReturned(NORTHWEST, NORTHWEST_FRC_LIST, MANCHESTER, MANCHESTER_COURTLIST,
            "FR_manchester_hc_list_3", "Wigan County Court and Family Court");

        verifyCorrectCourtReturned(NORTHWEST, NORTHWEST_FRC_LIST, MANCHESTER, MANCHESTER_COURTLIST,
            "invalid", "");

        verifyCorrectCourtReturned(NORTHWEST, NORTHWEST_FRC_LIST, "invalid", MANCHESTER_COURTLIST,
            "FR_manchester_hc_list_3", "");
    }

    @Test
    public void londonCourtListTest() {
        verifyCorrectCourtReturned(LONDON, LONDON_FRC_LIST, LONDON_CFC, CFC_COURTLIST,
            "FR_s_CFCList_1", "Bromley County Court and Family Court");

        verifyCorrectCourtReturned(LONDON, LONDON_FRC_LIST, LONDON_CFC, CFC_COURTLIST,
            "FR_s_CFCList_2", "Croydon County Court and Family Court");

        verifyCorrectCourtReturned(LONDON, LONDON_FRC_LIST, LONDON_CFC, CFC_COURTLIST,
            "FR_s_CFCList_3", "Edmonton County Court and Family Court");

        verifyCorrectCourtReturned(LONDON, LONDON_FRC_LIST, LONDON_CFC, CFC_COURTLIST,
            "FR_s_CFCList_4", "Kingston-upon-thames County Court and Family Court");

        verifyCorrectCourtReturned(LONDON, LONDON_FRC_LIST, LONDON_CFC, CFC_COURTLIST,
            "FR_s_CFCList_5", "Romford County and Family Court");

        verifyCorrectCourtReturned(LONDON, LONDON_FRC_LIST, LONDON_CFC, CFC_COURTLIST,
            "FR_s_CFCList_6", "Barnet Civil and Family Courts Centre");

        verifyCorrectCourtReturned(LONDON, LONDON_FRC_LIST, LONDON_CFC, CFC_COURTLIST,
            "FR_s_CFCList_8", "Brentford County and Family Court");

        verifyCorrectCourtReturned(LONDON, LONDON_FRC_LIST, LONDON_CFC, CFC_COURTLIST,
            "FR_s_CFCList_9", "Central Family Court");

        verifyCorrectCourtReturned(LONDON, LONDON_FRC_LIST, LONDON_CFC, CFC_COURTLIST,
            "FR_s_CFCList_11", "East London Family Court");

        verifyCorrectCourtReturned(LONDON, LONDON_FRC_LIST, LONDON_CFC, CFC_COURTLIST,
            "FR_s_CFCList_14", "Uxbridge County Court and Family Court");

        verifyCorrectCourtReturned(LONDON, LONDON_FRC_LIST, LONDON_CFC, CFC_COURTLIST,
            "FR_s_CFCList_16", "Willesden County Court and Family Court");

        verifyCorrectCourtReturned(LONDON, LONDON_FRC_LIST, "invalid", CFC_COURTLIST,
            "FR_s_CFCList_1", "");

        verifyCorrectCourtReturned(LONDON, LONDON_FRC_LIST, LONDON_CFC, CFC_COURTLIST,
            "invalid", "");
    }

    @Test
    public void birminghamCourtListTest() {
        verifyCorrectCourtReturned(MIDLANDS, MIDLANDS_FRC_LIST, BIRMINGHAM, BIRMINGHAM_COURTLIST,
            "FR_birmingham_hc_list_1", "Birmingham Civil and Family Justice Centre");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDS_FRC_LIST, BIRMINGHAM, BIRMINGHAM_COURTLIST,
            "FR_birmingham_hc_list_2", "Coventry Combined Court Centre");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDS_FRC_LIST, BIRMINGHAM, BIRMINGHAM_COURTLIST,
            "FR_birmingham_hc_list_3", "Telford County Court and Family Court");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDS_FRC_LIST, BIRMINGHAM, BIRMINGHAM_COURTLIST,
            "FR_birmingham_hc_list_4", "Wolverhampton Combined Court Centre");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDS_FRC_LIST, BIRMINGHAM, BIRMINGHAM_COURTLIST,
            "FR_birmingham_hc_list_5", "Dudley County Court and Family Court");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDS_FRC_LIST, BIRMINGHAM, BIRMINGHAM_COURTLIST,
            "FR_birmingham_hc_list_6", "Walsall County and Family Court");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDS_FRC_LIST, BIRMINGHAM, BIRMINGHAM_COURTLIST,
            "FR_birmingham_hc_list_7", "Stoke on Trent Combined Court");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDS_FRC_LIST, BIRMINGHAM, BIRMINGHAM_COURTLIST,
            "FR_birmingham_hc_list_8", "Worcester Combined Court");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDS_FRC_LIST, BIRMINGHAM, BIRMINGHAM_COURTLIST,
            "FR_birmingham_hc_list_9", "Stafford Combined Court");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDS_FRC_LIST, BIRMINGHAM, BIRMINGHAM_COURTLIST,
            "FR_birmingham_hc_list_10", "Hereford County Court and Family Court");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDS_FRC_LIST, BIRMINGHAM, BIRMINGHAM_COURTLIST,
            "invalid", "");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDS_FRC_LIST, "invalid", BIRMINGHAM_COURTLIST,
            "FR_birmingham_hc_list_10", "");

    }

    @Test
    public void nottinghamCourtListTest() {

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDS_FRC_LIST, NOTTINGHAM, NOTTINGHAM_COURTLIST,
            "FR_s_NottinghamList_1", "Nottingham County Court and Family Court");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDS_FRC_LIST, NOTTINGHAM, NOTTINGHAM_COURTLIST,
            "FR_s_NottinghamList_2", "Derby Combined Court Centre");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDS_FRC_LIST, NOTTINGHAM, NOTTINGHAM_COURTLIST,
            "FR_s_NottinghamList_3", "Leicester County Court and Family Court");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDS_FRC_LIST, NOTTINGHAM, NOTTINGHAM_COURTLIST,
            "FR_s_NottinghamList_4", "Lincoln County Court and Family Court");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDS_FRC_LIST, NOTTINGHAM, NOTTINGHAM_COURTLIST,
            "FR_s_NottinghamList_5", "Northampton Crown, County and Family Court");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDS_FRC_LIST, NOTTINGHAM, NOTTINGHAM_COURTLIST,
            "FR_s_NottinghamList_6", "Chesterfield County Court");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDS_FRC_LIST, NOTTINGHAM, NOTTINGHAM_COURTLIST,
            "FR_s_NottinghamList_7", "Mansfield Magistrates and County Court");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDS_FRC_LIST, NOTTINGHAM, NOTTINGHAM_COURTLIST,
            "FR_s_NottinghamList_8", "Boston County Court and Family Court");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDS_FRC_LIST, NOTTINGHAM, NOTTINGHAM_COURTLIST,
            "INVALID", "");

        verifyCorrectCourtReturned(MIDLANDS, MIDLANDS_FRC_LIST, "invalid", NOTTINGHAM_COURTLIST,
            "FR_s_NottinghamList_8", "");
    }


    private CaseDetails getCaseDetailsWithAllocatedValues(String region, String subRegionListName, String subRegion,
                                                          String courtListName, String court) {
        CaseDetails details = defaultCaseDetails();
        details.getData().put(REGION, region);
        details.getData().put(subRegionListName, subRegion);
        details.getData().put(courtListName, court);
        return details;
    }

    private void verifyCorrectCourtReturned(String region, String subRegionListName, String subRegion,
                                                          String courtListName, String court, String expectedValue) {
        details = getCaseDetailsWithAllocatedValues(region, subRegionListName, subRegion, courtListName,
            court);
        assertThat(ContestedCourtHelper.getSelectedCourt(details), is(expectedValue));
    }
}
