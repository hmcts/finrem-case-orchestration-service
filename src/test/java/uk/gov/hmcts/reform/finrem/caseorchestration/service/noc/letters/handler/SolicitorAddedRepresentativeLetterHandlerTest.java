package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.handler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestObjectMapperFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.NoticeType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.SolicitorNocDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.generators.SolicitorAddedLetterDetailsGenerator;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.handler.representative.SolicitorAddedRepresentativeLetterHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors.CheckApplicantSolicitorIsDigitalService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors.CheckRespondentSolicitorIsDigitalService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SolicitorAddedRepresentativeLetterHandlerTest extends LetterHandlerTestBase {

    @Mock
    CaseDataService caseDataService;

    @Mock
    CheckApplicantSolicitorIsDigitalService checkApplicantSolicitorIsDigitalService;

    @Mock
    CheckRespondentSolicitorIsDigitalService checkRespondentSolicitorIsDigitalService;

    SolicitorAddedRepresentativeLetterHandler solicitorAddedRepresentativeLetterHandler;

    public SolicitorAddedRepresentativeLetterHandlerTest() {
        super(Mockito.mock(SolicitorAddedLetterDetailsGenerator.class), Mockito.mock(SolicitorNocDocumentService.class), NoticeType.ADD,
            DocumentHelper.PaperNotificationRecipient.SOLICITOR);
    }

    @Before
    public void setUpTest() {
        solicitorAddedRepresentativeLetterHandler = new SolicitorAddedRepresentativeLetterHandler(
            (SolicitorAddedLetterDetailsGenerator) letterDetailsGenerator,
            (SolicitorNocDocumentService) nocDocumentService,
            bulkPrintServiceAdapter,
            caseDataService,
            checkApplicantSolicitorIsDigitalService,
            checkRespondentSolicitorIsDigitalService,
            TestObjectMapperFactory.createObjectMapper()
        );
        when(caseDataService.isConsentedApplication(any(CaseDetails.class))).thenReturn(Boolean.TRUE);
    }

    @Test
    public void givenARespondentSolicitorHasBeenAddedWithAnAddressLetterDocumentShouldBeSent() {
        shouldSendLetter("/fixtures/noticeOfChange/consented/add-respondent-solicitor-with-no-respondent-address-and-no-email.json",
            "/fixtures/noticeOfChange/consented/add-respondent-solicitor-with-no-respondent-address-and-no-email-before.json");

    }

    @Test
    public void givenARespondentSolicitorHasBeenAddedWithAnEmailAddressLetterDocumentShouldNotBeSent() {
        shouldNotSendLetter("/fixtures/noticeOfChange/consented/add-respondent-solicitor-with-no-respondent-address-and-with-solicitor-email.json",
            "/fixtures/noticeOfChange/contested/noc/noc-letter-notifications-no-solicitor-email-no-applicant-address-before.json");

    }

    public AbstractLetterHandler getLetterHandler() {
        return solicitorAddedRepresentativeLetterHandler;
    }

}
