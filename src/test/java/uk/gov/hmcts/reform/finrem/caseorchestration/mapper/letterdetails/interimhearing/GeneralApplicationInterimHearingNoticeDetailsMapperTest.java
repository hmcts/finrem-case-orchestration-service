package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.interimhearing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.AbstractLetterDetailsMapperTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.DocumentTemplateDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.FrcCourtDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.CfcCourt;
import uk.gov.hmcts.reform.finrem.ccd.domain.InterimHearingCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.wrapper.InterimCourtListWrapper;

import java.time.LocalDate;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
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
            caseDetails.getCaseData().getRegionWrapper().getInterimCourtList());

        assertEquals(expected, actual);
    }

    @Test
    public void givenValidCaseData_whenBuildDocumentTemplateDetailsFromCollection_thenReturnExpectedTemplateDetails() {
        setCaseDetails(ONE_MIGRATED_MODIFIED_AND_ONE_ADDED_HEARING_JSON);
        InterimHearingCollection interimHearingCollectionItem =
            Iterables.getLast(caseDetails.getCaseData().getInterimWrapper().getInterimHearings());

        DocumentTemplateDetails expected = getExpectedDetailsFromCollectionItem();

        DocumentTemplateDetails actual = generalApplicationInterimHearingNoticeDetailsMapper.buildDocumentTemplateDetails(caseDetails,
            interimHearingCollectionItem);

        assertEquals(expected, actual);
    }

    @Test
    public void givenValidCaseData_whenGetPlaceholdersMapFromCollection_thenReturnExpectedMap() {
        setCaseDetails(ONE_MIGRATED_MODIFIED_AND_ONE_ADDED_HEARING_JSON);
        InterimHearingCollection interimHearingCollectionItem =
            Iterables.getLast(caseDetails.getCaseData().getInterimWrapper().getInterimHearings());

        GeneralApplicationInterimHearingNoticeDetails expected = getExpectedDetailsFromCollectionItem();

        Map<String, Object> placeholdersMap = generalApplicationInterimHearingNoticeDetailsMapper
            .getDocumentTemplateDetailsFromCollectionItem(caseDetails, interimHearingCollectionItem);

        Map<String, Object> actualData = getCaseData(placeholdersMap);

        assertThat(actualData, allOf(
            hasEntry("courtDetails", ImmutableMap.of(
                "courtName", expected.getCourtDetails().getCourtName(),
                "courtAddress", expected.getCourtDetails().getCourtAddress(),
                "phoneNumber", expected.getCourtDetails().getPhoneNumber(),
                "email", expected.getCourtDetails().getEmail())),
            Matchers.<String, Object>hasEntry("applicantName", expected.getApplicantName()),
            Matchers.<String, Object>hasEntry("respondentName", expected.getRespondentName()),
            Matchers.<String, Object>hasEntry("hearingVenue",
                expected.getHearingVenue()),
            Matchers.<String, Object>hasEntry("interimHearingType",
                expected.getInterimHearingType()),
            Matchers.<String, Object>hasEntry("interimHearingType",
                expected.getInterimHearingType()),
            Matchers.<String, Object>hasEntry("interimHearingTime",
                expected.getInterimHearingTime()),
            Matchers.<String, Object>hasEntry("interimTimeEstimate",
                expected.getInterimTimeEstimate()),
            Matchers.<String, Object>hasEntry("interimHearingDate",
                expected.getInterimHearingDate()),
            hasKey("letterDate")));
    }

    private GeneralApplicationInterimHearingNoticeDetails getExpectedGeneralApplicationInterimHearingNoticeDetails() {
        FrcCourtDetails courtDetails = getCourtDetails(CfcCourt.KINGSTON_UPON_THAMES_COUNTY_COURT_AND_FAMILY_COURT);
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
        FrcCourtDetails courtDetails = getCourtDetails(CfcCourt.CROYDON_COUNTY_COURT_AND_FAMILY_COURT);
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

    private FrcCourtDetails getCourtDetails(CfcCourt court) {
        InterimCourtListWrapper courtListWrapper = new InterimCourtListWrapper();
        courtListWrapper.setInterimCfcCourtList(court);
        return new CourtDetailsMapper(new ObjectMapper()).getCourtDetails(courtListWrapper);
    }
}