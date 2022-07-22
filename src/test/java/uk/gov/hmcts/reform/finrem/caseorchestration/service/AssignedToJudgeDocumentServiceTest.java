package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.LetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentGenerationRequest;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.assertCaseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.defaultConsentedFinremCaseDetails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.defaultContestedFinremCaseDetails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.newDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.APPLICANT;

@ActiveProfiles("test-mock-feign-clients")
public class AssignedToJudgeDocumentServiceTest extends BaseServiceTest {

    @Autowired private AssignedToJudgeDocumentService assignedToJudgeDocumentService;
    @Autowired private DocumentConfiguration documentConfiguration;
    @MockBean private GenericDocumentService genericDocumentServiceMock;
    @MockBean(name = "letterDetailsMapper") private LetterDetailsMapper letterDetailsMapper;


    private FinremCaseDetails caseDetails;

    @Captor
    private ArgumentCaptor<DocumentGenerationRequest> documentGenerationRequestCaptor;

    @Test
    public void givenValidDataConsented_whenGenerateAssignedToJudgeNotification_thenGenerateDocument() {
        caseDetails = defaultConsentedFinremCaseDetails();

        when(genericDocumentServiceMock.generateDocumentFromPlaceholdersMap(any(), any(),
            eq(documentConfiguration.getAssignedToJudgeNotificationTemplate()),
            eq(documentConfiguration.getAssignedToJudgeNotificationFileName())))
            .thenReturn(newDocument());

        Document generateAssignedToJudgeNotificationLetter
            = assignedToJudgeDocumentService.generateAssignedToJudgeNotificationLetter(caseDetails, AUTH_TOKEN, APPLICANT);

        assertCaseDocument(generateAssignedToJudgeNotificationLetter);
        verify(genericDocumentServiceMock).generateDocumentFromPlaceholdersMap(any(), any(),
            eq(documentConfiguration.getAssignedToJudgeNotificationTemplate()),
            eq(documentConfiguration.getAssignedToJudgeNotificationFileName()));
    }

    @Test
    public void givenValidDataConsentInContested_whenGenerateConsentInContestedAssignedToJudgeNotificationLetter_thenGenerateDocument() {
        caseDetails = defaultContestedFinremCaseDetails();

        when(genericDocumentServiceMock.generateDocumentFromPlaceholdersMap(any(), any(),
            eq(documentConfiguration.getConsentInContestedAssignedToJudgeNotificationTemplate()),
            eq(documentConfiguration.getConsentInContestedAssignedToJudgeNotificationFileName())))
            .thenReturn(newDocument());

        Document generateAssignedToJudgeNotificationLetter
            = assignedToJudgeDocumentService.generateConsentInContestedAssignedToJudgeNotificationLetter(caseDetails, AUTH_TOKEN, APPLICANT);

        assertCaseDocument(generateAssignedToJudgeNotificationLetter);
        verify(genericDocumentServiceMock).generateDocumentFromPlaceholdersMap(any(), any(),
            eq(documentConfiguration.getConsentInContestedAssignedToJudgeNotificationTemplate()),
            eq(documentConfiguration.getConsentInContestedAssignedToJudgeNotificationFileName()));
    }
}
