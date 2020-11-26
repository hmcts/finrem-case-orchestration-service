package uk.gov.hmcts.reform.finrem.caseorchestration.service.migration;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ALDERSHOT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BARNSTAPLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BARROW;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BASINGSTOKE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BATH;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BEDFORD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BEDFORDSHIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BEDFORDSHIRE_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BLACKBURN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BLACKPOOL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BODMIN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BOURNEMOUTH;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BRISTOL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BRISTOLFRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BRISTOL_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BURY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CAMBRIDGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CARLISLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CHELMSFORD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DEVON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DEVON_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DORSET;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DORSET_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.EXETER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GLOUCESTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HERTFORD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.IPSWICH;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ISLE_OF_WIGHT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.KENTFRC_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LANCASHIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LANCASHIRE_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LANCASTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LUTON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MILTON_KEYNES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MOLD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHWALES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHWEST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHWEST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTH_WALES_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORWICH;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NWOTHER_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.OTHER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.OXFORD;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.PETERBOROUGH;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.PLYMOUTH;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.PORTSMOUTH;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.PRESTATYN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.PRESTON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.READING;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SWINDON;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.isConsentedApplication;

@Slf4j
public class Rpet164FrcCourtListMigrationImpl implements MigrationHandler {

    public static final String EMPTY_STRING = "";

    // FRCs
    public static final String KENT = "kent";

    //NW OTHER COURT CODES
    public static final String WEST_CUMBRIA_OLD = "FR_NWList_1";
    public static final String PRESTON_OLD = "FR_NWList_2";
    public static final String LANCASTER_OLD = "FR_NWList_3";
    public static final String CARLISLE_OLD = "FR_NWList_4";
    public static final String BURNLEY_OLD = "FR_NWList_5";
    public static final String BLACKPOOL_OLD = "FR_NWList_6";
    public static final String BLACKBURN_OLD = "FR_NWList_7";
    public static final String BARROW_OLD = "FR_NWList_8";

    //Temporary Court Code
    public static final String LONDON_TEMP = "FR_londonList_12";

    //SE OTHER COURT CODES
    public static final String BASILDON_OLD = "FR_SEList_1";
    public static final String BEDFORD_OLD = "FR_SEList_2";
    public static final String BRIGHTON_OLD = "FR_SEList_3";
    public static final String BURY_OLD = "FR_SEList_4";
    public static final String CAMBRIDGE_OLD = "FR_SEList_5";
    public static final String CHELMSFORD_OLD = "FR_SEList_6";
    public static final String COLCHESTER_OLD = "FR_SEList_7";
    public static final String HERTFORD_OLD = "FR_SEList_8";
    public static final String HIGH_WYCOMBE_OLD = "FR_SEList_9";
    public static final String IPSWICH_OLD = "FR_SEList_10";
    public static final String LEWES_OLD = "FR_SEList_11";
    public static final String LUTON_OLD = "FR_SEList_12";
    public static final String MILTON_KEYNES_OLD = "FR_SEList_13";
    public static final String NORWICH_OLD = "FR_SEList_14";
    public static final String OXFORD_OLD = "FR_SEList_15";
    public static final String PETERBOROUGH_OLD = "FR_SEList_16";
    public static final String READING_OLD = "FR_SEList_17";
    public static final String SLOUGH_OLD = "FR_SEList_18";
    public static final String SOUTHEND_OLD = "FR_SEList_19";
    public static final String WATFORD_OLD = "FR_SEList_20";
    public static final String THANET_OLD = "FR_SEList_21";

    //Kent Surrey Court List
    public static final String BRIGHTON = "FR_kent_surreyList_7";

    //SW Other Court Codes
    public static final String ALDERSHOT_OLD = "FR_SWList_1";
    public static final String YEOVIL_OLD = "FR_SWList_2";
    public static final String WINCHESTER_OLD = "FR_SWList_3";
    public static final String WEYMOUTH_OLD = "FR_SWList_4";
    public static final String WESTON_OLD = "FR_SWList_5";
    public static final String TRURO_OLD = "FR_SWList_6";
    public static final String TORQUAY_OLD = "FR_SWList_7";
    public static final String SOUTHAMPTON_OLD = "FR_SWList_8";
    public static final String TAUNTON_OLD = "FR_SWList_9";
    public static final String SWINDON_OLD = "FR_SWList_10";
    public static final String SALISBURY_OLD = "FR_SWList_11";
    public static final String PORTSMOUTH_OLD = "FR_SWList_12";
    public static final String PLYMOUTH_OLD = "FR_SWList_13";
    public static final String NEWPORT_OLD = "FR_SWList_14";
    public static final String GLOUCESTER_OLD = "FR_SWList_15";
    public static final String EXETER_OLD = "FR_SWList_16";
    public static final String BRISTOL_OLD = "FR_SWList_17";
    public static final String BODMIN_OLD = "FR_SWList_18";
    public static final String BASINGSTOKE_OLD = "FR_SWList_19";
    public static final String BOURNEMOUTH_OLD = "FR_SWList_20";
    public static final String BATH_OLD = "FR_SWList_21";
    public static final String BARNSTAPLE_OLD = "FR_SWList_22";

