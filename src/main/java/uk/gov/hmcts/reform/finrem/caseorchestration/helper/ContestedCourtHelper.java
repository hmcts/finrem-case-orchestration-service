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
            return getNwYorkshireCourt(allocatedCourtMap);
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
        .put("FR_s_NottinghamList_1", "Nottingham County Court and Family Court")
        .put("FR_s_NottinghamList_2", "Derby Combined Court Centre")
        .put("FR_s_NottinghamList_3", "Leicester County Court and Family Court")
        .put("FR_s_NottinghamList_4", "Lincoln County Court and Family Court")
        .put("FR_s_NottinghamList_5", "Northampton Crown, County and Family Court")
        .put("FR_s_NottinghamList_6", "Chesterfield County Court")
        .put("FR_s_NottinghamList_7", "Mansfield Magistrates and County Court")
        .put("FR_s_NottinghamList_8", "Boston County Court and Family Court")
        .build();

    public static String getBirminghamCourt(Map allocatedCourtMap) {
        return birminghamMap.getOrDefault(allocatedCourtMap.get(BIRMINGHAM_COURTLIST), "");
    }

    private static Map<String, String> birminghamMap = ImmutableMap.<String, String>builder()
        .put("FR_birmingham_hc_list_1", "Birmingham Civil and Family Justice Centre")
        .put("FR_birmingham_hc_list_2", "Coventry Combined Court Centre")
        .put("FR_birmingham_hc_list_3", "Telford County Court and Family Court")
        .put("FR_birmingham_hc_list_4", "Wolverhampton Combined Court Centre")
        .put("FR_birmingham_hc_list_5", "Dudley County Court and Family Court")
        .put("FR_birmingham_hc_list_6", "Walsall County and Family Court")
        .put("FR_birmingham_hc_list_7", "Stoke on Trent Combined Court")
        .put("FR_birmingham_hc_list_8", "Worcester Combined Court")
        .put("FR_birmingham_hc_list_9", "Stafford Combined Court")
        .put("FR_birmingham_hc_list_10", "Hereford County Court and Family Court")
        .build();

    public static String getLondonCourt(Map allocatedCourtMap) {
        return londonMap.getOrDefault(allocatedCourtMap.get(LONDON_COURTLIST), "");
    }

    private static Map<String, String> londonMap = ImmutableMap.<String, String>builder()
        .put("FR_s_CFCList_1", "Bromley County Court and Family Court")
        .put("FR_s_CFCList_2", "Croydon County Court and Family Court")
        .put("FR_s_CFCList_3", "Edmonton County Court and Family Court")
        .put("FR_s_CFCList_4", "Kingston-upon-thames County Court and Family Court")
        .put("FR_s_CFCList_5", "Romford County and Family Court")
        .put("FR_s_CFCList_6", "Barnet Civil and Family Courts Centre")
        .put("FR_s_CFCList_8", "Brentford County and Family Court")
        .put("FR_s_CFCList_9", "Central Family Court")
        .put("FR_s_CFCList_11", "East London Family Court")
        .put("FR_s_CFCList_14", "Uxbridge County Court and Family Court")
        .put("FR_s_CFCList_16", "Willesden County Court and Family Court")
        .build();

    public static String getLiverpoolCourt(Map allocatedCourtMap) {
        return liverpoolMap.getOrDefault(allocatedCourtMap.get(LIVERPOOL_COURTLIST), "");
    }

    private static Map<String, String> liverpoolMap = ImmutableMap.<String, String>builder()
        .put("FR_liverpool_hc_list_1", "Liverpool Civil and Family Court")
        .put("FR_liverpool_hc_list_2", "Chester Civil and Family Justice Centre")
        .put("FR_liverpool_hc_list_3", "Crewe County Court and Family Court")
        .put("FR_liverpool_hc_list_4", "St. Helens County Court and Family Court")
        .put("FR_liverpool_hc_list_5", "Birkenhead County Court and Family Court")
        .build();

    public static String getManchesterCourt(Map allocatedCourtMap) {
        return manchesterMap.getOrDefault(allocatedCourtMap.get(MANCHESTER_COURTLIST), "");
    }

    private static Map<String, String> manchesterMap = ImmutableMap.<String, String>builder()
        .put("FR_manchester_hc_list_1", "Manchester County and Family Court")
        .put("FR_manchester_hc_list_2", "Stockport County Court and Family Court")
        .put("FR_manchester_hc_list_3", "Wigan County Court and Family Court")
        .build();

    public static String getCleavelandCourt(Map allocatedCourtMap) {
        return cleavelandMap.getOrDefault(allocatedCourtMap.get(CLEAVELAND_COURTLIST), "");
    }

    private static Map<String, String> cleavelandMap = ImmutableMap.<String, String>builder()
        .put("FR_cleaveland_hc_list_1", "Newcastle upon Tyne Justice Centre")
        .put("FR_cleaveland_hc_list_2", "Durham Justice Centre")
        .put("FR_cleaveland_hc_list_3", "Sunderland County and Family Court")
        .put("FR_cleaveland_hc_list_4", "Middlesbrough County Court at Teesside Combined Court")
        .put("FR_cleaveland_hc_list_5", "Gateshead County Court and Family Court")
        .put("FR_cleaveland_hc_list_6", "South Shields County Court and Family Court")
        .put("FR_cleaveland_hc_list_7", "North Shields County Court and Family Court")
        .put("FR_cleaveland_hc_list_8", "Darlington County Court and Family Court")
        .build();

    public static String getNwYorkshireCourt(Map allocatedCourtMap) {
        return yorkshireMap.getOrDefault(allocatedCourtMap.get(NWYORKSHIRE_COURTLIST), "");
    }

    private static Map<String, String> yorkshireMap = ImmutableMap.<String, String>builder()
        .put("FR_nw_yorkshire_hc_list_1", "Harrogate Justice Centre")
        .put("FR_nw_yorkshire_hc_list_2", "Bradford Combined Court Centre")
        .put("FR_nw_yorkshire_hc_list_3", "Huddersfield County Court and Family Court")
        .put("FR_nw_yorkshire_hc_list_4", "Wakefield Civil and Family Justice Centre")
        .put("FR_nw_yorkshire_hc_list_5", "York County Court and Family Court")
        .put("FR_nw_yorkshire_hc_list_6", "Scarborough Justice Centre")
        .put("FR_nw_yorkshire_hc_list_7", "Skipton County Court and Family Court")
        .put("FR_nw_yorkshire_hc_list_8", "Leeds Combined Court Centre")
        .build();

    public static String getHumberCourt(Map allocatedCourtMap) {
        return humberMap.getOrDefault(allocatedCourtMap.get(HSYORKSHIRE_COURTLIST), "");
    }

    private static Map<String, String> humberMap = ImmutableMap.<String, String>builder()
        .put("FR_humber_hc_list_1", "Sheffield Family Hearing Centre")
        .put("FR_humber_hc_list_2", "Kingston-upon-Hull Combined Court Centre")
        .put("FR_humber_hc_list_3", "Doncaster Justice Centre North")
        .put("FR_humber_hc_list_4", "Great Grimsby Combined Court Centre")
        .put("FR_humber_hc_list_5", "Barnsley Law Courts")
        .build();

    public static String getKentCourt(Map allocatedCourtMap) {
        return kentMap.getOrDefault(allocatedCourtMap.get(KENTFRC_COURTLIST), "");
    }

    private static Map<String, String> kentMap = ImmutableMap.<String, String>builder()
        .put("FR_kent_surrey_hc_list_1", "Canterbury Family Court Hearing Centre")
        .put("FR_kent_surrey_hc_list_2", "Maidstone Combined Court Centre")
        .put("FR_kent_surrey_hc_list_3", "Dartford County Court and Family Court")
        .put("FR_kent_surrey_hc_list_4", "Medway County Court and Family Court")
        .put("FR_kent_surrey_hc_list_5", "Guildford County Court and Family Court")
        .put("FR_kent_surrey_hc_list_6", "Staines County Court and Family Court")
        .put("FR_kent_surrey_hc_list_7", "Brighton County and Family Court")
        .put("FR_kent_surrey_hc_list_8", "Worthing County Court and Family Court")
        .put("FR_kent_surrey_hc_list_9", "Hastings County Court and Family Court Hearing Centre")
        .put("FR_kent_surrey_hc_list_10", "Horsham County Court and Family Court")
        .build();

    public static String getNewportCourt(Map allocatedCourtMap) {
        return newportMap.getOrDefault(allocatedCourtMap.get(NEWPORT_COURTLIST), "");
    }

    private static Map<String, String> newportMap = ImmutableMap.<String, String>builder()
        .put("FR_newport_hc_list_1", "Newport Civil and Family Court")
        .put("FR_newport_hc_list_2", "Cardiff Civil and Family Justice Centre")
        .put("FR_newport_hc_list_3", "Merthyr Tydfil Combined Court Centre")
        .put("FR_newport_hc_list_4", "Pontypridd County and Family Court")
        .put("FR_newport_hc_list_5", "Blackwood Civil and Family Court")
        .build();

    public static String getSwanseaCourt(Map allocatedCourtMap) {
        return swanseaMap.getOrDefault(allocatedCourtMap.get(SWANSEA_COURTLIST), "");
    }

    private static Map<String, String> swanseaMap = ImmutableMap.<String, String>builder()
        .put("FR_swansea_hc_list_1", "Swansea Civil and Family Justice Centre")
        .put("FR_swansea_hc_list_2", "Aberystwyth Justice Centre")
        .put("FR_swansea_hc_list_3", "Haverfordwest County and Family Court")
        .put("FR_swansea_hc_list_4", "Carmarthen County and Family Court")
        .put("FR_swansea_hc_list_5", "Llanelli Law Courts")
        .put("FR_swansea_hc_list_6", "Port Talbot Justice Centre")
        .build();
}
