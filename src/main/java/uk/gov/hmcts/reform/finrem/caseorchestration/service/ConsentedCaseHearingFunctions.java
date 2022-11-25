package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CourtList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionMidlandsFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionNorthEastFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionNorthWestFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionSouthEastFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionSouthWestFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionWalesFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DefaultCourtListWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.RegionWrapper;

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

    private ConsentedCaseHearingFunctions() {

    }

    public static String getSelectedCourt(Map<String, Object> caseData) {
        return switch (Objects.toString(caseData.get(REGION), EMPTY)) {
            case SOUTHWEST -> getSouthWestFRC(caseData);
            case MIDLANDS -> getMidlandFRC(caseData);
            case NORTHWEST -> getNorthWestFRC(caseData);
            case LONDON -> getLondonFRC(caseData);
            case NORTHEAST -> getNorthEastFRC(caseData);
            case WALES -> getWalesFRC(caseData);
            case SOUTHEAST -> getSouthEastFRC(caseData);
            default -> EMPTY;
        };
    }

    public static CourtList getSelectedCourt(FinremCaseData caseData) {
        RegionWrapper courtInfo = caseData.getRegionWrapper();
        return switch (courtInfo.getDefaultRegionWrapper().getRegionList()) {
            case SOUTHWEST -> getSouthWestFRC(caseData);
            case MIDLANDS -> getMidlandFRC(caseData);
            case NORTHWEST -> getNorthWestFRC(caseData);
            case LONDON -> getLondonFRC(caseData);
            case NORTHEAST -> getNorthEastFRC(caseData);
            case WALES -> getWalesFRC(caseData);
            case SOUTHEAST -> getSouthEastFRC(caseData);
        };
    }

    private static String getSouthWestFRC(Map<String, Object> caseData) {
        return switch (Objects.toString(caseData.get(SOUTHWEST_FRC_LIST), EMPTY)) {
            case OTHER -> Objects.toString(caseData.get(SWOTHER_COURTLIST), EMPTY);
            case DEVON -> Objects.toString(caseData.get(DEVON_COURTLIST), EMPTY);
            case DORSET -> Objects.toString(caseData.get(DORSET_COURTLIST), "");
            case BRISTOLFRC -> Objects.toString(caseData.get(BRISTOL_COURTLIST), "");
            default -> EMPTY;
        };
    }

    private static CourtList getSouthWestFRC(FinremCaseData caseData) {
        RegionSouthWestFrc southWestFrc = caseData.getRegionWrapper().getDefaultRegionWrapper().getSouthWestFrcList();
        DefaultCourtListWrapper courts = caseData.getRegionWrapper().getDefaultRegionWrapper().getDefaultCourtListWrapper();
        return switch (southWestFrc) {
            case DEVON -> courts.getDevonCourtList();
            case DORSET -> courts.getDorsetCourtList();
            case BRISTOL -> courts.getBristolCourt();
        };
    }

    private static String getSouthEastFRC(Map<String, Object> caseData) {
        return switch (Objects.toString(caseData.get(SOUTHEAST_FRC_LIST), EMPTY)) {
            case KENT -> Objects.toString(caseData.get(KENTFRC_COURTLIST), EMPTY);
            case OTHER -> Objects.toString(caseData.get(SEOTHER_COURTLIST), EMPTY);
            case BEDFORDSHIRE -> Objects.toString(caseData.get(BEDFORDSHIRE_COURTLIST), EMPTY);
            case THAMESVALLEY -> Objects.toString(caseData.get(THAMESVALLEY_COURTLIST), EMPTY);
            default -> EMPTY;
        };
    }

    private static CourtList getSouthEastFRC(FinremCaseData caseData) {
        RegionSouthEastFrc southEastFrcList = caseData.getRegionWrapper().getDefaultRegionWrapper().getSouthEastFrcList();
        DefaultCourtListWrapper courts = caseData.getRegionWrapper().getDefaultRegionWrapper().getDefaultCourtListWrapper();

        return switch (southEastFrcList) {
            case KENT -> courts.getKentSurreyCourt();
            case BEDFORDSHIRE -> courts.getBedfordshireCourt();
            case THAMES_VALLEY -> courts.getThamesValleyCourt();
        };
    }

    private static String getWalesFRC(Map<String, Object> caseData) {
        return switch (Objects.toString(caseData.get(WALES_FRC_LIST), EMPTY)) {
            case NEWPORT -> Objects.toString(caseData.get(NEWPORT_COURTLIST), EMPTY);
            case SWANSEA -> Objects.toString(caseData.get(SWANSEA_COURTLIST), EMPTY);
            case OTHER -> Objects.toString(caseData.get(WALES_OTHER_COURTLIST), EMPTY);
            case NORTHWALES ->  Objects.toString(caseData.get(NORTH_WALES_COURTLIST), EMPTY);
            default -> EMPTY;
        };
    }

    private static CourtList getWalesFRC(FinremCaseData caseData) {
        RegionWalesFrc walesFrc = caseData.getRegionWrapper().getDefaultRegionWrapper().getWalesFrcList();
        DefaultCourtListWrapper courts = caseData.getRegionWrapper().getDefaultRegionWrapper().getDefaultCourtListWrapper();

        return switch (walesFrc) {
            case NORTH_WALES -> courts.getNorthWalesCourt();
            case NEWPORT -> courts.getNewportCourt();
            case SWANSEA -> courts.getSwanseaCourt();
        };
    }

    private static String getNorthWestFRC(Map<String, Object> caseData) {
        return switch (Objects.toString(caseData.get(NORTHWEST_FRC_LIST), EMPTY)) {
            case LIVERPOOL -> Objects.toString(caseData.get(LIVERPOOL_COURTLIST), EMPTY);
            case MANCHESTER -> Objects.toString(caseData.get(MANCHESTER_COURTLIST), EMPTY);
            case OTHER -> Objects.toString(caseData.get(NWOTHER_COURTLIST), EMPTY);
            case LANCASHIRE -> Objects.toString(caseData.get(LANCASHIRE_COURTLIST), EMPTY);
            default -> EMPTY;
        };
    }

    private static CourtList getNorthWestFRC(FinremCaseData caseData) {
        RegionNorthWestFrc northWestFrc = caseData.getRegionWrapper().getDefaultRegionWrapper().getNorthWestFrcList();
        DefaultCourtListWrapper courts = caseData.getRegionWrapper().getDefaultRegionWrapper().getDefaultCourtListWrapper();

        return switch (northWestFrc) {
            case LIVERPOOL -> courts.getLiverpoolCourt();
            case LANCASHIRE -> courts.getLancashireCourt();
            case MANCHESTER -> courts.getManchesterCourt();
        };
    }

    private static String getLondonFRC(Map<String, Object> caseData) {
        String londonList = Objects.toString(caseData.get(LONDON_FRC_LIST), EMPTY);
        if (LONDON.equalsIgnoreCase(londonList)) {
            return Objects.toString(caseData.get(LONDON_COURTLIST), EMPTY);
        }
        return EMPTY;
    }

    private static CourtList getLondonFRC(FinremCaseData caseData) {
        return caseData.getRegionWrapper().getDefaultRegionWrapper().getDefaultCourtListWrapper().getLondonCourtList();
    }

    private static String getNorthEastFRC(Map<String, Object> caseData) {
        return switch (Objects.toString(caseData.get(NORTHEAST_FRC_LIST), EMPTY)) {
            case CLEAVELAND -> Objects.toString(caseData.get(CLEAVELAND_COURTLIST), EMPTY);
            case NWYORKSHIRE -> Objects.toString(caseData.get(NWYORKSHIRE_COURTLIST), EMPTY);
            case HSYORKSHIRE -> Objects.toString(caseData.get(HSYORKSHIRE_COURTLIST), EMPTY);
            default -> EMPTY;
        };
    }

    private static CourtList getNorthEastFRC(FinremCaseData caseData) {
        RegionNorthEastFrc northEastFrcList = caseData.getRegionWrapper().getDefaultRegionWrapper().getNorthEastFrcList();
        DefaultCourtListWrapper courts = caseData.getRegionWrapper().getDefaultRegionWrapper().getDefaultCourtListWrapper();

        return switch (northEastFrcList) {
            case CLEVELAND -> courts.getClevelandCourt(true);
            case NW_YORKSHIRE -> courts.getNwYorkshireCourt();
            case HS_YORKSHIRE -> courts.getHumberCourt();
        };
    }

    private static String getMidlandFRC(Map<String, Object> caseData) {
        return switch (Objects.toString(caseData.get(MIDLANDS_FRC_LIST), EMPTY)) {
            case NOTTINGHAM -> Objects.toString(caseData.get(NOTTINGHAM_COURTLIST), EMPTY);
            case BIRMINGHAM -> Objects.toString(caseData.get(BIRMINGHAM_COURTLIST), EMPTY);
            default -> EMPTY;
        };
    }

    private static CourtList getMidlandFRC(FinremCaseData caseData) {
        RegionMidlandsFrc midlandsFrcList = caseData.getRegionWrapper().getDefaultRegionWrapper().getMidlandsFrcList();
        DefaultCourtListWrapper courts = caseData.getRegionWrapper().getDefaultRegionWrapper().getDefaultCourtListWrapper();

        return switch (midlandsFrcList) {
            case BIRMINGHAM -> courts.getBirminghamCourt();
            case NOTTINGHAM -> courts.getNottinghamCourt();
        };
    }
}
