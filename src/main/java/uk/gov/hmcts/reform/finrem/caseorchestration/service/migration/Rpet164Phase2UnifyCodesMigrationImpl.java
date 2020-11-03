package uk.gov.hmcts.reform.finrem.caseorchestration.service.migration;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BIRMINGHAM_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CFC_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HUMBER_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.KENTFRC_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LIVERPOOL_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MANCHESTER_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIDLANDS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIDLANDS_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NEWPORT_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHEAST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHEAST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHWEST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHWEST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTTINGHAM_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NWYORKSHIRE_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.REGION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHEAST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHEAST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SWANSEA_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WALES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WALES_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.isContestedApplication;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.migration.CourtDetailsMigration.BIRMINGHAM_COURT_LIST;

@Slf4j
public class Rpet164Phase2UnifyCodesMigrationImpl implements MigrationHandler {
    public static final String EMPTY_STRING = "";

    // FRCs
    public static final String BIRMINGHAM = "birmingham";
    public static final String BRISTOLFRC = "bristol";
    public static final String CFC = "cfc";
    public static final String CLEAVELAND_OLD = "cleaveland";
    public static final String CLEAVELAND = "cleaveland";
    public static final String HSYORKSHIRE = "hsyorkshire";
    public static final String KENT_OLD = "kentfrc";
    public static final String KENT = "kent";
    public static final String LONDONFRC = "london";
    public static final String LIVERPOOL = "liverpool";
    public static final String MANCHESTER = "manchester";
    public static final String NEWPORT = "newport";
    public static final String NOTTINGHAM = "nottingham";
    public static final String NWYORKSHIRE = "nwyorkshire";
    public static final String SWANSEA = "swansea";
    public static final String THAMESVALLEY = "thamesvalley";

    // Court lists
    public static final String CLEAVELAND_COURT_LIST_OLD = "cleavelandCourtList";
    public static final String CLEAVELAND_COURT_LIST = "clevelandCourtList";

    //CFC  Court Codes
    public static final String CENTRAL_OLD = "FR_s_CFCList_9";
    public static final String WILLESDEN_OLD = "FR_s_CFCList_16";
    public static final String UXBRIDGE_OLD = "FR_s_CFCList_14";
    public static final String EAST_LONDON_OLD = "FR_s_CFCList_11";
    public static final String BRENTFORD_OLD = "FR_s_CFCList_8";
    public static final String BARNET_OLD = "FR_s_CFCList_6";
    public static final String ROMFORD_OLD = "FR_s_CFCList_5";
    public static final String KINGSTON_UPON_THAMES_OLD = "FR_s_CFCList_4";
    public static final String EDMONTON_OLD = "FR_s_CFCList_3";
    public static final String CROYDON_OLD = "FR_s_CFCList_2";
    public static final String BROMLEY_OLD = "FR_s_CFCList_1";

    //London Court Codes
    public static final String CENTRAL = "FR_londonList_1";
    public static final String WILLESDEN = "FR_londonList_2";
    public static final String UXBRIDGE = "FR_londonList_3";
    public static final String EAST_LONDON = "FR_londonList_4";
    public static final String BRENTFORD = "FR_londonList_5";
    public static final String BARNET = "FR_londonList_6";
    public static final String ROMFORD = "FR_londonList_7";
    public static final String KINGSTON_UPON_THAMES = "FR_londonList_8";
    public static final String EDMONTON = "FR_londonList_9";
    public static final String CROYDON = "FR_londonList_10";
    public static final String BROMLEY = "FR_londonList_11";

    @Override
    public Map<String, Object> migrate(CaseDetails caseDetails) {
        if (migrationRequired(caseDetails)) {
            log.info("Applying RPET-164 unify HC element codes for {}", caseDetails.getId());
            return migrateCaseData(caseDetails);
        }
        return caseDetails.getData();
    }

