package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NWYORKSHIRE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NWYORKSHIRE_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.REGION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHEAST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHEAST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHWEST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOUTHWEST_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SWANSEA;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SWANSEA_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.THAMESVALLEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.THAMESVALLEY_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WALES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.WALES_FRC_LIST;

public class ConsentedCaseHearingFunctionsTest {

    public static final String BRISTOL_1 = "bristol_1";
    public static final String BIRMINGHAM_1 = "birmingham_1";
    public static final String THAMES_1 = "thames_1";
    public static final String LANCASHIRE_1 = "lancashire_1";
    public static final String HS_YORKSHIRE_1 = "hs_yorkshire_1";
    public static final String NORTHWALES_1 = "northwales_1";
    private Map<String, Object> caseData;

    @ParameterizedTest
    @MethodSource("provideSouthWestParameters")
    public void shouldReturnCorrectSouthWestList(String frc, String courtList, String court) {
        caseData = new HashMap<>();
        caseData.put(REGION, SOUTHWEST);
        caseData.put(SOUTHWEST_FRC_LIST, frc);
        caseData.put(courtList, court);

        assertThat(ConsentedCaseHearingFunctions.getSelectedCourt(caseData), is(court));
    }

    private static Stream<Arguments> provideSouthWestParameters() {
        return Stream.of(
            Arguments.of(BRISTOLFRC, BRISTOL_COURTLIST, BRISTOL_1),
            Arguments.of(DEVON, DEVON_COURTLIST, "devon_1"),
            Arguments.of(DORSET, DORSET_COURTLIST, "doreset_1")
        );
    }

    @ParameterizedTest
    @MethodSource("provideMidlandsParameters")
    public void shouldReturnCorrectMidlandsList(String frc, String courtList, String court) {
        caseData = new HashMap<>();
        caseData.put(REGION, MIDLANDS);
        caseData.put(MIDLANDS_FRC_LIST, frc);
        caseData.put(courtList, court);

        assertThat(ConsentedCaseHearingFunctions.getSelectedCourt(caseData), is(court));
    }

    private static Stream<Arguments> provideMidlandsParameters() {
        return Stream.of(
            Arguments.of(BIRMINGHAM, BIRMINGHAM_COURTLIST, BIRMINGHAM_1),
            Arguments.of(NOTTINGHAM, NOTTINGHAM_COURTLIST, "nottingham_1")
        );
    }

    @ParameterizedTest
    @MethodSource("provideSoutheastParameters")
    public void shouldReturnCorrectSoutheastList(String frc, String courtList, String court) {
        caseData = new HashMap<>();
        caseData.put(REGION, SOUTHEAST);
        caseData.put(SOUTHEAST_FRC_LIST, frc);
        caseData.put(courtList, court);

        assertThat(ConsentedCaseHearingFunctions.getSelectedCourt(caseData), is(court));
    }

    private static Stream<Arguments> provideSoutheastParameters() {
        return Stream.of(
            Arguments.of(THAMESVALLEY, THAMESVALLEY_COURTLIST, THAMES_1),
            Arguments.of(BEDFORDSHIRE, BEDFORDSHIRE_COURTLIST, "bedfordshire_1"),
            Arguments.of(KENT, KENTFRC_COURTLIST, "kent_1")
        );
    }

    @ParameterizedTest
    @MethodSource("provideNorthwestParameters")
    public void shouldReturnCorrectNorthwestList(String frc, String courtList, String court) {
        caseData = new HashMap<>();
        caseData.put(REGION, NORTHWEST);
        caseData.put(NORTHWEST_FRC_LIST, frc);
        caseData.put(courtList, court);

        assertThat(ConsentedCaseHearingFunctions.getSelectedCourt(caseData), is(court));
    }

    private static Stream<Arguments> provideNorthwestParameters() {
        return Stream.of(
            Arguments.of(LANCASHIRE, LANCASHIRE_COURTLIST, LANCASHIRE_1),
            Arguments.of(MANCHESTER, MANCHESTER_COURTLIST, "manchester_1"),
            Arguments.of(LIVERPOOL, LIVERPOOL_COURTLIST, "liverpool_1")
        );
    }

    @ParameterizedTest
    @MethodSource("provideNortheastParameters")
    public void shouldReturnCorrectNortheastList(String frc, String courtList, String court) {
        caseData = new HashMap<>();
        caseData.put(REGION, NORTHEAST);
        caseData.put(NORTHEAST_FRC_LIST, frc);
        caseData.put(courtList, court);

        assertThat(ConsentedCaseHearingFunctions.getSelectedCourt(caseData), is(court));
    }

    private static Stream<Arguments> provideNortheastParameters() {
        return Stream.of(
            Arguments.of(HSYORKSHIRE, HSYORKSHIRE_COURTLIST, HS_YORKSHIRE_1),
            Arguments.of(NWYORKSHIRE, NWYORKSHIRE_COURTLIST, "nwYorkshire_1"),
            Arguments.of(CLEAVELAND, CLEAVELAND_COURTLIST, "cleveland_1")
        );
    }

    @ParameterizedTest
    @MethodSource("provideWalesParameters")
    public void shouldReturnCorrectWalesList(String frc, String courtList, String court) {
        caseData = new HashMap<>();
        caseData.put(REGION, WALES);
        caseData.put(WALES_FRC_LIST, frc);
        caseData.put(courtList, court);

        assertThat(ConsentedCaseHearingFunctions.getSelectedCourt(caseData), is(court));
    }

    private static Stream<Arguments> provideWalesParameters() {
        return Stream.of(
            Arguments.of(NORTHWALES, NORTH_WALES_COURTLIST, NORTHWALES_1),
            Arguments.of(SWANSEA, SWANSEA_COURTLIST, "swansea_1"),
            Arguments.of(NEWPORT, NEWPORT_COURTLIST, "newport_1")
        );
    }
}