package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.handler;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.NoticeType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.LitigantSolicitorRemovedNocDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.handler.litigant.applicant.SolicitorRemovedApplicantLetterHandler;

@RunWith(MockitoJUnitRunner.class)
public class SolicitorRemovedApplicantLetterHandlerTest extends LetterHandlerTestBase {

    @InjectMocks
    SolicitorRemovedApplicantLetterHandler solicitorRemovedApplicantLetterHandler;

    public SolicitorRemovedApplicantLetterHandlerTest() {
        super(Mockito.mock(LitigantSolicitorRemovedNocDocumentService.class), NoticeType.REMOVE, DocumentHelper.PaperNotificationRecipient.APPLICANT);
    }

    @Test
    public void givenASolicitorHasBeenRemovedWithAnApplicantAddressLetterDocumentShouldBeSent() {
        shouldSendLetter("/fixtures/noticeOfChange/contested/noc/remove-with-solicitor-and-applicant-addresses-and-no-emails.json",
            "/fixtures/noticeOfChange/contested/noc/remove-with-solicitor-and-applicant-addresses-and-no-emails-before.json");

    }

    @Test
    public void givenASolicitorHasBeenRemovedWithNoApplicantAddressLetterDocumentShouldNotBeSent() {
        shouldNotSendLetter("/fixtures/noticeOfChange/contested/noc/remove-with-no-solicitor-address-and-with-applicant-addresses.json",
            "/fixtures/noticeOfChange/contested/noc/remove-with-no-solicitor-address-and-with-applicant-addresses-before.json");

    }

    public AbstractLetterHandler getLetterHandler() {
        return solicitorRemovedApplicantLetterHandler;
    }
}