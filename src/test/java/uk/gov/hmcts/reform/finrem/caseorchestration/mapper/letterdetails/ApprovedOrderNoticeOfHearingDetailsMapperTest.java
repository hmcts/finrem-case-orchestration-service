package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.approvedorderhearing.ApprovedOrderNoticeOfHearingDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.approvedorderhearing.ApprovedOrderNoticeOfHearingDetailsMapper;

import java.util.Date;
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
        DocumentTemplateDetails actual = approvedOrderNoticeOfHearingDetailsMapper
            .buildDocumentTemplateDetails(caseDetails, caseDetails.getCaseData().getRegionWrapper().getDefaultCourtList());

        DocumentTemplateDetails expected = getExpectedApprovedOrderNoticeOfHearingDetails();

        assertEquals(expected, actual);
    }

    @Test
    public void givenValidCaseData_whenGetDocumentTemplateDetailsAsMap_thenReturnExpectedPlaceholdersMap() {
        Map<String, Object> placeholdersMap = approvedOrderNoticeOfHearingDetailsMapper
            .getDocumentTemplateDetailsAsMap(caseDetails, caseDetails.getCaseData().getRegionWrapper().getDefaultCourtList());

        ApprovedOrderNoticeOfHearingDetails expected = getExpectedApprovedOrderNoticeOfHearingDetails();
        Map<String, Object> actualData = getCaseData(placeholdersMap);

        assertEquals(actualData.get("HearingType"), expected.getHearingType());
        assertEquals(actualData.get("CourtName"), expected.getCourtName());
        assertEquals(actualData.get("ApplicantName"), expected.getApplicantName());
        assertEquals(actualData.get("HearingLength"), expected.getHearingLength());
        assertEquals(actualData.get("CourtAddress"), expected.getCourtAddress());
    }

    private ApprovedOrderNoticeOfHearingDetails getExpectedApprovedOrderNoticeOfHearingDetails() {
        return ApprovedOrderNoticeOfHearingDetails.builder()
            .ccdCaseNumber(1234567890L)
            .hearingVenue("Hastings County Court And Family Court Hearing Centre, The Law Courts, Bohemia Road, Hastings, TN34 1QX")
            .divorceCaseNumber("DD12D12345")
            .hearingType("Final Hearing (FH)")
            .hearingLength("1hr")
            .hearingTime("1pm")
            .applicantName("Poor Guy")
            .respondentName("test Korivi")
            .courtPhone("01634 887900")
            .courtEmail("FRCKSS@justice.gov.uk")
            .courtAddress("The Law Courts, Bohemia Road, Hastings, TN34 1QX")
            .courtName("Hastings County Court And Family Court Hearing Centre")
            .hearingDate("")
            .additionalHearingDated(new Date().toString())
            .build();
    }
}
