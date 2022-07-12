package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.noc.NoticeOfChangeLetterDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.NoticeType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.NocDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.generators.AbstractLetterDetailsGenerator;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.RepresentationUpdate;

import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.finremCaseDetailsFromResource;

public abstract class LetterHandlerTestBase {

    protected static final String AUTH_TOKEN = "AUTH_TOKEN";
    protected static final String DOCUMENT_UUID = "dd153d4c-d2bd-11ec-9d64-0242ac120002";

    private final AbstractLetterDetailsGenerator letterDetailsGenerator;
    protected final NocDocumentService nocDocumentService;
    private final NoticeType noticeType;
    private final DocumentHelper.PaperNotificationRecipient recipient;

    @Mock
    BulkPrintService bulkPrintService;

    @Captor
    ArgumentCaptor<FinremCaseDetails> caseDetailsArgumentCaptor;
    @Captor
    ArgumentCaptor<FinremCaseDetails> caseDetailsBeforeArgumentCaptor;
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

    protected FinremCaseDetails getCaseDetails(String resourcePath) {
        return finremCaseDetailsFromResource(resourcePath, new ObjectMapper());
    }

    protected void shouldSendLetter(String caseDetailsPath, String caseDetailsBeforePath) {
        FinremCaseDetails caseDetails =
            getCaseDetails(caseDetailsPath);
        FinremCaseDetails caseDetailsBefore =
            getCaseDetails(
                caseDetailsBeforePath);

        NoticeOfChangeLetterDetails noticeOfChangeLetterDetails =
            setUpNoticeOfChangeLetterDetailsInteraction();

        Document caseDocumentApplicant =
            setUpCaseDocumentInteraction(noticeOfChangeLetterDetails,
                nocDocumentService, "appDocFileName");

        when(bulkPrintService.sendDocumentForPrint(caseDocumentApplicant, caseDetails)).thenReturn(UUID.fromString(DOCUMENT_UUID));

        getLetterHandler().handle(caseDetails, caseDetailsBefore, AUTH_TOKEN);

        assertThat(paperNotificationRecipientArgumentCaptor.getValue(), is(recipient));

        if (recipient != DocumentHelper.PaperNotificationRecipient.SOLICITOR) {
            assertThat(representationUpdateArgumentCaptor.getValue().getParty(),
                is(recipient == DocumentHelper.PaperNotificationRecipient.APPLICANT ? "Applicant" : "Respondent"));
        }

        verify(nocDocumentService).generateNoticeOfChangeLetter(AUTH_TOKEN, noticeOfChangeLetterDetails);
        verify(bulkPrintService).sendDocumentForPrint(caseDocumentApplicant, caseDetails);
    }


    protected void shouldNotSendLetter(String caseDetailsPath, String caseDetailsBeforePath) {
        FinremCaseDetails caseDetails =
            getCaseDetails(caseDetailsPath);
        FinremCaseDetails caseDetailsBefore =
            getCaseDetails(
                caseDetailsBeforePath);

        getLetterHandler().handle(caseDetails, caseDetailsBefore, AUTH_TOKEN);
        verifyNoInteractions(nocDocumentService);
        verifyNoInteractions(bulkPrintService);
    }

    protected Document setUpCaseDocumentInteraction(NoticeOfChangeLetterDetails noticeOfChangeLetterDetails,
                                                        NocDocumentService nocDocumentService, String docFileName) {
        Document caseDocument = Document.builder().filename(docFileName).build();
        when(nocDocumentService.generateNoticeOfChangeLetter(AUTH_TOKEN, noticeOfChangeLetterDetails)).thenReturn(caseDocument);
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
