package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hearing;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.address.LetterAddresseeGeneratorMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PaymentDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PaymentDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PaymentDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ListForHearingWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;

@ExtendWith(MockitoExtension.class)
class FinremFormCandGCorresponderTest extends FinremHearingCorrespondenceBaseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final FinremCaseDetailsMapper finremCaseDetailsMapper = new FinremCaseDetailsMapper(objectMapper);

    @Mock
    private GenericDocumentService genericDocumentService;
    @Mock
    private LetterAddresseeGeneratorMapper letterAddresseeGenerator;
    @Mock
    private InternationalPostalService postalService;

    @Captor
    private ArgumentCaptor<List<BulkPrintDocument>> applicantBulkPrintDocumentsCaptor;
    @Captor
    private ArgumentCaptor<List<BulkPrintDocument>> respondentBulkPrintDocumentsCaptor;

    private static final String DATE_OF_HEARING = "2019-01-01";

    @BeforeEach
    public void setUp() {
        caseDetails = caseDetails();
        applicantAndRespondentMultiLetterCorresponder =
            new FinremFormCandGCorresponder(bulkPrintService, notificationService,
                new DocumentHelper(objectMapper, new CaseDataService(objectMapper), genericDocumentService, finremCaseDetailsMapper,
                    letterAddresseeGenerator, postalService));
    }

    @Test
    void getDocumentsToPrint() {
        List<CaseDocument> documentsToPrint = applicantAndRespondentMultiLetterCorresponder.getCaseDocuments(caseDetails);
        assertEquals(6, documentsToPrint.size());
    }

    @Test
    void shouldSendPfdNcdrDocuments() {
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);

        applicantAndRespondentMultiLetterCorresponder.sendCorrespondence(caseDetails, AUTH_TOKEN);

        // Verify applicant receives the compliance letter but not the cover sheet
        verify(bulkPrintService).printApplicantDocuments(eq(caseDetails), eq(AUTH_TOKEN),
            applicantBulkPrintDocumentsCaptor.capture());
        List<BulkPrintDocument> applicantDocuments = applicantBulkPrintDocumentsCaptor.getValue();
        assertEquals(6, applicantDocuments.size());
        assertTrue(applicantDocuments.stream().anyMatch(doc -> doc.getFileName().equals("pfd-ncdr-compliance-letter")
            && doc.getBinaryFileUrl().equals("http://localhost/compliance/binary")));
        assertFalse(applicantDocuments.stream().anyMatch(doc -> doc.getFileName().equals("pfd-ncdr-cover-letter")
            && doc.getBinaryFileUrl().equals("http://localhost/cover/binary")));
        verify(notificationService, never()).sendPrepareForHearingEmailApplicant(caseDetails);

        // Verify respondent receives both the compliance letter and cover sheet
        verify(bulkPrintService).printRespondentDocuments(eq(caseDetails), eq(AUTH_TOKEN),
            respondentBulkPrintDocumentsCaptor.capture());
        List<BulkPrintDocument> respondentDocuments = respondentBulkPrintDocumentsCaptor.getValue();
        assertEquals(7, respondentDocuments.size());

        assertTrue(respondentDocuments.stream().anyMatch(doc -> doc.getFileName().equals("pfd-ncdr-compliance-letter")
            && doc.getBinaryFileUrl().equals("http://localhost/compliance/binary")));
        assertTrue(respondentDocuments.stream().anyMatch(doc -> doc.getFileName().equals("pfd-ncdr-cover-letter")
            && doc.getBinaryFileUrl().equals("http://localhost/cover/binary")));
        verify(notificationService, never()).sendPrepareForHearingEmailRespondent(caseDetails);
    }

    private FinremCaseDetails caseDetails() {
        FinremCaseData caseData = FinremCaseData.builder()
            .listForHearingWrapper(ListForHearingWrapper.builder()
                .hearingDate(LocalDate.parse(DATE_OF_HEARING))
                .additionalListOfHearingDocuments(caseDocument())
                .formC(caseDocument())
                .formG(caseDocument())
                .pfdNcdrComplianceLetter(caseDocument("http://localhost/compliance", "pfd-ncdr-compliance-letter",
                    "http://localhost/compliance/binary"))
                .pfdNcdrCoverLetter(caseDocument("http://localhost/cover", "pfd-ncdr-cover-letter",
                    "http://localhost/cover/binary"))
                .build())
            .fastTrackDecision(YesOrNo.forValue(NO_VALUE))
            .copyOfPaperFormA(List.of(
                PaymentDocumentCollection.builder()
                    .value(PaymentDocument.builder()
                        .typeOfDocument(PaymentDocumentType.COPY_OF_PAPER_FORM_A)
                        .uploadedDocument(caseDocument())
                        .build())
                    .build()))
            .outOfFamilyCourtResolution(caseDocument())
            .build();
        return FinremCaseDetails.builder().id(1234L).data(caseData).build();
    }
}
