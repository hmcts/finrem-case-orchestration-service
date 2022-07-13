package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.ccd.domain.Address;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.YesOrNo;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.assertCaseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.defaultConsentedFinremCaseDetails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.newDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.APPLICANT;

@ActiveProfiles("test-mock-feign-clients")
public class HelpWithFeesDocumentServiceTest extends BaseServiceTest {

    @Autowired private HelpWithFeesDocumentService helpWithFeesDocumentService;
    @Autowired private DocumentClient documentClientMock;

    private FinremCaseDetails caseDetails;

    @Before
    public void setUp() {
        DocumentConfiguration config = new DocumentConfiguration();
        config.setHelpWithFeesSuccessfulNotificationTemplate("FL-FRM-LET-ENG-00096.docx");
        config.setHelpWithFeesSuccessfulNotificationFileName("HelpWithFeesSuccessfulNotificationLetter.pdf");

        caseDetails = defaultConsentedFinremCaseDetails();
    }

    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @Test
    public void shouldGenerateHwfSuccessfulNotificationLetterForApplicant() {

        when(documentClientMock.generatePdf(any(), anyString())).thenReturn(newDocument());

        Document generatedHwfSuccessfulNotificationLetter = helpWithFeesDocumentService.generateHwfSuccessfulNotificationLetter(
            caseDetails, AUTH_TOKEN, APPLICANT);

        assertCaseDocument(generatedHwfSuccessfulNotificationLetter);
        verify(documentClientMock).generatePdf(any(), anyString());
    }

    @Test
    public void shouldGenerateHwfSuccessfulNotificationLetterForApplicantSolicitor() {
        when(documentClientMock.generatePdf(any(), anyString())).thenReturn(newDocument());

        Address solicitorAddress = Address.builder()
            .addressLine1("123 Applicant Solicitor Street")
            .addressLine2("Second Address Line")
            .addressLine3("Third Address Line")
            .county("London")
            .country("England")
            .postTown("London")
            .postCode("SE1")
            .build();

        FinremCaseData caseData = caseDetails.getCaseData();
        caseData.getContactDetailsWrapper().setApplicantRepresented(YesOrNo.YES);
        caseData.getContactDetailsWrapper().setSolicitorName(TEST_SOLICITOR_NAME);
        caseData.getContactDetailsWrapper().setSolicitorReference(TEST_SOLICITOR_REFERENCE);
        caseData.getContactDetailsWrapper().setSolicitorAddress(solicitorAddress);

        Document generatedHwfSuccessfulNotificationLetter = helpWithFeesDocumentService
            .generateHwfSuccessfulNotificationLetter(caseDetails, AUTH_TOKEN, APPLICANT);

        assertCaseDocument(generatedHwfSuccessfulNotificationLetter);
    }
}