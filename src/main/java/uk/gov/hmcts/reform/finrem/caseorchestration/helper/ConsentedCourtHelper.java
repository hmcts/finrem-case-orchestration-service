package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import com.google.common.collect.ImmutableMap;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Map;

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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CHELMSFORD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CLEAVELAND;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CLEAVELAND_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DEVON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DEVON_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DORSET;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DORSET_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.EXETER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GLOUCESTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HERTFORD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HIGHCOURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HIGHCOURT_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HIGHCOURT_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HSYORKSHIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HSYORKSHIRE_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.IPSWICH;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ISLE_OF_WIGHT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.KENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.KENTFRC_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LANCASHIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LANCASHIRE_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LANCASTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LEYLAND;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LIVERPOOL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LIVERPOOL_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON_COURTLIST;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NWOTHER_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NWYORKSHIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NWYORKSHIRE_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.OTHER;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SEOTHER_COURTLIST;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SWOTHER_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.TAUNTON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.THAMESVALLEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.THAMESVALLEY_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.TORQUAY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.TRURO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WALES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WALES_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WALES_OTHER_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WATFORD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WELSHPOOL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WESTON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WEST_CUMBRIA;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WEYMOUTH;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WINCHESTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WREXHAM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.YEOVIL;

public class ConsentedCourtHelper {
    private static Map<String, String> birminghamMap = ImmutableMap.<String, String>builder()
        .put("FR_birminghamList_1", "Birmingham Civil and Family Justice Centre")
        .put("FR_birminghamList_2", "Coventry Combined Court Centre")
        .put("FR_birminghamList_3", "Telford County Court and Family Court")
        .put("FR_birminghamList_4", "Wolverhampton Combined Court Centre")
        .put("FR_birminghamList_5", "Dudley County Court and Family Court")
        .put("FR_birminghamList_6", "Walsall County and Family Court")
        .put("FR_birminghamList_7", "Stoke on Trent Combined Court")
        .put("FR_birminghamList_8", "Worcester Combined Court")
        .put("FR_birminghamList_9", "Stafford Combined Court")
        .put("FR_birminghamList_10", "Hereford County Court and Family Court")
        .build();
    private static Map<String, String> londonMap = ImmutableMap.<String, String>builder()
        .put("FR_londonList_13", "The Royal Courts of Justice")
        .put("FR_londonList_11", "Bromley County Court and Family Court")
        .put("FR_londonList_10", "Croydon County Court and Family Court")
        .put("FR_londonList_9", "Edmonton County Court and Family Court")
        .put("FR_londonList_8", "Kingston-upon-thames County Court and Family Court")
        .put("FR_londonList_7", "Romford County and Family Court")
        .put("FR_londonList_6", "Barnet Civil and Family Courts Centre")
        .put("FR_londonList_5", "Brentford County and Family Court")
        .put("FR_londonList_1", "Central Family Court")
        .put("FR_londonList_4", "East London Family Court")
        .put("FR_londonList_3", "Uxbridge County Court and Family Court")
        .put("FR_londonList_2", "Willesden County Court and Family Court")
        .build();
    private static Map<String, String> nottinghamMap = ImmutableMap.<String, String>builder()
        .put("FR_nottinghamList_1", "Nottingham County Court and Family Court")
        .put("FR_nottinghamList_2", "Derby Combined Court Centre")
        .put("FR_nottinghamList_3", "Leicester County Court and Family Court")
        .put("FR_nottinghamList_4", "Lincoln County Court and Family Court")
        .put("FR_nottinghamList_5", "Northampton Crown, County and Family Court")
        .put("FR_nottinghamList_6", "Chesterfield County Court")
        .put("FR_nottinghamList_7", "Mansfield Magistrates and County Court")
        .put("FR_nottinghamList_8", "Boston County Court and Family Court")
        .build();
    private static Map<String, String> liverpoolMap = ImmutableMap.<String, String>builder()
        .put("FR_liverpoolList_1", "Liverpool Civil and Family Court")
        .put("FR_liverpoolList_2", "Chester Civil and Family Justice Centre")
        .put("FR_liverpoolList_3", "Crewe County Court and Family Court")
        .put("FR_liverpoolList_4", "St. Helens County Court and Family Court")
        .put("FR_liverpoolList_5", "Birkenhead County Court and Family Court")
        .build();
    private static Map<String, String> nwOtherMap = ImmutableMap.<String, String>builder()
        .put("FR_NWList_1", "West Cumbria Courthouse")
        .put("FR_NWList_2", "Preston Designated Family Court")
        .put("FR_NWList_3", "Lancaster Courthouse")
        .put("FR_NWList_4", "Carlisle Combined Court")
        .put("FR_NWList_5", "Burnley Combined Court")
        .put("FR_NWList_6", "Blackpool Family Court")
        .put("FR_NWList_7", "Blackburn Family Court")
        .put("FR_NWList_8", "Barrow-in-Furness County and Family Court")
        .build();
    private static Map<String, String> cleavelandMap = ImmutableMap.<String, String>builder()
        .put("FR_clevelandList_1", "Newcastle Civil and Family Courts and Tribunals Centre")
        .put("FR_clevelandList_2", "Durham Justice Centre")
        .put("FR_clevelandList_3", "Sunderland County and Family Court")
        .put("FR_clevelandList_4", "Middlesbrough County Court at Teesside Combined Court")
        .put("FR_clevelandList_5", "Gateshead County Court and Family Court")
        .put("FR_clevelandList_6", "South Shields County Court and Family Court")
        .put("FR_clevelandList_7", "North Shields County Court and Family Court")
        .put("FR_clevelandList_8", "Darlington County Court and Family Court")
        .build();
    private static Map<String, String> manchesterMap = ImmutableMap.<String, String>builder()
        .put("FR_manchesterList_1", "Manchester County and Family Court")
        .put("FR_manchesterList_2", "Stockport County Court and Family Court")
        .put("FR_manchesterList_3", "Wigan County Court and Family Court")
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
    private static Map<String, String> yorkshireMap = ImmutableMap.<String, String>builder()
        .put("FR_nw_yorkshireList_1", "Harrogate Justice Centre")
        .put("FR_nw_yorkshireList_2", "Bradford Combined Court Centre")
        .put("FR_nw_yorkshireList_3", "Huddersfield County Court and Family Court")
        .put("FR_nw_yorkshireList_4", "Wakefield Civil and Family Justice Centre")
        .put("FR_nw_yorkshireList_5", "York County Court and Family Court")
        .put("FR_nw_yorkshireList_6", "Scarborough Justice Centre")
        .put("FR_nw_yorkshireList_7", "Skipton County Court and Family Court")
        .put("FR_nw_yorkshireList_8", "Leeds Combined Court Centre")
        .build();
    private static Map<String, String> kentMap = ImmutableMap.<String, String>builder()
        .put("FR_kent_surreyList_1", "Canterbury Family Court Hearing Centre")
        .put("FR_kent_surreyList_2", "Maidstone Combined Court Centre")
        .put("FR_kent_surreyList_3", "Dartford County Court and Family Court")
        .put("FR_kent_surreyList_4", "Medway County Court and Family Court")
        .put("FR_kent_surreyList_5", "Guildford County Court and Family Court")
        .put("FR_kent_surreyList_6", "Staines County Court and Family Court")
        .put("FR_kent_surreyList_7", "Brighton County and Family Court")
        .put("FR_kent_surreyList_8", "Worthing County Court and Family Court")
        .put("FR_kent_surreyList_9", "Hastings County Court and Family Court Hearing Centre")
        .put("FR_kent_surreyList_10", "Horsham County Court and Family Court")
        .build();
    private static Map<String, String> seOtherMap = ImmutableMap.<String, String>builder()
        .put("FR_SEList_1", "Basildon Magistrates Court and Family Court")
        .put("FR_SEList_2", "Bedford County Court and Family Court")
        .put("FR_SEList_3", "Brighton County and Family Court")
        .put("FR_SEList_4", "Bury St Edmunds County Court and Family Court")
        .put("FR_SEList_5", "Cambridge County Court and Family Court")
        .put("FR_SEList_6", "Chelmsford County Court and Family Hearing Centre")
        .put("FR_SEList_7", "Colchester Magistrates Court and Family Court")
        .put("FR_SEList_8", "Hertford County Court and Family Court")
        .put("FR_SEList_9", "High Wycombe County Court and Family Court")
        .put("FR_SEList_10", "Ipswich County Court and Family Hearing Centre")
        .put("FR_SEList_11", "Lewes Combined Court Centre")
        .put("FR_SEList_12", "Luton Justice Centre")
        .put("FR_SEList_13", "Milton Keynes County Court and Family Court")
        .put("FR_SEList_14", "Norwich Combined Court Centre")
        .put("FR_SEList_15", "Oxford Combined Court Centre")
        .put("FR_SEList_16", "Peterborough Combined Court Centre")
        .put("FR_SEList_17", "Reading County Court and Family Court")
        .put("FR_SEList_18", "Slough County Court and Family Court")
        .put("FR_SEList_19", "Southend County Court and Family Court")
        .put("FR_SEList_20", "Watford County Court and Family Court")
        .put("FR_SEList_21", "Thanet County Court and Family Court")
        .build();
    private static Map<String, String> bedfordshireMap = ImmutableMap.<String, String>builder()
        .put(PETERBOROUGH, "Peterborough Combined Court Centre")
        .put(CAMBRIDGE, "Cambridge County Court and Family Court")
        .put(BURY, "Bury St Edmunds County Court and Family Court")
        .put(NORWICH, "Norwich Combined Court Centre")
        .put(IPSWICH, "Ipswich County Court and Faily Hearing Centre")
        .put(CHELMSFORD, "Chelmsford County Court and Family Hearing Centre")
        .put(SOUTHEND, "Southend County Court and Family Court")
        .put(BEDFORD, "Bedford County Court and Family Court")
        .put(LUTON, "Luton Justice Centre")
        .put(HERTFORD, "Hertford County Court and Family Court")
        .put(WATFORD, "Watford County Court and Family Court")
        .build();
    private static Map<String, String> thamesvalleyMap = ImmutableMap.<String, String>builder()
        .put(OXFORD, "Oxford Combined Court Centre")
        .put(READING, "Reading County Court and Family Court")
        .put(MILTON_KEYNES, "Milton Keynes County Court and Family Court")
        .put(SLOUGH, "Slough County Court and Family Court")
        .build();
    private static Map<String, String> swOtherMap = ImmutableMap.<String, String>builder()
        .put("FR_SWList_1", "Aldershot Justice Centre")
        .put("FR_SWList_2", "Yeovil County, Family and Magistrates Court")
        .put("FR_SWList_3", "Winchester Combined Court Centre")
        .put("FR_SWList_4", "Weymouth Combined Court")
        .put("FR_SWList_5", "Weston-Super-Mare County and Family Court")
        .put("FR_SWList_6", "Truro County Court and Family Court")
        .put("FR_SWList_7", "Torquay and Newton Abbot County Court and Family Court")
        .put("FR_SWList_8", "Southampton County and Family Court")
        .put("FR_SWList_9", "Taunton Crown, County and Family Court")
        .put("FR_SWList_10", "Swindon Combined Court")
        .put("FR_SWList_11", "Salisbury Law Courts")
        .put("FR_SWList_12", "Portsmouth Combined Court Centre")
        .put("FR_SWList_13", "Plymouth Combined Court")
        .put("FR_SWList_14", "Isle of Wight Combined Court")
        .put("FR_SWList_15", "Gloucester and Cheltenham County and Family Court")
        .put("FR_SWList_16", "Exeter Combined Court Centre")
        .put("FR_SWList_17", "Bristol Civil and Family Justice Centre")
        .put("FR_SWList_18", "Bodmin County Court and Family Court")
        .put("FR_SWList_19", "Basingstoke County and Family Court")
        .put("FR_SWList_20", "Bournemouth and Poole County Court and Family Court")
        .put("FR_SWList_21", "Bath Law Courts")
        .put("FR_SWList_22", "Barnstaple Magistrates, County and Family Court")
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
    private static Map<String, String> newportMap = ImmutableMap.<String, String>builder()
        .put("FR_newportList_1", "Newport Civil and Family Court")
        .put("FR_newportList_2", "Cardiff Civil and Family Justice Centre")
        .put("FR_newportList_3", "Merthyr Tydfil Combined Court Centre")
        .put("FR_newportList_4", "Pontypridd County and Family Court")
        .put("FR_newportList_5", "Blackwood Civil and Family Court")
        .build();
    private static Map<String, String> humberMap = ImmutableMap.<String, String>builder()
        .put("FR_humberList_1", "Sheffield Family Hearing Centre")
        .put("FR_humberList_2", "Kingston-upon-Hull Combined Court Centre")
        .put("FR_humberList_3", "Doncaster Justice Centre North")
        .put("FR_humberList_4", "Great Grimsby Combined Court Centre")
        .put("FR_humberList_5", "Barnsley Law Courts")
        .build();
    private static Map<String, String> swanseaMap = ImmutableMap.<String, String>builder()
        .put("FR_swanseaList_1", "Swansea Civil and Family Justice Centre")
        .put("FR_swanseaList_2", "Aberystwyth Justice Centre")
        .put("FR_swanseaList_3", "Haverfordwest County and Family Court")
        .put("FR_swanseaList_4", "Carmarthen County and Family Court")
        .put("FR_swanseaList_5", "Llanelli Law Courts")
        .put("FR_swanseaList_6", "Port Talbot Justice Centre")
        .build();
    private static Map<String, String> walesOtherMap = ImmutableMap.<String, String>builder()
        .put("FR_WList_1", "Prestatyn Justice Centre")
        .put("FR_WList_2", "Welshpool Civil and Family Court")
        .put("FR_WList_3", "Wrexham County Court and Family Court")
        .put("FR_WList_4", "Mold County")
        .build();
    private static Map<String, String> northWalesMap = ImmutableMap.<String, String>builder()
        .put(WREXHAM, "Wrexham County Court and Family Court")
        .put(CAERNARFON, "Caernarfon Justice Centre")
        .put(PRESTATYN, "Prestatyn Justice Centre")
        .put(WELSHPOOL, "Welshpool Civil and Family Court")
        .put(MOLD, "Mold County")
        .build();

