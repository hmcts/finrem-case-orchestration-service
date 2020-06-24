package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import com.google.common.collect.ImmutableMap;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ALLOCATED_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BIRMINGHAM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BIRMINGHAM_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CLEAVELAND;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CLEAVELAND_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HSYORKSHIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HSYORKSHIRE_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.KENTFRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.KENTFRC_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LIVERPOOL_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON_LIST;
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

public class ContestedCourtHelper {

    public static String getSelectedCourt(CaseDetails caseDetails) {

        Map<String, Object> caseData = caseDetails.getData();
        Object allocatedCourtList = caseData.get(ALLOCATED_COURT_LIST);
        HashMap<String, Object> allocatedCourtMap = (HashMap<String, Object>) allocatedCourtList;
        String region = (String) allocatedCourtMap.get(REGION);

        if (MIDLANDS.equalsIgnoreCase(region)) {
            return getMidlandFRC(allocatedCourtMap);
        }
        if (LONDON.equalsIgnoreCase(region)) {
            return getLondonFRC(allocatedCourtMap);
        }
        if (NORTHWEST.equalsIgnoreCase(region)) {
            return getNorthWestFRC(allocatedCourtMap);
        }
        if (NORTHEAST.equalsIgnoreCase(region)) {
            return getNorthEastFRC(allocatedCourtMap);
        }
        if (SOUTHEAST.equalsIgnoreCase(region)) {
            return getSouthEastFRC(allocatedCourtMap);
        }
        if (WALES.equalsIgnoreCase(region)) {
            return getWalesFRC(allocatedCourtMap);
        }
        return EMPTY;
    }

    private static String getWalesFRC(Map allocatedCourtMap) {
        String walesList = (String) allocatedCourtMap.get(WALESLIST);
        if (NEWPORT.equalsIgnoreCase(walesList)) {
            return getNewportCourt(allocatedCourtMap);
        } else if (SWANSEA.equalsIgnoreCase(walesList)) {
            return getSwanseaCourt(allocatedCourtMap);
        }
        return EMPTY;
    }

    private static String getSouthEastFRC(Map allocatedCourtMap) {
        String southEastList = (String) allocatedCourtMap.get(SOUTHEASTLIST);
        if (KENTFRC.equalsIgnoreCase(southEastList)) {
            return getKentCourt(allocatedCourtMap);
        }
        return EMPTY;
    }

    private static String getNorthEastFRC(Map allocatedCourtMap) {
        String northEastList = (String) allocatedCourtMap.get(NORTHEASTLIST);
        if (CLEAVELAND.equalsIgnoreCase(northEastList)) {
            return getCleavelandCourt(allocatedCourtMap);
        } else if (NWYORKSHIRE.equalsIgnoreCase(northEastList)) {
            return getNWYorkshireCourt(allocatedCourtMap);
        } else if (HSYORKSHIRE.equalsIgnoreCase(northEastList)) {
            return getHumberCourt(allocatedCourtMap);
        }
        return EMPTY;
    }

    private static String getNorthWestFRC(Map allocatedCourtMap) {
        String northWestList = (String) allocatedCourtMap.get(NORTHWESTLIST);
        if ("liverpool".equalsIgnoreCase(northWestList)) {
            return getLiverpoolCourt(allocatedCourtMap);
        } else if ("manchester".equalsIgnoreCase(northWestList)) {
            return getManchesterCourt(allocatedCourtMap);
        }
        return EMPTY;
    }

    private static String getLondonFRC(Map allocatedCourtMap) {
        String londonList = (String) allocatedCourtMap.get(LONDON_LIST);
        if ("cfc".equalsIgnoreCase(londonList)) {
            return getLondonCourt(allocatedCourtMap);
        }
        return EMPTY;
    }

    private static String getMidlandFRC(Map allocatedCourtMap) {
        String midlandsList = (String) allocatedCourtMap.get(MIDLANDSLIST);
        if (NOTTINGHAM.equalsIgnoreCase(midlandsList)) {
            return getNottinghamCourt(allocatedCourtMap);
        } else if (BIRMINGHAM.equalsIgnoreCase(midlandsList)) {
            return getBirminghamCourt(allocatedCourtMap);
        }
        return EMPTY;
    }

