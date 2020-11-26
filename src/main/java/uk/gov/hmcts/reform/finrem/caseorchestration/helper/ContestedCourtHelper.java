package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import com.google.common.collect.ImmutableMap;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Map;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.ConsentedCourtHelper.getBedfordshireCourt;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.ConsentedCourtHelper.getBristolCourt;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.ConsentedCourtHelper.getDevonCourt;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.ConsentedCourtHelper.getDorsetCourt;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.ConsentedCourtHelper.getLancashireCourt;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.ConsentedCourtHelper.getNorthWalesCourt;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.ConsentedCourtHelper.getThamesValleyCourt;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BEDFORDSHIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BIRMINGHAM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BIRMINGHAM_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BRISTOLFRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CFC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CFC_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CLEAVELAND;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CLEAVELAND_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DEVON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DORSET;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HSYORKSHIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HSYORKSHIRE_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.KENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.KENTFRC_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LANCASHIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LIVERPOOL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LIVERPOOL_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MANCHESTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MANCHESTER_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIDLANDS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIDLANDS_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NEWPORT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NEWPORT_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHEAST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHEAST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHWALES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHWEST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHWEST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTTINGHAM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTTINGHAM_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NWYORKSHIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NWYORKSHIRE_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.REGION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHEAST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHEAST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHWEST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHWEST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SWANSEA;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SWANSEA_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.THAMESVALLEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WALES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WALES_FRC_LIST;

public class ContestedCourtHelper {
    private ContestedCourtHelper() {
    }

    public static String getSelectedCourt(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();

        switch (getSelectedFrc(caseDetails)) {
            case BEDFORDSHIRE:
                return getBedfordshireCourt(caseData);
            case BIRMINGHAM:
                return getBirminghamCourt(caseData);
            case BRISTOLFRC:
                return getBristolCourt(caseData);
            case CFC:
                return getLondonCourt(caseData);
            case CLEAVELAND:
                return getCleavelandCourt(caseData);
            case DEVON:
                return getDevonCourt(caseData);
            case DORSET:
                return getDorsetCourt(caseData);
            case HSYORKSHIRE:
                return getHumberCourt(caseData);
            case KENT:
                return getKentCourt(caseData);
            case LANCASHIRE:
                return getLancashireCourt(caseData);
            case LIVERPOOL:
                return getLiverpoolCourt(caseData);
            case MANCHESTER:
                return getManchesterCourt(caseData);
            case NEWPORT:
                return getNewportCourt(caseData);
            case NORTHWALES:
                return getNorthWalesCourt(caseData);
            case NOTTINGHAM:
                return getNottinghamCourt(caseData);
            case NWYORKSHIRE:
                return getNwYorkshireCourt(caseData);
            case SWANSEA:
                return getSwanseaCourt(caseData);
            case THAMESVALLEY:
                return getThamesValleyCourt(caseData);
            default:
                return EMPTY;
        }
    }

    public static String getSelectedFrc(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();
        String region = (String) caseData.get(REGION);

        if (MIDLANDS.equalsIgnoreCase(region)) {
            return getMidlandFRC(caseData);
        }
        if (LONDON.equalsIgnoreCase(region)) {
            return getLondonFRC(caseData);
        }
        if (NORTHWEST.equalsIgnoreCase(region)) {
            return getNorthWestFRC(caseData);
        }
        if (NORTHEAST.equalsIgnoreCase(region)) {
            return getNorthEastFRC(caseData);
        }
        if (SOUTHEAST.equalsIgnoreCase(region)) {
            return getSouthEastFRC(caseData);
        }
        if (SOUTHWEST.equalsIgnoreCase(region)) {
            return getSouthWestFRC(caseData);
        }
        if (WALES.equalsIgnoreCase(region)) {
            return getWalesFRC(caseData);
        }
        return EMPTY;
    }

    private static String getWalesFRC(Map mapOfCaseData) {
        String walesList = (String) mapOfCaseData.get(WALES_FRC_LIST);
        if (NEWPORT.equalsIgnoreCase(walesList)) {
            return NEWPORT;
        } else if (SWANSEA.equalsIgnoreCase(walesList)) {
            return SWANSEA;
        } else if (NORTHWALES.equalsIgnoreCase(walesList)) {
            return NORTHWALES;
        }
        return EMPTY;
    }

    private static String getSouthWestFRC(Map mapOfCaseData) {
        String southWestList = (String) mapOfCaseData.get(SOUTHWEST_FRC_LIST);
        if (DEVON.equalsIgnoreCase(southWestList)) {
            return DEVON;
        } else if (DORSET.equalsIgnoreCase(southWestList)) {
            return DORSET;
        } else if (BRISTOLFRC.equalsIgnoreCase(southWestList)) {
            return BRISTOLFRC;
        }
        return EMPTY;
    }

    private static String getSouthEastFRC(Map mapOfCaseData) {
        String southEastList = (String) mapOfCaseData.get(SOUTHEAST_FRC_LIST);
        if (KENT.equalsIgnoreCase(southEastList)) {
            return KENT;
        } else if (BEDFORDSHIRE.equalsIgnoreCase(southEastList)) {
            return BEDFORDSHIRE;
        } else if (THAMESVALLEY.equalsIgnoreCase(southEastList)) {
            return THAMESVALLEY;
        }
        return EMPTY;
    }

