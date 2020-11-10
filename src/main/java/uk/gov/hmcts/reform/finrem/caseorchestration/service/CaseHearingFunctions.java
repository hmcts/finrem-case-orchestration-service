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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HSYORKSHIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HSYORKSHIRE_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.KENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.KENTFRC_COURTLIST;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHWEST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NORTHWEST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTTINGHAM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTTINGHAM_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NWYORKSHIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NWYORKSHIRE_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.REGION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHEAST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHEAST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SWANSEA;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SWANSEA_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WALES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WALES_FRC_LIST;

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

    static String getSelectedCourt(Map<String, Object> mapOfCaseData) {
        return getSelectedCourt(mapOfCaseData, "");
    }

    static String getSelectedCourt(Map<String, Object> mapOfCaseData, String fieldNamePrefix) {
        String courtListFieldName;

        switch ((String) mapOfCaseData.get(fieldNamePrefix + REGION)) {
            case MIDLANDS:
                courtListFieldName = getMidlandFRC(mapOfCaseData, fieldNamePrefix);
                break;
            case LONDON:
                courtListFieldName = getLondonFRC(mapOfCaseData, fieldNamePrefix);
                break;
            case NORTHWEST:
                courtListFieldName = getNorthWestFRC(mapOfCaseData, fieldNamePrefix);
                break;
            case NORTHEAST:
                courtListFieldName = getNorthEastFRC(mapOfCaseData, fieldNamePrefix);
                break;
            case SOUTHEAST:
                courtListFieldName = getSouthEastFRC(mapOfCaseData, fieldNamePrefix);
                break;
            case WALES:
                courtListFieldName = getWalesFRC(mapOfCaseData, fieldNamePrefix);
                break;
            default:
                return null;
        }

        return fieldNamePrefix + courtListFieldName;
    }

    private static String getWalesFRC(Map mapOfCaseData, String fieldNamePrefix) {
        String walesList = (String) mapOfCaseData.get(fieldNamePrefix + WALES_FRC_LIST);
        if (NEWPORT.equalsIgnoreCase(walesList)) {
            return NEWPORT_COURTLIST;
        } else if (SWANSEA.equalsIgnoreCase(walesList)) {
            return SWANSEA_COURTLIST;
        }
        return null;
    }

    private static String getSouthEastFRC(Map mapOfCaseData, String fieldNamePrefix) {
        String southEastList = (String) mapOfCaseData.get(fieldNamePrefix + SOUTHEAST_FRC_LIST);
        if (KENT.equalsIgnoreCase(southEastList)) {
            return KENTFRC_COURTLIST;
        }
        return null;
    }

    private static String getNorthEastFRC(Map mapOfCaseData, String fieldNamePrefix) {
        String northEastList = (String) mapOfCaseData.get(fieldNamePrefix + NORTHEAST_FRC_LIST);
        if (CLEAVELAND.equalsIgnoreCase(northEastList)) {
            return CLEAVELAND_COURTLIST;
        } else if (NWYORKSHIRE.equalsIgnoreCase(northEastList)) {
            return NWYORKSHIRE_COURTLIST;
        } else if (HSYORKSHIRE.equalsIgnoreCase(northEastList)) {
            return HSYORKSHIRE_COURTLIST;
        }
        return null;
    }

    private static String getNorthWestFRC(Map mapOfCaseData, String fieldNamePrefix) {
        String northWestList = (String) mapOfCaseData.get(fieldNamePrefix + NORTHWEST_FRC_LIST);
        if (LIVERPOOL.equalsIgnoreCase(northWestList)) {
            return LIVERPOOL_COURTLIST;
        } else if (MANCHESTER.equalsIgnoreCase(northWestList)) {
            return MANCHESTER_COURTLIST;
        }
        return null;
    }

    private static String getLondonFRC(Map mapOfCaseData, String fieldNamePrefix) {
        String londonList = (String) mapOfCaseData.get(fieldNamePrefix + LONDON_FRC_LIST);
        if (CFC.equalsIgnoreCase(londonList)) {
            return CFC_COURTLIST;
        }
        return null;
    }

    private static String getMidlandFRC(Map mapOfCaseData, String fieldNamePrefix) {
        String midlandsList = (String) mapOfCaseData.get(fieldNamePrefix + MIDLANDS_FRC_LIST);
        if (NOTTINGHAM.equalsIgnoreCase(midlandsList)) {
            return NOTTINGHAM_COURTLIST;
        } else if (BIRMINGHAM.equalsIgnoreCase(midlandsList)) {
            return BIRMINGHAM_COURTLIST;
        }
        return null;
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

    static String getCourtDetailsString() throws IOException {
        try (InputStream inputStream = CaseHearingFunctions.class.getResourceAsStream(COURT_DETAILS_JSON_PATH)) {
            return IOUtils.toString(inputStream, UTF_8);
        }
    }

    static String getFrcCourtDetailsAsOneLineAddressString(Map<String, Object> courtDetailsMap) {
        return StringUtils.joinWith(", ", courtDetailsMap.get(COURT_DETAILS_NAME_KEY), courtDetailsMap.get(COURT_DETAILS_ADDRESS_KEY));
    }
}
