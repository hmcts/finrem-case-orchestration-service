package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CourtDetailsConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.CourtDetailsParseException;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CourtDetailsTemplateFields;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static java.nio.charset.StandardCharsets.UTF_8;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CTSC_OPENING_HOURS;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CLEVELAND;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_HIGHCOURT_FRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_LONDON_FRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_MIDLANDS_FRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_NORTHEAST_FRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_NORTHWEST_FRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_PREFIX;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_SOUTHEAST_FRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_SOUTHWEST_FRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_WALES_FRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_HIGHCOURT_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_LONDON_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_MIDLANDS_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_NORTHEAST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_NORTHWEST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_PREFIX;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_REGION_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_SOUTHEAST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_SOUTHWEST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_WALES_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HIGHCOURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HIGHCOURT_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HIGHCOURT_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HIGHCOURT_FRC_LIST_CT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HSYORKSHIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HSYORKSHIRE_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_PREFIX;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HIGHCOURT_FRC_LIST;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LETTER_DATE_FORMAT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LIVERPOOL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LIVERPOOL_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LONDON_COURTLIST;
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
@Slf4j
public final class CaseHearingFunctions {

    private CaseHearingFunctions() {
    }

    static UnaryOperator<CaseDetails> addFastTrackFields = caseDetails -> {
        Map<String, Object> data = caseDetails.getData();
        data.put("formCCreatedDate", DateTimeFormatter.ofPattern(LETTER_DATE_FORMAT).format(LocalDate.now()));
        data.put("eventDatePlus21Days", DateTimeFormatter.ofPattern(LETTER_DATE_FORMAT).format(LocalDate.now().plusDays(21)));

        return caseDetails;
    };

    public static String getSelectedHearingCourt(Map<String, Object> mapOfCaseData) {
        return HEARING_PREFIX + getSelectedCourt(mapOfCaseData, HEARING_REGION_LIST,
            HEARING_MIDLANDS_FRC_LIST, HEARING_LONDON_FRC_LIST, HEARING_NORTHWEST_FRC_LIST,
            HEARING_NORTHEAST_FRC_LIST, HEARING_SOUTHWEST_FRC_LIST, HEARING_SOUTHEAST_FRC_LIST,
            HEARING_WALES_FRC_LIST, HEARING_HIGHCOURT_FRC_LIST);
    }

    static UnaryOperator<CaseDetails> addNonFastTrackFields = caseDetails -> {
        Map<String, Object> data = caseDetails.getData();

        String hearingDate = Objects.toString(data.get(HEARING_DATE));
        LocalDate hearingLocalDate = LocalDate.parse(hearingDate);

        data.put("formCCreatedDate", DateTimeFormatter.ofPattern(LETTER_DATE_FORMAT).format(LocalDate.now()));
        data.put("hearingDateLess35Days", DateTimeFormatter.ofPattern(LETTER_DATE_FORMAT).format(hearingLocalDate.minusDays(35)));
        data.put("hearingDateLess14Days", DateTimeFormatter.ofPattern(LETTER_DATE_FORMAT).format(hearingLocalDate.minusDays(14)));

        return caseDetails;
    };