    private static Map<String, String> highCourtMap = ImmutableMap.<String, String>builder()
        .put("FR_highCourtList_1", "High Court Family Division")
        .build();

    private ConsentedCourtHelper() {
    }

    public static String getSelectedCourt(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();
        String region = (String) caseData.get(REGION);

        if (SOUTHWEST.equalsIgnoreCase(region)) {
            return getSouthWestFRC(caseData);
        }
        if (MIDLANDS.equalsIgnoreCase(region)) {
            return getMidlandFRC(caseData);
        }
        if (NORTHWEST.equalsIgnoreCase(region)) {
            return getNorthWestFRC(caseData);
        }
        if (LONDON.equalsIgnoreCase(region)) {
            return getLondonFRC(caseData);
        }
        if (NORTHEAST.equalsIgnoreCase(region)) {
            return getNorthEastFRC(caseData);
        }
        if (WALES.equalsIgnoreCase(region)) {
            return getWalesFRC(caseData);
        }
        if (SOUTHEAST.equalsIgnoreCase(region)) {
            return getSouthEastFRC(caseData);
        }
        if (HIGHCOURT.equalsIgnoreCase(region)) {
            return getHighCourtFRC(caseData);
        }
        return EMPTY;
    }

    private static String getSouthWestFRC(Map<String, Object> caseData) {
        String southWestList = (String) caseData.get(SOUTHWEST_FRC_LIST);
        if (OTHER.equalsIgnoreCase(southWestList)) {
            return getSwOtherCourt(caseData);
        } else if (DEVON.equalsIgnoreCase(southWestList)) {
            return getDevonCourt(caseData);
        } else if (DORSET.equalsIgnoreCase(southWestList)) {
            return getDorsetCourt(caseData);
        } else if (BRISTOLFRC.equalsIgnoreCase(southWestList)) {
            return getBristolCourt(caseData);
        }
        return EMPTY;
    }