    public static String getNottinghamCourt(Map allocatedCourtMap) {
        return nottinghamMap.getOrDefault(allocatedCourtMap.get(NOTTINGHAM_COURTLIST), "");
    }

    private static Map<String, String> nottinghamMap = ImmutableMap.<String, String>builder()
        .put("FR_s_NottinghamList_1", "NOTTINGHAM COUNTY COURT AND FAMILY COURT")
        .put("FR_s_NottinghamList_2", "DERBY COMBINED COURT CENTRE")
        .put("FR_s_NottinghamList_3", "LEICESTER COUNTY COURT AND FAMILY COURT")
        .put("FR_s_NottinghamList_4", "LINCOLN COUNTY COURT AND FAMILY COURT")
        .put("FR_s_NottinghamList_5", "NORTHAMPTON CROWN, COUNTY AND FAMILY COURT")
        .put("FR_s_NottinghamList_6", "CHESTERFIELD COUNTY COURT")
        .put("FR_s_NottinghamList_7", "MANSFIELD MAGISTRATES AND COUNTY COURT")
        .put("FR_s_NottinghamList_8", "BOSTON COUNTY COURT AND FAMILY COURT")
        .build();

    public static String getBirminghamCourt(Map allocatedCourtMap) {
        return birminghamMap.getOrDefault(allocatedCourtMap.get(BIRMINGHAM_COURTLIST), "");
    }

    private static Map<String, String> birminghamMap = ImmutableMap.<String, String>builder()
        .put("FR_birmingham_hc_list_1", "BIRMINGHAM CIVIL AND FAMILY JUSTICE CENTRE")
        .put("FR_birmingham_hc_list_2", "COVENTRY COMBINED COURT CENTRE")
        .put("FR_birmingham_hc_list_3", "TELFORD COUNTY COURT AND FAMILY COURT")
        .put("FR_birmingham_hc_list_4", "WOLVERHAMPTON COMBINED COURT CENTRE")
        .put("FR_birmingham_hc_list_5", "DUDLEY COUNTY COURT AND FAMILY COURT")
        .put("FR_birmingham_hc_list_6", "WALSALL COUNTY AND FAMILY COURT")
        .put("FR_birmingham_hc_list_7", "STOKE ON TRENT COMBINED COURT")
        .put("FR_birmingham_hc_list_8", "WORCESTER COMBINED COURT")
        .put("FR_birmingham_hc_list_9", "STAFFORD COMBINED COURT")
        .put("FR_birmingham_hc_list_10", "HEREFORD COUNTY COURT AND FAMILY COURT")
        .build();

    public static String getLondonCourt(Map allocatedCourtMap) {
        return londonMap.getOrDefault(allocatedCourtMap.get(LONDON_COURTLIST), "");
    }

    private static Map<String, String> londonMap = ImmutableMap.<String, String>builder()
        .put("FR_s_CFCList_1", "BROMLEY COUNTY COURT AND FAMILY COURT")
        .put("FR_s_CFCList_2", "CROYDON COUNTY COURT AND FAMILY COURT")
        .put("FR_s_CFCList_3", "EDMONTON COUNTY COURT AND FAMILY COURT")
        .put("FR_s_CFCList_4", "KINGSTON-UPON-THAMES COUNTY COURT AND FAMILY COURT")
        .put("FR_s_CFCList_5", "ROMFORD COUNTY AND FAMILY COURT")
        .put("FR_s_CFCList_6", "BARNET CIVIL AND FAMILY COURTS CENTRE")
        .put("FR_s_CFCList_8", "BRENTFORD COUNTY AND FAMILY COURT")
        .put("FR_s_CFCList_9", "CENTRAL FAMILY COURT")
        .put("FR_s_CFCList_11", "EAST LONDON FAMILY COURT")
        .put("FR_s_CFCList_14", "UXBRIDGE COUNTY COURT AND FAMILY COURT")
        .put("FR_s_CFCList_16", "WILLESDEN COUNTY COURT AND FAMILY COURT")
        .build();

