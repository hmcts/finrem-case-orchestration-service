package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.managehearings;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CourtDetailsConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CfcCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Court;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Region;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionLondonFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingMode;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DefaultCourtListWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CourtDetailsTemplateFields;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.DocumentTemplateDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.managehearings.HearingNoticeLetterDetails;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class HearingNoticeLetterDetailsMapperTest {
    @Mock
    private CourtDetailsMapper courtDetailsMapper;

    @Mock
    private CourtDetailsConfiguration courtDetailsConfiguration;

    @InjectMocks
    private HearingNoticeLetterDetailsMapper hearingNoticeLetterDetailsMapper;

    @Test
    void shouldBuildDocumentTemplateDetails() {
        // Arrange
        FinremCaseData caseData = FinremCaseData.builder()
            .contactDetailsWrapper(ContactDetailsWrapper
                .builder()
                .applicantFmName("John")
                .applicantLname("Doe")
                .respondentFmName("Jane")
                .respondentLname("Smith")
                .solicitorReference(TestConstants.TEST_SOLICITOR_REFERENCE)
                .respondentSolicitorReference(TestConstants.TEST_RESP_SOLICITOR_REFERENCE)
                .build())
            .manageHearingsWrapper(
                ManageHearingsWrapper.builder()
                    .workingHearing(Hearing.builder()
                        .hearingType(HearingType.FDR)
                        .hearingDate(LocalDate.of(2025, 8,1))
                        .hearingTime("10:00 AM")
                        .hearingTimeEstimate("2 hours")
                        .hearingMode(HearingMode.IN_PERSON)
                        .additionalHearingInformation("Additional info")
                        .hearingCourtSelection(Court
                            .builder()
                            .region(Region.LONDON)
                            .londonList(RegionLondonFrc.LONDON)
                            .courtListWrapper(DefaultCourtListWrapper
                                .builder()
                                .cfcCourtList(CfcCourt.BROMLEY_COUNTY_COURT_AND_FAMILY_COURT)
                            .build())
                        .build())
                    .build())
                .build())
            .build();

        FinremCaseDetails caseDetails = FinremCaseDetails.builder()
            .id(12345L)
            .data(caseData)
            .build();

        CourtDetailsTemplateFields courtTemplateFields = CourtDetailsTemplateFields.builder()
            .courtName("London Court")
            .phoneNumber("010000 00000")
            .email("email@test.com")
            .courtAddress("123 Court Street, London")
            .build();

        when(courtDetailsConfiguration.buildCourtDetailsTemplateFields("London Court"))
            .thenReturn(courtTemplateFields);

        // Act
        DocumentTemplateDetails result = hearingNoticeLetterDetailsMapper.buildDocumentTemplateDetails(caseDetails);

        // Assert
        assertThat(result).isInstanceOf(HearingNoticeLetterDetails.class);
        HearingNoticeLetterDetails hearingNoticeDetails = (HearingNoticeLetterDetails) result;
        assertThat(hearingNoticeDetails.getCcdCaseNumber()).isEqualTo("12345");
        assertThat(hearingNoticeDetails.getApplicantName()).isEqualTo("John Doe");
        assertThat(hearingNoticeDetails.getRespondentName()).isEqualTo("Jane Smith");
        assertThat(hearingNoticeDetails.getHearingType()).isEqualTo("FDR");
        assertThat(hearingNoticeDetails.getHearingDate()).isEqualTo("2023-10-01");
        assertThat(hearingNoticeDetails.getHearingTime()).isEqualTo("10:00 AM");
        assertThat(hearingNoticeDetails.getHearingTimeEstimate()).isEqualTo("2 hours");
        assertThat(hearingNoticeDetails.getAttendance()).isEqualTo("In Person");
        assertThat(hearingNoticeDetails.getAdditionalHearingInformation()).isEqualTo("Additional info");
        assertThat(hearingNoticeDetails.getCourtDetails()).isEqualTo(courtTemplateFields);
        assertThat(hearingNoticeDetails.getHearingVenue()).isEqualTo("123 Court Street, London");
    }
}