    private static String getSouthEastFRC(Map<String, Object> caseData) {
        String southEastList = (String) caseData.get(SOUTHEAST_FRC_LIST);
        if (KENT.equalsIgnoreCase(southEastList)) {
            return getKentCourt(caseData);
        } else if (OTHER.equalsIgnoreCase(southEastList)) {
            return getSeOtherCourt(caseData);
        } else if (BEDFORDSHIRE.equalsIgnoreCase(southEastList)) {
            return getBedfordshireCourt(caseData);
        } else if (THAMESVALLEY.equalsIgnoreCase(southEastList)) {
            return getThamesValleyCourt(caseData);
        }
        return EMPTY;
    }

    private static String getWalesFRC(Map<String, Object> caseData) {
        String walesList = (String) caseData.get(WALES_FRC_LIST);
        if (NEWPORT.equalsIgnoreCase(walesList)) {
            return getNewportCourt(caseData);
        } else if (SWANSEA.equalsIgnoreCase(walesList)) {
            return getSwanseaCourt(caseData);
        } else if (OTHER.equalsIgnoreCase(walesList)) {
            return getWalesOtherCourt(caseData);
        } else if (NORTHWALES.equalsIgnoreCase(walesList)) {
            return getNorthWalesCourt(caseData);
        }
        return EMPTY;
    }

