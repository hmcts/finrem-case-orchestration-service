package uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.tabdata.managehearings.HearingTabDataMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.tabs.HearingTabCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.tabs.HearingTabItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ManageHearingActionServiceTest {

    @Mock
    private ManageHearingsDocumentService manageHearingsDocumentService;

    @Mock
    private HearingTabDataMapper hearingTabDataMapper;

    @InjectMocks
    private ManageHearingActionService manageHearingActionService;

    @Captor
    private ArgumentCaptor<ManageHearingsCollectionItem> hearingCaptor;

    private static final String AUTH_TOKEN = "authToken";

    private FinremCaseDetails finremCaseDetails;
    private ManageHearingsWrapper hearingWrapper;
    private Hearing hearing;

    @BeforeEach
    void setUp() {
        hearing = Hearing.builder()
            .hearingType(HearingType.DIR)
            .hearingDate(java.time.LocalDate.now())
            .hearingTime("10:00")
            .hearingTimeEstimate("30mins")
            .build();

        hearingWrapper = ManageHearingsWrapper.builder()
            .workingHearing(hearing)
            .build();

        finremCaseDetails = FinremCaseDetails.builder()
            .data(uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData.builder()
                .manageHearingsWrapper(hearingWrapper)
                .build())
            .build();
    }

    @Test
    void shouldAddHearingAndGenerateHearingNotice() {
        // Arrange
        CaseDocument hearingNotice = CaseDocument.builder()
            .documentFilename("HearingNotice.pdf")
            .documentUrl("http://example.com/hearing-notice")
            .build();

        HearingTabItem hearingTabItem = HearingTabItem.builder()
            .tabHearingType("Some Hearing Type")
            .tabCourtSelection("Some Court")
            .tabAttendance("Some Attendance")
            .tabDateTime("20 Jul 2025 10:00")
            .tabTimeEstimate("30 mins")
            .tabConfidentialParties("Party A, Party B")
            .tabAdditionalInformation("Some additional information")
            .tabHearingDocuments(List.of(DocumentCollectionItem
                .builder()
                    .value(CaseDocument
                        .builder()
                        .documentFilename("HearingNotice.pdf")
                        .documentUrl("http://example.com/hearing-notice")
                        .documentBinaryUrl("http://example.com/hearing-notice-binary")
                        .build())
                .build()))
            .build();

        when(manageHearingsDocumentService.generateHearingNotice(hearing, finremCaseDetails, AUTH_TOKEN))
            .thenReturn(hearingNotice);

        when(hearingTabDataMapper.mapHearingToTabData(any(), any()))
            .thenReturn(hearingTabItem);

        // Act
        manageHearingActionService.performAddHearing(finremCaseDetails, AUTH_TOKEN);

        // Assert
        assertThat(hearingWrapper.getHearings()).hasSize(1);
        assertThat(hearingWrapper.getHearingTabItems()).hasSize(1);
        UUID hearingId = hearingWrapper.getWorkingHearingId();
        assertThat(hearingWrapper.getHearings().getFirst().getId()).isEqualTo(hearingId);
        assertThat(hearingWrapper.getHearings().getFirst().getValue()).isEqualTo(hearing);

        assertThat(hearingWrapper.getHearingDocumentsCollection()).hasSize(1);
        assertThat(hearingWrapper.getHearingDocumentsCollection().getFirst().getValue().getHearingId())
            .isEqualTo(hearingId);
        assertThat(hearingWrapper.getHearingDocumentsCollection().getFirst().getValue().getHearingDocument())
            .isEqualTo(hearingNotice);

        // Verify the mocked method is called with the expected hearing
        verify(hearingTabDataMapper).mapHearingToTabData(
            argThat(hearingItem -> hearingItem.getValue().equals(hearing)),
            argThat(docCollection -> ((
                docCollection.getFirst().getValue().getHearingId().equals(hearingId)
                    && docCollection.getFirst().getValue().getHearingDocument().equals(hearingNotice))
            ))
        );
    }

    @Test
    void shouldAddHearingToTabCollectionInCorrectOrder() {
        // Arrange
        Hearing hearing1 = Hearing.builder()
            .hearingType(HearingType.DIR)
            .hearingDate(java.time.LocalDate.of(2025, 7, 20))
            .hearingTime("10:00")
            .build();

        Hearing hearing2 = Hearing.builder()
            .hearingType(HearingType.FDA)
            .hearingDate(java.time.LocalDate.of(2025, 7, 15))
            .hearingTime("11:00")
            .build();

        ManageHearingsWrapper hearingWrapper = ManageHearingsWrapper.builder()
            .workingHearing(hearing1)
            .hearings(new ArrayList<>(List.of(ManageHearingsCollectionItem.builder()
                .id(UUID.randomUUID())
                .value(hearing2)
                .build())))
            .build();

        finremCaseDetails.getData().setManageHearingsWrapper(hearingWrapper);

        HearingTabItem hearingTabItem1 = HearingTabItem.builder()
            .tabHearingType("Hearing 1")
            .tabDateTime("20 Jul 2025 10:00")
            .build();

        HearingTabItem hearingTabItem2 = HearingTabItem.builder()
            .tabHearingType("Hearing 2")
            .tabDateTime("15 Jul 2025 11:00")
            .build();

        when(manageHearingsDocumentService.generateHearingNotice(hearing1, finremCaseDetails, AUTH_TOKEN))
            .thenReturn(CaseDocument.builder().build());

        when(hearingTabDataMapper.mapHearingToTabData(any(), any()))
            .thenReturn(hearingTabItem2, hearingTabItem1);

        // Act
        manageHearingActionService.performAddHearing(finremCaseDetails, AUTH_TOKEN);

        // Assert
        verify(hearingTabDataMapper, times(2)).mapHearingToTabData(hearingCaptor.capture(), any());

        List<ManageHearingsCollectionItem> capturedHearings = hearingCaptor.getAllValues();
        assertThat(capturedHearings.get(0).getValue()).isEqualTo(hearing2);
        assertThat(capturedHearings.get(1).getValue()).isEqualTo(hearing1);

        List<HearingTabCollectionItem> hearingTabItems = hearingWrapper.getHearingTabItems();
        assertThat(hearingTabItems).hasSize(2);
        assertThat(hearingTabItems.get(0).getValue().getTabDateTime()).isEqualTo("15 Jul 2025 11:00");
        assertThat(hearingTabItems.get(1).getValue().getTabDateTime()).isEqualTo("20 Jul 2025 10:00");
    }
}
