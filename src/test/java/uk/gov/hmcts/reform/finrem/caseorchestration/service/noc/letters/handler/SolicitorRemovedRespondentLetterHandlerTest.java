package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.handler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestObjectMapperFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.NoticeType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.LitigantSolicitorRemovedNocDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.generators.SolicitorRemovedLetterDetailsGenerator;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.handler.litigant.respondent.SolicitorRemovedRespondentLetterHandler;

@RunWith(MockitoJUnitRunner.class)
public class SolicitorRemovedRespondentLetterHandlerTest extends LetterHandlerTestBase {

    SolicitorRemovedRespondentLetterHandler solicitorRemovedRespondentLetterHandler;

    public SolicitorRemovedRespondentLetterHandlerTest() {
        super(Mockito.mock(SolicitorRemovedLetterDetailsGenerator.class), Mockito.mock(LitigantSolicitorRemovedNocDocumentService.class),
            NoticeType.REMOVE,
            DocumentHelper.PaperNotificationRecipient.RESPONDENT);
    }

    @Before
    public void setUpTest() {
        solicitorRemovedRespondentLetterHandler = new SolicitorRemovedRespondentLetterHandler(
            (SolicitorRemovedLetterDetailsGenerator) letterDetailsGenerator,
            (LitigantSolicitorRemovedNocDocumentService) nocDocumentService,
            bulkPrintServiceAdapter,
            TestObjectMapperFactory.createObjectMapper()
        );
    }

    @Test
    public void givenASolicitorHasBeenRemovedWithARespondentAddressLetterDocumentShouldBeSent() {
        shouldSendLetter("/fixtures/noticeOfChange/consented/remove-respondent-solicitor-with-sol-address-and-no-email.json",
            "/fixtures/noticeOfChange/consented/remove-respondent-solicitor-with-sol-address-and-no-email-before.json");

    }

    @Test
    public void givenASolicitorHasBeenRemovedWithNoRespondentAddressLetterDocumentShouldNotBeSent() {
        shouldNotSendLetter("/fixtures/noticeOfChange/consented/add-respondent-solicitor-with-no-respondent-address-and-no-email.json",
            "/fixtures/noticeOfChange/consented/add-respondent-solicitor-with-sol-address-and-no-email.json");

    }

    @Override
    public AbstractLetterHandler getLetterHandler() {
        return solicitorRemovedRespondentLetterHandler;
    }
}
