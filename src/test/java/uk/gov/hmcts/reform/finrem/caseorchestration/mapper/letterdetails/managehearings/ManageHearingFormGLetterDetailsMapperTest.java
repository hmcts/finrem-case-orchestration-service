package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.managehearings;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CourtDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CourtDetailsConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CfcCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Court;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Region;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionLondonFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingMode;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.WorkingHearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DefaultCourtListWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CourtDetailsTemplateFields;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.DocumentTemplateDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.FormGLetterDetails;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;

@ExtendWith(MockitoExtension.class)
class ManageHearingFormGLetterDetailsMapperTest {

    @Mock
    private CourtDetailsConfiguration courtDetailsConfiguration;

    private ManageHearingFormGLetterDetailsMapper manageHearingFormGLetterDetailsMapper;

    @BeforeEach
    void setUp() {
        manageHearingFormGLetterDetailsMapper = new ManageHearingFormGLetterDetailsMapper(
            new ObjectMapper(), courtDetailsConfiguration);
    }

    @Test
    void shouldBuildDocumentTemplateDetails() {
        // Arrange
        FinremCaseData caseData = FinremCaseData.builder()
            .ccdCaseType(CaseType.CONTESTED)
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
                    .workingHearing(WorkingHearing.builder()
                        .hearingTypeDynamicList(DynamicList.builder()
                            .value(DynamicListElement.builder()
                                .code(HearingType.FDR.name())
                                .label(HearingType.FDR.getId())
                                .build())
                            .build())
                        .hearingDate(LocalDate.of(2025, 8, 1))
                        .hearingTime("10:00 AM")
                        .hearingTimeEstimate("2 hours")
                        .hearingMode(HearingMode.IN_PERSON)
                        .additionalHearingInformation("Additional info")
                        .hearingCourtSelection(Court.builder()
                            .region(Region.LONDON)
                            .londonList(RegionLondonFrc.LONDON)
                            .courtListWrapper(DefaultCourtListWrapper.builder()
                                .cfcCourtList(CfcCourt.BROMLEY_COUNTY_COURT_AND_FAMILY_COURT)
                                .build())
                            .build())
                        .partiesOnCaseMultiSelectList(DynamicMultiSelectList.builder()
                            .value(List.of(
                                DynamicMultiSelectListElement.builder()
                                    .code("[APPSOLICITOR]")
                                    .label("Applicant Solicitor - Hamzah")
                                    .build()
                            ))
                            .build())
                        .build())
                    .build())
            .build();

        FinremCaseDetails caseDetails = FinremCaseDetails.builder()
            .id(Long.valueOf(CASE_ID))
            .data(caseData)
            .build();

        CourtDetailsTemplateFields courtTemplateFields = CourtDetailsTemplateFields.builder()
            .courtName("London Court")
            .phoneNumber("010000 00000")
            .email("email@test.com")
            .courtAddress("123 Court Street, London")
            .build();

        CourtDetails courtDetails = CourtDetails.builder()
            .courtName("London Court")
            .phoneNumber("010000 00000")
            .email("email@test.com")
            .courtAddress("123 Court Street, London")
            .build();

        when(courtDetailsConfiguration.getCourts()).thenReturn(Map.of("FR_s_CFCList_1", courtDetails));

        // Act
        DocumentTemplateDetails result = manageHearingFormGLetterDetailsMapper.buildDocumentTemplateDetails(caseDetails);

        // Assert
        FormGLetterDetails formGLetterDetails = (FormGLetterDetails) result;
        assertThat(formGLetterDetails.getCaseNumber()).isEqualTo(CASE_ID);
        assertThat(formGLetterDetails.getApplicantFmName()).isEqualTo("John");
        assertThat(formGLetterDetails.getApplicantLName()).isEqualTo("Doe");
        assertThat(formGLetterDetails.getRespondentFmName()).isEqualTo("Jane");
        assertThat(formGLetterDetails.getRespondentLName()).isEqualTo("Smith");
        assertThat(formGLetterDetails.getSolicitorReference()).isEqualTo(TestConstants.TEST_SOLICITOR_REFERENCE);
        assertThat(formGLetterDetails.getRespondentSolicitorReference()).isEqualTo(TestConstants.TEST_RESP_SOLICITOR_REFERENCE);
        assertThat(formGLetterDetails.getHearingDate()).isEqualTo("2025-08-01");
        assertThat(formGLetterDetails.getHearingTime()).isEqualTo("10:00 AM");
        assertThat(formGLetterDetails.getCourtDetails()).isEqualTo(courtTemplateFields);
    }

    @Test
    void shouldThrowExceptionWhenWorkingHearingIsNull() {
        // Arrange
        FinremCaseData caseData = FinremCaseData.builder()
            .manageHearingsWrapper(ManageHearingsWrapper.builder().workingHearing(null).build())
            .build();

        FinremCaseDetails caseDetails = FinremCaseDetails.builder()
            .id(Long.valueOf(CASE_ID))
            .data(caseData)
            .build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> manageHearingFormGLetterDetailsMapper.buildDocumentTemplateDetails(caseDetails));
        assertThat(exception.getMessage()).isEqualTo("Working hearing is null");
    }
}
