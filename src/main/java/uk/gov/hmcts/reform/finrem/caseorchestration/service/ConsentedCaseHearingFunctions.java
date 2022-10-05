package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Map;
import java.util.Objects;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BEDFORDSHIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BEDFORDSHIRE_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BIRMINGHAM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BIRMINGHAM_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BRISTOLFRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BRISTOL_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CLEAVELAND;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CLEAVELAND_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DEVON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DEVON_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DORSET;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DORSET_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HSYORKSHIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HSYORKSHIRE_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.KENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.KENTFRC_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LANCASHIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LANCASHIRE_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LIVERPOOL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LIVERPOOL_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON_COURTLIST;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTH_WALES_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTTINGHAM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTTINGHAM_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NWOTHER_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NWYORKSHIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NWYORKSHIRE_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.OTHER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.REGION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SEOTHER_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHEAST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHEAST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHWEST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHWEST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SWANSEA;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SWANSEA_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SWOTHER_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.THAMESVALLEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.THAMESVALLEY_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WALES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WALES_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WALES_OTHER_COURTLIST;

public class ConsentedCaseHearingFunctions {

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
        return Objects.toString(caseData.get(BIRMINGHAM_COURTLIST), EMPTY);
    }

    public static String getLondonCourt(Map<String, Object> caseData) {
        return Objects.toString(caseData.get(LONDON_COURTLIST), EMPTY);
    }

    public static String getNottinghamCourt(Map<String, Object> caseData) {
        return Objects.toString(caseData.get(NOTTINGHAM_COURTLIST), EMPTY);
    }

    public static String getLiverpoolCourt(Map<String, Object> caseData) {
        return Objects.toString(caseData.get(LIVERPOOL_COURTLIST), EMPTY);
    }

    public static String getNwOtherCourt(Map<String, Object> caseData) {
        return Objects.toString(caseData.get(NWOTHER_COURTLIST), EMPTY);
    }

    public static String getCleavelandCourt(Map<String, Object> caseData) {
        return Objects.toString(caseData.get(CLEAVELAND_COURTLIST), EMPTY);
    }

    public static String getManchesterCourt(Map<String, Object> caseData) {
        return Objects.toString(caseData.get(MANCHESTER_COURTLIST), EMPTY);
    }

    public static String getLancashireCourt(Map<String, Object> caseData) {
        return Objects.toString(caseData.get(LANCASHIRE_COURTLIST), EMPTY);
    }

    public static String getNwYorkshireCourt(Map<String, Object> caseData) {
        return Objects.toString(caseData.get(NWYORKSHIRE_COURTLIST), EMPTY);
    }

    public static String getKentCourt(Map<String, Object> caseData) {
        return Objects.toString(caseData.get(KENTFRC_COURTLIST), EMPTY);
    }

    public static String getSeOtherCourt(Map<String, Object> caseData) {
        return Objects.toString(caseData.get(SEOTHER_COURTLIST), EMPTY);
    }

    public static String getBedfordshireCourt(Map<String, Object> caseData) {
        return Objects.toString(caseData.get(BEDFORDSHIRE_COURTLIST), EMPTY);
    }

    public static String getThamesValleyCourt(Map<String, Object> caseData) {
        return Objects.toString(caseData.get(THAMESVALLEY_COURTLIST), EMPTY);
    }

    public static String getSwOtherCourt(Map<String, Object> caseData) {
        return Objects.toString(caseData.get(SWOTHER_COURTLIST), EMPTY);
    }

    public static String getDevonCourt(Map<String, Object> caseData) {
        return Objects.toString(caseData.get(DEVON_COURTLIST), EMPTY);
    }

    public static String getDorsetCourt(Map<String, Object> caseData) {
        return Objects.toString(caseData.get(DORSET_COURTLIST), "");
    }

    public static String getBristolCourt(Map<String, Object> caseData) {
        return Objects.toString(caseData.get(BRISTOL_COURTLIST), "");
    }

    public static String getNewportCourt(Map<String, Object> caseData) {
        return Objects.toString(caseData.get(NEWPORT_COURTLIST), "");
    }

    public static String getHumberCourt(Map<String, Object> caseData) {
        return Objects.toString(caseData.get(HSYORKSHIRE_COURTLIST), "");
    }

    public static String getSwanseaCourt(Map<String, Object> caseData) {
        return Objects.toString(caseData.get(SWANSEA_COURTLIST), "");
    }

    public static String getWalesOtherCourt(Map<String, Object> caseData) {
        return Objects.toString(caseData.get(WALES_OTHER_COURTLIST), "");
    }

    public static String getNorthWalesCourt(Map<String, Object> caseData) {
        return Objects.toString(caseData.get(NORTH_WALES_COURTLIST), "");
    }
}
