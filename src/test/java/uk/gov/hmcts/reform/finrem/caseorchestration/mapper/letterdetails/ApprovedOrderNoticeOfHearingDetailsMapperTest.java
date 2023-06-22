package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.approvedorderhearing.ApprovedOrderNoticeOfHearingDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.ApprovedOrderNoticeOfHearingDetails;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ApprovedOrderNoticeOfHearingDetailsMapperTest extends AbstractLetterDetailsMapperTest {
    public static final String TEST_RESOURCE = "/fixtures/general-application-directions.json";

    @Autowired
    private ApprovedOrderNoticeOfHearingDetailsMapper approvedOrderNoticeOfHearingDetailsMapper;

    @Before
    public void setUp() {
        setCaseDetails(TEST_RESOURCE);
    }

    @Test
    public void givenValidCaseData_whenBuildDocumentTemplateDetails_thenReturnExpectedDetails() {
        ApprovedOrderNoticeOfHearingDetails actual = (ApprovedOrderNoticeOfHearingDetails) approvedOrderNoticeOfHearingDetailsMapper
            .buildDocumentTemplateDetails(caseDetails, caseDetails.getData().getRegionWrapper().getDefaultCourtList());
        ApprovedOrderNoticeOfHearingDetails expected = getExpectedApprovedOrderNoticeOfHearingDetails(actual.getAdditionalHearingDated());


        assertEquals(expected, actual);
    }

    @Test
    public void givenValidCaseData_whenGetDocumentTemplateDetailsAsMap_thenReturnExpectedPlaceholdersMap() {
        Map<String, Object> placeholdersMap = approvedOrderNoticeOfHearingDetailsMapper
            .getDocumentTemplateDetailsAsMap(caseDetails, caseDetails.getData().getRegionWrapper().getDefaultCourtList());

        Map<String, Object> caseDetails = (Map<String, Object>) placeholdersMap.get("caseDetails");
        Map<String, Object> caseData = (Map<String, Object>) caseDetails.get("case_data");
        ApprovedOrderNoticeOfHearingDetails expected =
            getExpectedApprovedOrderNoticeOfHearingDetails(caseData.get("AdditionalHearingDated").toString());
        Map<String, Object> actualData = getCaseData(placeholdersMap);

        assertEquals(actualData.get("HearingType"), expected.getHearingType());
        assertEquals(actualData.get("CourtName"), expected.getCourtName());
        assertEquals(actualData.get("ApplicantName"), expected.getApplicantName());
        assertEquals(actualData.get("HearingLength"), expected.getHearingLength());
        assertEquals(actualData.get("CourtAddress"), expected.getCourtAddress());
    }

    private ApprovedOrderNoticeOfHearingDetails getExpectedApprovedOrderNoticeOfHearingDetails(String additionalHearingDated) {
        return ApprovedOrderNoticeOfHearingDetails.builder()
            .ccdCaseNumber(123123123L)
            .hearingVenue("Hastings County Court And Family Court Hearing Centre, The Law Courts, Bohemia Road, Hastings, TN34 1QX")
            .divorceCaseNumber("DD12D12345")
            .hearingType("Final Hearing (FH)")
            .hearingLength("1hr")
            .hearingTime("1pm")
            .applicantName("Poor Guy")
            .respondentName("test Korivi")
            .courtPhone("0300 1235577")
            .courtEmail("hastingsfamily@justice.gov.uk")
            .courtAddress("The Law Courts, Bohemia Road, Hastings, TN34 1QX")
            .courtName("Hastings County Court And Family Court Hearing Centre")
            .hearingDate("")
            .additionalHearingDated(additionalHearingDated)
            .build();
    }
}
