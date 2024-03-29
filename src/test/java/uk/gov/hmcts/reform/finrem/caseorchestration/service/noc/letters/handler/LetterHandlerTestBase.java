package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdate;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.noc.NoticeOfChangeLetterDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.NoticeType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.NocDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.generators.AbstractLetterDetailsGenerator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDetailsFromResource;

public abstract class LetterHandlerTestBase {

    protected static final String AUTH_TOKEN = "AUTH_TOKEN";
    public static final String CASE_ID = "1234";

    private AbstractLetterDetailsGenerator letterDetailsGenerator;
    protected final NocDocumentService nocDocumentService;
    private final NoticeType noticeType;
    private final DocumentHelper.PaperNotificationRecipient recipient;

    @Mock
    BulkPrintService bulkPrintService;

    @Captor
    ArgumentCaptor<CaseDetails> caseDetailsArgumentCaptor;
    @Captor
    ArgumentCaptor<CaseDetails> caseDetailsBeforeArgumentCaptor;
    @Captor
    ArgumentCaptor<RepresentationUpdate> representationUpdateArgumentCaptor;
    @Captor
    ArgumentCaptor<DocumentHelper.PaperNotificationRecipient> paperNotificationRecipientArgumentCaptor;

    public LetterHandlerTestBase(AbstractLetterDetailsGenerator letterDetailsGenerator, NocDocumentService nocDocumentService, NoticeType noticeType,
                                 DocumentHelper.PaperNotificationRecipient recipient) {
        this.letterDetailsGenerator = letterDetailsGenerator;
        this.nocDocumentService = nocDocumentService;
        this.noticeType = noticeType;
        this.recipient = recipient;
    }

    protected CaseDetails getCaseDetails(String resourcePath) {
        CaseDetails caseDetails =
            caseDetailsFromResource(resourcePath, new ObjectMapper());
        return caseDetails;
    }

    protected void shouldSendLetter(String caseDetailsPath, String caseDetailsBeforePath) {
        CaseDetails caseDetails =
            getCaseDetails(caseDetailsPath);
        CaseDetails caseDetailsBefore =
            getCaseDetails(
                caseDetailsBeforePath);

        NoticeOfChangeLetterDetails noticeOfChangeLetterDetails =
            setUpNoticeOfChangeLetterDetailsInteraction();

        CaseDocument caseDocumentApplicant =
            setUpCaseDocumentInteraction(noticeOfChangeLetterDetails,
                nocDocumentService, "appDocFileName");

        Assert.assertNotNull(caseDocumentApplicant);
        getLetterHandler().handle(caseDetails, caseDetailsBefore, AUTH_TOKEN);

        assertThat(paperNotificationRecipientArgumentCaptor.getValue(), is(recipient));

        if (recipient != DocumentHelper.PaperNotificationRecipient.SOLICITOR) {
            assertThat(representationUpdateArgumentCaptor.getValue().getParty(),
                is(recipient == DocumentHelper.PaperNotificationRecipient.APPLICANT ? "Applicant" : "Respondent"));
        }

        verify(nocDocumentService).generateNoticeOfChangeLetter(AUTH_TOKEN, noticeOfChangeLetterDetails, CASE_ID);
        verify(bulkPrintService).sendDocumentForPrint(caseDocumentApplicant, caseDetails,
            bulkPrintService.getRecipient(recipient.toString()), AUTH_TOKEN);
    }


    protected void shouldNotSendLetter(String caseDetailsPath, String caseDetailsBeforePath) {
        CaseDetails caseDetails =
            getCaseDetails(caseDetailsPath);
        CaseDetails caseDetailsBefore =
            getCaseDetails(
                caseDetailsBeforePath);

        getLetterHandler().handle(caseDetails, caseDetailsBefore, AUTH_TOKEN);
        verifyNoInteractions(nocDocumentService);
        verifyNoInteractions(bulkPrintService);
    }

    protected CaseDocument setUpCaseDocumentInteraction(NoticeOfChangeLetterDetails noticeOfChangeLetterDetails,
                                                        NocDocumentService nocDocumentService, String docFileName) {
        CaseDocument caseDocument = CaseDocument.builder().documentFilename(docFileName).build();
        when(nocDocumentService.generateNoticeOfChangeLetter(AUTH_TOKEN, noticeOfChangeLetterDetails, CASE_ID)).thenReturn(caseDocument);
        return caseDocument;
    }

    protected NoticeOfChangeLetterDetails setUpNoticeOfChangeLetterDetailsInteraction() {
        NoticeOfChangeLetterDetails noticeOfChangeLetterDetails = NoticeOfChangeLetterDetails.builder().build();
        when(letterDetailsGenerator.generate(caseDetailsArgumentCaptor.capture(), caseDetailsBeforeArgumentCaptor.capture(),
            representationUpdateArgumentCaptor.capture(), paperNotificationRecipientArgumentCaptor.capture())).thenReturn(
            noticeOfChangeLetterDetails);
        return noticeOfChangeLetterDetails;
    }

    public abstract AbstractLetterHandler getLetterHandler();
}
