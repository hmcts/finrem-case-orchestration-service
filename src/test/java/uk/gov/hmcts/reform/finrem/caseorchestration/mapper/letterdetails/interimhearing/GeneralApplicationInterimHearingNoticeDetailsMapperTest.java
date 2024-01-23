package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.interimhearing;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.AbstractLetterDetailsMapperTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CfcCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.InterimCourtListWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CourtDetailsTemplateFields;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.DocumentTemplateDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.GeneralApplicationInterimHearingNoticeDetails;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GeneralApplicationInterimHearingNoticeDetailsMapperTest extends AbstractLetterDetailsMapperTest {

    private static final String INTERIM_JSON = "/fixtures/contested-interim-hearing.json";
    private static final String ONE_MIGRATED_MODIFIED_AND_ONE_ADDED_HEARING_JSON =
        "/fixtures/contested/interim-hearing-two-collection-modified.json";

    @Autowired
    private GeneralApplicationInterimHearingNoticeDetailsMapper generalApplicationInterimHearingNoticeDetailsMapper;

    @Test
    public void givenValidCaseData_whenBuildDocumentTemplateDetails_thenReturnExpectedTemplateDetails() {
        setCaseDetails(INTERIM_JSON);

        DocumentTemplateDetails expected = getExpectedGeneralApplicationInterimHearingNoticeDetails();

        DocumentTemplateDetails actual = generalApplicationInterimHearingNoticeDetailsMapper.buildDocumentTemplateDetails(caseDetails,
            caseDetails.getData().getRegionWrapper().getInterimCourtList());

        assertEquals(expected, actual);
    }


    private GeneralApplicationInterimHearingNoticeDetails getExpectedGeneralApplicationInterimHearingNoticeDetails() {
        CourtDetailsTemplateFields courtDetails = getCourtDetails(CfcCourt.KINGSTON_UPON_THAMES_COUNTY_COURT_AND_FAMILY_COURT);
        return GeneralApplicationInterimHearingNoticeDetails.builder()
            .letterDate(String.valueOf(LocalDate.now()))
            .interimTimeEstimate("30 minutes")
            .interimHearingType("Directions (DIR)")
            .interimHearingTime("2:00 pm")
            .interimHearingDate("2020-06-01")
            .ccdCaseNumber(1234567890L)
            .interimAdditionalInformationAboutHearing("refreshments will be provided")
            .hearingVenue(courtDetails.getCourtContactDetailsAsOneLineAddressString())
            .courtDetails(courtDetails)
            .divorceCaseNumber("DD12D12345")
            .applicantName("Poor Guy")
            .respondentName("test Korivi")
            .build();
    }

    private GeneralApplicationInterimHearingNoticeDetails getExpectedDetailsFromCollectionItem() {
        CourtDetailsTemplateFields courtDetails = getCourtDetails(CfcCourt.CROYDON_COUNTY_COURT_AND_FAMILY_COURT);
        return GeneralApplicationInterimHearingNoticeDetails.builder()
            .letterDate(String.valueOf(LocalDate.now()))
            .interimHearingType("First Directions Appointment (FDA)")
            .interimHearingTime("5 hour")
            .interimTimeEstimate("Test")
            .interimHearingDate("2040-10-10")
            .ccdCaseNumber(123L)
            .interimAdditionalInformationAboutHearing("This is second hearing")
            .hearingVenue(courtDetails.getCourtContactDetailsAsOneLineAddressString())
            .courtDetails(courtDetails)
            .divorceCaseNumber("DD12D12345")
            .applicantName("Applicant test")
            .respondentName("respondent test")
            .build();
    }

    private CourtDetailsTemplateFields getCourtDetails(CfcCourt court) {
        InterimCourtListWrapper courtListWrapper = new InterimCourtListWrapper();
        courtListWrapper.setInterimCfcCourtList(court);
        return new CourtDetailsMapper(new ObjectMapper()).getCourtDetails(courtListWrapper);
    }
}