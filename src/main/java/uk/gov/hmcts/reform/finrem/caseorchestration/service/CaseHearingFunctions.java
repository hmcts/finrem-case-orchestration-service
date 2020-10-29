package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.FrcCourtDetails;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static java.nio.charset.StandardCharsets.UTF_8;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BIRMINGHAM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BIRMINGHAM_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_ALLOCATED_TO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CFC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CFC_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CLEAVELAND;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CLEAVELAND_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_ADDRESS_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_EMAIL_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_NAME_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_PHONE_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FAST_TRACK_DECISION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_HEARING_REGION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_LONDON_FRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_MIDLANDS_FRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_NORTHEAST_FRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_NORTHWEST_FRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_PREFIX;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_SOUTHEAST_FRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_WALES_FRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HSYORKSHIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HSYORKSHIRE_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.KENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.KENTFRC_COURTLIST;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHWEST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHWEST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHWEST_FRC_LIST_CT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTTINGHAM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTTINGHAM_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NWYORKSHIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NWYORKSHIRE_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.REGION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.REGION_CT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHEAST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHEAST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHEAST_FRC_LIST_CT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SWANSEA;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SWANSEA_COURTLIST;
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
            GENERAL_APPLICATION_DIRECTIONS_NORTHEAST_FRC, GENERAL_APPLICATION_DIRECTIONS_SOUTHEAST_FRC, GENERAL_APPLICATION_DIRECTIONS_WALES_FRC);
    }

    static String getSelectedCourtComplexType(Map<String, Object> mapOfCaseData) {
        return getSelectedCourt(mapOfCaseData, REGION_CT, MIDLANDS_FRC_LIST_CT, LONDON_FRC_LIST_CT, NORTHWEST_FRC_LIST_CT,
            NORTHEAST_FRC_LIST_CT, SOUTHEAST_FRC_LIST_CT, WALES_FRC_LIST_CT);
    }

    static String getSelectedCourt(Map<String, Object> mapOfCaseData) {
        return getSelectedCourt(mapOfCaseData, REGION, MIDLANDS_FRC_LIST, LONDON_FRC_LIST, NORTHWEST_FRC_LIST,
            NORTHEAST_FRC_LIST, SOUTHEAST_FRC_LIST, WALES_FRC_LIST);
    }

    private static String getSelectedCourt(Map<String, Object> mapOfCaseData, String regionListName, String midlandsListName,
                                           String londonListName, String northwestListName, String northeastListName,
                                           String southeastListName, String walesListName) {
        switch ((String) mapOfCaseData.get(regionListName)) {
            case MIDLANDS:
                return getMidlandFRC(mapOfCaseData, midlandsListName);
            case LONDON:
                return getLondonFRC(mapOfCaseData, londonListName);
            case NORTHWEST:
                return getNorthWestFRC(mapOfCaseData, northwestListName);
            case NORTHEAST:
                return getNorthEastFRC(mapOfCaseData, northeastListName);
            case SOUTHEAST:
                return getSouthEastFRC(mapOfCaseData, southeastListName);
            case WALES:
                return getWalesFRC(mapOfCaseData, walesListName);
            default:
                return null;
        }
    }

    private static String getWalesFRC(Map mapOfCaseData, String frcListName) {
        String walesList = (String) mapOfCaseData.get(frcListName);
        if (NEWPORT.equalsIgnoreCase(walesList)) {
            return NEWPORT_COURTLIST;
        } else if (SWANSEA.equalsIgnoreCase(walesList)) {
            return SWANSEA_COURTLIST;
        }
        return null;
    }

    private static String getSouthEastFRC(Map mapOfCaseData, String frcListName) {
        String southEastList = (String) mapOfCaseData.get(frcListName);
        if (KENT.equalsIgnoreCase(southEastList)) {
            return KENTFRC_COURTLIST;
        }
        return null;
    }

    private static String getNorthEastFRC(Map mapOfCaseData, String frcListName) {
        String northEastList = (String) mapOfCaseData.get(frcListName);
        if (CLEAVELAND.equalsIgnoreCase(northEastList)) {
            return CLEAVELAND_COURTLIST;
        } else if (NWYORKSHIRE.equalsIgnoreCase(northEastList)) {
            return NWYORKSHIRE_COURTLIST;
        } else if (HSYORKSHIRE.equalsIgnoreCase(northEastList)) {
            return HSYORKSHIRE_COURTLIST;
        }
        return null;
    }

    private static String getNorthWestFRC(Map mapOfCaseData, String frcListName) {
        String northWestList = (String) mapOfCaseData.get(frcListName);
        if (LIVERPOOL.equalsIgnoreCase(northWestList)) {
            return LIVERPOOL_COURTLIST;
        } else if (MANCHESTER.equalsIgnoreCase(northWestList)) {
            return MANCHESTER_COURTLIST;
        }
        return null;
    }

    private static String getLondonFRC(Map mapOfCaseData, String frcListName) {
        String londonList = (String) mapOfCaseData.get(frcListName);
        if (CFC.equalsIgnoreCase(londonList)) {
            return CFC_COURTLIST;
        }
        return null;
    }

    private static String getMidlandFRC(Map mapOfCaseData, String frcListName) {
        String midlandsList = (String) mapOfCaseData.get(frcListName);
        if (NOTTINGHAM.equalsIgnoreCase(midlandsList)) {
            return NOTTINGHAM_COURTLIST;
        } else if (BIRMINGHAM.equalsIgnoreCase(midlandsList)) {
            return BIRMINGHAM_COURTLIST;
        }
        return null;
    }

    public static Map<String, Object> buildFrcCourtDetails(Map<String, Object> data, ObjectMapper objectMapper) {
        try {
            Map<String, Object> courtDetailsMap = objectMapper.readValue(getCourtDetailsString(), HashMap.class);
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

    static String getCourtDetailsString() throws IOException {
        try (InputStream inputStream = CaseHearingFunctions.class.getResourceAsStream(COURT_DETAILS_JSON_PATH)) {
            return IOUtils.toString(inputStream, UTF_8);
        }
    }

    static String getFrcCourtDetailsAsOneLineAddressString(Map<String, Object> courtDetailsMap) {
        return StringUtils.joinWith(", ", courtDetailsMap.get(COURT_DETAILS_NAME_KEY), courtDetailsMap.get(COURT_DETAILS_ADDRESS_KEY));
    }
}