    private static String getNorthWestFRC(Map<String, Object> caseData) {
        String northWestList = (String) caseData.get(NORTHWEST_FRC_LIST);
        if (LIVERPOOL.equalsIgnoreCase(northWestList)) {
            return getLiverpoolCourt(caseData);
        } else if (MANCHESTER.equalsIgnoreCase(northWestList)) {
            return getManchesterCourt(caseData);
        } else if (OTHER.equalsIgnoreCase(northWestList)) {
            return getNwOtherCourt(caseData);
        } else if (LANCASHIRE.equalsIgnoreCase(northWestList)) {
            return getLancashireCourt(caseData);
        }
        return EMPTY;
    }

    private static String getLondonFRC(Map<String, Object> caseData) {
        String londonList = (String) caseData.get(LONDON_FRC_LIST);
        if (LONDON.equalsIgnoreCase(londonList)) {
            return getLondonCourt(caseData);
        }
        return EMPTY;
    }

    private static String getHighCourtFRC(Map<String, Object> caseData) {
        String londonList = (String) caseData.get(HIGHCOURT_FRC_LIST);
        if (HIGHCOURT.equalsIgnoreCase(londonList)) {
            return getHighCourt(caseData);
        }
        return EMPTY;
    }

    private static String getNorthEastFRC(Map<String, Object> caseData) {
        String northEastList = (String) caseData.get(NORTHEAST_FRC_LIST);
        if (CLEAVELAND.equalsIgnoreCase(northEastList)) {
            return getCleavelandCourt(caseData);
        } else if (NWYORKSHIRE.equalsIgnoreCase(northEastList)) {
            return getNwYorkshireCourt(caseData);
        } else if (HSYORKSHIRE.equalsIgnoreCase(northEastList)) {
            return getHumberCourt(caseData);
        }
        return EMPTY;
    }

