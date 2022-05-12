package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdate;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.noc.NoticeOfChangeLetterDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.NoticeType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.LitigantSolicitorRemovedNocDocumentService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.NocLettersProcessor.COR_APPLICANT;

@RunWith(MockitoJUnitRunner.class)
public class NocSolicitorRemovedLettersProcessorTest extends NocLettersProcessorBaseTest {

    @Mock
    private LitigantSolicitorRemovedNocDocumentService litigantSolicitorRemovedNocDocumentService;

    @InjectMocks
    NocSolicitorRemovedLettersProcessor nocSolicitorRemovedLettersProcessor;

    public NocSolicitorRemovedLettersProcessorTest() {
        super(NoticeType.REMOVE);
    }

    @Test
    public void givenSolicitorRemovedAndSolicitorEmailProvidedAndApplicantEmailProvidedThenShouldNotGenerateSolicitorAndApplicantLetters() {

        CaseDetails caseDetails =
            getCaseDetails("/fixtures/noticeOfChange/contested/noc/remove-with-solicitor-and-applicant-emails.json");

        CaseDetails caseDetailsBefore =
            getCaseDetails("/fixtures/noticeOfChange/contested/noc/remove-with-solicitor-and-applicant-emails-before.json");

        RepresentationUpdate representationUpdate = RepresentationUpdate.builder().party(COR_APPLICANT).build();

        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(Boolean.FALSE);

        nocSolicitorRemovedLettersProcessor.processSolicitorAndLitigantLetters(caseDetails, caseDetailsBefore, AUTH_TOKEN, representationUpdate);

        verifyNoInteractions(litigantSolicitorRemovedNocDocumentService);
        verifyNoInteractions(solicitorNocDocumentService);
        verifyNoInteractions(bulkPrintService);
    }

    @Test
    public void shouldGenerateSolicitorLettersWhenSolicitorRemovedAndAddressIsPresentWithNoEmailAddress() {

        CaseDetails caseDetails = getCaseDetails(
            "/fixtures/noticeOfChange/contested/noc/remove-with-solicitor-and-applicant-addresses-and-no-emails.json");
        CaseDetails caseDetailsBefore = getCaseDetails(
            "/fixtures/noticeOfChange/contested/noc/remove-with-solicitor-and-applicant-addresses-and-no-emails-before.json");
        RepresentationUpdate representationUpdate = RepresentationUpdate.builder().party(COR_APPLICANT).build();

        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(Boolean.FALSE);

        NoticeOfChangeLetterDetails noticeOfChangeLetterDetailsApplicant =
            getNoticeOfChangeLetterDetails(caseDetails, caseDetailsBefore, representationUpdate, APPLICANT);

        NoticeOfChangeLetterDetails noticeOfChangeLetterDetailsSolicitor =
            getNoticeOfChangeLetterDetails(caseDetails, caseDetailsBefore, representationUpdate, SOLICITOR);

        final CaseDocument caseDocument = setUpCaseDocumentInteraction(noticeOfChangeLetterDetailsApplicant,
            litigantSolicitorRemovedNocDocumentService,
            "litSolRemovedDocFileName");
        setUpCaseDocumentInteraction(noticeOfChangeLetterDetailsSolicitor,
            solicitorNocDocumentService,
            "solRemovedDocFileName");

        nocSolicitorRemovedLettersProcessor.processSolicitorAndLitigantLetters(caseDetails, caseDetailsBefore, AUTH_TOKEN, representationUpdate);

        verify(litigantSolicitorRemovedNocDocumentService).generateNoticeOfChangeLetter(AUTH_TOKEN, noticeOfChangeLetterDetailsApplicant);
        verify(solicitorNocDocumentService).generateNoticeOfChangeLetter(AUTH_TOKEN, noticeOfChangeLetterDetailsSolicitor);
        ;
        verify(bulkPrintService).sendDocumentForPrint(caseDocument, caseDetails);

    }

    @Test
    public void shouldOnlyGenerateSolicitorLetterWhenRespondentEmailProvidedAndNoSolicitorEmailProvided() {

        CaseDetails caseDetails =
            getCaseDetails("/fixtures/noticeOfChange/consented/noc-letter-notifications-with-solicitor-no-respondent-email.json");

        CaseDetails caseDetailsBefore =
            getCaseDetails("/fixtures/noticeOfChange/consented/noc-letter-notifications-with-solicitor-no-respondent-email-before.json");
        RepresentationUpdate representationUpdate = RepresentationUpdate.builder().party(COR_RESPONDENT).build();

        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(Boolean.TRUE);

        NoticeOfChangeLetterDetails noticeOfChangeLetterDetailsRespondent =
            getNoticeOfChangeLetterDetails(caseDetails, caseDetailsBefore, representationUpdate, RESPONDENT);

        final CaseDocument caseDocument = setUpCaseDocumentInteraction(noticeOfChangeLetterDetailsRespondent,
            litigantSolicitorRemovedNocDocumentService,
            "litSolRemovedDocFileName");

        nocSolicitorRemovedLettersProcessor.processSolicitorAndLitigantLetters(caseDetails, caseDetailsBefore, AUTH_TOKEN, representationUpdate);

        verify(litigantSolicitorRemovedNocDocumentService).generateNoticeOfChangeLetter(AUTH_TOKEN, noticeOfChangeLetterDetailsRespondent);
        verify(bulkPrintService).sendDocumentForPrint(caseDocument, caseDetails);
        verifyNoInteractions(solicitorNocDocumentService);
    }

}