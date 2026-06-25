package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.LitigantSolicitorAddedNocDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.generators.SolicitorAddedLetterDetailsGenerator;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.handler.litigant.applicant.SolicitorAddedApplicantLetterHandler;

import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class SolicitorAddedApplicantLetterHandlerTest extends LetterHandlerTestBase {

    @InjectMocks
    SolicitorAddedApplicantLetterHandler solicitorAddedApplicantLetterHandler;

    public SolicitorAddedApplicantLetterHandlerTest() {
        super(mock(SolicitorAddedLetterDetailsGenerator.class), mock(LitigantSolicitorAddedNocDocumentService.class),
            DocumentHelper.PaperNotificationRecipient.APPLICANT);
    }

    @Test
    void givenASolicitorHasBeenAddedWithAnApplicantAddressLetterDocumentShouldBeSent() {
        shouldSendLetter("/fixtures/noticeOfChange/contested/noc/noc-letter-notifications-no-solicitor-email-applicant.json",
            "/fixtures/noticeOfChange/contested/noc/noc-letter-notifications-no-solicitor-email-applicant-before.json");
    }

    @Test
    void givenASolicitorHasBeenAddedWithNoApplicantAddressLetterDocumentShouldNotBeSent() {
        shouldNotSendLetter("/fixtures/noticeOfChange/contested/noc/noc-letter-notifications-no-solicitor-email-no-applicant-address.json",
            "/fixtures/noticeOfChange/contested/noc/noc-letter-notifications-no-solicitor-email-no-applicant-address-before.json");
    }

    public AbstractLetterHandler getLetterHandler() {
        return solicitorAddedApplicantLetterHandler;
    }

}
