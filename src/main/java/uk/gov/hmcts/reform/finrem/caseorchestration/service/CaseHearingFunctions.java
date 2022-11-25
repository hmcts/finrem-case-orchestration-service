package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.CourtDetailsParseException;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CourtList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Region;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionMidlandsFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionNorthEastFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionNorthWestFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionSouthEastFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionSouthWestFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionWalesFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DefaultRegionWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.FrcCourtDetails;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static java.nio.charset.StandardCharsets.UTF_8;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BEDFORDSHIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BEDFORDSHIRE_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BIRMINGHAM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BIRMINGHAM_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BRISTOLFRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BRISTOL_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_ALLOCATED_TO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CFC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CFC_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CLEAVELAND;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CLEAVELAND_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_ADDRESS_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_EMAIL_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_NAME_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_PHONE_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DEVON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DEVON_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DORSET;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DORSET_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FAST_TRACK_DECISION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_HEARING_REGION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_LONDON_FRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_MIDLANDS_FRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_NORTHEAST_FRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_NORTHWEST_FRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_PREFIX;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_SOUTHEAST_FRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_SOUTHWEST_FRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_WALES_FRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HSYORKSHIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HSYORKSHIRE_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_PREFIX;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_LONDON_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_MIDLANDS_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_NORTHEAST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_NORTHWEST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_REGION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_SOUTHEAST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_SOUTHWEST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_WALES_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.KENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.KENTFRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.KENTFRC_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LANCASHIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LANCASHIRE_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LIVERPOOL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LIVERPOOL_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON_FRC_LIST_CT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MANCHESTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MANCHESTER_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIDLANDS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIDLANDS_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIDLANDS_FRC_LIST_CT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NEWPORT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NEWPORT_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHEAST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHEAST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHEAST_FRC_LIST_CT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHWALES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHWEST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHWEST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHWEST_FRC_LIST_CT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTH_WALES_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTTINGHAM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTTINGHAM_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NWYORKSHIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NWYORKSHIRE_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.REGION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.REGION_CT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHEAST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHEAST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHEAST_FRC_LIST_CT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHWEST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHWEST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHWEST_FRC_LIST_CT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SWANSEA;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SWANSEA_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.THAMESVALLEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.THAMESVALLEY_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WALES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WALES_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WALES_FRC_LIST_CT;

@Component
@RequiredArgsConstructor
@Slf4j
public final class CaseHearingFunctions {

    public static final String COURT_DETAILS_JSON_PATH = "/json/court-details.json";

    static UnaryOperator<CaseDetails> addFastTrackFields = caseDetails -> {
        Map<String, Object> data = caseDetails.getData();
        data.put("formCCreatedDate", new Date());
        data.put("eventDatePlus21Days", LocalDate.now().plusDays(21));

        return caseDetails;
    };

    static UnaryOperator<CaseDetails> addNonFastTrackFields = caseDetails -> {
        Map<String, Object> data = caseDetails.getData();

        String hearingDate = Objects.toString(data.get(HEARING_DATE));
        LocalDate hearingLocalDate = LocalDate.parse(hearingDate);

        data.put("formCCreatedDate", new Date());
        data.put("hearingDateLess35Days", hearingLocalDate.minusDays(35));
        data.put("hearingDateLess14Days", hearingLocalDate.minusDays(14));

        return caseDetails;
    };

    static Function<Map<String, Object>, Boolean> isFastTrackApplication = caseData -> {
        String fastTrackDecision = Objects.toString(caseData.get(FAST_TRACK_DECISION));
        String caseAllocatedTo = (String) caseData.get(CASE_ALLOCATED_TO);

        return Optional.ofNullable(caseAllocatedTo)
            .map(s -> s.equalsIgnoreCase("yes"))
            .orElseGet(() -> fastTrackDecision.equalsIgnoreCase("yes"));
    };

    static String getSelectedCourtGA(Map<String, Object> mapOfCaseData) {
        return GENERAL_APPLICATION_DIRECTIONS_PREFIX + getSelectedCourt(mapOfCaseData, GENERAL_APPLICATION_DIRECTIONS_HEARING_REGION,
            GENERAL_APPLICATION_DIRECTIONS_MIDLANDS_FRC, GENERAL_APPLICATION_DIRECTIONS_LONDON_FRC, GENERAL_APPLICATION_DIRECTIONS_NORTHWEST_FRC,
            GENERAL_APPLICATION_DIRECTIONS_NORTHEAST_FRC, GENERAL_APPLICATION_DIRECTIONS_SOUTHWEST_FRC, GENERAL_APPLICATION_DIRECTIONS_SOUTHEAST_FRC,
            GENERAL_APPLICATION_DIRECTIONS_WALES_FRC);
    }

