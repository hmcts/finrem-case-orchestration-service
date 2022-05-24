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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.handler.litigant.respondent.SolicitorAddedRespondentLetterHandler;

@RunWith(MockitoJUnitRunner.class)
public class SolicitorAddedRespondentLetterHandlerTest extends LetterHandlerTestBase {

    @InjectMocks
    SolicitorAddedRespondentLetterHandler solicitorAddedRespondentLetterHandler;

    public SolicitorAddedRespondentLetterHandlerTest() {
        super(Mockito.mock(SolicitorAddedLetterDetailsGenerator.class), Mockito.mock(LitigantSolicitorAddedNocDocumentService.class), NoticeType.ADD, DocumentHelper.PaperNotificationRecipient.RESPONDENT);
    }

    @Test
    public void givenASolicitorHasBeenAddedWithARespondentAddressLetterDocumentShouldBeSent() {
        shouldSendLetter("/fixtures/noticeOfChange/consented/add-respondent-solicitor-with-sol-address-and-no-email.json",
            "/fixtures/noticeOfChange/consented/add-respondent-solicitor-with-sol-address-and-no-email-before.json");

    }

    @Test
    public void givenASolicitorHasBeenAddedWithNoRespondentAddressLetterDocumentShouldNotBeSent() {
        shouldNotSendLetter("/fixtures/noticeOfChange/consented/add-respondent-solicitor-with-no-respondent-address-and-no-email.json",
            "/fixtures/noticeOfChange/consented/add-respondent-solicitor-with-sol-address-and-no-email.json");

    }

    @Override
    public AbstractLetterHandler getLetterHandler() {
        return solicitorAddedRespondentLetterHandler;
    }
}
