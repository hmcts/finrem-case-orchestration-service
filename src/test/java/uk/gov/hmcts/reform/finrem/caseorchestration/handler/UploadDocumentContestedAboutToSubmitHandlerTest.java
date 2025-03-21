package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCaseDetailsBuilderFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.helper.DocumentWarningsHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadGeneralDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadGeneralDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadGeneralDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NewUploadedDocumentsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.UploadGeneralDocumentsCategoriser;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentchecker.DocumentCheckerService;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogs;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(SpringExtension.class)
class UploadDocumentContestedAboutToSubmitHandlerTest {

    @TestLogs
    private final TestLogger logs = new TestLogger(DocumentWarningsHelper.class);

    @Mock
    private NewUploadedDocumentsService newUploadedDocumentsService;
    @Mock
    private DocumentCheckerService documentCheckerService;
    @Mock
    private UploadGeneralDocumentsCategoriser uploadGeneralDocumentsCategoriser;

    private UploadDocumentContestedAboutToSubmitHandler underTest;

    @BeforeEach
    public void setUpTest() {
        underTest = new UploadDocumentContestedAboutToSubmitHandler(
            new FinremCaseDetailsMapper(new ObjectMapper().registerModule(new JavaTimeModule())),
            new DocumentWarningsHelper(documentCheckerService, newUploadedDocumentsService), uploadGeneralDocumentsCategoriser);
    }

    @Test
    void givenACcdCallbackContestedCase_WhenAnAboutToSubmitEventUploadDocumentContested_thenHandlerCanHandle() {
        assertCanHandle(underTest, CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.UPLOAD_DOCUMENT_CONTESTED);
    }

    @Test
    void givenValidCaseDataWithoutDocumentUploaded_thenNoWarningShouldBeReturned() {
        when(newUploadedDocumentsService.getNewUploadDocuments(any(), any(), any())).thenReturn(List.of());
        when(documentCheckerService.getWarnings(any(), any(), any(), any())).thenReturn(List.of("whatever")); // never reach

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = underTest.handle(
            FinremCallbackRequest.builder()
                .caseDetails(FinremCaseDetailsBuilderFactory.from(Long.valueOf(CASE_ID), CaseType.CONTESTED).build())
                .caseDetailsBefore(FinremCaseDetailsBuilderFactory.from(Long.valueOf(CASE_ID), CaseType.CONTESTED).build()).build(),
            AUTH_TOKEN);
        assertThat(response.getWarnings()).isEmpty();
    }