    public static String getLiverpoolCourt(Map allocatedCourtMap) {
        return liverpoolMap.getOrDefault(allocatedCourtMap.get(LIVERPOOL_COURTLIST), "");
    }

    private static Map<String, String> liverpoolMap = ImmutableMap.<String, String>builder()
        .put("FR_liverpool_hc_list_1", "LIVERPOOL CIVIL AND FAMILY COURT")
        .put("FR_liverpool_hc_list_2", "CHESTER CIVIL AND FAMILY JUSTICE CENTRE")
        .put("FR_liverpool_hc_list_3", "CREWE COUNTY COURT AND FAMILY COURT ")
        .put("FR_liverpool_hc_list_4", "ST. HELENS COUNTY COURT AND FAMILY COURT ")
        .put("FR_liverpool_hc_list_5", "BIRKENHEAD COUNTY COURT AND FAMILY COURT")
        .build();

    public static String getManchesterCourt(Map allocatedCourtMap) {
        return manchesterMap.getOrDefault(allocatedCourtMap.get(MANCHESTER_COURTLIST), "");
    }

    private static Map<String, String> manchesterMap = ImmutableMap.<String, String>builder()
        .put("FR_manchester_hc_list_1", "MANCHESTER COUNTY AND FAMILY COURT ")
        .put("FR_manchester_hc_list_2", "STOCKPORT COUNTY COURT AND FAMILY COURT")
        .put("FR_manchester_hc_list_3", "WIGAN COUNTY COURT AND FAMILY COURT ")
        .build();

    public static String getCleavelandCourt(Map allocatedCourtMap) {
        return cleavelandMap.getOrDefault(allocatedCourtMap.get(CLEAVELAND_COURTLIST), "");
    }

    private static Map<String, String> cleavelandMap = ImmutableMap.<String, String>builder()
        .put("FR_cleaveland_hc_list_1", "NEWCASTLE UPON TYNE JUSTICE CENTRE")
        .put("FR_cleaveland_hc_list_2", "DURHAM JUSTICE CENTRE")
        .put("FR_cleaveland_hc_list_3", "SUNDERLAND COUNTY AND FAMILY COURT")
        .put("FR_cleaveland_hc_list_4", "MIDDLESBROUGH COUNTY COURT AT TEESSIDE COMBINED COURT")
        .put("FR_cleaveland_hc_list_5", "GATESHEAD COUNTY COURT AND FAMILY COURT")
        .put("FR_cleaveland_hc_list_6", "SOUTH SHIELDS COUNTY COURT AND FAMILY COURT")
        .put("FR_cleaveland_hc_list_7", "NORTH SHIELDS COUNTY COURT AND FAMILY COURT")
        .put("FR_cleaveland_hc_list_8", "DARLINGTON COUNTY COURT AND FAMILY COURT")
        .build();

    public static String getNWYorkshireCourt(Map allocatedCourtMap) {
        return yorkshireMap.getOrDefault(allocatedCourtMap.get(NWYORKSHIRE_COURTLIST), "");
    }

    private static Map<String, String> yorkshireMap = ImmutableMap.<String, String>builder()
        .put("FR_nw_yorkshire_hc_list_1", "HARROGATE JUSTICE CENTRE")
        .put("FR_nw_yorkshire_hc_list_2", "BRADFORD COMBINED COURT CENTRE")
        .put("FR_nw_yorkshire_hc_list_3", "HUDDERSFIELD COUNTY COURT AND FAMILY COURT")
        .put("FR_nw_yorkshire_hc_list_4", "WAKEFIELD CIVIL AND FAMILY JUSTICE CENTRE")
        .put("FR_nw_yorkshire_hc_list_5", "YORK COUNTY COURT AND FAMILY COURT")
        .put("FR_nw_yorkshire_hc_list_6", "SCARBOROUGH JUSTICE CENTRE")
        .put("FR_nw_yorkshire_hc_list_7", "SKIPTON COUNTY COURT AND FAMILY COURT")
        .put("FR_nw_yorkshire_hc_list_8", "LEEDS COMBINED COURT CENTRE")
        .build();


