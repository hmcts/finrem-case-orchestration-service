package uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CourtDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CourtDetailsConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManageHearingsDocumentServiceTest {

    @InjectMocks
    private ManageHearingsDocumentService manageHearingsDocumentService;

    @Mock
    private CourtDetailsConfiguration courtDetailsConfiguration;
    @Mock
    private GenericDocumentService genericDocumentService;
    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;
    @Mock
    private DocumentConfiguration documentConfiguration;

    private static final String AUTHORISATION_TOKEN = "authToken";
    private static final String TEMPLATE = "template";
    private static final String FILE_NAME = "fileName";

    /**
     * Test case for generateHearingNotice method.
     * This test checks that the method forwards the correct info for doc generation.
     * Builds a FinremCaseDetails instance composed of Hearing and FinremCaseData instances.
     * A simple CaseDetails instance is built to be returned by the mock finremCaseDetailsMapper.
     */
    @ParameterizedTest
    @MethodSource("hearingProvider")
    void shouldGenerateHearingNotice() {
        Hearing hearing = Hearing.builder()
            .hearingType(HearingType.APPEAL_HEARING)
            .hearingDate(LocalDate.now())
            .hearingTime("10:00 AM")
            .hearingTimeEstimate("2 hours")
            .hearingMode(HearingMode.IN_PERSON)
            .additionalHearingInformation("Additional Info")
            .hearingCourtSelection(Court
                .builder()
                .region(Region.LONDON)
                .londonList(RegionLondonFrc.LONDON)
                .courtListWrapper(DefaultCourtListWrapper.builder()
                    .cfcCourtList(CfcCourt.BROMLEY_COUNTY_COURT_AND_FAMILY_COURT)
                    .build()).build())
            .build();

        FinremCaseData finremCaseData = FinremCaseData.builder()
            .contactDetailsWrapper(ContactDetailsWrapper
                .builder()
                .applicantFmName("Bilbo")
                .applicantLname("Baggins")
                .respondentFmName("Smeagol")
                .respondentLname("Gollum")
                .build())
            .build();
        finremCaseData.getManageHearingsWrapper().setWorkingHearing(hearing);

        FinremCaseDetails finremCaseDetails = FinremCaseDetails.builder()
            .id(12345L)
            .data(finremCaseData)
            .build();

        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345L)
            .data(new HashMap<>())
            .build();

        CaseDocument expectedDocument = CaseDocument.builder()
            .documentUrl("http://document.url")
            .build();

        when(finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails)).thenReturn(caseDetails);
        when(courtDetailsConfiguration.getCourts()).thenReturn(Map.of("FR_s_CFCList_1", CourtDetails
            .builder()
                .courtAddress("Bromley County Court, College Road, Bromley, BR1 3PX")
                .courtName("Bromley County Court And Family Court")
                .phoneNumber("1234567890")
                .email("some@email.com")
            .build()));
        when(documentConfiguration.getManageHearingNoticeTemplate()).thenReturn(TEMPLATE);
        when(documentConfiguration.getManageHearingNoticeFileName()).thenReturn(FILE_NAME);
        when(genericDocumentService.generateDocumentFromPlaceholdersMap(eq(AUTHORISATION_TOKEN), any(), eq(TEMPLATE), eq(FILE_NAME), eq("12345")))
            .thenReturn(expectedDocument);

        // Call the method under test
        CaseDocument result = manageHearingsDocumentService.generateHearingNotice(hearing, finremCaseDetails, AUTHORISATION_TOKEN);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getDocumentUrl()).isEqualTo(expectedDocument.getDocumentUrl());
        assertThat(result.getCategoryId()).isEqualTo(DocumentCategory.HEARING_DOCUMENTS.getDocumentCategoryId());

        verify(finremCaseDetailsMapper).mapToCaseDetails(finremCaseDetails);
        verify(courtDetailsConfiguration).getCourts();
        verify(documentConfiguration).getManageHearingNoticeTemplate();
        verify(documentConfiguration).getManageHearingNoticeFileName();
        verify(genericDocumentService).generateDocumentFromPlaceholdersMap(eq(AUTHORISATION_TOKEN), any(), eq(TEMPLATE), eq(FILE_NAME), eq("12345"));
    }

    static Stream<Arguments> hearingProvider() {
        return Stream.of(
                // a complete hearing
                Arguments.of(Hearing.builder()
                        .hearingType(HearingType.APPEAL_HEARING)
                        .hearingDate(LocalDate.now())
                        .hearingTime("10:00 AM")
                        .hearingTimeEstimate("2 hours")
                        .hearingMode(HearingMode.IN_PERSON)
                        .additionalHearingInformation("Additional Info")
                        .hearingCourtSelection(Court.builder()
                                .region(Region.LONDON)
                                .londonList(RegionLondonFrc.LONDON)
                                .courtListWrapper(DefaultCourtListWrapper.builder()
                                        .cfcCourtList(CfcCourt.BROMLEY_COUNTY_COURT_AND_FAMILY_COURT)
                                        .build())
                                .build())
                        .build()),
                // a hearing without hearing mode (migrated cases), or additional information (optional field)
                Arguments.of(Hearing.builder()
                        .hearingType(HearingType.FDA)
                        .hearingDate(LocalDate.now())
                        .hearingTime("14:00")
                        .hearingTimeEstimate("An amount of time")
                        .hearingMode(null)
                        .additionalHearingInformation(null)
                        .hearingCourtSelection(Court.builder()
                                .region(Region.LONDON)
                                .londonList(RegionLondonFrc.LONDON)
                                .courtListWrapper(DefaultCourtListWrapper.builder()
                                        .cfcCourtList(CfcCourt.BROMLEY_COUNTY_COURT_AND_FAMILY_COURT)
                                        .build())
                                .build())
                        .build())
        );
    }
}
