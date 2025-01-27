package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.defaultContestedCaseDetails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_ORDER_APPROVED_COVER_LETTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_ORDER_APPROVED_JUDGE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_ORDER_APPROVED_JUDGE_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIDLANDS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIDLANDS_FRC_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTTINGHAM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTTINGHAM_COURTLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.REGION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@ExtendWith(MockitoExtension.class)
class ContestedOrderApprovedLetterServiceTest {

    @InjectMocks
    private ContestedOrderApprovedLetterService contestedOrderApprovedLetterService;

    @Mock
    private DocumentConfiguration documentConfiguration;

    @Mock
    private GenericDocumentService genericDocumentService;

    @Captor
    private ArgumentCaptor<CaseDetails> caseDetailsArgumentCaptor;

    @Mock
    private FinremCaseDetailsMapper mapper;

    @Mock
    private DocumentHelper documentHelper;

    @Test
    void whenContestedApprovedOrderLetterGenerated_thenTemplateVarsPopulatedAndDocumentCreatedAndStoredInCaseDetails() {
        CaseDocument expectedCaseDocument = caseDocument();
        when(genericDocumentService.generateDocument(any(), any(), any(), any())).thenReturn(expectedCaseDocument);

        CaseDetails caseDetails = testCaseDetails();

        LocalDate fixedDate = LocalDate.of(2024, 11, 4);
        try (MockedStatic<LocalDate> mockedLocalDate = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
            mockedLocalDate.when(LocalDate::now).thenReturn(fixedDate);
            when(documentHelper.deepCopy(any(), any())).thenReturn(caseDetails);
            when(documentConfiguration.getContestedOrderApprovedCoverLetterTemplate(caseDetails)).thenReturn("FL-FRM-LET-ENG-HC-00666.docx");
            when(documentConfiguration.getContestedOrderApprovedCoverLetterFileName()).thenReturn("contestedOrderApprovedCoverLetter.pdf");
            when(documentHelper.getApplicantFullName(any(CaseDetails.class))).thenReturn("Contested Applicant Name");
            when(documentHelper.getRespondentFullNameContested(any(CaseDetails.class))).thenReturn("Contested Respondent Name");

            contestedOrderApprovedLetterService.generateAndStoreContestedOrderApprovedLetter(caseDetails, AUTH_TOKEN);

            verify(genericDocumentService).generateDocument(eq(AUTH_TOKEN), caseDetailsArgumentCaptor.capture(),
                eq("FL-FRM-LET-ENG-HC-00666.docx"),
                eq("contestedOrderApprovedCoverLetter.pdf"));

            verifyTemplateVariablesArePopulated();
            assertThat(caseDetails.getData()).extracting(CONTESTED_ORDER_APPROVED_COVER_LETTER).isEqualTo(expectedCaseDocument);
        }
    }

    @Test
    void whenContestedApprovedOrderLetterGenerated_thenTemplateVarsPopulatedAndDocumentCreatedAndStoredInFinremCaseDetails() {
        CaseDocument expectedCaseDocument = caseDocument();
        when(genericDocumentService.generateDocument(any(), any(), any(), any())).thenReturn(expectedCaseDocument);
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseDetails finremCaseDetails = callbackRequest.getCaseDetails();
        CaseDetails caseDetails = testCaseDetails();

        LocalDate fixedDate = LocalDate.of(2024, 11, 4);
        try (MockedStatic<LocalDate> mockedLocalDate = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
            mockedLocalDate.when(LocalDate::now).thenReturn(fixedDate);
            when(documentHelper.deepCopy(any(), any())).thenReturn(caseDetails);
            when(mapper.mapToCaseDetails(finremCaseDetails)).thenReturn(caseDetails);
            when(documentConfiguration.getContestedOrderApprovedCoverLetterTemplate(caseDetails)).thenReturn("FL-FRM-LET-ENG-HC-00666.docx");
            when(documentConfiguration.getContestedOrderApprovedCoverLetterFileName()).thenReturn("contestedOrderApprovedCoverLetter.pdf");
            when(documentHelper.getApplicantFullName(any(CaseDetails.class))).thenReturn("Contested Applicant Name");
            when(documentHelper.getRespondentFullNameContested(any(CaseDetails.class))).thenReturn("Contested Respondent Name");

            contestedOrderApprovedLetterService.generateAndStoreContestedOrderApprovedLetter(finremCaseDetails, AUTH_TOKEN);

            verify(genericDocumentService).generateDocument(eq(AUTH_TOKEN), caseDetailsArgumentCaptor.capture(),
                eq("FL-FRM-LET-ENG-HC-00666.docx"),
                eq("contestedOrderApprovedCoverLetter.pdf"));

            verifyTemplateVariablesArePopulated();
            assertThat(finremCaseDetails.getData().getOrderApprovedCoverLetter()).isEqualTo(expectedCaseDocument);
        }
    }

