package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.handler;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.NoticeType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.LitigantSolicitorAddedNocDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.generators.SolicitorAddedLetterDetailsGenerator;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.handler.litigant.applicant.SolicitorAddedApplicantLetterHandler;

@RunWith(MockitoJUnitRunner.class)
public class SolicitorAddedApplicantLetterHandlerTest extends LetterHandlerTestBase {

    @InjectMocks
    SolicitorAddedApplicantLetterHandler solicitorAddedApplicantLetterHandler;

    public SolicitorAddedApplicantLetterHandlerTest() {
        super(Mockito.mock(SolicitorAddedLetterDetailsGenerator.class), Mockito.mock(LitigantSolicitorAddedNocDocumentService.class), NoticeType.ADD,
            DocumentHelper.PaperNotificationRecipient.APPLICANT);
    }

    @Test
    public void givenASolicitorHasBeenAddedWithAnApplicantAddressLetterDocumentShouldBeSent() {
        shouldSendLetter("/fixtures/noticeOfChange/contested/noc/noc-letter-notifications-no-solicitor-email-applicant.json",
            "/fixtures/noticeOfChange/contested/noc/noc-letter-notifications-no-solicitor-email-applicant-before.json");

    }

    @Test
    public void givenASolicitorHasBeenAddedWithNoApplicantAddressLetterDocumentShouldNotBeSent() {
        shouldNotSendLetter("/fixtures/noticeOfChange/contested/noc/noc-letter-notifications-no-solicitor-email-no-applicant-address.json",
            "/fixtures/noticeOfChange/contested/noc/noc-letter-notifications-no-solicitor-email-no-applicant-address-before.json");

    }


    public AbstractLetterHandler getLetterHandler() {
        return solicitorAddedApplicantLetterHandler;
    }

}