    @SuppressWarnings("java:S4276")
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
            GENERAL_APPLICATION_DIRECTIONS_WALES_FRC, GENERAL_APPLICATION_DIRECTIONS_HIGHCOURT_FRC);
    }

    public static String getSelectedCourtIH(Map<String, Object> mapOfCaseData) {
        return INTERIM_HEARING_PREFIX + getSelectedCourt(mapOfCaseData, INTERIM_REGION,
            INTERIM_MIDLANDS_FRC_LIST, INTERIM_LONDON_FRC_LIST, INTERIM_NORTHWEST_FRC_LIST,
            INTERIM_NORTHEAST_FRC_LIST, INTERIM_SOUTHWEST_FRC_LIST, INTERIM_SOUTHEAST_FRC_LIST,
            INTERIM_WALES_FRC_LIST, INTERIM_HIGHCOURT_FRC_LIST);
    }

    public static String getSelectedCourtComplexType(Map<String, Object> mapOfCaseData) {
        return getSelectedCourt(mapOfCaseData, REGION_CT, MIDLANDS_FRC_LIST_CT, LONDON_FRC_LIST_CT, NORTHWEST_FRC_LIST_CT,
            NORTHEAST_FRC_LIST_CT, SOUTHWEST_FRC_LIST_CT, SOUTHEAST_FRC_LIST_CT, WALES_FRC_LIST_CT, HIGHCOURT_FRC_LIST_CT);
    }

    public static String getSelectedCourt(Map<String, Object> mapOfCaseData) {
        return getSelectedCourt(mapOfCaseData, REGION, MIDLANDS_FRC_LIST, LONDON_FRC_LIST, NORTHWEST_FRC_LIST,
            NORTHEAST_FRC_LIST, SOUTHWEST_FRC_LIST, SOUTHEAST_FRC_LIST, WALES_FRC_LIST, HIGHCOURT_FRC_LIST);
    }

    @SuppressWarnings("java:S107")
    private static String getSelectedCourt(Map<String, Object> mapOfCaseData, String regionListName, String midlandsListName,
                                           String londonListName, String northwestListName, String northeastListName,
                                           String southwestListName, String southeastListName, String walesListName,
                                           String highCourtListName) {
        switch ((String) mapOfCaseData.get(regionListName)) {
            case MIDLANDS:
                return getMidlandFRC(mapOfCaseData, midlandsListName);
            case LONDON:
                return getLondonFRC(mapOfCaseData, londonListName);
            case NORTHWEST:
                return getNorthWestFRC(mapOfCaseData, northwestListName);
            case NORTHEAST:
                return getNorthEastFRC(mapOfCaseData, northeastListName);
            case SOUTHWEST:
                return getSouthWestFRC(mapOfCaseData, southwestListName);
            case SOUTHEAST:
                return getSouthEastFRC(mapOfCaseData, southeastListName);
            case WALES:
                return getWalesFRC(mapOfCaseData, walesListName);
            case HIGHCOURT:
                return getHighCourtFRC(mapOfCaseData, highCourtListName);
            default:
                return null;
        }
    }

    private static String getWalesFRC(Map<String, Object> mapOfCaseData, String frcListName) {
        String walesList = (String) mapOfCaseData.get(frcListName);
        if (NEWPORT.equalsIgnoreCase(walesList)) {
            return NEWPORT_COURTLIST;
        } else if (SWANSEA.equalsIgnoreCase(walesList)) {
            return SWANSEA_COURTLIST;
        } else if (NORTHWALES.equalsIgnoreCase(walesList)) {
            return NORTH_WALES_COURTLIST;
        }
        return null;
    }

    private static String getHighCourtFRC(Map<String, Object> mapOfCaseData, String frcListName) {
        String highCourtList = (String) mapOfCaseData.get(frcListName);
        if (HIGHCOURT.equalsIgnoreCase(highCourtList)) {
            return HIGHCOURT_COURTLIST;
        }
        return null;
    }

    private static String getSouthEastFRC(Map<String, Object> mapOfCaseData, String frcListName) {
        String southEastList = (String) mapOfCaseData.get(frcListName);
        if (KENT.equalsIgnoreCase(southEastList) || KENTFRC.equalsIgnoreCase(southEastList)) {
            return KENTFRC_COURTLIST;
        } else if (BEDFORDSHIRE.equalsIgnoreCase(southEastList)) {
            return BEDFORDSHIRE_COURTLIST;
        } else if (THAMESVALLEY.equalsIgnoreCase(southEastList)) {
            return THAMESVALLEY_COURTLIST;
        }
        return null;
    }

    private static String getSouthWestFRC(Map<String, Object> mapOfCaseData, String frcListName) {
        String southWestList = (String) mapOfCaseData.get(frcListName);
        if (DEVON.equalsIgnoreCase(southWestList)) {
            return DEVON_COURTLIST;
        } else if (DORSET.equalsIgnoreCase(southWestList)) {
            return DORSET_COURTLIST;
        } else if (BRISTOLFRC.equalsIgnoreCase(southWestList)) {
            return BRISTOL_COURTLIST;
        }
        return null;
    }

    private static String getNorthEastFRC(Map<String, Object> mapOfCaseData, String frcListName) {
        String northEastList = (String) mapOfCaseData.get(frcListName);
        if (CLEAVELAND.equalsIgnoreCase(northEastList) || CLEVELAND.equalsIgnoreCase(northEastList)) {
            return CLEAVELAND_COURTLIST;
        } else if (NWYORKSHIRE.equalsIgnoreCase(northEastList)) {
            return NWYORKSHIRE_COURTLIST;
        } else if (HSYORKSHIRE.equalsIgnoreCase(northEastList)) {
            return HSYORKSHIRE_COURTLIST;
        }
        return null;
    }

    private static String getNorthWestFRC(Map<String, Object> mapOfCaseData, String frcListName) {
        String northWestList = (String) mapOfCaseData.get(frcListName);
        if (LIVERPOOL.equalsIgnoreCase(northWestList)) {
            return LIVERPOOL_COURTLIST;
        } else if (MANCHESTER.equalsIgnoreCase(northWestList)) {
            return MANCHESTER_COURTLIST;
        } else if (LANCASHIRE.equalsIgnoreCase(northWestList)) {
            return LANCASHIRE_COURTLIST;
        }
        return null;
    }

    private static String getLondonFRC(Map<String, Object> mapOfCaseData, String frcListName) {
        String londonList = (String) mapOfCaseData.get(frcListName);
        if (CFC.equalsIgnoreCase(londonList)) {
            return CFC_COURTLIST;
        } else if (LONDON.equalsIgnoreCase(londonList)) {
            return LONDON_COURTLIST;
        }

        return null;
    }

    private static String getMidlandFRC(Map<String, Object> mapOfCaseData, String frcListName) {
        String midlandsList = (String) mapOfCaseData.get(frcListName);
        if (NOTTINGHAM.equalsIgnoreCase(midlandsList)) {
            return NOTTINGHAM_COURTLIST;
        } else if (BIRMINGHAM.equalsIgnoreCase(midlandsList)) {
            return BIRMINGHAM_COURTLIST;
        }
        return null;
    }

    /**
     * Return BulkPrintDocument.
     * <p>Please use @{@link #buildFrcCourtDetails(FinremCaseData)}</p>
     *
     * @param data instance of Map
     * @deprecated Use {@link Map data}
     */
    @Deprecated(since = "15-june-2023")
    @SuppressWarnings("java:S1133")
    public static Map<String, Object> buildFrcCourtDetails(Map<String, Object> data) {
        try {
            Map<String, Object> courtDetailsMap = new ObjectMapper().readValue(getCourtDetailsString(), HashMap.class);
            Map<String, Object> courtDetails = (Map<String, Object>) courtDetailsMap.get(data.get(getSelectedCourt(data)));

            return new ObjectMapper().convertValue(CourtDetailsTemplateFields.builder()
                .courtName((String) courtDetails.get(COURT_DETAILS_NAME_KEY))
                .courtAddress((String) courtDetails.get(COURT_DETAILS_ADDRESS_KEY))
                .phoneNumber((String) courtDetails.get(COURT_DETAILS_PHONE_KEY))
                .email((String) courtDetails.get(COURT_DETAILS_EMAIL_KEY))
                .openingHours(CTSC_OPENING_HOURS)
                .build(), Map.class);
        } catch (IOException | NullPointerException e) {
            return Collections.emptyMap();
        }
    }


    public static Map<String, Object> buildFrcCourtDetails(FinremCaseData data) {
        try {
            Map<String, Object> courtDetailsMap = new ObjectMapper().readValue(getCourtDetailsString(), HashMap.class);
            Map<String, Object> courtDetails = (Map<String, Object>) courtDetailsMap.get(data.getSelectedAllocatedCourt());

            return new ObjectMapper().convertValue(CourtDetailsTemplateFields.builder()
                .courtName((String) courtDetails.get(COURT_DETAILS_NAME_KEY))
                .courtAddress((String) courtDetails.get(COURT_DETAILS_ADDRESS_KEY))
                .phoneNumber((String) courtDetails.get(COURT_DETAILS_PHONE_KEY))
                .email((String) courtDetails.get(COURT_DETAILS_EMAIL_KEY))
                .openingHours(CTSC_OPENING_HOURS)
                .build(), Map.class);
        } catch (IOException | NullPointerException e) {
            return Collections.emptyMap();
        }
    }

    public static Map<String, Object> buildHearingCourtDetails(Map<String, Object> data) {
        try {
            Map<String, Object> courtDetailsMap = new ObjectMapper().readValue(getCourtDetailsString(), HashMap.class);
            Map<String, Object> allocatedCourtDetails = (Map<String, Object>) courtDetailsMap.get(data.get(getSelectedCourt(data)));
            Map<String, Object> hearingCourtDetails = (Map<String, Object>) courtDetailsMap.get(data.get(getSelectedHearingCourt(data)));

            return new ObjectMapper().convertValue(CourtDetailsTemplateFields.builder()
                .courtName((String) allocatedCourtDetails.get(COURT_DETAILS_NAME_KEY))
                .courtAddress((String) allocatedCourtDetails.get(COURT_DETAILS_ADDRESS_KEY))
                .phoneNumber((String) allocatedCourtDetails.get(COURT_DETAILS_PHONE_KEY))
                .email((String) allocatedCourtDetails.get(COURT_DETAILS_EMAIL_KEY))
                .hearingCourtName((String) hearingCourtDetails.get(COURT_DETAILS_NAME_KEY))
                .hearingCourtAddress((String) hearingCourtDetails.get(COURT_DETAILS_ADDRESS_KEY))
                .hearingCourtPhoneNumber((String) hearingCourtDetails.get(COURT_DETAILS_PHONE_KEY))
                .hearingCourtEmail((String) hearingCourtDetails.get(COURT_DETAILS_EMAIL_KEY))
                .build(), Map.class);
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    public static Map<String, Object> buildInterimFrcCourtDetails(Map<String, Object> data) {
        try {
            Map<String, Object> courtDetailsMap = new ObjectMapper().readValue(getCourtDetailsString(), HashMap.class);
            Map<String, Object> courtDetails = (Map<String, Object>) courtDetailsMap.get(data.get(getSelectedCourtIH(data)));

            return new ObjectMapper().convertValue(CourtDetailsTemplateFields.builder()
                .courtName((String) courtDetails.get(COURT_DETAILS_NAME_KEY))
                .courtAddress((String) courtDetails.get(COURT_DETAILS_ADDRESS_KEY))
                .phoneNumber((String) courtDetails.get(COURT_DETAILS_PHONE_KEY))
                .email((String) courtDetails.get(COURT_DETAILS_EMAIL_KEY))
                .build(), Map.class);
        } catch (IOException | NullPointerException e) {
            return Collections.emptyMap();
        }
    }

    public static Map<String, Object> buildConsentedFrcCourtDetails() {
        return new ObjectMapper().convertValue(CourtDetailsTemplateFields.builder()
            .courtName(OrchestrationConstants.CTSC_COURT_NAME)
            .courtAddress(OrchestrationConstants.CTSC_COURT_ADDRESS)
            .phoneNumber(OrchestrationConstants.CTSC_PHONE_NUMBER)
            .email((OrchestrationConstants.CTSC_EMAIL_ADDRESS))
            .build(), Map.class);
    }

    public static CourtDetailsTemplateFields buildConsentedFrcCourtDetailsObject() {
        return CourtDetailsTemplateFields.builder()
            .courtName(OrchestrationConstants.CTSC_COURT_NAME)
            .courtAddress(OrchestrationConstants.CTSC_COURT_ADDRESS)
            .phoneNumber(OrchestrationConstants.CTSC_PHONE_NUMBER)
            .email((OrchestrationConstants.CTSC_EMAIL_ADDRESS))
            .build();
    }

    /**
     * Get court details as a Json string.
     *
     * @return Json string of court details
     * @deprecated Use {@link CourtDetailsConfiguration#getCourts()} instead.
     */
    @Deprecated(since = "4 Nov 2024")
    public static String getCourtDetailsString() {
        try (InputStream inputStream = CaseHearingFunctions.class.getResourceAsStream("/json/court-details.json")) {
            return IOUtils.toString(inputStream, UTF_8);
        } catch (IOException e) {
            throw new CourtDetailsParseException();
        }
    }

    public static String getFrcCourtDetailsAsOneLineAddressString(Map<String, Object> courtDetailsMap) {
        return StringUtils.joinWith(", ", courtDetailsMap.get(COURT_DETAILS_NAME_KEY), courtDetailsMap.get(COURT_DETAILS_ADDRESS_KEY));
    }

    public static Map<String, Object> buildInterimHearingFrcCourtDetails(Map<String, Object> data) {
        try {
            Map<String, Object> courtDetailsMap = new ObjectMapper().readValue(getCourtDetailsString(), HashMap.class);
            Map<String, Object> courtDetails = (Map<String, Object>) courtDetailsMap.get(data.get(getSelectedCourtIH(data)));

            return new ObjectMapper().convertValue(CourtDetailsTemplateFields.builder()
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
