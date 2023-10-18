package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.generalapplicationorder;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.AbstractLetterDetailsMapperTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CfcCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DefaultCourtListWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CourtDetailsTemplateFields;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.DocumentTemplateDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.GeneralApplicationOrderDetails;

import java.time.LocalDate;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class GeneralApplicationOrderDetailsMapperTest extends AbstractLetterDetailsMapperTest {

    public static final String TEST_JSON = "/fixtures/general-application-directions.json";

    @Autowired
    private GeneralApplicationOrderDetailsMapper generalApplicationOrderDetailsMapper;

    @Before
    public void setUp() throws Exception {
        setCaseDetails(TEST_JSON);
    }

    @Test
    public void givenValidCaseData_whenBuildDocumentTemplateDetails_thenReturnExpectedTemplateDetails() {
        DocumentTemplateDetails actual = generalApplicationOrderDetailsMapper.buildDocumentTemplateDetails(caseDetails,
            caseDetails.getData().getRegionWrapper().getDefaultCourtList());

        DocumentTemplateDetails expected = getExpectedGeneralApplicationOrderDetails();

        assertEquals(expected, actual);
    }

    @Test
    public void givenValidCaseData_whenGetDocumentTemplateDetailsAsMap_thenReturnExpectedMap() {
        Map<String, Object> placeholdersMap = generalApplicationOrderDetailsMapper.getDocumentTemplateDetailsAsMap(caseDetails,
            caseDetails.getData().getRegionWrapper().getDefaultCourtList());

        GeneralApplicationOrderDetails expected = getExpectedGeneralApplicationOrderDetails();

        Map<String, Object> actualData = getCaseData(placeholdersMap);
        Map<String, Object> courtDetails = (Map<String, Object>) actualData.get("courtDetails");

        assertThat(actualData.get("respondentName"), is(expected.getRespondentName()));
        assertThat(actualData.get("generalApplicationDirectionsJudgeType"), is(expected.getGeneralApplicationDirectionsJudgeType()));
        assertThat(courtDetails.get("courtName"), is(expected.getCourtDetails().getCourtName()));
        assertThat(courtDetails.get("courtAddress"), is(expected.getCourtDetails().getCourtAddress()));
        assertThat(actualData.get("generalApplicationDirectionsCourtOrderDate"), is(expected.getGeneralApplicationDirectionsCourtOrderDate()));
    }

    private GeneralApplicationOrderDetails getExpectedGeneralApplicationOrderDetails() {
        return GeneralApplicationOrderDetails.builder()
            .applicantName("Poor Guy")
            .respondentName("test Korivi")
            .divorceCaseNumber("DD12D12345")
            .courtDetails(getCourtDetails())
            .civilPartnership("No")
            .letterDate(String.valueOf(LocalDate.now()))
            .generalApplicationDirectionsTextFromJudge("the case needs to be reconsidered")
            .generalApplicationDirectionsJudgeType("Her Honour Judge")
            .generalApplicationDirectionsJudgeName("Ms Justice")
            .generalApplicationDirectionsCourtOrderDate("2020-07-06")
            .build();
    }

    private CourtDetailsTemplateFields getCourtDetails() {
        DefaultCourtListWrapper courtListWrapper = new DefaultCourtListWrapper();
        courtListWrapper.setCfcCourtList(CfcCourt.KINGSTON_UPON_THAMES_COUNTY_COURT_AND_FAMILY_COURT);
        return new CourtDetailsMapper(new ObjectMapper()).getCourtDetails(courtListWrapper);
    }
}