    public static String getHumberCourt(Map allocatedCourtMap) {
        return humberMap.getOrDefault(allocatedCourtMap.get(HSYORKSHIRE_COURTLIST), "");
    }

    private static Map<String, String> humberMap = ImmutableMap.<String, String>builder()
        .put("FR_humber_hc_list_1", "SHEFFIELD FAMILY HEARING CENTRE")
        .put("FR_humber_hc_list_2", "KINGSTON-UPON-HULL COMBINED COURT CENTRE")
        .put("FR_humber_hc_list_3", "DONCASTER JUSTICE CENTRE NORTH")
        .put("FR_humber_hc_list_4", "GREAT GRIMSBY COMBINED COURT CENTRE")
        .put("FR_humber_hc_list_5", "BARNSLEY LAW COURTS")
        .build();

    public static String getKentCourt(Map allocatedCourtMap) {
        return kentMap.getOrDefault(allocatedCourtMap.get(KENTFRC_COURTLIST), "");
    }

    private static Map<String, String> kentMap = ImmutableMap.<String, String>builder()
        .put("FR_kent_surrey_hc_list_1", "CANTERBURY FAMILY COURT HEARING CENTRE")
        .put("FR_kent_surrey_hc_list_2", "MAIDSTONE COMBINED COURT CENTRE")
        .put("FR_kent_surrey_hc_list_3", "DARTFORD COUNTY COURT AND FAMILY COURT")
        .put("FR_kent_surrey_hc_list_4", "MEDWAY COUNTY COURT AND FAMILY COURT")
        .put("FR_kent_surrey_hc_list_5", "GUILDFORD COUNTY COURT AND FAMILY COURT")
        .put("FR_kent_surrey_hc_list_6", "STAINES COUNTY COURT AND FAMILY COURT")
        .put("FR_kent_surrey_hc_list_7", "BRIGHTON COUNTY AND FAMILY COURT")
        .put("FR_kent_surrey_hc_list_8", "WORTHING COUNTY COURT AND FAMILY COURT")
        .put("FR_kent_surrey_hc_list_9", "HASTINGS COUNTY COURT AND FAMILY COURT HEARING CENTRE")
        .put("FR_kent_surrey_hc_list_10", "HORSHAM COUNTY COURT AND FAMILY COURT")
        .build();

    public static String getNewportCourt(Map allocatedCourtMap) {
        return newportMap.getOrDefault(allocatedCourtMap.get(NEWPORT_COURTLIST), "");
    }

    private static Map<String, String> newportMap = ImmutableMap.<String, String>builder()
        .put("FR_newport_hc_list_1", "NEWPORT CIVIL AND FAMILY COURT")
        .put("FR_newport_hc_list_2", "CARDIFF CIVIL & FAMILY JUSTICE CENTRE")
        .put("FR_newport_hc_list_3", "MERTHYR TYDFIL COMBINED COURT CENTRE")
        .put("FR_newport_hc_list_4", "PONTYPRIDD COUNTY AND FAMILY COURT")
        .put("FR_newport_hc_list_5", "BLACKWOOD CIVIL AND FAMILY COURT")
        .build();

    public static String getSwanseaCourt(Map allocatedCourtMap) {
        return swanseaMap.getOrDefault(allocatedCourtMap.get(SWANSEA_COURTLIST), "");
    }

    private static Map<String, String> swanseaMap = ImmutableMap.<String, String>builder()
        .put("FR_swansea_hc_list_1", "SWANSEA CIVIL & FAMILY JUSTICE CENTRE")
        .put("FR_swansea_hc_list_2", "ABERYSTWYTH JUSTICE CENTRE")
        .put("FR_swansea_hc_list_3", "HAVERFORDWEST COUNTY & FAMILY COURT")
        .put("FR_swansea_hc_list_4", "CARMARTHEN COUNTY AND FAMILY COURT")
        .put("FR_swansea_hc_list_5", "LLANELLI LAW COURTS")
        .put("FR_swansea_hc_list_6", "PORT TALBOT JUSTICE CENTRE")
        .build();
}
