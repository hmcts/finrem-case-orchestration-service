package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import com.google.common.collect.ImmutableMap;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Region;

import java.util.Map;
import java.util.Objects;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ALDERSHOT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BARNSTAPLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BARROW;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BASINGSTOKE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BATH;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BEDFORD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BEDFORDSHIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BEDFORDSHIRE_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BIRMINGHAM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BIRMINGHAM_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BLACKBURN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BLACKPOOL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BODMIN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BOURNEMOUTH;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BRISTOL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BRISTOLFRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BRISTOL_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BRISTOL_MAGISTRATES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BURY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CAERNARFON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CAMBRIDGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CARLISLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CFC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CFC_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CHELMSFORD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CLEAVELAND;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CLEAVELAND_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CLEVELAND;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DEVON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DEVON_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DORSET;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DORSET_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.EXETER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GLOUCESTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_HIGHCOURT_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_LONDON_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_MIDLANDS_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_NORTHEAST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_NORTHWEST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_REGION_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_SOUTHEAST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_SOUTHWEST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_WALES_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HERTFORD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HIGHCOURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HIGHCOURT_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HIGHCOURT_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HSYORKSHIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HSYORKSHIRE_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_REGION_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HIGHCOURT_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_LONDON_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_MIDLANDS_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_NORTHEAST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_NORTHWEST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_SOUTHEAST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_SOUTHWEST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_WALES_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.IPSWICH;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ISLE_OF_WIGHT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.KENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.KENTFRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.KENTFRC_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LANCASHIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LANCASHIRE_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LANCASTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LEYLAND;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LIVERPOOL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LIVERPOOL_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LUTON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MANCHESTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MANCHESTER_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIDLANDS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIDLANDS_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MILTON_KEYNES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MOLD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NEWPORT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NEWPORT_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHEAST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHEAST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHWALES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHWEST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHWEST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTH_WALES_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORWICH;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTTINGHAM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTTINGHAM_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NWYORKSHIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NWYORKSHIRE_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.OXFORD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.PETERBOROUGH;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.PLYMOUTH;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.PORTSMOUTH;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.PRESTATYN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.PRESTON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.READING;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.REEDLEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.REGION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SALISBURY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SLOUGH;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHAMPTON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHEAST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHEAST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHEND;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHWEST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHWEST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SWANSEA;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SWANSEA_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SWINDON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SWINDON_MAGISTRATES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.TAUNTON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.THAMESVALLEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.THAMESVALLEY_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.TORQUAY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.TRURO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WALES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WALES_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WATFORD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WELSHPOOL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WESTON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WEST_CUMBRIA;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WEYMOUTH;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WINCHESTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WREXHAM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.YEOVIL;

public class CourtHelper {

    private CourtHelper() {
    }