    private boolean migrationRequired(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();
        return isContestedApplication(caseDetails) && hasRegionList(caseData);
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
            case MIDLANDS:
                frcValue = (String) caseData.getOrDefault(MIDLANDS_FRC_LIST, EMPTY_STRING);
                switch (frcValue) {
                    case BIRMINGHAM:
                        selectedHC = (String) caseData.getOrDefault(BIRMINGHAM_COURTLIST, EMPTY_STRING);
                        caseData.put(BIRMINGHAM_COURT_LIST, selectedHC.replace("_hc_l", "L"));
                        break;
                    case NOTTINGHAM:
                        selectedHC = (String) caseData.getOrDefault(NOTTINGHAM_COURTLIST, EMPTY_STRING);
                        caseData.put(NOTTINGHAM_COURTLIST, selectedHC.replace("s_N", "n"));
                        break;
                    default:
                        log.info("Unknown Midlands FRC {} for {}", frcValue, caseDetails.getId());
                        break;
                }
                break;
            case LONDON:
                frcValue = (String) caseData.getOrDefault(LONDON_FRC_LIST, EMPTY_STRING);
                if (frcValue.equalsIgnoreCase(CFC)) {
                    selectedHC = (String) caseData.getOrDefault(CFC_COURTLIST, EMPTY_STRING);
                    caseData.remove(CFC_COURTLIST);
                    caseData.put(LONDON_FRC_LIST, LONDONFRC);
                    switch (selectedHC) {
                        case CENTRAL_OLD:
                            caseData.put(LONDON_COURTLIST, CENTRAL);
                            break;
                        case WILLESDEN_OLD:
                            caseData.put(LONDON_COURTLIST, WILLESDEN);
                            break;
                        case UXBRIDGE_OLD:
                            caseData.put(LONDON_COURTLIST, UXBRIDGE);
                            break;
                        case EAST_LONDON_OLD:
                            caseData.put(LONDON_COURTLIST, EAST_LONDON);
                            break;
                        case BRENTFORD_OLD:
                            caseData.put(LONDON_COURTLIST, BRENTFORD);
                            break;
                        case BARNET_OLD:
                            caseData.put(LONDON_COURTLIST, BARNET);
                            break;
                        case ROMFORD_OLD:
                            caseData.put(LONDON_COURTLIST, ROMFORD);
                            break;
                        case KINGSTON_UPON_THAMES_OLD:
                            caseData.put(LONDON_COURTLIST, KINGSTON_UPON_THAMES);
                            break;
                        case EDMONTON_OLD:
                            caseData.put(LONDON_COURTLIST, EDMONTON);
                            break;
                        case CROYDON_OLD:
                            caseData.put(LONDON_COURTLIST, CROYDON);
                            break;
                        case BROMLEY_OLD:
                            caseData.put(LONDON_COURTLIST, BROMLEY);
                            break;
                        default:
                            log.info("Unknown hearing centre {} for {}", selectedHC, caseDetails.getId());
                            caseData.put(LONDON_COURTLIST, selectedHC);
                            break;
                    }
                } else {
                    log.info("Unknown London FRC {} for {}", frcValue, caseDetails.getId());
                }
                break;
            case NORTHWEST:
                frcValue = (String) caseData.getOrDefault(NORTHWEST_FRC_LIST, EMPTY_STRING);
                switch (frcValue) {
                    case LIVERPOOL:
                        selectedHC = (String) caseData.getOrDefault(LIVERPOOL_COURTLIST, EMPTY_STRING);
                        caseData.put(LIVERPOOL_COURTLIST, selectedHC.replace("_hc_l", "L"));
                        break;
                    case MANCHESTER:
                        selectedHC = (String) caseData.getOrDefault(MANCHESTER_COURTLIST, EMPTY_STRING);
                        caseData.put(MANCHESTER_COURTLIST, selectedHC.replace("_hc_l", "L"));
                        break;
                    default:
                        log.info("Unknown Northwest FRC {} for {}", frcValue, caseDetails.getId());
                        break;
                }
                break;
            case NORTHEAST:
                frcValue = (String) caseData.getOrDefault(NORTHEAST_FRC_LIST, EMPTY_STRING);
                switch (frcValue) {
                    case CLEAVELAND_OLD:
                        selectedHC = (String) caseData.getOrDefault(CLEAVELAND_COURT_LIST_OLD, EMPTY_STRING);
                        caseData.put(CLEAVELAND_COURT_LIST, selectedHC.replace("cleaveland_hc_l", "clevelandL"));
                        caseData.remove(CLEAVELAND_COURT_LIST_OLD);
                        caseData.put(NORTHEAST_FRC_LIST, CLEAVELAND);
                        break;
                    case NWYORKSHIRE:
                        selectedHC = (String) caseData.getOrDefault(NWYORKSHIRE_COURTLIST, EMPTY_STRING);
                        caseData.put(NWYORKSHIRE_COURTLIST, selectedHC.replace("_hc_l", "L"));
                        break;
                    case HSYORKSHIRE:
                        selectedHC = (String) caseData.getOrDefault(HUMBER_COURTLIST, EMPTY_STRING);
                        caseData.put(HUMBER_COURTLIST, selectedHC.replace("_hc_l", "L"));
                        break;
                    default:
                        log.info("Unknown Northeast FRC {} for {}", frcValue, caseDetails.getId());
                        break;
                }
                break;
            case SOUTHEAST:
                frcValue = (String) caseData.getOrDefault(SOUTHEAST_FRC_LIST, EMPTY_STRING);
                if (KENT_OLD.equals(frcValue)) {
                    selectedHC = (String) caseData.getOrDefault(KENTFRC_COURTLIST, EMPTY_STRING);
                    caseData.put(KENTFRC_COURTLIST, selectedHC.replace("_hc_l", "L"));
                    caseData.put(SOUTHEAST_FRC_LIST, KENT);
                } else {
                    log.info("Unknown Southeast FRC {} for {}", frcValue, caseDetails.getId());
                }
                break;
            case WALES:
                frcValue = (String) caseData.getOrDefault(WALES_FRC_LIST, EMPTY_STRING);
                switch (frcValue) {
                    case NEWPORT:
                        selectedHC = (String) caseData.getOrDefault(NEWPORT_COURTLIST, EMPTY_STRING);
                        caseData.put(NEWPORT_COURTLIST, selectedHC.replace("_hc_l", "L"));
                        break;
                    case SWANSEA:
                        selectedHC = (String) caseData.getOrDefault(SWANSEA_COURTLIST, EMPTY_STRING);
                        caseData.put(SWANSEA_COURTLIST, selectedHC.replace("_hc_l", "L"));
                        break;
                    default:
                        log.info("Unknown Wales FRC {} for {}", frcValue, caseDetails.getId());
                        break;
                }
                break;
            default:
                log.info("Unknown region {} for {}", region, caseDetails.getId());
                return caseData;
        }
        return caseData;
    }
}