    @Test
    void givenValidCaseDataWithDocumentUpload_whenUnexpectedExceptionThrownInWarningChecking_thenNotBlockTheSubmission() {
        when(newUploadedDocumentsService.getNewUploadDocuments(any(), any(), any())).thenReturn(List.of(caseDocument()));
        when(documentCheckerService.getWarnings(any(), any(), any(), any())).thenThrow(new RuntimeException("unexpected exception"));

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = underTest.handle(
            FinremCallbackRequest.builder()
                .caseDetails(FinremCaseDetailsBuilderFactory.from(Long.valueOf(CASE_ID), CaseType.CONTESTED).build())
                .caseDetailsBefore(FinremCaseDetailsBuilderFactory.from(Long.valueOf(CASE_ID), CaseType.CONTESTED).build()).build(),
            AUTH_TOKEN);
        assertThat(response.getWarnings()).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void givenValidCaseData_whenWarningIsDetected_thenPopulateWarning(boolean hasWarnings) {
        CaseDocument caseDocument = caseDocument();
        List<String> expectedWarnings = hasWarnings ? List.of("warnings") : List.of();
        when(newUploadedDocumentsService.getNewUploadDocuments(any(), any(), any())).thenReturn(List.of(caseDocument));
        when(documentCheckerService.getWarnings(any(), any(), any(), any())).thenReturn(expectedWarnings);

        FinremCaseDetails finremCaseDetails = FinremCaseDetailsBuilderFactory.from(Long.valueOf(CASE_ID), CaseType.CONTESTED, FinremCaseData.builder()
                .uploadGeneralDocuments(List.of(
                    createGeneralUploadDocumentItem(
                        UploadGeneralDocumentType.LETTER_EMAIL_FROM_RESPONDENT, "New email content",
                        caseDocument, LocalDate.now(), "New Example", "newDocument.filename")
                ))
            )
            .build();
        FinremCaseDetails finremCaseDetailsBefore = FinremCaseDetailsBuilderFactory
            .from(Long.valueOf(CASE_ID), CaseType.CONTESTED, FinremCaseData.builder())
            .build();

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = underTest.handle(
            FinremCallbackRequest.builder()
                .caseDetails(finremCaseDetails)
                .caseDetailsBefore(finremCaseDetailsBefore).build(),
            AUTH_TOKEN);
        assertThat(response.getWarnings()).isEqualTo(expectedWarnings);
        if (hasWarnings) {
            assertThat(logs.getInfos()).containsExactly(format(
                "%s - Number of warnings encountered when uploading document: %s", CASE_ID, 1));
        } else {
            assertThat(logs.getInfos()).isEmpty();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void givenValidCaseData_whenWarningAreDetected_thenPopulateWarnings(boolean hasWarnings) {
        CaseDocument caseDocument = caseDocument();
        when(newUploadedDocumentsService.getNewUploadDocuments(any(), any(), any())).thenReturn(List.of(caseDocument));
        when(documentCheckerService.getWarnings(any(), any(), any(), any())).thenReturn(hasWarnings ? List.of("2warnings", "2warnings", "1warnings")
            : List.of());

        FinremCaseDetails finremCaseDetails = FinremCaseDetailsBuilderFactory.from(Long.valueOf(CASE_ID), CaseType.CONTESTED, FinremCaseData.builder()
                .uploadGeneralDocuments(List.of(
                    createGeneralUploadDocumentItem(
                        UploadGeneralDocumentType.LETTER_EMAIL_FROM_RESPONDENT, "New email content",
                        caseDocument(), LocalDate.now(), "New Example", "newDocument.filename")
                ))
            )
            .build();
        FinremCaseDetails finremCaseDetailsBefore = FinremCaseDetailsBuilderFactory
            .from(Long.valueOf(CASE_ID), CaseType.CONTESTED, FinremCaseData.builder())
            .build();

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = underTest.handle(
            FinremCallbackRequest.builder()
                .caseDetails(finremCaseDetails)
                .caseDetailsBefore(finremCaseDetailsBefore).build(),
            AUTH_TOKEN);
        assertThat(response.getWarnings()).isEqualTo(hasWarnings ? List.of("1warnings", "2warnings") : List.of());
        if (hasWarnings) {
            assertThat(logs.getInfos()).containsExactly(format(
                "%s - Number of warnings encountered when uploading document: %s", CASE_ID, 2));
        } else {
            assertThat(logs.getInfos()).isEmpty();
        }
    }

    @Test
    void givenValidCaseData_whenHandleUploadGeneralDocument_thenSortCollectionByDateAndCategoriserInvoked() {
        CaseDocument caseDocument = caseDocument();
        UploadGeneralDocumentCollection oldDoc = createGeneralUploadDocumentItem(
            UploadGeneralDocumentType.LETTER_EMAIL_FROM_APPLICANT,
            "Old email content", caseDocument, LocalDate.now().minusDays(1),
            "Old Example", "oldDocument.filename");
        UploadGeneralDocumentCollection newDoc = createGeneralUploadDocumentItem(
            UploadGeneralDocumentType.LETTER_EMAIL_FROM_RESPONDENT, "New email content",
            caseDocument, LocalDate.now(), "New Example", "newDocument.filename");

        FinremCaseDetails finremCaseDetails = FinremCaseDetailsBuilderFactory.from(Long.valueOf(CASE_ID), CaseType.CONTESTED, FinremCaseData.builder()
            .uploadGeneralDocuments(List.of(newDoc, oldDoc)))
            .build();
        FinremCaseDetails finremCaseDetailsBefore = FinremCaseDetailsBuilderFactory.from(Long.valueOf(CASE_ID), CaseType.CONTESTED,
                FinremCaseData.builder().uploadGeneralDocuments(List.of(oldDoc)))
            .build();

        List<UploadGeneralDocumentCollection> expectedDocumentIdList = new ArrayList<>();
        expectedDocumentIdList.add(newDoc);
        expectedDocumentIdList.add(oldDoc);

        List<UploadGeneralDocumentCollection> actual = underTest.handle(
            FinremCallbackRequest.builder().caseDetails(finremCaseDetails)
                .caseDetailsBefore(finremCaseDetailsBefore).build(),
            AUTH_TOKEN).getData().getUploadGeneralDocuments();

        assertThat(actual).isEqualTo(expectedDocumentIdList);
        verify(uploadGeneralDocumentsCategoriser).categorise(finremCaseDetails.getData());
    }

    private UploadGeneralDocumentCollection createGeneralUploadDocumentItem(UploadGeneralDocumentType type, String emailContent, CaseDocument link,
                                                                            LocalDate dateAdded, String comment, String fileName) {
        return UploadGeneralDocumentCollection.builder()
            .value(UploadGeneralDocument
                .builder()
                .documentType(type)
                .documentEmailContent(emailContent)
                .documentLink(link)
                .documentDateAdded(dateAdded)
                .documentComment(comment)
                .documentFileName(fileName)
                .build())
            .build();
    }
}
