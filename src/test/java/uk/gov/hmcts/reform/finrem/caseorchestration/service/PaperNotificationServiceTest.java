package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.generalapplication.service.RejectGeneralApplicationDocumentService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.RESPONDENT;

public class PaperNotificationServiceTest extends BaseServiceTest {

    @Autowired
    private PaperNotificationService paperNotificationService;

    @MockBean
    private AssignedToJudgeDocumentService assignedToJudgeDocumentService;
    @MockBean
    private BulkPrintService bulkPrintService;
    @MockBean
    private CaseDataService caseDataService;
    @MockBean
    private RejectGeneralApplicationDocumentService rejectGeneralApplicationDocumentService;

    @Test
    public void sendAssignToJudgeNotificationLetterIfIsPaperApplication() {
        when(caseDataService.isConsentedApplication(any(CaseDetails.class))).thenReturn(true);
        when(caseDataService.isPaperApplication(anyMap())).thenReturn(true);
        when(caseDataService.isRespondentRepresentedByASolicitor(any())).thenReturn(true);

        paperNotificationService.printAssignToJudgeNotification(buildCaseDetails(), AUTH_TOKEN);

        verify(assignedToJudgeDocumentService).generateAssignedToJudgeNotificationLetter(any(CaseDetails.class), eq(AUTH_TOKEN), eq(APPLICANT));
        verify(assignedToJudgeDocumentService).generateAssignedToJudgeNotificationLetter(any(CaseDetails.class), eq(AUTH_TOKEN), eq(RESPONDENT));
        verify(bulkPrintService, times(2)).sendDocumentForPrint(any(), any(CaseDetails.class), any(), any());
    }

    @Test
    public void shouldPrintForApplicantIfNotRepresented() {
        final String json
            = "/fixtures/refusal-order-contested.json";
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(json, mapper);
        caseDetails.getData().put("applicantRepresented", "No");
        caseDetails.getData().remove("applicantSolicitorConsentForEmails");
        caseDetails.getData().put("paperApplication", "No");

        assertThat(paperNotificationService.shouldPrintForApplicant(caseDetails), is(true));
    }

    @Test
    public void shouldPrintForApplicantIfRepresentedButNotAgreedToEmail() {
        final String json
            = "/fixtures/refusal-order-contested.json";
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(json, mapper);
        caseDetails.getData().put("applicantRepresented", "Yes");
        caseDetails.getData().put("applicantSolicitorConsentForEmails", "No");
        caseDetails.getData().put("paperApplication", "No");

        assertThat(paperNotificationService.shouldPrintForApplicant(caseDetails), is(true));
    }

    @Test
    public void shouldPrintForApplicantIfPaperCase() {
        final String json
            = "/fixtures/refusal-order-contested.json";
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(json, mapper);
        caseDetails.getData().put("paperApplication", "YES");

        assertThat(paperNotificationService.shouldPrintForApplicant(caseDetails), is(true));
    }

    @Test
    public void givenValidCaseData_whenPrintApplicantRejection_thenCallBulkPrintService() {
        final String json
            = "/fixtures/refusal-order-contested.json";
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(json, mapper);
        caseDetails.getData().put("paperApplication", "YES");
        CaseDocument caseDocument = CaseDocument.builder().documentFilename("general_application_rejected").build();

        when(rejectGeneralApplicationDocumentService.generateGeneralApplicationRejectionLetter(eq(caseDetails), any(), eq(APPLICANT)))
            .thenReturn(caseDocument);
        paperNotificationService.printApplicantRejectionGeneralApplication(caseDetails, AUTH_TOKEN);
        verify(bulkPrintService).sendDocumentForPrint(caseDocument, caseDetails, CCDConfigConstant.APPLICANT, AUTH_TOKEN);
    }

    @Test
    public void givenValidCaseData_whenPrintRespondentRejection_thenCallBulkPrintService() {
        final String json
            = "/fixtures/refusal-order-contested.json";
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(json, mapper);
        caseDetails.getData().put("paperApplication", "YES");
        CaseDocument caseDocument = CaseDocument.builder().documentFilename("general_application_rejected").build();

        when(rejectGeneralApplicationDocumentService.generateGeneralApplicationRejectionLetter(eq(caseDetails), any(), eq(RESPONDENT)))
            .thenReturn(caseDocument);
        paperNotificationService.printRespondentRejectionGeneralApplication(caseDetails, AUTH_TOKEN);
        verify(bulkPrintService).sendDocumentForPrint(caseDocument, caseDetails, CCDConfigConstant.RESPONDENT, AUTH_TOKEN);
    }
}
