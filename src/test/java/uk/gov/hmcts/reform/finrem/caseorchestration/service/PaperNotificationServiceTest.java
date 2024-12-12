package uk.gov.hmcts.reform.finrem.caseorchestration.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOne;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.generalapplication.service.RejectGeneralApplicationDocumentService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.INTERVENER_ONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper.PaperNotificationRecipient.RESPONDENT;

@ExtendWith(MockitoExtension.class)
class PaperNotificationServiceTest {

    @InjectMocks
    private PaperNotificationService paperNotificationService;

    @Mock
    private AssignedToJudgeDocumentService assignedToJudgeDocumentService;
    @Mock
    private BulkPrintService bulkPrintService;
    @Mock
    private CaseDataService caseDataService;
    @Mock
    private RejectGeneralApplicationDocumentService rejectGeneralApplicationDocumentService;

    private ObjectMapper mapper;

    @BeforeEach
    void setup() {
        mapper = new ObjectMapper();
    }

    @Test
    void sendAssignToJudgeNotificationLetterIfIsPaperApplication() {
        when(caseDataService.isPaperApplication(anyMap())).thenReturn(true);
        when(caseDataService.isRespondentRepresentedByASolicitor(anyMap())).thenReturn(true);

        paperNotificationService.printAssignToJudgeNotification(buildCaseDetails(), AUTH_TOKEN);

        verify(assignedToJudgeDocumentService).generateAssignedToJudgeNotificationLetter(any(CaseDetails.class), eq(AUTH_TOKEN), eq(APPLICANT));
        verify(assignedToJudgeDocumentService).generateAssignedToJudgeNotificationLetter(any(CaseDetails.class), eq(AUTH_TOKEN), eq(RESPONDENT));
        verify(bulkPrintService, times(2)).sendDocumentForPrint(any(), any(CaseDetails.class), any(), any());
    }

    @Test
    void shouldPrintForApplicantIfNotRepresented() {
        final String json
            = "/fixtures/refusal-order-contested.json";
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(json, mapper);
        caseDetails.getData().put("applicantRepresented", "No");
        caseDetails.getData().remove("applicantSolicitorConsentForEmails");
        caseDetails.getData().put("paperApplication", "No");

        assertThat(paperNotificationService.shouldPrintForApplicant(caseDetails), is(true));
    }

    @Test
    void shouldPrintForApplicantIfRepresentedButNotAgreedToEmail() {
        final String json
            = "/fixtures/refusal-order-contested.json";
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(json, mapper);
        caseDetails.getData().put("applicantRepresented", "Yes");
        caseDetails.getData().put("applicantSolicitorConsentForEmails", "No");
        caseDetails.getData().put("paperApplication", "No");

        assertThat(paperNotificationService.shouldPrintForApplicant(caseDetails), is(true));
    }

    @Test
    void shouldPrintForApplicantIfPaperCase() {
        final String json
            = "/fixtures/refusal-order-contested.json";
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(json, mapper);
        caseDetails.getData().put("paperApplication", "YES");

        assertThat(paperNotificationService.shouldPrintForApplicant(caseDetails), is(true));
    }

    @Test
    void givenValidCaseData_whenPrintApplicantRejection_thenCallBulkPrintService() {
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
    void givenValidCaseData_whenPrintRespondentRejection_thenCallBulkPrintService() {
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

    @Test
    void givenValidCaseData_whenPrintIntervenerRejection_thenCallBulkPrintService() {
        final String json
            = "/fixtures/refusal-order-contested.json";
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(json, mapper);
        caseDetails.getData().put("paperApplication", "YES");
        CaseDocument caseDocument = CaseDocument.builder().documentFilename("general_application_rejected").build();
        IntervenerOne intervenerWrapper = IntervenerOne.builder().build();
        when(rejectGeneralApplicationDocumentService.generateGeneralApplicationRejectionLetter(eq(caseDetails), any(), eq(INTERVENER_ONE)))
            .thenReturn(caseDocument);
        paperNotificationService.printIntervenerRejectionGeneralApplication(caseDetails, intervenerWrapper, AUTH_TOKEN);
        verify(bulkPrintService).sendDocumentForPrint(caseDocument, caseDetails,
            intervenerWrapper.getIntervenerType().getTypeValue(), AUTH_TOKEN);
    }

    protected CaseDetails buildCaseDetails() {
        Map<String, Object> caseData = new HashMap<>();
        List<String> natureOfApplication = List.of("Lump Sum Order",
            "Periodical Payment Order",
            "Pension Sharing Order",
            "Pension Attachment Order",
            "Pension Compensation Sharing Order",
            "Pension Compensation Attachment Order",
            "A settlement or a transfer of property",
            "Property Adjustment Order");
        caseData.put("natureOfApplication2", natureOfApplication);
        return CaseDetails.builder().id(123L).caseTypeId(CaseType.CONSENTED.getCcdType()).data(caseData).build();
    }
}
