package uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ManageHearingActionServiceTest {

    @Mock
    private ManageHearingsDocumentService manageHearingsDocumentService;

    @InjectMocks
    private ManageHearingActionService manageHearingActionService;

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

        when(manageHearingsDocumentService.generateHearingNotice(hearing, finremCaseDetails, AUTH_TOKEN))
            .thenReturn(hearingNotice);

        // Act
        manageHearingActionService.performAddHearing(finremCaseDetails, AUTH_TOKEN);

        // Assert
        assertThat(hearingWrapper.getHearings()).hasSize(1);
        UUID hearingId = hearingWrapper.getWorkingHearingId();
        assertThat(hearingWrapper.getHearings().getFirst().getId()).isEqualTo(hearingId);
        assertThat(hearingWrapper.getHearings().getFirst().getValue()).isEqualTo(hearing);

        assertThat(hearingWrapper.getHearingDocumentsCollection()).hasSize(1);
        assertThat(hearingWrapper.getHearingDocumentsCollection().getFirst().getValue().getHearingId())
            .isEqualTo(hearingId);
        assertThat(hearingWrapper.getHearingDocumentsCollection().getFirst().getValue().getHearingDocument())
            .isEqualTo(hearingNotice);
    }
}
