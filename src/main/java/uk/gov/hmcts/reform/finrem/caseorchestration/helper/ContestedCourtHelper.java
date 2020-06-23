package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ALLOCATED_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BIRMINGHAM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CLEAVELAND;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HSYORKSHIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.KENTFRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NEWPORT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTTINGHAM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NWYORKSHIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.REGION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SWANSEA;

public class ContestedCourtHelper {

    public static String getSelectedCourt(CaseDetails caseDetails) {

        Map<String, Object> caseData = caseDetails.getData();
        Object allocatedCourtList = caseData.get(ALLOCATED_COURT_LIST);
        HashMap<String, Object> allocatedCourtMap = (HashMap<String, Object>) allocatedCourtList;
        String region = (String) allocatedCourtMap.get(REGION);
        if ("midlands".equalsIgnoreCase(region)) {
            return getMidlandFRC(allocatedCourtMap);
        }
        if ("london".equalsIgnoreCase(region)) {
            return getLondonFRC(allocatedCourtMap);
        }
        if ("northwest".equalsIgnoreCase(region)) {
            return getNorthWestFRC(allocatedCourtMap);
        }
        if ("northeast".equalsIgnoreCase(region)) {
            return getNorthEastFRC(allocatedCourtMap);
        }
        if ("southeast".equalsIgnoreCase(region)) {
            return getSouthEastFRC(allocatedCourtMap);
        } else if ("wales".equalsIgnoreCase(region)) {
            return getWalesFRC(allocatedCourtMap);
        }
        return EMPTY;
    }

    private static String getWalesFRC(Map allocatedCourtMap) {
        String walesList = (String) allocatedCourtMap.get("walesList");
        if (NEWPORT.equalsIgnoreCase(walesList)) {
            return NEWPORT;
        } else if (SWANSEA.equalsIgnoreCase(walesList)) {
            return SWANSEA;
        }
        return EMPTY;
    }

    private static String getSouthEastFRC(Map allocatedCourtMap) {
        String southEastList = (String) allocatedCourtMap.get("southEastList");
        if (KENTFRC.equalsIgnoreCase(southEastList)) {
            return KENTFRC;
        }
        return EMPTY;
    }

    private static String getNorthEastFRC(Map allocatedCourtMap) {
        String northEastList = (String) allocatedCourtMap.get("northEastList");
        if (CLEAVELAND.equalsIgnoreCase(northEastList)) {
            return CLEAVELAND;
        } else if (NWYORKSHIRE.equalsIgnoreCase(northEastList)) {
            return NWYORKSHIRE;
        } else if (HSYORKSHIRE.equalsIgnoreCase(northEastList)) {
            return HSYORKSHIRE;
        }
        return EMPTY;
    }

    private static String getNorthWestFRC(Map allocatedCourtMap) {
        String northWestList = (String) allocatedCourtMap.get("northWestList");
        if ("liverpool".equalsIgnoreCase(northWestList)) {
            return "liverpool";
        } else if ("manchester".equalsIgnoreCase(northWestList)) {
            return "manchester";
        }
        return EMPTY;
    }

    private static String getLondonFRC(Map allocatedCourtMap) {
        String londonList = (String) allocatedCourtMap.get("londonList");
        if ("cfc".equalsIgnoreCase(londonList)) {
            return getLondonCourtNames(allocatedCourtMap);
        }
        return EMPTY;
    }

    private static String getMidlandFRC(Map allocatedCourtMap) {
        String midlandsList = (String) allocatedCourtMap.get("midlandsList");
        if (NOTTINGHAM.equalsIgnoreCase(midlandsList)) {
            return getNottinghamCourtNames(allocatedCourtMap);
        } else if (BIRMINGHAM.equalsIgnoreCase(midlandsList)) {
            return getBirminghamCourtNames(allocatedCourtMap);
        }
        return EMPTY;
    }

