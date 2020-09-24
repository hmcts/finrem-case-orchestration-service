package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
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
        switch ((String) mapOfCaseData.get(REGION)) {
            case MIDLANDS:
                return getMidlandFRC(mapOfCaseData);
            case LONDON:
                return getLondonFRC(mapOfCaseData);
            case NORTHWEST:
                return getNorthWestFRC(mapOfCaseData);
            case NORTHEAST:
                return getNorthEastFRC(mapOfCaseData);
            case SOUTHEAST:
                return getSouthEastFRC(mapOfCaseData);
            case WALES:
                return getWalesFRC(mapOfCaseData);
            default:
                return null;
        }
    }

    static String getWalesFRC(Map mapOfCaseData) {
        String walesList = (String) mapOfCaseData.get(WALES_FRC_LIST);
        if (NEWPORT.equalsIgnoreCase(walesList)) {
            return NEWPORT_COURTLIST;
        } else if (SWANSEA.equalsIgnoreCase(walesList)) {
            return SWANSEA_COURTLIST;
        }
        return null;
    }

    static String getSouthEastFRC(Map mapOfCaseData) {
        String southEastList = (String) mapOfCaseData.get(SOUTHEAST_FRC_LIST);
        if (KENT.equalsIgnoreCase(southEastList)) {
            return KENTFRC_COURTLIST;
        }
        return null;
    }

    static String getNorthEastFRC(Map mapOfCaseData) {
        String northEastList = (String) mapOfCaseData.get(NORTHEAST_FRC_LIST);
        if (CLEAVELAND.equalsIgnoreCase(northEastList)) {
            return CLEAVELAND_COURTLIST;
        } else if (NWYORKSHIRE.equalsIgnoreCase(northEastList)) {
            return NWYORKSHIRE_COURTLIST;
        } else if (HSYORKSHIRE.equalsIgnoreCase(northEastList)) {
            return HSYORKSHIRE_COURTLIST;
        }
        return null;
    }

    static String getNorthWestFRC(Map mapOfCaseData) {
        String northWestList = (String) mapOfCaseData.get(NORTHWEST_FRC_LIST);
        if (LIVERPOOL.equalsIgnoreCase(northWestList)) {
            return LIVERPOOL_COURTLIST;
        } else if (MANCHESTER.equalsIgnoreCase(northWestList)) {
            return MANCHESTER_COURTLIST;
        }
        return null;
    }

    static String getLondonFRC(Map mapOfCaseData) {
        String londonList = (String) mapOfCaseData.get(LONDON_FRC_LIST);
        if (CFC.equalsIgnoreCase(londonList)) {
            return CFC_COURTLIST;
        }
        return null;
    }

    static String getMidlandFRC(Map mapOfCaseData) {
        String midlandsList = (String) mapOfCaseData.get(MIDLANDS_FRC_LIST);
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
}
