package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.LitigantSolicitorAddedNocDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.generators.SolicitorAddedLetterDetailsGenerator;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.handler.litigant.respondent.SolicitorAddedRespondentLetterHandler;

import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class SolicitorAddedRespondentLetterHandlerTest extends LetterHandlerTestBase {

    @InjectMocks
    SolicitorAddedRespondentLetterHandler solicitorAddedRespondentLetterHandler;

    public SolicitorAddedRespondentLetterHandlerTest() {
        super(mock(SolicitorAddedLetterDetailsGenerator.class), mock(LitigantSolicitorAddedNocDocumentService.class),
            DocumentHelper.PaperNotificationRecipient.RESPONDENT);
    }

    @Test
    void givenASolicitorHasBeenAddedWithARespondentAddressLetterDocumentShouldBeSent() {
        shouldSendLetter("/fixtures/noticeOfChange/consented/add-respondent-solicitor-with-sol-address-and-no-email.json",
            "/fixtures/noticeOfChange/consented/add-respondent-solicitor-with-sol-address-and-no-email-before.json");
    }

    @Test
    void givenASolicitorHasBeenAddedWithNoRespondentAddressLetterDocumentShouldNotBeSent() {
        shouldNotSendLetter("/fixtures/noticeOfChange/consented/add-respondent-solicitor-with-no-respondent-address-and-no-email.json",
            "/fixtures/noticeOfChange/consented/add-respondent-solicitor-with-sol-address-and-no-email.json");
    }

    @Override
    public AbstractLetterHandler getLetterHandler() {
        return solicitorAddedRespondentLetterHandler;
    }
}
