package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hearing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ListForHearingWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
class FinremFormCandGCorresponderTest {

    @Mock
    private BulkPrintService bulkPrintService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private DocumentHelper documentHelper;

    @Spy
    @InjectMocks
    private FinremFormCandGCorresponder underTest;

    private FinremCaseDetails caseDetails;

    @Mock
    private CaseType caseType;

    @Mock
    private CaseDocument pfdNcdrComplianceLetter;

    @Mock
    private CaseDocument pfdNcdrCoverLetter;

    @Mock
    private CaseDocument outOfFamilyCourtResolution;

    @Mock
    private CaseDocument formCDocument;

    @Mock
    private CaseDocument formGDocument;

    @Mock
    private CaseDocument additionalListOfHearingDocuments;

    @Mock
    private CaseDocument formACaseDocumentA;

    @Mock
    private CaseDocument miniFormADocument;

    @BeforeEach
    void setUp() {
        caseDetails = caseDetails();
        when(documentHelper.getFormADocumentsData(caseDetails.getData()))
            .thenReturn(List.of(formACaseDocumentA));
    }

    @Test
    void getDocumentsToPrint() {
        List<CaseDocument> documentsToPrint = underTest.getCaseDocuments(caseDetails);
        assertThat(documentsToPrint).containsExactlyInAnyOrder(
            formCDocument,
            formGDocument,
            outOfFamilyCourtResolution,
            additionalListOfHearingDocuments,
            pfdNcdrComplianceLetter,
            formACaseDocumentA,
            miniFormADocument
        );
    }

    @Test
    void getDocumentsToPrint_whenFormCIsNull_thenExcluded() {
        caseDetails.getData().getListForHearingWrapper().setFormC(null);

        List<CaseDocument> documentsToPrint = underTest.getCaseDocuments(caseDetails);
        assertThat(documentsToPrint).containsExactlyInAnyOrder(
            formGDocument,
            outOfFamilyCourtResolution,
            additionalListOfHearingDocuments,
            pfdNcdrComplianceLetter,
            formACaseDocumentA,
            miniFormADocument
        );
    }

    @Test
    void getDocumentsToPrint_whenFormGIsNull_thenExcluded() {
        caseDetails.getData().getListForHearingWrapper().setFormG(null);

        List<CaseDocument> documentsToPrint = underTest.getCaseDocuments(caseDetails);

        assertThat(documentsToPrint).containsExactlyInAnyOrder(
            formCDocument,
            outOfFamilyCourtResolution,
            additionalListOfHearingDocuments,
            pfdNcdrComplianceLetter,
            formACaseDocumentA,
            miniFormADocument
        );
    }

    @Test
    void getDocumentsToPrint_whenMiniFormAIsNull_thenExcluded() {
        caseDetails.getData().setMiniFormA(null);

        List<CaseDocument> documentsToPrint = underTest.getCaseDocuments(caseDetails);

        assertThat(documentsToPrint).containsExactlyInAnyOrder(
            formCDocument,
            formGDocument,
            outOfFamilyCourtResolution,
            additionalListOfHearingDocuments,
            pfdNcdrComplianceLetter,
            formACaseDocumentA
        );
    }

    @Test
    void getDocumentsToPrint_whenOutOfFamilyCourtResolutionIsNull_thenExcluded() {
        caseDetails.getData().setOutOfFamilyCourtResolution(null);

        List<CaseDocument> documentsToPrint = underTest.getCaseDocuments(caseDetails);

        assertThat(documentsToPrint).containsExactlyInAnyOrder(
            formCDocument,
            formGDocument,
            additionalListOfHearingDocuments,
            pfdNcdrComplianceLetter,
            formACaseDocumentA,
            miniFormADocument
        );
    }

    @Test
    void getDocumentsToPrint_whenAdditionalListOfHearingDocumentsIsNull_thenExcluded() {
        caseDetails.getData().getListForHearingWrapper().setAdditionalListOfHearingDocuments(null);

        List<CaseDocument> documentsToPrint = underTest.getCaseDocuments(caseDetails);

        assertThat(documentsToPrint).containsExactlyInAnyOrder(
            formCDocument,
            formGDocument,
            outOfFamilyCourtResolution,
            pfdNcdrComplianceLetter,
            formACaseDocumentA,
            miniFormADocument
        );
    }

    @Test
    void getDocumentsToPrint_whenPfdNcdrComplianceLetterIsNull_thenExcluded() {
        caseDetails.getData().getListForHearingWrapper().setPfdNcdrComplianceLetter(null);

        List<CaseDocument> documentsToPrint = underTest.getCaseDocuments(caseDetails);

        assertThat(documentsToPrint).containsExactlyInAnyOrder(
            formCDocument,
            formGDocument,
            outOfFamilyCourtResolution,
            additionalListOfHearingDocuments,
            formACaseDocumentA,
            miniFormADocument
        );
    }

    @Test
    void getDocumentsToPrint_whenAllOptionalDocumentsAreNull_thenOnlyFormADocumentsReturned() {
        caseDetails.getData().setMiniFormA(null);
        caseDetails.getData().setOutOfFamilyCourtResolution(null);
        caseDetails.getData().setListForHearingWrapper(ListForHearingWrapper.builder().build());

        List<CaseDocument> documentsToPrint = underTest.getCaseDocuments(caseDetails);

        assertThat(documentsToPrint).containsExactlyInAnyOrder(formACaseDocumentA);
    }

    @Test
    void getDocumentsToPrint_pfdNcdrCoverLetterIsNeverIncluded() {
        List<CaseDocument> documentsToPrint = underTest.getCaseDocuments(caseDetails);

        assertThat(documentsToPrint).doesNotContain(pfdNcdrCoverLetter);
    }

    @Test
    void shouldPrintApplicantDocuments() {
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);

        List<CaseDocument> documentsToPrint = mock(List.class);
        when(underTest.getCaseDocuments(caseDetails)).thenReturn(documentsToPrint);

        List<BulkPrintDocument> bulkPrintDocuments = mock(List.class);
        when(documentHelper.getCaseDocumentsAsBulkPrintDocuments(documentsToPrint,
            caseType, AUTH_TOKEN)).thenReturn(bulkPrintDocuments);

        underTest.sendCorrespondence(caseDetails, AUTH_TOKEN);

        verify(bulkPrintService).printApplicantDocuments(caseDetails, AUTH_TOKEN, bulkPrintDocuments);
    }

    private FinremCaseDetails caseDetails() {
        FinremCaseData caseData = FinremCaseData.builder()
            .miniFormA(miniFormADocument)
            .listForHearingWrapper(ListForHearingWrapper.builder()
                .additionalListOfHearingDocuments(additionalListOfHearingDocuments)
                .formC(formCDocument)
                .formG(formGDocument)
                .pfdNcdrComplianceLetter(pfdNcdrComplianceLetter)
                .pfdNcdrCoverLetter(pfdNcdrCoverLetter)
                .build())
            .fastTrackDecision(YesOrNo.forValue(NO_VALUE))
            .outOfFamilyCourtResolution(outOfFamilyCourtResolution)
            .build();
        return FinremCaseDetails.builder().caseType(caseType).data(caseData).build();
    }
}
