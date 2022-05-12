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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.LitigantSolicitorAddedNocDocumentService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.NocLettersProcessor.COR_APPLICANT;


@RunWith(MockitoJUnitRunner.class)
public class NocSolicitorAddedLettersProcessorTest extends NocLettersProcessorBaseTest {

    @Mock
    private LitigantSolicitorAddedNocDocumentService litigantSolicitorAddedNocDocumentService;

    @InjectMocks
    NocSolicitorAddedLettersProcessor noticeOfChangeLettersProcessor;

    public NocSolicitorAddedLettersProcessorTest() {
        super(NoticeType.ADD);
    }

    @Test
    public void givenSolicitorAddedAndNoSolicitorEmailProvidedAndAddressesPopulatedGenerateSolicitorAndApplicantLetters() {

        CaseDetails caseDetails =
            getCaseDetails("/fixtures/noticeOfChange/contested/noc/noc-letter-notifications-no-solicitor-email-applicant.json");

        CaseDetails caseDetailsBefore =
            getCaseDetails("/fixtures/noticeOfChange/contested/noc/noc-letter-notifications-no-solicitor-email-applicant-before.json");

        RepresentationUpdate representationUpdate = RepresentationUpdate.builder().party(COR_APPLICANT).build();

        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(Boolean.FALSE);

        NoticeOfChangeLetterDetails noticeOfChangeLetterDetailsApplicant =
            getNoticeOfChangeLetterDetails(caseDetails,caseDetailsBefore, representationUpdate, APPLICANT);
        NoticeOfChangeLetterDetails noticeOfChangeLetterDetailsSolicitor =
            getNoticeOfChangeLetterDetails(caseDetails, caseDetailsBefore, representationUpdate, SOLICITOR);

        final CaseDocument caseDocumentSol =
            setUpCaseDocumentInteraction(noticeOfChangeLetterDetailsSolicitor, solicitorNocDocumentService, "solDocFileName");
        final CaseDocument caseDocumentApplicant =
            setUpCaseDocumentInteraction(noticeOfChangeLetterDetailsApplicant, litigantSolicitorAddedNocDocumentService, "appDocFileName");

        noticeOfChangeLettersProcessor.processSolicitorAndLitigantLetters(caseDetails, caseDetailsBefore, AUTH_TOKEN, representationUpdate);

        verify(litigantSolicitorAddedNocDocumentService).generateNoticeOfChangeLetter(AUTH_TOKEN, noticeOfChangeLetterDetailsApplicant);
        verify(solicitorNocDocumentService).generateNoticeOfChangeLetter(AUTH_TOKEN, noticeOfChangeLetterDetailsSolicitor);
        verify(bulkPrintService).sendDocumentForPrint(caseDocumentSol, caseDetails);
        verify(bulkPrintService).sendDocumentForPrint(caseDocumentApplicant, caseDetails);
    }


    @Test
    public void givenSolicitorAddedAndSolicitorEmailProvidedAndAddressesPopulatedShouldGenerateApplicantLettersOnly() {

        CaseDetails caseDetails = getCaseDetails(
            "/fixtures/noticeOfChange/contested/noc/add-with-solicitor-and-applicant-addresses-and-no-emails.json");
        RepresentationUpdate representationUpdate = RepresentationUpdate.builder().party(COR_APPLICANT).build();

        CaseDetails caseDetailsBefore = getCaseDetails(
            "/fixtures/noticeOfChange/contested/noc/add-with-solicitor-and-applicant-addresses-and-no-emails-before.json");


        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(Boolean.FALSE);

        NoticeOfChangeLetterDetails noticeOfChangeLetterDetailsApplicant =
            getNoticeOfChangeLetterDetails(caseDetails, caseDetailsBefore, representationUpdate, APPLICANT);

        final CaseDocument caseDocumentApplicant =
            setUpCaseDocumentInteraction(noticeOfChangeLetterDetailsApplicant, litigantSolicitorAddedNocDocumentService, "appDocFileName1");

        noticeOfChangeLettersProcessor.processSolicitorAndLitigantLetters(caseDetails, caseDetailsBefore, AUTH_TOKEN, representationUpdate);

        verify(litigantSolicitorAddedNocDocumentService).generateNoticeOfChangeLetter(AUTH_TOKEN, noticeOfChangeLetterDetailsApplicant);
        verifyNoInteractions(solicitorNocDocumentService);
        verify(bulkPrintService).sendDocumentForPrint(caseDocumentApplicant, caseDetails);
    }

