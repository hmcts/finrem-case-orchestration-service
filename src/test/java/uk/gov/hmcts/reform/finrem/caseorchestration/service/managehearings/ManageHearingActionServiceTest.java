package uk.gov.hmcts.reform.finrem.caseorchestration.service.managehearings;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.express.ExpressCaseService;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
class ManageHearingActionServiceTest {

    @Mock
    private ManageHearingsDocumentService manageHearingsDocumentService;
    @Mock
    private ExpressCaseService expressCaseService;

    @InjectMocks
    private ManageHearingActionService manageHearingActionService;

    private FinremCaseDetails finremCaseDetails;
    private ManageHearingsWrapper hearingWrapper;
    private Hearing hearing;

    CaseDocument hearingNotice;
    CaseDocument formC;
    CaseDocument formG;
    Map<String, CaseDocument> pfdNcdrDocuments;
    CaseDocument outOfCourtResolution;

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

        hearingNotice = CaseDocument.builder()
            .documentFilename("HearingNotice.pdf")
            .documentUrl("http://example.com/hearing-notice")
            .build();

        when(manageHearingsDocumentService.generateHearingNotice(finremCaseDetails, AUTH_TOKEN))
            .thenReturn(hearingNotice);

    }

    @Test
    void shouldAddHearingAndGenerateHearingNotice() {

        // Act
        manageHearingActionService.performAddHearing(finremCaseDetails, AUTH_TOKEN);

        // Assert
        assertThat(hearingWrapper.getHearings()).hasSize(1);
        UUID hearingId = hearingWrapper.getWorkingHearingId();
        assertThat(hearingWrapper.getHearings().getFirst().getId()).isEqualTo(hearingId);
        assertThat(hearingWrapper.getHearings().getFirst().getValue()).isEqualTo(hearing);

        assertThat(hearingWrapper.getHearingDocumentsCollection()).hasSize(1);
        assertThat(hearingWrapper.getHearingDocumentsCollection().get(0).getValue().getHearingId())
            .isEqualTo(hearingId);
        assertThat(hearingWrapper.getHearingDocumentsCollection().get(0).getValue().getHearingDocument())
            .isEqualTo(hearingNotice);
    }

    @Test
    void shouldGenerateAllDocumentsForFdaHearingType() {
        hearing.setHearingType(HearingType.FDA);

        formC = CaseDocument.builder()
            .documentFilename("FormC.pdf")
            .documentUrl("http://example.com/form-c")
            .build();

        when(manageHearingsDocumentService.generateFormC(finremCaseDetails, AUTH_TOKEN))
            .thenReturn(formC);

        formG = CaseDocument.builder()
            .documentFilename("FormG.pdf")
            .documentUrl("http://example.com/form-g")
            .build();

        when(manageHearingsDocumentService.generateFormG(finremCaseDetails, AUTH_TOKEN))
            .thenReturn(formG);

        pfdNcdrDocuments = Map.of(
            "PFD_NCDR_COMPLIANCE_LETTER", CaseDocument.builder()
                .documentFilename("ComplianceLetter.pdf")
                .documentUrl("http://example.com/compliance-letter")
                .build(),
            "PFD_NCDR_COVER_LETTER", CaseDocument.builder()
                .documentFilename("CoverLetter.pdf")
                .documentUrl("http://example.com/cover-letter")
                .build()
        );

        when(manageHearingsDocumentService.generatePfdNcdrDocuments(finremCaseDetails, AUTH_TOKEN))
            .thenReturn(pfdNcdrDocuments);

        outOfCourtResolution = CaseDocument.builder()
            .documentFilename("OutOfCourtReolution.pdf")
            .documentUrl("http://example.com/OutOfCourtResolution")
            .build();

        when(manageHearingsDocumentService.generateOutOfCourtResolutionDoc(finremCaseDetails, AUTH_TOKEN))
            .thenReturn(outOfCourtResolution);

        manageHearingActionService.performAddHearing(finremCaseDetails, AUTH_TOKEN);

        assertThat(hearingWrapper.getHearingDocumentsCollection()).hasSize(6);
        assertThat(hearingWrapper.getHearingDocumentsCollection())
            .extracting(item ->
                item.getValue().getHearingDocument())
            .contains(hearingNotice, formC, formG,
                pfdNcdrDocuments.get("PFD_NCDR_COMPLIANCE_LETTER"),
                pfdNcdrDocuments.get("PFD_NCDR_COVER_LETTER"),
                outOfCourtResolution);

        // Verify that all other methods were called
        verify(manageHearingsDocumentService).generateHearingNotice(finremCaseDetails, AUTH_TOKEN);
        verify(manageHearingsDocumentService).generateFormC(finremCaseDetails, AUTH_TOKEN);
        verify(manageHearingsDocumentService).generateFormG(finremCaseDetails, AUTH_TOKEN);
        verify(manageHearingsDocumentService).generatePfdNcdrDocuments(finremCaseDetails, AUTH_TOKEN);
        verify(manageHearingsDocumentService).generateOutOfCourtResolutionDoc(finremCaseDetails, AUTH_TOKEN);
    }

    @Test
    void shouldGenerateDocumentsForFdrHearingTypeWithExpressCase() {
        hearing.setHearingType(HearingType.FDR);

        formC = CaseDocument.builder()
            .documentFilename("FormC.pdf")
            .documentUrl("http://example.com/form-c")
            .build();

        when(manageHearingsDocumentService.generateFormC(finremCaseDetails, AUTH_TOKEN))
            .thenReturn(formC);

        formG = CaseDocument.builder()
            .documentFilename("FormG.pdf")
            .documentUrl("http://example.com/form-g")
            .build();

        when(manageHearingsDocumentService.generateFormG(finremCaseDetails, AUTH_TOKEN))
            .thenReturn(formG);

        pfdNcdrDocuments = Map.of(
            "PFD_NCDR_COMPLIANCE_LETTER", CaseDocument.builder()
                .documentFilename("ComplianceLetter.pdf")
                .documentUrl("http://example.com/compliance-letter")
                .build(),
            "PFD_NCDR_COVER_LETTER", CaseDocument.builder()
                .documentFilename("CoverLetter.pdf")
                .documentUrl("http://example.com/cover-letter")
                .build()
        );

        when(manageHearingsDocumentService.generatePfdNcdrDocuments(finremCaseDetails, AUTH_TOKEN))
            .thenReturn(pfdNcdrDocuments);

        outOfCourtResolution = CaseDocument.builder()
            .documentFilename("OutOfCourtReolution.pdf")
            .documentUrl("http://example.com/OutOfCourtResolution")
            .build();

        when(manageHearingsDocumentService.generateOutOfCourtResolutionDoc(finremCaseDetails, AUTH_TOKEN))
            .thenReturn(outOfCourtResolution);

        when(expressCaseService.isExpressCase(finremCaseDetails.getData())).thenReturn(true);

        // Act
        manageHearingActionService.performAddHearing(finremCaseDetails, AUTH_TOKEN);

        // Assert
        assertThat(hearingWrapper.getHearingDocumentsCollection()).hasSize(6);
        assertThat(hearingWrapper.getHearingDocumentsCollection())
            .extracting(item ->
                item.getValue().getHearingDocument())
            .contains(hearingNotice, formC, formG,
                pfdNcdrDocuments.get("PFD_NCDR_COMPLIANCE_LETTER"),
                pfdNcdrDocuments.get("PFD_NCDR_COVER_LETTER"),
                outOfCourtResolution);

        // Verify that all other methods were called
        verify(manageHearingsDocumentService).generateHearingNotice(finremCaseDetails, AUTH_TOKEN);
        verify(manageHearingsDocumentService).generateFormC(finremCaseDetails, AUTH_TOKEN);
        verify(manageHearingsDocumentService).generateFormG(finremCaseDetails, AUTH_TOKEN);
        verify(manageHearingsDocumentService).generatePfdNcdrDocuments(finremCaseDetails, AUTH_TOKEN);
        verify(manageHearingsDocumentService).generateOutOfCourtResolutionDoc(finremCaseDetails, AUTH_TOKEN);
    }

    @Test
    void shouldNotGenerateFormGForFastTrackApplication() {
        hearing.setHearingType(HearingType.FDA);
        finremCaseDetails.getData().setFastTrackDecision(YesOrNo.YES);

        hearing.setHearingType(HearingType.FDR);

        formC = CaseDocument.builder()
            .documentFilename("FormC.pdf")
            .documentUrl("http://example.com/form-c")
            .build();

        when(manageHearingsDocumentService.generateFormC(finremCaseDetails, AUTH_TOKEN))
            .thenReturn(formC);

        pfdNcdrDocuments = Map.of(
            "PFD_NCDR_COMPLIANCE_LETTER", CaseDocument.builder()
                .documentFilename("ComplianceLetter.pdf")
                .documentUrl("http://example.com/compliance-letter")
                .build(),
            "PFD_NCDR_COVER_LETTER", CaseDocument.builder()
                .documentFilename("CoverLetter.pdf")
                .documentUrl("http://example.com/cover-letter")
                .build()
        );

        when(manageHearingsDocumentService.generatePfdNcdrDocuments(finremCaseDetails, AUTH_TOKEN))
            .thenReturn(pfdNcdrDocuments);

        outOfCourtResolution = CaseDocument.builder()
            .documentFilename("OutOfCourtReolution.pdf")
            .documentUrl("http://example.com/OutOfCourtResolution")
            .build();

        when(manageHearingsDocumentService.generateOutOfCourtResolutionDoc(finremCaseDetails, AUTH_TOKEN))
            .thenReturn(outOfCourtResolution);

        when(expressCaseService.isExpressCase(finremCaseDetails.getData())).thenReturn(true);

        // Act
        manageHearingActionService.performAddHearing(finremCaseDetails, AUTH_TOKEN);

        // Assert
        assertThat(hearingWrapper.getHearingDocumentsCollection()).hasSize(5);
        assertThat(hearingWrapper.getHearingDocumentsCollection())
            .extracting(item ->
                item.getValue().getHearingDocument())
            .contains(hearingNotice, formC,
                pfdNcdrDocuments.get("PFD_NCDR_COMPLIANCE_LETTER"),
                pfdNcdrDocuments.get("PFD_NCDR_COVER_LETTER"),
                outOfCourtResolution);

        // Verify that all other methods were called
        verify(manageHearingsDocumentService).generateHearingNotice(finremCaseDetails, AUTH_TOKEN);
        verify(manageHearingsDocumentService).generateFormC(finremCaseDetails, AUTH_TOKEN);
        verify(manageHearingsDocumentService).generatePfdNcdrDocuments(finremCaseDetails, AUTH_TOKEN);
        verify(manageHearingsDocumentService).generateOutOfCourtResolutionDoc(finremCaseDetails, AUTH_TOKEN);

        // Verify that generateFormC was not called
        verify(manageHearingsDocumentService, never()).generateFormG(finremCaseDetails, AUTH_TOKEN);
    }
}