    private static final Map<String, String> nottinghamMap = ImmutableMap.<String, String>builder()
        .put("FR_s_NottinghamList_1", "Nottingham County Court and Family Court")
        .put("FR_s_NottinghamList_2", "Derby Combined Court Centre")
        .put("FR_s_NottinghamList_3", "Leicester County Court and Family Court")
        .put("FR_s_NottinghamList_4", "Lincoln County Court and Family Court")
        .put("FR_s_NottinghamList_5", "Northampton Crown, County and Family Court")
        .put("FR_s_NottinghamList_6", "Chesterfield County Court")
        .put("FR_s_NottinghamList_7", "Mansfield Magistrates and County Court")
        .put("FR_s_NottinghamList_8", "Boston County Court and Family Court")
        .build();
    private static final Map<String, String> birminghamMap = ImmutableMap.<String, String>builder()
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
    private static final Map<String, String> londonMap = ImmutableMap.<String, String>builder()
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
        .put("FR_s_CFCList_17", "The Royal Courts of Justice")
        .build();
    private static final Map<String, String> liverpoolMap = ImmutableMap.<String, String>builder()
        .put("FR_liverpool_hc_list_1", "Liverpool Civil and Family Court")
        .put("FR_liverpool_hc_list_2", "Chester Civil and Family Justice Centre")
        .put("FR_liverpool_hc_list_3", "Crewe County Court and Family Court")
        .put("FR_liverpool_hc_list_4", "St. Helens County Court and Family Court")
        .put("FR_liverpool_hc_list_5", "Birkenhead County Court and Family Court")
        .build();
    private static final Map<String, String> manchesterMap = ImmutableMap.<String, String>builder()
        .put("FR_manchester_hc_list_1", "Manchester County and Family Court")
        .put("FR_manchester_hc_list_2", "Stockport County Court and Family Court")
        .put("FR_manchester_hc_list_3", "Wigan County Court and Family Court")
        .build();
    private static final Map<String, String> cleavelandMap = ImmutableMap.<String, String>builder()
        .put("FR_cleaveland_hc_list_1", "Newcastle Civil and Family Courts and Tribunals Centre")
        .put("FR_cleaveland_hc_list_2", "Durham Justice Centre")
        .put("FR_cleaveland_hc_list_3", "Sunderland County and Family Court")
        .put("FR_cleaveland_hc_list_4", "Middlesbrough County Court at Teesside Combined Court")
        .put("FR_cleaveland_hc_list_5", "Gateshead County Court and Family Court")
        .put("FR_cleaveland_hc_list_6", "South Shields County Court and Family Court")
        .put("FR_cleaveland_hc_list_7", "North Shields County Court and Family Court")
        .put("FR_cleaveland_hc_list_8", "Darlington County Court and Family Court")
        .build();
    private static final Map<String, String> yorkshireMap = ImmutableMap.<String, String>builder()
        .put("FR_nw_yorkshire_hc_list_1", "Harrogate Justice Centre")
        .put("FR_nw_yorkshire_hc_list_2", "Bradford Combined Court Centre")
        .put("FR_nw_yorkshire_hc_list_3", "Huddersfield County Court and Family Court")
        .put("FR_nw_yorkshire_hc_list_4", "Wakefield Civil and Family Justice Centre")
        .put("FR_nw_yorkshire_hc_list_5", "York County Court and Family Court")
        .put("FR_nw_yorkshire_hc_list_6", "Scarborough Justice Centre")
        .put("FR_nw_yorkshire_hc_list_7", "Skipton County Court and Family Court")
        .put("FR_nw_yorkshire_hc_list_8", "Leeds Combined Court Centre")
        .build();
    private static final Map<String, String> humberMap = ImmutableMap.<String, String>builder()
        .put("FR_humber_hc_list_1", "Sheffield Family Hearing Centre")
        .put("FR_humber_hc_list_2", "Kingston-upon-Hull Combined Court Centre")
        .put("FR_humber_hc_list_3", "Doncaster Justice Centre North")
        .put("FR_humber_hc_list_4", "Great Grimsby Combined Court Centre")
        .put("FR_humber_hc_list_5", "Barnsley Law Courts")
        .build();
    private static final Map<String, String> kentMap = ImmutableMap.<String, String>builder()
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
        .put("FR_kent_surrey_hc_list_11", "Thanet Family Court Hearing Centre")
        .build();
    private static final Map<String, String> newportMap = ImmutableMap.<String, String>builder()
        .put("FR_newport_hc_list_1", "Newport Civil and Family Court")
        .put("FR_newport_hc_list_2", "Cardiff Civil and Family Justice Centre")
        .put("FR_newport_hc_list_3", "Merthyr Tydfil Combined Court Centre")
        .put("FR_newport_hc_list_4", "Pontypridd County and Family Court")
        .put("FR_newport_hc_list_5", "Blackwood Civil and Family Court")
        .build();
    private static final Map<String, String> swanseaMap = ImmutableMap.<String, String>builder()
        .put("FR_swansea_hc_list_1", "Swansea Civil and Family Justice Centre")
        .put("FR_swansea_hc_list_2", "Aberystwyth Justice Centre")
        .put("FR_swansea_hc_list_3", "Haverfordwest County and Family Court")
        .put("FR_swansea_hc_list_4", "Carmarthen County and Family Court")
        .put("FR_swansea_hc_list_5", "Llanelli Law Courts")
        .put("FR_swansea_hc_list_6", "Port Talbot Justice Centre")
        .build();

