package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.LitigantSolicitorRemovedNocDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.generators.SolicitorRemovedLetterDetailsGenerator;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.handler.litigant.respondent.SolicitorRemovedRespondentLetterHandler;

import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class SolicitorRemovedRespondentLetterHandlerTest extends LetterHandlerTestBase {

    @InjectMocks
    SolicitorRemovedRespondentLetterHandler solicitorRemovedRespondentLetterHandler;

    public SolicitorRemovedRespondentLetterHandlerTest() {
        super(mock(SolicitorRemovedLetterDetailsGenerator.class), mock(LitigantSolicitorRemovedNocDocumentService.class),
            DocumentHelper.PaperNotificationRecipient.RESPONDENT);
    }

    @Test
    void givenASolicitorHasBeenRemovedWithARespondentAddressLetterDocumentShouldBeSent() {
        shouldSendLetter("/fixtures/noticeOfChange/consented/remove-respondent-solicitor-with-sol-address-and-no-email.json",
            "/fixtures/noticeOfChange/consented/remove-respondent-solicitor-with-sol-address-and-no-email-before.json");
    }

    @Test
    void givenASolicitorHasBeenRemovedWithNoRespondentAddressLetterDocumentShouldNotBeSent() {
        shouldNotSendLetter("/fixtures/noticeOfChange/consented/add-respondent-solicitor-with-no-respondent-address-and-no-email.json",
            "/fixtures/noticeOfChange/consented/add-respondent-solicitor-with-sol-address-and-no-email.json");
    }

    @Override
    public AbstractLetterHandler getLetterHandler() {
        return solicitorRemovedRespondentLetterHandler;
    }
}