    public static String getSelectedCourtIH(Map<String, Object> mapOfCaseData) {
        return INTERIM_HEARING_PREFIX + getSelectedCourt(mapOfCaseData, INTERIM_REGION,
            INTERIM_MIDLANDS_FRC_LIST, INTERIM_LONDON_FRC_LIST, INTERIM_NORTHWEST_FRC_LIST,
            INTERIM_NORTHEAST_FRC_LIST, INTERIM_SOUTHWEST_FRC_LIST, INTERIM_SOUTHEAST_FRC_LIST,
            INTERIM_WALES_FRC_LIST);
    }

    static String getSelectedCourtComplexType(Map<String, Object> mapOfCaseData) {
        return getSelectedCourt(mapOfCaseData, REGION_CT, MIDLANDS_FRC_LIST_CT, LONDON_FRC_LIST_CT, NORTHWEST_FRC_LIST_CT,
            NORTHEAST_FRC_LIST_CT, SOUTHWEST_FRC_LIST_CT, SOUTHEAST_FRC_LIST_CT, WALES_FRC_LIST_CT);
    }

    public static String getSelectedCourt(Map<String, Object> mapOfCaseData) {
        return getSelectedCourt(mapOfCaseData, REGION, MIDLANDS_FRC_LIST, LONDON_FRC_LIST, NORTHWEST_FRC_LIST,
            NORTHEAST_FRC_LIST, SOUTHWEST_FRC_LIST, SOUTHEAST_FRC_LIST, WALES_FRC_LIST);
    }

    private static String getSelectedCourt(Map<String, Object> mapOfCaseData, String regionListName, String midlandsListName,
                                           String londonListName, String northwestListName, String northeastListName,
                                           String southwestListName, String southeastListName, String walesListName) {

        String region = Objects.toString(mapOfCaseData.get(regionListName), StringUtils.EMPTY);

        return switch (region) {
            case MIDLANDS -> getMidlandFRC(mapOfCaseData, midlandsListName);
            case LONDON -> getLondonFRC(mapOfCaseData, londonListName);
            case NORTHWEST -> getNorthWestFRC(mapOfCaseData, northwestListName);
            case NORTHEAST -> getNorthEastFRC(mapOfCaseData, northeastListName);
            case SOUTHWEST -> getSouthWestFRC(mapOfCaseData, southwestListName);
            case SOUTHEAST -> getSouthEastFRC(mapOfCaseData, southeastListName);
            case WALES -> getWalesFRC(mapOfCaseData, walesListName);
            default -> null;
        };
    }

    public static CourtList getSelectedCourt(FinremCaseData caseData) {
        Region region = caseData.getRegionWrapper().getDefaultRegionWrapper().getRegionList();
        DefaultRegionWrapper regionWrapper = caseData.getRegionWrapper().getDefaultRegionWrapper();

        return switch (region) {
            case LONDON -> getLondonFRC(regionWrapper);
            case WALES -> getWalesFRC(regionWrapper);
            case MIDLANDS -> getMidlandFRC(regionWrapper);
            case NORTHEAST -> getNorthEastFRC(regionWrapper);
            case NORTHWEST -> getNorthWestFRC(regionWrapper);
            case SOUTHEAST -> getSouthEastFRC(regionWrapper);
            case SOUTHWEST -> getSouthWestFRC(regionWrapper);
        };
    }

    private static String getWalesFRC(Map mapOfCaseData, String frcListName) {
        String walesList = Objects.toString(mapOfCaseData.get(frcListName), StringUtils.EMPTY);
        if (NEWPORT.equalsIgnoreCase(walesList)) {
            return NEWPORT_COURTLIST;
        } else if (SWANSEA.equalsIgnoreCase(walesList)) {
            return SWANSEA_COURTLIST;
        } else if (NORTHWALES.equalsIgnoreCase(walesList)) {
            return NORTH_WALES_COURTLIST;
        }
        return null;
    }

    private static CourtList getWalesFRC(DefaultRegionWrapper courtInfo) {
        RegionWalesFrc walesFrc = courtInfo.getWalesFrcList();

        return switch (walesFrc) {
            case NEWPORT -> courtInfo.getDefaultCourtListWrapper().getNewportCourt();
            case SWANSEA -> courtInfo.getDefaultCourtListWrapper().getSwanseaCourt();
            case NORTH_WALES -> courtInfo.getDefaultCourtListWrapper().getNorthWalesCourt();
        };
    }

    private static String getSouthEastFRC(Map mapOfCaseData, String frcListName) {
        String southEastList = Objects.toString(mapOfCaseData.get(frcListName), StringUtils.EMPTY);
        if (KENT.equalsIgnoreCase(southEastList) || KENTFRC.equalsIgnoreCase(southEastList)) {
            return KENTFRC_COURTLIST;
        } else if (BEDFORDSHIRE.equalsIgnoreCase(southEastList)) {
            return BEDFORDSHIRE_COURTLIST;
        } else if (THAMESVALLEY.equalsIgnoreCase(southEastList)) {
            return THAMESVALLEY_COURTLIST;
        }
        return null;
    }