    private static String getMidlandFRC(Map<String, Object> caseData) {
        String midlandsList = (String) caseData.get(MIDLANDS_FRC_LIST);
        if (NOTTINGHAM.equalsIgnoreCase(midlandsList)) {
            return getNottinghamCourt(caseData);
        } else if (BIRMINGHAM.equalsIgnoreCase(midlandsList)) {
            return getBirminghamCourt(caseData);
        }
        return EMPTY;
    }

    public static String getBirminghamCourt(Map<String, Object> caseData) {
        return birminghamMap.getOrDefault(caseData.get(BIRMINGHAM_COURTLIST), "");
    }

    public static String getLondonCourt(Map<String, Object> caseData) {
        return londonMap.getOrDefault(caseData.get(LONDON_COURTLIST), "");
    }

    public static String getNottinghamCourt(Map<String, Object> caseData) {
        return nottinghamMap.getOrDefault(caseData.get(NOTTINGHAM_COURTLIST), "");
    }

    public static String getLiverpoolCourt(Map<String, Object> caseData) {
        return liverpoolMap.getOrDefault(caseData.get(LIVERPOOL_COURTLIST), "");
    }

    public static String getNwOtherCourt(Map<String, Object> caseData) {
        return nwOtherMap.getOrDefault(caseData.get(NWOTHER_COURTLIST), "");
    }

