package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.generalapplicationinterim;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.ContestedAbstractLetterDetailsMapperTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CfcCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationCourtListWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.FrcCourtDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.DocumentTemplateDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.GeneralApplicationLetterDetails;

import java.time.LocalDate;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class GeneralApplicationLetterDetailsMapperTest extends ContestedAbstractLetterDetailsMapperTest {

    public static final String TEST_JSON = "/fixtures/general-application-directions.json";

    @Autowired
    private GeneralApplicationLetterDetailsMapperContested generalApplicationLetterDetailsMapper;

    @Before
    public void setUp() throws Exception {
        setCaseDetails(TEST_JSON);
    }

    @Test
    public void givenValidCaseData_whenBuildDocumentTemplateDetails_thenReturnExpectedTemplatedDetails() {
        DocumentTemplateDetails actual = generalApplicationLetterDetailsMapper.buildDocumentTemplateDetails(caseDetails,
            caseDetails.getData().getRegionWrapper().getDefaultCourtList());

        DocumentTemplateDetails expected = getExpectedGeneralApplicationLetterDetails();

        assertEquals(expected, actual);
    }

    @Test
    public void givenValidCaseData_whenGetDocumentTemplateDetailsAsMap_thenReturnExpectedMap() {
        Map<String, Object> placeholdersMap = generalApplicationLetterDetailsMapper.getDocumentTemplateDetailsAsMap(caseDetails,
            caseDetails.getData().getRegionWrapper().getDefaultCourtList());

        GeneralApplicationLetterDetails expected = getExpectedGeneralApplicationLetterDetails();

        Map<String, Object> actualData = getCaseData(placeholdersMap);

        assertThat(actualData.get("applicantName"), is("Poor Guy"));
        assertThat(actualData.get("hearingVenue"), is(expected.getHearingVenue()));
        assertThat(actualData.get("ccdCaseNumber"), is(expected.getCcdCaseNumber()));
        assertThat(actualData.get("generalApplicationDirectionsHearingDate"),
            is(expected.getGeneralApplicationDirectionsHearingDate()));

        Map<String, Object> courtDetails = (Map<String, Object>) actualData.get("courtDetails");
        assertThat(courtDetails.get("courtName"), is(expected.getCourtDetails().getCourtName()));
        assertThat(courtDetails.get("courtAddress"), is(expected.getCourtDetails().getCourtAddress()));
    }

    private GeneralApplicationLetterDetails getExpectedGeneralApplicationLetterDetails() {
        return GeneralApplicationLetterDetails.builder()
            .applicantName("Poor Guy")
            .respondentName("test Korivi")
            .ccdCaseNumber("123123123")
            .divorceCaseNumber("DD12D12345")
            .generalApplicationDirectionsHearingTimeEstimate("30 minutes")
            .generalApplicationDirectionsHearingTime("2:00 pm")
            .generalApplicationDirectionsHearingDate("2020-06-01")
            .generalApplicationDirectionsAdditionalInformation("refreshments will be provided")
            .courtDetails(getCourtDetails())
            .letterDate(String.valueOf(LocalDate.now()))
            .hearingVenue("Croydon County Court And Family Court, Croydon County Court, Altyre Road, Croydon, CR9 5AB")
            .build();
    }

    private FrcCourtDetails getCourtDetails() {
        GeneralApplicationCourtListWrapper courtListWrapper = new GeneralApplicationCourtListWrapper();
        courtListWrapper.setGeneralApplicationDirectionsCfcCourtList(CfcCourt.KINGSTON_UPON_THAMES_COUNTY_COURT_AND_FAMILY_COURT);
        return new CourtDetailsMapper(new ObjectMapper()).getCourtDetails(courtListWrapper);
    }
}