    private static CourtList getSouthEastFRC(DefaultRegionWrapper courtInfo) {
        RegionSouthEastFrc southEastFrc = courtInfo.getSouthEastFrcList();

        return switch (southEastFrc) {
            case KENT -> courtInfo.getDefaultCourtListWrapper().getKentSurreyCourt();
            case BEDFORDSHIRE -> courtInfo.getDefaultCourtListWrapper().getBedfordshireCourt();
            case THAMES_VALLEY -> courtInfo.getDefaultCourtListWrapper().getThamesValleyCourt();
        };
    }


    private static String getSouthWestFRC(Map mapOfCaseData, String frcListName) {
        String southWestList = Objects.toString(mapOfCaseData.get(frcListName), StringUtils.EMPTY);
        if (DEVON.equalsIgnoreCase(southWestList)) {
            return DEVON_COURTLIST;
        } else if (DORSET.equalsIgnoreCase(southWestList)) {
            return DORSET_COURTLIST;
        } else if (BRISTOLFRC.equalsIgnoreCase(southWestList)) {
            return BRISTOL_COURTLIST;
        }
        return null;
    }

    private static CourtList getSouthWestFRC(DefaultRegionWrapper courtInfo) {
        RegionSouthWestFrc southWestFrc = courtInfo.getSouthWestFrcList();

        return switch (southWestFrc) {
            case DEVON -> courtInfo.getDefaultCourtListWrapper().getDevonCourt();
            case BRISTOL -> courtInfo.getDefaultCourtListWrapper().getBristolCourt();
            case DORSET -> courtInfo.getDefaultCourtListWrapper().getDorsetCourt();
        };
    }

    private static String getNorthEastFRC(Map mapOfCaseData, String frcListName) {
        String northEastList = Objects.toString(mapOfCaseData.get(frcListName), StringUtils.EMPTY);
        if (CLEAVELAND.equalsIgnoreCase(northEastList)) {
            return CLEAVELAND_COURTLIST;
        } else if (NWYORKSHIRE.equalsIgnoreCase(northEastList)) {
            return NWYORKSHIRE_COURTLIST;
        } else if (HSYORKSHIRE.equalsIgnoreCase(northEastList)) {
            return HSYORKSHIRE_COURTLIST;
        }
        return null;
    }

    private static CourtList getNorthEastFRC(DefaultRegionWrapper courtInfo) {
        RegionNorthEastFrc northEastFrc = courtInfo.getNorthEastFrcList();

        return switch (northEastFrc) {
            case CLEVELAND -> courtInfo.getDefaultCourtListWrapper().getClevelandCourt(false);
            case NW_YORKSHIRE -> courtInfo.getDefaultCourtListWrapper().getNwYorkshireCourt();
            case HS_YORKSHIRE -> courtInfo.getDefaultCourtListWrapper().getHumberCourt();
        };
    }

    private static String getNorthWestFRC(Map mapOfCaseData, String frcListName) {
        String northWestList = Objects.toString(mapOfCaseData.get(frcListName), StringUtils.EMPTY);
        if (LIVERPOOL.equalsIgnoreCase(northWestList)) {
            return LIVERPOOL_COURTLIST;
        } else if (MANCHESTER.equalsIgnoreCase(northWestList)) {
            return MANCHESTER_COURTLIST;
        } else if (LANCASHIRE.equalsIgnoreCase(northWestList)) {
            return LANCASHIRE_COURTLIST;
        }
        return null;
    }

    private static CourtList getNorthWestFRC(DefaultRegionWrapper courtInfo) {
        RegionNorthWestFrc northWestFrc = courtInfo.getNorthWestFrcList();

        return switch (northWestFrc) {
            case LANCASHIRE -> courtInfo.getDefaultCourtListWrapper().getLancashireCourt();
            case LIVERPOOL -> courtInfo.getDefaultCourtListWrapper().getLiverpoolCourt();
            case MANCHESTER -> courtInfo.getDefaultCourtListWrapper().getManchesterCourt();
        };
    }

    private static String getLondonFRC(Map mapOfCaseData, String frcListName) {
        String londonList = Objects.toString(mapOfCaseData.get(frcListName), StringUtils.EMPTY);
        if (CFC.equalsIgnoreCase(londonList)) {
            return CFC_COURTLIST;
        }
        return null;
    }

    private static CourtList getLondonFRC(DefaultRegionWrapper courtInfo) {
        return courtInfo.getDefaultCourtListWrapper().getCfcCourtList();
    }

