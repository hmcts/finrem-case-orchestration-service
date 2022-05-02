package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RepresentationUpdate;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.noc.NoticeOfChangeLetterDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.NoticeType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.LitigantSolicitorAddedNocDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.SolicitorNocDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.generators.NocLetterDetailsGenerator;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDetailsFromResource;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.SOLICITOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.NocLettersProcessor.COR_APPLICANT;

@RunWith(MockitoJUnitRunner.class)
public class NocSolicitorAddedLettersProcessorTest {

    protected static final String AUTH_TOKEN = "authToken";
    protected static final String COR_RESPONDENT = "respondent";
    @Mock
    private LitigantSolicitorAddedNocDocumentService litigantSolicitorAddedNocLetterGenerator;
    @Mock
    private SolicitorNocDocumentService solicitorNocLetterGenerator;
    @Mock
    private NocLetterDetailsGenerator noticeOfChangeLetterDetailsGenerator;
    @Mock
    private CaseDataService caseDataService;

    @InjectMocks
    NocSolicitorAddedLettersProcessor noticeOfChangeLettersProcessor;

    @Test
    public void shouldGenerateSolicitorAndApplicantLettersWhenSolicitorAddedAndNoSolicicitorEmailProvided() {

        CaseDetails caseDetails =
            caseDetailsFromResource("/fixtures/noticeOfChange/contested/noc-letter-notifications-no-solicitor-email.json", new ObjectMapper());
        RepresentationUpdate representationUpdate = RepresentationUpdate.builder().party(COR_APPLICANT).build();

        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(Boolean.FALSE);

        NoticeOfChangeLetterDetails noticeOfChangeLetterDetailsApplicant = NoticeOfChangeLetterDetails.builder().build();
        when(noticeOfChangeLetterDetailsGenerator.generate(caseDetails, representationUpdate, APPLICANT,
            NoticeType.ADD)).thenReturn(noticeOfChangeLetterDetailsApplicant);

        NoticeOfChangeLetterDetails noticeOfChangeLetterDetailsSolicitor = NoticeOfChangeLetterDetails.builder().build();
        when(noticeOfChangeLetterDetailsGenerator.generate(caseDetails, representationUpdate, SOLICITOR,
            NoticeType.ADD)).thenReturn(noticeOfChangeLetterDetailsSolicitor);

        noticeOfChangeLettersProcessor.processSolicitorAndLitigantLetters(caseDetails, AUTH_TOKEN, representationUpdate);

        verify(litigantSolicitorAddedNocLetterGenerator).generateNoticeOfChangeLetter(AUTH_TOKEN, noticeOfChangeLetterDetailsApplicant);
        verify(solicitorNocLetterGenerator).generateNoticeOfChangeLetter(AUTH_TOKEN, noticeOfChangeLetterDetailsSolicitor);
    }


    @Test
    public void shouldOnlyGenerateApplicantLettersWhenSolicicitorEmailProvided() {

        CaseDetails caseDetails =
            caseDetailsFromResource("/fixtures/noticeOfChange/contested/noc-letter-notifications-with-solicitor-email.json", new ObjectMapper());
        RepresentationUpdate representationUpdate = RepresentationUpdate.builder().party(COR_APPLICANT).build();

        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(Boolean.FALSE);

        NoticeOfChangeLetterDetails noticeOfChangeLetterDetailsApplicant = NoticeOfChangeLetterDetails.builder().build();
        when(noticeOfChangeLetterDetailsGenerator.generate(caseDetails, representationUpdate, APPLICANT,
            NoticeType.ADD)).thenReturn(noticeOfChangeLetterDetailsApplicant);

        noticeOfChangeLettersProcessor.processSolicitorAndLitigantLetters(caseDetails, AUTH_TOKEN, representationUpdate);

        verify(litigantSolicitorAddedNocLetterGenerator).generateNoticeOfChangeLetter(AUTH_TOKEN, noticeOfChangeLetterDetailsApplicant);
        verifyNoInteractions(solicitorNocLetterGenerator);
    }

    @Test
    public void shouldOnlyGenerateSolicitorLetterWhenRespondentEmailProvidedAndNoSolicitorEmailProvided() {

        CaseDetails caseDetails =
            caseDetailsFromResource("/fixtures/noticeOfChange/consented/noc-letter-notifications-with-solicitor-no-respondent-email.json",
                new ObjectMapper());
        RepresentationUpdate representationUpdate = RepresentationUpdate.builder().party(COR_RESPONDENT).build();

        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(Boolean.TRUE);

        NoticeOfChangeLetterDetails noticeOfChangeLetterDetailsApplicant = NoticeOfChangeLetterDetails.builder().build();
        when(noticeOfChangeLetterDetailsGenerator.generate(caseDetails, representationUpdate, RESPONDENT,
            NoticeType.ADD)).thenReturn(noticeOfChangeLetterDetailsApplicant);

        noticeOfChangeLettersProcessor.processSolicitorAndLitigantLetters(caseDetails, AUTH_TOKEN, representationUpdate);

        verify(litigantSolicitorAddedNocLetterGenerator).generateNoticeOfChangeLetter(AUTH_TOKEN, noticeOfChangeLetterDetailsApplicant);
        verifyNoInteractions(solicitorNocLetterGenerator);
    }


}