    private static String getNorthEastFRC(Map mapOfCaseData) {
        String northEastList = (String) mapOfCaseData.get(NORTHEAST_FRC_LIST);
        if (CLEAVELAND.equalsIgnoreCase(northEastList)) {
            return CLEAVELAND;
        } else if (NWYORKSHIRE.equalsIgnoreCase(northEastList)) {
            return NWYORKSHIRE;
        } else if (HSYORKSHIRE.equalsIgnoreCase(northEastList)) {
            return HSYORKSHIRE;
        }
        return EMPTY;
    }

    private static String getNorthWestFRC(Map mapOfCaseData) {
        String northWestList = (String) mapOfCaseData.get(NORTHWEST_FRC_LIST);
        if (LIVERPOOL.equalsIgnoreCase(northWestList)) {
            return LIVERPOOL;
        } else if (MANCHESTER.equalsIgnoreCase(northWestList)) {
            return MANCHESTER;
        } else if (LANCASHIRE.equalsIgnoreCase(northWestList)) {
            return LANCASHIRE;
        }
        return EMPTY;
    }

    private static String getLondonFRC(Map mapOfCaseData) {
        String londonList = (String) mapOfCaseData.get(LONDON_FRC_LIST);
        if (CFC.equalsIgnoreCase(londonList)) {
            return CFC;
        }
        return EMPTY;
    }

    private static String getMidlandFRC(Map mapOfCaseData) {
        String midlandsList = (String) mapOfCaseData.get(MIDLANDS_FRC_LIST);
        if (NOTTINGHAM.equalsIgnoreCase(midlandsList)) {
            return NOTTINGHAM;
        } else if (BIRMINGHAM.equalsIgnoreCase(midlandsList)) {
            return BIRMINGHAM;
        }
        return EMPTY;
    }

    public static String getNottinghamCourt(Map<String, Object> caseData) {
        return nottinghamMap.getOrDefault(caseData.get(NOTTINGHAM_COURTLIST), "");
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

    public static String getBirminghamCourt(Map<String, Object> caseData) {
        return birminghamMap.getOrDefault(caseData.get(BIRMINGHAM_COURTLIST), "");
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

    public static String getLondonCourt(Map<String, Object> caseData) {
        return londonMap.getOrDefault(caseData.get(CFC_COURTLIST), "");
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

    public static String getLiverpoolCourt(Map<String, Object> caseData) {
        return liverpoolMap.getOrDefault(caseData.get(LIVERPOOL_COURTLIST), "");
    }

    private static Map<String, String> liverpoolMap = ImmutableMap.<String, String>builder()
        .put("FR_liverpool_hc_list_1", "Liverpool Civil and Family Court")
        .put("FR_liverpool_hc_list_2", "Chester Civil and Family Justice Centre")
        .put("FR_liverpool_hc_list_3", "Crewe County Court and Family Court")
        .put("FR_liverpool_hc_list_4", "St. Helens County Court and Family Court")
        .put("FR_liverpool_hc_list_5", "Birkenhead County Court and Family Court")
        .build();

    public static String getManchesterCourt(Map<String, Object> caseData) {
        return manchesterMap.getOrDefault(caseData.get(MANCHESTER_COURTLIST), "");
    }

    private static Map<String, String> manchesterMap = ImmutableMap.<String, String>builder()
        .put("FR_manchester_hc_list_1", "Manchester County and Family Court")
        .put("FR_manchester_hc_list_2", "Stockport County Court and Family Court")
        .put("FR_manchester_hc_list_3", "Wigan County Court and Family Court")
        .build();

    public static String getCleavelandCourt(Map<String, Object> caseData) {
        return cleavelandMap.getOrDefault(caseData.get(CLEAVELAND_COURTLIST), "");
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

    public static String getNwYorkshireCourt(Map<String, Object> caseData) {
        return yorkshireMap.getOrDefault(caseData.get(NWYORKSHIRE_COURTLIST), "");
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

    public static String getHumberCourt(Map<String, Object> caseData) {
        return humberMap.getOrDefault(caseData.get(HSYORKSHIRE_COURTLIST), "");
    }

    private static Map<String, String> humberMap = ImmutableMap.<String, String>builder()
        .put("FR_humber_hc_list_1", "Sheffield Family Hearing Centre")
        .put("FR_humber_hc_list_2", "Kingston-upon-Hull Combined Court Centre")
        .put("FR_humber_hc_list_3", "Doncaster Justice Centre North")
        .put("FR_humber_hc_list_4", "Great Grimsby Combined Court Centre")
        .put("FR_humber_hc_list_5", "Barnsley Law Courts")
        .build();

    public static String getKentCourt(Map<String, Object> caseData) {
        return kentMap.getOrDefault(caseData.get(KENTFRC_COURTLIST), "");
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

    public static String getNewportCourt(Map<String, Object> caseData) {
        return newportMap.getOrDefault(caseData.get(NEWPORT_COURTLIST), "");
    }

    private static Map<String, String> newportMap = ImmutableMap.<String, String>builder()
        .put("FR_newport_hc_list_1", "Newport Civil and Family Court")
        .put("FR_newport_hc_list_2", "Cardiff Civil and Family Justice Centre")
        .put("FR_newport_hc_list_3", "Merthyr Tydfil Combined Court Centre")
        .put("FR_newport_hc_list_4", "Pontypridd County and Family Court")
        .put("FR_newport_hc_list_5", "Blackwood Civil and Family Court")
        .build();

    public static String getSwanseaCourt(Map<String, Object> caseData) {
        return swanseaMap.getOrDefault(caseData.get(SWANSEA_COURTLIST), "");
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
