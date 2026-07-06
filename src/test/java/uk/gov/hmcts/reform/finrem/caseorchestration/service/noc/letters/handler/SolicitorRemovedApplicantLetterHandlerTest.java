package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.LitigantSolicitorRemovedNocDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.generators.SolicitorRemovedLetterDetailsGenerator;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.handler.litigant.applicant.SolicitorRemovedApplicantLetterHandler;

import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class SolicitorRemovedApplicantLetterHandlerTest extends LetterHandlerTestBase {

    @InjectMocks
    SolicitorRemovedApplicantLetterHandler solicitorRemovedApplicantLetterHandler;

    public SolicitorRemovedApplicantLetterHandlerTest() {
        super(mock(SolicitorRemovedLetterDetailsGenerator.class), mock(LitigantSolicitorRemovedNocDocumentService.class),
            DocumentHelper.PaperNotificationRecipient.APPLICANT);
    }

    @Test
    void givenASolicitorHasBeenRemovedWithAnApplicantAddressLetterDocumentShouldBeSent() {
        shouldSendLetter("/fixtures/noticeOfChange/contested/noc/remove-with-solicitor-and-applicant-addresses-and-no-emails.json",
            "/fixtures/noticeOfChange/contested/noc/remove-with-solicitor-and-applicant-addresses-and-no-emails-before.json");
    }

    @Test
    void givenASolicitorHasBeenRemovedWithNoApplicantAddressLetterDocumentShouldNotBeSent() {
        shouldNotSendLetter("/fixtures/noticeOfChange/contested/noc/remove-with-no-solicitor-address-and-with-applicant-addresses.json",
            "/fixtures/noticeOfChange/contested/noc/remove-with-no-solicitor-address-and-with-applicant-addresses-before.json");
    }

    public AbstractLetterHandler getLetterHandler() {
        return solicitorRemovedApplicantLetterHandler;
    }
}