    public static String getCleavelandCourt(Map<String, Object> caseData) {
        return cleavelandMap.getOrDefault(caseData.get(CLEAVELAND_COURTLIST), "");
    }

    public static String getManchesterCourt(Map<String, Object> caseData) {
        return manchesterMap.getOrDefault(caseData.get(MANCHESTER_COURTLIST), "");
    }

    public static String getLancashireCourt(Map<String, Object> caseData) {
        return lancashireMap.getOrDefault(caseData.get(LANCASHIRE_COURTLIST), "");
    }

    public static String getNwYorkshireCourt(Map<String, Object> caseData) {
        return yorkshireMap.getOrDefault(caseData.get(NWYORKSHIRE_COURTLIST), "");
    }

    public static String getKentCourt(Map<String, Object> caseData) {
        return kentMap.getOrDefault(caseData.get(KENTFRC_COURTLIST), "");
    }

    public static String getSeOtherCourt(Map<String, Object> caseData) {
        return seOtherMap.getOrDefault(caseData.get(SEOTHER_COURTLIST), "");
    }

    public static String getBedfordshireCourt(Map<String, Object> caseData) {
        return bedfordshireMap.getOrDefault(caseData.get(BEDFORDSHIRE_COURTLIST), "");
    }

    public static String getThamesValleyCourt(Map<String, Object> caseData) {
        return thamesvalleyMap.getOrDefault(caseData.get(THAMESVALLEY_COURTLIST), "");
    }

    public static String getSwOtherCourt(Map<String, Object> caseData) {
        return swOtherMap.getOrDefault(caseData.get(SWOTHER_COURTLIST), "");
    }

    public static String getDevonCourt(Map<String, Object> caseData) {
        return devonMap.getOrDefault(caseData.get(DEVON_COURTLIST), "");
    }

    public static String getDorsetCourt(Map<String, Object> caseData) {
        return dorsetMap.getOrDefault(caseData.get(DORSET_COURTLIST), "");
    }

    public static String getBristolCourt(Map<String, Object> caseData) {
        return bristolMap.getOrDefault(caseData.get(BRISTOL_COURTLIST), "");
    }

    public static String getNewportCourt(Map<String, Object> caseData) {
        return newportMap.getOrDefault(caseData.get(NEWPORT_COURTLIST), "");
    }

    public static String getHumberCourt(Map<String, Object> caseData) {
        return humberMap.getOrDefault(caseData.get(HSYORKSHIRE_COURTLIST), "");
    }

    public static String getSwanseaCourt(Map<String, Object> caseData) {
        return swanseaMap.getOrDefault(caseData.get(SWANSEA_COURTLIST), "");
    }

    public static String getWalesOtherCourt(Map<String, Object> caseData) {
        return walesOtherMap.getOrDefault(caseData.get(WALES_OTHER_COURTLIST), "");
    }

    public static String getNorthWalesCourt(Map<String, Object> caseData) {
        return northWalesMap.getOrDefault(caseData.get(NORTH_WALES_COURTLIST), "");
    }

    public static String getHighCourt(Map<String, Object> caseData) {
        return highCourtMap.getOrDefault(caseData.get(HIGHCOURT_COURTLIST), "");
    }
}
