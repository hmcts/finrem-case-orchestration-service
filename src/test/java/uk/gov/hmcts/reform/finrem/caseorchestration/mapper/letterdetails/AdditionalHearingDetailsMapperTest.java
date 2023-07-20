package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.additionalhearing.AdditionalHearingDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.AdditionalHearingDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.DocumentTemplateDetails;

import java.util.Date;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AdditionalHearingDetailsMapperTest extends AbstractLetterDetailsMapperTest {

    public static final String BULK_PRINT_ADDITIONAL_HEARING_JSON = "/fixtures/bulkprint/bulk-print-additional-hearing.json";

    @Autowired
    AdditionalHearingDetailsMapper additionalHearingDetailsMapper;

    @Before
    public void setUp() {
        CaseDetails cd = buildCaseDetailsFromJson(BULK_PRINT_ADDITIONAL_HEARING_JSON);
        caseDetails = finremCaseDetailsMapper.mapToFinremCaseDetails(cd);
    }

    @Test
    public void mapBulkPrintDetails() {
        assertNotNull(caseDetails);
        Assert.assertEquals("Test", caseDetails.getData().getContactDetailsWrapper().getApplicantFmName());
    }

    @Test
    public void givenValidCaseDetails_whenBuildDocumentTemplateDetails_thenReturnDocumentTemplateDetails() {
        DocumentTemplateDetails actual = additionalHearingDetailsMapper.buildDocumentTemplateDetails(caseDetails,
            caseDetails.getData().getRegionWrapper().getDefaultCourtList());

        DocumentTemplateDetails expected = getExpectedAdditionalHearingDetails();

        assertEquals(expected, actual);
    }

    @Test
    public void givenValidCaseDetails_whenGetDocumentTemplateDetailsAsMap_thenReturnDocumentTemplatesMap() {
        Map<String, Object> actualPlaceholdersMap = additionalHearingDetailsMapper.getDocumentTemplateDetailsAsMap(caseDetails,
            caseDetails.getData().getRegionWrapper().getDefaultCourtList());

        AdditionalHearingDetails expected = getExpectedAdditionalHearingDetails();

        Map<String, Object> actualData = getCaseData(actualPlaceholdersMap);

        assertEquals(actualData.get("CourtName"), expected.getCourtName());
        assertEquals(actualData.get("DivorceCaseNumber"), expected.getDivorceCaseNumber());
        assertEquals(actualData.get("RespondentName"), expected.getRespondentName());
        assertEquals(actualData.get("HearingType"), expected.getHearingType());
        assertEquals(actualData.get("HearingVenue"), expected.getHearingVenue());
    }

    private AdditionalHearingDetails getExpectedAdditionalHearingDetails() {
        return AdditionalHearingDetails.builder()
            .ccdCaseNumber("1234567890")
            .additionalHearingDated(new Date())
            .hearingDate("2021-01-01")
            .hearingType("Directions (DIR)")
            .hearingTime("12:00")
            .courtName("Nottingham County Court And Family Court")
            .courtAddress("60 Canal Street, Nottingham NG1 7EJ")
            .courtPhone("0115 910 3504")
            .courtEmail("FRCNottingham@justice.gov.uk")
            .hearingLength("30 minutes")
            .hearingVenue("Nottingham County Court And Family Court")
            .divorceCaseNumber("AB01D23456")
            .applicantName("Test Applicant")
            .respondentName("Name Respondent")
            .build();
    }
}