    @Test
    public void givenSolicitorAddedAndSolicitorEmailNotProvidedWillGenerateSolicitorLetter() {

        CaseDetails caseDetails =
            getCaseDetails("/fixtures/noticeOfChange/consented/noc-letter-notifications-with-solicitor-no-respondent-email.json");

        CaseDetails caseDetailsBefore =
            getCaseDetails("/fixtures/noticeOfChange/consented/noc-letter-notifications-with-solicitor-no-respondent-email-before.json");

        RepresentationUpdate representationUpdate = RepresentationUpdate.builder().party(COR_RESPONDENT).build();

        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(Boolean.TRUE);

        NoticeOfChangeLetterDetails noticeOfChangeLetterDetailsRespondent =
            getNoticeOfChangeLetterDetails(caseDetails, caseDetailsBefore, representationUpdate, RESPONDENT);

        final CaseDocument caseDocumentRespondent =
            setUpCaseDocumentInteraction(noticeOfChangeLetterDetailsRespondent, litigantSolicitorAddedNocDocumentService, "respondentDocFileName");

        noticeOfChangeLettersProcessor.processSolicitorAndLitigantLetters(caseDetails, caseDetailsBefore, AUTH_TOKEN, representationUpdate);

        verify(litigantSolicitorAddedNocDocumentService).generateNoticeOfChangeLetter(AUTH_TOKEN, noticeOfChangeLetterDetailsRespondent);
        verifyNoInteractions(solicitorNocDocumentService);
        verify(bulkPrintService).sendDocumentForPrint(caseDocumentRespondent, caseDetails);
    }

    @Test
    public void givenSolicitorAddedAndNoApplicantAddressesPopulatedShouldGenerateSolicitoLettersOnly() {

        CaseDetails caseDetails =
            getCaseDetails("/fixtures/noticeOfChange/contested/noc/noc-letter-notifications-no-solicitor-email-no-applicant-address.json");

        CaseDetails caseDetailsBefore =
            getCaseDetails(
                "/fixtures/noticeOfChange/contested/noc/noc-letter-notifications-no-solicitor-email-no-applicant-address-before.json");

        RepresentationUpdate representationUpdate = RepresentationUpdate.builder().party(COR_APPLICANT).build();

        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(Boolean.FALSE);

        NoticeOfChangeLetterDetails noticeOfChangeLetterDetailsApplicant =
            getNoticeOfChangeLetterDetails(caseDetails, caseDetailsBefore, representationUpdate, APPLICANT);
        NoticeOfChangeLetterDetails noticeOfChangeLetterDetailsSolicitor =
            getNoticeOfChangeLetterDetails(caseDetails, caseDetailsBefore, representationUpdate, SOLICITOR);

        final CaseDocument caseDocumentSol =
            setUpCaseDocumentInteraction(noticeOfChangeLetterDetailsSolicitor, solicitorNocDocumentService, "solDocFileName");
        final CaseDocument caseDocumentApplicant =
            setUpCaseDocumentInteraction(noticeOfChangeLetterDetailsApplicant, litigantSolicitorAddedNocDocumentService, "appDocFileName");

        noticeOfChangeLettersProcessor.processSolicitorAndLitigantLetters(caseDetails, caseDetailsBefore,  AUTH_TOKEN, representationUpdate);

        verifyNoInteractions(litigantSolicitorAddedNocDocumentService);
        verify(solicitorNocDocumentService).generateNoticeOfChangeLetter(AUTH_TOKEN, noticeOfChangeLetterDetailsSolicitor);
        verify(bulkPrintService).sendDocumentForPrint(caseDocumentSol, caseDetails);
        verifyNoMoreInteractions(litigantSolicitorAddedNocDocumentService);
    }

}