    private static final Map<String, String> highCourtMap = ImmutableMap.<String, String>builder()
        .put("FR_highCourtList_1", "High Court Family Division")
        .build();
    private static Map<String, String> bedfordshireMap = ImmutableMap.<String, String>builder()
        .put(PETERBOROUGH, "Peterborough Combined Court Centre")
        .put(CAMBRIDGE, "Cambridge County Court and Family Court")
        .put(BURY, "Bury St Edmunds County Court and Family Court")
        .put(NORWICH, "Norwich Combined Court Centre")
        .put(IPSWICH, "Ipswich County Court and Family Hearing Centre")
        .put(CHELMSFORD, "Chelmsford Justice Centre")
        .put(SOUTHEND, "Southend County Court and Family Court")
        .put(BEDFORD, "Bedford County Court and Family Court")
        .put(LUTON, "Luton Justice Centre")
        .put(HERTFORD, "Hertford County Court and Family Court")
        .put(WATFORD, "Watford County Court and Family Court")
        .build();
    private static Map<String, String> dorsetMap = ImmutableMap.<String, String>builder()
        .put(BOURNEMOUTH, "Bournemouth and Poole County Court and Family Court")
        .put(WEYMOUTH, "Weymouth Combined Court")
        .put(WINCHESTER, "Winchester Combined Court Centre")
        .put(PORTSMOUTH, "Portsmouth Combined Court Centre")
        .put(SOUTHAMPTON, "Southampton Combined Court Centre")
        .put(ALDERSHOT, "Aldershot Justice Centre")
        .put(BASINGSTOKE, "Basingstoke County and Family Court")
        .put(ISLE_OF_WIGHT, "Newport (Isle of Wight) Combined Court")
        .build();
    private static Map<String, String> devonMap = ImmutableMap.<String, String>builder()
        .put(PLYMOUTH, "Plymouth Combined Court")
        .put(EXETER, "Exeter Combined Court Centre")
        .put(TAUNTON, "Taunton Crown, County and Family Court")
        .put(TORQUAY, "Torquay and Newton Abbot County and Family Court")
        .put(BARNSTAPLE, "Barnstaple Magistrates, County and Family Court")
        .put(TRURO, "Truro County Court and Family Court")
        .put(YEOVIL, "Yeovil County, Family and Magistrates Court")
        .put(BODMIN, "Bodmin County Court and Family Court")
        .build();
    private static Map<String, String> bristolMap = ImmutableMap.<String, String>builder()
        .put(BRISTOL, "Bristol Civil and Family Justice Centre")
        .put(GLOUCESTER, "Gloucester and Cheltenham County and Family Court")
        .put(SWINDON, "Swindon Combined Court")
        .put(SALISBURY, "Salisbury Law Courts")
        .put(BATH, "Bath Law Courts")
        .put(WESTON, "Weston Super Mare County and Family Court")
        .put(BRISTOL_MAGISTRATES, "Bristol Magistrates Court")
        .put(SWINDON_MAGISTRATES, "Swindon Magistrates Court")
        .build();
    private static Map<String, String> thamesvalleyMap = ImmutableMap.<String, String>builder()
        .put(OXFORD, "Oxford Combined Court Centre")
        .put(READING, "Reading County Court and Family Court")
        .put(MILTON_KEYNES, "Milton Keynes County Court and Family Court")
        .put(SLOUGH, "Slough County Court and Family Court")
        .build();
    private static Map<String, String> lancashireMap = ImmutableMap.<String, String>builder()
        .put(PRESTON, "Preston Designated Family Court")
        .put(BLACKBURN, "Blackburn Family Court")
        .put(BLACKPOOL, "Blackpool Family Court")
        .put(LANCASTER, "Lancaster Courthouse")
        .put(LEYLAND, "Leyland Family Hearing Centre")
        .put(REEDLEY, "Reedley Family Hearing Centre")
        .put(BARROW, "Barrow in Furness County and Family Court")
        .put(CARLISLE, "Carlisle Combined Court")
        .put(WEST_CUMBRIA, "West Cumbria Courthouse")
        .build();
    private static Map<String, String> northWalesMap = ImmutableMap.<String, String>builder()
        .put(WREXHAM, "Wrexham County Court and Family Court")
        .put(CAERNARFON, "Caernarfon Justice Centre")
        .put(PRESTATYN, "Prestatyn Justice Centre")
        .put(WELSHPOOL, "Welshpool Civil and Family Court")
        .put(MOLD, "Mold County")
        .build();

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
            case CLEVELAND:
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
            case HIGHCOURT:
                return getHighCourt(caseData);
            default:
                return EMPTY;
        }
    }

    public static String getSelectedInterimHearingFrc(Map<String, Object> interimHearingData) {
        String region = (String) interimHearingData.get(INTERIM_HEARING_REGION_LIST);

        if (MIDLANDS.equalsIgnoreCase(region)) {
            return getMidlandInterimHearingFRC(interimHearingData);
        }
        if (LONDON.equalsIgnoreCase(region)) {
            return getLondonInterimHearingFRC(interimHearingData);
        }
        if (NORTHWEST.equalsIgnoreCase(region)) {
            return getNorthWestInterimHearingFRC(interimHearingData);
        }
        if (NORTHEAST.equalsIgnoreCase(region)) {
            return getNorthEastInterimHearingFRC(interimHearingData);
        }
        if (SOUTHEAST.equalsIgnoreCase(region)) {
            return getSouthEastInterimHearingFRC(interimHearingData);
        }
        if (SOUTHWEST.equalsIgnoreCase(region)) {
            return getSouthWestInterimHearingFRC(interimHearingData);
        }
        if (WALES.equalsIgnoreCase(region)) {
            return getWalesInterimHearingFRC(interimHearingData);
        }
        if (HIGHCOURT.equalsIgnoreCase(region)) {
            return getHighCourtInterimHearingFRC(interimHearingData);
        }
        return EMPTY;
    }

    private static String getWalesInterimHearingFRC(Map<String, Object> interimHearingData) {
        String walesList = (String) interimHearingData.get(INTERIM_WALES_FRC_LIST);
        if (NEWPORT.equalsIgnoreCase(walesList)) {
            return NEWPORT;
        } else if (SWANSEA.equalsIgnoreCase(walesList)) {
            return SWANSEA;
        } else if (NORTHWALES.equalsIgnoreCase(walesList)) {
            return NORTHWALES;
        }
        return EMPTY;
    }

    private static String getHighCourtInterimHearingFRC(Map<String, Object> interimHearingData) {
        String highCourtList = (String) interimHearingData.get(INTERIM_HIGHCOURT_FRC_LIST);
        if (HIGHCOURT.equalsIgnoreCase(highCourtList)) {
            return HIGHCOURT;
        }
        return EMPTY;
    }

    private static String getSouthWestInterimHearingFRC(Map<String, Object> interimHearingData) {
        String southWestList = (String) interimHearingData.get(INTERIM_SOUTHWEST_FRC_LIST);
        if (DEVON.equalsIgnoreCase(southWestList)) {
            return DEVON;
        } else if (DORSET.equalsIgnoreCase(southWestList)) {
            return DORSET;
        } else if (BRISTOLFRC.equalsIgnoreCase(southWestList)) {
            return BRISTOLFRC;
        }
        return EMPTY;
    }

    private static String getSouthEastInterimHearingFRC(Map<String, Object> interimHearingData) {
        String southEastList = (String) interimHearingData.get(INTERIM_SOUTHEAST_FRC_LIST);
        if (KENT.equalsIgnoreCase(southEastList)) {
            return KENT;
        } else if (BEDFORDSHIRE.equalsIgnoreCase(southEastList)) {
            return BEDFORDSHIRE;
        } else if (THAMESVALLEY.equalsIgnoreCase(southEastList)) {
            return THAMESVALLEY;
        }
        return EMPTY;
    }

    private static String getNorthEastInterimHearingFRC(Map<String, Object> interimHearingData) {
        String northEastList = (String) interimHearingData.get(INTERIM_NORTHEAST_FRC_LIST);
        if (CLEAVELAND.equalsIgnoreCase(northEastList)) {
            return CLEAVELAND;
        } else if (NWYORKSHIRE.equalsIgnoreCase(northEastList)) {
            return NWYORKSHIRE;
        } else if (HSYORKSHIRE.equalsIgnoreCase(northEastList)) {
            return HSYORKSHIRE;
        }
        return EMPTY;
    }

    private static String getNorthWestInterimHearingFRC(Map<String, Object> interimHearingData) {
        String northWestList = (String) interimHearingData.get(INTERIM_NORTHWEST_FRC_LIST);
        if (LIVERPOOL.equalsIgnoreCase(northWestList)) {
            return LIVERPOOL;
        } else if (MANCHESTER.equalsIgnoreCase(northWestList)) {
            return MANCHESTER;
        } else if (LANCASHIRE.equalsIgnoreCase(northWestList)) {
            return LANCASHIRE;
        }
        return EMPTY;
    }

    private static String getLondonInterimHearingFRC(Map<String, Object> interimHearingData) {
        String londonList = (String) interimHearingData.get(INTERIM_LONDON_FRC_LIST);
        if (CFC.equalsIgnoreCase(londonList)) {
            return CFC;
        }
        return EMPTY;
    }

    private static String getMidlandInterimHearingFRC(Map<String, Object> interimHearingData) {
        String midlandsList = (String) interimHearingData.get(INTERIM_MIDLANDS_FRC_LIST);
        return getMidlandsCourtName(midlandsList);
    }

    public static String getSelectedHearingFrc(Map<String, Object> hearingData) {
        String region = Objects.toString(hearingData.get(HEARING_REGION_LIST), "");

        if (MIDLANDS.equalsIgnoreCase(region)) {
            return getMidlandHearingFRC(hearingData);
        }
        if (LONDON.equalsIgnoreCase(region)) {
            return getLondonHearingFRC(hearingData);
        }
        if (NORTHWEST.equalsIgnoreCase(region)) {
            return getNorthWestHearingFRC(hearingData);
        }
        if (NORTHEAST.equalsIgnoreCase(region)) {
            return getNorthEastHearingFRC(hearingData);
        }
        if (SOUTHEAST.equalsIgnoreCase(region)) {
            return getSouthEastHearingFRC(hearingData);
        }
        if (SOUTHWEST.equalsIgnoreCase(region)) {
            return getSouthWestHearingFRC(hearingData);
        }
        if (WALES.equalsIgnoreCase(region)) {
            return getWalesHearingFRC(hearingData);
        }
        if (HIGHCOURT.equalsIgnoreCase(region)) {
            return getHighCourtHearingFRC(hearingData);
        }
        return EMPTY;
    }

    private static String getWalesHearingFRC(Map<String, Object> hearingData) {
        String walesList = Objects.toString(hearingData.get(HEARING_WALES_FRC_LIST),"");
        if (NEWPORT.equalsIgnoreCase(walesList)) {
            return NEWPORT;
        } else if (SWANSEA.equalsIgnoreCase(walesList)) {
            return SWANSEA;
        } else if (NORTHWALES.equalsIgnoreCase(walesList)) {
            return NORTHWALES;
        }
        return EMPTY;
    }

    private static String getHighCourtHearingFRC(Map<String, Object> hearingData) {
        String highCourtList = Objects.toString(hearingData.get(HEARING_HIGHCOURT_FRC_LIST),"");
        if (HIGHCOURT.equalsIgnoreCase(highCourtList)) {
            return HIGHCOURT;
        }
        return EMPTY;
    }

    private static String getSouthWestHearingFRC(Map<String, Object> hearingData) {
        String southWestList = Objects.toString(hearingData.get(HEARING_SOUTHWEST_FRC_LIST),"");
        if (DEVON.equalsIgnoreCase(southWestList)) {
            return DEVON;
        } else if (DORSET.equalsIgnoreCase(southWestList)) {
            return DORSET;
        } else if (BRISTOLFRC.equalsIgnoreCase(southWestList)) {
            return BRISTOLFRC;
        }
        return EMPTY;
    }

    private static String getSouthEastHearingFRC(Map<String, Object> hearingData) {
        String southEastList = Objects.toString(hearingData.get(HEARING_SOUTHEAST_FRC_LIST),"");
        if (KENT.equalsIgnoreCase(southEastList)) {
            return KENT;
        } else if (BEDFORDSHIRE.equalsIgnoreCase(southEastList)) {
            return BEDFORDSHIRE;
        } else if (THAMESVALLEY.equalsIgnoreCase(southEastList)) {
            return THAMESVALLEY;
        }
        return EMPTY;
    }

    private static String getNorthEastHearingFRC(Map<String, Object> hearingData) {
        String northEastList = Objects.toString(hearingData.get(HEARING_NORTHEAST_FRC_LIST),"");
        if (CLEAVELAND.equalsIgnoreCase(northEastList) || CLEVELAND.equalsIgnoreCase(northEastList)) {
            return CLEAVELAND;
        } else if (NWYORKSHIRE.equalsIgnoreCase(northEastList)) {
            return NWYORKSHIRE;
        } else if (HSYORKSHIRE.equalsIgnoreCase(northEastList)) {
            return HSYORKSHIRE;
        }
        return EMPTY;
    }

    private static String getNorthWestHearingFRC(Map<String, Object> hearingData) {
        String northWestList = Objects.toString(hearingData.get(HEARING_NORTHWEST_FRC_LIST),"");
        if (LIVERPOOL.equalsIgnoreCase(northWestList)) {
            return LIVERPOOL;
        } else if (MANCHESTER.equalsIgnoreCase(northWestList)) {
            return MANCHESTER;
        } else if (LANCASHIRE.equalsIgnoreCase(northWestList)) {
            return LANCASHIRE;
        }
        return EMPTY;
    }

    private static String getLondonHearingFRC(Map<String, Object> hearingData) {
        String londonList = Objects.toString(hearingData.get(HEARING_LONDON_FRC_LIST),"");
        if (CFC.equalsIgnoreCase(londonList)) {
            return CFC;
        }
        return EMPTY;
    }

    private static String getMidlandHearingFRC(Map<String, Object> hearingData) {
        String midlandsList = Objects.toString(hearingData.get(HEARING_MIDLANDS_FRC_LIST),"");
        return getMidlandsCourtName(midlandsList);
    }

    public static String getSelectedFrc(Map<String, Object> caseData) {

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
        if (HIGHCOURT.equalsIgnoreCase(region)) {
            return getHighCourtFRC(caseData);
        }
        return EMPTY;
    }

    public static String getSelectedFrc(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();
        return getSelectedFrc(caseData);
    }

    public static String getSelectedFrc(FinremCaseDetails caseDetails) {
        FinremCaseData caseData = caseDetails.getData();
        Region region = caseData.getRegionWrapper().getAllocatedRegionWrapper().getRegionList();

        if (MIDLANDS.equalsIgnoreCase(region.getValue())) {
            return caseData.getRegionWrapper().getAllocatedRegionWrapper().getMidlandsFrcList().getValue();
        }
        if (LONDON.equalsIgnoreCase(region.getValue())) {
            return caseData.getRegionWrapper().getAllocatedRegionWrapper().getLondonFrcList().getValue();
        }
        if (NORTHWEST.equalsIgnoreCase(region.getValue())) {
            return caseData.getRegionWrapper().getAllocatedRegionWrapper().getNorthWestFrcList().getValue();
        }
        if (NORTHEAST.equalsIgnoreCase(region.getValue())) {
            return caseData.getRegionWrapper().getAllocatedRegionWrapper().getNorthEastFrcList().getValue();
        }
        if (SOUTHEAST.equalsIgnoreCase(region.getValue())) {
            return caseData.getRegionWrapper().getAllocatedRegionWrapper().getSouthEastFrcList().getValue();
        }
        if (SOUTHWEST.equalsIgnoreCase(region.getValue())) {
            return caseData.getRegionWrapper().getAllocatedRegionWrapper().getSouthWestFrcList().getValue();
        }
        if (WALES.equalsIgnoreCase(region.getValue())) {
            return caseData.getRegionWrapper().getAllocatedRegionWrapper().getWalesFrcList().getValue();
        }
        if (HIGHCOURT.equalsIgnoreCase(region.getValue())) {
            return caseData.getRegionWrapper().getAllocatedRegionWrapper().getHighCourtFrcList().getValue();
        }
        return EMPTY;
    }

    private static String getWalesFRC(Map<String, Object> mapOfCaseData) {
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

    private static String getHighCourtFRC(Map<String, Object> mapOfCaseData) {
        String highCourtList = (String) mapOfCaseData.get(HIGHCOURT_FRC_LIST);
        if (HIGHCOURT.equalsIgnoreCase(highCourtList)) {
            return HIGHCOURT;
        }
        return EMPTY;
    }

    private static String getSouthWestFRC(Map<String, Object> mapOfCaseData) {
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

    private static String getSouthEastFRC(Map<String, Object> mapOfCaseData) {
        String southEastList = (String) mapOfCaseData.get(SOUTHEAST_FRC_LIST);
        if (KENT.equalsIgnoreCase(southEastList) || KENTFRC.equalsIgnoreCase(southEastList)) {
            return KENT;
        } else if (BEDFORDSHIRE.equalsIgnoreCase(southEastList)) {
            return BEDFORDSHIRE;
        } else if (THAMESVALLEY.equalsIgnoreCase(southEastList)) {
            return THAMESVALLEY;
        }
        return EMPTY;
    }

    private static String getNorthEastFRC(Map<String, Object> mapOfCaseData) {
        String northEastList = (String) mapOfCaseData.get(NORTHEAST_FRC_LIST);
        if (CLEAVELAND.equalsIgnoreCase(northEastList) || CLEVELAND.equalsIgnoreCase(northEastList)) {
            return CLEAVELAND;
        } else if (NWYORKSHIRE.equalsIgnoreCase(northEastList)) {
            return NWYORKSHIRE;
        } else if (HSYORKSHIRE.equalsIgnoreCase(northEastList)) {
            return HSYORKSHIRE;
        }
        return EMPTY;
    }

    private static String getNorthWestFRC(Map<String, Object> mapOfCaseData) {
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

    private static String getLondonFRC(Map<String, Object> mapOfCaseData) {
        String londonList = (String) mapOfCaseData.get(LONDON_FRC_LIST);
        if (CFC.equalsIgnoreCase(londonList)) {
            return CFC;
        }
        return EMPTY;
    }


    private static String getMidlandFRC(Map<String, Object> mapOfCaseData) {
        String midlandsList = (String) mapOfCaseData.get(MIDLANDS_FRC_LIST);
        return getMidlandsCourtName(midlandsList);
    }

    private static String getMidlandsCourtName(String midlandsList) {
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

    public static String getBirminghamCourt(Map<String, Object> caseData) {
        return birminghamMap.getOrDefault(caseData.get(BIRMINGHAM_COURTLIST), "");
    }

    public static String getLondonCourt(Map<String, Object> caseData) {
        return londonMap.getOrDefault(caseData.get(CFC_COURTLIST), "");
    }

    public static String getLiverpoolCourt(Map<String, Object> caseData) {
        return liverpoolMap.getOrDefault(caseData.get(LIVERPOOL_COURTLIST), "");
    }

    public static String getManchesterCourt(Map<String, Object> caseData) {
        return manchesterMap.getOrDefault(caseData.get(MANCHESTER_COURTLIST), "");
    }

    public static String getCleavelandCourt(Map<String, Object> caseData) {
        return cleavelandMap.getOrDefault(caseData.get(CLEAVELAND_COURTLIST), "");
    }

    public static String getNwYorkshireCourt(Map<String, Object> caseData) {
        return yorkshireMap.getOrDefault(caseData.get(NWYORKSHIRE_COURTLIST), "");
    }

    public static String getHumberCourt(Map<String, Object> caseData) {
        return humberMap.getOrDefault(caseData.get(HSYORKSHIRE_COURTLIST), "");
    }

    public static String getKentCourt(Map<String, Object> caseData) {
        return kentMap.getOrDefault(caseData.get(KENTFRC_COURTLIST), "");
    }

    public static String getNewportCourt(Map<String, Object> caseData) {
        return newportMap.getOrDefault(caseData.get(NEWPORT_COURTLIST), "");
    }

    public static String getSwanseaCourt(Map<String, Object> caseData) {
        return swanseaMap.getOrDefault(caseData.get(SWANSEA_COURTLIST), "");
    }

    public static String getBedfordshireCourt(Map<String, Object> caseData) {
        return bedfordshireMap.getOrDefault(caseData.get(BEDFORDSHIRE_COURTLIST), "");
    }

    public static String getLancashireCourt(Map<String, Object> caseData) {
        return lancashireMap.getOrDefault(caseData.get(LANCASHIRE_COURTLIST), "");
    }

    public static String getThamesValleyCourt(Map<String, Object> caseData) {
        return thamesvalleyMap.getOrDefault(caseData.get(THAMESVALLEY_COURTLIST), "");
    }

    public static String getDevonCourt(Map<String, Object> caseData) {
        return devonMap.getOrDefault(caseData.get(DEVON_COURTLIST), "");
    }

    public static String getBristolCourt(Map<String, Object> caseData) {
        return bristolMap.getOrDefault(caseData.get(BRISTOL_COURTLIST), "");
    }

    public static String getDorsetCourt(Map<String, Object> caseData) {
        return dorsetMap.getOrDefault(caseData.get(DORSET_COURTLIST), "");
    }

    public static String getNorthWalesCourt(Map<String, Object> caseData) {
        return northWalesMap.getOrDefault(caseData.get(NORTH_WALES_COURTLIST), "");
    }

    public static String getHighCourt(Map<String, Object> caseData) {
        return highCourtMap.getOrDefault(caseData.get(HIGHCOURT_COURTLIST), "");
    }
}