    private static String getMidlandFRC(Map mapOfCaseData, String frcListName) {
        String midlandsList = Objects.toString(mapOfCaseData.get(frcListName), StringUtils.EMPTY);
        if (NOTTINGHAM.equalsIgnoreCase(midlandsList)) {
            return NOTTINGHAM_COURTLIST;
        } else if (BIRMINGHAM.equalsIgnoreCase(midlandsList)) {
            return BIRMINGHAM_COURTLIST;
        }
        return null;
    }

    private static CourtList getMidlandFRC(DefaultRegionWrapper courtInfo) {
        RegionMidlandsFrc midlandsFrc = courtInfo.getMidlandsFrcList();

        return switch (midlandsFrc) {
            case BIRMINGHAM -> courtInfo.getDefaultCourtListWrapper().getBirminghamCourt();
            case NOTTINGHAM -> courtInfo.getDefaultCourtListWrapper().getNottinghamCourt();
        };
    }

    public static Map<String, Object> buildFrcCourtDetails(Map<String, Object> data) {
        try {
            Map<String, Object> courtDetailsMap = new ObjectMapper().readValue(getCourtDetailsString(), HashMap.class);
            Map<String, Object> courtDetails = (Map<String, Object>) courtDetailsMap.get(data.get(getSelectedCourt(data)));

            return new ObjectMapper().convertValue(FrcCourtDetails.builder()
                .courtName((String) courtDetails.get(COURT_DETAILS_NAME_KEY))
                .courtAddress((String) courtDetails.get(COURT_DETAILS_ADDRESS_KEY))
                .phoneNumber((String) courtDetails.get(COURT_DETAILS_PHONE_KEY))
                .email((String) courtDetails.get(COURT_DETAILS_EMAIL_KEY))
                .build(), Map.class);
        } catch (IOException | NullPointerException e) {
            return null;
        }
    }

    public static Map<String, Object> buildInterimFrcCourtDetails(Map<String, Object> data) {
        try {
            Map<String, Object> courtDetailsMap = new ObjectMapper().readValue(getCourtDetailsString(), HashMap.class);
            Map<String, Object> courtDetails = (Map<String, Object>) courtDetailsMap.get(data.get(getSelectedCourtIH(data)));

            return new ObjectMapper().convertValue(FrcCourtDetails.builder()
                .courtName((String) courtDetails.get(COURT_DETAILS_NAME_KEY))
                .courtAddress((String) courtDetails.get(COURT_DETAILS_ADDRESS_KEY))
                .phoneNumber((String) courtDetails.get(COURT_DETAILS_PHONE_KEY))
                .email((String) courtDetails.get(COURT_DETAILS_EMAIL_KEY))
                .build(), Map.class);
        } catch (IOException | NullPointerException e) {
            return null;
        }
    }

    public static Map<String, Object> buildConsentedFrcCourtDetails() {
        return new ObjectMapper().convertValue(FrcCourtDetails.builder()
            .courtName(OrchestrationConstants.CTSC_COURT_NAME)
            .courtAddress(OrchestrationConstants.CTSC_COURT_ADDRESS)
            .phoneNumber(OrchestrationConstants.CTSC_PHONE_NUMBER)
            .email((OrchestrationConstants.CTSC_EMAIL_ADDRESS))
            .build(), Map.class);
    }

    public static String getCourtDetailsString() {
        try (InputStream inputStream = CaseHearingFunctions.class.getResourceAsStream(COURT_DETAILS_JSON_PATH)) {
            return IOUtils.toString(inputStream, UTF_8);
        } catch (IOException e) {
            throw new CourtDetailsParseException();
        }
    }

    static String getFrcCourtDetailsAsOneLineAddressString(Map<String, Object> courtDetailsMap) {
        return StringUtils.joinWith(", ", courtDetailsMap.get(COURT_DETAILS_NAME_KEY), courtDetailsMap.get(COURT_DETAILS_ADDRESS_KEY));
    }

    public static Map<String, Object> buildInterimHearingFrcCourtDetails(Map<String, Object> data) {
        try {
            Map<String, Object> courtDetailsMap = new ObjectMapper().readValue(getCourtDetailsString(), HashMap.class);
            Map<String, Object> courtDetails = (Map<String, Object>) courtDetailsMap.get(data.get(getSelectedCourtIH(data)));

            return new ObjectMapper().convertValue(FrcCourtDetails.builder()
                .courtName((String) courtDetails.get(COURT_DETAILS_NAME_KEY))
                .courtAddress((String) courtDetails.get(COURT_DETAILS_ADDRESS_KEY))
                .phoneNumber((String) courtDetails.get(COURT_DETAILS_PHONE_KEY))
                .email((String) courtDetails.get(COURT_DETAILS_EMAIL_KEY))
                .build(), Map.class);
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }
}