    //Welsh Other Court Codes
    public static final String PRESTATYN_OLD = "FR_WList_1";
    public static final String WELSHPOOL_OLD = "FR_WList_2";
    public static final String WREXHAM_OLD = "FR_WList_3";
    public static final String MOLD_OLD = "FR_WList_4";

    @Override
    public Map<String, Object> migrate(CaseDetails caseDetails) {
        if (migrationRequired(caseDetails)) {
            log.info("Applying RPET-164 case migration for case {}", caseDetails.getId());
            return migrateCaseData(caseDetails);
        }
        return caseDetails.getData();
    }

    private boolean migrationRequired(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();
        return isConsentedApplication(caseDetails) && hasRegionList(caseData);
    }

    private boolean hasRegionList(Map<String, Object> caseData) {
        return caseData.containsKey(REGION);
    }

    private Map<String, Object> migrateCaseData(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();
        String region = (String) caseData.getOrDefault(REGION, EMPTY_STRING);
        String frcValue;
        String selectedHC;
        switch (region) {
            case NORTHWEST:
                frcValue = (String) caseData.getOrDefault(NORTHWEST_FRC_LIST, EMPTY_STRING);
                if (frcValue.equals(OTHER)) {
                    caseData.put(NORTHWEST_FRC_LIST, LANCASHIRE);
                    selectedHC = (String) caseData.getOrDefault(NWOTHER_COURTLIST, EMPTY_STRING);
                    caseData.remove(NWOTHER_COURTLIST);
                    switch (selectedHC) {
                        case WEST_CUMBRIA_OLD:
                            caseData.put(LANCASHIRE_COURTLIST, WEST_CUMBRIA);
                            break;
                        case PRESTON_OLD:
                            caseData.put(LANCASHIRE_COURTLIST, PRESTON);
                            break;
                        case LANCASTER_OLD:
                            caseData.put(LANCASHIRE_COURTLIST, LANCASTER);
                            break;
                        case CARLISLE_OLD:
                            caseData.put(LANCASHIRE_COURTLIST, CARLISLE);
                            break;
                        case BLACKPOOL_OLD:
                            caseData.put(LANCASHIRE_COURTLIST, BLACKPOOL);
                            break;
                        case BLACKBURN_OLD:
                            caseData.put(LANCASHIRE_COURTLIST, BLACKBURN);
                            break;
                        case BARROW_OLD:
                            caseData.put(LANCASHIRE_COURTLIST, BARROW);
                            break;
                        default:
                            caseData.remove(NORTHWEST_FRC_LIST);
                            handleUnknown(caseDetails, caseData, region, frcValue, selectedHC);
                            break;
                    }
                }
                break;
            case SOUTHEAST:
                frcValue = (String) caseData.getOrDefault(SOUTHEAST_FRC_LIST, EMPTY_STRING);
                if (frcValue.equals(OTHER)) {
                    caseData.remove(SOUTHEAST_FRC_LIST);
                    selectedHC = (String) caseData.getOrDefault(SEOTHER_COURTLIST, EMPTY_STRING);
                    caseData.remove(SEOTHER_COURTLIST);
                    switch (selectedHC) {
                        case PETERBOROUGH_OLD:
                            caseData.put(BEDFORDSHIRE_COURTLIST, PETERBOROUGH);
                            caseData.put(SOUTHEAST_FRC_LIST, BEDFORDSHIRE);
                            break;
                        case CAMBRIDGE_OLD:
                            caseData.put(BEDFORDSHIRE_COURTLIST, CAMBRIDGE);
                            caseData.put(SOUTHEAST_FRC_LIST, BEDFORDSHIRE);
                            break;
                        case BURY_OLD:
                            caseData.put(BEDFORDSHIRE_COURTLIST, BURY);
                            caseData.put(SOUTHEAST_FRC_LIST, BEDFORDSHIRE);
                            break;
                        case NORWICH_OLD:
                            caseData.put(BEDFORDSHIRE_COURTLIST, NORWICH);
                            caseData.put(SOUTHEAST_FRC_LIST, BEDFORDSHIRE);
                            break;
                        case IPSWICH_OLD:
                            caseData.put(BEDFORDSHIRE_COURTLIST, IPSWICH);
                            caseData.put(SOUTHEAST_FRC_LIST, BEDFORDSHIRE);
                            break;
                        case CHELMSFORD_OLD:
                            caseData.put(BEDFORDSHIRE_COURTLIST, CHELMSFORD);
                            caseData.put(SOUTHEAST_FRC_LIST, BEDFORDSHIRE);
                            break;
                        case SOUTHEND_OLD:
                            caseData.put(BEDFORDSHIRE_COURTLIST, SOUTHEND);
                            caseData.put(SOUTHEAST_FRC_LIST, BEDFORDSHIRE);
                            break;
                        case BEDFORD_OLD:
                            caseData.put(BEDFORDSHIRE_COURTLIST, BEDFORD);
                            caseData.put(SOUTHEAST_FRC_LIST, BEDFORDSHIRE);
                            break;
                        case LUTON_OLD:
                            caseData.put(BEDFORDSHIRE_COURTLIST, LUTON);
                            caseData.put(SOUTHEAST_FRC_LIST, BEDFORDSHIRE);
                            break;
                        case HERTFORD_OLD:
                            caseData.put(BEDFORDSHIRE_COURTLIST, HERTFORD);
                            caseData.put(SOUTHEAST_FRC_LIST, BEDFORDSHIRE);
                            break;
                        case WATFORD_OLD:
                            caseData.put(BEDFORDSHIRE_COURTLIST, WATFORD);
                            caseData.put(SOUTHEAST_FRC_LIST, BEDFORDSHIRE);
                            break;
                        case OXFORD_OLD:
                            caseData.put(THAMESVALLEY_COURTLIST, OXFORD);
                            caseData.put(SOUTHEAST_FRC_LIST, THAMESVALLEY);
                            break;
                        case READING_OLD:
                            caseData.put(THAMESVALLEY_COURTLIST, READING);
                            caseData.put(SOUTHEAST_FRC_LIST, THAMESVALLEY);
                            break;
                        case MILTON_KEYNES_OLD:
                            caseData.put(THAMESVALLEY_COURTLIST, MILTON_KEYNES);
                            caseData.put(SOUTHEAST_FRC_LIST, THAMESVALLEY);
                            break;
                        case SLOUGH_OLD:
                            caseData.put(THAMESVALLEY_COURTLIST, SLOUGH);
                            caseData.put(SOUTHEAST_FRC_LIST, THAMESVALLEY);
                            break;
                        case BRIGHTON_OLD:
                            caseData.put(SOUTHEAST_FRC_LIST, KENT);
                            caseData.put(KENTFRC_COURTLIST, BRIGHTON);
                            break;
                        default:
                            handleUnknown(caseDetails, caseData, region, frcValue, selectedHC);
                            break;
                    }
                }
                break;
            case SOUTHWEST:
                frcValue = (String) caseData.getOrDefault(SOUTHWEST_FRC_LIST, EMPTY_STRING);
                if (frcValue.equals(OTHER)) {
                    caseData.remove(SOUTHWEST_FRC_LIST);
                    selectedHC = (String) caseData.getOrDefault(SWOTHER_COURTLIST, EMPTY_STRING);
                    caseData.remove(SWOTHER_COURTLIST);
                    switch (selectedHC) {
                        case PLYMOUTH_OLD:
                            caseData.put(DEVON_COURTLIST, PLYMOUTH);
                            caseData.put(SOUTHWEST_FRC_LIST, DEVON);
                            break;
                        case EXETER_OLD:
                            caseData.put(DEVON_COURTLIST, EXETER);
                            caseData.put(SOUTHWEST_FRC_LIST, DEVON);
                            break;
                        case TAUNTON_OLD:
                            caseData.put(DEVON_COURTLIST, TAUNTON);
                            caseData.put(SOUTHWEST_FRC_LIST, DEVON);
                            break;
                        case TORQUAY_OLD:
                            caseData.put(DEVON_COURTLIST, TORQUAY);
                            caseData.put(SOUTHWEST_FRC_LIST, DEVON);
                            break;
                        case BARNSTAPLE_OLD:
                            caseData.put(DEVON_COURTLIST, BARNSTAPLE);
                            caseData.put(SOUTHWEST_FRC_LIST, DEVON);
                            break;
                        case TRURO_OLD:
                            caseData.put(DEVON_COURTLIST, TRURO);
                            caseData.put(SOUTHWEST_FRC_LIST, DEVON);
                            break;
                        case YEOVIL_OLD:
                            caseData.put(DEVON_COURTLIST, YEOVIL);
                            caseData.put(SOUTHWEST_FRC_LIST, DEVON);
                            break;
                        case BODMIN_OLD:
                            caseData.put(DEVON_COURTLIST, BODMIN);
                            caseData.put(SOUTHWEST_FRC_LIST, DEVON);
                            break;
                        case BOURNEMOUTH_OLD:
                            caseData.put(DORSET_COURTLIST, BOURNEMOUTH);
                            caseData.put(SOUTHWEST_FRC_LIST, DORSET);
                            break;
                        case WEYMOUTH_OLD:
                            caseData.put(DORSET_COURTLIST, WEYMOUTH);
                            caseData.put(SOUTHWEST_FRC_LIST, DORSET);
                            break;
                        case WINCHESTER_OLD:
                            caseData.put(DORSET_COURTLIST, WINCHESTER);
                            caseData.put(SOUTHWEST_FRC_LIST, DORSET);
                            break;
                        case PORTSMOUTH_OLD:
                            caseData.put(DORSET_COURTLIST, PORTSMOUTH);
                            caseData.put(SOUTHWEST_FRC_LIST, DORSET);
                            break;
                        case SOUTHAMPTON_OLD:
                            caseData.put(DORSET_COURTLIST, SOUTHAMPTON);
                            caseData.put(SOUTHWEST_FRC_LIST, DORSET);
                            break;
                        case ALDERSHOT_OLD:
                            caseData.put(DORSET_COURTLIST, ALDERSHOT);
                            caseData.put(SOUTHWEST_FRC_LIST, DORSET);
                            break;
                        case BASINGSTOKE_OLD:
                            caseData.put(DORSET_COURTLIST, BASINGSTOKE);
                            caseData.put(SOUTHWEST_FRC_LIST, DORSET);
                            break;
                        case NEWPORT_OLD:
                            caseData.put(DORSET_COURTLIST, ISLE_OF_WIGHT);
                            caseData.put(SOUTHWEST_FRC_LIST, DORSET);
                            break;
                        case BRISTOL_OLD:
                            caseData.put(BRISTOL_COURTLIST, BRISTOL);
                            caseData.put(SOUTHWEST_FRC_LIST, BRISTOLFRC);
                            break;
                        case GLOUCESTER_OLD:
                            caseData.put(BRISTOL_COURTLIST, GLOUCESTER);
                            caseData.put(SOUTHWEST_FRC_LIST, BRISTOLFRC);
                            break;
                        case SWINDON_OLD:
                            caseData.put(BRISTOL_COURTLIST, SWINDON);
                            caseData.put(SOUTHWEST_FRC_LIST, BRISTOLFRC);
                            break;
                        case SALISBURY_OLD:
                            caseData.put(BRISTOL_COURTLIST, SALISBURY);
                            caseData.put(SOUTHWEST_FRC_LIST, BRISTOLFRC);
                            break;
                        case BATH_OLD:
                            caseData.put(BRISTOL_COURTLIST, BATH);
                            caseData.put(SOUTHWEST_FRC_LIST, BRISTOLFRC);
                            break;
                        case WESTON_OLD:
                            caseData.put(BRISTOL_COURTLIST, WESTON);
                            caseData.put(SOUTHWEST_FRC_LIST, BRISTOLFRC);
                            break;
                        default:
                            handleUnknown(caseDetails, caseData, region, frcValue, selectedHC);
                            break;
                    }
                }
                break;
            case WALES:
                frcValue = (String) caseData.getOrDefault(WALES_FRC_LIST, EMPTY_STRING);
                if (frcValue.equals(OTHER)) {
                    caseData.put(WALES_FRC_LIST, NORTHWALES);
                    selectedHC = (String) caseData.getOrDefault(WALES_OTHER_COURTLIST, EMPTY_STRING);
                    caseData.remove(WALES_OTHER_COURTLIST);
                    switch (selectedHC) {
                        case WREXHAM_OLD:
                            caseData.put(NORTH_WALES_COURTLIST, WREXHAM);
                            break;
                        case PRESTATYN_OLD:
                            caseData.put(NORTH_WALES_COURTLIST, PRESTATYN);
                            break;
                        case WELSHPOOL_OLD:
                            caseData.put(NORTH_WALES_COURTLIST, WELSHPOOL);
                            break;
                        case MOLD_OLD:
                            caseData.put(NORTH_WALES_COURTLIST, MOLD);
                            break;
                        default:
                            caseData.remove(WALES_FRC_LIST);
                            handleUnknown(caseDetails, caseData, region, frcValue, selectedHC);
                            break;
                    }
                }
                break;
            default:
                return caseData;
        }
        return caseData;
    }

    private void handleUnknown(CaseDetails caseDetails, Map<String, Object> caseData, String region, String frcValue, String selectedHC) {
        caseData.put(REGION, LONDON);
        caseData.put(LONDON_FRC_LIST, LONDON);
        caseData.put(LONDON_COURTLIST, LONDON_TEMP);
        log.info("Unknown hearing centre:{} in FRC:{} region:{} for case:{} Moving to London>Temp", selectedHC, frcValue, region,
            caseDetails.getId());
    }
}