    @Test
    void shouldPopulateJudgeDetailsWhenJudgeDetailsProvided() {
        CaseDocument expectedCaseDocument = caseDocument();
        when(genericDocumentService.generateDocument(any(), any(), any(), any())).thenReturn(expectedCaseDocument);

        CaseDetails caseDetails = testCaseDetails();
        FinremCaseData finremCaseData = FinremCaseData.builder().build();
        FinremCaseDetails finremCaseDetails = FinremCaseDetails.builder().data(finremCaseData).build();

        LocalDate fixedDate = LocalDate.of(2024, 11, 4);
        try (MockedStatic<LocalDate> mockedLocalDate = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
            mockedLocalDate.when(LocalDate::now).thenReturn(fixedDate);
            when(mapper.mapToCaseDetails(finremCaseDetails)).thenReturn(caseDetails);
            when(documentHelper.deepCopy(any(), any())).thenReturn(caseDetails);
            when(documentConfiguration.getContestedOrderApprovedCoverLetterTemplate(caseDetails)).thenReturn("FL-FRM-LET-ENG-HC-00666.docx");
            when(documentConfiguration.getContestedOrderApprovedCoverLetterFileName()).thenReturn("contestedOrderApprovedCoverLetter.pdf");
            when(documentHelper.getApplicantFullName(any(CaseDetails.class))).thenReturn("Contested Applicant Name");
            when(documentHelper.getRespondentFullNameContested(any(CaseDetails.class))).thenReturn("Contested Respondent Name");

            contestedOrderApprovedLetterService.generateAndStoreContestedOrderApprovedLetter(finremCaseDetails, "District Judge Peter Chapman",
                AUTH_TOKEN);

            verify(genericDocumentService).generateDocument(eq(AUTH_TOKEN), caseDetailsArgumentCaptor.capture(),
                eq("FL-FRM-LET-ENG-HC-00666.docx"),
                eq("contestedOrderApprovedCoverLetter.pdf"));

            verifyTemplateVariablesArePopulatedIfJudgeDetailsProvided();
            assertThat(finremCaseData.getOrderApprovedCoverLetter()).isEqualTo(expectedCaseDocument);
        }
    }

    protected FinremCallbackRequest buildCallbackRequest() {
        return FinremCallbackRequest
            .builder()
            .eventType(EventType.SEND_ORDER)
            .caseDetailsBefore(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(new FinremCaseData()).build())
            .caseDetails(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(new FinremCaseData()).build())
            .build();
    }

    private CaseDetails testCaseDetails() {
        CaseDetails caseDetails = defaultContestedCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();

        caseData.put(APPLICANT_FIRST_MIDDLE_NAME, "Contested Applicant");
        caseData.put(APPLICANT_LAST_NAME, "Name");
        caseData.put(CONTESTED_RESPONDENT_FIRST_MIDDLE_NAME, "Contested Respondent");
        caseData.put(CONTESTED_RESPONDENT_LAST_NAME, "Name");
        caseData.put(REGION, MIDLANDS);
        caseData.put(MIDLANDS_FRC_LIST, NOTTINGHAM);
        caseData.put(NOTTINGHAM_COURTLIST, "FR_s_NottinghamList_1");
        caseData.put(CONTESTED_ORDER_APPROVED_JUDGE_TYPE, "Her Honour");
        caseData.put(CONTESTED_ORDER_APPROVED_JUDGE_NAME, "Judge Contested");

        return caseDetails;
    }

    private void verifyTemplateVariablesArePopulated() {
        Map<String, Object> data = caseDetailsArgumentCaptor.getValue().getData();
        assertThat(data)
            .extracting("ApplicantName", "RespondentName", "Court", "JudgeDetails", "letterDate")
            .containsExactly("Contested Applicant Name", "Contested Respondent Name", "Nottingham County Court and Family Court",
                "Her Honour Judge Contested", "2024-11-04");
    }

    private void verifyTemplateVariablesArePopulatedIfJudgeDetailsProvided() {
        Map<String, Object> data = caseDetailsArgumentCaptor.getValue().getData();
        assertThat(data)
            .extracting("ApplicantName", "RespondentName", "Court", "JudgeDetails", "letterDate")
            .containsExactly("Contested Applicant Name", "Contested Respondent Name", "Nottingham County Court and Family Court",
                "District Judge Peter Chapman", "2024-11-04");
    }
}
