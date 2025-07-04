package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.defaultConsentedFinremCaseDetails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.APPLICANT;

@ExtendWith(MockitoExtension.class)
class HelpWithFeesDocumentServiceTest extends BaseServiceTest {

    @InjectMocks
    private HelpWithFeesDocumentService helpWithFeesDocumentService;

    @Mock
    private GenericDocumentService genericDocumentService;

    @Mock
    private DocumentConfiguration documentConfiguration;

    @Mock
    private DocumentHelper documentHelper;

    @BeforeEach
    void setUp() {
        when(documentConfiguration.getHelpWithFeesSuccessfulNotificationTemplate()).thenReturn("FL-FRM-LET-ENG-00096.docx");
        when(documentConfiguration.getHelpWithFeesSuccessfulNotificationFileName()).thenReturn("HelpWithFeesSuccessfulNotificationLetter.pdf");
    }

    @Test
    void shouldGenerateHwfSuccessfulNotificationLetterForApplicant() {
        // Arrange
        FinremCaseDetails finremCaseDetails = defaultConsentedFinremCaseDetails();
        CaseDetails caseDetailsForBulkPrint = CaseDetails.builder().build();
        CaseDocument expecetedCaseDocument = CaseDocument.builder().build();

        when(documentHelper.prepareLetterTemplateData(finremCaseDetails, APPLICANT)).thenReturn(caseDetailsForBulkPrint);
        when(genericDocumentService.generateDocument(AUTH_TOKEN, caseDetailsForBulkPrint,
            "FL-FRM-LET-ENG-00096.docx", "HelpWithFeesSuccessfulNotificationLetter.pdf"))
            .thenReturn(expecetedCaseDocument);

        // Act
        CaseDocument generatedHwfSuccessfulNotificationLetter =
            helpWithFeesDocumentService.generateHwfSuccessfulNotificationLetter(finremCaseDetails, AUTH_TOKEN, APPLICANT);

        // Assert
        assertThat(generatedHwfSuccessfulNotificationLetter).isEqualTo(expecetedCaseDocument);
    }
}
