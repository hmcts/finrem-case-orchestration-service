package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOne;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.generalapplication.service.RejectGeneralApplicationDocumentService;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
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

    @Mock
    FinremCaseDetailsMapper finremCaseDetailsMapper;

    @BeforeEach
    void setup() {
        mapper = JsonMapper
            .builder()
            .addModule(new JavaTimeModule())
            .addModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES))
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

        finremCaseDetailsMapper =  new FinremCaseDetailsMapper(mapper);
    }

    @Test
    void sendAssignToJudgeNotificationLetterIfIsPaperApplication() {
        when(caseDataService.isPaperApplication(anyMap())).thenReturn(true);
        when(caseDataService.isRespondentRepresentedByASolicitor(anyMap())).thenReturn(true);

        paperNotificationService.printAssignToJudgeNotification(
            CaseDetails.builder().id(123L).caseTypeId(CaseType.CONSENTED.getCcdType()).data(Map.of()).build(), AUTH_TOKEN);

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

        assertTrue(paperNotificationService.shouldPrintForApplicant(caseDetails));
        assertTrue(paperNotificationService.shouldPrintForApplicant(finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails)));
    }

    @Test
    void shouldPrintForApplicantIfRepresentedButNotAgreedToEmail() {
        final String json
            = "/fixtures/refusal-order-contested.json";
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(json, mapper);
        caseDetails.getData().put("applicantRepresented", "Yes");
        caseDetails.getData().put("applicantSolicitorConsentForEmails", "No");
        caseDetails.getData().put("paperApplication", "No");

        assertTrue(paperNotificationService.shouldPrintForApplicant(caseDetails));
        assertTrue(paperNotificationService.shouldPrintForApplicant(finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails)));
    }

    @Test
    void shouldPrintForApplicantIfPaperCase() {
        final String json
            = "/fixtures/refusal-order-contested.json";
        CaseDetails caseDetails = TestSetUpUtils.caseDetailsFromResource(json, mapper);
        caseDetails.getData().put("paperApplication", "Yes");

        assertTrue(paperNotificationService.shouldPrintForApplicant(caseDetails));
        assertTrue(paperNotificationService.shouldPrintForApplicant(finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails)));
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

    @ParameterizedTest
    @CsvSource({
        "true, Yes, false",  // Respondent represented, consent given, should not print
        "true, No, true",   // Respondent represented, no consent, should print
        "false, Yes, true", // Respondent not represented, should print regardless of consent
        "false, No, true"   // Respondent not represented, should print regardless of consent
    })
    void testShouldPrintForRespondent(boolean isRespRepresented, String respSolConsent, boolean expected) {
        // Arrange
        FinremCaseDetails caseDetails = mock(FinremCaseDetails.class);
        FinremCaseData caseData = mock(FinremCaseData.class);

        lenient().when(caseDetails.getData()).thenReturn(caseData);
        lenient().when(caseDataService.isRespondentRepresentedByASolicitor(caseData)).thenReturn(isRespRepresented);
        lenient().when(caseData.getRespSolNotificationsEmailConsent()).thenReturn(YesOrNo.forValue(respSolConsent));

        // Act
        boolean result = paperNotificationService.shouldPrintForRespondent(caseDetails);

        // Assert
        assertEquals(expected, result);
    }
}
