package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.handler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.NoticeType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.SolicitorNocDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.handler.representative.SolicitorRemovedRepresentativeLetterHandler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SolicitorRemovedRepresentativeLetterHandlerTest extends LetterHandlerTestBase {

    @Mock
    CaseDataService caseDataService;

    @InjectMocks
    SolicitorRemovedRepresentativeLetterHandler solicitorRemovedRepresentativeLetterHandler;

    public SolicitorRemovedRepresentativeLetterHandlerTest() {
        super(Mockito.mock(SolicitorNocDocumentService.class), NoticeType.REMOVE, DocumentHelper.PaperNotificationRecipient.SOLICITOR);
    }

    @Before
    public void setUpTest() {
        when(caseDataService.isConsentedApplication(any())).thenReturn(Boolean.FALSE);
    }

    @Test
    public void givenAnApplicantSolicitorHasBeenRemovedWithAnAddressLetterDocumentShouldBeSent() {
        shouldSendLetter("/fixtures/noticeOfChange/contested/noc/remove-with-no-solicitor-address-and-with-applicant-addresses.json",
            "/fixtures/noticeOfChange/contested/noc/remove-with-no-solicitor-address-and-with-applicant-addresses-before.json");

    }

    @Test
    public void givenARespondentSolicitorHasBeenAddedWithAnEmailAddressLetterDocumentShouldNotBeSent() {
        shouldNotSendLetter("/fixtures/noticeOfChange/consented/add-respondent-solicitor-with-no-respondent-address-and-with-solicitor-email.json",
            "/fixtures/noticeOfChange/contested/noc/noc-letter-notifications-no-solicitor-email-no-applicant-address-before.json");

    }


    public AbstractLetterHandler getLetterHandler() {
        return solicitorRemovedRepresentativeLetterHandler;
    }

}