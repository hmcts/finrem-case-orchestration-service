package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.tabdata.managehearings;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CourtDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.hearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingMode;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingDocumentsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.PartyOnCase;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.PartyOnCaseCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.tabs.HearingTabItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogs;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HearingTabDataMapperTest {

    private static final String DEFAULT_HEARING_MODE = "Hearing mode not specified";
    private static final String DEFAULT_DATE_TIME = "Date and time not provided";
    private static final String DEFAULT_CONFIDENTIAL_PARTIES = "Parties not specified";

    private static final String COURT_NAME = "courtName";
    private static final String HEARING_TYPE = "Financial Dispute Resolution (FDR)";
    private static final String HEARING_DATE_TIME = "01 Aug 2025 10:00 AM";
    private static final String HEARING_TIME_ESTIMATE = "2 hours";
    private static final String HEARING_MODE = "Hybrid - In person and remote";
    private static final String ADDITIONAL_INFO = "Additional Info";
    private static final String DOCUMENT_1_FILENAME = "From1.pdf";
    private static final String DOCUMENT_2_FILENAME = "Form2.pdf";

    @TestLogs
    private final TestLogger logs = new TestLogger(HearingTabDataMapper.class);

    @InjectMocks
    private HearingTabDataMapper hearingTabDataMapper;
    @Mock
    private CourtDetailsMapper courtDetailsMapper;

    @Test
    void mapHearingToTabData() {
        // Arrange
        UUID hearingId = UUID.randomUUID();
        when(courtDetailsMapper.convertToFrcCourtDetails(any())).thenReturn(CourtDetails.builder().courtName(COURT_NAME).build());

        Hearing hearing = Hearing.builder()
            .hearingType(HearingType.FDR)
            .hearingDate(LocalDate.of(2025, 8, 1))
            .hearingTime("10:00 AM")
            .hearingTimeEstimate(HEARING_TIME_ESTIMATE)
            .hearingMode(HearingMode.HYBRID)
            .partiesOnCase(List.of(
                PartyOnCaseCollectionItem.builder()
                    .value(PartyOnCase.builder().role("Role1").label("Party1").build())
                    .build(),
                PartyOnCaseCollectionItem.builder()
                    .value(PartyOnCase.builder().role("Role2").label("Party2").build())
                    .build()
            ))
            .additionalHearingInformation(ADDITIONAL_INFO)
            .additionalHearingDocs(List.of(DocumentCollectionItem
                .builder()
                .value(CaseDocument.builder()
                    .documentFilename(DOCUMENT_1_FILENAME)
                    .build())
                .build()))
            .build();

        ManageHearingsCollectionItem hearingCollectionItem = ManageHearingsCollectionItem.builder()
            .id(hearingId)
            .value(hearing)
            .build();

        List<ManageHearingDocumentsCollectionItem> hearingDocumentsCollection = List.of(
            ManageHearingDocumentsCollectionItem.builder()
                .value(ManageHearingDocument.builder()
                    .hearingId(hearingId)
                    .hearingDocument(CaseDocument.builder()
                        .documentFilename(DOCUMENT_2_FILENAME)
                        .build())
                    .build())
                .build()
        );

        // Act
        HearingTabItem result = hearingTabDataMapper.mapHearingToTabData(hearingCollectionItem, hearingDocumentsCollection);

        // Assert
        assertNotNull(result);
        assertEquals(HEARING_TYPE, result.getTabHearingType());
        assertEquals(COURT_NAME, result.getTabCourtSelection());
        assertEquals(HEARING_MODE, result.getTabAttendance());
        assertEquals(HEARING_DATE_TIME, result.getTabDateTime());
        assertEquals(HEARING_TIME_ESTIMATE, result.getTabTimeEstimate());
        assertEquals(ADDITIONAL_INFO, result.getTabAdditionalInformation());
        assertEquals("Party1, Party2", result.getTabConfidentialParties());
        assertEquals(2, result.getTabHearingDocuments().size());
        assertTrue(result.getTabHearingDocuments().stream()
            .anyMatch(doc -> doc.getValue().getDocumentFilename().equals(DOCUMENT_1_FILENAME)));
        assertTrue(result.getTabHearingDocuments().stream()
            .anyMatch(doc -> doc.getValue().getDocumentFilename().equals(DOCUMENT_2_FILENAME)));
    }

    @Test
    void mapHearingToTabData_withNullValues_returnsDefaultsAndEmptyDocs() {
        // Arrange
        UUID hearingId = UUID.randomUUID();
        when(courtDetailsMapper.convertToFrcCourtDetails(any())).thenReturn(CourtDetails.builder().courtName(COURT_NAME).build());

        Hearing hearing = Hearing.builder()
            .hearingType(HearingType.FDR)
            .hearingDate(null)
            .hearingTime("10:00 AM")
            .hearingTimeEstimate(HEARING_TIME_ESTIMATE)
            .hearingMode(null)
            .partiesOnCase(null)
            .additionalHearingInformation(null)
            .additionalHearingDocs(null)
            .build();

        ManageHearingsCollectionItem hearingCollectionItem = ManageHearingsCollectionItem.builder()
            .id(hearingId)
            .value(hearing)
            .build();

        List<ManageHearingDocumentsCollectionItem> hearingDocumentsCollection = List.of();

        // Act
        HearingTabItem result = hearingTabDataMapper.mapHearingToTabData(hearingCollectionItem, hearingDocumentsCollection);

        // Assert
        assertNotNull(result);
        assertEquals(HEARING_TYPE, result.getTabHearingType());
        assertEquals(COURT_NAME, result.getTabCourtSelection());
        assertEquals(DEFAULT_HEARING_MODE, result.getTabAttendance());
        assertEquals(DEFAULT_DATE_TIME, result.getTabDateTime());
        assertEquals(HEARING_TIME_ESTIMATE, result.getTabTimeEstimate());
        assertEquals(DEFAULT_CONFIDENTIAL_PARTIES, result.getTabConfidentialParties());
        assertEquals(" ", result.getTabAdditionalInformation());
        assertThat(result.getTabHearingDocuments()).isEmpty();
    }

    /*
     * mapHearingDocumentsToTabData and convertToFrcCourtDetails methods can throw exceptions with invalid data.
     * this test is mocked to confirm that calls to these methods handle the expected exceptions.
     * Assertions made to check the information returned when these exceptions are handled.
     * Also asserts that expected log messages are produced.
     */
    @Test
    void mapHearingToTabDataWithExceptions() {
        // Arrange
        when(courtDetailsMapper.convertToFrcCourtDetails(any())).thenThrow(new IllegalStateException("a message"));

        Hearing hearing = Hearing.builder()
            .hearingType(HearingType.FDR)
            .additionalHearingDocs(List.of(DocumentCollectionItem
                .builder()
                .value(CaseDocument.builder()
                    .documentFilename(DOCUMENT_1_FILENAME)
                    .build())
                .build()))
            .build();

        ManageHearingsCollectionItem hearingCollectionItem = ManageHearingsCollectionItem.builder()
            .id(UUID.randomUUID())
            .value(hearing)
            .build();

        // Create a situation resulting in a Null Pointer Exception (document item missing values)
        List<ManageHearingDocumentsCollectionItem> hearingDocumentsCollection =
            List.of(new ManageHearingDocumentsCollectionItem());

        // Act
        HearingTabItem result = hearingTabDataMapper.mapHearingToTabData(hearingCollectionItem, hearingDocumentsCollection);

        // Assert - default values returned when exceptions handled
        assertEquals("Court name not available", result.getTabCourtSelection());
        // Hearing had 1 valid document, but empty list returned when there is an exception.
        assertThat(result.getTabHearingDocuments().isEmpty());

        // assert logs
        assertThat(logs.getErrors()).contains("Caught an exception when retrieving court name. 'Court name not available' provided instead.");
        assertThat(logs.getErrors()).contains("NullPointerException mapping hearing documents to tab data.");
    }
}
