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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HSYORKSHIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LIVERPOOL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LIVERPOOL_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON_CFC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MANCHESTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIDLANDS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIDLANDSLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHEAST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHEASTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHWEST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHWESTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTTINGHAM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTTINGHAM_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NWYORKSHIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.REGION;

public class ContestedCourtHelperTest {

    private CaseDetails details;

    /*
   northeast
    	northEastList
    		if (CLEAVELAND.equalsIgnoreCase(northEastList)) {
            return CLEAVELAND;
        } else if (NWYORKSHIRE.equalsIgnoreCase(northEastList)) {
            return NWYORKSHIRE;
        } else if (HSYORKSHIRE.equalsIgnoreCase(northEastList)) {
            return HSYORKSHIRE;
     */


    @Test
    public void northEastCourts() {
        verifyCorrectCourtReturned(NORTHEAST, NORTHEASTLIST, CLEAVELAND, "",
            "", CLEAVELAND);

        verifyCorrectCourtReturned(NORTHEAST, NORTHEASTLIST, NWYORKSHIRE, "",
            "", NWYORKSHIRE);

        verifyCorrectCourtReturned(NORTHEAST, NORTHEASTLIST, HSYORKSHIRE, "",
            "", HSYORKSHIRE);

        verifyCorrectCourtReturned(NORTHEAST, NORTHEASTLIST, "invalid", "",
            "", "");

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
        verifyCorrectCourtReturned(NORTHWEST, NORTHWESTLIST, MANCHESTER, "",
            "", "manchester");
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
