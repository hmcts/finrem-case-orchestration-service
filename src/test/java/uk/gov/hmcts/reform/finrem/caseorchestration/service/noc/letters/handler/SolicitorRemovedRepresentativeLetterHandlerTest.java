package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.handler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestObjectMapperFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.SolicitorNocDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.documents.generators.SolicitorRemovedLetterDetailsGenerator;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters.handler.representative.SolicitorRemovedRepresentativeLetterHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors.CheckApplicantSolicitorIsDigitalService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors.CheckRespondentSolicitorIsDigitalService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SolicitorRemovedRepresentativeLetterHandlerTest extends LetterHandlerTestBase {

    @Mock
    CaseDataService caseDataService;

    @Mock
    CheckApplicantSolicitorIsDigitalService checkApplicantSolicitorIsDigitalService;

    @Mock
    CheckRespondentSolicitorIsDigitalService checkRespondentSolicitorIsDigitalService;

    SolicitorRemovedRepresentativeLetterHandler solicitorRemovedRepresentativeLetterHandler;

    public SolicitorRemovedRepresentativeLetterHandlerTest() {
        super(mock(SolicitorRemovedLetterDetailsGenerator.class), mock(SolicitorNocDocumentService.class),
            DocumentHelper.PaperNotificationRecipient.SOLICITOR);
    }

    @Before
    public void setUpTest() {
        solicitorRemovedRepresentativeLetterHandler = new SolicitorRemovedRepresentativeLetterHandler(
            (SolicitorRemovedLetterDetailsGenerator) letterDetailsGenerator,
            (SolicitorNocDocumentService) nocDocumentService,
            bulkPrintServiceAdapter,
            caseDataService,
            checkApplicantSolicitorIsDigitalService,
            checkRespondentSolicitorIsDigitalService,
            TestObjectMapperFactory.createObjectMapper()
        );
        when(caseDataService.isConsentedApplication(any(CaseDetails.class))).thenReturn(Boolean.FALSE);
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