    private static String getNottinghamCourtNames(Map allocatedCourtMap) {
        String courtItem = (String) allocatedCourtMap.get("nottinghamCourtList");
        if (courtItem.equalsIgnoreCase("FR_s_NottinghamList_1")) {
            return "NOTTINGHAM COUNTY COURT AND FAMILY COURT";
        } else if (courtItem.equalsIgnoreCase("FR_s_NottinghamList_2")) {
            return "DERBY COMBINED COURT CENTRE";
        } else if (courtItem.equalsIgnoreCase("FR_s_NottinghamList_3")) {
            return "LEICESTER COUNTY COURT AND FAMILY COURT";
        } else if (courtItem.equalsIgnoreCase("FR_s_NottinghamList_4")) {
            return "LINCOLN COUNTY COURT AND FAMILY COURT";
        } else if (courtItem.equalsIgnoreCase("FR_s_NottinghamList_5")) {
            return "NORTHAMPTON CROWN, COUNTY AND FAMILY COURT";
        } else if (courtItem.equalsIgnoreCase("FR_s_NottinghamList_6")) {
            return "CHESTERFIELD COUNTY COURT";
        } else if (courtItem.equalsIgnoreCase("FR_s_NottinghamList_7")) {
            return "MANSFIELD MAGISTRATES AND COUNTY COURT";
        } else if (courtItem.equalsIgnoreCase("FR_s_NottinghamList_8")) {
            return "BOSTON COUNTY COURT AND FAMILY COURT";
        }
        return EMPTY;
    }

    private static String getBirminghamCourtNames(Map allocatedCourtMap) {
        String courtItem = (String) allocatedCourtMap.get("FR_birmingham_hc_list");
        if (courtItem.equalsIgnoreCase("FR_birmingham_hc_list_1")) {
            return "BIRMINGHAM CIVIL AND FAMILY JUSTICE CENTRE";
        } else if (courtItem.equalsIgnoreCase("FR_birmingham_hc_list_2")) {
            return "COVENTRY COMBINED COURT CENTRE";
        } else if (courtItem.equalsIgnoreCase("FR_birmingham_hc_list_3")) {
            return "TELFORD COUNTY COURT AND FAMILY COURT";
        } else if (courtItem.equalsIgnoreCase("FR_birmingham_hc_list_4")) {
            return "WOLVERHAMPTON COMBINED COURT CENTRE";
        } else if (courtItem.equalsIgnoreCase("FR_birmingham_hc_list_5")) {
            return "DUDLEY COUNTY COURT AND FAMILY COURT";
        } else if (courtItem.equalsIgnoreCase("FR_birmingham_hc_list_6")) {
            return "WALSALL COUNTY AND FAMILY COURT";
        } else if (courtItem.equalsIgnoreCase("FR_birmingham_hc_list_7")) {
            return "STOKE ON TRENT COMBINED COURT";
        } else if (courtItem.equalsIgnoreCase("FR_birmingham_hc_list_8")) {
            return "WORCESTER COMBINED COURT";
        } else if (courtItem.equalsIgnoreCase("FR_birmingham_hc_list_9")) {
            return "STAFFORD COMBINED COURT";
        } else if (courtItem.equalsIgnoreCase("FR_birmingham_hc_list_10")) {
            return "HEREFORD COUNTY COURT AND FAMILY COURT";
        }
        return EMPTY;
    }

    private static String getLondonCourtNames(Map allocatedCourtMap) {
        String courtItem = (String) allocatedCourtMap.get("FR_s_CFCList");
        if (courtItem.equalsIgnoreCase("FR_s_CFCList_1")) {
            return "BROMLEY COUNTY COURT AND FAMILY COURT";
        } else if (courtItem.equalsIgnoreCase("FR_s_CFCList_2")) {
            return "CROYDON COUNTY COURT AND FAMILY COURT";
        }
        return EMPTY;
    }
}
